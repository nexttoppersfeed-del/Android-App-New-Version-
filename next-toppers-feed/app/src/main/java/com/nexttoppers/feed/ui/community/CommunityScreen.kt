package com.nexttoppers.feed.ui.community

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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.nexttoppers.feed.data.model.CommunityPost
import com.nexttoppers.feed.ui.theme.AccentCyan
import com.nexttoppers.feed.ui.theme.AccentEmerald
import com.nexttoppers.feed.ui.theme.AccentViolet
import com.nexttoppers.feed.ui.theme.BackgroundBlack
import com.nexttoppers.feed.ui.theme.NeonGreen
import com.nexttoppers.feed.ui.theme.SurfaceCard
import com.nexttoppers.feed.ui.theme.SurfaceElevated
import com.nexttoppers.feed.ui.theme.TextMuted
import com.nexttoppers.feed.ui.theme.TextPrimary
import com.nexttoppers.feed.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun CommunityScreen(
    onNavigateToCreatePost: () -> Unit,
    onNavigateToPostDetail: (String) -> Unit,
    viewModel: CommunityViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentUid = viewModel.currentUid

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundBlack)
            .imePadding()
    ) {
        // ── Header ────────────────────────────────────────────────────────────
        CommunityHeader()

        // ── Content ───────────────────────────────────────────────────────────
        Box(modifier = Modifier.weight(1f)) {
            when (val state = uiState) {
                is CommunityUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AccentCyan, modifier = Modifier.size(28.dp))
                    }
                }
                is CommunityUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(state.message, color = TextSecondary, fontSize = 14.sp)
                    }
                }
                is CommunityUiState.Success -> {
                    if (state.posts.isEmpty()) {
                        EmptyCommunity(onPostClick = onNavigateToCreatePost)
                    } else {
                        val listState = rememberLazyListState()
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(top = 8.dp, bottom = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            items(state.posts, key = { it.postId }) { post ->
                                val isOwn = post.userId == currentUid
                                ChatBubble(
                                    post       = post,
                                    isOwn      = isOwn,
                                    onClick    = { onNavigateToPostDetail(post.postId) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // ── Message input bar ─────────────────────────────────────────────────
        MessageInputBar(onTap = onNavigateToCreatePost)
    }
}

@Composable
private fun CommunityHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceCard)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    Brush.linearGradient(listOf(AccentEmerald, AccentCyan)),
                    RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.Forum, null, tint = Color.White, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text("NextToppers Community", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(6.dp).background(AccentEmerald, CircleShape))
                Spacer(Modifier.width(5.dp))
                Text("Active now · Global feed", fontSize = 11.sp, color = AccentEmerald)
            }
        }
    }
    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(SurfaceElevated))
}

@Composable
private fun ChatBubble(
    post: CommunityPost,
    isOwn: Boolean,
    onClick: () -> Unit
) {
    val timeStr = try {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(post.createdAt.toDate())
    } catch (_: Exception) { "" }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 3.dp),
        horizontalArrangement = if (isOwn) Arrangement.End else Arrangement.Start
    ) {
        // Avatar (others only)
        if (!isOwn) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(AccentCyan.copy(0.2f))
                    .align(Alignment.Bottom),
                contentAlignment = Alignment.Center
            ) {
                if (post.userPhoto.isNotEmpty()) {
                    AsyncImage(
                        model              = post.userPhoto,
                        contentDescription = null,
                        contentScale       = ContentScale.Crop,
                        modifier           = Modifier.fillMaxSize().clip(CircleShape)
                    )
                } else {
                    Text(
                        post.username.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
        }

        Column(
            modifier = Modifier.weight(1f, fill = false),
            horizontalAlignment = if (isOwn) Alignment.End else Alignment.Start
        ) {
            // Username (others only)
            if (!isOwn) {
                Text(
                    post.username,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AccentCyan,
                    modifier = Modifier.padding(bottom = 2.dp, start = 4.dp)
                )
            }

            // Bubble
            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart    = if (isOwn) 16.dp else 4.dp,
                            topEnd      = if (isOwn) 4.dp  else 16.dp,
                            bottomStart = 16.dp,
                            bottomEnd   = 16.dp
                        )
                    )
                    .background(
                        if (isOwn) Brush.linearGradient(listOf(AccentEmerald.copy(0.8f), AccentCyan.copy(0.7f)))
                        else Brush.linearGradient(listOf(SurfaceCard, SurfaceCard))
                    )
                    .border(
                        1.dp,
                        if (isOwn) Color.Transparent else SurfaceElevated,
                        RoundedCornerShape(
                            topStart = if (isOwn) 16.dp else 4.dp,
                            topEnd = if (isOwn) 4.dp else 16.dp,
                            bottomStart = 16.dp, bottomEnd = 16.dp
                        )
                    )
                    .clickable(onClick = onClick)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .widthIn(max = 280.dp)
            ) {
                Column {
                    // Title (if present)
                    if (post.title.isNotEmpty()) {
                        Text(
                            post.title,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isOwn) Color.White else TextPrimary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(2.dp))
                    }
                    // Content
                    if (post.content.isNotEmpty()) {
                        Text(
                            post.content,
                            fontSize = 13.sp,
                            color = if (isOwn) Color.White.copy(0.95f) else TextPrimary,
                            lineHeight = 18.sp,
                            maxLines = 8,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    // Time + likes
                    Spacer(Modifier.height(3.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (post.likes.isNotEmpty()) {
                            Text("👍 ${post.likes.size}", fontSize = 10.sp, color = if (isOwn) Color.White.copy(0.7f) else TextMuted)
                            Spacer(Modifier.width(6.dp))
                        }
                        Text(timeStr, fontSize = 10.sp, color = if (isOwn) Color.White.copy(0.7f) else TextMuted)
                    }
                }
            }
        }

        if (isOwn) Spacer(Modifier.width(8.dp))
    }
}

@Composable
private fun MessageInputBar(onTap: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceCard)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(44.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(SurfaceElevated)
                .border(1.dp, SurfaceElevated, RoundedCornerShape(22.dp))
                .clickable(onClick = onTap)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text("Type a message...", color = TextMuted, fontSize = 14.sp)
        }
        Spacer(Modifier.width(10.dp))
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(
                    Brush.linearGradient(listOf(AccentEmerald, AccentCyan)),
                    CircleShape
                )
                .clickable(onClick = onTap),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.Send, null, tint = Color.White, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun EmptyCommunity(onPostClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("💬", fontSize = 48.sp)
        Spacer(Modifier.height(16.dp))
        Text("Community Chat", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(Modifier.height(8.dp))
        Text(
            "No messages yet. Be the first to say something!",
            fontSize = 13.sp,
            color = TextSecondary,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(Modifier.height(24.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(AccentEmerald.copy(0.15f))
                .border(1.dp, AccentEmerald.copy(0.4f), RoundedCornerShape(12.dp))
                .clickable(onClick = onPostClick)
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Add, null, tint = AccentEmerald, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Start the conversation", color = AccentEmerald, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            }
        }
    }
}

