package com.nexttoppers.feed.data.model

import com.google.firebase.Timestamp

enum class QuizDifficulty(val label: String, val emoji: String) {
    EASY("Easy", "🟢"),
    MEDIUM("Medium", "🟡"),
    HARD("Hard", "🔴")
}

data class Quiz(
    val id: String = "",
    val title: String = "",
    val subject: String = "",
    val chapter: String = "",
    val duration: Int = 10,          // minutes
    val premium: Boolean = false,
    val totalQuestions: Int = 0,
    val createdAt: Timestamp = Timestamp.now(),
    val xpReward: Int = 50,
    val difficulty: String = "MEDIUM",
    val description: String = "",
    val attempts: Long = 0L,
    val tags: List<String> = emptyList()
) {
    fun difficultyEnum(): QuizDifficulty =
        QuizDifficulty.values().firstOrNull { it.name.equals(difficulty, ignoreCase = true) }
            ?: QuizDifficulty.MEDIUM

    fun subjectEnum(): ResourceSubject? =
        ResourceSubject.values().firstOrNull { it.name.equals(subject, ignoreCase = true) }

    fun durationSeconds() = duration * 60
}
