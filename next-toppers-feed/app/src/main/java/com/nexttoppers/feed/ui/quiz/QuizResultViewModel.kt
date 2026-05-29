package com.nexttoppers.feed.ui.quiz

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.nexttoppers.feed.data.model.QuizAttempt
import com.nexttoppers.feed.data.repository.QuizRepository
import com.nexttoppers.feed.data.repository.XpRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.net.URLDecoder
import javax.inject.Inject

@HiltViewModel
class QuizResultViewModel @Inject constructor(
    private val quizRepository: QuizRepository,
    private val xpRepository: XpRepository,
    private val auth: FirebaseAuth,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val quizId: String =
        savedStateHandle.get<String>("quizId") ?: ""

    val score: Int =
        savedStateHandle.get<String>("score")
            ?.toIntOrNull() ?: 0

    val total: Int =
        savedStateHandle.get<String>("total")
            ?.toIntOrNull() ?: 0

    val timeTaken: Int =
        savedStateHandle.get<String>("timeTaken")
            ?.toIntOrNull() ?: 0

    val xpEarned: Int =
        savedStateHandle.get<String>("xpEarned")
            ?.toIntOrNull() ?: 0

    val quizTitle: String =
        URLDecoder.decode(
            savedStateHandle.get<String>("title") ?: "",
            "UTF-8"
        )

    val accuracy: Float
        get() = if (total > 0) {
            score.toFloat() / total.toFloat()
        } else {
            0f
        }

    val accuracyPercent: Int
        get() = (accuracy * 100).toInt()

    // ─────────────────────────────────────────────
    // Quiz history
    // ─────────────────────────────────────────────

    val history: StateFlow<List<QuizAttempt>> =
        if (auth.currentUser?.uid != null) {

            xpRepository.observeHistory(
                auth.currentUser!!.uid
            ).map { result ->

                result.getOrElse {
                    emptyList<QuizAttempt>()
                }

            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

        } else {

            MutableStateFlow<List<QuizAttempt>>(
                emptyList()
            )
        }

    // ─────────────────────────────────────────────
    // Motivational message
    // ─────────────────────────────────────────────

    fun motivationalMessage(): String {

        return when {

            accuracyPercent >= 90 ->
                "Outstanding! You're a topper! 🏆"

            accuracyPercent >= 75 ->
                "Excellent work! Keep it up! 🌟"

            accuracyPercent >= 60 ->
                "Good job! A little more practice! 💪"

            accuracyPercent >= 40 ->
                "Not bad! Review your mistakes! 📖"

            else ->
                "Keep practicing! Every attempt counts 🎯"
        }
    }
}
