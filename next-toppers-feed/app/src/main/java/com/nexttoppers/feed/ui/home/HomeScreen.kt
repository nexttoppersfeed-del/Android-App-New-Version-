package com.nexttoppers.feed.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.Chat
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Quiz
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.nexttoppers.feed.data.model.LeaderboardEntry
import com.nexttoppers.feed.data.model.User
import com.nexttoppers.feed.data.model.toPremiumMembership
import com.nexttoppers.feed.ui.achievements.AchievementsMiniRow
import com.nexttoppers.feed.ui.announcements.AnnouncementCarouselWithViewModel
import com.nexttoppers.feed.ui.announcements.AnnouncementsSection
import com.nexttoppers.feed.ui.components.NtfCard
import com.nexttoppers.feed.ui.components.NtfGradientCard
import com.nexttoppers.feed.ui.components.SectionHeader
import com.nexttoppers.feed.ui.components.SkeletonCard
import com.nexttoppers.feed.ui.components.SkeletonRow
import com.nexttoppers.feed.ui.components.XpBadge
import com.nexttoppers.feed.ui.notifications.NotificationBellBadge
import com.nexttoppers.feed.ui.premium.PremiumBannerCard
import com.nexttoppers.feed.ui.theme.AccentBlue
import com.nexttoppers.feed.ui.theme.AccentCyan
import com.nexttoppers.feed.ui.theme.AccentIndigo
import com.nexttoppers.feed.ui.theme.AccentViolet
import com.nexttoppers.feed.ui.theme.AccentEmerald
import com.nexttoppers.feed.ui.theme.BackgroundBlack
import com.nexttoppers.feed.ui.theme.NeonCyan
import com.nexttoppers.feed.ui.theme.NeonGreen
import com.nexttoppers.feed.ui.theme.PremiumGold
import com.nexttoppers.feed.ui.theme.SurfaceCard
import com.nexttoppers.feed.ui.theme.SurfaceElevated
import com.nexttoppers.feed.ui.theme.TextMuted
import com.nexttoppers.feed.ui.theme.TextPrimary
import com.nexttoppers.feed.ui.theme.TextSecondary
import com.nexttoppers.feed.ui.xp.GlowingLevelCard
import com.nexttoppers.feed.ui.xp.RankCard
import com.nexttoppers.feed.ui.xp.StreakCard
import com.nexttoppers.feed.util.LevelUtils
import java.util.Calendar

@Composable
fun HomeScreen(
    onNavigateToNotes: () -> Unit,
    onNavigateToLectures: () -> Unit,
    onNavigateToTests: () -> Unit,
    onNavigateToChats: () -> Unit,
    onNavigateToLeaderboard: () -> Unit,
    onNavigateToPremium: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToAnnouncementDetail: (String) -> Unit = {},
    onNavigateToActivityFeed: () -> Unit = {},
    onNavigateToSubject: (String) -> Unit = {},
    onOpenDrawer: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState                 by viewModel.uiState.collectAsState()
    val userRank                by viewModel.userRank.collectAsState()
    val topEntries              by viewModel.topEntries.collectAsState()
    val unreadNotificationCount by viewModel.unreadNotificationCount.collectAsState()
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundBlack)
    ) {
        when (val state = uiState) {
            is HomeUiState.Loading -> HomeLoadingSkeleton()
            is HomeUiState.Error   -> {
                Text(state.message, color = TextSecondary, modifier = Modifier.align(Alignment.Center))
            }
            is HomeUiState.Success -> {
                AnimatedVisibility(
                    visible = visible,
                    enter   = fadeIn(tween(500)) + slideInVertically(tween(500, easing = FastOutSlowInEasing)) { it / 6 }
                ) {
                    HomeContent(
                        user                           = state.user,
                        userRank                       = userRank,
                        topEntries                     = topEntries,
                        unreadNotificationCount        = unreadNotificationCount,
                        onNavigateToNotes              = onNavigateToNotes,
                        onNavigateToLectures           = onNavigateToLectures,
                        onNavigateToTests              = onNavigateToTests,
                        onNavigateToChats              = onNavigateToChats,
                        onNavigateToLeaderboard        = onNavigateToLeaderboard,
                        onNavigateToPremium            = onNavigateToPremium,
                        onNavigateToSettings           = onNavigateToSettings,
                        onNavigateToNotifications      = onNavigateToNotifications,
                        onNavigateToAnnouncementDetail = onNavigateToAnnouncementDetail,
                        onNavigateToActivityFeed       = onNavigateToActivityFeed,
                        onNavigateToSubject            = onNavigateToSubject,
                        onOpenDrawer                   = onOpenDrawer
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeLoadingSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .padding(top = 60.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        SkeletonRow()
        SkeletonCard(height = 52.dp)
        SkeletonCard(height = 180.dp)
        SkeletonCard(height = 120.dp)
        SkeletonCard(height = 120.dp)
        SkeletonCard(height = 180.dp)
    }
}

@Composable
private fun HomeContent(
    user: User,
    userRank: Int,
    topEntries: List<LeaderboardEntry>,
    unreadNotificationCount: Int,
    onNavigateToNotes: () -> Unit,
    onNavigateToLectures: () -> Unit,
    onNavigateToTests: () -> Unit,
    onNavigateToChats: () -> Unit,
    onNavigateToLeaderboard: () -> Unit,
    onNavigateToPremium: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToAnnouncementDetail: (String) -> Unit,
    onNavigateToActivityFeed: () -> Unit,
    onNavigateToSubject: (String) -> Unit = {},
    onOpenDrawer: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 52.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // ── Top bar ──────────────────────────────────────────────────────────
        HomeTopBar(
            user                    = user,
            unreadNotificationCount = unreadNotificationCount,
            onNotificationClick     = onNavigateToNotifications,
            onSettingsClick         = onNavigateToSettings,
            onOpenDrawer            = onOpenDrawer
        )

        // ── Greeting ─────────────────────────────────────────────────────────
        GreetingSection(user)

        // ── Announcement carousel ─────────────────────────────────────────────
        AnnouncementCarouselWithViewModel(
            onAnnouncementClick = onNavigateToAnnouncementDetail
        )

        // ── Subjects quick access (primary focus) ─────────────────────────────
        SectionHeader("Subjects")
        SubjectsGrid(onNavigateToSubject = onNavigateToSubject)

        // ── Quick actions ──────────────────────────────────────────────────────
        SectionHeader("Quick Actions")
        QuickActionsGrid(
            onNotes       = onNavigateToNotes,
            onLectures    = onNavigateToLectures,
            onTests       = onNavigateToTests,
            onChats       = onNavigateToChats,
            onLeaderboard = onNavigateToLeaderboard,
            onPremium     = onNavigateToPremium
        )

        // ── Premium banner ────────────────────────────────────────────────────
        if (!user.isPremium) {
            PremiumBannerCard(
                isPremium     = false,
                membership    = user.toPremiumMembership(),
                onUpgradeClick = onNavigateToPremium,
                modifier      = Modifier.fillMaxWidth()
            )
        }

        // ── Top learners ──────────────────────────────────────────────────────
        if (topEntries.isNotEmpty()) {
            LeaderboardPreviewSection(
                entries    = topEntries,
                onSeeAll   = onNavigateToLeaderboard,
                currentUid = ""
            )
        }

        // ── Latest announcements ──────────────────────────────────────────────
        SectionHeader("Latest Announcements")
        AnnouncementsSection(onAnnouncementClick = onNavigateToAnnouncementDetail)
    }
}

// ── Subjects grid ──────────────────────────────────────────────────────────────
@Composable
private fun SubjectsGrid(onNavigateToSubject: (String) -> Unit) {
    val subjects = listOf(
        Triple("MATHS",   "Maths",   "📐"),
        Triple("SCIENCE", "Science", "🔬"),
        Triple("SST",     "SST",     "🌍"),
        Triple("ENGLISH", "English", "📖"),
        Triple("HINDI",   "Hindi",   "🇮🇳")
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        subjects.forEach { (key, label, emoji) ->
            Column(
                modifier = Modifier
                    .clickable { onNavigateToSubject(key) }
                    .padding(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(NeonGreen.copy(0.10f), RoundedCornerShape(14.dp))
                        .border(1.dp, NeonGreen.copy(0.25f), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(emoji, fontSize = 22.sp)
                }
                Text(label, fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
            }
        }
    }
}

// ── Top bar ────────────────────────────────────────────────────────────────────
@Composable
private fun HomeTopBar(
    user: User,
    unreadNotificationCount: Int,
    onNotificationClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onOpenDrawer: () -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Hamburger + Logo
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onOpenDrawer, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Rounded.Menu, null, tint = TextSecondary, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        Brush.linearGradient(listOf(AccentCyan, AccentEmerald)),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text("NT", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            }
            Spacer(Modifier.width(8.dp))
            Text(
                "Next Toppers",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        // Right actions
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            NotificationBellBadge(
                unreadCount = unreadNotificationCount,
                onClick     = onNotificationClick
            )
            XpBadge(xp = user.xp)
            // Avatar
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(AccentCyan.copy(0.2f))
                    .border(1.dp, AccentCyan.copy(0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (user.photoURL.isNotEmpty()) {
                    AsyncImage(
                        model = user.photoURL,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().clip(CircleShape)
                    )
                } else {
                    Text(
                        user.name.firstOrNull()?.uppercaseChar()?.toString() ?: "U",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentCyan
                    )
                }
            }
        }
    }
}

// ── Greeting ───────────────────────────────────────────────────────────────────
@Composable
private fun GreetingSection(user: User) {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when {
        hour < 12 -> "Good morning"
        hour < 17 -> "Good afternoon"
        else      -> "Good evening"
    }
    val firstName = user.name.split(" ").firstOrNull() ?: "Topper"

    Column {
        Text(
            text     = "$greeting, $firstName",
            fontSize = 14.sp,
            color    = TextSecondary
        )
        Spacer(Modifier.height(2.dp))
        Row {
            Text(
                text       = "Study smarter, ",
                fontSize   = 20.sp,
                fontWeight = FontWeight.Bold,
                color      = TextPrimary
            )
            Text(
                text       = "reach the top",
                fontSize   = 20.sp,
                fontWeight = FontWeight.Bold,
                color      = AccentCyan
            )
        }
    }
}

// ── Hero stats card ────────────────────────────────────────────────────────────
@Composable
private fun HeroStatsCard(user: User, rank: Int) {
    NtfGradientCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // User row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(SurfaceCard)
                            .border(2.dp, AccentCyan.copy(0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (user.photoURL.isNotEmpty()) {
                            AsyncImage(
                                model              = user.photoURL,
                                contentDescription = null,
                                contentScale       = ContentScale.Crop,
                                modifier           = Modifier.fillMaxSize().clip(CircleShape)
                            )
                        } else {
                            Text(
                                user.name.firstOrNull()?.uppercaseChar()?.toString() ?: "U",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(user.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                        Text(user.email, fontSize = 11.sp, color = TextMuted)
                    }
                }
                Text(
                    text = "View Profile",
                    fontSize = 11.sp,
                    color = AccentCyan,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Stats chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                HomeStatChip(value = "${user.xp}", label = "XP Earned",    icon = "⚡", color = AccentCyan)
                HomeStatChip(value = "${user.streak}", label = "Day Streak", icon = "🔥", color = PremiumGold)
                HomeStatChip(value = "#$rank",  label = "Global Rank",    icon = "🏆", color = AccentBlue)
            }
        }
    }
}

@Composable
private fun HomeStatChip(value: String, label: String, icon: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(icon, fontSize = 13.sp)
            Spacer(Modifier.width(3.dp))
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = color)
        }
        Text(label, fontSize = 10.sp, color = TextMuted)
    }
}

// ── Quick actions grid ─────────────────────────────────────────────────────────
@Composable
private fun QuickActionsGrid(
    onNotes: () -> Unit,
    onLectures: () -> Unit,
    onTests: () -> Unit,
    onChats: () -> Unit,
    onLeaderboard: () -> Unit,
    onPremium: () -> Unit
) {
    val items = listOf(
        QuickAction("Notes",       Icons.Rounded.AutoStories,      AccentCyan,    onNotes),
        QuickAction("Lectures",    Icons.Rounded.PlayCircle,        AccentEmerald, onLectures),
        QuickAction("Tests",       Icons.Rounded.Quiz,              PremiumGold,   onTests),
        QuickAction("Community",   Icons.Rounded.Chat,              AccentIndigo,  onChats),
        QuickAction("Leaderboard", Icons.Rounded.EmojiEvents,       AccentViolet,  onLeaderboard),
        QuickAction("Premium",     Icons.Rounded.WorkspacePremium,  PremiumGold,   onPremium)
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        items.forEach { item ->
            QuickActionButton(item)
        }
    }
}

private data class QuickAction(
    val label: String,
    val icon: ImageVector,
    val color: Color,
    val onClick: () -> Unit
)

@Composable
private fun QuickActionButton(item: QuickAction) {
    Column(
        modifier = Modifier
            .clickable(onClick = item.onClick)
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .background(item.color.copy(0.12f), RoundedCornerShape(14.dp))
                .border(1.dp, item.color.copy(0.3f), RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(item.icon, null, tint = item.color, modifier = Modifier.size(24.dp))
        }
        Text(item.label, fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
    }
}

// ── Leaderboard preview ────────────────────────────────────────────────────────
@Composable
private fun LeaderboardPreviewSection(
    entries: List<LeaderboardEntry>,
    onSeeAll: () -> Unit,
    currentUid: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Top Learners", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
            Text("View All →", fontSize = 12.sp, color = AccentCyan,
                modifier = Modifier.clickable(onClick = onSeeAll))
        }
        NtfCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                entries.take(3).forEachIndexed { idx, entry ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val medals = listOf("🥇", "🥈", "🥉")
                        Text(medals[idx], fontSize = 18.sp)
                        Spacer(Modifier.width(10.dp))
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(AccentCyan.copy(0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                entry.name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            val isYou = entry.uid == currentUid
                            Row {
                                Text(
                                    entry.name,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                if (isYou) {
                                    Spacer(Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .background(AccentCyan.copy(0.2f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 5.dp, vertical = 1.dp)
                                    ) {
                                        Text("You", fontSize = 9.sp, color = AccentCyan, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            Text("Lv. ${entry.level}", fontSize = 10.sp, color = TextMuted)
                        }
                        Text(
                            "${entry.xp} XP",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = PremiumGold
                        )
                    }
                }
            }
        }
    }
}

// ── Motivational card (unused but referenced, kept) ────────────────────────────
@Composable
private fun MotivationalCard(user: User) {
    val xpToNext = LevelUtils.xpForNextLevel(user.level) - user.xp
    NtfCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("🎯", fontSize = 22.sp)
            Spacer(Modifier.width(12.dp))
            Column {
                Text("Keep it up!", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Text(
                    "$xpToNext XP to reach Level ${user.level + 1}",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }
    }
}
