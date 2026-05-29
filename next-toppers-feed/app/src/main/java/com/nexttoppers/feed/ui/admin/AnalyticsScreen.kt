package com.nexttoppers.feed.ui.admin

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Construction
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.nexttoppers.feed.data.repository.AdminStats
import com.nexttoppers.feed.ui.theme.BackgroundBlack
import com.nexttoppers.feed.ui.theme.NeonCyan
import com.nexttoppers.feed.ui.theme.NeonGreen
import com.nexttoppers.feed.ui.theme.PremiumGold
import com.nexttoppers.feed.ui.theme.SurfaceCard
import com.nexttoppers.feed.ui.theme.TextMuted
import com.nexttoppers.feed.ui.theme.TextPrimary
import com.nexttoppers.feed.ui.theme.TextSecondary

private val AnalyticsPurple = Color(0xFFB388FF)

@Composable
fun AnalyticsScreen(
    onBack: () -> Unit,
    viewModel: AnalyticsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Box(Modifier.fillMaxSize().background(BackgroundBlack)) {
        Box(
            Modifier.fillMaxWidth().height(220.dp)
                .background(Brush.radialGradient(listOf(AnalyticsPurple.copy(0.08f), Color.Transparent)))
        )

        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 56.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            item {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
                        Icon(Icons.Rounded.ArrowBack, null, tint = TextSecondary)
                    }
                    Spacer(Modifier.width(8.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            "Analytics",
                            style = TextStyle(
                                fontSize = 22.sp, fontWeight = FontWeight.ExtraBold,
                                brush = Brush.linearGradient(listOf(AnalyticsPurple, NeonCyan)),
                                shadow = Shadow(AnalyticsPurple.copy(0.4f), Offset.Zero, 12f)
                            )
                        )
                        Text("Platform insights overview", color = TextMuted, fontSize = 12.sp)
                    }
                    Icon(Icons.Rounded.Analytics, null, tint = AnalyticsPurple.copy(0.6f), modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.height(16.dp))
            }

            // Coming soon banner
            item {
                Row(
                    Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(AnalyticsPurple.copy(0.08f))
                        .border(1.dp, AnalyticsPurple.copy(0.25f), RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.Construction, null, tint = AnalyticsPurple, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Analytics Backend Coming Soon", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Real-time data will be available in the next update", color = TextMuted, fontSize = 11.sp)
                    }
                }
                Spacer(Modifier.height(20.dp))
            }

            // Placeholder metric cards
            item {
                Text("Key Metrics (Placeholder)", color = TextSecondary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(Modifier.height(10.dp))
            }
            item {
                val rows = state.metrics.chunked(2)
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    rows.forEach { pair ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            pair.forEach { metric ->
                                AnalyticsCard(metric, Modifier.weight(1f))
                            }
                            if (pair.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
            }

            // Subject breakdown placeholder
            item {
                Text("Top Subjects (Placeholder)", color = TextSecondary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(Modifier.height(10.dp))
            }
            item {
                SubjectBreakdownCard(state.topSubjects)
                Spacer(Modifier.height(24.dp))
            }

            // Chart placeholder
            item {
                Text("Activity Chart (Placeholder)", color = TextSecondary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(Modifier.height(10.dp))
            }
            item {
                ActivityChartPlaceholder()
            }
        }
    }
}

@Composable
private fun AnalyticsCard(metric: AnalyticsPlaceholder, modifier: Modifier = Modifier) {
    val color = Color(metric.color)
    val infinite = rememberInfiniteTransition(label = "card")
    val alpha by infinite.animateFloat(
        0.15f, 0.28f,
        animationSpec = infiniteRepeatable(tween(2200, easing = LinearEasing), RepeatMode.Reverse),
        label = "alpha"
    )
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.verticalGradient(listOf(color.copy(0.1f), color.copy(0.03f))))
            .border(1.dp, color.copy(alpha), RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(metric.label, color = TextMuted, fontSize = 11.sp)
            Text(metric.value, color = color, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.ArrowUpward, null, tint = NeonGreen.copy(0.6f), modifier = Modifier.size(12.dp))
                Text(metric.trend, color = TextMuted, fontSize = 10.sp)
            }
        }
    }
}

@Composable
private fun SubjectBreakdownCard(subjects: List<Pair<String, Int>>) {
    val colors = listOf(NeonGreen, NeonCyan, PremiumGold, AnalyticsPurple)
    Box(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceCard)
            .border(1.dp, AnalyticsPurple.copy(0.2f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            subjects.forEachIndexed { i, (subject, _) ->
                val color = colors[i % colors.size]
                val fraction = 0.3f + (i * 0.18f)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(subject, color = TextSecondary, fontSize = 12.sp, modifier = Modifier.width(72.dp))
                    Spacer(Modifier.width(8.dp))
                    Box(
                        Modifier
                            .weight(1f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(color.copy(0.1f))
                    ) {
                        Box(
                            Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(fraction)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Brush.horizontalGradient(listOf(color.copy(0.7f), color)))
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text("—", color = TextMuted, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun ActivityChartPlaceholder() {
    val bars = listOf(0.3f, 0.5f, 0.45f, 0.8f, 0.6f, 0.9f, 0.7f, 0.55f, 0.65f, 0.4f, 0.75f, 0.85f)
    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun", "Mon", "Tue", "Wed", "Thu", "Fri")
    Box(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceCard)
            .border(1.dp, NeonCyan.copy(0.15f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                bars.forEachIndexed { i, fraction ->
                    val color = if (i == bars.size - 1) NeonGreen else NeonCyan
                    Box(Modifier.weight(1f).height((80 * fraction).dp)
                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        .background(Brush.verticalGradient(listOf(color, color.copy(0.3f))))
                    )
                }
            }
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(days) { day ->
                    Box(Modifier.width(28.dp), contentAlignment = Alignment.Center) {
                        Text(day, color = TextMuted, fontSize = 9.sp)
                    }
                }
            }
            Text("Daily activity (placeholder data)", color = TextMuted, fontSize = 10.sp)
        }
    }
}
