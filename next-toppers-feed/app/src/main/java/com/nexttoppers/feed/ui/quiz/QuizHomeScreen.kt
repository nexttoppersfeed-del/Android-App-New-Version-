package com.nexttoppers.feed.ui.quiz

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nexttoppers.feed.data.model.QuizDifficulty
import com.nexttoppers.feed.data.model.ResourceSubject
import com.nexttoppers.feed.ui.components.NeonDivider
import com.nexttoppers.feed.ui.components.SectionHeader
import com.nexttoppers.feed.ui.components.SkeletonCard
import com.nexttoppers.feed.ui.components.SkeletonRow
import com.nexttoppers.feed.ui.quiz.components.DifficultyChip
import com.nexttoppers.feed.ui.quiz.components.FeaturedQuizCard
import com.nexttoppers.feed.ui.quiz.components.QuizCard
import com.nexttoppers.feed.ui.quiz.components.QuizHistoryCard
import com.nexttoppers.feed.ui.resources.components.FilterChip
import com.nexttoppers.feed.ui.resources.components.subjectAccentColor
import com.nexttoppers.feed.ui.theme.BackgroundBlack
import com.nexttoppers.feed.ui.theme.NeonCyan
import com.nexttoppers.feed.ui.theme.NeonGreen
import com.nexttoppers.feed.ui.theme.SurfaceCard
import com.nexttoppers.feed.ui.theme.TextMuted
import com.nexttoppers.feed.ui.theme.TextPrimary
import com.nexttoppers.feed.ui.theme.TextSecondary

@Composable
fun QuizHomeScreen(
    onNavigateToPlayer: (quizId: String) -> Unit,
    viewModel: QuizHomeViewModel = hiltViewModel()
) {
    val uiState         by viewModel.uiState.collectAsState()
    val selectedSubject by viewModel.selectedSubject.collectAsState()
    val selectedDiff    by viewModel.selectedDifficulty.collectAsState()

    Box(
        modifier = Modifier.fillMaxSize().background(BackgroundBlack)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().height(280.dp).background(
                Brush.radialGradient(listOf(NeonCyan.copy(0.06f), Color.Transparent))
            )
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 100.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // ── Header ────────────────────────────────────────────────────────
            item {
                Column {
                    Text(
                        "Quiz Center",
                        style = TextStyle(
                            fontSize = 26.sp, fontWeight = FontWeight.ExtraBold,
                            brush = Brush.linearGradient(listOf(NeonGreen, NeonCyan)),
                            shadow = Shadow(NeonGreen.copy(0.3f), Offset.Zero, 14f)
                        )
                    )
                    Text("Test your knowledge. Earn XP.", color = TextSecondary, fontSize = 13.sp)
                    Spacer(Modifier.height(20.dp))
                }
            }

            when (val state = uiState) {
                is QuizHomeUiState.Loading -> {
                    item { repeat(4) { SkeletonCard(height = 90.dp); Spacer(Modifier.height(10.dp)) } }
                }
                is QuizHomeUiState.Error -> {
                    item { Text(state.message, color = TextSecondary) }
                }
                is QuizHomeUiState.Success -> {
                    val data = state.data

                    // ── Featured ──────────────────────────────────────────────
                    if (data.featured.isNotEmpty()) {
                        item {
                            SectionHeader("⭐  Featured Quizzes")
                            Spacer(Modifier.height(12.dp))
                        }
                        item {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(end = 20.dp)
                            ) {
                                itemsIndexed(data.featured) { _, quiz ->
                                    FeaturedQuizCard(quiz = quiz, onClick = {
                                        onNavigateToPlayer(quiz.id)
                                    })
                                }
                            }
                            Spacer(Modifier.height(24.dp))
                        }
                    }

                    // ── Subject filter ────────────────────────────────────────
                    item {
                        SectionHeader("Browse Quizzes")
                        Spacer(Modifier.height(10.dp))
                    }
                    item {
                        Row(
                            Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                label    = "All Subjects",
                                selected = selectedSubject == null,
                                color    = NeonGreen,
                                onClick  = { viewModel.selectSubject(null) }
                            )
                            ResourceSubject.values().forEach { subj ->
                                FilterChip(
                                    label    = "${subj.emoji} ${subj.displayName}",
                                    selected = selectedSubject == subj,
                                    color    = subjectAccentColor(subj),
                                    onClick  = { viewModel.selectSubject(if (selectedSubject == subj) null else subj) }
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                    }

                    // ── Difficulty filter ─────────────────────────────────────
                    item {
                        Row(
                            Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip("All Levels", selectedDiff == null, NeonGreen) { viewModel.selectDifficulty(null) }
                            QuizDifficulty.values().forEach { diff ->
                                val color = when (diff) {
                                    QuizDifficulty.EASY   -> Color(0xFF80CBC4)
                                    QuizDifficulty.MEDIUM -> Color(0xFFFFB347)
                                    QuizDifficulty.HARD   -> Color(0xFFFF6B6B)
                                }
                                FilterChip(
                                    label    = "${diff.emoji} ${diff.label}",
                                    selected = selectedDiff == diff.name,
                                    color    = color,
                                    onClick  = { viewModel.selectDifficulty(if (selectedDiff == diff.name) null else diff.name) }
                                )
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        NeonDivider()
                        Spacer(Modifier.height(12.dp))
                    }

                    // ── Quiz list ─────────────────────────────────────────────
                    if (data.all.isEmpty()) {
                        item {
                            Box(Modifier.fillMaxWidth().padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("🎯", fontSize = 40.sp)
                                    Spacer(Modifier.height(8.dp))
                                    Text("No quizzes found", color = TextSecondary)
                                    Text("Try a different filter", color = TextMuted, fontSize = 12.sp)
                                }
                            }
                        }
                    } else {
                        itemsIndexed(data.all) { index, quiz ->
                            QuizCard(
                                quiz      = quiz,
                                index     = index,
                                attempted = viewModel.isAttempted(quiz.id),
                                onClick   = { onNavigateToPlayer(quiz.id) }
                            )
                            Spacer(Modifier.height(10.dp))
                        }
                    }

                    // ── Recent attempts ───────────────────────────────────────
                    if (data.history.isNotEmpty()) {
                        item {
                            Spacer(Modifier.height(12.dp))
                            NeonDivider()
                            Spacer(Modifier.height(12.dp))
                            SectionHeader("📊  Recent Attempts")
                            Spacer(Modifier.height(10.dp))
                        }
                        itemsIndexed(data.history.take(5)) { index, attempt ->
                            QuizHistoryCard(
                                title    = attempt.quizTitle,
                                score    = attempt.score,
                                total    = attempt.totalQuestions,
                                xpEarned = attempt.xpEarned,
                                subject  = attempt.subject,
                                index    = index
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}
