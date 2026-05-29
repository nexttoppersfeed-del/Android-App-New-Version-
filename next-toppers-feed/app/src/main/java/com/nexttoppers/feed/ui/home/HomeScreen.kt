package com.nexttoppers.feed.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Quiz
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.WorkspacePremium
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.nexttoppers.feed.data.model.LeaderboardEntry
import com.nexttoppers.feed.data.model.User
import com.nexttoppers.feed.ui.achievements.AchievementsMiniRow
import com.nexttoppers.feed.ui.announcements.AnnouncementsSection
import com.nexttoppers.feed.ui.announcements.AnnouncementCarouselWithViewModel
import com.nexttoppers.feed.ui.notifications.NotificationBellBadge
import com.nexttoppers.feed.ui.components.NtfCard
import com.nexttoppers.feed.ui.components.NtfGradientCard
import com.nexttoppers.feed.ui.components.PulsingDot
import com.nexttoppers.feed.ui.components.SectionHeader
import com.nexttoppers.feed.ui.components.SkeletonCard
import com.nexttoppers.feed.ui.components.SkeletonRow
import com.nexttoppers.feed.ui.components.XpBadge
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
import com.nexttoppers.feed.ui.premium.PremiumBannerCard
import com.nexttoppers.feed.data.model.toPremiumMembership
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
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState                  by viewModel.uiState.collectAsState()
    val userRank                 by viewModel.userRank.collectAsState()
    val topEntries               by viewModel.topEntries.collectAsState()
    val unreadNotificationCount  by viewModel.unreadNotificationCount.collectAsState()
    var visible    by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundBlack)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(NeonGreen.copy(0.07f), Color.Transparent),
                        center = Offset(Float.POSITIVE_INFINITY / 2, 0f),
                        radius = 600f
                    )
                )
        )

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
                        onNavigateToActivityFeed       = onNavigateToActivityFeed
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
        SkeletonCard(height = 140.dp)
        SkeletonCard(height = 100.dp)
        SkeletonCard(height = 100.dp)
        SkeletonCard(height = 200.dp)
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
    onNavigateToActivityFeed: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 52.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp)
    ) {
        TopBar(
            user                    = user,
            onSettingsClick         = onNavigateToSettings,
            unreadNotificationCount = unreadNotificationCount,
            onNotificationClick     = onNavigateToNotifications
        )
        GreetingSection(user)
        HeroStatsCard(user = user, rank = userRank)

        // ── XP Level progress ─────────────────────────────────────────────────
        GlowingLevelCard(xp = user.xp, modifier = Modifier.fillMaxWidth())

        // ── Streak + Rank row ─────────────────────────────────────────────────
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StreakCard(streak = user.streak, modifier = Modifier.weight(1f))
            RankCard(rank = userRank, xp = user.xp, modifier = Modifier.weight(1f))
        }

        // ── Premium banner ────────────────────────────────────────────────────
        PremiumBannerCard(
            isPremium     = user.isPremium,
            membership    = user.toPremiumMembership(),
            onUpgradeClick = onNavigateToPremium,
            modifier      = Modifier.fillMaxWidth()
        )

        // ── Quick actions ─────────────────────────────────────────────────────
        SectionHeader("Quick Access")
        QuickActionsGrid(
            onNotes       = onNavigateToNotes,
            onLectures    = onNavigateToLectures,
            onTests       = onNavigateToTests,
            onChats       = onNavigateToChats,
            onLeaderboard = onNavigateToLeaderboard,
            onPremium     = onNavigateToPremium
        )

        // ── Leaderboard preview ───────────────────────────────────────────────
        if (topEntries.isNotEmpty()) {
            LeaderboardPreviewSection(
                entries     = topEntries,
                onSeeAll    = onNavigateToLeaderboard,
                currentUid  = ""
            )
        }

        // ── Achievements mini row ─────────────────────────────────────────────
        SectionHeader("Achievements")
        NtfCard(modifier = Modifier.fillMaxWidth()) {
            AchievementsMiniRow(
                quizzesCompleted = user.quizzesCompleted,
                streak           = user.streak,
                resourcesOpened  = user.resourcesOpened,
                level            = user.level
            )
        }

        // ── Motivational message ──────────────────────────────────────────────
        MotivationalCard(user)

        // ── Announcement carousel (auto-scrolling, shows pinned + ticker) ────
        SectionHeader("Announcements")
        AnnouncementCarouselWithViewModel(
            onAnnouncementClick = onNavigateToAnnouncementDetail
        )

        // ── Full announcement list with click-to-detail ────────────────────
        AnnouncementsSection(onAnnouncementClick = onNavigateToAnnouncementDetail)
    }
}

// ── Top bar ────────────────────────────────────────────────────────────────────
@Composable
private fun TopBar(
    user: User,
    onSettingsClick: () -> Unit,
    unreadNotificationCount: Int = 0,
    onNotificationClick: () -> Unit = {}
) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            PulsingDot(NeonGreen)
            Spacer(Modifier.width(6.dp))
            Text("Live", color = NeonGreen, fontSize = 11.sp)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            NotificationBellBadge(
                unreadCount = unreadNotificationCount,
                onClick     = onNotificationClick
            )
            Spacer(Modifier.width(6.dp))
            XpBadge(xp = user.xp)
            Spacer(Modifier.width(10.dp))
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .border(1.5.dp, Brush.linearGradient(listOf(NeonGreen, NeonCyan)), CircleShape)
                    .padding(2.dp)
                    .clip(CircleShape)
                    .background(SurfaceElevated),
                contentAlignment = Alignment.Center
            ) {
                if (user.photoUrl.isNotEmpty()) {
                    AsyncImage(
                        model            = user.photoUrl,
                        contentDescription = "Avatar",
                        contentScale     = ContentScale.Crop,
                        modifier         = Modifier.fillMaxSize().clip(CircleShape)
                    )
                } else {
                    Text(user.name.take(1).uppercase(), color = NeonGreen, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
            Spacer(Modifier.width(4.dp))
            IconButton(onClick = onSettingsClick, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Rounded.Settings, null, tint = TextSecondary, modifier = Modifier.size(20.dp))
            }
        }
    }
}

// ── Greeting ───────────────────────────────────────────────────────────────────
@Composable
private fun GreetingSection(user: User) {
    val hour     = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when { hour < 12 -> "Good morning"; hour < 17 -> "Good afternoon"; else -> "Good evening" }
    val level    = LevelUtils.levelForXp(user.xp)
    Column {
        Text(greeting, color = TextMuted, fontSize = 13.sp)
        Spacer(Modifier.height(2.dp))
        Text(
            text  = user.name.split(" ").firstOrNull()?.let { "$it 👋" } ?: "Topper 👋",
            style = TextStyle(
                fontSize   = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                brush      = Brush.linearGradient(listOf(NeonGreen, NeonCyan)),
                shadow     = Shadow(NeonGreen.copy(0.35f), Offset.Zero, 16f)
            )
        )
        Text(
            motivationalMessage(level, user.streak),
            color    = TextSecondary,
            fontSize = 13.sp
        )
    }
}

private fun motivationalMessage(level: Int, streak: Int): String = when {
    streak >= 30 -> "🔥 Legendary streak — you're unstoppable!"
    streak >= 7  -> "🎯 7-day streak warrior — keep climbing!"
    level >= 10  -> "👑 Next Topper — you've reached Legend status!"
    level >= 5   -> "⚡ Expert level — the top 10 is in sight!"
    streak == 0  -> "🚀 Start your streak today — every day counts!"
    else         -> "📚 Ready to top the charts today?"
}

// ── Hero stats card ────────────────────────────────────────────────────────────
@Composable
private fun HeroStatsCard(user: User, rank: Int) {
    NtfGradientCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier          = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .border(2.dp, Brush.linearGradient(listOf(NeonGreen, NeonCyan)), CircleShape)
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(SurfaceElevated),
                    contentAlignment = Alignment.Center
                ) {
                    if (user.photoUrl.isNotEmpty()) {
                        AsyncImage(
                            model            = user.photoUrl,
                            contentDescription = null,
                            contentScale     = ContentScale.Crop,
                            modifier         = Modifier.fillMaxSize().clip(CircleShape)
                        )
                    } else {
                        Text(user.name.take(1).uppercase(), color = NeonGreen, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
                    }
                }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(user.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(user.email, color = TextMuted, fontSize = 11.sp)
                    if (user.isPremium) { Spacer(Modifier.height(4.dp)); PremiumTag() }
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatChipHome("XP",     "${user.xp}",   NeonGreen,             Modifier.weight(1f))
                StatChipHome("Streak", "${user.streak}d", Color(0xFFFF6B35),  Modifier.weight(1f))
                StatChipHome("Rank",   if (rank > 0) "#$rank" else "–", NeonCyan, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun StatChipHome(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(color.copy(0.08f))
            .border(1.dp, color.copy(0.25f), RoundedCornerShape(14.dp))
            .padding(horizontal = 8.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, color = color, fontWeight = FontWeight.Bold, fontSize = 17.sp)
        Spacer(Modifier.height(2.dp))
        Text(label, color = TextSecondary, fontSize = 11.sp)
    }
}

@Composable
private fun PremiumTag() {
    Box(
        modifier = Modifier
            .background(PremiumGold.copy(0.15f), RoundedCornerShape(6.dp))
            .border(1.dp, PremiumGold.copy(0.5f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text("⭐ PRO", color = PremiumGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

// ── Leaderboard preview section ────────────────────────────────────────────────
@Composable
private fun LeaderboardPreviewSection(
    entries: List<LeaderboardEntry>,
    onSeeAll: () -> Unit,
    currentUid: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            SectionHeader("Top Toppers")
            Text(
                "See All →",
                color      = NeonGreen,
                fontSize   = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier   = Modifier.clickable(onClick = onSeeAll)
            )
        }
        NtfCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                entries.take(3).forEachIndexed { idx, entry ->
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val medal = listOf("🥇", "🥈", "🥉").getOrElse(idx) { "#${idx + 1}" }
                        Text(medal, fontSize = 18.sp, modifier = Modifier.width(28.dp))

                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(SurfaceElevated),
                            contentAlignment = Alignment.Center
                        ) {
                            if (entry.photoUrl.isNotEmpty()) {
                                AsyncImage(
                                    model            = entry.photoUrl,
                                    contentDescription = null,
                                    contentScale     = ContentScale.Crop,
                                    modifier         = Modifier.fillMaxSize().clip(CircleShape)
                                )
                            } else {
                                Text(
                                    entry.name.take(1).uppercase(),
                                    color      = NeonGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize   = 14.sp
                                )
                            }
                        }

                        Column(Modifier.weight(1f)) {
                            Text(
                                entry.name,
                                color      = if (entry.uid == currentUid) NeonGreen else TextPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize   = 13.sp
                            )
                            Text("Lv ${entry.level}", color = NeonCyan.copy(0.8f), fontSize = 11.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("${entry.xp}", color = NeonGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("XP", color = TextMuted, fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}

// ── Motivational card ──────────────────────────────────────────────────────────
@Composable
private fun MotivationalCard(user: User) {
    val level = LevelUtils.levelForXp(user.xp)
    val xpToNext = LevelUtils.xpToNextLevel(user.xp)
    val msg = when {
        xpToNext <= 50  -> "⚡ Almost there! Only $xpToNext XP to Level ${level + 1}!"
        user.streak > 0 -> "🔥 ${user.streak}-day streak! Keep studying daily."
        else            -> "🚀 Earn XP by completing quizzes and opening resources!"
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.horizontalGradient(listOf(NeonCyan.copy(0.07f), NeonGreen.copy(0.05f)))
            )
            .border(1.dp, NeonCyan.copy(0.2f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Star, null, tint = NeonCyan, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Text(msg, color = NeonCyan.copy(0.9f), fontSize = 13.sp, lineHeight = 19.sp)
        }
    }
}

// ── Premium banners ────────────────────────────────────────────────────────────
@Composable
private fun PremiumBadgeCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(listOf(PremiumGold.copy(0.18f), NeonCyan.copy(0.1f))),
                RoundedCornerShape(20.dp)
            )
            .border(1.dp, PremiumGold.copy(0.5f), RoundedCornerShape(20.dp))
            .padding(18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.WorkspacePremium, null, tint = PremiumGold, modifier = Modifier.size(30.dp))
            Spacer(Modifier.width(14.dp))
            Column {
                Text("Premium Member", color = PremiumGold, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text("All features unlocked — enjoy!", color = TextSecondary, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun UpgradeToPremiumBanner(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.horizontalGradient(listOf(NeonGreen.copy(0.12f), NeonCyan.copy(0.08f))))
            .border(1.dp, NeonGreen.copy(0.3f), RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.WorkspacePremium, null, tint = NeonGreen, modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text("Upgrade to Premium", color = NeonGreen, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("Unlock all notes, lectures & quizzes", color = TextSecondary, fontSize = 12.sp)
            }
            Text("→", color = NeonGreen, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
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
    val actions = listOf(
        Triple("Notes",       Icons.Rounded.AutoStories,      onNotes),
        Triple("Lectures",    Icons.Rounded.PlayCircle,       onLectures),
        Triple("Tests",       Icons.Rounded.Quiz,             onTests),
        Triple("Chats",       Icons.Rounded.Chat,             onChats),
        Triple("Leaderboard", Icons.Rounded.EmojiEvents,      onLeaderboard),
        Triple("Premium",     Icons.Rounded.WorkspacePremium, onPremium)
    )
    val rows = actions.chunked(3)
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        rows.forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { (label, icon, action) ->
                    QuickActionButton(label, icon, action, Modifier.weight(1f))
                }
                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun QuickActionButton(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = NeonGreen
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(SurfaceCard)
            .border(1.dp, accentColor.copy(0.18f), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .background(accentColor.copy(0.12f), RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, label, tint = accentColor, modifier = Modifier.size(24.dp))
        }
        Text(label, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}
