package com.nexttoppers.feed.ui.streak

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.nexttoppers.feed.ui.theme.BackgroundBlack
import com.nexttoppers.feed.ui.theme.ErrorRed
import com.nexttoppers.feed.ui.theme.NeonGreen
import com.nexttoppers.feed.ui.theme.PremiumGold
import com.nexttoppers.feed.ui.theme.SurfaceCard
import com.nexttoppers.feed.ui.theme.SurfaceElevated
import com.nexttoppers.feed.ui.theme.TextMuted
import com.nexttoppers.feed.ui.theme.TextPrimary
import com.nexttoppers.feed.ui.theme.TextSecondary

// ── Streak warning banner (streak will break today if no activity) ─────────────
@Composable
fun StreakWarningCard(
    streak: Int,
    onStudyNow: () -> Unit,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "streakWarn")
    val pulseAlpha by transition.animateFloat(
        initialValue  = 0.4f,
        targetValue   = 0.9f,
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing), RepeatMode.Reverse),
        label         = "streakWarnAlpha"
    )
    val fireScale by transition.animateFloat(
        initialValue  = 0.95f,
        targetValue   = 1.08f,
        animationSpec = infiniteRepeatable(tween(700, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label         = "fireScale"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.horizontalGradient(listOf(ErrorRed.copy(0.12f), ErrorRed.copy(0.06f))))
            .border(1.5.dp, ErrorRed.copy(pulseAlpha), RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(
                Icons.Rounded.LocalFireDepartment, null,
                tint     = ErrorRed,
                modifier = Modifier.size(30.dp).scale(fireScale)
            )
            Column(Modifier.weight(1f)) {
                Text(
                    "🔥 $streak-Day Streak at Risk!",
                    color      = ErrorRed,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize   = 14.sp
                )
                Text("Study something today to keep your streak alive.", color = TextSecondary, fontSize = 12.sp)
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(NeonGreen)
                    .clickable(onClick = onStudyNow)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text("Study →", color = BackgroundBlack, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
            }
        }
    }
}

// ── Missed streak dialog ───────────────────────────────────────────────────────
@Composable
fun MissedStreakDialog(
    previousStreak: Int,
    onDismiss: () -> Unit,
    onRestart: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(SurfaceCard)
                .border(1.5.dp, ErrorRed.copy(0.5f), RoundedCornerShape(24.dp))
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text("💔", fontSize = 52.sp, textAlign = TextAlign.Center)
                Text(
                    "Streak Broken",
                    color      = ErrorRed,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize   = 20.sp,
                    textAlign  = TextAlign.Center
                )
                Text(
                    "Your $previousStreak-day streak ended. Don't give up — start a new one today!",
                    color     = TextSecondary,
                    fontSize  = 13.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 19.sp
                )
                // Motivational stats
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StreakStatChip("🔥", "Previous", "$previousStreak days", ErrorRed)
                    StreakStatChip("⚡", "New Goal", "${previousStreak + 1} days", NeonGreen)
                }
                // Restart button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Brush.horizontalGradient(listOf(NeonGreen.copy(0.8f), NeonGreen)))
                        .clickable(onClick = onRestart)
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Rounded.RestartAlt, null, tint = BackgroundBlack, modifier = Modifier.size(18.dp))
                        Text("Start New Streak", color = BackgroundBlack, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                    }
                }
                Text("Close", color = TextMuted, fontSize = 12.sp, modifier = Modifier.clickable(onClick = onDismiss))
            }
        }
    }
}

@Composable
private fun StreakStatChip(emoji: String, label: String, value: String, color: Color) {
    Column(
        modifier            = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(0.08f))
            .border(1.dp, color.copy(0.3f), RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(emoji, fontSize = 18.sp)
        Text(value, color = color, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
        Text(label, color = TextMuted, fontSize = 10.sp)
    }
}

// ── Streak restore placeholder card ───────────────────────────────────────────
@Composable
fun StreakRestoreCard(
    onRestoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.horizontalGradient(listOf(PremiumGold.copy(0.08f), SurfaceCard)))
            .border(1.dp, PremiumGold.copy(0.35f), RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("🛡️", fontSize = 28.sp)
            Column(Modifier.weight(1f)) {
                Text(
                    "Streak Shield Available",
                    color      = PremiumGold,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize   = 13.sp
                )
                Text("Restore your streak with a Streak Shield (Premium feature — coming soon).", color = TextSecondary, fontSize = 11.sp)
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(PremiumGold.copy(0.15f))
                    .border(1.dp, PremiumGold.copy(0.5f), RoundedCornerShape(10.dp))
                    .clickable(onClick = onRestoreClick)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text("Soon", color = PremiumGold, fontWeight = FontWeight.Bold, fontSize = 10.sp)
            }
        }
    }
}

// ── Motivational reminder card ─────────────────────────────────────────────────
@Composable
fun MotivationalReminderCard(
    streak: Int,
    modifier: Modifier = Modifier
) {
    val msg = when {
        streak == 0  -> "🚀 Start your first day streak today — every champion starts at day 1!"
        streak < 7   -> "💪 Day $streak! Keep going — 7-day milestone is almost here!"
        streak < 30  -> "🔥 $streak days strong! You're building incredible habits."
        streak < 100 -> "⚡ $streak-day streak — you're a study machine!"
        else         -> "👑 $streak days! You are a Next Topper legend."
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.horizontalGradient(listOf(NeonGreen.copy(0.06f), SurfaceCard)))
            .border(1.dp, NeonGreen.copy(0.2f), RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Text(msg, color = NeonGreen.copy(0.9f), fontSize = 13.sp, lineHeight = 19.sp)
    }
}
