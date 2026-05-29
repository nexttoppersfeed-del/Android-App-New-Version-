package com.nexttoppers.feed.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.nexttoppers.feed.data.model.MembershipBadge
import com.nexttoppers.feed.data.model.MembershipType
import com.nexttoppers.feed.data.model.PremiumRequest
import com.nexttoppers.feed.data.model.PremiumRequestStatus
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PremiumRequestRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val requestsCol = firestore.collection("premiumRequests")
    private val usersCol = firestore.collection("users")

    // ── Observe requests ──────────────────────────────────────────────────────────

    fun observeAllRequests(statusFilter: String? = null): Flow<Result<List<PremiumRequest>>> =
        callbackFlow {
            var query: Query = requestsCol.orderBy("createdAt", Query.Direction.DESCENDING).limit(50)
            if (!statusFilter.isNullOrBlank()) query = query.whereEqualTo("status", statusFilter)

            val listener = query.addSnapshotListener { snap, err ->
                if (err != null) { trySend(Result.failure(err)); return@addSnapshotListener }
                val requests = snap?.documents?.mapNotNull { doc ->
                    try { doc.toObject(PremiumRequest::class.java)?.copy(requestId = doc.id) }
                    catch (e: Exception) { null }
                } ?: emptyList()
                trySend(Result.success(requests))
            }
            awaitClose { listener.remove() }
        }

    fun observePendingRequests(): Flow<Result<List<PremiumRequest>>> =
        observeAllRequests(PremiumRequestStatus.PENDING.name)

    suspend fun getRequestById(requestId: String): Result<PremiumRequest> = runCatching {
        val snap = requestsCol.document(requestId).get().await()
        snap.toObject(PremiumRequest::class.java)?.copy(requestId = snap.id)
            ?: throw Exception("Request not found")
    }

    // ── Submit request (user-side) ────────────────────────────────────────────────

    /**
     * Submit a premium payment request.
     *
     * @param paymentScreenshotBase64 Optional Base64-encoded payment screenshot.
     *   Encode with [com.nexttoppers.feed.util.Base64ImageUtils.bitmapToBase64].
     *   Max ~500 KB decoded (enforced by Firestore rules). Pass empty string if
     *   no screenshot is available.
     */
    suspend fun submitRequest(
        userId: String,
        username: String,
        userEmail: String,
        plan: String,
        amount: String,
        utrId: String,
        paymentScreenshotBase64: String = ""
    ): Result<String> = runCatching {
        val id = UUID.randomUUID().toString()
        val request = PremiumRequest(
            requestId                = id,
            userId                   = userId,
            username                 = username,
            userEmail                = userEmail,
            plan                     = plan,
            amount                   = amount,
            utrId                    = utrId,
            paymentScreenshotBase64  = paymentScreenshotBase64,
            status                   = PremiumRequestStatus.PENDING.name
        )
        requestsCol.document(id).set(request.toMap()).await()
        id
    }

    // ── Admin: approve request ────────────────────────────────────────────────────

    suspend fun approveRequest(
        requestId: String,
        userId: String,
        plan: String,
        durationDays: Int = 30
    ): Result<Unit> = runCatching {
        val adminUid = auth.currentUser?.uid ?: throw Exception("Not authenticated")
        val nowMs = System.currentTimeMillis()
        val endMs = nowMs + durationDays.toLong() * 24 * 60 * 60 * 1000

        val membershipType = when {
            plan.contains("weekly", true) || durationDays <= 7   -> MembershipType.WEEKLY
            plan.contains("yearly", true) || durationDays >= 300 -> MembershipType.YEARLY
            plan.contains("lifetime", true)                      -> MembershipType.LIFETIME
            else                                                 -> MembershipType.MONTHLY
        }
        val badge = when (membershipType) {
            MembershipType.YEARLY, MembershipType.LIFETIME -> MembershipBadge.VIP
            else -> MembershipBadge.PREMIUM
        }

        firestore.runBatch { batch ->
            batch.update(requestsCol.document(requestId), mapOf(
                "status"     to PremiumRequestStatus.APPROVED.name,
                "reviewedAt" to Timestamp.now(),
                "reviewedBy" to adminUid
            ))
            batch.update(usersCol.document(userId), mapOf(
                "isPremium"       to true,
                "premium"         to true,
                "premiumType"     to membershipType.name.lowercase(),
                "premiumStart"    to Timestamp.now(),
                "premiumEnd"      to Timestamp(endMs / 1000, 0),
                "premiumActive"   to true,
                "membershipBadge" to badge.name
            ))
        }.await()
    }

    // ── Admin: reject request ─────────────────────────────────────────────────────

    suspend fun rejectRequest(
        requestId: String,
        adminNote: String = ""
    ): Result<Unit> = runCatching {
        val adminUid = auth.currentUser?.uid ?: throw Exception("Not authenticated")
        requestsCol.document(requestId).update(mapOf(
            "status"     to PremiumRequestStatus.REJECTED.name,
            "reviewedAt" to Timestamp.now(),
            "reviewedBy" to adminUid,
            "adminNote"  to adminNote
        )).await()
    }

    // ── Pending count ─────────────────────────────────────────────────────────────

    fun observePendingCount(): Flow<Int> = callbackFlow {
        val listener = requestsCol
            .whereEqualTo("status", PremiumRequestStatus.PENDING.name)
            .addSnapshotListener { snap, _ -> trySend(snap?.size() ?: 0) }
        awaitClose { listener.remove() }
    }
}
