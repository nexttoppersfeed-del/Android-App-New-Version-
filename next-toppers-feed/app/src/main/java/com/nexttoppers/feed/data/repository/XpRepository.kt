package com.nexttoppers.feed.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.nexttoppers.feed.data.model.QuizAttempt
import com.nexttoppers.feed.util.LevelUtils
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class XpRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private fun userDoc(uid: String) = firestore.collection("users").document(uid)
    private fun historyCol(uid: String) = userDoc(uid).collection("quizHistory")

    // ── XP constants ──────────────────────────────────────────────────────────
    companion object {
        const val XP_DAILY_LOGIN    = 25
        const val XP_RESOURCE_OPEN  = 10
        const val XP_STREAK_BONUS   = 5
        const val XP_QUIZ_BASE      = 10
    }

    // ── Award XP from quiz + update level, quizzesCompleted, streak ───────────
    suspend fun awardXp(uid: String, xp: Int): Result<Unit> = runCatching {
        val doc      = userDoc(uid).get().await()
        val currentXp = (doc.getLong("xp") ?: 0L)
        val newXp     = currentXp + xp
        val newLevel  = LevelUtils.levelForXp(newXp)

        val updates = mutableMapOf<String, Any>(
            "xp"               to FieldValue.increment(xp.toLong()),
            "level"            to newLevel,
            "quizzesCompleted" to FieldValue.increment(1L),
            "lastActive"       to Timestamp.now()
        )

        val streakResult = calculateStreak(doc)
        updates["streak"]   = streakResult.newStreak
        updates["lastSeen"] = Timestamp.now()

        userDoc(uid).update(updates).await()
    }

    // ── Award XP for opening a resource ───────────────────────────────────────
    suspend fun awardResourceXp(uid: String): Result<Unit> = runCatching {
        val doc      = userDoc(uid).get().await()
        val currentXp = (doc.getLong("xp") ?: 0L)
        val newXp     = currentXp + XP_RESOURCE_OPEN
        val newLevel  = LevelUtils.levelForXp(newXp)

        userDoc(uid).update(
            mapOf(
                "xp"              to FieldValue.increment(XP_RESOURCE_OPEN.toLong()),
                "level"           to newLevel,
                "resourcesOpened" to FieldValue.increment(1L),
                "lastActive"      to Timestamp.now()
            )
        ).await()
    }

    // ── Award daily login XP (25 XP, once per day) ───────────────────────────
    suspend fun awardDailyLoginXp(uid: String): Result<Boolean> = runCatching {
        val doc       = userDoc(uid).get().await()
        val lastActive = doc.getTimestamp("lastActive")?.toDate()

        if (lastActive != null && isSameDay(lastActive, Date())) {
            return@runCatching false
        }

        val currentXp = (doc.getLong("xp") ?: 0L)
        val newXp     = currentXp + XP_DAILY_LOGIN
        val newLevel  = LevelUtils.levelForXp(newXp)
        val streakResult = calculateStreak(doc)

        userDoc(uid).update(
            mapOf(
                "xp"         to FieldValue.increment(XP_DAILY_LOGIN.toLong()),
                "level"      to newLevel,
                "streak"     to streakResult.newStreak,
                "lastActive" to Timestamp.now(),
                "lastSeen"   to Timestamp.now()
            )
        ).await()
        true
    }

    // ── Save quiz attempt to history ──────────────────────────────────────────
    suspend fun saveAttempt(uid: String, attempt: QuizAttempt): Result<Unit> = runCatching {
        historyCol(uid).add(attempt).await()
        Unit
    }

    // ── Load quiz history ─────────────────────────────────────────────────────
    fun observeHistory(uid: String): Flow<Result<List<QuizAttempt>>> = callbackFlow {
        val listener = historyCol(uid)
            .orderBy("completedAt", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snap, err ->
                if (err != null) { trySend(Result.failure(err)); return@addSnapshotListener }
                val items = snap?.documents?.mapNotNull { doc ->
                    doc.toObject(QuizAttempt::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(Result.success(items))
            }
        awaitClose { listener.remove() }
    }

    suspend fun getHistory(uid: String, limit: Long = 20): Result<List<QuizAttempt>> = runCatching {
        val snap = historyCol(uid)
            .orderBy("completedAt", Query.Direction.DESCENDING)
            .limit(limit)
            .get().await()
        snap.documents.mapNotNull { it.toObject(QuizAttempt::class.java)?.copy(id = it.id) }
    }

    // ── Check if quiz already attempted (for UI badge) ────────────────────────
    suspend fun hasAttempted(uid: String, quizId: String): Boolean = runCatching {
        val snap = historyCol(uid)
            .whereEqualTo("quizId", quizId)
            .limit(1)
            .get().await()
        !snap.isEmpty
    }.getOrDefault(false)

    // ── Internal streak helper ────────────────────────────────────────────────
    private data class StreakResult(val newStreak: Int, val wasUpdated: Boolean)

    private fun calculateStreak(
        doc: com.google.firebase.firestore.DocumentSnapshot
    ): StreakResult {
        val lastActive = doc.getTimestamp("lastActive")?.toDate()
            ?: doc.getTimestamp("lastSeen")?.toDate()
        val currentStreak = (doc.getLong("streak") ?: 0L).toInt()
        val today = dayStart(Date())

        return when {
            lastActive == null            -> StreakResult(1, true)
            isSameDay(lastActive, today)  -> StreakResult(currentStreak, false)
            isYesterday(lastActive, today)-> StreakResult(currentStreak + 1, true)
            else                          -> StreakResult(1, true)
        }
    }

    private fun dayStart(date: Date): Date =
        Calendar.getInstance().apply {
            time = date
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

    private fun isSameDay(a: Date, b: Date): Boolean {
        val ca = Calendar.getInstance().apply { time = a }
        val cb = Calendar.getInstance().apply { time = b }
        return ca.get(Calendar.YEAR)         == cb.get(Calendar.YEAR) &&
               ca.get(Calendar.DAY_OF_YEAR) == cb.get(Calendar.DAY_OF_YEAR)
    }

    private fun isYesterday(past: Date, today: Date): Boolean {
        val yesterday = Calendar.getInstance().apply {
            time = today
            add(Calendar.DAY_OF_YEAR, -1)
        }.time
        return isSameDay(past, yesterday)
    }
}
