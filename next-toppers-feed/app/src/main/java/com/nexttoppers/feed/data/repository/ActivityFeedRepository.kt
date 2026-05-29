package com.nexttoppers.feed.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.nexttoppers.feed.data.model.ActivityFeedItem
import com.nexttoppers.feed.data.model.ActivityType
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActivityFeedRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val col = firestore.collection("activityFeed")

    // ── Personal activity feed (user's own actions) ─────────────────────────────
    fun observePersonalFeed(uid: String): Flow<Result<List<ActivityFeedItem>>> = callbackFlow {
        val listener = col
            .whereEqualTo("userId", uid)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snap, err ->
                if (err != null) { trySend(Result.failure(err)); return@addSnapshotListener }
                val items = snap?.documents?.mapNotNull { doc ->
                    doc.toObject(ActivityFeedItem::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(Result.success(items))
            }
        awaitClose { listener.remove() }
    }

    // ── Global activity feed (all users, recent actions) ──────────────────────
    fun observeGlobalFeed(): Flow<Result<List<ActivityFeedItem>>> = callbackFlow {
        val listener = col
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snap, err ->
                if (err != null) { trySend(Result.failure(err)); return@addSnapshotListener }
                val items = snap?.documents?.mapNotNull { doc ->
                    doc.toObject(ActivityFeedItem::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(Result.success(items))
            }
        awaitClose { listener.remove() }
    }

    // ── Write an activity item ─────────────────────────────────────────────────
    suspend fun addActivity(item: ActivityFeedItem): Result<Unit> = runCatching {
        col.add(item.toMap()).await()
    }

    // ── Convenience builder for common activity types ──────────────────────────
    suspend fun recordQuizCompleted(uid: String, username: String, xp: Long, quizTitle: String) =
        addActivity(ActivityFeedItem(
            userId      = uid,
            username    = username,
            type        = ActivityType.QUIZ_COMPLETED.name,
            description = "$username completed \"$quizTitle\" and earned $xp XP",
            xpEarned    = xp,
            timestamp   = Timestamp.now()
        ))

    suspend fun recordLevelUp(uid: String, username: String, level: Int) =
        addActivity(ActivityFeedItem(
            userId      = uid,
            username    = username,
            type        = ActivityType.LEVEL_UP.name,
            description = "$username reached Level $level! 🎉",
            timestamp   = Timestamp.now(),
            metadata    = mapOf("level" to level.toString())
        ))

    suspend fun recordStreakMilestone(uid: String, username: String, streak: Int) =
        addActivity(ActivityFeedItem(
            userId      = uid,
            username    = username,
            type        = ActivityType.STREAK_MAINTAINED.name,
            description = "$username is on a $streak-day streak! 🔥",
            timestamp   = Timestamp.now(),
            metadata    = mapOf("streak" to streak.toString())
        ))

    suspend fun recordAchievementUnlocked(uid: String, username: String, achievement: String) =
        addActivity(ActivityFeedItem(
            userId      = uid,
            username    = username,
            type        = ActivityType.ACHIEVEMENT_UNLOCKED.name,
            description = "$username unlocked \"$achievement\" 🏅",
            timestamp   = Timestamp.now(),
            metadata    = mapOf("achievement" to achievement)
        ))

    suspend fun recordPremiumActivated(uid: String, username: String, planName: String) =
        addActivity(ActivityFeedItem(
            userId      = uid,
            username    = username,
            type        = ActivityType.PREMIUM_ACTIVATED.name,
            description = "$username just went $planName Premium! 👑",
            timestamp   = Timestamp.now()
        ))
}
