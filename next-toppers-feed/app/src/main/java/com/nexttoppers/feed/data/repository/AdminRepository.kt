package com.nexttoppers.feed.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.nexttoppers.feed.data.model.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

data class AdminStats(
    val totalUsers: Int = 0,
    val premiumUsers: Int = 0,
    val pendingRequests: Int = 0,
    val totalResources: Int = 0,
    val totalAnnouncements: Int = 0,
    val totalCommunityPosts: Int = 0
)

@Singleton
class AdminRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val usersCol = firestore.collection("users")

    // ── Admin check ──────────────────────────────────────────────────────────────

    suspend fun isCurrentUserAdmin(): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        return runCatching {
            val snap = usersCol.document(uid).get().await()
            snap.getBoolean("isAdmin") ?: false
        }.getOrDefault(false)
    }

    fun observeAdminStatus(uid: String): Flow<Boolean> = callbackFlow {
        val listener = usersCol.document(uid).addSnapshotListener { snap, _ ->
            trySend(snap?.getBoolean("isAdmin") ?: false)
        }
        awaitClose { listener.remove() }
    }

    // ── Users management ─────────────────────────────────────────────────────────

    fun observeAllUsers(limit: Long = 50): Flow<Result<List<User>>> = callbackFlow {
        val query = usersCol.orderBy("joinedAt", Query.Direction.DESCENDING).limit(limit)
        val listener = query.addSnapshotListener { snap, err ->
            if (err != null) { trySend(Result.failure(err)); return@addSnapshotListener }
            val users = snap?.documents?.mapNotNull { doc ->
                try { doc.toObject(User::class.java)?.copy(uid = doc.id) } catch (e: Exception) { null }
            } ?: emptyList()
            trySend(Result.success(users))
        }
        awaitClose { listener.remove() }
    }

    suspend fun searchUsers(query: String): Result<List<User>> = runCatching {
        val byName = usersCol
            .whereGreaterThanOrEqualTo("name", query)
            .whereLessThanOrEqualTo("name", query + "\uf8ff")
            .limit(20).get().await()
        byName.documents.mapNotNull { doc ->
            try { doc.toObject(User::class.java)?.copy(uid = doc.id) } catch (e: Exception) { null }
        }
    }

    suspend fun getUserById(uid: String): Result<User> = runCatching {
        val snap = usersCol.document(uid).get().await()
        snap.toObject(User::class.java)?.copy(uid = snap.id)
            ?: throw Exception("User not found")
    }

    // ── Admin stats ──────────────────────────────────────────────────────────────

    suspend fun getAdminStats(): Result<AdminStats> = runCatching {
        val usersSnap = usersCol.get().await()
        val allUsers = usersSnap.documents.mapNotNull { it.toObject(User::class.java) }
        val totalUsers = allUsers.size
        val premiumUsers = allUsers.count { it.isPremium }

        val pendingSnap = firestore.collection("premiumRequests")
            .whereEqualTo("status", "PENDING").get().await()
        val pendingRequests = pendingSnap.size()

        val resourcesSnap = firestore.collection("resources").get().await()
        val totalResources = resourcesSnap.size()

        val announcementsSnap = firestore.collection("announcements").get().await()
        val totalAnnouncements = announcementsSnap.size()

        val postsSnap = firestore.collection("communityPosts").get().await()
        val totalPosts = postsSnap.size()

        AdminStats(
            totalUsers = totalUsers,
            premiumUsers = premiumUsers,
            pendingRequests = pendingRequests,
            totalResources = totalResources,
            totalAnnouncements = totalAnnouncements,
            totalCommunityPosts = totalPosts
        )
    }

    // ── User actions ─────────────────────────────────────────────────────────────

    suspend fun setAdminStatus(uid: String, isAdmin: Boolean): Result<Unit> = runCatching {
        usersCol.document(uid).update("isAdmin", isAdmin).await()
    }

    suspend fun banUser(uid: String): Result<Unit> = runCatching {
        usersCol.document(uid).update(mapOf(
            "banned" to true,
            "bannedAt" to com.google.firebase.Timestamp.now()
        )).await()
    }

    suspend fun unbanUser(uid: String): Result<Unit> = runCatching {
        usersCol.document(uid).update("banned", false).await()
    }

    fun observeRecentUsers(limit: Long = 10): Flow<Result<List<User>>> = callbackFlow {
        val query = usersCol.orderBy("joinedAt", Query.Direction.DESCENDING).limit(limit)
        val listener = query.addSnapshotListener { snap, err ->
            if (err != null) { trySend(Result.failure(err)); return@addSnapshotListener }
            val users = snap?.documents?.mapNotNull { doc ->
                try { doc.toObject(User::class.java)?.copy(uid = doc.id) } catch (e: Exception) { null }
            } ?: emptyList()
            trySend(Result.success(users))
        }
        awaitClose { listener.remove() }
    }
}
