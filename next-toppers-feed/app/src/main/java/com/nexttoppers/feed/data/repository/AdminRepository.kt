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
    private val usersCol  = firestore.collection("users")
    // F01: admin detection uses /admins/{uid} — the website's source of truth
    private val adminsCol = firestore.collection("admins")

    // ── Admin check — reads /admins/{uid} as the website does ────────────────────

    suspend fun isCurrentUserAdmin(): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        return runCatching {
            val snap = adminsCol.document(uid).get().await()
            snap.exists() && snap.getString("role") in listOf("owner", "admin")
        }.getOrDefault(false)
    }

    suspend fun isCurrentUserOwner(): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        return runCatching {
            val snap = adminsCol.document(uid).get().await()
            snap.exists() && snap.getString("role") == "owner"
        }.getOrDefault(false)
    }

    fun observeAdminStatus(uid: String): Flow<Boolean> = callbackFlow {
        // F01: observe /admins/{uid} directly, same as website AuthContext
        val listener = adminsCol.document(uid).addSnapshotListener { snap, _ ->
            val isAdmin = snap?.exists() == true &&
                snap.getString("role") in listOf("owner", "admin")
            trySend(isAdmin)
        }
        awaitClose { listener.remove() }
    }

    // ── Users management ─────────────────────────────────────────────────────────

    // F14: website orders by "createdAt" (was "joinedAt")
    fun observeAllUsers(limit: Long = 50): Flow<Result<List<User>>> = callbackFlow {
        val query = usersCol.orderBy("createdAt", Query.Direction.DESCENDING).limit(limit)
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
        val allUsers  = usersSnap.documents.mapNotNull { it.toObject(User::class.java) }
        val totalUsers    = allUsers.size
        val premiumCount  = allUsers.count { it.isPremium }

        val pendingSnap = firestore.collection("premiumRequests")
            .whereEqualTo("status", "pending").get().await()
        val pendingRequests = pendingSnap.size()

        // F: website uses "files" + "lectures" — not "resources"
        val filesSnap    = firestore.collection("files").get().await()
        val lecturesSnap = firestore.collection("lectures").get().await()
        val totalResources = filesSnap.size() + lecturesSnap.size()

        val announcementsSnap = firestore.collection("announcements").get().await()
        val totalAnnouncements = announcementsSnap.size()

        // F: website uses "communityMessages" — not "communityPosts"
        val postsSnap  = firestore.collection("communityMessages").get().await()
        val totalPosts = postsSnap.size()

        AdminStats(
            totalUsers           = totalUsers,
            premiumUsers         = premiumCount,
            pendingRequests      = pendingRequests,
            totalResources       = totalResources,
            totalAnnouncements   = totalAnnouncements,
            totalCommunityPosts  = totalPosts
        )
    }

    // ── User actions ─────────────────────────────────────────────────────────────

    // F02: admin status is granted by writing to /admins collection (same as website)
    suspend fun grantAdminRole(uid: String, role: String = "admin"): Result<Unit> = runCatching {
        adminsCol.document(uid).set(mapOf("role" to role)).await()
        usersCol.document(uid).update("role", role).await()
    }

    suspend fun revokeAdminRole(uid: String): Result<Unit> = runCatching {
        adminsCol.document(uid).delete().await()
        usersCol.document(uid).update("role", "student").await()
    }

    suspend fun banUser(uid: String): Result<Unit> = runCatching {
        usersCol.document(uid).update(mapOf(
            "banned"   to true,
            "bannedAt" to com.google.firebase.Timestamp.now()
        )).await()
    }

    suspend fun unbanUser(uid: String): Result<Unit> = runCatching {
        usersCol.document(uid).update("banned", false).await()
    }

    fun observeRecentUsers(limit: Long = 10): Flow<Result<List<User>>> = callbackFlow {
        val query = usersCol.orderBy("createdAt", Query.Direction.DESCENDING).limit(limit)
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
