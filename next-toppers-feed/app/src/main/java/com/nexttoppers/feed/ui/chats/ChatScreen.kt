package com.nexttoppers.feed.ui.chats

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.AttachFile
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.EmojiEmotions
import androidx.compose.material.icons.rounded.Reply
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.nexttoppers.feed.data.model.ChatMessage
import com.nexttoppers.feed.data.model.MessageType
import com.nexttoppers.feed.ui.theme.AccentCyan
import com.nexttoppers.feed.ui.theme.AccentEmerald
import com.nexttoppers.feed.ui.theme.BackgroundBlack
import com.nexttoppers.feed.ui.theme.NeonCyan
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ChatScreen(
    onBack: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val uiState      by viewModel.uiState.collectAsState()
    val messageInput by viewModel.messageInput.collectAsState()
    val isSending    by viewModel.isSending.collectAsState()
    val replyTo      by viewModel.replyTo.collectAsState()
    val editTarget   by viewModel.editTarget.collectAsState()
    val clipboard    = LocalClipboardManager.current

    val listState = rememberLazyListState()
    val scope     = rememberCoroutineScope()

    val messages = when (val state = uiState) {
        is ChatUiState.Success -> state.messages
        else -> emptyList()
    }

    val isAtBottom by remember {
        derivedStateOf {
            val info        = listState.layoutInfo
            val lastVisible = info.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisible >= info.totalItemsCount - 2
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty() && (isAtBottom || messages.size <= 3)) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        containerColor = BackgroundBlack,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val name     = viewModel.getDisplayName()
                        val photoUrl = viewModel.getDisplayPhoto()
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(NeonGreen.copy(0.15f))
                                .border(1.5.dp, NeonGreen.copy(0.4f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (photoUrl.isNotBlank()) {
                                AsyncImage(
                                    model              = photoUrl,
                                    contentDescription = null,
                                    contentScale       = ContentScale.Crop,
                                    modifier           = Modifier.fillMaxSize().clip(CircleShape)
                                )
                            } else {
                                Text(
                                    name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                                    color      = NeonGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize   = 14.sp
                                )
                            }
                        }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                name.ifBlank { "Chat" },
                                color      = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize   = 15.sp
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(Modifier.size(6.dp).background(AccentEmerald, CircleShape))
                                Spacer(Modifier.width(4.dp))
                                Text("Online", color = AccentEmerald, fontSize = 11.sp)
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Rounded.ArrowBack, null, tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceCard)
            )
        },
        bottomBar = {
            Column {
                replyTo?.let { msg ->
                    ReplyBar(
                        senderName = msg.senderName,
                        preview    = msg.message,
                        onCancel   = { viewModel.setReplyTo(null) }
                    )
                }
                editTarget?.let { msg ->
                    EditBar(
                        preview  = msg.message,
                        onCancel = { viewModel.cancelEdit() }
                    )
                }
                ChatInputBar(
                    value         = messageInput,
                    onValueChange = viewModel::setMessageInput,
                    onSend        = viewModel::sendMessage,
                    isSending     = isSending,
                    isEditing     = editTarget != null
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when (uiState) {
                is ChatUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = NeonGreen, strokeWidth = 2.dp, modifier = Modifier.size(28.dp))
                    }
                }
                is ChatUiState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text((uiState as ChatUiState.Error).message, color = TextSecondary)
                    }
                }
                is ChatUiState.Success -> {
                    if (messages.isEmpty()) {
                        EmptyChatState(Modifier.fillMaxSize())
                    } else {
                        val grouped           = remember(messages) { groupMessagesByDate(messages) }
                        val participantPhotos = (uiState as ChatUiState.Success).chat.participantPhotos

                        LazyColumn(
                            state           = listState,
                            modifier        = Modifier.fillMaxSize(),
                            contentPadding  = PaddingValues(vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            grouped.forEach { (label, msgs) ->
                                item(key = "date_$label") {
                                    DateSeparator(label)
                                }
                                items(msgs, key = { it.messageId }) { message ->
                                    AnimatedVisibility(
                                        visible = true,
                                        enter   = fadeIn(tween(200)) + slideInVertically(tween(200)) { it / 2 }
                                    ) {
                                        MessageBubble(
                                            message        = message,
                                            isOwn          = message.senderId == viewModel.currentUid,
                                            senderPhotoUrl = participantPhotos[message.senderId] ?: "",
                                            onDelete       = { viewModel.deleteMessage(message.messageId) },
                                            onReply        = { viewModel.setReplyTo(message) },
                                            onEdit         = { viewModel.startEdit(message) },
                                            onCopy         = { clipboard.setText(AnnotatedString(message.message)) },
                                            onReport       = { viewModel.reportMessage(message.messageId) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible  = !isAtBottom && messages.isNotEmpty(),
                enter    = fadeIn(tween(200)),
                exit     = fadeOut(tween(200)),
                modifier = Modifier.align(Alignment.BottomEnd).padding(end = 16.dp, bottom = 8.dp)
            ) {
                FloatingActionButton(
                    onClick        = { scope.launch { listState.animateScrollToItem(messages.size - 1) } },
                    modifier       = Modifier.size(40.dp),
                    containerColor = NeonGreen,
                    contentColor   = BackgroundBlack
                ) {
                    Icon(Icons.Rounded.ArrowDownward, null, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MessageBubble(
    message: ChatMessage,
    isOwn: Boolean,
    senderPhotoUrl: String,
    onDelete: () -> Unit,
    onReply: () -> Unit,
    onEdit: () -> Unit,
    onCopy: () -> Unit,
    onReport: () -> Unit
) {
    if (message.deleted) {
        Row(
            modifier              = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 2.dp),
            horizontalArrangement = if (isOwn) Arrangement.End else Arrangement.Start
        ) {
            Text(
                "🚫 Message deleted",
                color    = TextMuted,
                fontSize = 11.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(SurfaceElevated)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }
        return
    }

    var showMenu by remember { mutableStateOf(false) }
    val maxWidth = (LocalConfiguration.current.screenWidthDp * 0.72f).dp
    val bubbleShape = if (isOwn) RoundedCornerShape(18.dp, 4.dp, 18.dp, 18.dp)
                     else        RoundedCornerShape(4.dp, 18.dp, 18.dp, 18.dp)
    val bubbleBrush = if (isOwn)
        Brush.linearGradient(listOf(NeonGreen.copy(0.9f), NeonCyan.copy(0.8f)))
    else
        Brush.linearGradient(listOf(SurfaceCard, SurfaceElevated))
    val textColor = if (isOwn) BackgroundBlack else TextPrimary

    Row(
        modifier              = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 3.dp),
        horizontalArrangement = if (isOwn) Arrangement.End else Arrangement.Start
    ) {
        if (!isOwn) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .align(Alignment.Bottom)
                    .clip(CircleShape)
                    .background(NeonGreen.copy(0.15f))
                    .border(1.dp, NeonGreen.copy(0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (senderPhotoUrl.isNotBlank()) {
                    AsyncImage(
                        model              = senderPhotoUrl,
                        contentDescription = null,
                        contentScale       = ContentScale.Crop,
                        modifier           = Modifier.fillMaxSize().clip(CircleShape)
                    )
                } else {
                    Text(
                        message.senderName.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                        color      = NeonGreen,
                        fontSize   = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(Modifier.width(6.dp))
        }

        Column(horizontalAlignment = if (isOwn) Alignment.End else Alignment.Start) {
            if (!isOwn && message.senderName.isNotBlank()) {
                Text(
                    message.senderName,
                    color      = NeonGreen,
                    fontSize   = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier   = Modifier.padding(start = 4.dp, bottom = 2.dp)
                )
            }

            Box {
                Box(
                    modifier = Modifier
                        .widthIn(max = maxWidth)
                        .clip(bubbleShape)
                        .background(bubbleBrush)
                        .combinedClickable(
                            onClick    = {},
                            onLongClick = { showMenu = true }
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    if (message.type == MessageType.SYSTEM.name) {
                        Text(message.message, color = TextMuted, fontSize = 11.sp, textAlign = TextAlign.Center)
                    } else {
                        Text(message.message, color = textColor, fontSize = 14.sp, lineHeight = 20.sp)
                    }
                }

                DropdownMenu(
                    expanded         = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier         = Modifier.background(SurfaceCard)
                ) {
                    DropdownMenuItem(
                        text    = { Text("Reply", color = TextPrimary, fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Rounded.Reply, null, tint = AccentCyan, modifier = Modifier.size(16.dp)) },
                        onClick = { onReply(); showMenu = false }
                    )
                    DropdownMenuItem(
                        text    = { Text("Copy", color = TextPrimary, fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Rounded.Done, null, tint = AccentCyan, modifier = Modifier.size(16.dp)) },
                        onClick = { onCopy(); showMenu = false }
                    )
                    if (isOwn) {
                        DropdownMenuItem(
                            text    = { Text("Edit", color = TextPrimary, fontSize = 13.sp) },
                            leadingIcon = { Icon(Icons.Rounded.EmojiEmotions, null, tint = NeonGreen, modifier = Modifier.size(16.dp)) },
                            onClick = { onEdit(); showMenu = false }
                        )
                        DropdownMenuItem(
                            text    = { Text("Delete", color = Color(0xFFEF5350), fontSize = 13.sp) },
                            leadingIcon = { Icon(Icons.Rounded.Close, null, tint = Color(0xFFEF5350), modifier = Modifier.size(16.dp)) },
                            onClick = { onDelete(); showMenu = false }
                        )
                    }
                    if (!isOwn) {
                        DropdownMenuItem(
                            text    = { Text("Report", color = Color(0xFFEF5350), fontSize = 13.sp) },
                            leadingIcon = { Icon(Icons.Rounded.Close, null, tint = Color(0xFFEF5350), modifier = Modifier.size(16.dp)) },
                            onClick = { onReport(); showMenu = false }
                        )
                    }
                }
            }

            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier              = Modifier.padding(top = 2.dp, start = 4.dp, end = 4.dp)
            ) {
                Text(formatMsgTime(message.timestamp.toDate()), color = TextMuted, fontSize = 10.sp)
                if (isOwn) {
                    Icon(
                        Icons.Rounded.DoneAll,
                        null,
                        tint     = if (message.seen) NeonCyan else TextMuted,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ReplyBar(senderName: String, preview: String, onCancel: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceCard)
            .border(width = 0.dp, color = Color.Transparent)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(32.dp)
                .background(AccentCyan, RoundedCornerShape(2.dp))
        )
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text("Replying to $senderName", fontSize = 11.sp, color = AccentCyan, fontWeight = FontWeight.SemiBold)
            Text(
                preview.take(80),
                fontSize = 12.sp,
                color    = TextMuted,
                maxLines = 1
            )
        }
        IconButton(onClick = onCancel, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Rounded.Close, null, tint = TextMuted, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun EditBar(preview: String, onCancel: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceCard)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(32.dp)
                .background(NeonGreen, RoundedCornerShape(2.dp))
        )
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text("Editing message", fontSize = 11.sp, color = NeonGreen, fontWeight = FontWeight.SemiBold)
            Text(preview.take(80), fontSize = 12.sp, color = TextMuted, maxLines = 1)
        }
        IconButton(onClick = onCancel, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Rounded.Close, null, tint = TextMuted, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun DateSeparator(label: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp, horizontal = 16.dp),
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
private fun ChatInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    isSending: Boolean,
    isEditing: Boolean
) {
    val sendAlpha by animateFloatAsState(
        targetValue   = if (value.isNotBlank()) 1f else 0.35f,
        animationSpec = tween(150, easing = FastOutSlowInEasing),
        label         = "send_alpha"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceCard)
            .imePadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            IconButton(onClick = {}, modifier = Modifier.size(40.dp)) {
                Icon(Icons.Rounded.EmojiEmotions, null, tint = TextMuted, modifier = Modifier.size(22.dp))
            }

            OutlinedTextField(
                value         = value,
                onValueChange = onValueChange,
                placeholder   = {
                    Text(
                        if (isEditing) "Edit message..." else "Message...",
                        color    = TextMuted,
                        fontSize = 13.sp
                    )
                },
                modifier  = Modifier.weight(1f),
                textStyle = TextStyle(color = TextPrimary, fontSize = 14.sp),
                shape     = RoundedCornerShape(20.dp),
                colors    = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = if (isEditing) NeonGreen.copy(0.6f) else NeonGreen.copy(0.5f),
                    unfocusedBorderColor = SurfaceElevated,
                    cursorColor          = NeonGreen
                ),
                maxLines  = 5
            )

            Spacer(Modifier.width(4.dp))

            if (!isEditing) {
                IconButton(onClick = {}, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Rounded.AttachFile, null, tint = TextMuted, modifier = Modifier.size(22.dp))
                }
            }

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        if (isEditing)
                            Brush.linearGradient(listOf(NeonGreen, AccentCyan))
                        else
                            Brush.linearGradient(listOf(NeonGreen, NeonCyan))
                    )
                    .alpha(sendAlpha)
                    .then(
                        if (value.isNotBlank() && !isSending)
                            Modifier.then(Modifier)
                        else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .then(
                            if (value.isNotBlank() && !isSending)
                                Modifier.combinedClickable(onClick = onSend, onLongClick = null)
                            else Modifier
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSending) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = BackgroundBlack, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Rounded.Send, null, tint = BackgroundBlack, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }

        if (value.length > 200) {
            Text(
                "${value.length}/500",
                fontSize = 10.sp,
                color    = if (value.length > 450) Color(0xFFEF5350) else TextMuted,
                modifier = Modifier.align(Alignment.End).padding(end = 14.dp, bottom = 6.dp)
            )
        }
    }
}

@Composable
private fun EmptyChatState(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("👋", fontSize = 52.sp)
            Spacer(Modifier.height(12.dp))
            Text("Say hello!", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Spacer(Modifier.height(6.dp))
            Text("Be the first to send a message", color = TextSecondary, fontSize = 13.sp)
        }
    }
}

private fun groupMessagesByDate(messages: List<ChatMessage>): List<Pair<String, List<ChatMessage>>> {
    val grouped = mutableListOf<Pair<String, List<ChatMessage>>>()
    var currentLabel = ""
    var currentGroup = mutableListOf<ChatMessage>()
    messages.forEach { msg ->
        val label = getDateLabel(msg.timestamp.toDate())
        if (label != currentLabel) {
            if (currentGroup.isNotEmpty()) grouped.add(Pair(currentLabel, currentGroup.toList()))
            currentLabel = label
            currentGroup = mutableListOf()
        }
        currentGroup.add(msg)
    }
    if (currentGroup.isNotEmpty()) grouped.add(Pair(currentLabel, currentGroup.toList()))
    return grouped
}

private fun getDateLabel(date: Date): String {
    val now    = Calendar.getInstance()
    val msgCal = Calendar.getInstance().apply { time = date }
    val isSameDay  = now.get(Calendar.DATE) == msgCal.get(Calendar.DATE) &&
            now.get(Calendar.YEAR) == msgCal.get(Calendar.YEAR)
    val isYesterday = now.get(Calendar.DATE) - msgCal.get(Calendar.DATE) == 1 &&
            now.get(Calendar.YEAR) == msgCal.get(Calendar.YEAR)
    val isSameWeek = now.get(Calendar.WEEK_OF_YEAR) == msgCal.get(Calendar.WEEK_OF_YEAR) &&
            now.get(Calendar.YEAR) == msgCal.get(Calendar.YEAR)
    return when {
        isSameDay  -> "Today"
        isYesterday -> "Yesterday"
        isSameWeek  -> SimpleDateFormat("EEEE", Locale.getDefault()).format(date)
        else        -> SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(date)
    }
}

private fun formatMsgTime(date: Date): String =
    SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
