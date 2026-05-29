package com.nexttoppers.feed.ui.admin

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.GppBad
import androidx.compose.material.icons.rounded.GppGood
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Report
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nexttoppers.feed.data.repository.ModerationLog
import com.nexttoppers.feed.data.repository.Report
import com.nexttoppers.feed.ui.theme.BackgroundBlack
import com.nexttoppers.feed.ui.theme.NeonCyan
import com.nexttoppers.feed.ui.theme.NeonGreen
import com.nexttoppers.feed.ui.theme.SurfaceCard
import com.nexttoppers.feed.ui.theme.TextMuted
import com.nexttoppers.feed.ui.theme.TextPrimary
import com.nexttoppers.feed.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Locale

private val ModRed    = Color(0xFFFF4D6D)
private val ModOrange = Color(0xFFFF6B35)

@Composable
fun ModerationScreen(
    onBack: () -> Unit,
    viewModel: ModerationViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(state.successMessage) {
        if (state.successMessage != null) {
            kotlinx.coroutines.delay(2000); viewModel.clearMessages()
        }
    }

    Box(Modifier.fillMaxSize().background(BackgroundBlack)) {
        Box(
            Modifier.fillMaxWidth().height(200.dp)
                .background(Brush.radialGradient(listOf(ModRed.copy(0.07f), Color.Transparent)))
        )

        Column(Modifier.fillMaxSize()) {
            // Header
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(top = 56.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Rounded.ArrowBack, null, tint = TextSecondary)
                }
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        "Moderation",
                        style = TextStyle(
                            fontSize = 22.sp, fontWeight = FontWeight.ExtraBold,
                            brush = Brush.linearGradient(listOf(ModRed, ModOrange)),
                            shadow = Shadow(ModRed.copy(0.4f), Offset.Zero, 12f)
                        )
                    )
                    Text("Content safety & reports", color = TextMuted, fontSize = 12.sp)
                }
                Box(
                    Modifier.size(36.dp).background(ModRed.copy(0.1f), RoundedCornerShape(10.dp))
                        .border(1.dp, ModRed.copy(0.3f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Shield, null, tint = ModRed, modifier = Modifier.size(18.dp))
                }
            }

            // Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor   = BackgroundBlack,
                contentColor     = TextPrimary,
                indicator        = { tabPositions ->
                    TabRowDefaults.Indicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = ModRed, height = 2.dp
                    )
                }
            ) {
                listOf("Reports" to Icons.Rounded.Report, "Logs" to Icons.Rounded.History)
                    .forEachIndexed { i, (label, icon) ->
                        Tab(
                            selected = selectedTab == i,
                            onClick  = { selectedTab = i },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(icon, null, modifier = Modifier.size(14.dp))
                                    Text(label, fontSize = 13.sp)
                                }
                            },
                            selectedContentColor   = ModRed,
                            unselectedContentColor = TextMuted
                        )
                    }
            }

            // Content
            when (selectedTab) {
                0 -> ReportsTab(state, viewModel::resolveReport, viewModel::toggleShowResolved)
                1 -> LogsTab(state.logs)
            }
        }

        // Toast
        state.successMessage?.let { msg ->
            Box(
                Modifier.align(Alignment.BottomCenter).padding(bottom = 100.dp)
                    .background(SurfaceCard, RoundedCornerShape(12.dp))
                    .border(1.dp, NeonGreen.copy(0.3f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) { Text(msg, color = TextPrimary, fontSize = 13.sp) }
        }
    }
}

@Composable
private fun ReportsTab(
    state: ModerationUiState,
    onResolve: (String) -> Unit,
    onToggleResolved: () -> Unit
) {
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (state.showResolved) "Resolved Reports" else "Open Reports",
                    color = TextSecondary, fontSize = 12.sp, modifier = Modifier.weight(1f)
                )
                Box(
                    Modifier.clip(RoundedCornerShape(8.dp))
                        .background(SurfaceCard)
                        .border(1.dp, TextMuted.copy(0.3f), RoundedCornerShape(8.dp))
                        .clickable(onClick = onToggleResolved)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        if (state.showResolved) "Show Open" else "Show Resolved",
                        color = TextMuted, fontSize = 11.sp
                    )
                }
            }
        }

        if (state.isLoading) {
            item {
                Box(Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = ModRed, modifier = Modifier.size(36.dp))
                }
            }
        } else if (state.reports.isEmpty()) {
            item {
                Box(Modifier.fillMaxWidth().padding(vertical = 40.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Rounded.GppGood, null, tint = NeonGreen.copy(0.4f), modifier = Modifier.size(60.dp))
                        Text("All clear!", color = TextSecondary, fontWeight = FontWeight.SemiBold)
                        Text("No ${if (state.showResolved) "resolved" else "open"} reports", color = TextMuted, fontSize = 12.sp)
                    }
                }
            }
        } else {
            itemsIndexed(state.reports) { index, report ->
                var visible by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) { visible = true }
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(200, index * 30)) + slideInVertically(tween(200, index * 30)) { it / 4 }
                ) {
                    ReportCard(
                        report       = report,
                        isProcessing = state.processingId == report.reportId,
                        onResolve    = { onResolve(report.reportId) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ReportCard(report: Report, isProcessing: Boolean, onResolve: () -> Unit) {
    val color = if (report.resolved) NeonGreen else ModRed
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.horizontalGradient(listOf(color.copy(0.08f), SurfaceCard)))
            .border(1.dp, color.copy(0.22f), RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(38.dp).background(color.copy(0.12f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                if (report.resolved) Icons.Rounded.GppGood else Icons.Rounded.GppBad,
                null, tint = color, modifier = Modifier.size(18.dp)
            )
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                report.type.replaceFirstChar { it.uppercase() } + " Report",
                color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp
            )
            Text("Reason: ${report.reason.take(50).ifBlank { "Not specified" }}", color = TextMuted, fontSize = 11.sp)
            val df = SimpleDateFormat("d MMM, h:mm a", Locale.getDefault())
            Text(df.format(report.timestamp.toDate()), color = TextMuted, fontSize = 10.sp)
        }
        if (!report.resolved) {
            if (isProcessing) {
                CircularProgressIndicator(color = NeonGreen, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
            } else {
                IconButton(onClick = onResolve, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Rounded.CheckCircle, null, tint = NeonGreen, modifier = Modifier.size(22.dp))
                }
            }
        }
    }
}

@Composable
private fun LogsTab(logs: List<ModerationLog>) {
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (logs.isEmpty()) {
            item {
                Box(Modifier.fillMaxWidth().padding(vertical = 40.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Rounded.History, null, tint = TextMuted.copy(0.3f), modifier = Modifier.size(56.dp))
                        Text("No moderation logs", color = TextSecondary)
                    }
                }
            }
        } else {
            itemsIndexed(logs) { _, log ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceCard)
                        .border(1.dp, NeonCyan.copy(0.1f), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(log.action, color = ModOrange, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("${log.targetType}: ${log.targetId.take(12)}", color = TextMuted, fontSize = 11.sp)
                        if (log.note.isNotBlank()) Text(log.note, color = TextSecondary, fontSize = 11.sp)
                    }
                    val df = SimpleDateFormat("d MMM", Locale.getDefault())
                    Text(df.format(log.timestamp.toDate()), color = TextMuted, fontSize = 10.sp)
                }
            }
        }
    }
}
