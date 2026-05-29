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
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LeaderboardRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val usersCol = firestore.collection("users")

    // ── Top-N global leaderboard (realtime) ───────────────────────────────────
    fun observeGlobalLeaderboard(limit: Long = 50): Flow<Result<List<LeaderboardEntry>>> =
        callbackFlow {
            val listener = usersCol
                .orderBy("xp", Query.Direction.DESCENDING)
                .limit(limit)
                .addSnapshotListener { snap, err ->
                    if (err != null) { trySend(Result.failure(err)); return@addSnapshotListener }
                    val entries = snap?.documents?.mapIndexedNotNull { idx, doc ->
                        val uid      = doc.getString("uid")      ?: doc.id
                        val name     = doc.getString("name")     ?: ""
                        val photo    = doc.getString("photoUrl") ?: ""
                        val xp       = doc.getLong("xp")         ?: 0L
                        val streak   = (doc.getLong("streak")    ?: 0L).toInt()
                        val level    = (doc.getLong("level")     ?: LevelUtils.levelForXp(xp).toLong()).toInt()
                        val premium  = doc.getBoolean("premium") ?: false
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

    // ── Weekly leaderboard — users active in last 7 days, ordered by XP ───────
    fun observeWeeklyLeaderboard(limit: Long = 50): Flow<Result<List<LeaderboardEntry>>> =
        callbackFlow {
            val sevenDaysAgo = Date(System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000)
            val cutoff       = Timestamp(sevenDaysAgo)
            val listener = usersCol
                .whereGreaterThanOrEqualTo("lastSeen", cutoff)
                .orderBy("lastSeen", Query.Direction.DESCENDING)
                .limit(limit)
                .addSnapshotListener { snap, err ->
                    if (err != null) { trySend(Result.failure(err)); return@addSnapshotListener }
                    val raw = snap?.documents?.mapNotNull { doc ->
                        val uid     = doc.getString("uid")      ?: doc.id
                        val name    = doc.getString("name")     ?: ""
                        val photo   = doc.getString("photoUrl") ?: ""
                        val xp      = doc.getLong("xp")         ?: 0L
                        val streak  = (doc.getLong("streak")    ?: 0L).toInt()
                        val level   = (doc.getLong("level")     ?: LevelUtils.levelForXp(xp).toLong()).toInt()
                        val premium = doc.getBoolean("premium") ?: false
                        LeaderboardEntry(uid, name, photo, xp, streak, level, premium, 0)
                    } ?: emptyList()

                    val ranked = raw
                        .sortedByDescending { it.xp }
                        .mapIndexed { idx, e -> e.copy(rank = idx + 1) }
                    trySend(Result.success(ranked))
                }
            awaitClose { listener.remove() }
        }

    // ── Get approximate rank for a single user (one-shot) ────────────────────
    suspend fun getUserRank(uid: String): Result<Int> = runCatching {
        val userSnap = usersCol.document(uid).get().await()
        val userXp   = userSnap.getLong("xp") ?: 0L

        val higherSnap = usersCol
            .whereGreaterThan("xp", userXp)
            .get().await()
        higherSnap.size() + 1
    }

    // ── Fetch a page of entries starting after a given rank (pagination) ──────
    suspend fun getLeaderboardPage(
        afterRank: Int = 0,
        pageSize: Long = 20
    ): Result<List<LeaderboardEntry>> = runCatching {
        val snap = usersCol
            .orderBy("xp", Query.Direction.DESCENDING)
            .limit(afterRank + pageSize)
            .get().await()
        snap.documents
            .drop(afterRank)
            .mapIndexedNotNull { idx, doc ->
                val uid     = doc.getString("uid")      ?: doc.id
                val name    = doc.getString("name")     ?: ""
                val photo   = doc.getString("photoUrl") ?: ""
                val xp      = doc.getLong("xp")         ?: 0L
                val streak  = (doc.getLong("streak")    ?: 0L).toInt()
                val level   = (doc.getLong("level")     ?: LevelUtils.levelForXp(xp).toLong()).toInt()
                val premium = doc.getBoolean("premium") ?: false
                LeaderboardEntry(uid, name, photo, xp, streak, level, premium, afterRank + idx + 1)
            }
    }
}
