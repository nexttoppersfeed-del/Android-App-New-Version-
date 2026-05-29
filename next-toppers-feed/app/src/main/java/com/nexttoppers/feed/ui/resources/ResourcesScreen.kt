package com.nexttoppers.feed.ui.resources

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nexttoppers.feed.ui.components.SectionHeader
import com.nexttoppers.feed.ui.components.SkeletonCard
import com.nexttoppers.feed.ui.components.SkeletonRow
import com.nexttoppers.feed.ui.resources.components.CategoryCard
import com.nexttoppers.feed.ui.resources.components.NtfSearchBar
import com.nexttoppers.feed.ui.resources.components.ResourceListCard
import com.nexttoppers.feed.ui.resources.components.ResourceMiniCard
import com.nexttoppers.feed.ui.theme.BackgroundBlack
import com.nexttoppers.feed.ui.theme.NeonCyan
import com.nexttoppers.feed.ui.theme.NeonGreen
import com.nexttoppers.feed.ui.theme.TextMuted
import com.nexttoppers.feed.ui.theme.TextSecondary

@Composable
fun ResourcesScreen(
    onNavigateToSubject: (String) -> Unit,
    onNavigateToDetail: (String) -> Unit,
    viewModel: ResourcesViewModel = hiltViewModel()
) {

    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchState by viewModel.searchState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundBlack)
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(
                    Brush.radialGradient(
                        listOf(
                            NeonCyan.copy(alpha = 0.07f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .padding(top = 52.dp, bottom = 100.dp)
                .verticalScroll(rememberScrollState()),

            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            Column {

                Text(
                    text = "Resources",

                    style = TextStyle(
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,

                        brush = Brush.linearGradient(
                            listOf(
                                NeonGreen,
                                NeonCyan
                            )
                        ),

                        shadow = Shadow(
                            color = NeonGreen.copy(alpha = 0.3f),
                            offset = Offset.Zero,
                            blurRadius = 14f
                        )
                    )
                )

                Text(
                    text = "Notes, Lectures, PDFs & More",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            }

            NtfSearchBar(
                query = searchQuery,
                onQueryChange = viewModel::onSearchQueryChange,
                onClear = viewModel::clearSearch
            )

            AnimatedVisibility(
                visible = searchQuery.isNotBlank(),

                enter =
                fadeIn(tween(200)) +
                        slideInVertically(
                            tween(200)
                        ) { -20 },

                exit = fadeOut(tween(150))
            ) {

                SearchResultsSection(
                    state = searchState,
                    onItemClick = onNavigateToDetail
                )
            }

            AnimatedVisibility(
                visible = searchQuery.isBlank(),

                enter = fadeIn(tween(200)),
                exit = fadeOut(tween(150))
            ) {

                Column(
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {

                    when (val state = uiState) {

                        is ResourcesUiState.Loading -> {
                            ResourcesDashboardSkeleton()
                        }

                        is ResourcesUiState.Error -> {

                            Text(
                                text = state.message,
                                color = TextSecondary,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        }

                        is ResourcesUiState.Success -> {

                            if (state.recent.isNotEmpty()) {

                                SectionHeader(
                                    title = "Recently Added"
                                )

                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),

                                    contentPadding = PaddingValues(
                                        end = 20.dp
                                    )
                                ) {

                                    itemsIndexed(state.recent) { _, resource ->

                                        ResourceMiniCard(
                                            resource = resource,

                                            onClick = {
                                                onNavigateToDetail(resource.id)
                                            }
                                        )
                                    }
                                }
                            }

                            SectionHeader(
                                title = "Browse by Subject"
                            )

                            Column(
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {

                                state.subjectCounts.forEachIndexed { index, subjectCount ->

                                    CategoryCard(
                                        subjectCount = subjectCount,
                                        index = index,

                                        onClick = {
                                            onNavigateToSubject(
                                                subjectCount.subject.name
                                            )
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
private fun SearchResultsSection(
    state: SearchState,
    onItemClick: (String) -> Unit
) {

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {

        when (state) {

            is SearchState.Searching -> {

                repeat(3) {
                    SkeletonCard(height = 80.dp)
                }
            }

            is SearchState.Empty -> {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),

                    contentAlignment = Alignment.Center
                ) {

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Text(
                            text = "🔍",
                            fontSize = 36.sp
                        )

                        Spacer(
                            modifier = Modifier.height(8.dp)
                        )

                        Text(
                            text = "No results found",
                            color = TextSecondary,
                            fontSize = 14.sp
                        )

                        Text(
                            text = "Try a different search term",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            is SearchState.Results -> {

                Text(
                    text =
                    "${state.items.size} result${
                        if (state.items.size != 1) "s" else ""
                    }",

                    color = NeonGreen,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )

                state.items.forEachIndexed { index, resource ->

                    ResourceListCard(
                        resource = resource,
                        index = index,

                        onClick = {
                            onItemClick(resource.id)
                        }
                    )
                }
            }

            else -> Unit
        }
    }
}

@Composable
private fun ResourcesDashboardSkeleton() {

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        SkeletonCard(
            height = 100.dp
        )

        repeat(5) {
            SkeletonRow()
        }
    }
}
