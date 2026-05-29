package com.nexttoppers.feed.ui.premium

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nexttoppers.feed.data.model.MembershipBadge
import com.nexttoppers.feed.data.model.MembershipType
import com.nexttoppers.feed.data.model.PremiumMembership
import com.nexttoppers.feed.ui.theme.BackgroundBlack
import com.nexttoppers.feed.ui.theme.NeonGreen
import com.nexttoppers.feed.ui.theme.PremiumGold
import com.nexttoppers.feed.ui.theme.PremiumViolet
import com.nexttoppers.feed.ui.theme.SurfaceCard
import com.nexttoppers.feed.ui.theme.TextPrimary
import com.nexttoppers.feed.ui.theme.TextSecondary
import kotlin.random.Random

// ── Upgrade Success Screen ─────────────────────────────────────────────────────
@Composable
fun UpgradeSuccessScreen(
    onContinue: () -> Unit,
    viewModel: PremiumViewModel = hiltViewModel()
) {
    val membership by viewModel.membership.collectAsState()
    UpgradeSuccessContent(membership = membership, onContinue = onContinue)
}

@Composable
fun UpgradeSuccessContent(
    membership: PremiumMembership,
    onContinue: () -> Unit
) {
    val transition = rememberInfiniteTransition(label = "success")
    val crownScale by transition.animateFloat(
        initialValue  = 0.9f,
        targetValue   = 1.08f,
        animationSpec = infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label         = "crownScale"
    )
    val glowAlpha by transition.animateFloat(
        initialValue  = 0.3f,
        targetValue   = 0.9f,
        animationSpec = infiniteRepeatable(tween(1500, easing = LinearEasing), RepeatMode.Reverse),
        label         = "successGlow"
    )
    val particleTick by transition.animateFloat(
        initialValue  = 0f,
        targetValue   = 1f,
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing), RepeatMode.Restart),
        label         = "particleTick"
    )

    val particles = remember { List(60) { randomConfettiParticle() } }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundBlack),
        contentAlignment = Alignment.Center
    ) {
        // Confetti canvas layer
        Canvas(modifier = Modifier.fillMaxSize()) {
            particles.forEach { p -> drawConfetti(p, particleTick, size.width, size.height) }
        }

        // Radial ambient glow
        Box(
            modifier = Modifier
                .size(360.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(PremiumGold.copy(glowAlpha * 0.14f), Color.Transparent)
                    ),
                    CircleShape
                )
        )

        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Animated crown
            Text("👑", fontSize = 80.sp, modifier = Modifier.scale(crownScale))

            // Headline
            Text(
                "You're Premium!",
                style = TextStyle(
                    fontSize   = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    brush      = Brush.horizontalGradient(listOf(PremiumGold, PremiumViolet))
                ),
                textAlign = TextAlign.Center
            )

            Text(
                "Welcome to the exclusive circle of top toppers. All premium features are now unlocked.",
                color     = TextSecondary,
                fontSize  = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            // Membership summary card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Brush.horizontalGradient(listOf(PremiumGold.copy(0.1f), PremiumViolet.copy(0.1f))))
                    .border(
                        1.5.dp,
                        Brush.horizontalGradient(listOf(PremiumGold.copy(glowAlpha), PremiumViolet.copy(glowAlpha))),
                        RoundedCornerShape(20.dp)
                    )
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Text(
                            "${membership.type.displayName} Premium",
                            color      = PremiumGold,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize   = 16.sp
                        )
                        if (membership.badge != MembershipBadge.NONE) {
                            PremiumBadge(badge = membership.badge)
                        }
                    }
                    val expiryText = when (membership.type) {
                        MembershipType.LIFETIME -> "Lifetime Access — Never Expires 🚀"
                        else -> "${membership.daysRemaining} days remaining"
                    }
                    Text(expiryText, color = TextSecondary, fontSize = 13.sp)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Premium notes & lectures", "Exclusive quizzes & tests", "Ad-free experience").forEach { f ->
                            Row(
                                verticalAlignment     = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("✓", color = NeonGreen, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                                Text(f, color = TextPrimary, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            // Continue button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Brush.horizontalGradient(listOf(PremiumGold, PremiumViolet)))
                    .clickable(onClick = onContinue)
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Start Learning →",
                    color      = BackgroundBlack,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize   = 17.sp
                )
            }
        }
    }
}

// ── Confetti helpers ───────────────────────────────────────────────────────────
private data class ConfettiParticle(
    val startX: Float,
    val startY: Float,
    val speed: Float,
    val color: Color,
    val radius: Float,
    val phase: Float
)

private val confettiColors = listOf(
    PremiumGold, PremiumViolet, NeonGreen, Color(0xFFFF3CAC), Color(0xFF00E5FF)
)

private fun randomConfettiParticle(): ConfettiParticle = ConfettiParticle(
    startX = Random.nextFloat(),
    startY = Random.nextFloat() * -0.5f,
    speed  = 0.08f + Random.nextFloat() * 0.25f,
    color  = confettiColors[Random.nextInt(confettiColors.size)],
    radius = 3f + Random.nextFloat() * 7f,
    phase  = Random.nextFloat()
)

private fun DrawScope.drawConfetti(p: ConfettiParticle, tick: Float, w: Float, h: Float) {
    val x = ((p.startX + p.phase * 0.3f) % 1f) * w
    val y = ((p.startY + tick * p.speed + p.phase) % 1.3f) * h
    if (y < 0f) return
    drawCircle(
        color  = p.color.copy(alpha = 0.65f),
        radius = p.radius,
        center = Offset(x, y)
    )
}
