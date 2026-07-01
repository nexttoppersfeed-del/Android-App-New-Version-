package com.nexttoppers.feed.ui.tests

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Quiz
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nexttoppers.feed.data.model.NtfTest
import com.nexttoppers.feed.data.model.TestAttempt
import com.nexttoppers.feed.ui.components.NeonDivider
import com.nexttoppers.feed.ui.components.SkeletonCard
import com.nexttoppers.feed.ui.theme.AccentCyan
import com.nexttoppers.feed.ui.theme.AccentEmerald
import com.nexttoppers.feed.ui.theme.AccentViolet
import com.nexttoppers.feed.ui.theme.BackgroundBlack
import com.nexttoppers.feed.ui.theme.PremiumGold
import com.nexttoppers.feed.ui.theme.SurfaceCard
import com.nexttoppers.feed.ui.theme.SurfaceElevated
import com.nexttoppers.feed.ui.theme.TextMuted
import com.nexttoppers.feed.ui.theme.TextPrimary
import com.nexttoppers.feed.ui.theme.TextSecondary

// ── Subject filter chips ───────────────────────────────────────────────────────
private val SUBJECT_FILTERS = listOf(
    "All", "PHYSICS", "CHEMISTRY", "MATHS", "BIOLOGY",
    "ENGLISH", "SST", "HINDI", "GENERAL"
)

@Composable
fun TestsScreen(
    onBack: () -> Unit,
    viewModel: TestsViewModel = hiltViewModel()
) {
    val uiState     by viewModel.uiState.collectAsState()
    val attempts    by viewModel.attempts.collectAsState()
    val subFilter   by viewModel.subjectFilter.collectAsState()
    var visible     by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { visible = true }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundBlack)
    ) {
        // Ambient glow
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(
                    Brush.radialGradient(
                        listOf(AccentViolet.copy(0.08f), Color.Transparent)
                    )
                )
        )

        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(280)) + slideInVertically(tween(300)) { it / 8 }
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                // ── Top bar ──────────────────────────────────────────────────────
                Row(
                    modifier          = Modifier
                        .fillMaxWidth()
                        .padding(top = 52.dp, start = 8.dp, end = 20.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
                        Icon(Icons.Rounded.ArrowBack, null, tint = TextSecondary)
                    }
                    Spacer(Modifier.width(6.dp))
                    Icon(Icons.Rounded.Quiz, null, tint = AccentViolet, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Tests",
                        style = TextStyle(
                            fontSize   = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            brush      = Brush.linearGradient(listOf(AccentViolet, AccentCyan)),
                            shadow     = Shadow(AccentViolet.copy(0.35f), Offset.Zero, 14f)
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                // ── Subject filter row ───────────────────────────────────────────
                LazyRow(
                    contentPadding    = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(SUBJECT_FILTERS) { subject ->
                        val isSelected = if (subject == "All") subFilter == null
                                         else subject == subFilter
                        SubjectFilterChip(
                            label      = subject,
                            isSelected = isSelected,
                            onClick    = {
                                viewModel.setSubjectFilter(
                                    if (subject == "All") null else subject
                                )
                            }
                        )
                    }
                }

                NeonDivider(Modifier.padding(horizontal = 20.dp))
                Spacer(Modifier.height(4.dp))

                // ── Content ──────────────────────────────────────────────────────
                when (val state = uiState) {

                    is TestsUiState.Loading -> {
                        Column(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            repeat(5) { SkeletonCard(height = 100.dp) }
                        }
                    }

                    is TestsUiState.Empty -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.Quiz,
                                    null,
                                    tint     = AccentViolet.copy(0.4f),
                                    modifier = Modifier.size(64.dp)
                                )
                                Text("No tests yet", color = TextSecondary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                                Text(
                                    "Tests will appear here once published",
                                    color     = TextMuted,
                                    fontSize  = 13.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    is TestsUiState.Error -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Failed to load tests", color = TextSecondary)
                                Text(state.message, color = TextMuted, fontSize = 12.sp)
                            }
                        }
                    }

                    is TestsUiState.Success -> {
                        Text(
                            "${state.tests.size} test${if (state.tests.size != 1) "s" else ""}",
                            color    = AccentViolet.copy(0.8f),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                        )
                        LazyColumn(
                            contentPadding      = PaddingValues(
                                start  = 20.dp, end = 20.dp,
                                top    = 0.dp,  bottom = 100.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            itemsIndexed(state.tests) { _, test ->
                                val attempt = viewModel.getAttemptForTest(test.id)
                                TestCard(test = test, attempt = attempt, onClick = {})
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Subject filter chip ────────────────────────────────────────────────────────
@Composable
private fun SubjectFilterChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    val bg     = if (isSelected) AccentViolet.copy(0.18f) else SurfaceCard
    val border = if (isSelected) AccentViolet.copy(0.65f) else SurfaceElevated
    val text   = if (isSelected) AccentViolet else TextSecondary
    val fw     = if (isSelected) FontWeight.Bold else FontWeight.Normal

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text       = label.lowercase().replaceFirstChar { it.uppercase() },
            color      = text,
            fontSize   = 12.sp,
            fontWeight = fw
        )
    }
}

// ── Test card ─────────────────────────────────────────────────────────────────
@Composable
private fun TestCard(test: NtfTest, attempt: TestAttempt?, onClick: () -> Unit) {
    val diffColor = when (test.difficulty.uppercase()) {
        "EASY"   -> AccentEmerald
        "HARD"   -> Color(0xFFEF4444)
        else     -> PremiumGold
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceCard)
            .border(
                1.dp,
                if (attempt != null) AccentEmerald.copy(0.35f) else SurfaceElevated,
                RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

            // Title row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(AccentViolet.copy(0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Quiz, null, tint = AccentViolet, modifier = Modifier.size(20.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        test.title,
                        color      = TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize   = 14.sp,
                        maxLines   = 2,
                        overflow   = TextOverflow.Ellipsis
                    )
                    if (test.subject.isNotEmpty()) {
                        Text(
                            test.subject.lowercase().replaceFirstChar { it.uppercase() },
                            color    = AccentCyan.copy(0.8f),
                            fontSize = 11.sp
                        )
                    }
                }
                if (test.isPremium) {
                    Icon(Icons.Rounded.Lock, null, tint = PremiumGold, modifier = Modifier.size(16.dp))
                }
            }

            // Meta row
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Questions
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Rounded.Quiz, null, tint = TextMuted, modifier = Modifier.size(13.dp))
                    Text("${test.questionCount} Qs", color = TextMuted, fontSize = 11.sp)
                }
                // Time
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Rounded.Timer, null, tint = TextMuted, modifier = Modifier.size(13.dp))
                    Text("${test.timeLimit} min", color = TextMuted, fontSize = 11.sp)
                }
                // Difficulty pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(diffColor.copy(0.15f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        test.difficulty.lowercase().replaceFirstChar { it.uppercase() },
                        color    = diffColor,
                        fontSize = 10.sp
                    )
                }

                Spacer(Modifier.weight(1f))

                // Attempt badge
                if (attempt != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Rounded.CheckCircle,
                            null,
                            tint     = AccentEmerald,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            "${attempt.percentage}%",
                            color      = AccentEmerald,
                            fontSize   = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(AccentViolet.copy(0.85f), AccentCyan.copy(0.85f))
                                )
                            )
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "Start",
                            color      = Color.White,
                            fontSize   = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Description if not empty
            if (test.description.isNotEmpty()) {
                Text(
                    test.description,
                    color    = TextMuted,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
