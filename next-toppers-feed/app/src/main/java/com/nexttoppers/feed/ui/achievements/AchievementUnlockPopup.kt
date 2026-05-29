package com.nexttoppers.feed.ui.achievements

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.nexttoppers.feed.ui.theme.BackgroundBlack
import com.nexttoppers.feed.ui.theme.NeonGreen
import com.nexttoppers.feed.ui.theme.PremiumGold
import com.nexttoppers.feed.ui.theme.PremiumViolet
import com.nexttoppers.feed.ui.theme.SurfaceCard
import com.nexttoppers.feed.ui.theme.TextMuted
import com.nexttoppers.feed.ui.theme.TextPrimary
import com.nexttoppers.feed.ui.theme.TextSecondary
import kotlin.random.Random

// ── Data class for achievement popup payload ───────────────────────────────────
data class AchievementUnlockData(
    val emoji: String,
    val title: String,
    val description: String,
    val xpReward: Long = 0L
)

// ── Main achievement unlock popup ─────────────────────────────────────────────
@Composable
fun AchievementUnlockPopup(
    achievement: AchievementUnlockData,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties       = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        val transition = rememberInfiniteTransition(label = "achieve")
        val badgeScale by transition.animateFloat(
            initialValue  = 0.92f,
            targetValue   = 1.08f,
            animationSpec = infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
            label         = "badgeScale"
        )
        val glowAlpha by transition.animateFloat(
            initialValue  = 0.4f,
            targetValue   = 0.95f,
            animationSpec = infiniteRepeatable(tween(1200, easing = LinearEasing), RepeatMode.Reverse),
            label         = "achieveGlow"
        )
        val confettiTick by transition.animateFloat(
            initialValue  = 0f,
            targetValue   = 1f,
            animationSpec = infiniteRepeatable(tween(2500, easing = LinearEasing), RepeatMode.Restart),
            label         = "confettiTick"
        )

        val particles = remember { List(50) { randomAchievParticle() } }

        Box(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .clip(RoundedCornerShape(28.dp))
                .background(SurfaceCard)
                .border(
                    2.dp,
                    Brush.linearGradient(listOf(PremiumGold.copy(glowAlpha), PremiumViolet.copy(glowAlpha * 0.7f))),
                    RoundedCornerShape(28.dp)
                )
        ) {
            // Confetti canvas overlay
            Canvas(modifier = Modifier.fillMaxWidth().size(200.dp)) {
                particles.forEach { p -> drawAchievParticle(p, confettiTick, size.width) }
            }

            Column(
                modifier            = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Badge glow ring
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(
                            Brush.radialGradient(listOf(PremiumGold.copy(glowAlpha * 0.2f), Color.Transparent)),
                            CircleShape
                        )
                        .border(2.dp, Brush.linearGradient(listOf(PremiumGold, PremiumViolet)), CircleShape)
                        .scale(badgeScale),
                    contentAlignment = Alignment.Center
                ) {
                    Text(achievement.emoji, fontSize = 48.sp)
                }

                // Headline
                Text(
                    "Achievement Unlocked!",
                    style = TextStyle(
                        fontSize   = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        brush      = Brush.horizontalGradient(listOf(PremiumGold, PremiumViolet))
                    ),
                    textAlign = TextAlign.Center
                )

                Text(
                    achievement.title,
                    color      = TextPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize   = 20.sp,
                    textAlign  = TextAlign.Center
                )

                Text(
                    achievement.description,
                    color     = TextSecondary,
                    fontSize  = 13.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 19.sp
                )

                if (achievement.xpReward > 0) {
                    Box(
                        modifier = Modifier
                            .background(NeonGreen.copy(0.1f), RoundedCornerShape(12.dp))
                            .border(1.dp, NeonGreen.copy(0.4f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text("⚡ +${achievement.xpReward} XP Bonus", color = NeonGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                // Dismiss button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Brush.horizontalGradient(listOf(PremiumGold, PremiumViolet)))
                        .clickable(onClick = onDismiss)
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Awesome! 🎉", color = BackgroundBlack, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                }

                Text("Tap anywhere to dismiss", color = TextMuted, fontSize = 10.sp)
            }
        }
    }
}

// ── Confetti particles ─────────────────────────────────────────────────────────
private data class AchievParticle(val x: Float, val y: Float, val speed: Float, val color: Color, val r: Float, val phase: Float)

private val achievColors = listOf(PremiumGold, PremiumViolet, NeonGreen, Color(0xFFFF3CAC), Color(0xFF00E5FF))

private fun randomAchievParticle(): AchievParticle = AchievParticle(
    x     = Random.nextFloat(),
    y     = Random.nextFloat() * -0.3f,
    speed = 0.1f + Random.nextFloat() * 0.3f,
    color = achievColors[Random.nextInt(achievColors.size)],
    r     = 3f + Random.nextFloat() * 6f,
    phase = Random.nextFloat()
)

private fun DrawScope.drawAchievParticle(p: AchievParticle, tick: Float, w: Float) {
    val x = ((p.x + p.phase * 0.4f) % 1f) * w
    val y = ((p.y + tick * p.speed + p.phase) % 1.4f) * size.height
    if (y < 0f) return
    drawCircle(p.color.copy(0.7f), p.r, Offset(x, y))
}

// ── Predefined common achievements ────────────────────────────────────────────
object CommonAchievements {
    fun streak(days: Int) = AchievementUnlockData(
        emoji       = "🔥",
        title       = "$days-Day Streak!",
        description = "You studied $days days in a row. Keep it up, topper!",
        xpReward    = days.toLong() * 5
    )

    fun level(level: Int) = AchievementUnlockData(
        emoji       = "⬆️",
        title       = "Level $level Reached!",
        description = "You've levelled up to Level $level through hard work and dedication.",
        xpReward    = 0L
    )

    fun quizzes(count: Int) = AchievementUnlockData(
        emoji       = "🎯",
        title       = "$count Quizzes Completed!",
        description = "You've completed $count quizzes and strengthened your knowledge.",
        xpReward    = 50L
    )

    fun rank(rank: Int) = AchievementUnlockData(
        emoji       = "🏆",
        title       = "Top $rank on Leaderboard!",
        description = "You've climbed to #$rank on the leaderboard. You're crushing it!",
        xpReward    = 0L
    )
}
