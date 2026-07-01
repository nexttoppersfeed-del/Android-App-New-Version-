package com.nexttoppers.feed.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.nexttoppers.feed.data.model.TestAttempt
import com.nexttoppers.feed.util.LevelUtils
import com.nexttoppers.feed.util.resolveLastActive
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class XpRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private fun userDoc(uid: String)  = firestore.collection("users").document(uid)
    // F09: quiz attempts go to top-level "testAttempts" — not users/{uid}/quizHistory
    private val attemptsCol           = firestore.collection("testAttempts")
    // F10: leaderboard sync target — same collection website writes to
    private val leaderboardCol        = firestore.collection("leaderboard")

    // ── XP constants — aligned with website XP_RULES ─────────────────────────
    // F20: values match the website's XPContext.tsx XP_RULES
    companion object {
        const val XP_DAILY_LOGIN        = 50   // website dailyLogin
        const val XP_LECTURE_WATCH      = 20   // website lectureWatch
        const val XP_PDF_READ           = 15   // website pdfRead
        const val XP_QUIZ_BASE          = 20   // website quizComplete
        const val XP_PER_CORRECT        = 2    // website quizCorrectAnswer
        const val XP_HIGH_SCORE_BONUS   = 30   // website bonus for 80%+
        const val XP_PERFECT_SCORE_BONUS = 50  // website bonus for 100%
    }

    // ── Award XP from quiz + update all website-compatible stat fields ─────────
    suspend fun awardXp(
        uid: String,
        xp: Int,
        correctAnswers: Int = 0,
        totalQuestions: Int = 0,
        score: Int = 0,
        maxScore: Int = 0
    ): Result<Unit> = runCatching {
        val doc        = userDoc(uid).get().await()
        val currentXp  = (doc.getLong("xp") ?: 0L)
        val newXp      = currentXp + xp
        val newLevel   = LevelUtils.levelForXp(newXp)
        val streakResult = calculateStreak(doc)

        // F21: lastActive as date string to match website schema
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

        // F16/F17: use correct website field names for all stats
        val prevTotalQuizzes  = (doc.getLong("totalQuizzes") ?: 0L).toInt()
        val prevTotalCorrect  = (doc.getLong("totalCorrect") ?: 0L).toInt()
        val prevTotalScore    = (doc.getLong("totalScore") ?: 0L).toInt()
        val prevPerfectScores = (doc.getLong("perfectScores") ?: 0L).toInt()

        val newTotalQuizzes  = prevTotalQuizzes + 1
        val newTotalCorrect  = prevTotalCorrect + correctAnswers
        val newTotalScore    = prevTotalScore + score
        val isPerfect        = totalQuestions > 0 && correctAnswers == totalQuestions
        val newPerfectScores = if (isPerfect) prevPerfectScores + 1 else prevPerfectScores
        val newAvgScore      = if (newTotalQuizzes > 0)
            (newTotalScore.toFloat() / newTotalQuizzes) else 0f

        val updates = mutableMapOf<String, Any>(
            "xp"           to FieldValue.increment(xp.toLong()),
            "level"        to newLevel,
            "totalQuizzes" to newTotalQuizzes,
            "totalCorrect" to newTotalCorrect,
            "totalScore"   to newTotalScore,
            "perfectScores" to newPerfectScores,
            "avgScore"     to newAvgScore,
            "streak"       to streakResult.newStreak,
            "lastSeen"     to Timestamp.now(),
            "lastActive"   to todayStr,
            "updatedAt"    to Timestamp.now()
        )

        userDoc(uid).update(updates).await()

        // F10: sync to leaderboard collection after every XP award
        syncLeaderboard(uid)
    }

    // ── Award XP for watching a lecture ───────────────────────────────────────
    // F15: updates lecturesWatched (not resourcesOpened)
    suspend fun awardLectureXp(uid: String): Result<Unit> = runCatching {
        val doc        = userDoc(uid).get().await()
        val currentXp  = (doc.getLong("xp") ?: 0L)
        val newXp      = currentXp + XP_LECTURE_WATCH
        val newLevel   = LevelUtils.levelForXp(newXp)
        val todayStr   = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

        userDoc(uid).update(mapOf(
            "xp"              to FieldValue.increment(XP_LECTURE_WATCH.toLong()),
            "level"           to newLevel,
            "lecturesWatched" to FieldValue.increment(1L),
            "lastActive"      to todayStr,
            "updatedAt"       to Timestamp.now()
        )).await()

        syncLeaderboard(uid)
    }

    // ── Award XP for reading a PDF ────────────────────────────────────────────
    // F15: updates pdfsRead (not resourcesOpened)
    suspend fun awardPdfXp(uid: String): Result<Unit> = runCatching {
        val doc        = userDoc(uid).get().await()
        val currentXp  = (doc.getLong("xp") ?: 0L)
        val newXp      = currentXp + XP_PDF_READ
        val newLevel   = LevelUtils.levelForXp(newXp)
        val todayStr   = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

        userDoc(uid).update(mapOf(
            "xp"         to FieldValue.increment(XP_PDF_READ.toLong()),
            "level"      to newLevel,
            "pdfsRead"   to FieldValue.increment(1L),
            "lastActive" to todayStr,
            "updatedAt"  to Timestamp.now()
        )).await()

        syncLeaderboard(uid)
    }

    // ── Kept for backward compat — now routes to lecture or PDF ──────────────
    suspend fun awardResourceXp(uid: String): Result<Unit> = awardLectureXp(uid)

    // ── Award daily login XP (50 XP, once per day) ───────────────────────────
    suspend fun awardDailyLoginXp(uid: String): Result<Boolean> = runCatching {
        val doc = userDoc(uid).get().await()

        // F21: resolveLastActive handles both legacy Timestamp and current String format
        val lastActiveStr = doc.resolveLastActive()
        val todayStr      = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

        if (lastActiveStr == todayStr) {
            return@runCatching false
        }

        val currentXp  = (doc.getLong("xp") ?: 0L)
        val newXp      = currentXp + XP_DAILY_LOGIN
        val newLevel   = LevelUtils.levelForXp(newXp)
        val streakResult = calculateStreak(doc)

        userDoc(uid).update(mapOf(
            "xp"         to FieldValue.increment(XP_DAILY_LOGIN.toLong()),
            "level"      to newLevel,
            "streak"     to streakResult.newStreak,
            "lastActive" to todayStr,
            "lastSeen"   to Timestamp.now(),
            "updatedAt"  to Timestamp.now()
        )).await()

        syncLeaderboard(uid)
        true
    }

    // ── F10: Sync user data to /leaderboard/{uid} (mirrors website's syncLeaderboard) ─
    suspend fun syncLeaderboard(uid: String) {
        runCatching {
            val doc = userDoc(uid).get().await()

            // F07: website uses "photoURL" (capital URL)
            val photoURL = doc.getString("photoURL") ?: ""

            // weekKey: "yyyy-Www", monthKey: "yyyy-MM" — matches website format
            val cal       = Calendar.getInstance()
            val year      = cal.get(Calendar.YEAR)
            val week      = cal.get(Calendar.WEEK_OF_YEAR)
            val month     = cal.get(Calendar.MONTH) + 1
            val weekKey   = "$year-W${week.toString().padStart(2, '0')}"
            val monthKey  = "$year-${month.toString().padStart(2, '0')}"

            leaderboardCol.document(uid).set(mapOf(
                "uid"          to uid,
                "name"         to (doc.getString("name") ?: ""),
                "photoURL"     to photoURL,
                "xp"           to (doc.getLong("xp") ?: 0L),
                "level"        to (doc.getLong("level") ?: 1L).toInt(),
                "streak"       to (doc.getLong("streak") ?: 0L).toInt(),
                "totalQuizzes" to (doc.getLong("totalQuizzes") ?: 0L).toInt(),
                "totalCorrect" to (doc.getLong("totalCorrect") ?: 0L).toInt(),
                "totalScore"   to (doc.getLong("totalScore") ?: 0L).toInt(),
                "perfectScores" to (doc.getLong("perfectScores") ?: 0L).toInt(),
                "avgScore"     to (doc.getDouble("avgScore") ?: 0.0),
                "isPremium"    to (doc.getBoolean("isPremium") ?: false),
                "weekKey"      to weekKey,
                "monthKey"     to monthKey,
                "updatedAt"    to Timestamp.now()
            )).await()
        }
    }

    // ── F09: Save quiz attempt to top-level testAttempts collection ───────────
    suspend fun saveAttempt(uid: String, attempt: TestAttempt): Result<Unit> = runCatching {
        attemptsCol.add(attempt.toMap()).await()
        Unit
    }

    // ── Load quiz history from testAttempts ───────────────────────────────────
    fun observeHistory(uid: String): Flow<Result<List<TestAttempt>>> = callbackFlow {
        val listener = attemptsCol
            .whereEqualTo("userId", uid)
            .orderBy("completedAt", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snap, err ->
                if (err != null) { trySend(Result.failure(err)); return@addSnapshotListener }
                val items = snap?.documents?.mapNotNull { doc ->
                    try { doc.toObject(TestAttempt::class.java)?.copy(id = doc.id) }
                    catch (_: Exception) { null }
                } ?: emptyList()
                trySend(Result.success(items))
            }
        awaitClose { listener.remove() }
    }

    suspend fun getHistory(uid: String, limit: Long = 20): Result<List<TestAttempt>> = runCatching {
        val snap = attemptsCol
            .whereEqualTo("userId", uid)
            .orderBy("completedAt", Query.Direction.DESCENDING)
            .limit(limit)
            .get().await()
        snap.documents.mapNotNull { doc ->
            try { doc.toObject(TestAttempt::class.java)?.copy(id = doc.id) }
            catch (_: Exception) { null }
        }
    }

    suspend fun hasAttempted(uid: String, quizId: String): Boolean = runCatching {
        val snap = attemptsCol
            .whereEqualTo("userId", uid)
            .whereEqualTo("testId", quizId)
            .limit(1)
            .get().await()
        !snap.isEmpty
    }.getOrDefault(false)

    // ── Internal streak helper ────────────────────────────────────────────────
    private data class StreakResult(val newStreak: Int, val wasUpdated: Boolean)

    private fun calculateStreak(doc: com.google.firebase.firestore.DocumentSnapshot): StreakResult {
        // F21: resolveLastActive handles both legacy Timestamp and current String format
        val lastActiveStr  = doc.resolveLastActive()
        val currentStreak  = (doc.getLong("streak") ?: 0L).toInt()
        val todayStr       = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val yesterdayStr   = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(
            Date(System.currentTimeMillis() - 86_400_000L)
        )

        return when {
            lastActiveStr.isBlank()       -> StreakResult(1, true)
            lastActiveStr == todayStr     -> StreakResult(currentStreak, false)
            lastActiveStr == yesterdayStr -> StreakResult(currentStreak + 1, true)
            else                          -> StreakResult(1, true)
        }
    }
}
