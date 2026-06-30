package com.nexttoppers.feed.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.nexttoppers.feed.data.model.MembershipBadge
import com.nexttoppers.feed.data.model.MembershipType
import com.nexttoppers.feed.data.model.PremiumMembership
import com.nexttoppers.feed.data.model.PremiumPlan
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
    // F03: primary source of truth is /premiumUsers/{uid} — same as PremiumContext.tsx
    private val premiumUsersCol = firestore.collection("premiumUsers")
    // users doc isPremium mirror is updated when premiumUsers changes
    private val usersCol        = firestore.collection("users")

    // ── Map a premiumUsers document to PremiumMembership ──────────────────────
    private fun mapMembership(snap: DocumentSnapshot): PremiumMembership {
        val data = snap.data ?: return PremiumMembership()

        val isPremium = data["isPremium"] as? Boolean ?: false
        if (!isPremium) return PremiumMembership()

        val planStr = data["plan"] as? String ?: ""
        val type = when (planStr.lowercase()) {
            "day", "daily"    -> MembershipType.WEEKLY   // website "day" plan → mapped to WEEKLY
            "month", "monthly"-> MembershipType.MONTHLY
            "year", "yearly"  -> MembershipType.YEARLY
            "lifetime"        -> MembershipType.LIFETIME
            else              -> MembershipType.MONTHLY
        }

        // F03: website uses "expiryTime" as primary; "expiresAt" is legacy duplicate
        val expiryTime = data["expiryTime"] as? Timestamp
            ?: data["expiresAt"] as? Timestamp

        val activatedAt = data["activatedAt"] as? Timestamp

        val badge = when (type) {
            MembershipType.YEARLY, MembershipType.LIFETIME -> MembershipBadge.VIP
            MembershipType.FREE                            -> MembershipBadge.NONE
            else                                           -> MembershipBadge.PREMIUM
        }

        val daysRemaining = (data["daysRemaining"] as? Long)?.toInt() ?: run {
            expiryTime?.let {
                val diff = it.toDate().time - System.currentTimeMillis()
                (diff / (1000L * 60 * 60 * 24)).toInt().coerceAtLeast(0)
            } ?: 0
        }

        return PremiumMembership(
            type          = type,
            isActive      = isPremium,
            startDate     = activatedAt,
            endDate       = expiryTime,
            badge         = badge,
            plan          = planStr,
            daysRemaining = daysRemaining
        )
    }

    // ── Real-time premium status — mirrors PremiumContext.tsx onSnapshot ───────
    fun observePremiumStatus(uid: String): Flow<Result<PremiumMembership>> = callbackFlow {
        val listener = premiumUsersCol.document(uid).addSnapshotListener { snap, err ->
            if (err != null) {
                trySend(Result.failure(err))
                return@addSnapshotListener
            }
            if (snap == null || !snap.exists()) {
                trySend(Result.success(PremiumMembership()))
                return@addSnapshotListener
            }

            val membership = mapMembership(snap)

            // F03: auto-expiry check — same as PremiumContext.tsx
            if (membership.isActive && membership.isExpired) {
                premiumUsersCol.document(uid).update("isPremium", false)
                usersCol.document(uid).update("isPremium", false)
                trySend(Result.success(PremiumMembership()))
            } else {
                trySend(Result.success(membership))
            }
        }
        awaitClose { listener.remove() }
    }

    // ── One-shot fetch ─────────────────────────────────────────────────────────
    suspend fun getPremiumStatus(uid: String): Result<PremiumMembership> = runCatching {
        val snap = premiumUsersCol.document(uid).get().await()
        if (!snap.exists()) return Result.success(PremiumMembership())
        val membership = mapMembership(snap)
        if (membership.isActive && membership.isExpired) {
            premiumUsersCol.document(uid).update("isPremium", false)
            usersCol.document(uid).update("isPremium", false)
            return Result.success(PremiumMembership())
        }
        membership
    }

    // ── Admin: grant premium — writes to /premiumUsers/{uid} as website does ──
    suspend fun activatePremium(uid: String, plan: PremiumPlan): Result<Unit> = runCatching {
        val nowMs = System.currentTimeMillis()
        val endMs = when (plan.type) {
            MembershipType.WEEKLY   -> nowMs + 7L   * 24 * 60 * 60 * 1000
            MembershipType.MONTHLY  -> nowMs + 30L  * 24 * 60 * 60 * 1000
            MembershipType.YEARLY   -> nowMs + 365L * 24 * 60 * 60 * 1000
            MembershipType.LIFETIME -> nowMs + 100L * 365 * 24 * 60 * 60 * 1000
            MembershipType.FREE     -> nowMs
        }
        val planId = when (plan.type) {
            MembershipType.WEEKLY   -> "day"
            MembershipType.MONTHLY  -> "month"
            MembershipType.YEARLY   -> "year"
            MembershipType.LIFETIME -> "lifetime"
            MembershipType.FREE     -> "free"
        }
        val expiryTimestamp = Timestamp(endMs / 1000, 0)

        // Write to /premiumUsers/{uid} — the website's grant flow
        premiumUsersCol.document(uid).set(mapOf(
            "uid"          to uid,
            "isPremium"    to true,
            "plan"         to planId,
            "activatedAt"  to Timestamp.now(),
            "expiryTime"   to expiryTimestamp,
            "expiresAt"    to expiryTimestamp,
            "grantedBy"    to "app",
            "updatedAt"    to Timestamp.now()
        )).await()

        // Mirror isPremium to users doc
        usersCol.document(uid).update("isPremium", true).await()
    }

    // ── Revoke premium ─────────────────────────────────────────────────────────
    suspend fun deactivatePremium(uid: String): Result<Unit> = runCatching {
        premiumUsersCol.document(uid).update("isPremium", false).await()
        usersCol.document(uid).update("isPremium", false).await()
    }

    suspend fun checkAndRefreshExpiry(uid: String) {
        runCatching {
            val snap = premiumUsersCol.document(uid).get().await()
            if (!snap.exists()) return
            val membership = mapMembership(snap)
            if (membership.isActive && membership.isExpired) {
                premiumUsersCol.document(uid).update("isPremium", false).await()
                usersCol.document(uid).update("isPremium", false).await()
            }
        }
    }

    suspend fun restorePurchase(uid: String): Result<PremiumMembership> = runCatching {
        val snap = premiumUsersCol.document(uid).get().await()
        if (!snap.exists()) PremiumMembership() else mapMembership(snap)
    }
}
