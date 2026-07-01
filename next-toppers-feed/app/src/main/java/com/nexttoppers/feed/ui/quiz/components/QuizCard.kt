package com.nexttoppers.feed.ui.quiz.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Quiz
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexttoppers.feed.data.model.Quiz
import com.nexttoppers.feed.data.model.QuizDifficulty
import com.nexttoppers.feed.data.model.ResourceSubject
import com.nexttoppers.feed.ui.resources.components.subjectAccentColor
import com.nexttoppers.feed.ui.theme.BackgroundBlack
import com.nexttoppers.feed.ui.theme.NeonCyan
import com.nexttoppers.feed.ui.theme.NeonGreen
import com.nexttoppers.feed.ui.theme.PremiumGold
import com.nexttoppers.feed.ui.theme.SurfaceCard
import com.nexttoppers.feed.ui.theme.TextMuted
import com.nexttoppers.feed.ui.theme.TextPrimary
import com.nexttoppers.feed.ui.theme.TextSecondary

// ── Standard quiz list card ────────────────────────────────────────────────────
@Composable
fun QuizCard(
    quiz: Quiz,
    index: Int = 0,
    attempted: Boolean = false,
    onClick: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(280, delayMillis = index * 55)) +
                slideInVertically(tween(280, delayMillis = index * 55)) { it / 4 }
    ) {
        val subjectEnum = ResourceSubject.values().firstOrNull { it.name.equals(quiz.subject, ignoreCase = true) }
        val accent = subjectEnum?.let { subjectAccentColor(it) } ?: NeonGreen
        val diffEnum = quiz.difficultyEnum()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(SurfaceCard)
                .border(
                    1.dp,
                    if (quiz.premium) PremiumGold.copy(0.3f) else accent.copy(0.2f),
                    RoundedCornerShape(18.dp)
                )
                .clickable(onClick = onClick)
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon box
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .background(accent.copy(0.12f), RoundedCornerShape(14.dp))
                    .border(1.dp, accent.copy(0.3f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(subjectEnum?.emoji ?: "📝", fontSize = 22.sp)
                if (quiz.premium) {
                    Box(
                        Modifier
                            .align(Alignment.TopEnd)
                            .size(16.dp)
                            .background(PremiumGold, RoundedCornerShape(4.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.Lock, null, tint = BackgroundBlack, modifier = Modifier.size(10.dp))
                    }
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    DifficultyChip(diffEnum)
                    if (attempted) {
                        Box(
                            Modifier
                                .background(NeonGreen.copy(0.12f), RoundedCornerShape(4.dp))
                                .border(1.dp, NeonGreen.copy(0.3f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text("Done", color = NeonGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Text(quiz.title, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                if (quiz.chapter.isNotEmpty()) {
                    Text(quiz.chapter, color = TextSecondary, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Quiz, null, tint = TextMuted, modifier = Modifier.size(11.dp))
                        Spacer(Modifier.width(3.dp))
                        Text("${quiz.totalQuestions} Qs", color = TextMuted, fontSize = 11.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.AccessTime, null, tint = TextMuted, modifier = Modifier.size(11.dp))
                        Spacer(Modifier.width(3.dp))
                        Text("${quiz.duration} min", color = TextMuted, fontSize = 11.sp)
                    }
                    XpRewardChip(quiz.xpReward, accent)
                }
            }
        }
    }
}

// ── Featured quiz card (horizontal, larger) ────────────────────────────────────
@Composable
fun FeaturedQuizCard(quiz: Quiz, onClick: () -> Unit) {
    val subjectEnum = ResourceSubject.values().firstOrNull { it.name.equals(quiz.subject, ignoreCase = true) }
    val accent = subjectEnum?.let { subjectAccentColor(it) } ?: NeonGreen
    val diffEnum = quiz.difficultyEnum()

    Column(
        modifier = Modifier
            .width(220.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(listOf(accent.copy(0.15f), SurfaceCard, SurfaceCard))
            )
            .border(
                1.dp,
                if (quiz.premium) PremiumGold.copy(0.5f) else accent.copy(0.35f),
                RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(subjectEnum?.emoji ?: "📝", fontSize = 28.sp)
            if (quiz.premium) {
                Box(
                    Modifier
                        .background(PremiumGold.copy(0.2f), RoundedCornerShape(6.dp))
                        .border(1.dp, PremiumGold.copy(0.5f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text("PRO", color = PremiumGold, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
        Text(quiz.title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 20.sp)
        if (quiz.chapter.isNotEmpty()) {
            Text(quiz.chapter, color = TextSecondary, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DifficultyChip(diffEnum)
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("${quiz.totalQuestions} Qs", color = TextMuted, fontSize = 11.sp)
            Text("•", color = TextMuted, fontSize = 11.sp)
            Text("${quiz.duration} min", color = TextMuted, fontSize = 11.sp)
        }
        XpRewardChip(quiz.xpReward, accent, large = true)
    }
}

// ── Mini quiz history card ─────────────────────────────────────────────────────
@Composable
fun QuizHistoryCard(
    title: String,
    score: Int,
    total: Int,
    xpEarned: Int,
    subject: String,
    index: Int = 0
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(250, delayMillis = index * 60))
    ) {
        val subjectEnum = ResourceSubject.values().firstOrNull { it.name.equals(subject, ignoreCase = true) }
        val accent = subjectEnum?.let { subjectAccentColor(it) } ?: NeonGreen
        val pct = if (total > 0) score * 100 / total else 0

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(SurfaceCard)
                .border(1.dp, accent.copy(0.18f), RoundedCornerShape(14.dp))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Rounded.CheckCircle, null, tint = if (pct >= 60) NeonGreen else NeonCyan.copy(0.6f), modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = TextPrimary, fontWeight = FontWeight.Medium, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("$score/$total correct • $pct%", color = TextSecondary, fontSize = 11.sp)
            }
            Box(
                Modifier
                    .background(NeonGreen.copy(0.12f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("+${xpEarned} XP", color = NeonGreen, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        }
    }
}

// ── Difficulty chip ────────────────────────────────────────────────────────────
@Composable
fun DifficultyChip(difficulty: QuizDifficulty) {
    val color = when (difficulty) {
        QuizDifficulty.EASY   -> Color(0xFF80CBC4)
        QuizDifficulty.MEDIUM -> Color(0xFFFFB347)
        QuizDifficulty.HARD   -> Color(0xFFFF6B6B)
    }
    Box(
        Modifier
            .background(color.copy(0.12f), RoundedCornerShape(6.dp))
            .border(1.dp, color.copy(0.3f), RoundedCornerShape(6.dp))
            .padding(horizontal = 7.dp, vertical = 3.dp)
    ) {
        Text("${difficulty.emoji} ${difficulty.label}", color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

// ── XP reward chip ─────────────────────────────────────────────────────────────
@Composable
fun XpRewardChip(xp: Int, accentColor: Color = NeonGreen, large: Boolean = false) {
    Box(
        Modifier
            .background(accentColor.copy(0.12f), RoundedCornerShape(6.dp))
            .border(1.dp, accentColor.copy(0.3f), RoundedCornerShape(6.dp))
            .padding(horizontal = if (large) 10.dp else 7.dp, vertical = if (large) 4.dp else 2.dp)
    ) {
        Text(
            "+$xp XP",
            color = accentColor,
            fontSize = if (large) 12.sp else 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
