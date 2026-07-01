package com.nexttoppers.feed.ui.activity

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nexttoppers.feed.data.model.FeedTab
import com.nexttoppers.feed.ui.components.SkeletonCard
import com.nexttoppers.feed.ui.theme.BackgroundBlack
import com.nexttoppers.feed.ui.theme.NeonGreen
import com.nexttoppers.feed.ui.theme.TextPrimary
import com.nexttoppers.feed.ui.theme.TextSecondary

@Composable
fun ActivityFeedScreen(
    onBack: () -> Unit,
    viewModel: ActivityFeedViewModel = hiltViewModel()
) {
    val uiState    by viewModel.uiState.collectAsState()
    val activeTab  by viewModel.activeTab.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundBlack)
    ) {
        // ── Top bar ────────────────────────────────────────────────────────────
        Row(
            modifier          = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Rounded.ArrowBackIosNew, null, tint = TextSecondary, modifier = Modifier.size(18.dp))
            }
            Column(Modifier.weight(1f)) {
                Text("Activity Feed", color = TextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                Text("See what toppers are achieving", color = TextSecondary.copy(0.6f), fontSize = 12.sp)
            }
        }

        // ── Feed tabs ──────────────────────────────────────────────────────────
        FeedTabRow(
            selectedTab    = activeTab,
            onTabSelected  = { viewModel.selectTab(it) },
            modifier       = Modifier.padding(horizontal = 20.dp).padding(bottom = 12.dp)
        )

        // ── Content ────────────────────────────────────────────────────────────
        when {
            activeTab == FeedTab.FRIENDS -> {
                Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    FriendsFeedPlaceholder()
                }
            }

            uiState is ActivityFeedUiState.Loading -> {
                Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    repeat(6) { SkeletonCard(height = 100.dp) }
                }
            }

            uiState is ActivityFeedUiState.Empty -> {
                Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    EmptyFeedState(isPersonal = activeTab == FeedTab.PERSONAL)
                }
            }

            uiState is ActivityFeedUiState.Error -> {
                Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Rounded.Warning, null, tint = TextSecondary, modifier = Modifier.size(40.dp))
                        Text((uiState as ActivityFeedUiState.Error).message, color = TextSecondary, fontSize = 13.sp, textAlign = TextAlign.Center)
                    }
                }
            }

            uiState is ActivityFeedUiState.Success -> {
                val items = (uiState as ActivityFeedUiState.Success).items
                LazyColumn(
                    modifier      = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    // Timeline header
                    item {
                        Text(
                            if (activeTab == FeedTab.PERSONAL) "Your Activity Timeline" else "Global Activity",
                            color      = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize   = 14.sp,
                            modifier   = Modifier.padding(bottom = 12.dp)
                        )
                    }
                    itemsIndexed(items, key = { _, item -> item.id }) { index, item ->
                        var visible by remember { mutableStateOf(false) }
                        LaunchedEffect(Unit) { visible = true }
                        AnimatedVisibility(
                            visible = visible,
                            enter   = fadeIn(tween(280, delayMillis = index * 50)) +
                                      slideInVertically(tween(280, delayMillis = index * 50)) { it / 5 }
                        ) {
                            ActivityFeedCard(
                                item    = item,
                                isFirst = index == 0,
                                isLast  = index == items.lastIndex
                            )
                        }
                    }
                    item { Spacer(Modifier.height(32.dp)) }
                }
            }
        }
    }
}
