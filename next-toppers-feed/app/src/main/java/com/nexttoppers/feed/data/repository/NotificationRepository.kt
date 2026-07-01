package com.nexttoppers.feed.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.WriteBatch
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
    private val firestore: FirebaseFirestore
) {
    private val col = firestore.collection("notifications")

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
}
