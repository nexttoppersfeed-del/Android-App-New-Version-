package com.nexttoppers.feed.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude

enum class NotificationTarget(val label: String) {
    ALL          ("All Users"),
    PREMIUM_ONLY ("Premium Users Only"),
    SELECTED     ("Selected Users"),
    TOPIC        ("By Topic")
}

data class AdminNotification(
    val id: String                   = "",
    val title: String                = "",
    val message: String              = "",
    val imageUrl: String             = "",
    val deepLink: String             = "",
    val target: String               = NotificationTarget.ALL.name,
    val selectedUserIds: List<String>= emptyList(),
    val topic: String                = "",
    val notificationType: String     = NotificationType.ANNOUNCEMENT.name,
    @get:Exclude @field:Exclude
    val sentAt: Timestamp            = Timestamp.now(),
    val sentBy: String               = "",
    val sentByName: String           = "",
    val recipientCount: Int          = 0
) {
    val targetEnum: NotificationTarget
        get() = NotificationTarget.values().firstOrNull { it.name == target }
            ?: NotificationTarget.ALL

    fun toMap(): Map<String, Any?> = mapOf(
        "title"            to title,
        "message"          to message,
        "imageUrl"         to imageUrl,
        "deepLink"         to deepLink,
        "target"           to target,
        "selectedUserIds"  to selectedUserIds,
        "topic"            to topic,
        "notificationType" to notificationType,
        "sentAt"           to sentAt,
        "sentBy"           to sentBy,
        "sentByName"       to sentByName,
        "recipientCount"   to recipientCount
    )
}
