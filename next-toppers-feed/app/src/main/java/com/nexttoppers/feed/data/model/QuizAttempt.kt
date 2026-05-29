package com.nexttoppers.feed.data.model

import com.google.firebase.Timestamp

data class QuizAttempt(
    val id: String = "",
    val quizId: String = "",
    val quizTitle: String = "",
    val subject: String = "",
    val score: Int = 0,
    val totalQuestions: Int = 0,
    val xpEarned: Int = 0,
    val timeTaken: Int = 0,           // seconds
    val completedAt: Timestamp = Timestamp.now(),
    val answers: Map<String, Int> = emptyMap()   // "questionIndex" -> selected option index
) {
    val accuracy: Float get() = if (totalQuestions > 0) score.toFloat() / totalQuestions else 0f
    val accuracyPercent: Int get() = (accuracy * 100).toInt()

    fun timeTakenLabel(): String {
        val m = timeTaken / 60
        val s = timeTaken % 60
        return if (m > 0) "${m}m ${s}s" else "${s}s"
    }
}
