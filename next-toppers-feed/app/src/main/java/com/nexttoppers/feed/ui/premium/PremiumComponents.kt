package com.nexttoppers.feed.ui.premium

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Diamond
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.nexttoppers.feed.data.model.MembershipBadge
import com.nexttoppers.feed.data.model.MembershipType
import com.nexttoppers.feed.data.model.PremiumMembership
import com.nexttoppers.feed.data.model.PremiumPlan
import com.nexttoppers.feed.ui.theme.BackgroundBlack
import com.nexttoppers.feed.ui.theme.NeonCyan
import com.nexttoppers.feed.ui.theme.NeonGreen
import com.nexttoppers.feed.ui.theme.PremiumGold
import com.nexttoppers.feed.ui.theme.PremiumGoldDim
import com.nexttoppers.feed.ui.theme.PremiumGoldGlow
import com.nexttoppers.feed.ui.theme.PremiumRose
import com.nexttoppers.feed.ui.theme.PremiumViolet
import com.nexttoppers.feed.ui.theme.SurfaceCard
import com.nexttoppers.feed.ui.theme.SurfaceElevated
import com.nexttoppers.feed.ui.theme.TextMuted
import com.nexttoppers.feed.ui.theme.TextPrimary
import com.nexttoppers.feed.ui.theme.TextSecondary

// ── Helpers ────────────────────────────────────────────────────────────────────
private fun badgeColors(badge: MembershipBadge): List<Color> = when (badge) {
    MembershipBadge.LIFETIME     -> listOf(PremiumViolet, PremiumRose)
    MembershipBadge.VIP          -> listOf(PremiumGold, PremiumRose)
    MembershipBadge.EARLY_MEMBER -> listOf(NeonCyan, NeonGreen)
    else                         -> listOf(PremiumGold, PremiumGoldDim)
}

private fun badgeIcon(badge: MembershipBadge): ImageVector = when (badge) {
    MembershipBadge.LIFETIME     -> Icons.Rounded.Diamond
    MembershipBadge.VIP          -> Icons.Rounded.WorkspacePremium
    MembershipBadge.EARLY_MEMBER -> Icons.Rounded.Bolt
    else                         -> Icons.Rounded.Star
}

@Composable
private fun BadgeIcon(badge: MembershipBadge, modifier: Modifier = Modifier) {
    val colors = badgeColors(badge)
    Icon(badgeIcon(badge), null, tint = colors.first(), modifier = modifier)
}

// ── PremiumBadge — animated glowing badge ─────────────────────────────────────
@Composable
fun PremiumBadge(
    badge: MembershipBadge,
    modifier: Modifier = Modifier
) {
    if (badge == MembershipBadge.NONE) return
    val colors = badgeColors(badge)
    androidx.compose.material3.Surface(
        shape  = RoundedCornerShape(50.dp),
        color  = colors.first().copy(0.18f),
        modifier = modifier,
        border = androidx.compose.foundation.BorderStroke(1.dp, colors.first().copy(0.5f))
    ) {
        Row(
            modifier              = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            BadgeIcon(badge, modifier = Modifier.size(12.dp))
            Text(
                badge.label,
                color      = colors.first(),
                fontWeight = FontWeight.ExtraBold,
                fontSize   = 10.sp
            )
        }
    }
}

// ── PremiumBannerCard — home screen contextual banner ─────────────────────────
@Composable
fun PremiumBannerCard(
    isPremium: Boolean,
    membership: PremiumMembership = PremiumMembership(),
    onUpgradeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "banner")
    val shimmer by transition.animateFloat(
        initialValue  = 0f,
        targetValue   = 1000f,
        animationSpec = infiniteRepeatable(tween(2200, easing = FastOutSlowInEasing), RepeatMode.Restart),
        label         = "bannerShimmer"
    )
    val glowAlpha by transition.animateFloat(
        initialValue  = 0.3f,
        targetValue   = 0.7f,
        animationSpec = infiniteRepeatable(tween(1400, easing = LinearEasing), RepeatMode.Reverse),
        label         = "bannerGlow"
    )

    if (isPremium) {
        // Premium member — show status card
        val colors = badgeColors(membership.badge)
        androidx.compose.material3.Card(
            modifier  = modifier.fillMaxWidth(),
            shape     = RoundedCornerShape(20.dp),
            colors    = androidx.compose.material3.CardDefaults.cardColors(
                containerColor = colors.first().copy(0.10f)
            ),
            elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 0.dp),
            border    = androidx.compose.foundation.BorderStroke(1.5.dp, colors.first().copy(0.5f))
        ) {
            Row(
                modifier          = Modifier.padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Rounded.WorkspacePremium, null, tint = colors.first(), modifier = Modifier.size(32.dp))
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        "${membership.badge.label.ifEmpty { "PREMIUM" }} MEMBER",
                        color      = colors.first(),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize   = 14.sp
                    )
                    val sub = when (membership.type) {
                        MembershipType.LIFETIME -> "Lifetime access — all features unlocked"
                        else -> if (membership.daysRemaining > 0)
                            "${membership.daysRemaining} days remaining • All features unlocked"
                        else "All features unlocked"
                    }
                    Text(sub, color = TextSecondary, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    } else {
        // Free user — upgrade CTA
        androidx.compose.material3.Card(
            onClick   = onUpgradeClick,
            modifier  = modifier.fillMaxWidth(),
            shape     = RoundedCornerShape(20.dp),
            colors    = androidx.compose.material3.CardDefaults.cardColors(
                containerColor = PremiumGold.copy(0.07f)
            ),
            elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 0.dp),
            border    = androidx.compose.foundation.BorderStroke(1.5.dp, PremiumGold.copy(0.45f))
        ) {
            Row(
                modifier          = Modifier.padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                androidx.compose.material3.Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = PremiumGold.copy(0.12f),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(44.dp)) {
                        Icon(Icons.Rounded.WorkspacePremium, null, tint = PremiumGold, modifier = Modifier.size(26.dp))
                    }
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        "Upgrade to Premium",
                        color      = PremiumGold,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize   = 14.sp
                    )
                    Text(
                        "Unlock all notes, quizzes & exclusive content",
                        color    = TextSecondary,
                        fontSize = 12.sp
                    )
                }
                Spacer(Modifier.width(8.dp))
                Column(
                    modifier            = Modifier
                        .background(PremiumGold.copy(0.15f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("₹83", color = PremiumGold, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                    Text("/mo", color = TextMuted, fontSize = 9.sp)
                }
            }
        }
    }
}

// ── CurrentMembershipCard — detailed membership (profile screen) ───────────────
@Composable
fun CurrentMembershipCard(
    membership: PremiumMembership,
    onManageClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "memberCard")
    val glowAlpha by transition.animateFloat(
        initialValue  = 0.35f,
        targetValue   = 0.8f,
        animationSpec = infiniteRepeatable(tween(1600, easing = LinearEasing), RepeatMode.Reverse),
        label         = "memberCardGlow"
    )

    val (borderColors, bgColors) = if (membership.isActive) {
        val c = badgeColors(membership.badge)
        Pair(c, c.map { it.copy(0.1f) })
    } else {
        Pair(listOf(TextMuted, TextMuted), listOf(SurfaceCard, SurfaceElevated))
    }

    androidx.compose.material3.Card(
        modifier  = modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(20.dp),
        colors    = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = if (membership.isActive) borderColors.first().copy(0.08f) else SurfaceCard
        ),
        elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 0.dp),
        border    = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            borderColors.first().copy(if (membership.isActive) 0.5f else 0.25f)
        )
    ) {
    Column(
        modifier            = Modifier.padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(
                        if (membership.isActive) Icons.Rounded.Diamond else Icons.Rounded.Star,
                        null,
                        tint     = if (membership.isActive) borderColors.first() else TextMuted,
                        modifier = Modifier.size(28.dp)
                    )
                    Column {
                        Text(
                            if (membership.isActive) membership.type.displayName + " Premium" else "Free Plan",
                            color      = if (membership.isActive) borderColors.first() else TextSecondary,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize   = 15.sp
                        )
                        if (membership.badge != MembershipBadge.NONE) {
                            Text(membership.badge.label, color = borderColors.first().copy(0.7f), fontSize = 11.sp)
                        }
                    }
                }
                if (membership.isActive) {
                    androidx.compose.material3.Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = NeonGreen.copy(0.15f)
                    ) {
                        Text(
                            "ACTIVE",
                            color      = NeonGreen,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize   = 10.sp,
                            modifier   = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            if (membership.isActive) {
                val expiry = when (membership.type) {
                    MembershipType.LIFETIME -> "Lifetime access — never expires"
                    else -> "${membership.daysRemaining} days remaining"
                }
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(expiry, color = TextSecondary, fontSize = 13.sp)
                    Text(
                        "Manage →",
                        color      = borderColors.first(),
                        fontSize   = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier   = Modifier.clickable(onClick = onManageClick)
                    )
                }
                // Expiry bar (only for time-limited plans)
                if (membership.type != MembershipType.LIFETIME) {
                    val totalDays = when (membership.type) {
                        MembershipType.WEEKLY  -> 7f
                        MembershipType.MONTHLY -> 30f
                        else                   -> 365f
                    }
                    val progress = (membership.daysRemaining / totalDays).coerceIn(0f, 1f)
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(SurfaceElevated)
                    ) {
                        Box(
                            Modifier
                                .fillMaxWidth(progress)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Brush.horizontalGradient(borderColors))
                        )
                    }
                }
            } else {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Limited access — upgrade for full features", color = TextMuted, fontSize = 12.sp, modifier = Modifier.weight(1f))
                    Text(
                        "Upgrade →",
                        color      = PremiumGold,
                        fontSize   = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier   = Modifier.clickable(onClick = onManageClick)
                    )
                }
            }
        }
    }
}

// ── MembershipPlanCard — plan selector card (premium screen) ──────────────────
@Composable
fun MembershipPlanCard(
    plan: PremiumPlan,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "planCard")
    val glowAlpha by transition.animateFloat(
        initialValue  = 0.4f,
        targetValue   = 0.85f,
        animationSpec = infiniteRepeatable(tween(1200, easing = LinearEasing), RepeatMode.Reverse),
        label         = "planGlow"
    )
    val selectedScale by transition.animateFloat(
        initialValue  = 1f,
        targetValue   = 1.01f,
        animationSpec = infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label         = "planScale"
    )

    val badgeColors = badgeColors(plan.badge)

    androidx.compose.material3.Card(
        onClick   = onClick,
        modifier  = modifier
            .fillMaxWidth()
            .scale(if (isSelected) selectedScale else 1f),
        shape     = RoundedCornerShape(20.dp),
        colors    = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = if (isSelected) badgeColors.first().copy(0.09f) else SurfaceCard
        ),
        elevation = androidx.compose.material3.CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 2.dp else 0.dp
        ),
        border    = androidx.compose.foundation.BorderStroke(
            if (isSelected) 1.5.dp else 1.dp,
            if (isSelected) badgeColors.first().copy(0.6f) else TextMuted.copy(0.2f)
        )
    ) {
    Column(
        modifier            = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
            // Header row: name + recommended/savings badge
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    BadgeIcon(plan.badge, modifier = Modifier.size(20.dp))
                    Text(
                        plan.type.displayName,
                        color      = if (isSelected) badgeColors.first() else TextPrimary,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize   = 16.sp
                    )
                }
                if (plan.isRecommended) {
                    androidx.compose.material3.Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = badgeColors.first().copy(0.85f)
                    ) {
                        Text(
                            "Recommended",
                            color      = SurfaceCard,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize   = 9.sp,
                            modifier   = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                } else if (plan.savingsLabel.isNotEmpty()) {
                    androidx.compose.material3.Surface(
                        shape  = RoundedCornerShape(8.dp),
                        color  = PremiumGold.copy(0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PremiumGold.copy(0.5f))
                    ) {
                        Text(
                            plan.savingsLabel,
                            color      = PremiumGold,
                            fontWeight = FontWeight.Bold,
                            fontSize   = 9.sp,
                            modifier   = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Price row
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    plan.price,
                    color      = if (isSelected) badgeColors.first() else TextPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize   = 28.sp
                )
                Text(
                    "• ${plan.duration}",
                    color    = TextSecondary,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Spacer(Modifier.weight(1f))
                Text(plan.pricePerMonth, color = TextMuted, fontSize = 11.sp, modifier = Modifier.padding(bottom = 4.dp))
            }

            // Benefits list
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                plan.benefits.take(3).forEach { benefit ->
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(
                            Icons.Rounded.CheckCircle, null,
                            tint     = if (isSelected) badgeColors.first() else NeonGreen.copy(0.7f),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(benefit, color = TextSecondary, fontSize = 12.sp)
                    }
                }
                if (plan.benefits.size > 3) {
                    Text("+${plan.benefits.size - 3} more", color = TextMuted, fontSize = 11.sp, modifier = Modifier.padding(start = 20.dp))
                }
            }
        }
    }
}

// ── PremiumBenefitItem — individual benefit row ───────────────────────────────
@Composable
fun PremiumBenefitItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier              = modifier.fillMaxWidth(),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        androidx.compose.material3.Surface(
            shape    = RoundedCornerShape(12.dp),
            color    = PremiumGold.copy(0.10f),
            modifier = Modifier.size(42.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(42.dp)) {
                Icon(icon, null, tint = PremiumGold, modifier = Modifier.size(20.dp))
            }
        }
        Column {
            Text(title, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(subtitle, color = TextMuted, fontSize = 12.sp)
        }
    }
}

// ── UpgradeDialog — shown when locked content is tapped (M3 ElevatedCard) ─────
@Composable
fun UpgradeDialog(
    onDismiss: () -> Unit,
    onUpgrade: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        androidx.compose.material3.ElevatedCard(
            shape     = RoundedCornerShape(28.dp),
            elevation = androidx.compose.material3.CardDefaults.elevatedCardElevation(defaultElevation = 6.dp),
            border    = androidx.compose.foundation.BorderStroke(1.5.dp, PremiumGold.copy(0.5f))
        ) {
            Column(
                modifier            = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                androidx.compose.material3.Surface(
                    shape    = RoundedCornerShape(22.dp),
                    color    = PremiumGold.copy(0.12f),
                    modifier = Modifier.size(80.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(80.dp)) {
                        Icon(Icons.Rounded.WorkspacePremium, null, tint = PremiumGold, modifier = Modifier.size(48.dp))
                    }
                }
                Text(
                    "Premium Content",
                    color      = PremiumGold,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize   = 20.sp,
                    textAlign  = TextAlign.Center
                )
                Text(
                    "This content is exclusive to Premium members. Upgrade now to unlock all notes, quizzes, and lectures.",
                    color      = TextSecondary,
                    fontSize   = 13.sp,
                    textAlign  = TextAlign.Center,
                    lineHeight = 20.sp
                )
                // Benefits preview
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("All premium notes", "Exclusive quizzes & tests", "Priority downloads").forEach { benefit ->
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Rounded.CheckCircle, null, tint = NeonGreen, modifier = Modifier.size(16.dp))
                            Text(benefit, color = TextPrimary, fontSize = 13.sp)
                        }
                    }
                }
                androidx.compose.material3.Button(
                    onClick  = onUpgrade,
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(16.dp),
                    colors   = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = PremiumGold,
                        contentColor   = SurfaceCard
                    )
                ) {
                    Text("Upgrade Now • From ₹49", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                }
                androidx.compose.material3.TextButton(onClick = onDismiss) {
                    Text("Cancel", color = TextMuted, fontSize = 13.sp)
                }
            }
        }
    }
}

// ── LockedContentCard — wraps any content with a premium lock overlay ──────────
@Composable
fun LockedContentCard(
    isPremium: Boolean,
    onLockedClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier) {
        Box(modifier = Modifier.then(if (!isPremium) Modifier.alpha(0.12f) else Modifier)) {
            content()
        }
        if (!isPremium) {
            val transition = rememberInfiniteTransition(label = "lock")
            val pulse by transition.animateFloat(
                initialValue  = 0.9f,
                targetValue   = 1.05f,
                animationSpec = infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
                label         = "lockPulse"
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.radialGradient(
                            colors = listOf(SurfaceCard.copy(0.92f), SurfaceCard.copy(0.97f)),
                            center = Offset(Float.POSITIVE_INFINITY / 2, Float.POSITIVE_INFINITY / 2),
                            radius = 300f
                        )
                    )
                    .border(1.dp, PremiumGold.copy(0.45f), RoundedCornerShape(16.dp))
                    .clickable(onClick = onLockedClick),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        Icons.Rounded.Lock, null,
                        tint     = PremiumGold,
                        modifier = Modifier.size(28.dp).scale(pulse)
                    )
                    Text("Premium", color = PremiumGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("Tap to unlock", color = TextSecondary, fontSize = 11.sp)
                }
            }
        }
    }
}
