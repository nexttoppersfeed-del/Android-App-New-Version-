package com.nexttoppers.feed.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude

data class NtfTest(
    val id: String = "",
    val title: String = "",
    val subject: String = "",
    val description: String = "",
    val questionCount: Int = 0,
    // website field: "duration" (minutes)
    val timeLimit: Int = 30,
    // F06/F07: website uses "isPremium" — fixed in TestRepository mapper
    val isPremium: Boolean = false,
    val active: Boolean = true,
    val createdAt: Timestamp = Timestamp.now(),
    val category: String = "",
    val difficulty: String = "MEDIUM",
    val totalMarks: Int = 0,
    val passMarks: Int = 0,
    val tags: List<String> = emptyList()
) {
    val percentage: Int get() = 0
}

data class TestAttempt(
    val id: String = "",
    // F09: match website's testAttempts schema exactly
    val userId: String = "",
    val testId: String = "",
    val score: Int = 0,
    val totalMarks: Int = 0,
    val totalQuestions: Int = 0,
    val correctAnswers: Int = 0,
    val wrongAnswers: Int = 0,
    val skipped: Int = 0,
    val timeTaken: Int = 0,
    val answers: Map<String, String> = emptyMap(),
    // Legacy docs may store as String; excluded so toObject() never crashes.
    // Callers resolve via doc.resolveTimestamp("completedAt").
    @get:Exclude @field:Exclude
    val completedAt: Timestamp = Timestamp.now()
) {
    // keep maxScore as a computed alias so existing UI doesn't break
    val maxScore: Int get() = totalMarks
    val timeTakenSeconds: Int get() = timeTaken

    val percentage: Int
        get() = if (totalMarks > 0) (score * 100 / totalMarks) else 0

    val isPassed: Boolean
        get() = score >= (totalMarks / 2)

    fun toMap(): Map<String, Any?> = mapOf(
        "userId"          to userId,
        "testId"          to testId,
        "score"           to score,
        "totalMarks"      to totalMarks,
        "totalQuestions"  to totalQuestions,
        "correctAnswers"  to correctAnswers,
        "wrongAnswers"    to wrongAnswers,
        "skipped"         to skipped,
        "timeTaken"       to timeTaken,
        "answers"         to answers,
        "completedAt"     to completedAt
    )
}
