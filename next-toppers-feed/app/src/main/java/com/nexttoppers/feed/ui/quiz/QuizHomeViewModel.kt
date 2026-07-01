package com.nexttoppers.feed.ui.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.nexttoppers.feed.data.model.Quiz
import com.nexttoppers.feed.data.model.TestAttempt
import com.nexttoppers.feed.data.model.ResourceSubject
import com.nexttoppers.feed.data.repository.QuizRepository
import com.nexttoppers.feed.data.repository.XpRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class QuizHomeData(
    val featured: List<Quiz>,
    val recent: List<Quiz>,
    val all: List<Quiz>,
    val history: List<TestAttempt>
)

sealed class QuizHomeUiState {
    object Loading : QuizHomeUiState()
    data class Success(val data: QuizHomeData) : QuizHomeUiState()
    data class Error(val message: String) : QuizHomeUiState()
}

@HiltViewModel
class QuizHomeViewModel @Inject constructor(
    private val quizRepository: QuizRepository,
    private val xpRepository: XpRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow<QuizHomeUiState>(QuizHomeUiState.Loading)
    val uiState: StateFlow<QuizHomeUiState> = _uiState

    private val _selectedSubject = MutableStateFlow<ResourceSubject?>(null)
    val selectedSubject: StateFlow<ResourceSubject?> = _selectedSubject

    private val _selectedDifficulty = MutableStateFlow<String?>(null)
    val selectedDifficulty: StateFlow<String?> = _selectedDifficulty

    private var allQuizzes: List<Quiz> = emptyList()
    private var history: List<TestAttempt> = emptyList()

    init {
        load()
        observeAll()
    }

    private fun load() {
        viewModelScope.launch {
            try {
                val featured = quizRepository.getFeatured(5).getOrDefault(emptyList())
                val recent   = quizRepository.getRecent(8).getOrDefault(emptyList())
                val uid      = auth.currentUser?.uid ?: ""
                history = if (uid.isNotEmpty())
                    xpRepository.getHistory(uid, 20).getOrDefault(emptyList())
                else emptyList()

                allQuizzes = recent
                _uiState.value = QuizHomeUiState.Success(
                    QuizHomeData(featured = featured, recent = recent, all = recent, history = history)
                )
            } catch (e: Exception) {
                _uiState.value = QuizHomeUiState.Error(e.message ?: "Failed to load")
            }
        }
    }

    private fun observeAll() {
        viewModelScope.launch {
            quizRepository.observeAll().collect { result ->
                result.onSuccess { quizzes ->
                    allQuizzes = quizzes
                    applyFilters(quizzes)
                }
            }
        }
    }

    fun selectSubject(subject: ResourceSubject?) {
        _selectedSubject.value = subject
        applyFilters(allQuizzes)
    }

    fun selectDifficulty(difficulty: String?) {
        _selectedDifficulty.value = difficulty
        applyFilters(allQuizzes)
    }

    private fun applyFilters(quizzes: List<Quiz>) {
        val current = (_uiState.value as? QuizHomeUiState.Success)?.data ?: return
        var filtered = quizzes
        _selectedSubject.value?.let { s -> filtered = filtered.filter { it.subject.equals(s.name, ignoreCase = true) } }
        _selectedDifficulty.value?.let { d -> filtered = filtered.filter { it.difficulty.equals(d, ignoreCase = true) } }
        _uiState.value = QuizHomeUiState.Success(current.copy(all = filtered))
    }

    fun isAttempted(quizId: String): Boolean =
        history.any { it.quizId == quizId }

    fun refresh() { load() }
}
