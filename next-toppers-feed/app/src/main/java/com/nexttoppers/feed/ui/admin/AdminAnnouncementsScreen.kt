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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nexttoppers.feed.data.model.Announcement
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

private val UrgentRed = Color(0xFFFF4D6D)

@Composable
fun AdminAnnouncementsScreen(
    onBack: () -> Unit,
    viewModel: AdminAnnouncementsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var deleteTarget by remember { mutableStateOf<Announcement?>(null) }

    LaunchedEffect(state.successMessage) {
        if (state.successMessage != null) {
            kotlinx.coroutines.delay(2500); viewModel.clearMessages()
        }
    }

    Box(Modifier.fillMaxSize().background(BackgroundBlack)) {
        Box(
            Modifier.fillMaxWidth().height(200.dp)
                .background(Brush.radialGradient(listOf(NeonGreen.copy(0.06f), Color.Transparent)))
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
                            "Announcements",
                            style = TextStyle(
                                fontSize = 22.sp, fontWeight = FontWeight.ExtraBold,
                                brush = Brush.linearGradient(listOf(NeonGreen, NeonCyan)),
                                shadow = Shadow(NeonGreen.copy(0.4f), Offset.Zero, 12f)
                            )
                        )
                        Text("${state.announcements.size} total", color = TextMuted, fontSize = 12.sp)
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            if (state.isLoading) {
                item {
                    Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = NeonGreen, modifier = Modifier.size(40.dp))
                    }
                }
            } else if (state.announcements.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(vertical = 48.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Rounded.Campaign, null, tint = NeonGreen.copy(0.3f), modifier = Modifier.size(64.dp))
                            Text("No announcements yet", color = TextSecondary, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            } else {
                itemsIndexed(state.announcements) { index, ann ->
                    var visible by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) { visible = true }
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(tween(220, index * 35)) + slideInVertically(tween(220, index * 35)) { it / 4 }
                    ) {
                        AnnouncementManageCard(
                            ann          = ann,
                            isProcessing = state.processingId == ann.id,
                            onEdit       = { viewModel.showEditDialog(ann) },
                            onDelete     = { deleteTarget = ann },
                            onTogglePin  = { viewModel.setPinned(ann.id, !ann.pinned) },
                            onToggleUrgent = { viewModel.setUrgent(ann.id, !ann.important) }
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                }
            }
        }

        // FAB
        FloatingActionButton(
            onClick = viewModel::showCreateDialog,
            modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp).padding(bottom = 80.dp),
            containerColor = NeonGreen,
            contentColor = BackgroundBlack
        ) {
            Icon(Icons.Rounded.Add, null)
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

    // Delete dialog
    deleteTarget?.let { ann ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Delete Announcement?", color = UrgentRed, fontWeight = FontWeight.Bold) },
            text = { Text("\"${ann.title}\" will be permanently deleted.", color = TextSecondary, fontSize = 13.sp) },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteAnnouncement(ann.id); deleteTarget = null }) {
                    Text("Delete", color = UrgentRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = { TextButton(onClick = { deleteTarget = null }) { Text("Cancel", color = TextMuted) } },
            containerColor = SurfaceCard
        )
    }

    // Create / Edit dialog
    if (state.showCreateDialog) {
        AnnouncementFormDialog(
            editing   = state.editingAnnouncement,
            onDismiss = viewModel::hideDialog,
            onSave    = { title, message, imageUrl, pinned, important, priority ->
                val editing = state.editingAnnouncement
                if (editing != null) {
                    viewModel.updateAnnouncement(editing.id, title, message, imageUrl, pinned, important, priority)
                } else {
                    viewModel.createAnnouncement(title, message, imageUrl, pinned, important, priority)
                }
            }
        )
    }
}

@Composable
private fun AnnouncementManageCard(
    ann: Announcement,
    isProcessing: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onTogglePin: () -> Unit,
    onToggleUrgent: () -> Unit
) {
    val accentColor = when {
        ann.isUrgent -> UrgentRed
        ann.pinned   -> PremiumGold
        else         -> NeonGreen
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.verticalGradient(listOf(accentColor.copy(0.07f), SurfaceCard)))
            .border(1.dp, accentColor.copy(0.22f), RoundedCornerShape(18.dp))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (ann.pinned) Icon(Icons.Rounded.PushPin, null, tint = PremiumGold, modifier = Modifier.size(14.dp))
                        if (ann.isUrgent) Icon(Icons.Rounded.Warning, null, tint = UrgentRed, modifier = Modifier.size(14.dp))
                        Text(ann.title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp,
                            maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    Text(
                        ann.content,
                        color = TextSecondary, fontSize = 12.sp, maxLines = 2,
                        overflow = TextOverflow.Ellipsis, lineHeight = 17.sp
                    )
                }
                if (isProcessing) {
                    CircularProgressIndicator(color = NeonGreen, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    IconButton(onClick = onTogglePin, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Rounded.PushPin, null, tint = if (ann.pinned) PremiumGold else TextMuted, modifier = Modifier.size(17.dp))
                    }
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Rounded.Edit, null, tint = NeonCyan.copy(0.8f), modifier = Modifier.size(17.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Rounded.Delete, null, tint = UrgentRed.copy(0.7f), modifier = Modifier.size(17.dp))
                    }
                }
            }
            val df = SimpleDateFormat("d MMM yyyy", Locale.getDefault())
            Text(df.format(ann.createdAt.toDate()), color = TextMuted, fontSize = 10.sp)
        }
    }
}

@Composable
private fun AnnouncementFormDialog(
    editing: Announcement?,
    onDismiss: () -> Unit,
    onSave: (String, String, String, Boolean, Boolean, Int) -> Unit
) {
    var title     by remember { mutableStateOf(editing?.title ?: "") }
    var message   by remember { mutableStateOf(editing?.content ?: "") }
    var imageUrl  by remember { mutableStateOf(editing?.imageUrl ?: "") }
    var pinned    by remember { mutableStateOf(editing?.pinned ?: false) }
    var important by remember { mutableStateOf(editing?.important ?: false) }
    var priority  by remember { mutableStateOf(editing?.priority ?: 0) }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor  = NeonGreen, unfocusedBorderColor = TextMuted.copy(0.3f),
        focusedLabelColor   = NeonGreen, unfocusedLabelColor  = TextMuted,
        focusedTextColor    = TextPrimary, unfocusedTextColor = TextPrimary, cursorColor = NeonGreen
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (editing != null) "Edit Announcement" else "New Announcement",
                color = NeonGreen, fontWeight = FontWeight.Bold)
        },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(vertical = 4.dp)) {
                item {
                    OutlinedTextField(
                        value = title, onValueChange = { title = it },
                        label = { Text("Title *", fontSize = 12.sp) },
                        singleLine = true, modifier = Modifier.fillMaxWidth(),
                        colors = fieldColors, shape = RoundedCornerShape(10.dp),
                        textStyle = TextStyle(fontSize = 13.sp)
                    )
                }
                item {
                    OutlinedTextField(
                        value = message, onValueChange = { message = it },
                        label = { Text("Message *", fontSize = 12.sp) },
                        singleLine = false, maxLines = 5, modifier = Modifier.fillMaxWidth(),
                        colors = fieldColors, shape = RoundedCornerShape(10.dp),
                        textStyle = TextStyle(fontSize = 13.sp)
                    )
                }
                item {
                    OutlinedTextField(
                        value = imageUrl, onValueChange = { imageUrl = it },
                        label = { Text("Image URL (optional)", fontSize = 12.sp) },
                        singleLine = true, modifier = Modifier.fillMaxWidth(),
                        colors = fieldColors, shape = RoundedCornerShape(10.dp),
                        textStyle = TextStyle(fontSize = 13.sp)
                    )
                }
                item {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("📌 Pin Announcement", color = TextPrimary, fontSize = 13.sp, modifier = Modifier.weight(1f))
                        Switch(checked = pinned, onCheckedChange = { pinned = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = PremiumGold, checkedTrackColor = PremiumGold.copy(0.3f)))
                    }
                }
                item {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("🚨 Mark Urgent", color = TextPrimary, fontSize = 13.sp, modifier = Modifier.weight(1f))
                        Switch(checked = important, onCheckedChange = { important = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = UrgentRed, checkedTrackColor = UrgentRed.copy(0.3f)))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (title.isNotBlank() && message.isNotBlank()) onSave(title, message, imageUrl, pinned, important, if (important) 10 else 0) },
                enabled = title.isNotBlank() && message.isNotBlank()
            ) {
                Text(if (editing != null) "Update" else "Publish", color = NeonGreen, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = TextMuted) } },
        containerColor = SurfaceCard
    )
}
