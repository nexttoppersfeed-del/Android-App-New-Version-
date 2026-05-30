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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowForwardIos
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.nexttoppers.feed.data.model.LeaderboardEntry
import com.nexttoppers.feed.ui.components.SkeletonCard
import com.nexttoppers.feed.ui.theme.AccentBlue
import com.nexttoppers.feed.ui.theme.AccentCyan
import com.nexttoppers.feed.ui.theme.AccentIndigo
import com.nexttoppers.feed.ui.theme.AccentViolet
import com.nexttoppers.feed.ui.theme.BackgroundBlack
import com.nexttoppers.feed.ui.theme.NeonGreen
import com.nexttoppers.feed.ui.theme.PremiumGold
import com.nexttoppers.feed.ui.theme.SurfaceCard
import com.nexttoppers.feed.ui.theme.SurfaceElevated
import com.nexttoppers.feed.ui.theme.TextMuted
import com.nexttoppers.feed.ui.theme.TextPrimary
import com.nexttoppers.feed.ui.theme.TextSecondary
import com.nexttoppers.feed.util.LevelUtils
import kotlinx.coroutines.launch

@Composable
fun LeaderboardScreen(viewModel: LeaderboardViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var searchQuery by remember { mutableStateOf("") }

    val filteredEntries = if (searchQuery.isBlank()) state.entries
    else state.entries.filter { it.name.contains(searchQuery, ignoreCase = true) }

    val tabLabels = LeaderboardTab.values().map { it.name.lowercase().replaceFirstChar { c -> c.uppercaseChar() } }

    // Calculate the LazyColumn index of the user's row so we can scroll to it.
    // Layout order: header(0), rankStrip(1 if shown), tabs(2), loading/entries...
    // Entries start after: header + rankStrip + tabs + searchBar + podium (if shown) + listHeader
    // We do a best-effort calculation.
    val userScrollIndex = remember(state.entries, state.currentUid, state.userRank) {
        if (state.userRank <= 0 || state.entries.isEmpty()) return@remember -1
        val hasRankStrip = state.userRank > 0
        val hasPodium = state.entries.size >= 3 && searchQuery.isBlank()
        // Fixed header items: header(1) + rankStrip(1?) + tabs(1) + searchBar(1) + podium(1?) + listHeader(1)
        val fixedCount = 1 + (if (hasRankStrip) 1 else 0) + 1 + 1 + (if (hasPodium) 1 else 0) + 1
        val listEntries = if (searchQuery.isBlank()) filteredEntries.drop(3) else filteredEntries
        val idxInList = listEntries.indexOfFirst { it.uid == state.currentUid }
        if (idxInList < 0) -1 else fixedCount + idxInList
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundBlack)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // ── Header ─────────────────────────────────────────────────────────
            item {
                LeaderboardHeader(state)
            }

            // ── Your ranking strip ─────────────────────────────────────────────
            if (state.userRank > 0) {
                item {
                    YourRankStrip(
                        rank       = state.userRank,
                        entries    = state.entries,
                        currentUid = state.currentUid,
                        onJumpToRank = {
                            if (userScrollIndex >= 0) {
                                scope.launch { listState.animateScrollToItem(userScrollIndex) }
                            }
                        }
                    )
                }
            }

            // ── Tabs ───────────────────────────────────────────────────────────
            item {
                TabRow(
                    selectedTabIndex = state.activeTab.ordinal,
                    modifier         = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceCard),
                    containerColor   = Color.Transparent,
                    indicator        = {},
                    divider          = {}
                ) {
                    LeaderboardTab.values().forEachIndexed { idx, tab ->
                        val selected = state.activeTab == tab
                        Tab(
                            selected = selected,
                            onClick  = { viewModel.selectTab(tab) },
                            modifier = Modifier
                                .padding(4.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selected) AccentCyan else Color.Transparent)
                        ) {
                            Text(
                                text       = tabLabels[idx],
                                fontSize   = 12.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                color      = if (selected) Color.White else TextSecondary,
                                modifier   = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            // ── Loading ────────────────────────────────────────────────────────
            if (state.isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AccentCyan, modifier = Modifier.size(28.dp))
                    }
                }
                return@LazyColumn
            }

            // ── Placeholder for locked tabs ────────────────────────────────────
            if (state.activeTab == LeaderboardTab.FRIENDS || state.activeTab == LeaderboardTab.SUBJECT) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("🔒", fontSize = 40.sp)
                        Text("Coming Soon", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("This feature is being built.", color = TextSecondary, fontSize = 13.sp)
                    }
                }
                return@LazyColumn
            }

            // ── Search bar ─────────────────────────────────────────────────────
            item {
                OutlinedTextField(
                    value         = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder   = { Text("Search by name...", color = TextMuted, fontSize = 14.sp) },
                    leadingIcon   = { Icon(Icons.Rounded.Search, null, tint = TextMuted, modifier = Modifier.size(18.dp)) },
                    modifier      = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 16.dp),
                    shape  = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor   = SurfaceCard,
                        unfocusedContainerColor = SurfaceCard,
                        focusedBorderColor      = AccentCyan.copy(0.6f),
                        unfocusedBorderColor    = SurfaceElevated,
                        focusedTextColor        = TextPrimary,
                        unfocusedTextColor      = TextPrimary
                    ),
                    singleLine = true
                )
            }

            // ── Podium (top 3) ─────────────────────────────────────────────────
            if (filteredEntries.size >= 3 && searchQuery.isBlank()) {
                item {
                    PodiumSection(
                        entries    = filteredEntries.take(3),
                        currentUid = state.currentUid
                    )
                    Spacer(Modifier.height(20.dp))
                }
            }

            // ── List header ────────────────────────────────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("RANK", fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.SemiBold)
                    Text("STUDENT", fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f).padding(start = 16.dp))
                    Text("XP", fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.SemiBold)
                }
            }

            // ── List items (4+) ────────────────────────────────────────────────
            val listEntries = if (searchQuery.isBlank()) filteredEntries.drop(3) else filteredEntries
            itemsIndexed(listEntries) { idx, entry ->
                val rank = if (searchQuery.isBlank()) idx + 4 else (entry.rank.takeIf { it > 0 } ?: idx + 1)
                val isCurrentUser = entry.uid == state.currentUid
                LeaderboardRow(
                    entry         = entry,
                    rank          = rank,
                    isCurrentUser = isCurrentUser
                )
            }

            if (filteredEntries.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No results found", color = TextMuted, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun LeaderboardHeader(state: LeaderboardUiState) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(AccentCyan.copy(0.07f), Color.Transparent)
                )
            )
            .padding(horizontal = 20.dp)
            .padding(top = 56.dp, bottom = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "Leaderboard",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )
                Text(
                    "Top performers, ranked by XP & quiz mastery",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
                Spacer(Modifier.height(12.dp))
                // Stats row
                Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    LeaderboardStat(
                        value = "${state.entries.size}",
                        label = "Students",
                        icon  = "👥"
                    )
                    val avgXp = if (state.entries.isEmpty()) 0L
                    else state.entries.sumOf { it.xp } / state.entries.size
                    LeaderboardStat(value = "${avgXp}", label = "Avg XP", icon = "⚡")
                    LeaderboardStat(value = "#${state.userRank}", label = "Your Rank", icon = "🎯")
                }
            }
            Text("🏆", fontSize = 56.sp)
        }
    }
}

@Composable
private fun LeaderboardStat(value: String, label: String, icon: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(icon, fontSize = 11.sp)
            Spacer(Modifier.width(3.dp))
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
        }
        Text(label, fontSize = 10.sp, color = TextMuted)
    }
}

@Composable
private fun YourRankStrip(
    rank: Int,
    entries: List<LeaderboardEntry>,
    currentUid: String,
    onJumpToRank: () -> Unit = {}
) {
    val myXp = entries.firstOrNull { it.uid == currentUid }?.xp ?: 0L
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 12.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceCard)
            .border(1.dp, AccentCyan.copy(0.3f), RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text("Your ranking", fontSize = 11.sp, color = TextMuted)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "#$rank of ${entries.size}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .background(AccentCyan.copy(0.15f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        "${myXp / 1000.0}K XP",
                        fontSize = 11.sp,
                        color = AccentCyan,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(AccentCyan.copy(0.12f))
                .clickable(onClick = onJumpToRank)
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Rounded.EmojiEvents, null, tint = AccentCyan, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            Text("Jump to my rank", fontSize = 11.sp, color = AccentCyan, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.width(2.dp))
            Icon(Icons.Rounded.ArrowForwardIos, null, tint = AccentCyan, modifier = Modifier.size(10.dp))
        }
    }
}

@Composable
private fun PodiumSection(entries: List<LeaderboardEntry>, currentUid: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceCard)
            .padding(top = 20.dp, bottom = 0.dp, start = 16.dp, end = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Silver — #2 (left)
            PodiumSlot(entry = entries[1], rank = 2, podiumHeight = 80.dp,  medalColor = Color(0xFFC0C0C0), isCurrentUser = entries[1].uid == currentUid)
            // Gold — #1 (center/elevated)
            PodiumSlot(entry = entries[0], rank = 1, podiumHeight = 110.dp, medalColor = PremiumGold,       isCurrentUser = entries[0].uid == currentUid)
            // Bronze — #3 (right)
            PodiumSlot(entry = entries[2], rank = 3, podiumHeight = 60.dp,  medalColor = Color(0xFFCD7F32), isCurrentUser = entries[2].uid == currentUid)
        }
    }
}

@Composable
private fun PodiumSlot(
    entry: LeaderboardEntry,
    rank: Int,
    podiumHeight: Dp,
    medalColor: Color,
    isCurrentUser: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
        modifier = Modifier.width(100.dp)
    ) {
        // Crown for #1
        if (rank == 1) {
            Text("👑", fontSize = 20.sp)
        }

        // Avatar
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(52.dp)) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(SurfaceElevated)
                    .border(2.dp, medalColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (entry.photoUrl.isNotEmpty()) {
                    AsyncImage(
                        model = entry.photoUrl, contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().clip(CircleShape)
                    )
                } else {
                    Text(
                        entry.name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                        fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary
                    )
                }
            }
            // Medal badge
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .align(Alignment.TopEnd)
                    .background(medalColor, CircleShape)
                    .border(1.5.dp, BackgroundBlack, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when(rank) { 1 -> "1"; 2 -> "2"; else -> "3" },
                    fontSize = 8.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (rank == 1) Color(0xFF7C5A00) else Color.White
                )
            }
        }

        Spacer(Modifier.height(4.dp))
        Text(
            entry.name.split(" ").firstOrNull() ?: entry.name,
            fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary,
            maxLines = 1, overflow = TextOverflow.Ellipsis
        )
        Box(
            modifier = Modifier
                .background(medalColor.copy(0.15f), RoundedCornerShape(4.dp))
                .padding(horizontal = 5.dp, vertical = 1.dp)
        ) {
            Text("LVL ${entry.level}", fontSize = 9.sp, color = medalColor, fontWeight = FontWeight.Bold)
        }
        Text(
            "${entry.xp / 1000.0}K XP",
            fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = medalColor
        )
        Spacer(Modifier.height(6.dp))
        // Podium block
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(podiumHeight)
                .background(
                    Brush.verticalGradient(listOf(medalColor.copy(0.3f), medalColor.copy(0.1f))),
                    RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "#$rank",
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = medalColor
            )
        }
    }
}

@Composable
private fun LeaderboardRow(
    entry: LeaderboardEntry,
    rank: Int,
    isCurrentUser: Boolean
) {
    val alpha by animateFloatAsState(
        targetValue   = 1f,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label         = "fade"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isCurrentUser) AccentCyan.copy(0.08f) else SurfaceCard
            )
            .border(
                1.dp,
                if (isCurrentUser) AccentCyan.copy(0.4f) else Color.Transparent,
                RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Rank badge
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(
                    when (rank) {
                        1 -> PremiumGold.copy(0.2f)
                        2 -> Color(0xFFC0C0C0).copy(0.2f)
                        3 -> Color(0xFFCD7F32).copy(0.2f)
                        else -> SurfaceElevated
                    },
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "$rank",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = when (rank) {
                    1 -> PremiumGold
                    2 -> Color(0xFFC0C0C0)
                    3 -> Color(0xFFCD7F32)
                    else -> TextSecondary
                }
            )
        }

        Spacer(Modifier.width(10.dp))

        // Avatar
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(AccentCyan.copy(0.2f)),
            contentAlignment = Alignment.Center
        ) {
            if (entry.photoUrl.isNotEmpty()) {
                AsyncImage(
                    model = entry.photoUrl, contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clip(CircleShape)
                )
            } else {
                Text(
                    entry.name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
        }

        Spacer(Modifier.width(10.dp))

        // Name + stats
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    entry.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (isCurrentUser) {
                    Spacer(Modifier.width(5.dp))
                    Box(
                        modifier = Modifier
                            .background(AccentCyan.copy(0.2f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text("You", fontSize = 9.sp, color = AccentCyan, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("LVL ${entry.level}", fontSize = 10.sp, color = TextMuted)
                if (entry.streak > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.LocalFireDepartment, null, tint = Color(0xFFF59E0B), modifier = Modifier.size(10.dp))
                        Text(" ${entry.streak}", fontSize = 10.sp, color = TextMuted)
                    }
                }
            }
        }

        // XP
        Column(horizontalAlignment = Alignment.End) {
            Text(
                "${entry.xp}",
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isCurrentUser) AccentCyan else TextPrimary
            )
            Text("XP", fontSize = 9.sp, color = TextMuted)
        }

        Spacer(Modifier.width(6.dp))
        Icon(Icons.Rounded.ArrowForwardIos, null, tint = TextMuted, modifier = Modifier.size(10.dp))
    }
}
