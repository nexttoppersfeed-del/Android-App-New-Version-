package com.nexttoppers.feed.data.model

import com.google.firebase.Timestamp

// ── Activity type enum ─────────────────────────────────────────────────────────
enum class ActivityType(val emoji: String, val label: String) {
    QUIZ_COMPLETED      ("🎯", "Quiz Completed"),
    XP_EARNED           ("⚡", "XP Earned"),
    RANK_ACHIEVED       ("🏆", "Rank Achieved"),
    ACHIEVEMENT_UNLOCKED("🏅", "Achievement Unlocked"),
    STREAK_MAINTAINED   ("🔥", "Streak Maintained"),
    RESOURCE_OPENED     ("📚", "Resource Opened"),
    PREMIUM_ACTIVATED   ("👑", "Premium Activated"),
    LEVEL_UP            ("⬆️", "Level Up")
}

// ── Activity feed item Firestore model ─────────────────────────────────────────
data class ActivityFeedItem(
    val id: String                   = "",
    val userId: String               = "",
    val username: String             = "",
    val photoUrl: String             = "",
    val type: String                 = ActivityType.XP_EARNED.name,
    val description: String          = "",
    val xpEarned: Long               = 0L,
    val timestamp: Timestamp         = Timestamp.now(),
    val metadata: Map<String, String> = emptyMap()
) {
    val activityType: ActivityType
        get() = ActivityType.values().firstOrNull { it.name == type }
            ?: ActivityType.XP_EARNED

    fun toMap(): Map<String, Any?> = mapOf(
        "userId"      to userId,
        "username"    to username,
        "photoUrl"    to photoUrl,
        "type"        to type,
        "description" to description,
        "xpEarned"    to xpEarned,
        "timestamp"   to timestamp,
        "metadata"    to metadata
    )
}

// ── Feed tab types ─────────────────────────────────────────────────────────────
enum class FeedTab(val label: String) {
    PERSONAL ("My Activity"),
    GLOBAL   ("Global Feed"),
    FRIENDS  ("Friends")
}
