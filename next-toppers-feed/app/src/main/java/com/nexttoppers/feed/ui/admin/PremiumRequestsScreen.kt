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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.nexttoppers.feed.data.model.PremiumRequest
import com.nexttoppers.feed.data.model.PremiumRequestStatus
import com.nexttoppers.feed.ui.theme.BackgroundBlack
import com.nexttoppers.feed.ui.theme.NeonCyan
import com.nexttoppers.feed.ui.theme.NeonGreen
import com.nexttoppers.feed.ui.theme.PremiumGold
import com.nexttoppers.feed.ui.theme.SurfaceCard
import com.nexttoppers.feed.ui.theme.TextMuted
import com.nexttoppers.feed.ui.theme.TextPrimary
import com.nexttoppers.feed.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Locale

private val RequestRed   = Color(0xFFFF4D6D)
private val RequestGreen = Color(0xFF00FF87)

@Composable
fun PremiumRequestsScreen(
    onBack: () -> Unit,
    viewModel: PremiumRequestsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var approvingRequest by remember { mutableStateOf<PremiumRequest?>(null) }
    var rejectingRequest by remember { mutableStateOf<PremiumRequest?>(null) }

    LaunchedEffect(state.successMessage) {
        if (state.successMessage != null) {
            kotlinx.coroutines.delay(2500)
            viewModel.clearMessages()
        }
    }

    Box(Modifier.fillMaxSize().background(BackgroundBlack)) {
        Box(
            Modifier.fillMaxWidth().height(200.dp)
                .background(Brush.radialGradient(listOf(PremiumGold.copy(0.07f), Color.Transparent)))
        )

        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 56.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // Header
            item {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
                        Icon(Icons.Rounded.ArrowBack, null, tint = TextSecondary)
                    }
                    Spacer(Modifier.width(8.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            "Premium Requests",
                            style = TextStyle(
                                fontSize = 22.sp, fontWeight = FontWeight.ExtraBold,
                                brush = Brush.linearGradient(listOf(PremiumGold, NeonCyan)),
                                shadow = Shadow(PremiumGold.copy(0.4f), Offset.Zero, 12f)
                            )
                        )
                        Text("${state.requests.size} requests", color = TextMuted, fontSize = 12.sp)
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // Filter tabs
            item {
                val filters = listOf("ALL", "PENDING", "APPROVED", "REJECTED")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    itemsIndexed(filters) { _, f ->
                        val isActive = state.activeFilter == f
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isActive) PremiumGold.copy(0.15f) else SurfaceCard)
                                .border(1.dp, if (isActive) PremiumGold.copy(0.6f) else TextMuted.copy(0.2f), RoundedCornerShape(20.dp))
                                .clickable { viewModel.setFilter(f) }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(f, color = if (isActive) PremiumGold else TextMuted,
                                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal, fontSize = 12.sp)
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            if (state.isLoading) {
                item {
                    Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PremiumGold, modifier = Modifier.size(40.dp))
                    }
                }
            } else if (state.requests.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(vertical = 48.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Rounded.WorkspacePremium, null, tint = PremiumGold.copy(0.3f), modifier = Modifier.size(64.dp))
                            Text("No requests", color = TextSecondary, fontWeight = FontWeight.SemiBold)
                            Text("No ${state.activeFilter.lowercase()} requests found", color = TextMuted, fontSize = 12.sp)
                        }
                    }
                }
            } else {
                itemsIndexed(state.requests) { index, request ->
                    var visible by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) { visible = true }
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(tween(240, index * 40)) + slideInVertically(tween(240, index * 40)) { it / 4 }
                    ) {
                        PremiumRequestCard(
                            request     = request,
                            isProcessing = state.processingId == request.requestId,
                            onApprove   = { approvingRequest = request },
                            onReject    = { rejectingRequest = request }
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                }
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

    // Approve dialog
    approvingRequest?.let { req ->
        var durationDays by remember { mutableIntStateOf(30) }
        AlertDialog(
            onDismissRequest = { approvingRequest = null },
            title = { Text("Approve Request", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Approve premium for ${req.username}?", color = TextSecondary, fontSize = 13.sp)
                    Text("Plan: ${req.plan}  ·  Amount: ₹${req.amount}", color = NeonGreen, fontSize = 12.sp)
                    Text("UTR: ${req.utrId.ifBlank { "Not provided" }}", color = TextMuted, fontSize = 12.sp)
                    Spacer(Modifier.height(4.dp))
                    Text("Duration", color = TextSecondary, fontSize = 12.sp)
                    val options = listOf(7 to "7 Days", 30 to "30 Days", 90 to "3 Months", 365 to "1 Year", 36500 to "Lifetime")
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        options.forEach { (days, label) ->
                            val sel = durationDays == days
                            Box(
                                Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (sel) NeonGreen.copy(0.2f) else SurfaceCard)
                                    .border(1.dp, if (sel) NeonGreen.copy(0.6f) else TextMuted.copy(0.2f), RoundedCornerShape(8.dp))
                                    .clickable { durationDays = days }
                                    .padding(horizontal = 8.dp, vertical = 5.dp)
                            ) {
                                Text(label, color = if (sel) NeonGreen else TextMuted, fontSize = 10.sp, fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.approveRequest(req.requestId, req.userId, req.plan, durationDays)
                    approvingRequest = null
                }) { Text("Approve ✅", color = NeonGreen, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { approvingRequest = null }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = SurfaceCard
        )
    }

    // Reject dialog
    rejectingRequest?.let { req ->
        var note by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { rejectingRequest = null },
            title = { Text("Reject Request", color = RequestRed, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Reject premium request for ${req.username}?", color = TextSecondary, fontSize = 13.sp)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.rejectRequest(req.requestId, note)
                    rejectingRequest = null
                }) { Text("Reject ❌", color = RequestRed, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { rejectingRequest = null }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = SurfaceCard
        )
    }
}

@Composable
private fun PremiumRequestCard(
    request: PremiumRequest,
    isProcessing: Boolean,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    val statusColor = when (request.statusEnum) {
        PremiumRequestStatus.PENDING  -> PremiumGold
        PremiumRequestStatus.APPROVED -> RequestGreen
        PremiumRequestStatus.REJECTED -> RequestRed
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.verticalGradient(listOf(statusColor.copy(0.07f), SurfaceCard)))
            .border(1.dp, statusColor.copy(0.25f), RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(44.dp)
                        .background(statusColor.copy(0.12f), RoundedCornerShape(13.dp))
                        .border(1.dp, statusColor.copy(0.3f), RoundedCornerShape(13.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(request.username.take(1).uppercase(), color = statusColor, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(request.username, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(request.userEmail, color = TextMuted, fontSize = 11.sp)
                }
                Box(
                    Modifier.background(statusColor.copy(0.15f), RoundedCornerShape(6.dp))
                        .border(1.dp, statusColor.copy(0.4f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(request.statusEnum.label, color = statusColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                InfoChip("Plan", request.plan, NeonCyan, Modifier.weight(1f))
                InfoChip("Amount", "₹${request.amount}", NeonGreen, Modifier.weight(1f))
                InfoChip("UTR", request.utrId.take(8).ifBlank { "—" }, PremiumGold, Modifier.weight(1f))
            }

            val df = SimpleDateFormat("d MMM yyyy, h:mm a", Locale.getDefault())
            Text(
                "Submitted: ${df.format(request.createdAt.toDate())}",
                color = TextMuted, fontSize = 10.sp
            )

            if (request.statusEnum == PremiumRequestStatus.PENDING) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (isProcessing) {
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = PremiumGold, modifier = Modifier.size(28.dp), strokeWidth = 2.dp)
                        }
                    } else {
                        AdminActionButton(
                            "✅ Approve", NeonGreen, onClick = onApprove, modifier = Modifier.weight(1f)
                        )
                        AdminActionButton(
                            "❌ Reject", RequestRed, onClick = onReject, modifier = Modifier.weight(1f)
                        )
                    }
                }
            } else if (request.reviewedAt != null) {
                val df2 = SimpleDateFormat("d MMM yyyy", Locale.getDefault())
                Text("Reviewed: ${df2.format(request.reviewedAt.toDate())}", color = TextMuted, fontSize = 10.sp)
                if (request.adminNote.isNotBlank()) {
                    Text("Note: ${request.adminNote}", color = TextSecondary, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun InfoChip(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(color.copy(0.07f))
            .border(1.dp, color.copy(0.2f), RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Text(label, color = TextMuted, fontSize = 9.sp)
        Text(value, color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun AdminActionButton(label: String, color: Color, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(color.copy(0.12f))
            .border(1.dp, color.copy(0.35f), RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = color, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}
