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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nexttoppers.feed.data.model.AdminNotification
import com.nexttoppers.feed.data.model.NotificationTarget
import com.nexttoppers.feed.data.model.NotificationType
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

private val NotifPurple = Color(0xFFB388FF)
private val NotifRed    = Color(0xFFFF4D6D)

@Composable
fun AdminNotificationsScreen(
    onBack: () -> Unit,
    viewModel: AdminNotificationsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Box(Modifier.fillMaxSize().background(BackgroundBlack)) {

        AnimatedVisibility(
            visible = visible,
            enter   = fadeIn(tween(300)) + slideInVertically(tween(300)) { it / 10 }
        ) {
            Column(Modifier.fillMaxSize()) {

                // ── Top bar ─────────────────────────────────────────────────────
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Rounded.ArrowBack, null, tint = TextSecondary, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.width(4.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            "Notifications",
                            style = TextStyle(
                                fontSize = 22.sp, fontWeight = FontWeight.ExtraBold,
                                brush = Brush.linearGradient(listOf(NotifPurple, NeonCyan)),
                                shadow = Shadow(NotifPurple.copy(0.4f), Offset.Zero, 12f)
                            )
                        )
                        Text("Admin Notification Center", color = TextMuted, fontSize = 11.sp)
                    }
                    Icon(Icons.Rounded.Campaign, null, tint = NotifPurple, modifier = Modifier.size(22.dp))
                }

                // ── Tabs ──────────────────────────────────────────────────────
                ScrollableTabRow(
                    selectedTabIndex  = selectedTab,
                    containerColor    = BackgroundBlack,
                    contentColor      = NotifPurple,
                    edgePadding       = 16.dp
                ) {
                    listOf("Send", "History").forEachIndexed { index, label ->
                        Tab(
                            selected = selectedTab == index,
                            onClick  = { selectedTab = index },
                            text = {
                                Text(
                                    label,
                                    color      = if (selectedTab == index) NotifPurple else TextMuted,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                    fontSize   = 13.sp
                                )
                            }
                        )
                    }
                }

                // ── Status banners ────────────────────────────────────────────
                state.successMessage?.let { msg ->
                    LaunchedEffect(msg) {
                        kotlinx.coroutines.delay(3500)
                        viewModel.clearMessages()
                    }
                    StatusBanner(text = msg, isSuccess = true)
                }
                state.error?.let { err ->
                    StatusBanner(text = err, isSuccess = false)
                }

                // ── Content ───────────────────────────────────────────────────
                when (selectedTab) {
                    0 -> SendNotificationPanel(state = state, viewModel = viewModel)
                    1 -> HistoryPanel(state = state, viewModel = viewModel)
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// SEND PANEL
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun SendNotificationPanel(
    state: AdminNotifUiState,
    viewModel: AdminNotificationsViewModel
) {
    var target by remember { mutableStateOf(NotificationTarget.ALL) }
    var title  by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var deepLink by remember { mutableStateOf("") }
    var topic   by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(NotificationType.ANNOUNCEMENT) }
    val selectedUserIds = remember { mutableStateListOf<String>() }
    var showUserPicker by remember { mutableStateOf(false) }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ── Target selector ────────────────────────────────────────────────
        item {
            SectionLabel("Target Audience")
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NotificationTarget.values().forEach { t ->
                    val sel = target == t
                    Box(
                        Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (sel) NotifPurple.copy(0.2f) else SurfaceCard)
                            .border(1.dp, if (sel) NotifPurple else NotifPurple.copy(0.15f), RoundedCornerShape(10.dp))
                            .clickable { target = t }
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            t.label,
                            color      = if (sel) NotifPurple else TextMuted,
                            fontSize   = 9.sp,
                            fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal,
                            textAlign  = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }

        // ── Topic field (when TOPIC selected) ──────────────────────────────
        if (target == NotificationTarget.TOPIC) {
            item {
                NtfTextField(
                    value       = topic,
                    onValueChange = { topic = it },
                    label       = "Topic (e.g. all, premium)",
                    singleLine  = true
                )
            }
        }

        // ── User picker (when SELECTED) ────────────────────────────────────
        if (target == NotificationTarget.SELECTED) {
            item {
                Row(
                    Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(SurfaceCard)
                        .border(1.dp, NotifPurple.copy(0.2f), RoundedCornerShape(10.dp))
                        .clickable { showUserPicker = !showUserPicker }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.Notifications, null, tint = NotifPurple, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(
                        if (selectedUserIds.isEmpty()) "Tap to select users"
                        else "${selectedUserIds.size} user${if (selectedUserIds.size > 1) "s" else ""} selected",
                        color    = if (selectedUserIds.isEmpty()) TextMuted else TextPrimary,
                        fontSize = 13.sp
                    )
                }
                if (showUserPicker && state.allUsers.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceCard)
                            .border(1.dp, NotifPurple.copy(0.15f), RoundedCornerShape(12.dp))
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        state.allUsers.take(50).forEach { user ->
                            val checked = selectedUserIds.contains(user.uid)
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        if (checked) selectedUserIds.remove(user.uid)
                                        else selectedUserIds.add(user.uid)
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked         = checked,
                                    onCheckedChange = {
                                        if (it) selectedUserIds.add(user.uid)
                                        else selectedUserIds.remove(user.uid)
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor   = NotifPurple,
                                        uncheckedColor = TextMuted
                                    ),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Column {
                                    Text(user.name.ifEmpty { "Unknown" }, color = TextPrimary, fontSize = 12.sp)
                                    Text(user.email, color = TextMuted, fontSize = 10.sp)
                                }
                                if (user.isPremium) {
                                    Spacer(Modifier.weight(1f))
                                    Box(
                                        Modifier
                                            .background(PremiumGold.copy(0.15f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    ) {
                                        Text("PRO", color = PremiumGold, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // ── Notification type ──────────────────────────────────────────────
        item {
            SectionLabel("Notification Type")
            var expanded by remember { mutableStateOf(false) }
            Box {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(SurfaceCard)
                        .border(1.dp, NotifPurple.copy(0.2f), RoundedCornerShape(10.dp))
                        .clickable { expanded = true }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(selectedType.emoji, fontSize = 16.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(selectedType.label, color = TextPrimary, fontSize = 13.sp, modifier = Modifier.weight(1f))
                }
                DropdownMenu(
                    expanded         = expanded,
                    onDismissRequest = { expanded = false },
                    modifier         = Modifier.background(SurfaceCard)
                ) {
                    NotificationType.values().forEach { t ->
                        DropdownMenuItem(
                            text    = { Text("${t.emoji} ${t.label}", color = TextPrimary, fontSize = 13.sp) },
                            onClick = { selectedType = t; expanded = false }
                        )
                    }
                }
            }
        }

        // ── Title ──────────────────────────────────────────────────────────
        item {
            SectionLabel("Title *")
            NtfTextField(value = title, onValueChange = { title = it }, label = "Notification Title", singleLine = true)
        }

        // ── Message ────────────────────────────────────────────────────────
        item {
            SectionLabel("Message *")
            NtfTextField(value = message, onValueChange = { message = it }, label = "Notification Message", minLines = 3)
        }

        // ── Image URL (optional) ───────────────────────────────────────────
        item {
            SectionLabel("Image URL (Optional)")
            NtfTextField(value = imageUrl, onValueChange = { imageUrl = it }, label = "https://...", singleLine = true)
        }

        // ── Deep Link / Action Route (optional) ───────────────────────────
        item {
            SectionLabel("Deep Link / Route (Optional)")
            var showRoutes by remember { mutableStateOf(false) }
            Column {
                NtfTextField(value = deepLink, onValueChange = { deepLink = it }, label = "e.g. notifications, chats, resources", singleLine = true)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Quick routes: ${commonRoutes.joinToString(" · ")}",
                    color    = TextMuted,
                    fontSize = 10.sp,
                    modifier = Modifier.clickable { showRoutes = !showRoutes }
                )
                if (showRoutes) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        commonRoutes.take(4).forEach { r ->
                            Box(
                                Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(NeonCyan.copy(0.08f))
                                    .border(1.dp, NeonCyan.copy(0.2f), RoundedCornerShape(6.dp))
                                    .clickable { deepLink = r }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(r, color = NeonCyan, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }

        // ── Send button ────────────────────────────────────────────────────
        item {
            Spacer(Modifier.height(4.dp))
            Button(
                onClick = {
                    viewModel.sendNotification(
                        target          = target,
                        title           = title.trim(),
                        message         = message.trim(),
                        type            = selectedType,
                        imageUrl        = imageUrl.trim(),
                        deepLink        = deepLink.trim(),
                        selectedUserIds = selectedUserIds.toList(),
                        topic           = topic.trim()
                    )
                },
                enabled  = !state.isSending && title.isNotBlank() && message.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape    = RoundedCornerShape(12.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor         = NotifPurple,
                    disabledContainerColor = NotifPurple.copy(0.3f)
                )
            ) {
                if (state.isSending) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(10.dp))
                    Text("Sending...", color = Color.White, fontWeight = FontWeight.Bold)
                } else {
                    Icon(Icons.Rounded.Send, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Send Notification", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// HISTORY PANEL
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun HistoryPanel(
    state: AdminNotifUiState,
    viewModel: AdminNotificationsViewModel
) {
    var confirmDeleteId by remember { mutableStateOf<String?>(null) }

    if (state.history.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("📭", fontSize = 48.sp)
                Text("No notifications sent yet", color = TextMuted, fontSize = 14.sp)
                Text("Use the Send tab to send your first notification.", color = TextMuted, fontSize = 12.sp)
            }
        }
        return
    }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding     = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("${state.history.size} notification${if (state.history.size > 1) "s" else ""} sent",
                    color = TextMuted, fontSize = 12.sp)
            }
        }
        items(state.history, key = { it.id }) { item ->
            HistoryCard(
                item      = item,
                onResend  = { viewModel.resend(item) },
                onDelete  = { confirmDeleteId = item.id },
                isSending = state.isSending
            )
        }
        item { Spacer(Modifier.height(24.dp)) }
    }

    confirmDeleteId?.let { hId ->
        AlertDialog(
            onDismissRequest = { confirmDeleteId = null },
            containerColor   = SurfaceCard,
            title = { Text("Delete History", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text  = { Text("Remove this entry from history?", color = TextSecondary, fontSize = 13.sp) },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteHistory(hId); confirmDeleteId = null }) {
                    Text("Delete", color = NotifRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDeleteId = null }) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
    }
}

@Composable
private fun HistoryCard(
    item: AdminNotification,
    onResend: () -> Unit,
    onDelete: () -> Unit,
    isSending: Boolean
) {
    val sdf = remember { SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault()) }
    val type = NotificationType.values().firstOrNull { it.name == item.notificationType }
        ?: NotificationType.SYSTEM

    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceCard)
            .border(1.dp, NotifPurple.copy(0.15f), RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            Modifier.size(40.dp)
                .background(NotifPurple.copy(0.1f), RoundedCornerShape(10.dp))
                .border(1.dp, NotifPurple.copy(0.25f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(type.emoji, fontSize = 18.sp)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(item.title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(item.message, color = TextSecondary, fontSize = 11.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HistoryChip(item.targetEnum.label, NotifPurple)
                if (item.recipientCount > 0) HistoryChip("${item.recipientCount} recipients", NeonGreen)
            }
            Text(sdf.format(item.sentAt.toDate()), color = TextMuted, fontSize = 10.sp)
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            IconButton(onClick = onResend, enabled = !isSending, modifier = Modifier.size(34.dp)) {
                Icon(Icons.Rounded.Refresh, null, tint = NeonGreen.copy(if (isSending) 0.3f else 0.8f), modifier = Modifier.size(16.dp))
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(34.dp)) {
                Icon(Icons.Rounded.Delete, null, tint = NotifRed.copy(0.7f), modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun HistoryChip(text: String, color: Color) {
    Box(
        Modifier
            .background(color.copy(0.1f), RoundedCornerShape(6.dp))
            .border(1.dp, color.copy(0.2f), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(text, color = color, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// SHARED HELPERS
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun SectionLabel(text: String) {
    Text(text, color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    Spacer(Modifier.height(6.dp))
}

@Composable
private fun NtfTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    singleLine: Boolean = false,
    minLines: Int = 1
) {
    OutlinedTextField(
        value         = value,
        onValueChange = onValueChange,
        placeholder   = { Text(label, color = TextMuted, fontSize = 13.sp) },
        singleLine    = singleLine,
        minLines      = minLines,
        modifier      = Modifier.fillMaxWidth(),
        shape         = RoundedCornerShape(10.dp),
        colors        = OutlinedTextFieldDefaults.colors(
            focusedBorderColor   = NotifPurple,
            unfocusedBorderColor = NotifPurple.copy(0.2f),
            focusedTextColor     = TextPrimary,
            unfocusedTextColor   = TextPrimary,
            cursorColor          = NotifPurple
        )
    )
}

@Composable
private fun StatusBanner(text: String, isSuccess: Boolean) {
    val color = if (isSuccess) NeonGreen else NotifRed
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(color.copy(0.1f))
            .border(1.dp, color.copy(0.3f), RoundedCornerShape(10.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            if (isSuccess) Icons.Rounded.CheckCircle else Icons.Rounded.ErrorOutline,
            null,
            tint     = color,
            modifier = Modifier.size(16.dp)
        )
        Text(text, color = color, fontSize = 12.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
    }
}

private val commonRoutes = listOf(
    "notifications", "home", "chats", "resources", "announcements"
)
