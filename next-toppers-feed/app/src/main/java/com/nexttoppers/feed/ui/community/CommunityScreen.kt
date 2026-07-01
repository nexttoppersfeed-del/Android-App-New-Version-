package com.nexttoppers.feed.ui.community

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.EmojiEmotions
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material.icons.rounded.Reply
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
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
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CommunityScreen(
    onNavigateToCreatePost: () -> Unit = {},
    onNavigateToPostDetail: (String) -> Unit = {},
    onOpenDmWith: (userId: String) -> Unit = {},
    viewModel: CommunityViewModel = hiltViewModel()
) {
    val uiState      by viewModel.uiState.collectAsState()
    val messageInput by viewModel.messageInput.collectAsState()
    val isSending    by viewModel.isSending.collectAsState()
    val replyTo      by viewModel.replyTo.collectAsState()
    val editTarget   by viewModel.editTarget.collectAsState()
    val likeAnims    by viewModel.likeAnimations.collectAsState()
    val currentUid   = viewModel.currentUid
    val listState    = rememberLazyListState()
    val scope        = rememberCoroutineScope()
    val clipboard    = LocalClipboardManager.current

    val posts = (uiState as? CommunityUiState.Success)?.posts ?: emptyList()

    val isAtBottom by remember {
        derivedStateOf {
            val info        = listState.layoutInfo
            val lastVisible = info.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisible >= info.totalItemsCount - 2
        }
    }

    LaunchedEffect(posts.size) {
        if (posts.isNotEmpty() && isAtBottom) {
            listState.animateScrollToItem(posts.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundBlack)
            .imePadding()
    ) {
        CommunityHeader()

        Box(modifier = Modifier.weight(1f)) {
            when (val state = uiState) {
                is CommunityUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AccentCyan, modifier = Modifier.size(28.dp), strokeWidth = 2.dp)
                    }
                }
                is CommunityUiState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(state.message, color = TextSecondary, fontSize = 13.sp)
                    }
                }
                is CommunityUiState.Success -> {
                    if (state.posts.isEmpty()) {
                        EmptyCommunity(onPostClick = onNavigateToCreatePost)
                    } else {
                        LazyColumn(
                            state               = listState,
                            modifier            = Modifier.fillMaxSize(),
                            contentPadding      = PaddingValues(top = 8.dp, bottom = 80.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            itemsIndexed(state.posts, key = { _, post -> post.postId }) { index, post ->
                                val showDateSep = index == 0 ||
                                        getDateLabel(post.createdAt.toDate()) !=
                                        getDateLabel(state.posts[index - 1].createdAt.toDate())
                                if (showDateSep) {
                                    CommunityDateSeparator(getDateLabel(post.createdAt.toDate()))
                                }
                                val isOwn     = post.userId == currentUid
                                val isLiked   = post.isLikedBy(currentUid)
                                val isAnimated = post.postId in likeAnims
                                CommunityBubble(
                                    post      = post,
                                    isOwn     = isOwn,
                                    isLiked   = isLiked,
                                    isLikeAnimating = isAnimated,
                                    onClick         = { onNavigateToPostDetail(post.postId) },
                                    onAvatarClick   = { if (!isOwn) onOpenDmWith(post.userId) },
                                    onLike          = { viewModel.toggleLike(post) },
                                    onReply         = { viewModel.setReplyTo(post) },
                                    onEdit          = { viewModel.startEdit(post) },
                                    onDelete        = { viewModel.deletePost(post.postId) },
                                    onCopy          = { clipboard.setText(AnnotatedString(post.content)) },
                                    onReport        = { viewModel.reportPost(post.postId) }
                                )
                            }
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 14.dp, bottom = 6.dp)
            ) {
                if (!isAtBottom && posts.isNotEmpty()) {
                    FloatingActionButton(
                        onClick        = { scope.launch { listState.animateScrollToItem(posts.size - 1) } },
                        modifier       = Modifier.size(40.dp),
                        containerColor = NeonGreen,
                        contentColor   = BackgroundBlack,
                        shape          = CircleShape
                    ) {
                        Icon(Icons.Rounded.ArrowDownward, null, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        replyTo?.let { replyPost ->
            CommunityReplyBar(
                author   = replyPost.username,
                preview  = replyPost.content,
                onCancel = { viewModel.clearReply() }
            )
        }
        editTarget?.let { ep ->
            CommunityEditBar(
                preview  = ep.content,
                onCancel = { viewModel.cancelEdit() }
            )
        }

        CommunityMessageInputBar(
            value         = messageInput,
            onValueChange = viewModel::setMessageInput,
            onSend        = viewModel::sendQuickMessage,
            onNewPost     = onNavigateToCreatePost,
            isSending     = isSending,
            isEditing     = editTarget != null
        )
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
                .background(Brush.linearGradient(listOf(AccentEmerald, AccentCyan)), RoundedCornerShape(12.dp)),
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CommunityBubble(
    post: CommunityPost,
    isOwn: Boolean,
    isLiked: Boolean,
    isLikeAnimating: Boolean,
    onClick: () -> Unit,
    onAvatarClick: () -> Unit,
    onLike: () -> Unit,
    onReply: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onCopy: () -> Unit,
    onReport: () -> Unit
) {
    val timeStr = try {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(post.createdAt.toDate())
    } catch (_: Exception) { "" }

    val maxWidth   = (LocalConfiguration.current.screenWidthDp * 0.74f).dp
    val likeScale  by animateFloatAsState(
        targetValue   = if (isLikeAnimating) 1.35f else 1f,
        animationSpec = tween(200, easing = FastOutSlowInEasing),
        label         = "like_scale"
    )
    var showMenu   by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 3.dp),
        horizontalArrangement = if (isOwn) Arrangement.End else Arrangement.Start
    ) {
        if (!isOwn) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(AccentCyan.copy(0.15f))
                    .border(1.dp, AccentCyan.copy(0.3f), CircleShape)
                    .clickable(onClick = onAvatarClick)
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
                        fontSize   = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color      = TextPrimary
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
        }

        Column(
            modifier           = Modifier.weight(1f, fill = false),
            horizontalAlignment = if (isOwn) Alignment.End else Alignment.Start
        ) {
            if (!isOwn) {
                Text(
                    post.username,
                    fontSize   = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = AccentCyan,
                    modifier   = Modifier.padding(bottom = 2.dp, start = 4.dp)
                )
            }

            Box {
                Box(
                    modifier = Modifier
                        .clip(
                            RoundedCornerShape(
                                topStart    = if (isOwn) 16.dp else 4.dp,
                                topEnd      = if (isOwn) 4.dp else 16.dp,
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
                                topStart    = if (isOwn) 16.dp else 4.dp,
                                topEnd      = if (isOwn) 4.dp else 16.dp,
                                bottomStart = 16.dp,
                                bottomEnd   = 16.dp
                            )
                        )
                        .combinedClickable(onClick = onClick, onLongClick = { showMenu = true })
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .widthIn(max = maxWidth)
                ) {
                    Column {
                        if (post.title.isNotEmpty()) {
                            Text(
                                post.title,
                                fontSize   = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color      = if (isOwn) Color.White else TextPrimary,
                                maxLines   = 2,
                                overflow   = TextOverflow.Ellipsis
                            )
                            Spacer(Modifier.height(2.dp))
                        }
                        if (post.content.isNotEmpty()) {
                            Text(
                                text      = buildMentionText(post.content, isOwn),
                                fontSize  = 13.sp,
                                lineHeight = 18.sp,
                                maxLines  = 8,
                                overflow  = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment  = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .scale(likeScale)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (isLiked) AccentCyan.copy(0.18f)
                                            else Color.Transparent
                                        )
                                        .clickable(onClick = onLike)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            if (isLiked) "👍" else "👍",
                                            fontSize = 12.sp,
                                            color    = if (isLiked) AccentCyan
                                                       else if (isOwn) Color.White.copy(0.6f)
                                                       else TextMuted
                                        )
                                        if (post.likes.isNotEmpty()) {
                                            Spacer(Modifier.width(3.dp))
                                            Text(
                                                "${post.likes.size}",
                                                fontSize   = 10.sp,
                                                color      = if (isLiked) AccentCyan
                                                             else if (isOwn) Color.White.copy(0.6f)
                                                             else TextMuted,
                                                fontWeight = if (isLiked) FontWeight.SemiBold else FontWeight.Normal
                                            )
                                        }
                                    }
                                }
                            }
                            Text(
                                timeStr,
                                fontSize = 10.sp,
                                color    = if (isOwn) Color.White.copy(0.65f) else TextMuted
                            )
                        }
                    }
                }

                DropdownMenu(
                    expanded         = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier         = Modifier.background(SurfaceCard)
                ) {
                    DropdownMenuItem(
                        text        = { Text("Reply", color = TextPrimary, fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Rounded.Reply, null, tint = AccentCyan, modifier = Modifier.size(16.dp)) },
                        onClick     = { onReply(); showMenu = false }
                    )
                    DropdownMenuItem(
                        text        = { Text("Copy", color = TextPrimary, fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Rounded.EmojiEmotions, null, tint = AccentCyan, modifier = Modifier.size(16.dp)) },
                        onClick     = { onCopy(); showMenu = false }
                    )
                    if (isOwn) {
                        DropdownMenuItem(
                            text        = { Text("Edit", color = TextPrimary, fontSize = 13.sp) },
                            leadingIcon = { Icon(Icons.Rounded.EmojiEmotions, null, tint = NeonGreen, modifier = Modifier.size(16.dp)) },
                            onClick     = { onEdit(); showMenu = false }
                        )
                        DropdownMenuItem(
                            text        = { Text("Delete", color = Color(0xFFEF5350), fontSize = 13.sp) },
                            leadingIcon = { Icon(Icons.Rounded.Close, null, tint = Color(0xFFEF5350), modifier = Modifier.size(16.dp)) },
                            onClick     = { onDelete(); showMenu = false }
                        )
                    }
                    if (!isOwn) {
                        DropdownMenuItem(
                            text        = { Text("Report", color = Color(0xFFEF5350), fontSize = 13.sp) },
                            leadingIcon = { Icon(Icons.Rounded.Close, null, tint = Color(0xFFEF5350), modifier = Modifier.size(16.dp)) },
                            onClick     = { onReport(); showMenu = false }
                        )
                    }
                }
            }
        }

        if (isOwn) Spacer(Modifier.width(8.dp))
    }
}

@Composable
private fun CommunityDateSeparator(label: String) {
    Row(
        modifier          = Modifier.fillMaxWidth().padding(vertical = 10.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.weight(1f).height(0.5.dp).background(SurfaceElevated))
        Spacer(Modifier.width(10.dp))
        Text(
            label,
            color      = TextMuted,
            fontSize   = 11.sp,
            fontWeight = FontWeight.SemiBold,
            modifier   = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(SurfaceCard)
                .padding(horizontal = 10.dp, vertical = 4.dp)
        )
        Spacer(Modifier.width(10.dp))
        Box(Modifier.weight(1f).height(0.5.dp).background(SurfaceElevated))
    }
}

@Composable
private fun CommunityReplyBar(author: String, preview: String, onCancel: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceCard)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.width(3.dp).height(32.dp).background(AccentCyan, RoundedCornerShape(2.dp)))
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text("Replying to @$author", fontSize = 11.sp, color = AccentCyan, fontWeight = FontWeight.SemiBold)
            Text(preview.take(80), fontSize = 12.sp, color = TextMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        IconButton(onClick = onCancel, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Rounded.Close, null, tint = TextMuted, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun CommunityEditBar(preview: String, onCancel: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceCard)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.width(3.dp).height(32.dp).background(NeonGreen, RoundedCornerShape(2.dp)))
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text("Editing message", fontSize = 11.sp, color = NeonGreen, fontWeight = FontWeight.SemiBold)
            Text(preview.take(80), fontSize = 12.sp, color = TextMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        IconButton(onClick = onCancel, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Rounded.Close, null, tint = TextMuted, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun CommunityMessageInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    onNewPost: () -> Unit,
    isSending: Boolean,
    isEditing: Boolean
) {
    Box(modifier = Modifier.fillMaxWidth().height(0.5.dp).background(SurfaceElevated))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceCard)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isEditing) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(AccentViolet.copy(0.12f))
                    .border(1.dp, AccentViolet.copy(0.3f), CircleShape)
                    .clickable(onClick = onNewPost),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Add, null, tint = AccentViolet, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(8.dp))
        }

        OutlinedTextField(
            value         = value,
            onValueChange = onValueChange,
            placeholder   = {
                Text(
                    if (isEditing) "Edit message..." else "Message community...",
                    color    = TextMuted,
                    fontSize = 13.sp
                )
            },
            modifier  = Modifier.weight(1f),
            textStyle = TextStyle(color = TextPrimary, fontSize = 14.sp),
            shape     = RoundedCornerShape(20.dp),
            colors    = OutlinedTextFieldDefaults.colors(
                focusedBorderColor      = if (isEditing) NeonGreen.copy(0.6f) else AccentEmerald.copy(0.5f),
                unfocusedBorderColor    = SurfaceElevated,
                cursorColor             = AccentEmerald,
                focusedContainerColor   = SurfaceElevated,
                unfocusedContainerColor = SurfaceElevated
            ),
            maxLines = 4
        )

        Spacer(Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(
                    if (value.isNotBlank())
                        Brush.linearGradient(listOf(AccentEmerald, AccentCyan))
                    else
                        Brush.linearGradient(listOf(SurfaceElevated, SurfaceElevated))
                )
                .clickable(enabled = value.isNotBlank() && !isSending, onClick = onSend),
            contentAlignment = Alignment.Center
        ) {
            if (isSending) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
            } else {
                Icon(
                    Icons.Rounded.Send,
                    null,
                    tint     = if (value.isNotBlank()) Color.White else TextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun EmptyCommunity(onPostClick: () -> Unit) {
    Column(
        modifier            = Modifier.fillMaxSize().padding(32.dp),
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
            color    = TextSecondary,
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

private fun buildMentionText(text: String, isOwn: Boolean): AnnotatedString {
    return buildAnnotatedString {
        val regex   = Regex("@\\w+")
        var lastEnd = 0
        val baseColor = if (isOwn) Color.White.copy(0.95f) else Color(0xFFE0E0E0)
        regex.findAll(text).forEach { match ->
            withStyle(SpanStyle(color = baseColor)) {
                append(text.substring(lastEnd, match.range.first))
            }
            withStyle(SpanStyle(
                color      = if (isOwn) Color(0xFFB2EBF2) else AccentCyan,
                fontWeight = FontWeight.SemiBold
            )) {
                append(match.value)
            }
            lastEnd = match.range.last + 1
        }
        if (lastEnd < text.length) {
            withStyle(SpanStyle(color = baseColor)) {
                append(text.substring(lastEnd))
            }
        }
    }
}

private fun getDateLabel(date: Date): String {
    val now       = Calendar.getInstance()
    val msgCal    = Calendar.getInstance().apply { time = date }
    val isSameDay = now.get(Calendar.DATE) == msgCal.get(Calendar.DATE) &&
            now.get(Calendar.YEAR) == msgCal.get(Calendar.YEAR)
    val isYesterday = now.get(Calendar.DATE) - msgCal.get(Calendar.DATE) == 1 &&
            now.get(Calendar.YEAR) == msgCal.get(Calendar.YEAR)
    val isSameWeek = now.get(Calendar.WEEK_OF_YEAR) == msgCal.get(Calendar.WEEK_OF_YEAR) &&
            now.get(Calendar.YEAR) == msgCal.get(Calendar.YEAR)
    return when {
        isSameDay   -> "Today"
        isYesterday -> "Yesterday"
        isSameWeek  -> SimpleDateFormat("EEEE", Locale.getDefault()).format(date)
        else        -> SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(date)
    }
}
