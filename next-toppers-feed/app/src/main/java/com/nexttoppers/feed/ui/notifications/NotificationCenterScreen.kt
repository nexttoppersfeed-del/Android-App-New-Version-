package com.nexttoppers.feed.ui.notifications

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.nexttoppers.feed.data.model.NotificationFilter
import com.nexttoppers.feed.ui.components.SkeletonCard
import com.nexttoppers.feed.ui.theme.BackgroundBlack
import com.nexttoppers.feed.ui.theme.ErrorRed
import com.nexttoppers.feed.ui.theme.NeonGreen
import com.nexttoppers.feed.ui.theme.TextMuted
import com.nexttoppers.feed.ui.theme.TextPrimary
import com.nexttoppers.feed.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationCenterScreen(
    onBack: () -> Unit,
    onNavigateTo: (String) -> Unit = {},
    viewModel: NotificationViewModel = hiltViewModel()
) {
    val uiState      by viewModel.uiState.collectAsState()
    val unreadCount  by viewModel.unreadCount.collectAsState()
    val activeFilter by viewModel.activeFilter.collectAsState()

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
                Text(
                    "Notifications",
                    color      = TextPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize   = 20.sp
                )
                if (unreadCount > 0) {
                    Text(
                        "$unreadCount unread",
                        color    = ErrorRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            MarkAllReadButton(
                unreadCount = unreadCount,
                onClick     = { viewModel.markAllAsRead() }
            )
        }

        // ── Filter chips ───────────────────────────────────────────────────────
        NotificationFilterChips(
            activeFilter     = activeFilter,
            onFilterSelected = { viewModel.setFilter(it) },
            modifier         = Modifier.padding(bottom = 12.dp)
        )

        // ── Body ───────────────────────────────────────────────────────────────
        when (val state = uiState) {
            is NotificationUiState.Loading -> {
                Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    repeat(5) { SkeletonCard(height = 80.dp) }
                }
            }

            is NotificationUiState.Empty -> {
                Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    EmptyNotificationsState(
                        onSeedClick = if (activeFilter == NotificationFilter.ALL) ({ viewModel.seedSampleNotifications() }) else null
                    )
                }
            }

            is NotificationUiState.Error -> {
                Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Rounded.Warning, null, tint = TextSecondary, modifier = Modifier.size(40.dp))
                        Text(state.message, color = TextSecondary, fontSize = 13.sp, textAlign = TextAlign.Center)
                    }
                }
            }

            is NotificationUiState.Success -> {
                LazyColumn(
                    modifier              = Modifier.fillMaxSize(),
                    contentPadding        = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 4.dp),
                    verticalArrangement   = Arrangement.spacedBy(8.dp)
                ) {
                    state.groups.forEach { group ->
                        item(key = "header_${group.label}") {
                            NotificationGroupHeader(label = group.label, modifier = Modifier.padding(vertical = 4.dp))
                        }
                        itemsIndexed(group.notifications, key = { _, n -> n.id }) { index, notification ->
                            var visible by remember { mutableStateOf(false) }
                            LaunchedEffect(Unit) { visible = true }
                            AnimatedVisibility(
                                visible = visible,
                                enter   = fadeIn(tween(250, delayMillis = index * 40)) +
                                          slideInVertically(tween(250, delayMillis = index * 40)) { it / 6 }
                            ) {
                                NotificationCard(
                                    notification = notification,
                                    onClick = {
                                        viewModel.markAsRead(notification.id)
                                        if (notification.actionRoute.isNotEmpty()) onNavigateTo(notification.actionRoute)
                                    }
                                )
                            }
                        }
                        item(key = "spacer_${group.label}") { Spacer(Modifier.height(4.dp)) }
                    }
                    item { Spacer(Modifier.height(24.dp)) }
                }
            }
        }
    }
}
