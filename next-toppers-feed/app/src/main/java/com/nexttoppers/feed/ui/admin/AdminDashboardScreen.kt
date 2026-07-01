package com.nexttoppers.feed.ui.admin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.Article
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.LibraryBooks
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.Quiz
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Report
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.WorkspacePremium
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nexttoppers.feed.data.model.Announcement
import com.nexttoppers.feed.data.model.Resource
import com.nexttoppers.feed.data.model.User
import com.nexttoppers.feed.data.repository.AdminStats
import com.nexttoppers.feed.ui.theme.BackgroundBlack
import com.nexttoppers.feed.ui.theme.NeonCyan
import com.nexttoppers.feed.ui.theme.NeonGreen
import com.nexttoppers.feed.ui.theme.PremiumGold
import com.nexttoppers.feed.ui.theme.SurfaceCard
import com.nexttoppers.feed.ui.theme.TextMuted
import com.nexttoppers.feed.ui.theme.TextPrimary
import com.nexttoppers.feed.ui.theme.TextSecondary

private val AdminRed    = Color(0xFFFF4D6D)
private val AdminPurple = Color(0xFFB388FF)

@Composable
fun AdminDashboardScreen(
    onNavigateToPremiumRequests: () -> Unit,
    onNavigateToResources: () -> Unit,
    onNavigateToAnnouncements: () -> Unit,
    onNavigateToModeration: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToQuizManagement: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    viewModel: AdminDashboardViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Box(Modifier.fillMaxSize().background(BackgroundBlack)) {
        // Ambient glow
        Box(
            Modifier.fillMaxWidth().height(320.dp)
                .background(
                    Brush.radialGradient(
                        listOf(AdminPurple.copy(0.08f), NeonGreen.copy(0.04f), Color.Transparent),
                        radius = 900f
                    )
                )
        )

        when {
            state.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = NeonGreen, modifier = Modifier.size(48.dp))
                }
            }
            !state.isAdmin -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Rounded.Shield, null, tint = AdminRed.copy(0.5f), modifier = Modifier.size(72.dp))
                        Text("Access Denied", color = AdminRed, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text("Admin privileges required", color = TextMuted, fontSize = 13.sp)
                    }
                }
            }
            else -> {
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(350)) + slideInVertically(tween(350)) { it / 10 }
                ) {
                    AdminDashboardContent(
                        state                     = state,
                        onRefresh                 = viewModel::refresh,
                        onPremiumRequests         = onNavigateToPremiumRequests,
                        onResources               = onNavigateToResources,
                        onAnnouncements           = onNavigateToAnnouncements,
                        onModeration              = onNavigateToModeration,
                        onAnalytics               = onNavigateToAnalytics,
                        onQuizManagement          = onNavigateToQuizManagement,
                        onNotifications           = onNavigateToNotifications
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminDashboardContent(
    state: AdminDashboardUiState,
    onRefresh: () -> Unit,
    onPremiumRequests: () -> Unit,
    onResources: () -> Unit,
    onAnnouncements: () -> Unit,
    onModeration: () -> Unit,
    onAnalytics: () -> Unit,
    onQuizManagement: () -> Unit,
    onNotifications: () -> Unit
) {
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 56.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // ── Header ────────────────────────────────────────────────────────────────
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "Admin Panel",
                        style = TextStyle(
                            fontSize = 28.sp, fontWeight = FontWeight.ExtraBold,
                            brush = Brush.linearGradient(listOf(AdminPurple, NeonCyan)),
                            shadow = Shadow(AdminPurple.copy(0.4f), Offset.Zero, 16f)
                        )
                    )
                    Text("Next Toppers Control Center", color = TextMuted, fontSize = 12.sp)
                }
                Box(
                    Modifier.size(40.dp)
                        .background(AdminPurple.copy(0.12f), CircleShape)
                        .border(1.dp, AdminPurple.copy(0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.AdminPanelSettings, null, tint = AdminPurple, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = onRefresh, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Rounded.Refresh, null, tint = NeonGreen.copy(0.7f), modifier = Modifier.size(20.dp))
                }
            }
            Spacer(Modifier.height(20.dp))
        }

        // ── Stats grid ────────────────────────────────────────────────────────────
        item {
            AdminStatsGrid(state.stats)
            Spacer(Modifier.height(24.dp))
        }

        // ── Pending premium badge ─────────────────────────────────────────────────
        if (state.stats.pendingRequests > 0) {
            item {
                PendingRequestsBanner(state.stats.pendingRequests, onPremiumRequests)
                Spacer(Modifier.height(16.dp))
            }
        }

        // ── Quick actions ─────────────────────────────────────────────────────────
        item {
            Text("Quick Actions", color = TextSecondary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Spacer(Modifier.height(10.dp))
        }
        item {
            AdminActionsGrid(
                onPremiumRequests = onPremiumRequests,
                onResources       = onResources,
                onAnnouncements   = onAnnouncements,
                onModeration      = onModeration,
                onAnalytics       = onAnalytics,
                onQuizManagement  = onQuizManagement,
                onNotifications   = onNotifications,
                pendingCount      = state.stats.pendingRequests
            )
            Spacer(Modifier.height(24.dp))
        }

        // ── Recent users ──────────────────────────────────────────────────────────
        if (state.recentUsers.isNotEmpty()) {
            item {
                Text("Recent Users", color = TextSecondary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(Modifier.height(10.dp))
            }
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(end = 8.dp)
                ) {
                    items(state.recentUsers) { user ->
                        UserMiniCard(user)
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }

        // ── Recent resources ──────────────────────────────────────────────────────
        if (state.recentResources.isNotEmpty()) {
            item {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("Recent Uploads", color = TextSecondary, fontWeight = FontWeight.Bold,
                        fontSize = 13.sp, modifier = Modifier.weight(1f))
                    Text("${state.recentResources.size} items", color = TextMuted, fontSize = 11.sp)
                }
                Spacer(Modifier.height(10.dp))
            }
            itemsIndexed(state.recentResources.take(4)) { _, resource ->
                ResourceAdminRow(resource, onResources)
                Spacer(Modifier.height(8.dp))
            }
            item { Spacer(Modifier.height(16.dp)) }
        }

        // ── Recent announcements ──────────────────────────────────────────────────
        if (state.recentAnnouncements.isNotEmpty()) {
            item {
                Text("Latest Announcements", color = TextSecondary, fontWeight = FontWeight.Bold,
                    fontSize = 13.sp)
                Spacer(Modifier.height(10.dp))
            }
            items(state.recentAnnouncements) { ann ->
                AnnouncementAdminRow(ann, onAnnouncements)
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun AdminStatsGrid(stats: AdminStats) {
    val items = listOf(
        Triple("Users", "${stats.totalUsers}", NeonGreen),
        Triple("Premium", "${stats.premiumUsers}", PremiumGold),
        Triple("Resources", "${stats.totalResources}", NeonCyan),
        Triple("Pending", "${stats.pendingRequests}", if (stats.pendingRequests > 0) AdminRed else TextMuted),
        Triple("Posts", "${stats.totalCommunityPosts}", AdminPurple),
        Triple("Announcements", "${stats.totalAnnouncements}", Color(0xFF69FF47))
    )
    val rows = items.chunked(3)
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        rows.forEach { rowItems ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                rowItems.forEach { (label, value, color) ->
                    AdminStatCard(label = label, value = value, color = color, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun AdminStatCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    val infinite = rememberInfiniteTransition(label = "glow")
    val alpha by infinite.animateFloat(
        0.15f, 0.3f,
        animationSpec = infiniteRepeatable(tween(1800, easing = LinearEasing), RepeatMode.Reverse),
        label = "alpha"
    )
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(listOf(color.copy(0.12f), color.copy(0.04f)))
            )
            .border(1.dp, color.copy(alpha), RoundedCornerShape(16.dp))
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                value, color = color, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp,
                style = TextStyle(
                    shadow = Shadow(color.copy(0.5f), Offset.Zero, 10f),
                    fontWeight = FontWeight.ExtraBold, fontSize = 22.sp
                )
            )
            Text(label, color = TextMuted, fontSize = 10.sp)
        }
    }
}

@Composable
private fun PendingRequestsBanner(count: Int, onClick: () -> Unit) {
    val infinite = rememberInfiniteTransition(label = "banner")
    val pulse by infinite.animateFloat(
        0.6f, 1f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "pulse"
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(AdminRed.copy(0.08f))
            .border(1.dp, AdminRed.copy(pulse * 0.6f), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(36.dp)
                .background(AdminRed.copy(0.15f), RoundedCornerShape(10.dp))
                .border(1.dp, AdminRed.copy(0.4f), RoundedCornerShape(10.dp))
                .graphicsLayer { alpha = pulse },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.WorkspacePremium, null, tint = AdminRed, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                "🔔 $count Premium Request${if (count > 1) "s" else ""} Pending",
                color = AdminRed, fontWeight = FontWeight.Bold, fontSize = 13.sp
            )
            Text("Tap to review & approve", color = TextMuted, fontSize = 11.sp)
        }
        Icon(Icons.Rounded.ChevronRight, null, tint = AdminRed.copy(0.7f), modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun AdminActionsGrid(
    onPremiumRequests: () -> Unit,
    onResources: () -> Unit,
    onAnnouncements: () -> Unit,
    onModeration: () -> Unit,
    onAnalytics: () -> Unit,
    onQuizManagement: () -> Unit,
    onNotifications: () -> Unit,
    pendingCount: Int
) {
    data class Action(val label: String, val icon: ImageVector, val color: Color, val badge: Int = 0, val onClick: () -> Unit)
    val actions = listOf(
        Action("Premium\nRequests",  Icons.Rounded.WorkspacePremium,   PremiumGold,           pendingCount, onPremiumRequests),
        Action("Resources",          Icons.Rounded.LibraryBooks,        NeonCyan,              0,            onResources),
        Action("Announcements",      Icons.Rounded.Campaign,            NeonGreen,             0,            onAnnouncements),
        Action("Moderation",         Icons.Rounded.Report,              AdminRed,              0,            onModeration),
        Action("Analytics",          Icons.Rounded.Analytics,           AdminPurple,           0,            onAnalytics),
        Action("Quiz Mgmt",          Icons.Rounded.Quiz,                Color(0xFFFF6B35),     0,            onQuizManagement),
        Action("Notifications",      Icons.Rounded.NotificationsActive, Color(0xFFB388FF),     0,            onNotifications)
    )
    val rows = actions.chunked(3)
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        rows.forEach { rowItems ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                rowItems.forEach { action ->
                    AdminActionCard(action.label, action.icon, action.color, action.badge, action.onClick, Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun AdminActionCard(
    label: String, icon: ImageVector, color: Color,
    badge: Int, onClick: () -> Unit, modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Brush.verticalGradient(listOf(color.copy(0.12f), color.copy(0.04f))))
                .border(1.dp, color.copy(0.25f), RoundedCornerShape(16.dp))
                .clickable(onClick = onClick)
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                Modifier.size(40.dp)
                    .background(color.copy(0.15f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            }
            Text(label, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center, lineHeight = 15.sp)
        }
        if (badge > 0) {
            Box(
                Modifier.align(Alignment.TopEnd).padding(6.dp)
                    .size(18.dp).background(AdminRed, CircleShape)
                    .border(1.5.dp, BackgroundBlack, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("$badge", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun UserMiniCard(user: User) {
    Column(
        modifier = Modifier
            .width(90.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceCard)
            .border(1.dp, NeonGreen.copy(0.15f), RoundedCornerShape(14.dp))
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            Modifier.size(34.dp)
                .background(
                    Brush.linearGradient(listOf(NeonGreen.copy(0.2f), NeonCyan.copy(0.1f))),
                    CircleShape
                )
                .border(1.dp, NeonGreen.copy(0.3f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                user.name.take(1).uppercase(),
                color = NeonGreen, fontWeight = FontWeight.Bold, fontSize = 14.sp
            )
        }
        Text(
            user.name.take(10), color = TextPrimary, fontSize = 11.sp,
            fontWeight = FontWeight.Medium, maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
        if (user.isPremium) {
            Box(
                Modifier.background(PremiumGold.copy(0.15f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 4.dp, vertical = 1.dp)
            ) {
                Text("PRO", color = PremiumGold, fontSize = 8.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ResourceAdminRow(resource: Resource, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceCard)
            .border(1.dp, NeonCyan.copy(0.12f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(36.dp)
                .background(NeonCyan.copy(0.1f), RoundedCornerShape(9.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.Article, null, tint = NeonCyan, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(resource.title, color = TextPrimary, fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold, maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
            Text("${resource.subject} · ${resource.type}", color = TextMuted, fontSize = 11.sp)
        }
        if (resource.premium) {
            Box(
                Modifier.background(PremiumGold.copy(0.15f), RoundedCornerShape(5.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text("PRO", color = PremiumGold, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun AnnouncementAdminRow(ann: Announcement, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceCard)
            .border(1.dp, NeonGreen.copy(0.12f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(36.dp)
                .background(NeonGreen.copy(0.1f), RoundedCornerShape(9.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.Campaign, null, tint = NeonGreen, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(ann.title, color = TextPrimary, fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold, maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
            Text(ann.content.take(50), color = TextMuted, fontSize = 11.sp, maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
        }
        if (ann.isUrgent) {
            Box(
                Modifier.background(AdminRed.copy(0.15f), RoundedCornerShape(5.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text("URGENT", color = AdminRed, fontSize = 8.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
