package com.nexttoppers.feed.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.nexttoppers.feed.data.model.MembershipBadge
import com.nexttoppers.feed.data.model.MembershipType
import com.nexttoppers.feed.data.model.PremiumMembership
import com.nexttoppers.feed.data.model.PremiumPlan
import com.nexttoppers.feed.data.model.User
import com.nexttoppers.feed.data.model.toPremiumMembership
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PremiumRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    private val users = firestore.collection("users")

    // ── Realtime premium status stream ─────────────────────────────────────────
    fun observePremiumStatus(uid: String): Flow<Result<PremiumMembership>> = callbackFlow {
        val listener = users.document(uid).addSnapshotListener { snap, err ->
            if (err != null) {
                trySend(Result.failure(err))
                return@addSnapshotListener
            }
            if (snap == null || !snap.exists()) {
                trySend(Result.success(PremiumMembership()))
                return@addSnapshotListener
            }
            val user = snap.toObject(User::class.java) ?: User()
            trySend(Result.success(user.toPremiumMembership()))
        }
        awaitClose { listener.remove() }
    }

    // ── Activate premium (placeholder payment succeeded) ───────────────────────
    suspend fun activatePremium(uid: String, plan: PremiumPlan): Result<Unit> = runCatching {
        val nowMs = System.currentTimeMillis()
        val endMs = when (plan.type) {
            MembershipType.WEEKLY   -> nowMs + 7L   * 24 * 60 * 60 * 1000
            MembershipType.MONTHLY  -> nowMs + 30L  * 24 * 60 * 60 * 1000
            MembershipType.YEARLY   -> nowMs + 365L * 24 * 60 * 60 * 1000
            MembershipType.LIFETIME -> nowMs + 100L * 365 * 24 * 60 * 60 * 1000
            MembershipType.FREE     -> nowMs
        }
        val updates = mapOf(
            "isPremium"       to true,
            "premium"         to true,
            "premiumType"     to plan.type.name.lowercase(),
            "premiumStart"    to Timestamp.now(),
            "premiumEnd"      to Timestamp(endMs / 1000, 0),
            "premiumActive"   to true,
            "membershipBadge" to plan.badge.name
        )
        users.document(uid).update(updates).await()
    }

    // ── Check expiry and downgrade to free if expired ──────────────────────────
    suspend fun checkAndRefreshExpiry(uid: String) {
        runCatching {
            val snap = users.document(uid).get().await()
            val user = snap.toObject(User::class.java) ?: return
            if (user.toPremiumMembership().isExpired) {
                users.document(uid).update(
                    mapOf(
                        "isPremium"    to false,
                        "premium"      to false,
                        "premiumActive" to false,
                        "premiumType"  to "free"
                    )
                ).await()
            }
        }
    }

    // ── Restore purchase placeholder ───────────────────────────────────────────
    suspend fun restorePurchase(uid: String): Result<PremiumMembership> = runCatching {
        val snap = users.document(uid).get().await()
        (snap.toObject(User::class.java) ?: User()).toPremiumMembership()
    }

    // ── Deactivate premium (cancel / manual) ───────────────────────────────────
    suspend fun deactivatePremium(uid: String): Result<Unit> = runCatching {
        users.document(uid).update(
            mapOf(
                "isPremium"       to false,
                "premium"         to false,
                "premiumActive"   to false,
                "premiumType"     to "free",
                "membershipBadge" to MembershipBadge.NONE.name
            )
        ).await()
    }
}
