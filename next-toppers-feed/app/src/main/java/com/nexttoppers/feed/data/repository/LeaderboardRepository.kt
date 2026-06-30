package com.nexttoppers.feed.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.nexttoppers.feed.data.model.LeaderboardEntry
import com.nexttoppers.feed.util.LevelUtils
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LeaderboardRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    // F11: website stores leaderboard in /leaderboard/{uid} — NOT users collection
    private val leaderboardCol = firestore.collection("leaderboard")

    // ── Top-N global leaderboard (realtime) ───────────────────────────────────
    fun observeGlobalLeaderboard(limit: Long = 100): Flow<Result<List<LeaderboardEntry>>> =
        callbackFlow {
            val listener = leaderboardCol
                .orderBy("xp", Query.Direction.DESCENDING)
                .limit(limit)
                .addSnapshotListener { snap, err ->
                    if (err != null) { trySend(Result.failure(err)); return@addSnapshotListener }
                    val entries = snap?.documents?.mapIndexedNotNull { idx, doc ->
                        val uid     = doc.getString("uid") ?: doc.id
                        val name    = doc.getString("name") ?: ""
                        // F07: website uses "photoURL" (capital URL)
                        val photo   = doc.getString("photoURL") ?: ""
                        val xp      = doc.getLong("xp") ?: 0L
                        val streak  = (doc.getLong("streak") ?: 0L).toInt()
                        val level   = (doc.getLong("level")
                                       ?: LevelUtils.levelForXp(xp).toLong()).toInt()
                        // F07: website uses "isPremium" in leaderboard doc
                        val premium = doc.getBoolean("isPremium") ?: false
                        LeaderboardEntry(
                            uid      = uid,
                            name     = name,
                            photoUrl = photo,
                            xp       = xp,
                            streak   = streak,
                            level    = level,
                            premium  = premium,
                            rank     = idx + 1
                        )
                    } ?: emptyList()
                    trySend(Result.success(entries))
                }
            awaitClose { listener.remove() }
        }

    // ── Weekly leaderboard — filter by weekKey matching current week ───────────
    fun observeWeeklyLeaderboard(limit: Long = 100): Flow<Result<List<LeaderboardEntry>>> =
        callbackFlow {
            val cal      = java.util.Calendar.getInstance()
            val year     = cal.get(java.util.Calendar.YEAR)
            val week     = cal.get(java.util.Calendar.WEEK_OF_YEAR)
            val weekKey  = "$year-W${week.toString().padStart(2, '0')}"

            val listener = leaderboardCol
                .whereEqualTo("weekKey", weekKey)
                .orderBy("xp", Query.Direction.DESCENDING)
                .limit(limit)
                .addSnapshotListener { snap, err ->
                    if (err != null) { trySend(Result.failure(err)); return@addSnapshotListener }
                    val entries = snap?.documents?.mapIndexedNotNull { idx, doc ->
                        val uid     = doc.getString("uid") ?: doc.id
                        val name    = doc.getString("name") ?: ""
                        val photo   = doc.getString("photoURL") ?: ""
                        val xp      = doc.getLong("xp") ?: 0L
                        val streak  = (doc.getLong("streak") ?: 0L).toInt()
                        val level   = (doc.getLong("level")
                                       ?: LevelUtils.levelForXp(xp).toLong()).toInt()
                        val premium = doc.getBoolean("isPremium") ?: false
                        LeaderboardEntry(uid, name, photo, xp, streak, level, premium, idx + 1)
                    } ?: emptyList()
                    trySend(Result.success(entries))
                }
            awaitClose { listener.remove() }
        }

    // ── Monthly leaderboard — filter by monthKey ──────────────────────────────
    fun observeMonthlyLeaderboard(limit: Long = 100): Flow<Result<List<LeaderboardEntry>>> =
        callbackFlow {
            val cal      = java.util.Calendar.getInstance()
            val year     = cal.get(java.util.Calendar.YEAR)
            val month    = cal.get(java.util.Calendar.MONTH) + 1
            val monthKey = "$year-${month.toString().padStart(2, '0')}"

            val listener = leaderboardCol
                .whereEqualTo("monthKey", monthKey)
                .orderBy("xp", Query.Direction.DESCENDING)
                .limit(limit)
                .addSnapshotListener { snap, err ->
                    if (err != null) { trySend(Result.failure(err)); return@addSnapshotListener }
                    val entries = snap?.documents?.mapIndexedNotNull { idx, doc ->
                        val uid     = doc.getString("uid") ?: doc.id
                        val name    = doc.getString("name") ?: ""
                        val photo   = doc.getString("photoURL") ?: ""
                        val xp      = doc.getLong("xp") ?: 0L
                        val streak  = (doc.getLong("streak") ?: 0L).toInt()
                        val level   = (doc.getLong("level")
                                       ?: LevelUtils.levelForXp(xp).toLong()).toInt()
                        val premium = doc.getBoolean("isPremium") ?: false
                        LeaderboardEntry(uid, name, photo, xp, streak, level, premium, idx + 1)
                    } ?: emptyList()
                    trySend(Result.success(entries))
                }
            awaitClose { listener.remove() }
        }

    // ── Get user's rank by counting documents with higher XP ─────────────────
    suspend fun getUserRank(uid: String): Result<Int> = runCatching {
        val userSnap = leaderboardCol.document(uid).get().await()
        val userXp   = userSnap.getLong("xp") ?: 0L

        val higherSnap = leaderboardCol
            .whereGreaterThan("xp", userXp)
            .get().await()
        higherSnap.size() + 1
    }

    // ── Paginated fetch ───────────────────────────────────────────────────────
    suspend fun getLeaderboardPage(
        afterRank: Int = 0,
        pageSize: Long = 20
    ): Result<List<LeaderboardEntry>> = runCatching {
        val snap = leaderboardCol
            .orderBy("xp", Query.Direction.DESCENDING)
            .limit(afterRank + pageSize)
            .get().await()
        snap.documents
            .drop(afterRank)
            .mapIndexedNotNull { idx, doc ->
                val uid     = doc.getString("uid") ?: doc.id
                val name    = doc.getString("name") ?: ""
                val photo   = doc.getString("photoURL") ?: ""
                val xp      = doc.getLong("xp") ?: 0L
                val streak  = (doc.getLong("streak") ?: 0L).toInt()
                val level   = (doc.getLong("level") ?: LevelUtils.levelForXp(xp).toLong()).toInt()
                val premium = doc.getBoolean("isPremium") ?: false
                LeaderboardEntry(uid, name, photo, xp, streak, level, premium, afterRank + idx + 1)
            }
    }
}
