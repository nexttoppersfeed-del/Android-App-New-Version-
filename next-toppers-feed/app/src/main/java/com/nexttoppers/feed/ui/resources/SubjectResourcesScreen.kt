package com.nexttoppers.feed.ui.resources

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nexttoppers.feed.data.model.ResourceSubject
import com.nexttoppers.feed.data.model.ResourceType
import com.nexttoppers.feed.ui.components.NeonDivider
import com.nexttoppers.feed.ui.components.SkeletonCard
import com.nexttoppers.feed.ui.resources.components.ResourceGridCard
import com.nexttoppers.feed.ui.resources.components.ResourceListCard
import com.nexttoppers.feed.ui.resources.components.ViewModeToggle
import com.nexttoppers.feed.ui.resources.components.subjectAccentColor
import com.nexttoppers.feed.ui.theme.AccentCyan
import com.nexttoppers.feed.ui.theme.AccentEmerald
import com.nexttoppers.feed.ui.theme.BackgroundBlack
import com.nexttoppers.feed.ui.theme.NeonGreen
import com.nexttoppers.feed.ui.theme.PremiumGold
import com.nexttoppers.feed.ui.theme.SurfaceCard
import com.nexttoppers.feed.ui.theme.SurfaceDark
import com.nexttoppers.feed.ui.theme.SurfaceElevated
import com.nexttoppers.feed.ui.theme.TextMuted
import com.nexttoppers.feed.ui.theme.TextPrimary
import com.nexttoppers.feed.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectResourcesScreen(
    onBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    viewModel: SubjectResourcesViewModel = hiltViewModel()
) {
    val uiState      by viewModel.uiState.collectAsState()
    val selectedType by viewModel.selectedType.collectAsState()
    val premiumFilter by viewModel.premiumFilter.collectAsState()
    val sortOrder    by viewModel.sortOrder.collectAsState()
    val isGridView   by viewModel.isGridView.collectAsState()

    var visible by remember { mutableStateOf(false) }
    var showFilterSheet by remember { mutableStateOf(false) }
    val sheetState  = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope       = rememberCoroutineScope()

    LaunchedEffect(Unit) { visible = true }

    val subjectEnum = ResourceSubject.values().firstOrNull {
        it.name.equals(viewModel.subject, ignoreCase = true)
    }
    val accent      = subjectEnum?.let { subjectAccentColor(it) } ?: NeonGreen
    val displayName = subjectEnum?.displayName ?: viewModel.subject
    val emoji       = subjectEnum?.emoji ?: "📚"

    // Count active filters for the badge
    val activeFilterCount = listOfNotNull(selectedType, premiumFilter).size +
            if (sortOrder != SortOrder.LATEST) 1 else 0

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
                    Brush.radialGradient(listOf(accent.copy(0.06f), Color.Transparent))
                )
        )

        AnimatedVisibility(
            visible = visible,
            enter   = fadeIn(tween(300)) + slideInVertically(tween(300)) { it / 8 }
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                // ── Header ───────────────────────────────────────────────────────
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(top = 52.dp, bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier          = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
                            Icon(Icons.Rounded.ArrowBack, null, tint = TextSecondary)
                        }
                        Spacer(Modifier.width(6.dp))
                        Text(emoji, fontSize = 22.sp)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text     = displayName,
                            style    = TextStyle(
                                fontSize   = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                brush      = Brush.linearGradient(listOf(accent, NeonGreen)),
                                shadow     = Shadow(accent.copy(0.3f), Offset.Zero, 12f)
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        // View mode toggle
                        ViewModeToggle(isGridView = isGridView, onToggle = viewModel::toggleViewMode)

                        Spacer(Modifier.width(4.dp))

                        // Filter button with active-count badge
                        Box {
                            IconButton(
                                onClick  = { showFilterSheet = true },
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (activeFilterCount > 0)
                                            accent.copy(0.18f)
                                        else
                                            Color.Transparent
                                    )
                                    .border(
                                        1.dp,
                                        if (activeFilterCount > 0) accent.copy(0.5f)
                                        else Color.Transparent,
                                        RoundedCornerShape(12.dp)
                                    )
                            ) {
                                Icon(
                                    Icons.Rounded.FilterList,
                                    null,
                                    tint = if (activeFilterCount > 0) accent else TextSecondary
                                )
                            }
                            // Badge
                            if (activeFilterCount > 0) {
                                Box(
                                    modifier          = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(accent)
                                        .align(Alignment.TopEnd),
                                    contentAlignment  = Alignment.Center
                                ) {
                                    Text(
                                        "$activeFilterCount",
                                        color    = BackgroundBlack,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }
                    }

                    NeonDivider()
                }

                // ── Content ──────────────────────────────────────────────────────
                when (val state = uiState) {

                    is SubjectResourcesUiState.Loading -> {
                        Column(
                            modifier = Modifier.padding(horizontal = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            repeat(5) { SkeletonCard(height = 90.dp) }
                        }
                    }

                    is SubjectResourcesUiState.Empty -> {
                        EmptySubjectState(subject = displayName)
                    }

                    is SubjectResourcesUiState.Error -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Failed to load resources", color = TextSecondary)
                                Text(state.message, color = TextMuted, fontSize = 12.sp)
                            }
                        }
                    }

                    is SubjectResourcesUiState.Success -> {
                        Text(
                            "${state.items.size} resources",
                            color    = accent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                        )

                        if (isGridView) {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                contentPadding = PaddingValues(
                                    start = 20.dp, top = 0.dp, end = 20.dp, bottom = 100.dp
                                ),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalArrangement   = Arrangement.spacedBy(12.dp)
                            ) {
                                itemsIndexed(state.items) { index, resource ->
                                    ResourceGridCard(
                                        resource = resource,
                                        index    = index,
                                        onClick  = { onNavigateToDetail(resource.id) }
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                contentPadding = PaddingValues(
                                    start = 20.dp, top = 0.dp, end = 20.dp, bottom = 100.dp
                                ),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                itemsIndexed(state.items) { index, resource ->
                                    ResourceListCard(
                                        resource = resource,
                                        index    = index,
                                        onClick  = { onNavigateToDetail(resource.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // ── Filter bottom sheet ──────────────────────────────────────────────────
        if (showFilterSheet) {
            FilterBottomSheet(
                sheetState    = sheetState,
                sortOrder     = sortOrder,
                selectedType  = selectedType,
                premiumFilter = premiumFilter,
                accent        = accent,
                onSortChange  = { viewModel.setSortOrder(it) },
                onTypeChange  = { viewModel.selectType(it) },
                onAccessChange = { viewModel.setPremiumFilter(it) },
                onClearAll    = { viewModel.clearAllFilters() },
                onDismiss     = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        showFilterSheet = false
                    }
                }
            )
        }
    }
}

// ── Filter bottom sheet ────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterBottomSheet(
    sheetState: SheetState,
    sortOrder: SortOrder,
    selectedType: ResourceType?,
    premiumFilter: Boolean?,
    accent: Color,
    onSortChange: (SortOrder) -> Unit,
    onTypeChange: (ResourceType?) -> Unit,
    onAccessChange: (Boolean?) -> Unit,
    onClearAll: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest  = onDismiss,
        sheetState        = sheetState,
        containerColor    = SurfaceDark,
        tonalElevation    = 0.dp,
        shape             = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Sheet header
            Row(
                modifier          = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Filter & Sort",
                    color      = TextPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize   = 18.sp
                )
                Text(
                    "Clear all",
                    color    = AccentCyan,
                    fontSize = 13.sp,
                    modifier = Modifier.clickable(onClick = onClearAll)
                )
            }

            NeonDivider()

            // ── Sort section ──────────────────────────────────────────────────
            FilterSectionLabel("Sort by")
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                SortOrder.values().forEach { order ->
                    FilterRadioRow(
                        label      = order.label,
                        selected   = sortOrder == order,
                        accentColor = accent,
                        onClick    = { onSortChange(order) }
                    )
                }
            }

            NeonDivider()

            // ── Type section ──────────────────────────────────────────────────
            FilterSectionLabel("Category")
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                FilterRadioRow(
                    label      = "All types",
                    selected   = selectedType == null,
                    accentColor = accent,
                    onClick    = { onTypeChange(null) }
                )
                listOf(
                    ResourceType.NOTES    to "Notes",
                    ResourceType.LECTURE  to "Lectures",
                    ResourceType.DPP      to "DPP / Practice",
                    ResourceType.MODULE   to "Modules"
                ).forEach { (type, label) ->
                    FilterRadioRow(
                        label      = label,
                        selected   = selectedType == type,
                        accentColor = accent,
                        onClick    = { onTypeChange(if (selectedType == type) null else type) }
                    )
                }
            }

            NeonDivider()

            // ── Access section ────────────────────────────────────────────────
            FilterSectionLabel("Access")
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                FilterRadioRow("All", premiumFilter == null, accent) { onAccessChange(null) }
                FilterRadioRow("Free only", premiumFilter == false, accent) {
                    onAccessChange(if (premiumFilter == false) null else false)
                }
                FilterRadioRow("Premium only", premiumFilter == true, PremiumGold) {
                    onAccessChange(if (premiumFilter == true) null else true)
                }
            }
        }
    }
}

@Composable
private fun FilterSectionLabel(text: String) {
    Text(text, color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
}

@Composable
private fun FilterRadioRow(
    label: String,
    selected: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) accentColor.copy(0.10f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        RadioButton(
            selected = selected,
            onClick  = null,
            colors   = RadioButtonDefaults.colors(
                selectedColor   = accentColor,
                unselectedColor = SurfaceElevated
            )
        )
        Text(
            text       = label,
            color      = if (selected) TextPrimary else TextSecondary,
            fontSize   = 14.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

// ── Empty state ────────────────────────────────────────────────────────────────
@Composable
private fun EmptySubjectState(subject: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.AutoStories,
                contentDescription = null,
                tint     = AccentEmerald.copy(0.4f),
                modifier = Modifier.size(64.dp)
            )
            Text("No resources yet", color = TextSecondary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Text(
                "Resources for $subject\nwill appear here once uploaded",
                color     = TextMuted,
                fontSize  = 13.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        }
    }
}
