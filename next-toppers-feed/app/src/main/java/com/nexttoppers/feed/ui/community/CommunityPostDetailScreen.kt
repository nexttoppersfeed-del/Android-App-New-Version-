package com.nexttoppers.feed.ui.community

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nexttoppers.feed.data.model.Comment
import com.nexttoppers.feed.data.model.CommunityPost
import com.nexttoppers.feed.data.model.PostType
import com.nexttoppers.feed.ui.theme.BackgroundBlack
import com.nexttoppers.feed.ui.theme.NeonCyan
import com.nexttoppers.feed.ui.theme.NeonGreen
import com.nexttoppers.feed.ui.theme.PremiumGold
import com.nexttoppers.feed.ui.theme.SurfaceCard
import com.nexttoppers.feed.ui.theme.SurfaceElevated
import com.nexttoppers.feed.ui.theme.TextMuted
import com.nexttoppers.feed.ui.theme.TextPrimary
import com.nexttoppers.feed.ui.theme.TextSecondary
import com.nexttoppers.feed.ui.theme.WarningAmber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityPostDetailScreen(
    onBack: () -> Unit,
    viewModel: CommunityPostDetailViewModel = hiltViewModel()
) {
    val post by viewModel.post.collectAsState()
    val comments by viewModel.comments.collectAsState()
    val commentInput by viewModel.commentInput.collectAsState()
    val isSending by viewModel.isSending.collectAsState()
    val expandedReplies by viewModel.expandedReplies.collectAsState()

    Scaffold(
        containerColor = BackgroundBlack,
        topBar = {
            TopAppBar(
                title = { Text("Discussion", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, null, tint = TextPrimary) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundBlack)
            )
        },
        bottomBar = {
            CommentInputBar(
                value = commentInput,
                onValueChange = viewModel::setCommentInput,
                onSend = viewModel::submitComment,
                isSending = isSending
            )
        }
    ) { padding ->
        if (post == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = NeonGreen, strokeWidth = 2.dp)
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item {
                PostDetailHeader(post = post!!, onLike = viewModel::toggleLikePost, currentUid = viewModel.currentUid)
            }

            if (comments.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("💬", fontSize = 32.sp)
                            Spacer(Modifier.height(8.dp))
                            Text("No comments yet. Be first!", color = TextSecondary, fontSize = 13.sp)
                        }
                    }
                }
            } else {
                item {
                    Text(
                        "${comments.size} Comment${if (comments.size != 1) "s" else ""}",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                items(comments) { comment ->
                    AnimatedVisibility(visible = true, enter = fadeIn() + expandVertically()) {
                        CommentCard(
                            comment = comment,
                            currentUid = viewModel.currentUid,
                            isRepliesExpanded = expandedReplies.contains(comment.commentId),
                            onLike = { viewModel.toggleLikeComment(comment) },
                            onToggleReplies = { viewModel.toggleRepliesExpanded(comment.commentId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PostDetailHeader(post: CommunityPost, onLike: () -> Unit, currentUid: String) {
    val postType = runCatching { PostType.valueOf(post.type) }.getOrNull()
    val accentColor = when (postType) {
        PostType.DOUBT      -> WarningAmber
        PostType.MOTIVATION -> NeonGreen
        PostType.TOPPER_TIP -> PremiumGold
        PostType.QUESTION   -> NeonCyan
        else                -> NeonGreen
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(SurfaceCard)
            .border(1.dp, Brush.linearGradient(listOf(accentColor.copy(0.3f), Color.Transparent)), RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            UserAvatar(name = post.username, size = 44.dp, color = accentColor)
            Spacer(Modifier.width(10.dp))
            Column {
                Text(post.username, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(
                    "${postType?.label ?: post.type}${if (post.subject.isNotBlank()) " · ${post.subject}" else ""}",
                    color = TextMuted, fontSize = 12.sp
                )
            }
        }

        if (post.title.isNotBlank()) {
            Spacer(Modifier.height(12.dp))
            Text(post.title, color = TextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, lineHeight = 26.sp)
        }
        if (post.content.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(post.content, color = TextSecondary, fontSize = 14.sp, lineHeight = 22.sp)
        }

        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp), verticalAlignment = Alignment.CenterVertically) {
            val isLiked = post.isLikedBy(currentUid)
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onLike() }) {
                Icon(
                    if (isLiked) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                    null, tint = if (isLiked) NeonGreen else TextMuted, modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text("${post.likeCount} Likes", color = if (isLiked) NeonGreen else TextMuted, fontSize = 13.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.ChatBubbleOutline, null, tint = TextMuted, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(6.dp))
                Text("${post.commentsCount} Comments", color = TextMuted, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun CommentCard(
    comment: Comment,
    currentUid: String,
    isRepliesExpanded: Boolean,
    onLike: () -> Unit,
    onToggleReplies: () -> Unit
) {
    val isLiked = comment.isLikedBy(currentUid)

    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            UserAvatar(name = comment.username, size = 32.dp, color = NeonGreen.copy(alpha = 0.7f))
            Spacer(Modifier.width(10.dp))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(SurfaceCard)
                    .padding(12.dp)
            ) {
                Text(comment.username, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Spacer(Modifier.height(4.dp))
                Text(comment.content, color = TextSecondary, fontSize = 13.sp, lineHeight = 20.sp)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onLike() }) {
                        Icon(
                            if (isLiked) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                            null, tint = if (isLiked) NeonGreen else TextMuted, modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("${comment.likeCount}", color = if (isLiked) NeonGreen else TextMuted, fontSize = 11.sp)
                    }
                    if (comment.repliesCount > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onToggleReplies() }) {
                            Icon(
                                if (isRepliesExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                                null, tint = NeonCyan, modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "${comment.repliesCount} repl${if (comment.repliesCount > 1) "ies" else "y"}",
                                color = NeonCyan, fontSize = 11.sp
                            )
                        }
                    } else {
                        Text("Reply", color = TextMuted, fontSize = 11.sp, modifier = Modifier.clickable { })
                    }
                }
            }
        }
    }
}

@Composable
private fun CommentInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    isSending: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BackgroundBlack)
            .imePadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text("Write a comment...", color = TextMuted, fontSize = 13.sp) },
            modifier = Modifier.weight(1f),
            textStyle = androidx.compose.ui.text.TextStyle(color = TextPrimary, fontSize = 13.sp),
            shape = RoundedCornerShape(20.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonGreen,
                unfocusedBorderColor = SurfaceElevated,
                cursorColor = NeonGreen
            ),
            maxLines = 3
        )
        Spacer(Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (value.isNotBlank()) NeonGreen else SurfaceCard)
                .clickable(enabled = value.isNotBlank() && !isSending) { onSend() },
            contentAlignment = Alignment.Center
        ) {
            if (isSending) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = BackgroundBlack, strokeWidth = 2.dp)
            else Icon(Icons.Rounded.Send, null, tint = if (value.isNotBlank()) BackgroundBlack else TextMuted, modifier = Modifier.size(20.dp))
        }
    }
}
