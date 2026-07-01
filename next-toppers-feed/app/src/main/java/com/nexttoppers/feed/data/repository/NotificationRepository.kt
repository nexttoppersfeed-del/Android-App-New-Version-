package com.nexttoppers.feed.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.WriteBatch
import com.nexttoppers.feed.data.model.AdminNotification
import com.nexttoppers.feed.data.model.NotificationTarget
import com.nexttoppers.feed.data.model.NotificationType
import com.nexttoppers.feed.data.model.NtfNotification
import com.nexttoppers.feed.util.resolveTimestamp
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val col      = firestore.collection("notifications")
    private val histCol  = firestore.collection("adminNotifications")
    private val usersCol = firestore.collection("users")
    private val fcmCol   = firestore.collection("fcmRequests")

    // ── Realtime notification stream for a user ────────────────────────────────
    fun observeNotifications(uid: String): Flow<Result<List<NtfNotification>>> = callbackFlow {
        val listener = col
            .whereEqualTo("userId", uid)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(60)
            .addSnapshotListener { snap, err ->
                if (err != null) { trySend(Result.failure(err)); return@addSnapshotListener }
                val items = snap?.documents?.mapNotNull { doc ->
                    doc.toObject(NtfNotification::class.java)
                        ?.copy(id = doc.id, timestamp = doc.resolveTimestamp("timestamp"))
                } ?: emptyList()
                trySend(Result.success(items))
            }
        awaitClose { listener.remove() }
    }

    // ── Realtime unread count stream ────────────────────────────────────────────
    fun observeUnreadCount(uid: String): Flow<Int> = callbackFlow {
        val listener = col
            .whereEqualTo("userId", uid)
            .whereEqualTo("read", false)
            .addSnapshotListener { snap, _ ->
                trySend(snap?.size() ?: 0)
            }
        awaitClose { listener.remove() }
    }

    // ── Mark single notification as read ───────────────────────────────────────
    suspend fun markAsRead(notificationId: String): Result<Unit> = runCatching {
        col.document(notificationId).update("read", true).await()
    }

    // ── Mark all user notifications as read ────────────────────────────────────
    suspend fun markAllAsRead(uid: String): Result<Unit> = runCatching {
        val unread = col.whereEqualTo("userId", uid)
            .whereEqualTo("read", false)
            .get().await()
        if (unread.isEmpty) return@runCatching
        val batch: WriteBatch = firestore.batch()
        unread.documents.forEach { doc -> batch.update(doc.reference, "read", true) }
        batch.commit().await()
    }

    // ── Delete a notification ──────────────────────────────────────────────────
    suspend fun deleteNotification(notificationId: String): Result<Unit> = runCatching {
        col.document(notificationId).delete().await()
    }

    // ── Create a notification (called from app events) ─────────────────────────
    suspend fun createNotification(
        uid: String,
        type: NotificationType,
        title: String,
        message: String,
        actionRoute: String = "",
        imageUrl: String    = ""
    ): Result<Unit> = runCatching {
        val notification = NtfNotification(
            userId      = uid,
            title       = title,
            message     = message,
            type        = type.name,
            timestamp   = Timestamp.now(),
            read        = false,
            actionRoute = actionRoute,
            imageUrl    = imageUrl
        )
        col.add(notification.toMap()).await()
    }

    // ── Seed sample notifications for demo/test purposes ──────────────────────
    suspend fun seedSampleNotifications(uid: String) {
        val samples = listOf(
            Triple(NotificationType.XP_EARNED,    "XP Earned!",              "You earned 25 XP for your daily login. Keep it up!"),
            Triple(NotificationType.STREAK_REMINDER, "Streak at Risk! 🔥",   "Log in before midnight to keep your streak alive."),
            Triple(NotificationType.ANNOUNCEMENT,  "New Exam Schedule",       "The board has released the new schedule. Check it now."),
            Triple(NotificationType.NEW_RESOURCE,  "New Maths Module Added",  "Chapter 7 – Triangles is now available in Resources."),
            Triple(NotificationType.ACHIEVEMENT_UNLOCKED, "Achievement Unlocked!", "You've completed 5 quizzes. 🏅 Quiz Warrior badge earned!")
        )
        samples.forEach { (type, title, msg) ->
            runCatching { createNotification(uid, type, title, msg) }
        }
    }

    // ════════════════════════════════════════════════════════════════════════════
    // ADMIN NOTIFICATION METHODS
    // ════════════════════════════════════════════════════════════════════════════

    // ── Send notification to ALL users ─────────────────────────────────────────
    suspend fun sendToAll(
        title: String,
        message: String,
        type: NotificationType,
        actionRoute: String = "",
        imageUrl: String    = ""
    ): Result<Int> = runCatching {
        val snap = usersCol.get().await()
        val uids = snap.documents.mapNotNull { it.id.takeIf { id -> id.isNotEmpty() } }
        writeNotificationsForUsers(uids, title, message, type, actionRoute, imageUrl)
        enqueueAdminFcmRequest(
            target          = NotificationTarget.ALL.name,
            title           = title,
            message         = message,
            type            = type,
            imageUrl        = imageUrl,
            deepLink        = actionRoute,
            selectedUserIds = emptyList(),
            topic           = ""
        )
        saveAdminHistory(
            target           = NotificationTarget.ALL,
            title            = title,
            message          = message,
            type             = type,
            imageUrl         = imageUrl,
            deepLink         = actionRoute,
            recipientCount   = uids.size,
            selectedUserIds  = emptyList(),
            topic            = ""
        )
        uids.size
    }

    // ── Send notification to PREMIUM users only ────────────────────────────────
    suspend fun sendToPremium(
        title: String,
        message: String,
        type: NotificationType,
        actionRoute: String = "",
        imageUrl: String    = ""
    ): Result<Int> = runCatching {
        val snap = usersCol.whereEqualTo("isPremium", true).get().await()
        val uids = snap.documents.mapNotNull { it.id.takeIf { id -> id.isNotEmpty() } }
        writeNotificationsForUsers(uids, title, message, type, actionRoute, imageUrl)
        enqueueAdminFcmRequest(
            target          = NotificationTarget.PREMIUM_ONLY.name,
            title           = title,
            message         = message,
            type            = type,
            imageUrl        = imageUrl,
            deepLink        = actionRoute,
            selectedUserIds = emptyList(),
            topic           = ""
        )
        saveAdminHistory(
            target           = NotificationTarget.PREMIUM_ONLY,
            title            = title,
            message          = message,
            type             = type,
            imageUrl         = imageUrl,
            deepLink         = actionRoute,
            recipientCount   = uids.size,
            selectedUserIds  = emptyList(),
            topic            = ""
        )
        uids.size
    }

    // ── Send notification to SELECTED users ────────────────────────────────────
    suspend fun sendToSelected(
        uids: List<String>,
        title: String,
        message: String,
        type: NotificationType,
        actionRoute: String = "",
        imageUrl: String    = ""
    ): Result<Int> = runCatching {
        if (uids.isEmpty()) return@runCatching 0
        writeNotificationsForUsers(uids, title, message, type, actionRoute, imageUrl)
        enqueueAdminFcmRequest(
            target          = NotificationTarget.SELECTED.name,
            title           = title,
            message         = message,
            type            = type,
            imageUrl        = imageUrl,
            deepLink        = actionRoute,
            selectedUserIds = uids,
            topic           = ""
        )
        saveAdminHistory(
            target           = NotificationTarget.SELECTED,
            title            = title,
            message          = message,
            type             = type,
            imageUrl         = imageUrl,
            deepLink         = actionRoute,
            recipientCount   = uids.size,
            selectedUserIds  = uids,
            topic            = ""
        )
        uids.size
    }

    // ── Send notification by TOPIC ─────────────────────────────────────────────
    suspend fun sendByTopic(
        topic: String,
        title: String,
        message: String,
        type: NotificationType,
        actionRoute: String = "",
        imageUrl: String    = ""
    ): Result<Int> = runCatching {
        enqueueAdminFcmRequest(
            target          = NotificationTarget.TOPIC.name,
            title           = title,
            message         = message,
            type            = type,
            imageUrl        = imageUrl,
            deepLink        = actionRoute,
            selectedUserIds = emptyList(),
            topic           = topic
        )
        saveAdminHistory(
            target           = NotificationTarget.TOPIC,
            title            = title,
            message          = message,
            type             = type,
            imageUrl         = imageUrl,
            deepLink         = actionRoute,
            recipientCount   = 0,
            selectedUserIds  = emptyList(),
            topic            = topic
        )
        0
    }

    // ── Admin notification history ─────────────────────────────────────────────
    fun observeAdminNotificationHistory(): Flow<Result<List<AdminNotification>>> = callbackFlow {
        val listener = histCol
            .orderBy("sentAt", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snap, err ->
                if (err != null) { trySend(Result.failure(err)); return@addSnapshotListener }
                val items = snap?.documents?.mapNotNull { doc ->
                    runCatching {
                        AdminNotification(
                            id               = doc.id,
                            title            = doc.getString("title") ?: "",
                            message          = doc.getString("message") ?: "",
                            imageUrl         = doc.getString("imageUrl") ?: "",
                            deepLink         = doc.getString("deepLink") ?: "",
                            target           = doc.getString("target") ?: NotificationTarget.ALL.name,
                            selectedUserIds  = (doc.get("selectedUserIds") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                            topic            = doc.getString("topic") ?: "",
                            notificationType = doc.getString("notificationType") ?: NotificationType.ANNOUNCEMENT.name,
                            sentAt           = doc.resolveTimestamp("sentAt"),
                            sentBy           = doc.getString("sentBy") ?: "",
                            sentByName       = doc.getString("sentByName") ?: "",
                            recipientCount   = (doc.getLong("recipientCount") ?: 0L).toInt()
                        )
                    }.getOrNull()
                } ?: emptyList()
                trySend(Result.success(items))
            }
        awaitClose { listener.remove() }
    }

    // ── Delete an admin notification history entry ─────────────────────────────
    suspend fun deleteAdminNotification(historyId: String): Result<Unit> = runCatching {
        histCol.document(historyId).delete().await()
    }

    // ── Resend an admin notification ───────────────────────────────────────────
    suspend fun resendAdminNotification(adminNotif: AdminNotification): Result<Int> {
        val type = NotificationType.values().firstOrNull { it.name == adminNotif.notificationType }
            ?: NotificationType.ANNOUNCEMENT
        return when (adminNotif.targetEnum) {
            NotificationTarget.ALL          -> sendToAll(adminNotif.title, adminNotif.message, type, adminNotif.deepLink, adminNotif.imageUrl)
            NotificationTarget.PREMIUM_ONLY -> sendToPremium(adminNotif.title, adminNotif.message, type, adminNotif.deepLink, adminNotif.imageUrl)
            NotificationTarget.SELECTED     -> sendToSelected(adminNotif.selectedUserIds, adminNotif.title, adminNotif.message, type, adminNotif.deepLink, adminNotif.imageUrl)
            NotificationTarget.TOPIC        -> sendByTopic(adminNotif.topic, adminNotif.title, adminNotif.message, type, adminNotif.deepLink, adminNotif.imageUrl)
        }
    }

    // ── Internal: write in-app notifications for a list of users ──────────────
    private suspend fun writeNotificationsForUsers(
        uids: List<String>,
        title: String,
        message: String,
        type: NotificationType,
        actionRoute: String,
        imageUrl: String
    ) {
        val chunks = uids.chunked(400)
        for (chunk in chunks) {
            val batch = firestore.batch()
            chunk.forEach { uid ->
                val ref = col.document()
                val notif = NtfNotification(
                    userId      = uid,
                    title       = title,
                    message     = message,
                    type        = type.name,
                    timestamp   = Timestamp.now(),
                    read        = false,
                    actionRoute = actionRoute,
                    imageUrl    = imageUrl
                )
                batch.set(ref, notif.toMap())
            }
            batch.commit().await()
        }
    }

    // ── Internal: queue FCM push request for Cloud Functions ──────────────────
    private suspend fun enqueueAdminFcmRequest(
        target: String,
        title: String,
        message: String,
        type: NotificationType,
        imageUrl: String,
        deepLink: String,
        selectedUserIds: List<String>,
        topic: String
    ) {
        val senderUid  = auth.currentUser?.uid ?: ""
        val requestMap = mapOf(
            "target"          to target,
            "title"           to title,
            "message"         to message,
            "type"            to type.name,
            "imageUrl"        to imageUrl,
            "deepLink"        to deepLink,
            "selectedUserIds" to selectedUserIds,
            "topic"           to topic,
            "sentBy"          to senderUid,
            "createdAt"       to Timestamp.now(),
            "status"          to "pending"
        )
        runCatching { fcmCol.add(requestMap).await() }
    }

    // ── Internal: save admin notification history ──────────────────────────────
    private suspend fun saveAdminHistory(
        target: NotificationTarget,
        title: String,
        message: String,
        type: NotificationType,
        imageUrl: String,
        deepLink: String,
        recipientCount: Int,
        selectedUserIds: List<String>,
        topic: String
    ) {
        val senderUid  = auth.currentUser?.uid ?: ""
        val senderName = auth.currentUser?.displayName ?: ""
        val record = AdminNotification(
            title            = title,
            message          = message,
            imageUrl         = imageUrl,
            deepLink         = deepLink,
            target           = target.name,
            selectedUserIds  = selectedUserIds,
            topic            = topic,
            notificationType = type.name,
            sentAt           = Timestamp.now(),
            sentBy           = senderUid,
            sentByName       = senderName,
            recipientCount   = recipientCount
        )
        runCatching { histCol.add(record.toMap()).await() }
    }
}
