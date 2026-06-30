package com.nexttoppers.feed.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
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
    private val usersCol    = firestore.collection("users")
    // F03: premiumUsers collection — synced on approval
    private val premiumCol  = firestore.collection("premiumUsers")

    // ── Manual mapper — reads both website and legacy field names ─────────────

    private fun mapRequest(doc: DocumentSnapshot): PremiumRequest? {
        val data = doc.data ?: return null
        return try {
            // F11: website writes "uid" — fall back to legacy "userId"
            val uid      = data["uid"] as? String ?: data["userId"] as? String ?: ""
            // F11: website writes "userName" — fall back to legacy "username"
            val userName = data["userName"] as? String ?: data["username"] as? String ?: ""
            // F11: website writes "price" as number — fall back to legacy "amount" string
            val price    = (data["price"] as? Double)
                            ?: (data["price"] as? Long)?.toDouble()
                            ?: (data["amount"] as? String)?.toDoubleOrNull() ?: 0.0
            // F11: website writes "transactionId" — fall back to legacy "utrId"
            val txId     = data["transactionId"] as? String ?: data["utrId"] as? String ?: ""

            PremiumRequest(
                requestId               = doc.id,
                uid                     = uid,
                userName                = userName,
                userEmail               = data["userEmail"] as? String ?: "",
                plan                    = data["plan"] as? String ?: "",
                price                   = price,
                transactionId           = txId,
                paymentScreenshotBase64 = data["paymentScreenshotBase64"] as? String ?: "",
                status                  = data["status"] as? String
                                          ?: PremiumRequestStatus.PENDING.name,
                createdAt               = data["createdAt"] as? Timestamp ?: Timestamp.now(),
                reviewedAt              = data["reviewedAt"] as? Timestamp,
                reviewedBy              = data["reviewedBy"] as? String ?: "",
                adminNote               = data["adminNote"] as? String ?: "",
                phoneNumber             = data["phoneNumber"] as? String ?: "",
                paymentMethod           = data["paymentMethod"] as? String ?: "UPI"
            )
        } catch (e: Exception) { null }
    }

    // ── Observe requests ──────────────────────────────────────────────────────

    fun observeAllRequests(statusFilter: String? = null): Flow<Result<List<PremiumRequest>>> =
        callbackFlow {
            var query: Query = requestsCol
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(50)
            if (!statusFilter.isNullOrBlank()) query = query.whereEqualTo("status", statusFilter)

            val listener = query.addSnapshotListener { snap, err ->
                if (err != null) { trySend(Result.failure(err)); return@addSnapshotListener }
                val requests = snap?.documents?.mapNotNull { mapRequest(it) } ?: emptyList()
                trySend(Result.success(requests))
            }
            awaitClose { listener.remove() }
        }

    fun observePendingRequests(): Flow<Result<List<PremiumRequest>>> =
        observeAllRequests(PremiumRequestStatus.PENDING.name)

    suspend fun getRequestById(requestId: String): Result<PremiumRequest> = runCatching {
        val snap = requestsCol.document(requestId).get().await()
        mapRequest(snap) ?: throw Exception("Request not found")
    }

    fun observeUserRequests(uid: String): Flow<Result<List<PremiumRequest>>> = callbackFlow {
        // Try querying by "uid" (website field) — if that returns nothing, try legacy "userId"
        val listener = requestsCol
            .whereEqualTo("uid", uid)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(10)
            .addSnapshotListener { snap, err ->
                if (err != null) { trySend(Result.failure(err)); return@addSnapshotListener }
                val requests = snap?.documents?.mapNotNull { mapRequest(it) } ?: emptyList()
                trySend(Result.success(requests))
            }
        awaitClose { listener.remove() }
    }

    // ── Submit request (user-side) ─────────────────────────────────────────────
    /**
     * Submit a premium payment request.
     *
     * F11: now writes website field names: uid, userName, price (number), transactionId.
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
        amount: String,          // kept for API compat — converted to price (number)
        utrId: String,           // kept for API compat — written as transactionId
        paymentScreenshotBase64: String = "",
        phoneNumber: String = "",
        paymentMethod: String = "UPI"
    ): Result<String> = runCatching {
        val id    = UUID.randomUUID().toString()
        val price = amount.toDoubleOrNull() ?: 0.0

        // F11: write website-compatible field names
        val data = mapOf(
            "requestId"               to id,
            "uid"                     to userId,
            "userName"                to username,
            "userEmail"               to userEmail,
            "plan"                    to plan,
            "price"                   to price,
            "transactionId"           to utrId,
            "paymentScreenshotBase64" to paymentScreenshotBase64,
            "status"                  to PremiumRequestStatus.PENDING.name,
            "createdAt"               to Timestamp.now(),
            "reviewedAt"              to null,
            "reviewedBy"              to "",
            "adminNote"               to "",
            "phoneNumber"             to phoneNumber,
            "paymentMethod"           to paymentMethod
        )
        requestsCol.document(id).set(data).await()
        id
    }

    // ── Admin: approve request ────────────────────────────────────────────────

    suspend fun approveRequest(
        requestId: String,
        userId: String,
        plan: String,
        durationDays: Int = 30
    ): Result<Unit> = runCatching {
        val adminUid = auth.currentUser?.uid ?: throw Exception("Not authenticated")
        val nowMs    = System.currentTimeMillis()
        val endMs    = nowMs + durationDays.toLong() * 24 * 60 * 60 * 1000

        val membershipType = when {
            plan.contains("weekly",   true) || durationDays <= 7   -> MembershipType.WEEKLY
            plan.contains("yearly",   true) || durationDays >= 300 -> MembershipType.YEARLY
            plan.contains("lifetime", true)                        -> MembershipType.LIFETIME
            else                                                   -> MembershipType.MONTHLY
        }
        val badge = when (membershipType) {
            MembershipType.YEARLY, MembershipType.LIFETIME -> MembershipBadge.VIP
            else -> MembershipBadge.PREMIUM
        }

        firestore.runBatch { batch ->
            // Mark request approved
            batch.update(requestsCol.document(requestId), mapOf(
                "status"     to PremiumRequestStatus.APPROVED.name,
                "reviewedAt" to Timestamp.now(),
                "reviewedBy" to adminUid
            ))

            // Update user doc
            batch.update(usersCol.document(userId), mapOf(
                "isPremium"       to true,
                "premium"         to true,
                "premiumType"     to membershipType.name.lowercase(),
                "premiumStart"    to Timestamp.now(),
                "premiumEnd"      to Timestamp(endMs / 1000, 0),
                "premiumActive"   to true,
                "membershipBadge" to badge.name
            ))

            // F03: also write to /premiumUsers/{uid} matching PremiumRepository's expected schema
            batch.set(premiumCol.document(userId), mapOf(
                "uid"         to userId,
                "plan"        to membershipType.name.lowercase(),
                "startDate"   to Timestamp.now(),
                "endDate"     to Timestamp(endMs / 1000, 0),
                "isActive"    to true,
                "approvedBy"  to adminUid,
                "approvedAt"  to Timestamp.now()
            ))
        }.await()
    }

    // ── Admin: reject request ─────────────────────────────────────────────────

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

    // ── Pending count ─────────────────────────────────────────────────────────

    fun observePendingCount(): Flow<Int> = callbackFlow {
        val listener = requestsCol
            .whereEqualTo("status", PremiumRequestStatus.PENDING.name)
            .addSnapshotListener { snap, _ -> trySend(snap?.size() ?: 0) }
        awaitClose { listener.remove() }
    }
}
