package com.nexttoppers.feed.ui.chats

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
import androidx.compose.material.icons.rounded.Chat
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.nexttoppers.feed.data.model.Chat
import com.nexttoppers.feed.data.model.ChatType
import com.nexttoppers.feed.ui.community.CommunityScreen
import com.nexttoppers.feed.ui.theme.AccentCyan
import com.nexttoppers.feed.ui.theme.AccentEmerald
import com.nexttoppers.feed.ui.theme.AccentIndigo
import com.nexttoppers.feed.ui.theme.BackgroundBlack
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
    val uiState     by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val currentUid  = viewModel.currentUid
    var selectedTab by remember { mutableIntStateOf(0) }

    Column(modifier = Modifier.fillMaxSize().background(BackgroundBlack)) {

        // ── Top bar ───────────────────────────────────────────────────────────
        ConnectTopBar(selectedTab = selectedTab)

        // ── Tabs ──────────────────────────────────────────────────────────────
        ConnectTabs(
            selectedTab  = selectedTab,
            onTabSelected = { selectedTab = it }
        )

        // ── Content ───────────────────────────────────────────────────────────
        when (selectedTab) {
            0 -> ChatListContent(
                uiState        = uiState,
                searchQuery    = searchQuery,
                currentUid     = currentUid,
                onSearchChange = viewModel::setSearchQuery,
                onChatClick    = onNavigateToChat,
                onGroupClick   = onNavigateToGroup
            )
            1 -> CommunityScreen(
                onNavigateToCreatePost = onNavigateToCreatePost,
                onNavigateToPostDetail = onNavigateToPostDetail
            )
        }
    }
}

@Composable
private fun ConnectTopBar(selectedTab: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                "Connect",
                color      = TextPrimary,
                fontWeight = FontWeight.ExtraBold,
                fontSize   = 26.sp
            )
            Text(
                if (selectedTab == 0) "Your conversations" else "Community chat room",
                color    = TextMuted,
                fontSize = 12.sp
            )
        }
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(AccentEmerald.copy(0.12f), RoundedCornerShape(12.dp))
                .border(1.dp, AccentEmerald.copy(0.3f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                if (selectedTab == 0) Icons.Rounded.Chat else Icons.Rounded.Forum,
                null,
                tint     = AccentEmerald,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun ConnectTabs(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    val tabItems = listOf(
        Pair("Chats",     Icons.Rounded.Chat),
        Pair("Community", Icons.Rounded.Forum)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 12.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceCard),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        tabItems.forEachIndexed { idx, (label, icon) ->
            val isSelected = selectedTab == idx
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(4.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (isSelected) Brush.linearGradient(listOf(AccentEmerald, AccentCyan))
                        else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                    )
                    .clickable { onTabSelected(idx) }
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Icon(
                    icon, null,
                    tint     = if (isSelected) Color.White else TextMuted,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    label,
                    color      = if (isSelected) Color.White else TextMuted,
                    fontSize   = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun ChatListContent(
    uiState: ChatListUiState,
    searchQuery: String,
    currentUid: String,
    onSearchChange: (String) -> Unit,
    onChatClick: (String) -> Unit,
    onGroupClick: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value         = searchQuery,
            onValueChange = onSearchChange,
            placeholder   = { Text("Search conversations...", color = TextMuted, fontSize = 13.sp) },
            leadingIcon   = { Icon(Icons.Rounded.Search, null, tint = TextMuted, modifier = Modifier.size(18.dp)) },
            modifier      = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 8.dp),
            shape  = RoundedCornerShape(12.dp),
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

        when (uiState) {
            is ChatListUiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AccentCyan, modifier = Modifier.size(28.dp))
                }
            }
            is ChatListUiState.Error -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(uiState.message, color = TextSecondary, fontSize = 13.sp)
                }
            }
            is ChatListUiState.Success -> {
                val chats = uiState.chats
                if (chats.isEmpty()) {
                    EmptyChatList()
                    return@Column
                }
                val filtered = if (searchQuery.isBlank()) chats
                else chats.filter {
                    it.getDisplayName(currentUid).contains(searchQuery, ignoreCase = true)
                }

                LazyColumn(
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    item { PinnedCommunityBanner() }

                    itemsIndexed(filtered) { _, chat ->
                        val isGroup = chat.type == ChatType.GROUP.name ||
                                chat.type == ChatType.STUDY_ROOM.name ||
                                chat.type == ChatType.SUBJECT_GROUP.name
                        ChatRow(
                            chat       = chat,
                            currentUid = currentUid,
                            onClick    = {
                                if (isGroup) onGroupClick(chat.chatId)
                                else onChatClick(chat.chatId)
                            }
                        )
                    }

                    if (filtered.isEmpty() && searchQuery.isNotBlank()) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No chats match \"$searchQuery\"", color = TextMuted, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PinnedCommunityBanner() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(AccentEmerald.copy(0.08f))
            .border(1.dp, AccentEmerald.copy(0.25f), RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(
                    Brush.linearGradient(listOf(AccentEmerald, AccentCyan)),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.Group, null, tint = Color.White, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Chat with Community",
                    fontWeight = FontWeight.Bold,
                    fontSize   = 14.sp,
                    color      = TextPrimary
                )
                Spacer(Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .background(AccentEmerald.copy(0.15f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 5.dp, vertical = 1.dp)
                ) {
                    Text("Group", fontSize = 9.sp, color = AccentEmerald, fontWeight = FontWeight.Bold)
                }
            }
            Text("All members · Active now", fontSize = 11.sp, color = TextMuted)
        }
        Box(modifier = Modifier.size(8.dp).background(AccentEmerald, CircleShape))
    }
}

@Composable
private fun ChatRow(
    chat: Chat,
    currentUid: String,
    onClick: () -> Unit
) {
    val displayName  = chat.getDisplayName(currentUid)
    val displayPhoto = chat.getDisplayPhoto(currentUid)
    val unreadCount  = chat.getUnreadCount(currentUid)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(listOf(AccentCyan.copy(0.3f), AccentIndigo.copy(0.3f)))
                )
                .border(1.dp, AccentCyan.copy(0.2f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (displayPhoto.isNotEmpty()) {
                AsyncImage(
                    model              = displayPhoto,
                    contentDescription = null,
                    contentScale       = ContentScale.Crop,
                    modifier           = Modifier.fillMaxSize().clip(CircleShape)
                )
            } else {
                Text(
                    displayName.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                    fontSize   = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color      = TextPrimary
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                displayName,
                fontWeight = FontWeight.SemiBold,
                fontSize   = 14.sp,
                color      = TextPrimary,
                maxLines   = 1,
                overflow   = TextOverflow.Ellipsis
            )
            Text(
                chat.lastMessage.ifEmpty { "Start the conversation" },
                fontSize = 12.sp,
                color    = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(Modifier.width(8.dp))

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val timeStr = try {
                SimpleDateFormat("HH:mm", Locale.getDefault()).format(chat.lastMessageTime.toDate())
            } catch (_: Exception) { "" }
            if (timeStr.isNotEmpty()) {
                Text(timeStr, fontSize = 10.sp, color = TextMuted)
            }
            if (unreadCount > 0) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .background(AccentCyan, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "${minOf(unreadCount, 99)}",
                        fontSize   = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color      = Color.White
                    )
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .padding(horizontal = 16.dp)
            .background(SurfaceElevated)
    )
}

@Composable
private fun EmptyChatList() {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("💬", fontSize = 48.sp)
        Spacer(Modifier.height(16.dp))
        Text("No chats yet", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(Modifier.height(8.dp))
        Text(
            "Your private conversations will appear here.",
            fontSize = 13.sp,
            color    = TextSecondary
        )
    }
}
