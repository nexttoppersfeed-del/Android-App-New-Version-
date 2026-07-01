package com.nexttoppers.feed.ui.chats

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
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
import com.nexttoppers.feed.data.model.User
import com.nexttoppers.feed.ui.community.CommunityScreen
import com.nexttoppers.feed.ui.theme.AccentCyan
import com.nexttoppers.feed.ui.theme.AccentEmerald
import com.nexttoppers.feed.ui.theme.AccentIndigo
import com.nexttoppers.feed.ui.theme.BackgroundBlack
import com.nexttoppers.feed.ui.theme.NeonGreen
import com.nexttoppers.feed.ui.theme.SurfaceCard
import com.nexttoppers.feed.ui.theme.SurfaceElevated
import com.nexttoppers.feed.ui.theme.TextMuted
import com.nexttoppers.feed.ui.theme.TextPrimary
import com.nexttoppers.feed.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatsScreen(
    onNavigateToChat: (String) -> Unit = {},
    onNavigateToCreatePost: () -> Unit = {},
    onNavigateToPostDetail: (String) -> Unit = {},
    onNavigateToGroup: (String) -> Unit = {},
    viewModel: ChatListViewModel = hiltViewModel()
) {
    val uiState           by viewModel.uiState.collectAsState()
    val searchQuery       by viewModel.searchQuery.collectAsState()
    val pendingNav        by viewModel.pendingNavigation.collectAsState()
    val isFirstLoad       by viewModel.isFirstLoad.collectAsState()
    val userSearchResults by viewModel.userSearchResults.collectAsState()
    val isSearchingUsers  by viewModel.isSearchingUsers.collectAsState()
    val currentUid        = viewModel.currentUid
    var selectedTab       by remember { mutableIntStateOf(0) }
    var showNewChatDialog by remember { mutableStateOf(false) }
    var newChatQuery      by remember { mutableStateOf("") }

    LaunchedEffect(pendingNav) {
        pendingNav?.let { chatId ->
            viewModel.consumeNavigation()
            onNavigateToChat(chatId)
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(BackgroundBlack)) {
        ConnectTopBar(
            selectedTab = selectedTab,
            onNewChat   = { showNewChatDialog = true }
        )
        ConnectTabs(selectedTab = selectedTab, onTabSelected = { selectedTab = it })

        when (selectedTab) {
            0 -> ChatListContent(
                uiState          = uiState,
                isFirstLoad      = isFirstLoad,
                searchQuery      = searchQuery,
                currentUid       = currentUid,
                onSearchChange   = viewModel::setSearchQuery,
                onChatClick      = onNavigateToChat,
                onGroupClick     = onNavigateToGroup,
                onCommunityClick = { selectedTab = 1 }
            )
            1 -> CommunityScreen(
                onNavigateToCreatePost = onNavigateToCreatePost,
                onNavigateToPostDetail = onNavigateToPostDetail,
                onOpenDmWith           = { userId -> viewModel.openDm(userId) }
            )
        }
    }

    if (showNewChatDialog) {
        NewChatDialog(
            query          = newChatQuery,
            onQueryChange  = { q ->
                newChatQuery = q
                viewModel.searchUsers(q)
            },
            results        = userSearchResults,
            isSearching    = isSearchingUsers,
            onUserSelected = { user ->
                showNewChatDialog = false
                newChatQuery      = ""
                viewModel.clearUserSearch()
                viewModel.openDm(user.uid)
            },
            onDismiss      = {
                showNewChatDialog = false
                newChatQuery      = ""
                viewModel.clearUserSearch()
            }
        )
    }
}

@Composable
private fun ConnectTopBar(selectedTab: Int, onNewChat: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
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
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            if (selectedTab == 0) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(AccentCyan.copy(0.12f), RoundedCornerShape(12.dp))
                        .border(1.dp, AccentCyan.copy(0.3f), RoundedCornerShape(12.dp))
                        .clickable(onClick = onNewChat),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.PersonAdd, null, tint = AccentCyan, modifier = Modifier.size(18.dp))
                }
            }
            Box(
                modifier = Modifier
                    .size(38.dp)
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
}

@Composable
private fun ConnectTabs(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    val tabItems = listOf(Pair("Chats", Icons.Rounded.Chat), Pair("Community", Icons.Rounded.Forum))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 10.dp)
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
                Icon(icon, null, tint = if (isSelected) Color.White else TextMuted, modifier = Modifier.size(15.dp))
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
    isFirstLoad: Boolean,
    searchQuery: String,
    currentUid: String,
    onSearchChange: (String) -> Unit,
    onChatClick: (String) -> Unit,
    onGroupClick: (String) -> Unit,
    onCommunityClick: () -> Unit
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

        if (isFirstLoad) {
            ChatSkeletonList()
            return@Column
        }

        when (uiState) {
            is ChatListUiState.Loading -> ChatSkeletonList()
            is ChatListUiState.Error -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(uiState.message, color = TextSecondary, fontSize = 13.sp)
                }
            }
            is ChatListUiState.Success -> {
                val chats = uiState.chats
                if (chats.isEmpty() && searchQuery.isBlank()) {
                    EmptyChatList()
                    return@Column
                }

                LazyColumn(contentPadding = PaddingValues(bottom = 100.dp)) {
                    if (searchQuery.isBlank()) {
                        item { PinnedCommunityBanner(onClick = onCommunityClick) }
                    }
                    itemsIndexed(chats, key = { _, chat -> chat.chatId }) { _, chat ->
                        val isGroup = chat.type == ChatType.GROUP.name ||
                                chat.type == ChatType.STUDY_ROOM.name ||
                                chat.type == ChatType.SUBJECT_GROUP.name
                        ChatRowItem(
                            chat       = chat,
                            currentUid = currentUid,
                            onClick    = { if (isGroup) onGroupClick(chat.chatId) else onChatClick(chat.chatId) }
                        )
                    }
                    if (chats.isEmpty() && searchQuery.isNotBlank()) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "No chats match \"$searchQuery\"",
                                    color    = TextMuted,
                                    fontSize = 13.sp
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
private fun ChatSkeletonList() {
    val brush = shimmerBrush()
    LazyColumn(contentPadding = PaddingValues(bottom = 100.dp)) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .height(64.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(brush)
            )
        }
        items(6) {
            ChatSkeletonRow(brush)
        }
    }
}

@Composable
private fun shimmerBrush(): Brush {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val alpha by transition.animateFloat(
        initialValue  = 0.25f,
        targetValue   = 0.6f,
        animationSpec = infiniteRepeatable(
            animation  = tween(850, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer_alpha"
    )
    return Brush.linearGradient(listOf(SurfaceCard, SurfaceElevated.copy(alpha), SurfaceCard))
}

@Composable
private fun ChatSkeletonRow(brush: Brush) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(50.dp).clip(CircleShape).background(brush))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Box(Modifier.fillMaxWidth(0.42f).height(13.dp).clip(RoundedCornerShape(7.dp)).background(brush))
            Spacer(Modifier.height(7.dp))
            Box(Modifier.fillMaxWidth(0.66f).height(11.dp).clip(RoundedCornerShape(5.dp)).background(brush))
        }
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(Modifier.width(30.dp).height(10.dp).clip(RoundedCornerShape(5.dp)).background(brush))
            Box(Modifier.size(18.dp).clip(CircleShape).background(brush))
        }
    }
    Box(Modifier.fillMaxWidth().height(0.5.dp).padding(horizontal = 78.dp).background(SurfaceElevated.copy(0.5f)))
}

@Composable
private fun PinnedCommunityBanner(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(AccentEmerald.copy(0.08f))
            .border(1.dp, AccentEmerald.copy(0.25f), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .background(Brush.linearGradient(listOf(AccentEmerald, AccentCyan)), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.Group, null, tint = Color.White, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("NextToppers Community", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                Spacer(Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .background(AccentEmerald.copy(0.2f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 5.dp, vertical = 1.dp)
                ) {
                    Text("Live", fontSize = 9.sp, color = AccentEmerald, fontWeight = FontWeight.Bold)
                }
            }
            Text("All members · Active now", fontSize = 11.sp, color = TextMuted)
        }
        Box(modifier = Modifier.size(8.dp).background(AccentEmerald, CircleShape))
    }
}

@Composable
private fun ChatRowItem(chat: Chat, currentUid: String, onClick: () -> Unit) {
    val displayName  = chat.getDisplayName(currentUid)
    val displayPhoto = chat.getDisplayPhoto(currentUid)
    val unreadCount  = chat.getUnreadCount(currentUid)
    val hasUnread    = unreadCount > 0

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(if (hasUnread) AccentCyan.copy(0.04f) else Color.Transparent)
            .padding(horizontal = 16.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(50.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(AccentCyan.copy(0.25f), AccentIndigo.copy(0.25f))))
                    .border(
                        width = if (hasUnread) 2.dp else 1.dp,
                        brush = if (hasUnread)
                            Brush.linearGradient(listOf(AccentCyan, AccentEmerald))
                        else
                            Brush.linearGradient(listOf(SurfaceElevated, SurfaceElevated)),
                        shape = CircleShape
                    ),
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
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                displayName,
                fontWeight = if (hasUnread) FontWeight.Bold else FontWeight.SemiBold,
                fontSize   = 14.sp,
                color      = TextPrimary,
                maxLines   = 1,
                overflow   = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Text(
                chat.lastMessage.ifEmpty { "Start the conversation" },
                fontSize   = 12.sp,
                color      = if (hasUnread) TextSecondary else TextMuted,
                fontWeight = if (hasUnread) FontWeight.Medium else FontWeight.Normal,
                maxLines   = 1,
                overflow   = TextOverflow.Ellipsis
            )
        }

        Spacer(Modifier.width(8.dp))

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            val timeStr = formatChatTime(chat.lastMessageTime.toDate())
            if (timeStr.isNotEmpty()) {
                Text(
                    timeStr,
                    fontSize   = 10.sp,
                    color      = if (hasUnread) AccentCyan else TextMuted,
                    fontWeight = if (hasUnread) FontWeight.SemiBold else FontWeight.Normal
                )
            }
            if (hasUnread) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(Brush.linearGradient(listOf(AccentCyan, AccentEmerald)), CircleShape),
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
        Modifier
            .fillMaxWidth()
            .height(0.5.dp)
            .padding(horizontal = 78.dp)
            .background(SurfaceElevated.copy(0.5f))
    )
}

@Composable
private fun EmptyChatList() {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(AccentCyan.copy(0.1f), CircleShape)
                .border(1.dp, AccentCyan.copy(0.2f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.Forum, null, tint = AccentCyan, modifier = Modifier.size(38.dp))
        }
        Spacer(Modifier.height(20.dp))
        Text("No conversations yet", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(Modifier.height(8.dp))
        Text(
            "Tap the person+ icon above to start a new direct message.",
            fontSize = 13.sp,
            color    = TextMuted,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Composable
private fun NewChatDialog(
    query: String,
    onQueryChange: (String) -> Unit,
    results: List<User>,
    isSearching: Boolean,
    onUserSelected: (User) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = SurfaceCard,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Edit, null, tint = AccentCyan, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("New Message", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column {
                OutlinedTextField(
                    value         = query,
                    onValueChange = onQueryChange,
                    placeholder   = { Text("Search by name...", color = TextMuted, fontSize = 13.sp) },
                    leadingIcon   = { Icon(Icons.Rounded.Search, null, tint = TextMuted, modifier = Modifier.size(18.dp)) },
                    modifier      = Modifier.fillMaxWidth(),
                    shape         = RoundedCornerShape(12.dp),
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor   = SurfaceElevated,
                        unfocusedContainerColor = SurfaceElevated,
                        focusedBorderColor      = AccentCyan.copy(0.6f),
                        unfocusedBorderColor    = SurfaceCard,
                        focusedTextColor        = TextPrimary,
                        unfocusedTextColor      = TextPrimary
                    ),
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                if (isSearching) {
                    Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AccentCyan, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    }
                } else if (results.isEmpty() && query.length >= 2) {
                    Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        Text("No users found", color = TextMuted, fontSize = 13.sp)
                    }
                } else {
                    results.forEach { user ->
                        UserSearchResultRow(user = user, onClick = { onUserSelected(user) })
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Icon(Icons.Rounded.Close, null, tint = TextMuted, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Cancel", color = TextMuted, fontSize = 13.sp)
            }
        }
    )
}

@Composable
private fun UserSearchResultRow(user: User, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(AccentCyan.copy(0.3f), AccentEmerald.copy(0.2f)))),
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
                    user.name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color      = TextPrimary
                )
            }
        }
        Spacer(Modifier.width(10.dp))
        Column {
            Text(user.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            if (user.email.isNotEmpty()) {
                Text(user.email, fontSize = 11.sp, color = TextMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

private fun formatChatTime(date: Date): String {
    val now     = Calendar.getInstance()
    val msgCal  = Calendar.getInstance().apply { time = date }
    val isSameDay  = now.get(Calendar.DATE) == msgCal.get(Calendar.DATE) &&
            now.get(Calendar.YEAR) == msgCal.get(Calendar.YEAR)
    val isSameWeek = now.get(Calendar.WEEK_OF_YEAR) == msgCal.get(Calendar.WEEK_OF_YEAR) &&
            now.get(Calendar.YEAR) == msgCal.get(Calendar.YEAR)
    return when {
        isSameDay  -> SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
        isSameWeek -> SimpleDateFormat("EEE", Locale.getDefault()).format(date)
        else       -> SimpleDateFormat("MM/dd", Locale.getDefault()).format(date)
    }
}
