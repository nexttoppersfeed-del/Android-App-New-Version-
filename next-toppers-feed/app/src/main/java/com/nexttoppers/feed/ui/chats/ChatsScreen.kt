package com.nexttoppers.feed.ui.chats

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Chat
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nexttoppers.feed.data.model.Chat
import com.nexttoppers.feed.data.model.ChatType
import com.nexttoppers.feed.ui.community.CommunityScreen
import com.nexttoppers.feed.ui.theme.BackgroundBlack
import com.nexttoppers.feed.ui.theme.NeonCyan
import com.nexttoppers.feed.ui.theme.NeonGreen
import com.nexttoppers.feed.ui.theme.PremiumGold
import com.nexttoppers.feed.ui.theme.SurfaceCard
import com.nexttoppers.feed.ui.theme.SurfaceElevated
import com.nexttoppers.feed.ui.theme.TextMuted
import com.nexttoppers.feed.ui.theme.TextPrimary
import com.nexttoppers.feed.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatsScreen(
    onNavigateToChat: (String) -> Unit = {},
    onNavigateToCreatePost: () -> Unit = {},
    onNavigateToPostDetail: (String) -> Unit = {},
    onNavigateToGroup: (String) -> Unit = {},
    viewModel: ChatListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        Triple("Chats",     Icons.Rounded.Chat,  null as Int?),
        Triple("Community", Icons.Rounded.Forum, null)
    )

    Column(
        modifier = Modifier.fillMaxSize().background(BackgroundBlack)
    ) {
        ChatsTopBar()

        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = BackgroundBlack,
            edgePadding = 16.dp,
            indicator = { tabPositions ->
                if (selectedTab < tabPositions.size) {
                    Box(
                        Modifier
                            .tabIndicatorOffset(tabPositions[selectedTab])
                            .height(3.dp)
                            .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                            .background(NeonGreen)
                    )
                }
            },
            divider = {}
        ) {
            tabs.forEachIndexed { index, (label, icon, _) ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    modifier = Modifier.padding(bottom = 2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 10.dp)
                    ) {
                        Icon(
                            icon, null,
                            tint = if (selectedTab == index) NeonGreen else TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            label,
                            color = if (selectedTab == index) NeonGreen else TextMuted,
                            fontSize = 13.sp,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        when (selectedTab) {
            0 -> ChatListContent(
                uiState = uiState,
                searchQuery = searchQuery,
                currentUid = viewModel.currentUid,
                onSearchChange = viewModel::setSearchQuery,
                onChatClick = onNavigateToChat
            )
            1 -> CommunityScreen(
                onNavigateToCreatePost = onNavigateToCreatePost,
                onNavigateToPostDetail = onNavigateToPostDetail
            )
        }
    }
}

@Composable
private fun ChatsTopBar() {
    val infiniteTransition = rememberInfiniteTransition(label = "top_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "glow"
    )
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "Connect",
                color = TextPrimary,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 26.sp
            )
            Text("Chats · Community", color = TextMuted, fontSize = 12.sp)
        }
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(Brush.radialGradient(listOf(NeonGreen.copy(alpha = glowAlpha * 0.2f), Color.Transparent)))
                .border(1.dp, NeonGreen.copy(alpha = glowAlpha * 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.Chat, null, tint = NeonGreen, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun ChatListContent(
    uiState: ChatListUiState,
    searchQuery: String,
    currentUid: String,
    onSearchChange: (String) -> Unit,
    onChatClick: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = { Text("Search chats...", color = TextMuted, fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Rounded.Search, null, tint = TextMuted, modifier = Modifier.size(18.dp)) },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonGreen,
                unfocusedBorderColor = SurfaceElevated,
                cursorColor = NeonGreen
            ),
            singleLine = true
        )

        when (uiState) {
            is ChatListUiState.Loading -> Box(
                Modifier.fillMaxSize(), contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(color = NeonGreen, strokeWidth = 2.dp) }

            is ChatListUiState.Error -> Box(
                Modifier.fillMaxSize(), contentAlignment = Alignment.Center
            ) { Text(uiState.message, color = TextSecondary, fontSize = 14.sp) }

            is ChatListUiState.Success -> {
                if (uiState.chats.isEmpty()) {
                    EmptyChatListState()
                } else {
                    LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
                        itemsIndexed(uiState.chats) { index, chat ->
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn(tween(200 + index * 50)) + expandVertically(tween(200 + index * 50))
                            ) {
                                ChatListItem(
                                    chat = chat,
                                    currentUid = currentUid,
                                    onClick = { onChatClick(chat.chatId) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatListItem(chat: Chat, currentUid: String, onClick: () -> Unit) {
    val displayName = chat.getDisplayName(currentUid)
    val unread = chat.getUnreadCount(currentUid)
    val isGroup = chat.type != ChatType.PRIVATE.name

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box {
            val avatarColor = if (isGroup) NeonCyan else NeonGreen
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(avatarColor.copy(alpha = 0.15f))
                    .border(1.5.dp, avatarColor.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isGroup) "👥" else displayName.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                    fontSize = if (isGroup) 22.sp else 18.sp,
                    color = avatarColor,
                    fontWeight = FontWeight.Bold
                )
            }
            if (chat.pinned) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .align(Alignment.TopEnd)
                        .clip(CircleShape)
                        .background(PremiumGold),
                    contentAlignment = Alignment.Center
                ) { Text("📌", fontSize = 8.sp) }
            }
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    displayName,
                    color = TextPrimary,
                    fontWeight = if (unread > 0) FontWeight.Bold else FontWeight.SemiBold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    formatChatTime(chat.lastMessageTime.toDate()),
                    color = if (unread > 0) NeonGreen else TextMuted,
                    fontSize = 11.sp
                )
            }
            Spacer(Modifier.height(3.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    chat.lastMessage.ifBlank { "No messages yet" },
                    color = if (unread > 0) TextSecondary else TextMuted,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (unread > 0) {
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(NeonGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            if (unread > 99) "99+" else "$unread",
                            color = BackgroundBlack,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).padding(start = 80.dp).background(SurfaceElevated))
}

@Composable
private fun EmptyChatListState() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("💬", fontSize = 52.sp)
            Spacer(Modifier.height(16.dp))
            Text("No chats yet", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Spacer(Modifier.height(8.dp))
            Text(
                "Start a conversation from a user's profile\nor join a group to get started",
                color = TextSecondary,
                fontSize = 13.sp,
                lineHeight = 20.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

private fun formatChatTime(date: Date): String {
    val now = System.currentTimeMillis()
    val diff = now - date.time
    return when {
        diff < 60_000     -> "now"
        diff < 3_600_000  -> "${diff / 60_000}m"
        diff < 86_400_000 -> "${diff / 3_600_000}h"
        diff < 604_800_000 -> SimpleDateFormat("EEE", Locale.getDefault()).format(date)
        else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(date)
    }
}
