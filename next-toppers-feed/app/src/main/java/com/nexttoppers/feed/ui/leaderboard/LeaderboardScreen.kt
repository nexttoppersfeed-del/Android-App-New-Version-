package com.nexttoppers.feed.ui.leaderboard

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.nexttoppers.feed.data.model.LeaderboardEntry
import com.nexttoppers.feed.ui.components.NeonDivider
import com.nexttoppers.feed.ui.components.SkeletonCard
import com.nexttoppers.feed.ui.theme.BackgroundBlack
import com.nexttoppers.feed.ui.theme.NeonCyan
import com.nexttoppers.feed.ui.theme.NeonGreen
import com.nexttoppers.feed.ui.theme.PremiumGold
import com.nexttoppers.feed.ui.theme.SurfaceCard
import com.nexttoppers.feed.ui.theme.SurfaceElevated
import com.nexttoppers.feed.ui.theme.TextMuted
import com.nexttoppers.feed.ui.theme.TextPrimary
import com.nexttoppers.feed.ui.theme.TextSecondary
import com.nexttoppers.feed.util.LevelUtils

@Composable
fun LeaderboardScreen(viewModel: LeaderboardViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundBlack)
    ) {
        // Ambient glow
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(NeonCyan.copy(0.06f), Color.Transparent),
                        radius = 700f
                    )
                )
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // ── Header ────────────────────────────────────────────────────────
            item {
                Spacer(Modifier.height(52.dp))
                LeaderboardHeader()
                Spacer(Modifier.height(16.dp))
            }

            // ── Tab row ───────────────────────────────────────────────────────
            item {
                LeaderboardTabs(
                    activeTab = state.activeTab,
                    onSelect  = viewModel::selectTab
                )
                Spacer(Modifier.height(20.dp))
            }

            if (state.isLoading) {
                item {
                    Column(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        repeat(5) {
                            SkeletonCard(height = 72.dp)
                        }
                    }
                }
                return@LazyColumn
            }

            // Placeholder tabs
            if (state.activeTab == LeaderboardTab.FRIENDS || state.activeTab == LeaderboardTab.SUBJECT) {
                item {
                    PlaceholderTabContent(
                        title    = if (state.activeTab == LeaderboardTab.FRIENDS) "Friends Leaderboard" else "Subject Leaderboard",
                        subtitle = "Coming soon in the next update!"
                    )
                }
                return@LazyColumn
            }

            if (state.entries.isEmpty()) {
                item {
                    Box(
                        Modifier.fillMaxWidth().padding(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No data yet", color = TextSecondary, fontSize = 15.sp)
                    }
                }
                return@LazyColumn
            }

            // ── Top 3 podium ──────────────────────────────────────────────────
            item {
                val top3 = state.entries.take(3)
                if (top3.size >= 1) {
                    PodiumSection(top3 = top3, currentUid = state.currentUid)
                    Spacer(Modifier.height(24.dp))
                }
            }

            // ── User's own rank bubble (if not in top 3) ─────────────────────
            if (state.userRank > 3) {
                item {
                    Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                        YourRankBubble(rank = state.userRank)
                    }
                    Spacer(Modifier.height(12.dp))
                    NeonDivider(Modifier.padding(horizontal = 20.dp))
                    Spacer(Modifier.height(12.dp))
                }
            }

            // ── Full list starting from rank 4 ────────────────────────────────
            val listEntries = if (state.entries.size > 3) state.entries.drop(3) else emptyList()
            itemsIndexed(listEntries) { _, entry ->
                LeaderboardRow(
                    entry      = entry,
                    isCurrentUser = entry.uid == state.currentUid
                )
            }
        }
    }
}

// ── Header ─────────────────────────────────────────────────────────────────────
@Composable
private fun LeaderboardHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "🏆 Leaderboard",
            style = TextStyle(
                fontSize   = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                brush      = Brush.linearGradient(listOf(NeonGreen, NeonCyan)),
                shadow     = Shadow(NeonGreen.copy(0.4f), Offset.Zero, 18f)
            )
        )
        Spacer(Modifier.height(4.dp))
        Text("Top Toppers — compete, climb, conquer", color = TextSecondary, fontSize = 13.sp)
    }
}

// ── Tab row ────────────────────────────────────────────────────────────────────
@Composable
private fun LeaderboardTabs(
    activeTab: LeaderboardTab,
    onSelect: (LeaderboardTab) -> Unit
) {
    val tabs = listOf("Global", "Weekly", "Friends", "Subject")
    val selectedIdx = LeaderboardTab.values().indexOf(activeTab)

    TabRow(
        selectedTabIndex  = selectedIdx,
        containerColor    = SurfaceCard,
        contentColor      = NeonGreen,
        indicator         = { tabPositions ->
            Box(
                modifier = Modifier
                    .tabIndicatorOffset(tabPositions[selectedIdx])
                    .height(2.dp)
                    .background(
                        Brush.horizontalGradient(listOf(NeonGreen, NeonCyan)),
                        RoundedCornerShape(1.dp)
                    )
            )
        },
        divider = {}
    ) {
        tabs.forEachIndexed { idx, label ->
            val selected = idx == selectedIdx
            Tab(
                selected = selected,
                onClick  = { onSelect(LeaderboardTab.values()[idx]) },
                modifier = Modifier.height(44.dp)
            ) {
                Text(
                    label,
                    color      = if (selected) NeonGreen else TextMuted,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    fontSize   = 13.sp
                )
            }
        }
    }
}

// ── Podium ─────────────────────────────────────────────────────────────────────
@Composable
private fun PodiumSection(top3: List<LeaderboardEntry>, currentUid: String) {
    val first  = top3.getOrNull(0)
    val second = top3.getOrNull(1)
    val third  = top3.getOrNull(2)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment     = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 2nd place
        if (second != null) {
            PodiumSlot(
                entry       = second,
                podiumHeight= 90.dp,
                medalColor  = Color(0xFFADB5BD),
                medalEmoji  = "🥈",
                isCurrentUser = second.uid == currentUid,
                modifier    = Modifier.weight(1f)
            )
        } else {
            Spacer(Modifier.weight(1f))
        }

        // 1st place — tallest
        if (first != null) {
            PodiumSlot(
                entry       = first,
                podiumHeight= 120.dp,
                medalColor  = PremiumGold,
                medalEmoji  = "🥇",
                isCurrentUser = first.uid == currentUid,
                modifier    = Modifier.weight(1f)
            )
        } else {
            Spacer(Modifier.weight(1f))
        }

        // 3rd place
        if (third != null) {
            PodiumSlot(
                entry       = third,
                podiumHeight= 70.dp,
                medalColor  = Color(0xFFCD7F32),
                medalEmoji  = "🥉",
                isCurrentUser = third.uid == currentUid,
                modifier    = Modifier.weight(1f)
            )
        } else {
            Spacer(Modifier.weight(1f))
        }
    }
}

@Composable
private fun PodiumSlot(
    entry: LeaderboardEntry,
    podiumHeight: androidx.compose.ui.unit.Dp,
    medalColor: Color,
    medalEmoji: String,
    isCurrentUser: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier            = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        // Medal badge
        Text(medalEmoji, fontSize = 20.sp)
        Spacer(Modifier.height(4.dp))

        // Avatar
        Box(
            modifier = Modifier
                .size(56.dp)
                .border(
                    2.5.dp,
                    if (isCurrentUser)
                        Brush.linearGradient(listOf(NeonGreen, NeonCyan))
                    else Brush.linearGradient(listOf(medalColor, medalColor.copy(0.6f))),
                    CircleShape
                )
                .padding(2.dp)
                .clip(CircleShape)
                .background(SurfaceElevated),
            contentAlignment = Alignment.Center
        ) {
            if (entry.photoUrl.isNotEmpty()) {
                AsyncImage(
                    model = entry.photoUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clip(CircleShape)
                )
            } else {
                Text(
                    entry.name.take(1).uppercase(),
                    color      = medalColor,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize   = 22.sp
                )
            }
        }

        Spacer(Modifier.height(6.dp))
        Text(
            entry.name.split(" ").first(),
            color      = if (isCurrentUser) NeonGreen else TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize   = 12.sp,
            maxLines   = 1,
            overflow   = TextOverflow.Ellipsis
        )
        Text(
            "${entry.xp} XP",
            color    = medalColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
        if (entry.premium) {
            Spacer(Modifier.height(2.dp))
            Text("⭐", fontSize = 10.sp)
        }

        // Podium block
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(podiumHeight)
                .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            medalColor.copy(0.25f),
                            medalColor.copy(0.10f)
                        )
                    )
                )
                .border(
                    1.dp,
                    Brush.verticalGradient(listOf(medalColor.copy(0.5f), Color.Transparent)),
                    RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "#${entry.rank}",
                color      = medalColor,
                fontWeight = FontWeight.ExtraBold,
                fontSize   = 18.sp
            )
        }
    }
}

// ── Your rank bubble ───────────────────────────────────────────────────────────
@Composable
private fun YourRankBubble(rank: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(NeonGreen.copy(0.08f))
            .border(1.dp, Brush.horizontalGradient(listOf(NeonGreen.copy(0.5f), NeonCyan.copy(0.5f))), RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Star, null, tint = NeonGreen, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                "You are ranked ",
                color    = TextSecondary,
                fontSize = 13.sp
            )
            Text(
                "#$rank",
                color      = NeonGreen,
                fontWeight = FontWeight.ExtraBold,
                fontSize   = 13.sp
            )
            Text(" globally", color = TextSecondary, fontSize = 13.sp)
        }
    }
}

// ── Full leaderboard row ───────────────────────────────────────────────────────
@Composable
private fun LeaderboardRow(entry: LeaderboardEntry, isCurrentUser: Boolean) {
    var triggered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { triggered = true }
    val alpha by animateFloatAsState(
        targetValue   = if (triggered) 1f else 0f,
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label         = "rowFade"
    )

    val rankColor = when (entry.rank) {
        1    -> PremiumGold
        2    -> Color(0xFFADB5BD)
        3    -> Color(0xFFCD7F32)
        else -> if (isCurrentUser) NeonGreen else TextMuted
    }

    Box(
        modifier = Modifier
            .alpha(alpha)
            .padding(horizontal = 20.dp, vertical = 5.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isCurrentUser) NeonGreen.copy(0.07f) else SurfaceCard
            )
            .border(
                1.dp,
                if (isCurrentUser)
                    Brush.horizontalGradient(listOf(NeonGreen.copy(0.5f), NeonCyan.copy(0.5f)))
                else Brush.horizontalGradient(listOf(SurfaceElevated, SurfaceElevated)),
                RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Rank badge
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(rankColor.copy(0.15f), RoundedCornerShape(10.dp))
                    .border(1.dp, rankColor.copy(0.3f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (entry.rank <= 3) listOf("🥇", "🥈", "🥉")[entry.rank - 1]
                    else "#${entry.rank}",
                    color      = rankColor,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize   = if (entry.rank <= 3) 14.sp else 11.sp
                )
            }

            Spacer(Modifier.width(10.dp))

            // Avatar
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .border(1.5.dp, rankColor.copy(0.4f), CircleShape)
                    .padding(1.5.dp)
                    .clip(CircleShape)
                    .background(SurfaceElevated),
                contentAlignment = Alignment.Center
            ) {
                if (entry.photoUrl.isNotEmpty()) {
                    AsyncImage(
                        model        = entry.photoUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier     = Modifier.fillMaxSize().clip(CircleShape)
                    )
                } else {
                    Text(
                        entry.name.take(1).uppercase(),
                        color      = rankColor,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize   = 16.sp
                    )
                }
            }

            Spacer(Modifier.width(10.dp))

            // Name + level
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        entry.name,
                        color      = if (isCurrentUser) NeonGreen else TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize   = 13.sp,
                        maxLines   = 1,
                        overflow   = TextOverflow.Ellipsis,
                        modifier   = Modifier.weight(1f, fill = false)
                    )
                    if (entry.premium) {
                        Spacer(Modifier.width(4.dp))
                        Icon(Icons.Rounded.WorkspacePremium, null, tint = PremiumGold, modifier = Modifier.size(13.dp))
                    }
                    if (isCurrentUser) {
                        Spacer(Modifier.width(4.dp))
                        Box(
                            Modifier
                                .background(NeonGreen.copy(0.15f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text("YOU", color = NeonGreen, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        "Lv ${entry.level}",
                        color    = NeonCyan.copy(0.8f),
                        fontSize = 11.sp
                    )
                    if (entry.streak > 0) {
                        Icon(Icons.Rounded.LocalFireDepartment, null, tint = Color(0xFFFF6B35), modifier = Modifier.size(11.dp))
                        Text("${entry.streak}d", color = Color(0xFFFF6B35), fontSize = 11.sp)
                    }
                }
            }

            // XP
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${entry.xp}",
                    color      = rankColor,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize   = 15.sp
                )
                Text("XP", color = TextMuted, fontSize = 10.sp)
            }
        }
    }
}

// ── Placeholder for coming-soon tabs ──────────────────────────────────────────
@Composable
private fun PlaceholderTabContent(title: String, subtitle: String) {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(48.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("🔒", fontSize = 40.sp)
            Text(
                title,
                color      = NeonCyan,
                fontWeight = FontWeight.Bold,
                fontSize   = 18.sp,
                textAlign  = TextAlign.Center
            )
            Text(
                subtitle,
                color     = TextSecondary,
                fontSize  = 13.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
