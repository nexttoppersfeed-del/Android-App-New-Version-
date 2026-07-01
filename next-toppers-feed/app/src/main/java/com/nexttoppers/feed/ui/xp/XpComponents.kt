package com.nexttoppers.feed.ui.xp

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.nexttoppers.feed.util.LevelUtils
import com.nexttoppers.feed.ui.theme.BackgroundBlack
import com.nexttoppers.feed.ui.theme.NeonCyan
import com.nexttoppers.feed.ui.theme.NeonGreen
import com.nexttoppers.feed.ui.theme.PremiumGold
import com.nexttoppers.feed.ui.theme.SurfaceCard
import com.nexttoppers.feed.ui.theme.SurfaceElevated
import com.nexttoppers.feed.ui.theme.TextMuted
import com.nexttoppers.feed.ui.theme.TextPrimary
import com.nexttoppers.feed.ui.theme.TextSecondary

// ── Animated XP level progress bar ────────────────────────────────────────────
@Composable
fun LevelProgressBar(xp: Long, modifier: Modifier = Modifier) {
    val level    = LevelUtils.levelForXp(xp)
    val progress = LevelUtils.progressToNextLevel(xp)
    val xpToNext = LevelUtils.xpToNextLevel(xp)
    val title    = LevelUtils.levelTitle(level)

    var triggered by remember { mutableStateOf(false) }
    LaunchedEffect(xp) { triggered = true }

    val animatedProgress by animateFloatAsState(
        targetValue   = if (triggered) progress else 0f,
        animationSpec = tween(900, easing = FastOutSlowInEasing),
        label         = "xpProgress"
    )

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape  = RoundedCornerShape(10.dp),
                    color  = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(32.dp)) {
                        Text(
                            "$level",
                            color      = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize   = 13.sp
                        )
                    }
                }
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(
                        title,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 13.sp,
                        color      = NeonGreen
                    )
                    Text("$xp XP total", color = TextMuted, fontSize = 10.sp)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Lv ${level + 1}", color = TextSecondary, fontSize = 11.sp)
                Text("$xpToNext XP to go", color = TextMuted, fontSize = 10.sp)
            }
        }

        // Progress bar using M3 surface colors
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(50.dp))
                .background(SurfaceElevated)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .height(10.dp)
                    .clip(RoundedCornerShape(50.dp))
                    .background(Brush.horizontalGradient(listOf(NeonGreen, NeonCyan)))
            )
        }
    }
}

// ── Level card (Material 3 ElevatedCard) ───────────────────────────────────────
@Composable
fun GlowingLevelCard(xp: Long, modifier: Modifier = Modifier) {
    val level = LevelUtils.levelForXp(xp)

    ElevatedCard(
        modifier  = modifier,
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier              = Modifier.padding(16.dp),
            verticalArrangement   = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape  = RoundedCornerShape(14.dp),
                    color  = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(48.dp)) {
                        Icon(
                            Icons.Rounded.Star,
                            null,
                            tint     = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
                Column {
                    Text(
                        "Level $level — ${LevelUtils.levelTitle(level)}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize   = 15.sp,
                        color      = NeonGreen
                    )
                    Text("$xp XP earned", color = TextSecondary, fontSize = 12.sp)
                }
            }
            LevelProgressBar(xp = xp)
        }
    }
}

// ── Streak card (Material 3 Card) ─────────────────────────────────────────────
@Composable
fun StreakCard(streak: Int, modifier: Modifier = Modifier) {
    val streakColor = when {
        streak >= 30 -> PremiumGold
        streak >= 7  -> Color(0xFFFF6B35)
        streak > 0   -> Color(0xFFFF8C42)
        else         -> TextSecondary
    }

    val infiniteTransition = rememberInfiniteTransition(label = "flameAnim")
    val flameScale by infiniteTransition.animateFloat(
        initialValue  = 0.92f,
        targetValue   = 1.08f,
        animationSpec = infiniteRepeatable(tween(800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label         = "flameScale"
    )

    Card(
        modifier  = modifier,
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(
            containerColor = streakColor.copy(0.08f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier              = Modifier.padding(16.dp),
            verticalArrangement   = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Rounded.LocalFireDepartment,
                    null,
                    tint     = streakColor,
                    modifier = Modifier.size(28.dp).scale(if (streak > 0) flameScale else 1f)
                )
                Text("Study Streak", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            Row(
                verticalAlignment     = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    "$streak",
                    fontSize   = 36.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color      = if (streak > 0) streakColor else TextSecondary
                )
                Text("days", color = TextSecondary, fontSize = 14.sp, modifier = Modifier.padding(bottom = 6.dp))
            }
            Text(
                when {
                    streak == 0 -> "Start your streak today!"
                    streak < 7  -> "Keep going — 7-day streak rewards await!"
                    streak < 30 -> "Blazing! 30-day legend milestone ahead"
                    else        -> "Legendary streak! You're unstoppable"
                },
                color      = streakColor.copy(0.8f),
                fontSize   = 11.sp,
                lineHeight = 16.sp
            )
        }
    }
}

// ── Rank card (Material 3 Card) ───────────────────────────────────────────────
@Composable
fun RankCard(rank: Int, xp: Long, modifier: Modifier = Modifier) {
    val rankColor = when {
        rank == 1  -> PremiumGold
        rank <= 3  -> NeonCyan
        rank <= 10 -> NeonGreen
        else       -> TextSecondary
    }

    Card(
        modifier  = modifier,
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(
            containerColor = rankColor.copy(0.08f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier            = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Rounded.EmojiEvents, null, tint = rankColor, modifier = Modifier.size(26.dp))
                Text("Your Rank", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (rank in 1..3) {
                    val medalColors = listOf(PremiumGold, Color(0xFFC0C0C0), Color(0xFFCD7F32))
                    Surface(
                        shape  = RoundedCornerShape(12.dp),
                        color  = medalColors[rank - 1].copy(0.15f),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(48.dp)) {
                            Text("#$rank", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = medalColors[rank - 1])
                        }
                    }
                } else {
                    Text(
                        if (rank > 0) "#$rank" else "–",
                        fontSize   = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color      = rankColor
                    )
                }
                Column {
                    Text(
                        if (rank > 0) "Global Rank" else "Unranked",
                        color    = TextSecondary,
                        fontSize = 11.sp
                    )
                    Text("$xp XP", color = rankColor, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                }
            }
        }
    }
}

// ── Level-up dialog (Material 3 AlertDialog) ──────────────────────────────────
@Composable
fun LevelUpDialog(newLevel: Int, onDismiss: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Dialog(onDismissRequest = onDismiss) {
        AnimatedVisibility(
            visible = visible,
            enter   = fadeIn(tween(300)) + scaleIn(tween(300, easing = FastOutSlowInEasing), 0.8f)
        ) {
            ElevatedCard(
                shape     = RoundedCornerShape(28.dp),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier            = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Surface(
                        shape  = RoundedCornerShape(20.dp),
                        color  = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(72.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(72.dp)) {
                            Icon(
                                Icons.Rounded.Bolt,
                                null,
                                tint     = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(42.dp)
                            )
                        }
                    }

                    Text(
                        "LEVEL UP!",
                        fontSize   = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color      = NeonGreen
                    )

                    Surface(
                        shape = RoundedCornerShape(50.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            "Level $newLevel — ${LevelUtils.levelTitle(newLevel)}",
                            color      = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize   = 14.sp,
                            modifier   = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                        )
                    }

                    Text(
                        "Keep studying to reach the next milestone!",
                        color     = TextSecondary,
                        fontSize  = 13.sp,
                        textAlign = TextAlign.Center
                    )

                    Button(
                        onClick   = onDismiss,
                        modifier  = Modifier.fillMaxWidth(),
                        shape     = RoundedCornerShape(14.dp)
                    ) {
                        Text("Awesome!", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
        }
    }
}
