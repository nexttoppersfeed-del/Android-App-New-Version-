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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.LibraryBooks
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.Search
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nexttoppers.feed.data.model.Resource
import com.nexttoppers.feed.data.model.ResourceSubject
import com.nexttoppers.feed.data.model.ResourceType
import com.nexttoppers.feed.ui.theme.BackgroundBlack
import com.nexttoppers.feed.ui.theme.NeonCyan
import com.nexttoppers.feed.ui.theme.NeonGreen
import com.nexttoppers.feed.ui.theme.PremiumGold
import com.nexttoppers.feed.ui.theme.SurfaceCard
import com.nexttoppers.feed.ui.theme.TextMuted
import com.nexttoppers.feed.ui.theme.TextPrimary
import com.nexttoppers.feed.ui.theme.TextSecondary

@Composable
fun ResourceManagementScreen(
    onBack: () -> Unit,
    viewModel: ResourceManagementViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var deleteTarget by remember { mutableStateOf<Resource?>(null) }

    LaunchedEffect(state.successMessage) {
        if (state.successMessage != null) {
            kotlinx.coroutines.delay(2500); viewModel.clearMessages()
        }
    }

    Box(Modifier.fillMaxSize().background(BackgroundBlack)) {
        Box(
            Modifier.fillMaxWidth().height(200.dp)
                .background(Brush.radialGradient(listOf(NeonCyan.copy(0.07f), Color.Transparent)))
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
                            "Resources",
                            style = TextStyle(
                                fontSize = 22.sp, fontWeight = FontWeight.ExtraBold,
                                brush = Brush.linearGradient(listOf(NeonCyan, NeonGreen)),
                                shadow = Shadow(NeonCyan.copy(0.4f), Offset.Zero, 12f)
                            )
                        )
                        Text("${viewModel.filteredResources.size} resources", color = TextMuted, fontSize = 12.sp)
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            // Search bar
            item {
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = viewModel::setSearchQuery,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search resources…", color = TextMuted) },
                    leadingIcon = { Icon(Icons.Rounded.Search, null, tint = NeonCyan.copy(0.7f)) },
                    trailingIcon = {
                        if (state.searchQuery.isNotBlank()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Rounded.Clear, null, tint = TextMuted, modifier = Modifier.size(18.dp))
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = TextMuted.copy(0.25f),
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = NeonCyan
                    ),
                    shape = RoundedCornerShape(14.dp),
                    textStyle = TextStyle(fontSize = 14.sp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
                )
                Spacer(Modifier.height(12.dp))
            }

            // Subject filter chips
            item {
                val subjects = listOf("ALL") + ResourceSubject.values().map { it.name }
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    itemsIndexed(subjects) { _, subj ->
                        val isActive = state.subjectFilter == subj
                        Box(
                            Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isActive) NeonCyan.copy(0.15f) else SurfaceCard)
                                .border(1.dp, if (isActive) NeonCyan.copy(0.6f) else TextMuted.copy(0.2f), RoundedCornerShape(20.dp))
                                .clickable { viewModel.setSubjectFilter(subj) }
                                .padding(horizontal = 14.dp, vertical = 7.dp)
                        ) {
                            Text(
                                if (subj == "ALL") "All" else ResourceSubject.values().firstOrNull { it.name == subj }?.displayName ?: subj,
                                color = if (isActive) NeonCyan else TextMuted,
                                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            if (state.isLoading) {
                item {
                    Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = NeonCyan, modifier = Modifier.size(40.dp))
                    }
                }
            } else {
                val filtered = viewModel.filteredResources
                if (filtered.isEmpty()) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(vertical = 48.dp), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Icon(Icons.Rounded.LibraryBooks, null, tint = NeonCyan.copy(0.3f), modifier = Modifier.size(64.dp))
                                Text("No resources found", color = TextSecondary, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                } else {
                    itemsIndexed(filtered) { index, resource ->
                        var visible by remember { mutableStateOf(false) }
                        LaunchedEffect(Unit) { visible = true }
                        AnimatedVisibility(
                            visible = visible,
                            enter = fadeIn(tween(200, index * 30)) + slideInVertically(tween(200, index * 30)) { it / 5 }
                        ) {
                            ResourceManageCard(
                                resource     = resource,
                                isProcessing = state.processingId == resource.id,
                                onEdit       = { viewModel.showEditDialog(resource) },
                                onDelete     = { deleteTarget = resource },
                                onTogglePremium = { viewModel.togglePremium(resource.id, resource.premium) }
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }

        // FAB
        FloatingActionButton(
            onClick = viewModel::showAddDialog,
            modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp).padding(bottom = 80.dp),
            containerColor = NeonCyan,
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
    deleteTarget?.let { res ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Delete Resource?", color = Color(0xFFFF4D6D), fontWeight = FontWeight.Bold) },
            text = { Text("\"${res.title}\" will be permanently deleted.", color = TextSecondary, fontSize = 13.sp) },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteResource(res.id); deleteTarget = null }) {
                    Text("Delete", color = Color(0xFFFF4D6D), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = { TextButton(onClick = { deleteTarget = null }) { Text("Cancel", color = TextMuted) } },
            containerColor = SurfaceCard
        )
    }

    // Add/Edit dialog
    if (state.showAddDialog) {
        ResourceFormDialog(
            editingResource = state.editingResource,
            onDismiss       = viewModel::hideDialog,
            onSave          = { title, desc, subject, type, fileUrl, thumbnailUrl, premium, tags ->
                val editing = state.editingResource
                if (editing != null) {
                    viewModel.updateResource(editing.id, title, desc, subject, type, fileUrl, premium, tags)
                } else {
                    viewModel.createResource(title, desc, subject, type, fileUrl, thumbnailUrl, premium, tags)
                }
            }
        )
    }
}

@Composable
private fun ResourceManageCard(
    resource: Resource,
    isProcessing: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onTogglePremium: () -> Unit
) {
    val accent = if (resource.premium) PremiumGold else NeonCyan
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.verticalGradient(listOf(accent.copy(0.06f), SurfaceCard)))
            .border(1.dp, accent.copy(0.2f), RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(44.dp)
                .background(accent.copy(0.1f), RoundedCornerShape(12.dp))
                .border(1.dp, accent.copy(0.25f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(resource.type.take(1), color = accent, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(resource.title, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                SubjectTag(resource.subject, NeonCyan)
                SubjectTag(resource.type, TextMuted)
            }
        }
        if (isProcessing) {
            CircularProgressIndicator(color = NeonCyan, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
        } else {
            IconButton(onClick = onTogglePremium, modifier = Modifier.size(32.dp)) {
                Icon(
                    if (resource.premium) Icons.Rounded.Lock else Icons.Rounded.LockOpen,
                    null,
                    tint = if (resource.premium) PremiumGold else TextMuted,
                    modifier = Modifier.size(17.dp)
                )
            }
            IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Rounded.Edit, null, tint = NeonGreen.copy(0.8f), modifier = Modifier.size(17.dp))
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Rounded.Delete, null, tint = Color(0xFFFF4D6D).copy(0.7f), modifier = Modifier.size(17.dp))
            }
        }
    }
}

@Composable
private fun SubjectTag(label: String, color: Color) {
    Box(
        Modifier
            .background(color.copy(0.1f), RoundedCornerShape(5.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(label.take(10), color = color, fontSize = 10.sp)
    }
}

@Composable
private fun ResourceFormDialog(
    editingResource: Resource?,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String, String, Boolean, List<String>) -> Unit
) {
    var title       by remember { mutableStateOf(editingResource?.title ?: "") }
    var description by remember { mutableStateOf(editingResource?.description ?: "") }
    var subject     by remember { mutableStateOf(editingResource?.subject ?: ResourceSubject.MATHS.name) }
    var type        by remember { mutableStateOf(editingResource?.type ?: ResourceType.PDF.name) }
    var fileUrl     by remember { mutableStateOf(editingResource?.fileUrl ?: "") }
    var thumbnailUrl by remember { mutableStateOf(editingResource?.thumbnailUrl ?: "") }
    var premium     by remember { mutableStateOf(editingResource?.premium ?: false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (editingResource != null) "Edit Resource" else "Add Resource",
                color = NeonCyan, fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.height(420.dp).background(SurfaceCard),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    item {
                        FormField("Title *", title, { title = it })
                    }
                    item {
                        FormField("Description", description, { description = it }, singleLine = false)
                    }
                    item {
                        Text("Subject", color = TextSecondary, fontSize = 12.sp)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            itemsIndexed(ResourceSubject.values().toList()) { _, s ->
                                val sel = subject == s.name
                                Box(
                                    Modifier.clip(RoundedCornerShape(8.dp))
                                        .background(if (sel) NeonCyan.copy(0.2f) else SurfaceCard)
                                        .border(1.dp, if (sel) NeonCyan.copy(0.5f) else TextMuted.copy(0.2f), RoundedCornerShape(8.dp))
                                        .clickable { subject = s.name }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(s.displayName, color = if (sel) NeonCyan else TextMuted, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                    item {
                        Text("Type", color = TextSecondary, fontSize = 12.sp)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            itemsIndexed(ResourceType.values().toList()) { _, t ->
                                val sel = type == t.name
                                Box(
                                    Modifier.clip(RoundedCornerShape(8.dp))
                                        .background(if (sel) NeonGreen.copy(0.2f) else SurfaceCard)
                                        .border(1.dp, if (sel) NeonGreen.copy(0.5f) else TextMuted.copy(0.2f), RoundedCornerShape(8.dp))
                                        .clickable { type = t.name }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(t.displayName, color = if (sel) NeonGreen else TextMuted, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                    item { FormField("File URL *", fileUrl, { fileUrl = it }) }
                    if (editingResource == null) {
                        item { FormField("Thumbnail URL", thumbnailUrl, { thumbnailUrl = it }) }
                    }
                    item {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("Premium Only", color = TextPrimary, fontSize = 13.sp)
                                Text("Lock to premium members", color = TextMuted, fontSize = 11.sp)
                            }
                            Switch(
                                checked = premium, onCheckedChange = { premium = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = PremiumGold,
                                    checkedTrackColor = PremiumGold.copy(0.3f)
                                )
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (title.isNotBlank() && fileUrl.isNotBlank()) onSave(title, description, subject, type, fileUrl, thumbnailUrl, premium, emptyList()) },
                enabled = title.isNotBlank() && fileUrl.isNotBlank()
            ) {
                Text(if (editingResource != null) "Update" else "Create", color = NeonCyan, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = TextMuted) } },
        containerColor = SurfaceCard
    )
}

@Composable
private fun FormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    singleLine: Boolean = true
) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange,
        label = { Text(label, fontSize = 12.sp) },
        singleLine = singleLine,
        maxLines = if (singleLine) 1 else 3,
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NeonCyan,
            unfocusedBorderColor = TextMuted.copy(0.3f),
            focusedLabelColor = NeonCyan,
            unfocusedLabelColor = TextMuted,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            cursorColor = NeonCyan
        ),
        shape = RoundedCornerShape(10.dp),
        textStyle = TextStyle(fontSize = 13.sp)
    )
}
