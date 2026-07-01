package com.nexttoppers.feed.ui.quiz

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.nexttoppers.feed.data.model.Question
import com.nexttoppers.feed.data.model.Quiz
import com.nexttoppers.feed.data.model.TestAttempt
import com.nexttoppers.feed.data.repository.QuizRepository
import com.nexttoppers.feed.data.repository.XpRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class QuizPlayerUiState {
    object Loading : QuizPlayerUiState()
    data class Ready(val quiz: Quiz, val questions: List<Question>) : QuizPlayerUiState()
    data class Error(val message: String) : QuizPlayerUiState()
    object Submitted : QuizPlayerUiState()
}

@HiltViewModel
class QuizPlayerViewModel @Inject constructor(
    private val quizRepository: QuizRepository,
    private val xpRepository: XpRepository,
    private val auth: FirebaseAuth,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val quizId: String = savedStateHandle["quizId"] ?: ""

    private val _uiState = MutableStateFlow<QuizPlayerUiState>(QuizPlayerUiState.Loading)
    val uiState: StateFlow<QuizPlayerUiState> = _uiState

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex

    private val _answers = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val answers: StateFlow<Map<Int, Int>> = _answers

    private val _timeRemaining = MutableStateFlow(0)
    val timeRemaining: StateFlow<Int> = _timeRemaining

    private val _showSubmitDialog = MutableStateFlow(false)
    val showSubmitDialog: StateFlow<Boolean> = _showSubmitDialog

    // Stores the completed result for navigation
    private val _result = MutableStateFlow<QuizResult?>(null)
    val result: StateFlow<QuizResult?> = _result

    private var timerJob: Job? = null
    private var startTime: Long = 0L

    init { loadQuiz() }

    private fun loadQuiz() {
        viewModelScope.launch {
            quizRepository.getById(quizId)
                .onSuccess { quiz ->
                    quizRepository.getQuestions(quizId)
                        .onSuccess { questions ->
                            _uiState.value = QuizPlayerUiState.Ready(quiz, questions)
                            _timeRemaining.value = quiz.durationSeconds()
                            startTime = System.currentTimeMillis()
                            startTimer(quiz.durationSeconds())
                        }
                        .onFailure { _uiState.value = QuizPlayerUiState.Error(it.message ?: "Failed to load questions") }
                }
                .onFailure { _uiState.value = QuizPlayerUiState.Error(it.message ?: "Quiz not found") }
        }
    }

    private fun startTimer(totalSeconds: Int) {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            for (remaining in totalSeconds downTo 0) {
                _timeRemaining.value = remaining
                if (remaining == 0) { autoSubmit(); break }
                delay(1000L)
            }
        }
    }

    fun selectAnswer(questionIndex: Int, optionIndex: Int) {
        _answers.value = _answers.value.toMutableMap().also { it[questionIndex] = optionIndex }
    }

    fun goToNext() {
        val ready = _uiState.value as? QuizPlayerUiState.Ready ?: return
        if (_currentIndex.value < ready.questions.size - 1)
            _currentIndex.value++
    }

    fun goToPrev() {
        if (_currentIndex.value > 0) _currentIndex.value--
    }

    fun goToQuestion(index: Int) { _currentIndex.value = index }

    fun requestSubmit() { _showSubmitDialog.value = true }
    fun dismissSubmitDialog() { _showSubmitDialog.value = false }

    fun confirmSubmit() {
        _showSubmitDialog.value = false
        submit()
    }

    private fun autoSubmit() { submit() }

    private fun submit() {
        timerJob?.cancel()
        val ready = _uiState.value as? QuizPlayerUiState.Ready ?: return
        val quiz      = ready.quiz
        val questions = ready.questions
        val elapsed   = ((System.currentTimeMillis() - startTime) / 1000).toInt()
            .coerceAtMost(quiz.durationSeconds())

        val score = questions.indices.count { idx ->
            _answers.value[idx] == questions[idx].correctAnswerIndex
        }
        val accuracy = if (questions.isNotEmpty()) score.toFloat() / questions.size else 0f
        val xpEarned = when {
            accuracy >= 0.8f -> quiz.xpReward
            accuracy >= 0.5f -> (quiz.xpReward * 0.6f).toInt()
            else             -> (quiz.xpReward * 0.25f).toInt()
        }

        val savedAnswers = _answers.value.entries
            .associate { (k, v) -> k.toString() to (v?.toString() ?: "") }

        _result.value = QuizResult(
            quizId    = quizId,
            quizTitle = quiz.title,
            score     = score,
            total     = questions.size,
            xpEarned  = xpEarned,
            timeTaken = elapsed,
            questions = questions,
            answers   = _answers.value
        )

        viewModelScope.launch {
            val uid = auth.currentUser?.uid ?: return@launch
            val attempt = TestAttempt(
                userId         = uid,
                testId         = quizId,
                score          = score,
                totalMarks     = questions.size,
                totalQuestions = questions.size,
                correctAnswers = score,
                wrongAnswers   = questions.size - score,
                skipped        = 0,
                timeTaken      = elapsed,
                answers        = savedAnswers
            )
            xpRepository.awardXp(uid, xpEarned)
            xpRepository.saveAttempt(uid, attempt)
            quizRepository.incrementAttempts(quizId)
        }

        _uiState.value = QuizPlayerUiState.Submitted
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}

data class QuizResult(
    val quizId: String,
    val quizTitle: String,
    val score: Int,
    val total: Int,
    val xpEarned: Int,
    val timeTaken: Int,
    val questions: List<Question>,
    val answers: Map<Int, Int>   // questionIndex → selected option index
) {
    val accuracy: Float get() = if (total > 0) score.toFloat() / total else 0f
    val accuracyPercent: Int get() = (accuracy * 100).toInt()
    val wrong: Int get() = total - score - unanswered
    val unanswered: Int get() = total - answers.size
    fun timeTakenLabel(): String {
        val m = timeTaken / 60; val s = timeTaken % 60
        return if (m > 0) "${m}m ${s}s" else "${s}s"
    }
    fun motivationalMessage(): String = when {
        accuracyPercent >= 90 -> "Outstanding! You're a topper! 🏆"
        accuracyPercent >= 75 -> "Excellent work! Keep it up! 🌟"
        accuracyPercent >= 60 -> "Good job! A little more practice! 💪"
        accuracyPercent >= 40 -> "Not bad! Review the answers below 📖"
        else                  -> "Keep practicing! Every attempt counts 🎯"
    }
}
