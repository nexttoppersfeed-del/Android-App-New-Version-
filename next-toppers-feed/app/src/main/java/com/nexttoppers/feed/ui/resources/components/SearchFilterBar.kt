package com.nexttoppers.feed.ui.resources.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.ViewList
import androidx.compose.material.icons.rounded.ViewModule
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexttoppers.feed.data.model.ResourceType
import com.nexttoppers.feed.ui.theme.NeonGreen
import com.nexttoppers.feed.ui.theme.SurfaceCard
import com.nexttoppers.feed.ui.theme.SurfaceElevated
import com.nexttoppers.feed.ui.theme.TextMuted
import com.nexttoppers.feed.ui.theme.TextPrimary
import com.nexttoppers.feed.ui.theme.TextSecondary

// ── Global search bar (for ResourcesScreen) ───────────────────────────────────
@Composable
fun NtfSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search resources…"
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceCard)
            .border(1.dp, NeonGreen.copy(0.2f), RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Rounded.Search, null, tint = NeonGreen.copy(0.7f), modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(10.dp))
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            singleLine = true,
            cursorBrush = SolidColor(NeonGreen),
            textStyle = TextStyle(color = TextPrimary, fontSize = 14.sp),
            modifier = Modifier.weight(1f),
            decorationBox = { inner ->
                if (query.isEmpty()) {
                    Text(placeholder, color = TextMuted, fontSize = 14.sp)
                }
                inner()
            }
        )
        if (query.isNotEmpty()) {
            IconButton(onClick = onClear, modifier = Modifier.size(20.dp)) {
                Icon(Icons.Rounded.Close, null, tint = TextSecondary, modifier = Modifier.size(16.dp))
            }
        }
    }
}

// ── Type filter chips (for SubjectResourcesScreen) ────────────────────────────
@Composable
fun TypeFilterChips(
    selectedType: ResourceType?,
    onTypeSelected: (ResourceType?) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            label = "All",
            selected = selectedType == null,
            color = NeonGreen,
            onClick = { onTypeSelected(null) }
        )
        ResourceType.values().forEach { type ->
            FilterChip(
                label = "${type.emoji} ${type.displayName}",
                selected = selectedType == type,
                color = resourceTypeAccent(type.name),
                onClick = { onTypeSelected(if (selectedType == type) null else type) }
            )
        }
    }
}

// ── Premium filter chips ───────────────────────────────────────────────────────
@Composable
fun PremiumFilterChips(
    premiumFilter: Boolean?,
    onFilterSelected: (Boolean?) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip("All", premiumFilter == null, NeonGreen) { onFilterSelected(null) }
        FilterChip("Free", premiumFilter == false, Color(0xFF80CBC4)) { onFilterSelected(if (premiumFilter == false) null else false) }
        FilterChip("Pro", premiumFilter == true, Color(0xFFFFD700)) { onFilterSelected(if (premiumFilter == true) null else true) }
    }
}

// ── Individual filter chip ─────────────────────────────────────────────────────
@Composable
fun FilterChip(
    label: String,
    selected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(if (selected) color else SurfaceElevated)
            .border(1.dp, if (selected) color else color.copy(0.3f), RoundedCornerShape(50.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Text(
            label,
            color = if (selected) Color.Black else color.copy(0.85f),
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 12.sp
        )
    }
}

// ── View mode toggle (list / grid) ────────────────────────────────────────────
@Composable
fun ViewModeToggle(isGridView: Boolean, onToggle: () -> Unit) {
    IconButton(onClick = onToggle, modifier = Modifier.size(36.dp)) {
        Icon(
            imageVector = if (isGridView) Icons.Rounded.ViewList else Icons.Rounded.ViewModule,
            contentDescription = "Toggle view",
            tint = NeonGreen,
            modifier = Modifier.size(22.dp)
        )
    }
}
