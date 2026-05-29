package com.nexttoppers.feed.data.model

import com.google.firebase.Timestamp

data class NtfTest(
    val id: String = "",
    val title: String = "",
    val subject: String = "",
    val description: String = "",
    val questionCount: Int = 0,
    val timeLimit: Int = 30,
    val premium: Boolean = false,
    val createdAt: Timestamp = Timestamp.now(),
    val category: String = "",
    val difficulty: String = "MEDIUM",
    val totalMarks: Int = 0,
    val passMarks: Int = 0,
    val tags: List<String> = emptyList()
)

data class TestAttempt(
    val id: String = "",
    val userId: String = "",
    val testId: String = "",
    val score: Int = 0,
    val maxScore: Int = 0,
    val correctAnswers: Int = 0,
    val totalQuestions: Int = 0,
    val timeTakenSeconds: Int = 0,
    val completedAt: Timestamp = Timestamp.now()
) {
    val percentage: Int
        get() = if (maxScore > 0) (score * 100 / maxScore) else 0

    val isPassed: Boolean
        get() = score >= (maxScore / 2)
}
