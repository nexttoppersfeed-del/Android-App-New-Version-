package com.nexttoppers.feed.ui.resources

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoStories
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
import com.nexttoppers.feed.ui.components.NeonDivider
import com.nexttoppers.feed.ui.components.SkeletonCard
import com.nexttoppers.feed.ui.resources.components.PremiumFilterChips
import com.nexttoppers.feed.ui.resources.components.ResourceGridCard
import com.nexttoppers.feed.ui.resources.components.ResourceListCard
import com.nexttoppers.feed.ui.resources.components.TypeFilterChips
import com.nexttoppers.feed.ui.resources.components.ViewModeToggle
import com.nexttoppers.feed.ui.resources.components.subjectAccentColor
import com.nexttoppers.feed.ui.theme.BackgroundBlack
import com.nexttoppers.feed.ui.theme.NeonGreen
import com.nexttoppers.feed.ui.theme.TextMuted
import com.nexttoppers.feed.ui.theme.TextPrimary
import com.nexttoppers.feed.ui.theme.TextSecondary

@Composable
fun SubjectResourcesScreen(
    onBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    viewModel: SubjectResourcesViewModel = hiltViewModel()
) {

    val uiState by viewModel.uiState.collectAsState()
    val selectedType by viewModel.selectedType.collectAsState()
    val premiumFilter by viewModel.premiumFilter.collectAsState()
    val isGridView by viewModel.isGridView.collectAsState()

    var visible by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {
        visible = true
    }

    val subjectEnum =
        ResourceSubject.values().firstOrNull {
            it.name.equals(
                viewModel.subject,
                ignoreCase = true
            )
        }

    val accent =
        subjectEnum?.let {
            subjectAccentColor(it)
        } ?: NeonGreen

    val displayName =
        subjectEnum?.displayName ?: viewModel.subject

    val emoji =
        subjectEnum?.emoji ?: "📚"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundBlack)
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(
                    Brush.radialGradient(
                        listOf(
                            accent.copy(alpha = 0.06f),
                            Color.Transparent
                        )
                    )
                )
        )

        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(
                animationSpec = tween(300)
            ) + slideInVertically(
                animationSpec = tween(300)
            ) { it / 8 }
        ) {

            Column(
                modifier = Modifier.fillMaxSize()
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(
                            top = 52.dp,
                            bottom = 12.dp
                        ),

                    verticalArrangement =
                        Arrangement.spacedBy(14.dp)
                ) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.size(40.dp)
                        ) {

                            Icon(
                                imageVector = Icons.Rounded.ArrowBack,
                                contentDescription = null,
                                tint = TextSecondary
                            )
                        }

                        Spacer(
                            modifier = Modifier.width(6.dp)
                        )

                        Text(
                            text = emoji,
                            fontSize = 22.sp
                        )

                        Spacer(
                            modifier = Modifier.width(8.dp)
                        )

                        Text(
                            text = displayName,

                            style = TextStyle(
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,

                                brush = Brush.linearGradient(
                                    listOf(
                                        accent,
                                        NeonGreen
                                    )
                                ),

                                shadow = Shadow(
                                    color = accent.copy(alpha = 0.3f),
                                    offset = Offset.Zero,
                                    blurRadius = 12f
                                )
                            ),

                            modifier = Modifier.weight(1f)
                        )

                        ViewModeToggle(
                            isGridView = isGridView,
                            onToggle = viewModel::toggleViewMode
                        )
                    }

                    TypeFilterChips(
                        selectedType = selectedType,
                        onTypeSelected = viewModel::selectType
                    )

                    PremiumFilterChips(
                        premiumFilter = premiumFilter,
                        onFilterSelected = viewModel::setPremiumFilter
                    )

                    NeonDivider()
                }

                when (val state = uiState) {

                    is SubjectResourcesUiState.Loading -> {

                        Column(
                            modifier = Modifier.padding(horizontal = 20.dp),

                            verticalArrangement =
                                Arrangement.spacedBy(12.dp)
                        ) {

                            repeat(5) {
                                SkeletonCard(
                                    height = 90.dp
                                )
                            }
                        }
                    }

                    is SubjectResourcesUiState.Empty -> {

                        EmptySubjectState(
                            subject = displayName
                        )
                    }

                    is SubjectResourcesUiState.Error -> {

                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {

                            Column(
                                horizontalAlignment =
                                    Alignment.CenterHorizontally
                            ) {

                                Text(
                                    text = "Failed to load resources",
                                    color = TextSecondary
                                )

                                Text(
                                    text = state.message,
                                    color = TextMuted,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    is SubjectResourcesUiState.Success -> {

                        Text(
                            text = "${state.items.size} resources",

                            color = accent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,

                            modifier = Modifier.padding(
                                horizontal = 20.dp,
                                vertical = 4.dp
                            )
                        )

                        if (isGridView) {

                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),

                                contentPadding = PaddingValues(
                                    start = 20.dp,
                                    top = 0.dp,
                                    end = 20.dp,
                                    bottom = 100.dp
                                ),

                                horizontalArrangement =
                                    Arrangement.spacedBy(12.dp),

                                verticalArrangement =
                                    Arrangement.spacedBy(12.dp)
                            ) {

                                itemsIndexed(state.items) { index, resource ->

                                    ResourceGridCard(
                                        resource = resource,
                                        index = index,
                                        onClick = {
                                            onNavigateToDetail(resource.id)
                                        }
                                    )
                                }
                            }

                        } else {

                            LazyColumn(

                                contentPadding = PaddingValues(
                                    start = 20.dp,
                                    top = 0.dp,
                                    end = 20.dp,
                                    bottom = 100.dp
                                ),

                                verticalArrangement =
                                    Arrangement.spacedBy(10.dp)
                            ) {

                                itemsIndexed(state.items) { index, resource ->

                                    ResourceListCard(
                                        resource = resource,
                                        index = index,
                                        onClick = {
                                            onNavigateToDetail(resource.id)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptySubjectState(
    subject: String
) {

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {

        Column(
            horizontalAlignment =
                Alignment.CenterHorizontally,

            verticalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {

            Icon(
                imageVector = Icons.Rounded.AutoStories,
                contentDescription = null,
                tint = NeonGreen.copy(alpha = 0.4f),
                modifier = Modifier.size(64.dp)
            )

            Text(
                text = "No resources yet",
                color = TextSecondary,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "Resources for $subject\nwill appear here once uploaded",

                color = TextMuted,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        }
    }
}
