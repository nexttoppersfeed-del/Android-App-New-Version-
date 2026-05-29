package com.nexttoppers.feed.data.model

import com.google.firebase.Timestamp

// ── Notification type enum ─────────────────────────────────────────────────────
enum class NotificationType(val emoji: String, val label: String) {
    XP_EARNED          ("⚡", "XP Earned"),
    LEVEL_UP           ("🎉", "Level Up"),
    STREAK_REMINDER    ("🔥", "Streak Reminder"),
    RANK_CHANGE        ("🏆", "Rank Change"),
    PREMIUM_EXPIRY     ("👑", "Premium Expiry"),
    NEW_RESOURCE       ("📚", "New Resource"),
    QUIZ_REMINDER      ("🎯", "Quiz Reminder"),
    ANNOUNCEMENT       ("📢", "Announcement"),
    ACHIEVEMENT_UNLOCKED("🏅", "Achievement Unlocked"),
    SYSTEM             ("🔔", "System")
}

// ── Notification Firestore model ───────────────────────────────────────────────
data class NtfNotification(
    val id: String          = "",
    val userId: String      = "",
    val title: String       = "",
    val message: String     = "",
    // stored as String for Firestore compat; use notificationType computed property
    val type: String        = NotificationType.SYSTEM.name,
    val timestamp: Timestamp = Timestamp.now(),
    val read: Boolean       = false,
    val actionRoute: String = "",
    val imageUrl: String    = ""
) {
    val notificationType: NotificationType
        get() = NotificationType.values().firstOrNull { it.name == type }
            ?: NotificationType.SYSTEM

    /** True if notification was created today (same calendar day). */
    val isToday: Boolean
        get() {
            val now    = System.currentTimeMillis()
            val ts     = timestamp.toDate().time
            val oneDayMs = 24L * 60 * 60 * 1000
            return (now - ts) < oneDayMs
        }

    /** True if within the past 7 days. */
    val isThisWeek: Boolean
        get() {
            val now     = System.currentTimeMillis()
            val ts      = timestamp.toDate().time
            val weekMs  = 7L * 24 * 60 * 60 * 1000
            return (now - ts) < weekMs
        }

    fun toMap(): Map<String, Any?> = mapOf(
        "userId"      to userId,
        "title"       to title,
        "message"     to message,
        "type"        to type,
        "timestamp"   to timestamp,
        "read"        to read,
        "actionRoute" to actionRoute,
        "imageUrl"    to imageUrl
    )
}

// ── Grouped notification model for UI ─────────────────────────────────────────
data class NotificationGroup(
    val label: String,
    val notifications: List<NtfNotification>
)

// ── Notification filter chips ──────────────────────────────────────────────────
enum class NotificationFilter(val label: String) {
    ALL          ("All"),
    UNREAD       ("Unread"),
    XP           ("XP & Levels"),
    ACHIEVEMENTS ("Achievements"),
    ANNOUNCEMENTS("Announcements"),
    SYSTEM       ("System")
}
