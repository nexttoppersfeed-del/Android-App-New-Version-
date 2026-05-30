package com.nexttoppers.feed.ui.chats

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
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
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nexttoppers.feed.data.model.ChatMessage
import com.nexttoppers.feed.data.model.MessageType
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    onBack: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {

    val uiState      by viewModel.uiState.collectAsState()
    val messageInput by viewModel.messageInput.collectAsState()
    val isSending    by viewModel.isSending.collectAsState()

    val listState = rememberLazyListState()
    val scope     = rememberCoroutineScope()

    val messages = when (val state = uiState) {
        is ChatUiState.Success -> state.messages
        else -> emptyList()
    }

    val isAtBottom by remember {
        derivedStateOf {
            val info = listState.layoutInfo
            val lastVisible = info.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisible >= info.totalItemsCount - 2
        }
    }

    // Auto-scroll to bottom when new messages arrive and user is already at the bottom
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty() && (isAtBottom || messages.size <= 2)) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        containerColor = BackgroundBlack,

        topBar = {
            TopAppBar(
                title = {

                    Row(verticalAlignment = Alignment.CenterVertically) {

                        val name = viewModel.getDisplayName()

                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(NeonGreen.copy(alpha = 0.15f))
                                .border(
                                    1.5.dp,
                                    NeonGreen.copy(alpha = 0.4f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                                color = NeonGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = name.ifBlank { "Chat" },
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )

                            Text(
                                text = "Online",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                },

                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBack,
                            contentDescription = null,
                            tint = TextPrimary
                        )
                    }
                },

                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceCard
                )
            )
        },

        bottomBar = {
            ChatInputBar(
                value = messageInput,
                onValueChange = viewModel::setMessageInput,
                onSend = viewModel::sendMessage,
                isSending = isSending
            )
        }
    ) { paddingValues ->

        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when (uiState) {

                is ChatUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = NeonGreen, strokeWidth = 2.dp)
                    }
                }

                is ChatUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = (uiState as ChatUiState.Error).message, color = TextSecondary)
                    }
                }

                is ChatUiState.Success -> {
                    if (messages.isEmpty()) {
                        EmptyChatState(modifier = Modifier.fillMaxSize())
                    } else {
                        val groupedMessages = remember(messages) { groupMessagesByDate(messages) }

                        LazyColumn(
                            state           = listState,
                            modifier        = Modifier.fillMaxSize(),
                            contentPadding  = PaddingValues(vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            groupedMessages.forEach { (label, msgs) ->
                                item(key = "date_$label") {
                                    DateSeparator(label = label)
                                }
                                items(items = msgs, key = { it.messageId }) { message ->
                                    AnimatedVisibility(
                                        visible = true,
                                        enter   = fadeIn(tween(220)) + slideInVertically(tween(220)) { it / 2 }
                                    ) {
                                        MessageBubble(
                                            message = message,
                                            isOwn   = message.senderId == viewModel.currentUid
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Scroll-to-bottom FAB
            AnimatedVisibility(
                visible = !isAtBottom && messages.isNotEmpty(),
                enter   = fadeIn(tween(200)),
                exit    = fadeOut(tween(200)),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 8.dp)
            ) {
                FloatingActionButton(
                    onClick           = { scope.launch { listState.animateScrollToItem(messages.size - 1) } },
                    modifier          = Modifier.size(40.dp),
                    containerColor    = NeonGreen,
                    contentColor      = BackgroundBlack
                ) {
                    Icon(
                        Icons.Rounded.ArrowDownward,
                        contentDescription = "Scroll to bottom",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(
    message: ChatMessage,
    isOwn: Boolean
) {

    if (message.deleted) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 2.dp),

            horizontalArrangement =
            if (isOwn) Arrangement.End
            else Arrangement.Start
        ) {

            Text(
                text = "🚫 This message was deleted",
                color = TextMuted,
                fontSize = 11.sp,

                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(SurfaceElevated)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }

        return
    }

    val maxWidth =
        (LocalConfiguration.current.screenWidthDp * 0.72f).dp

    val bubbleShape =
        if (isOwn) {
            RoundedCornerShape(18.dp, 4.dp, 18.dp, 18.dp)
        } else {
            RoundedCornerShape(4.dp, 18.dp, 18.dp, 18.dp)
        }

    val bubbleBrush =
        if (isOwn) {
            Brush.linearGradient(
                listOf(
                    NeonGreen.copy(alpha = 0.9f),
                    NeonCyan.copy(alpha = 0.8f)
                )
            )
        } else {
            Brush.linearGradient(
                listOf(
                    SurfaceCard,
                    SurfaceElevated
                )
            )
        }

    val textColor =
        if (isOwn) BackgroundBlack
        else TextPrimary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 3.dp),

        horizontalArrangement =
        if (isOwn) Arrangement.End
        else Arrangement.Start
    ) {

        if (!isOwn) {

            Box(
                modifier = Modifier
                    .size(28.dp)
                    .align(Alignment.Bottom)
                    .clip(CircleShape)
                    .background(NeonGreen.copy(alpha = 0.15f))
                    .border(
                        1.dp,
                        NeonGreen.copy(alpha = 0.3f),
                        CircleShape
                    ),

                contentAlignment = Alignment.Center
            ) {

                Text(
                    text = message.senderName
                        .firstOrNull()
                        ?.uppercaseChar()
                        ?.toString() ?: "?",

                    color = NeonGreen,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(6.dp))
        }

        Column(
            horizontalAlignment =
            if (isOwn) Alignment.End
            else Alignment.Start
        ) {

            if (!isOwn && message.senderName.isNotBlank()) {

                Text(
                    text = message.senderName,
                    color = NeonGreen,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                )
            }

            Box(
                modifier = Modifier
                    .widthIn(max = maxWidth)
                    .clip(bubbleShape)
                    .background(bubbleBrush)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {

                if (message.type == MessageType.SYSTEM.name) {

                    Text(
                        text = message.message,
                        color = TextMuted,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )

                } else {

                    Text(
                        text = message.message,
                        color = textColor,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),

                modifier = Modifier.padding(
                    top = 2.dp,
                    start = 4.dp,
                    end = 4.dp
                )
            ) {

                Text(
                    text = formatMessageTime(message.timestamp.toDate()),
                    color = TextMuted,
                    fontSize = 10.sp
                )

                if (isOwn) {

                    Icon(
                        imageVector = Icons.Rounded.DoneAll,
                        contentDescription = null,
                        tint =
                        if (message.seen) NeonCyan
                        else TextMuted,

                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DateSeparator(label: String) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 16.dp),

        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .weight(1f)
                .height(0.5.dp)
                .background(SurfaceElevated)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = label,
            color = TextMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,

            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(SurfaceCard)
                .padding(horizontal = 10.dp, vertical = 4.dp)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .height(0.5.dp)
                .background(SurfaceElevated)
        )
    }
}

@Composable
private fun ChatInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    isSending: Boolean
) {

    val sendAlpha by animateFloatAsState(
        targetValue =
        if (value.isNotBlank()) 1f else 0.4f,

        animationSpec = tween(
            durationMillis = 150,
            easing = FastOutSlowInEasing
        ),

        label = "send_alpha"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceCard)
            .imePadding()
            .padding(horizontal = 12.dp, vertical = 10.dp),

        verticalAlignment = Alignment.Bottom
    ) {

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,

            placeholder = {
                Text(
                    text = "Message...",
                    color = TextMuted,
                    fontSize = 13.sp
                )
            },

            modifier = Modifier.weight(1f),

            textStyle = TextStyle(
                color = TextPrimary,
                fontSize = 14.sp
            ),

            shape = RoundedCornerShape(20.dp),

            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonGreen.copy(alpha = 0.5f),
                unfocusedBorderColor = SurfaceElevated,
                cursorColor = NeonGreen
            ),

            maxLines = 5
        )

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(NeonGreen, NeonCyan)
                    )
                )
                .alpha(sendAlpha)
                .clickable(
                    enabled = value.isNotBlank() && !isSending
                ) {
                    onSend()
                },

            contentAlignment = Alignment.Center
        ) {

            if (isSending) {

                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = BackgroundBlack,
                    strokeWidth = 2.dp
                )

            } else {

                Icon(
                    imageVector = Icons.Rounded.Send,
                    contentDescription = null,
                    tint = BackgroundBlack,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun EmptyChatState(
    modifier: Modifier = Modifier
) {

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "👋",
                fontSize = 52.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Say hello!",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Be the first to send a message",
                color = TextSecondary,
                fontSize = 13.sp
            )
        }
    }
}

private fun groupMessagesByDate(
    messages: List<ChatMessage>
): List<Pair<String, List<ChatMessage>>> {

    val grouped =
        mutableListOf<Pair<String, List<ChatMessage>>>()

    var currentLabel = ""

    var currentGroup =
        mutableListOf<ChatMessage>()

    messages.forEach { msg ->

        val label =
            getDateLabel(msg.timestamp.toDate())

        if (label != currentLabel) {

            if (currentGroup.isNotEmpty()) {

                grouped.add(
                    currentLabel to currentGroup.toList()
                )
            }

            currentLabel = label
            currentGroup = mutableListOf()
        }

        currentGroup.add(msg)
    }

    if (currentGroup.isNotEmpty()) {
        grouped.add(currentLabel to currentGroup.toList())
    }

    return grouped
}

private fun getDateLabel(date: Date): String {

    val cal = Calendar.getInstance()
    val now = Calendar.getInstance()

    cal.time = date

    return when {

        isSameDay(cal, now) -> {
            "Today"
        }

        isYesterday(cal, now) -> {
            "Yesterday"
        }

        else -> {
            SimpleDateFormat(
                "MMMM d, yyyy",
                Locale.getDefault()
            ).format(date)
        }
    }
}

private fun isSameDay(
    a: Calendar,
    b: Calendar
): Boolean {

    return a.get(Calendar.YEAR) ==
            b.get(Calendar.YEAR) &&
            a.get(Calendar.DAY_OF_YEAR) ==
            b.get(Calendar.DAY_OF_YEAR)
}

private fun isYesterday(
    a: Calendar,
    now: Calendar
): Boolean {

    val yesterday =
        now.clone() as Calendar

    yesterday.add(
        Calendar.DAY_OF_YEAR,
        -1
    )

    return isSameDay(a, yesterday)
}

private fun formatMessageTime(
    date: Date
): String {

    return SimpleDateFormat(
        "h:mm a",
        Locale.getDefault()
    ).format(date)
}
