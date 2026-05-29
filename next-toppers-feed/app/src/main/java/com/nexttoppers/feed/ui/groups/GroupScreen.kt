package com.nexttoppers.feed.ui.groups

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.nexttoppers.feed.data.model.Group
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

@Composable
fun GroupListScreen(
    onNavigateToGroup: (String) -> Unit = {},
    viewModel: GroupViewModel = hiltViewModel()
) {
    val listState by viewModel.listState.collectAsState()
    val joinSuccess by viewModel.joinSuccess.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(joinSuccess) {
        if (joinSuccess != null) {
            snackbarHostState.showSnackbar("✅ Joined group successfully!")
            viewModel.clearJoinSuccess()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(BackgroundBlack)) {
        when (listState) {
            is GroupListUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = NeonGreen, strokeWidth = 2.dp)
            }
            is GroupListUiState.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text((listState as GroupListUiState.Error).message, color = TextSecondary)
            }
            is GroupListUiState.Success -> {
                val groups = (listState as GroupListUiState.Success).groups
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    item {
                        GroupsHeader()
                    }
                    itemsIndexed(groups) { index, group ->
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(tween(200 + index * 60)) + expandVertically(tween(200 + index * 60))
                        ) {
                            GroupCard(
                                group = group,
                                currentUid = viewModel.currentUid,
                                onJoin = { viewModel.joinGroup(group.groupId) },
                                onLeave = { viewModel.leaveGroup(group.groupId) },
                                onClick = { onNavigateToGroup(group.groupId) }
                            )
                        }
                    }
                }
            }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailScreen(
    onBack: () -> Unit,
    viewModel: GroupViewModel = hiltViewModel()
) {
    val detailState by viewModel.detailState.collectAsState()
    val joinSuccess by viewModel.joinSuccess.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(joinSuccess) {
        if (joinSuccess != null) {
            snackbarHostState.showSnackbar("✅ Joined group!")
            viewModel.clearJoinSuccess()
        }
    }

    Scaffold(
        containerColor = BackgroundBlack,
        topBar = {
            TopAppBar(
                title = {
                    val name = (detailState as? GroupDetailUiState.Success)?.group?.groupName ?: "Group"
                    Text(name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, null, tint = TextPrimary) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundBlack)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        when (detailState) {
            is GroupDetailUiState.Loading -> Box(
                Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(color = NeonGreen, strokeWidth = 2.dp) }

            is GroupDetailUiState.Error -> Box(
                Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center
            ) { Text((detailState as GroupDetailUiState.Error).message, color = TextSecondary) }

            is GroupDetailUiState.Success -> {
                val group = (detailState as GroupDetailUiState.Success).group
                val isMember = group.isMember(viewModel.currentUid)

                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    item { GroupDetailHeader(group = group, isMember = isMember, onJoin = { viewModel.joinGroup(group.groupId) }, onLeave = { viewModel.leaveGroup(group.groupId) }) }
                    item {
                        Spacer(Modifier.height(16.dp))
                        Text("Members (${group.memberCount})", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                    item {
                        Spacer(Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(SurfaceCard)
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Rounded.Groups, null, tint = TextMuted, modifier = Modifier.size(32.dp))
                                Spacer(Modifier.height(6.dp))
                                Text("${group.memberCount} members", color = TextMuted, fontSize = 13.sp)
                            }
                        }
                    }
                    item {
                        Spacer(Modifier.height(16.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(SurfaceCard)
                                .border(1.dp, SurfaceElevated, RoundedCornerShape(14.dp))
                                .padding(16.dp)
                        ) {
                            Column {
                                Text("Group Rules", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                Spacer(Modifier.height(8.dp))
                                listOf("Be respectful to all members", "No spam or self-promotion", "Stay on topic", "Share quality content").forEach { rule ->
                                    Row(verticalAlignment = Alignment.Top, modifier = Modifier.padding(vertical = 3.dp)) {
                                        Text("•", color = NeonGreen, fontSize = 13.sp, modifier = Modifier.padding(end = 8.dp, top = 1.dp))
                                        Text(rule, color = TextSecondary, fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GroupsHeader() {
    val infiniteTransition = rememberInfiniteTransition(label = "groups_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f, targetValue = 0.5f,
        animationSpec = infiniteRepeatable(tween(2500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "glow"
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.linearGradient(listOf(NeonCyan.copy(alpha = 0.08f), NeonGreen.copy(alpha = 0.05f))))
            .border(1.dp, NeonCyan.copy(alpha = glowAlpha), RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Rounded.Groups, null, tint = NeonCyan, modifier = Modifier.size(28.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text("Study Groups", color = TextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
            Text("Join subject groups and collaborate", color = TextSecondary, fontSize = 12.sp)
        }
    }
}

@Composable
private fun GroupCard(
    group: Group,
    currentUid: String,
    onJoin: () -> Unit,
    onLeave: () -> Unit,
    onClick: () -> Unit
) {
    val isMember = group.isMember(currentUid)
    val isPremium = group.premiumOnly
    val accentColor = when {
        isPremium         -> PremiumGold
        group.subject == "Science" -> NeonCyan
        group.subject == "Maths"   -> NeonGreen
        else              -> NeonCyan.copy(alpha = 0.7f)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(SurfaceCard)
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(listOf(accentColor.copy(alpha = 0.3f), Color.Transparent)),
                shape = RoundedCornerShape(18.dp)
            )
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(accentColor.copy(alpha = 0.15f))
                    .border(1.5.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(group.emoji.ifBlank { "👥" }, fontSize = 24.sp)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(group.groupName, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    if (isPremium) {
                        Spacer(Modifier.width(6.dp))
                        Icon(Icons.Rounded.Star, null, tint = PremiumGold, modifier = Modifier.size(14.dp))
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Person, null, tint = TextMuted, modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(3.dp))
                        Text("${group.memberCount} members", color = TextMuted, fontSize = 12.sp)
                    }
                    if (group.subject.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(accentColor.copy(alpha = 0.12f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(group.subject, color = accentColor, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        if (group.description.isNotBlank()) {
            Spacer(Modifier.height(10.dp))
            Text(group.description, color = TextSecondary, fontSize = 12.sp, lineHeight = 18.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }

        if (isPremium) {
            Spacer(Modifier.height(10.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(PremiumGold.copy(alpha = 0.1f))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Rounded.Lock, null, tint = PremiumGold, modifier = Modifier.size(12.dp))
                Spacer(Modifier.width(6.dp))
                Text("Premium Members Only", color = PremiumGold, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
        } else {
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (isMember) {
                    Text(
                        "✓ Joined",
                        color = NeonGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(NeonGreen.copy(alpha = 0.12f))
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                    )
                    Text(
                        "Leave",
                        color = WarningAmber,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(WarningAmber.copy(alpha = 0.08f))
                            .clickable { onLeave() }
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                    )
                } else {
                    Text(
                        "Join Group",
                        color = BackgroundBlack,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(accentColor)
                            .clickable { onJoin() }
                            .padding(horizontal = 20.dp, vertical = 7.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun GroupDetailHeader(group: Group, isMember: Boolean, onJoin: () -> Unit, onLeave: () -> Unit) {
    val isPremium = group.premiumOnly
    val accentColor = if (isPremium) PremiumGold else NeonCyan

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(listOf(accentColor.copy(alpha = 0.12f), accentColor.copy(alpha = 0.04f)))
            )
            .border(1.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
            .padding(20.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(accentColor.copy(alpha = 0.15f))
                    .border(2.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(group.emoji.ifBlank { "👥" }, fontSize = 36.sp)
            }
            Spacer(Modifier.height(12.dp))
            Text(group.groupName, color = TextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
            if (group.subject.isNotBlank()) {
                Text(group.subject, color = accentColor, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
            if (group.description.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(group.description, color = TextSecondary, fontSize = 13.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${group.memberCount}", color = accentColor, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                    Text("Members", color = TextMuted, fontSize = 11.sp)
                }
            }
            Spacer(Modifier.height(14.dp))
            if (!isPremium) {
                if (isMember) {
                    Text(
                        "✓ Member · Leave Group",
                        color = WarningAmber,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(WarningAmber.copy(alpha = 0.1f))
                            .clickable { onLeave() }
                            .padding(horizontal = 24.dp, vertical = 10.dp)
                    )
                } else {
                    Text(
                        "Join Group",
                        color = BackgroundBlack,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(accentColor)
                            .clickable { onJoin() }
                            .padding(horizontal = 32.dp, vertical = 12.dp)
                    )
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(PremiumGold.copy(alpha = 0.1f))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Rounded.Lock, null, tint = PremiumGold, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Premium Members Only", color = PremiumGold, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
