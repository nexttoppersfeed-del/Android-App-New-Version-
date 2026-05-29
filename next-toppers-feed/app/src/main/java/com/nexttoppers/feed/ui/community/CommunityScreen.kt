package com.nexttoppers.feed.ui.community

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CommunityScreen(
    onNavigateToCreatePost: () -> Unit,
    onNavigateToPostDetail: (String) -> Unit,
    viewModel: CommunityViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val likeAnimations by viewModel.likeAnimations.collectAsState()

    Scaffold(
        containerColor = BackgroundBlack,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToCreatePost,
                icon = { Icon(Icons.Rounded.Add, null, tint = BackgroundBlack) },
                text = { Text("Post", color = BackgroundBlack, fontWeight = FontWeight.Bold) },
                containerColor = NeonGreen,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 88.dp)
        ) {
            item {
                CommunityHeader()
            }

            item {
                FilterChipRow(
                    selected = selectedFilter,
                    onFilterSelected = { viewModel.setFilter(it) }
                )
            }

            when (val state = uiState) {
                is CommunityUiState.Loading -> item {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(300.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = NeonGreen, strokeWidth = 2.dp)
                    }
                }
                is CommunityUiState.Error -> item {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(state.message, color = TextSecondary, fontSize = 14.sp)
                    }
                }
                is CommunityUiState.Success -> {
                    if (state.posts.isEmpty()) {
                        item { EmptyCommunityState(onNavigateToCreatePost) }
                    } else {
                        itemsIndexed(state.posts) { index, post ->
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn(tween(300 + index * 60)) + expandVertically(tween(300 + index * 60))
                            ) {
                                CommunityPostCard(
                                    post = post,
                                    currentUid = viewModel.currentUid,
                                    isLikeAnimating = likeAnimations.contains(post.postId),
                                    onLike = { viewModel.toggleLike(post) },
                                    onComment = { onNavigateToPostDetail(post.postId) },
                                    onClick = { onNavigateToPostDetail(post.postId) },
                                    onDelete = { viewModel.deletePost(post.postId) },
                                    onReport = { viewModel.reportPost(post.postId) }
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
private fun CommunityHeader() {
    val infiniteTransition = rememberInfiniteTransition(label = "header_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 0.7f,
        animationSpec = infiniteRepeatable(tween(2200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "glow"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    listOf(NeonGreen.copy(alpha = 0.08f), NeonCyan.copy(alpha = 0.08f))
                )
            )
            .border(1.dp, NeonGreen.copy(alpha = glowAlpha * 0.4f), RoundedCornerShape(20.dp))
            .padding(20.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Forum, null, tint = NeonGreen, modifier = Modifier.size(28.dp))
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("Community", color = TextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
                    Text("Connect · Discuss · Grow", color = TextSecondary, fontSize = 12.sp)
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatChip("🔥 Hot", NeonGreen)
                StatChip("💬 Discussions", NeonCyan)
                StatChip("⭐ Tips", PremiumGold)
            }
        }
    }
}

@Composable
private fun StatChip(label: String, color: Color) {
    Text(
        text = label,
        color = color,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}

@Composable
private fun FilterChipRow(
    selected: String?,
    onFilterSelected: (PostType?) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(bottom = 8.dp)
    ) {
        item {
            NtfFilterChip(label = "All", isSelected = selected == null) { onFilterSelected(null) }
        }
        items(PostType.values().toList()) { type ->
            NtfFilterChip(
                label = "${type.emoji} ${type.label}",
                isSelected = selected == type.name
            ) { onFilterSelected(type) }
        }
    }
}

@Composable
private fun NtfFilterChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    val bgColor by animateColorAsState(
        if (isSelected) NeonGreen else SurfaceCard, label = "chip_bg"
    )
    val textColor by animateColorAsState(
        if (isSelected) BackgroundBlack else TextSecondary, label = "chip_text"
    )
    Text(
        text = label,
        color = textColor,
        fontSize = 12.sp,
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .border(1.dp, if (isSelected) Color.Transparent else SurfaceElevated, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 7.dp)
    )
}

@Composable
fun CommunityPostCard(
    post: CommunityPost,
    currentUid: String,
    isLikeAnimating: Boolean,
    onLike: () -> Unit,
    onComment: () -> Unit,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onReport: () -> Unit
) {
    val isLiked = post.isLikedBy(currentUid)
    val likeScale by animateFloatAsState(
        targetValue = if (isLikeAnimating) 1.3f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "like_scale"
    )
    val postType = runCatching { PostType.valueOf(post.type) }.getOrNull()
    val accentColor = when (postType) {
        PostType.DOUBT       -> WarningAmber
        PostType.MOTIVATION  -> NeonGreen
        PostType.TOPPER_TIP  -> PremiumGold
        PostType.QUESTION    -> NeonCyan
        else                 -> NeonGreen.copy(alpha = 0.5f)
    }

    var showMenu by remember { mutableStateOf(false) }
    val isOwner = post.userId == currentUid

    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(SurfaceCard)
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(listOf(accentColor.copy(alpha = 0.25f), Color.Transparent)),
                    shape = RoundedCornerShape(18.dp)
                )
                .clickable { onClick() }
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                UserAvatar(name = post.username, size = 38.dp, color = accentColor)
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(post.username, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        if (post.premiumOnly) {
                            Spacer(Modifier.width(6.dp))
                            Text("★", color = PremiumGold, fontSize = 12.sp)
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        TypeBadge(postType)
                        if (post.subject.isNotBlank() && post.subject != "General") {
                            Text("· ${post.subject}", color = TextMuted, fontSize = 11.sp)
                        }
                        Text("· ${formatTime(post.createdAt.toDate())}", color = TextMuted, fontSize = 11.sp)
                    }
                }
                Row {
                    if (post.pinned) Icon(Icons.Rounded.PushPin, null, tint = PremiumGold, modifier = Modifier.size(16.dp))
                    if (post.hot)   Icon(Icons.Rounded.LocalFireDepartment, null, tint = WarningAmber, modifier = Modifier.size(16.dp))
                    Box {
                        IconButton(onClick = { showMenu = true }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Rounded.MoreVert, null, tint = TextMuted, modifier = Modifier.size(18.dp))
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier.background(SurfaceElevated)
                        ) {
                            if (isOwner) DropdownMenuItem(
                                text = { Text("Delete", color = TextPrimary, fontSize = 13.sp) },
                                onClick = { showMenu = false; onDelete() }
                            )
                            DropdownMenuItem(
                                text = { Text("Report", color = TextPrimary, fontSize = 13.sp) },
                                onClick = { showMenu = false; onReport() }
                            )
                        }
                    }
                }
            }

            if (post.title.isNotBlank()) {
                Spacer(Modifier.height(10.dp))
                Text(post.title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
            if (post.content.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(post.content, color = TextSecondary, fontSize = 13.sp, lineHeight = 20.sp, maxLines = 4, overflow = TextOverflow.Ellipsis)
            }

            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onLike() }
                ) {
                    Icon(
                        imageVector = if (isLiked) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        contentDescription = null,
                        tint = if (isLiked) NeonGreen else TextMuted,
                        modifier = Modifier.size(18.dp).scale(likeScale)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("${post.likeCount}", color = if (isLiked) NeonGreen else TextMuted, fontSize = 12.sp)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onComment() }
                ) {
                    Icon(Icons.Rounded.ChatBubbleOutline, null, tint = TextMuted, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("${post.commentsCount}", color = TextMuted, fontSize = 12.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Share, null, tint = TextMuted, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Share", color = TextMuted, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun UserAvatar(name: String, size: androidx.compose.ui.unit.Dp, color: Color) {
    val initial = name.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.18f))
            .border(1.5.dp, color.copy(alpha = 0.5f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(initial, color = color, fontWeight = FontWeight.Bold, fontSize = (size.value * 0.4f).sp)
    }
}

@Composable
private fun TypeBadge(type: PostType?) {
    if (type == null) return
    val (bg, text) = when (type) {
        PostType.DOUBT      -> WarningAmber.copy(alpha = 0.15f) to WarningAmber
        PostType.MOTIVATION -> NeonGreen.copy(alpha = 0.15f)    to NeonGreen
        PostType.TOPPER_TIP -> PremiumGold.copy(alpha = 0.15f)  to PremiumGold
        PostType.QUESTION   -> NeonCyan.copy(alpha = 0.15f)     to NeonCyan
        else                -> SurfaceElevated to TextSecondary
    }
    Text(
        text = "${type.emoji} ${type.label}",
        color = text,
        fontSize = 10.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    )
}

@Composable
private fun EmptyCommunityState(onCreatePost: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth().height(300.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("💬", fontSize = 48.sp)
            Spacer(Modifier.height(12.dp))
            Text("No discussions yet", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(Modifier.height(6.dp))
            Text("Be the first to start a discussion!", color = TextSecondary, fontSize = 14.sp)
            Spacer(Modifier.height(16.dp))
            Text(
                "Start Discussion",
                color = BackgroundBlack,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(NeonGreen)
                    .clickable { onCreatePost() }
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            )
        }
    }
}

private fun formatTime(date: Date): String {
    val now = System.currentTimeMillis()
    val diff = now - date.time
    return when {
        diff < 60_000    -> "just now"
        diff < 3_600_000 -> "${diff / 60_000}m ago"
        diff < 86_400_000 -> "${diff / 3_600_000}h ago"
        else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(date)
    }
}
