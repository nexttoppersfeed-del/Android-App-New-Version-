package com.nexttoppers.feed.data.model

import com.google.firebase.Timestamp

// ── Membership types ───────────────────────────────────────────────────────────
enum class MembershipType(val displayName: String) {
    FREE("Free"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly"),
    YEARLY("Yearly"),
    LIFETIME("Lifetime")
}

// ── Membership badges ──────────────────────────────────────────────────────────
enum class MembershipBadge(val label: String) {
    NONE(""),
    PREMIUM("PREMIUM"),
    VIP("VIP"),
    LIFETIME("LIFETIME"),
    EARLY_MEMBER("EARLY MEMBER")
}

// ── Runtime membership state (sourced from /premiumUsers/{uid}) ────────────────
data class PremiumMembership(
    val type: MembershipType = MembershipType.FREE,
    val isActive: Boolean = false,
    val startDate: Timestamp? = null,
    val endDate: Timestamp? = null,
    val badge: MembershipBadge = MembershipBadge.NONE,
    val plan: String = "",
    val daysRemaining: Int = 0
) {
    val isExpired: Boolean
        get() {
            if (!isActive) return false
            if (type == MembershipType.FREE || type == MembershipType.LIFETIME) return false
            val end = endDate ?: return true
            return end.toDate().time < System.currentTimeMillis()
        }

    val daysLeft: Int
        get() {
            val end = endDate ?: return 0
            if (type == MembershipType.LIFETIME) return Int.MAX_VALUE
            val diff = end.toDate().time - System.currentTimeMillis()
            return (diff / (1000L * 60 * 60 * 24)).toInt().coerceAtLeast(0)
        }
}

// ── Plan definitions shown in PremiumScreen ────────────────────────────────────
data class PremiumPlan(
    val type: MembershipType,
    val price: String,
    val pricePerMonth: String,
    val duration: String,
    val benefits: List<String>,
    val badge: MembershipBadge = MembershipBadge.NONE,
    val isRecommended: Boolean = false,
    val savingsLabel: String = ""
)

val premiumPlans: List<PremiumPlan> = listOf(
    PremiumPlan(
        type          = MembershipType.WEEKLY,
        price         = "₹49",
        pricePerMonth = "₹196/mo",
        duration      = "7 Days",
        benefits      = listOf(
            "All premium notes",
            "Exclusive quizzes",
            "Ad-free experience"
        ),
        badge         = MembershipBadge.PREMIUM
    ),
    PremiumPlan(
        type          = MembershipType.MONTHLY,
        price         = "₹149",
        pricePerMonth = "₹149/mo",
        duration      = "30 Days",
        benefits      = listOf(
            "All premium notes",
            "Exclusive quizzes & tests",
            "Ad-free experience",
            "Priority support"
        ),
        badge         = MembershipBadge.PREMIUM
    ),
    PremiumPlan(
        type          = MembershipType.YEARLY,
        price         = "₹999",
        pricePerMonth = "₹83/mo",
        duration      = "365 Days",
        benefits      = listOf(
            "Everything in Monthly",
            "Early access resources",
            "Premium leaderboard badge",
            "Download priority"
        ),
        badge         = MembershipBadge.VIP,
        isRecommended = true,
        savingsLabel  = "Save 44%"
    ),
    PremiumPlan(
        type          = MembershipType.LIFETIME,
        price         = "₹1,999",
        pricePerMonth = "One-time",
        duration      = "Forever",
        benefits      = listOf(
            "Everything in Yearly",
            "Lifetime access",
            "Exclusive LIFETIME badge",
            "All future features free"
        ),
        badge         = MembershipBadge.LIFETIME,
        savingsLabel  = "Best Value"
    )
)

// ── Extension: simple fallback from User.isPremium (primary source is premiumUsers) ─
fun User.toPremiumMembership(): PremiumMembership {
    if (!isPremium) return PremiumMembership()
    return PremiumMembership(
        type     = MembershipType.MONTHLY,
        isActive = true,
        badge    = MembershipBadge.PREMIUM
    )
}
