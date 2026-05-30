@file:OptIn(androidx.media3.common.util.UnstableApi::class)

package com.nexttoppers.feed.ui.lecture

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.FullscreenExit
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.Player
import androidx.media3.ui.PlayerView
import com.nexttoppers.feed.ui.theme.BackgroundBlack
import com.nexttoppers.feed.ui.theme.NeonGreen
import com.nexttoppers.feed.ui.theme.SurfaceElevated
import com.nexttoppers.feed.ui.theme.TextMuted
import com.nexttoppers.feed.ui.theme.TextPrimary
import com.nexttoppers.feed.ui.theme.TextSecondary
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

@Composable
fun LecturePlayerScreen(
    onBack: () -> Unit,
    viewModel: LecturePlayerViewModel = hiltViewModel()
) {
    val context        = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val player         = viewModel.player
    val videoUrl       = viewModel.videoUrl
    val title          = viewModel.title

    val playbackSpeed by viewModel.playbackSpeed.collectAsState()
    val isFullscreen  by viewModel.isFullscreen.collectAsState()
    val showSpeedMenu by viewModel.showSpeedMenu.collectAsState()

    val isYouTube = videoUrl.contains("youtu.be") || videoUrl.contains("youtube.com")

    var isPlaying    by remember { mutableStateOf(player.isPlaying) }
    var position     by remember { mutableLongStateOf(0L) }
    var duration     by remember { mutableLongStateOf(0L) }
    var showControls by remember { mutableStateOf(true) }
    var isSeeking    by remember { mutableStateOf(false) }
    var seekTarget   by remember { mutableLongStateOf(0L) }

    val scope   = rememberCoroutineScope()
    var hideJob by remember { mutableStateOf<Job?>(null) }

    fun scheduleHide() {
        hideJob?.cancel()
        showControls = true
        hideJob = scope.launch {
            delay(3500)
            showControls = false
        }
    }

    LaunchedEffect(Unit) { scheduleHide() }

    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) { isPlaying = playing }
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_READY && player.duration > 0) {
                    duration = player.duration
                }
            }
        }
        player.addListener(listener)
        onDispose { player.removeListener(listener) }
    }

    LaunchedEffect(Unit) {
        while (true) {
            if (!isSeeking) {
                position = player.currentPosition.coerceAtLeast(0L)
                if (player.duration > 0) duration = player.duration
            }
            delay(500)
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE  -> player.pause()
                Lifecycle.Event.ON_RESUME -> if (isPlaying) player.play()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val activity = context as? Activity
    DisposableEffect(isFullscreen) {
        activity?.requestedOrientation = if (isFullscreen)
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        else
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundBlack)
    ) {
        if (isYouTube || videoUrl.isBlank()) {
            YouTubeFallback(
                title    = title,
                videoUrl = videoUrl,
                onBack   = onBack,
                onOpen   = {
                    if (videoUrl.isNotBlank()) {
                        runCatching {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl)))
                        }
                    }
                }
            )
        } else {
            val playerView = remember {
                PlayerView(context).apply {
                    this.player = player
                    useController = false
                }
            }

            if (isFullscreen) {
                // ── Fullscreen layout ────────────────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                        .clickable(
                            indication           = null,
                            interactionSource    = remember { MutableInteractionSource() }
                        ) { scheduleHide() }
                ) {
                    AndroidView(factory = { playerView }, modifier = Modifier.fillMaxSize())
                    PlayerControlsOverlay(
                        title           = title,
                        isPlaying       = isPlaying,
                        position        = if (isSeeking) seekTarget else position,
                        duration        = duration,
                        showControls    = showControls,
                        isFullscreen    = true,
                        playbackSpeed   = playbackSpeed,
                        showSpeedMenu   = showSpeedMenu,
                        speedOptions    = viewModel.speedOptions,
                        onPlayPause     = { if (player.isPlaying) player.pause() else player.play() },
                        onSeekStart     = { val pos = (it * duration).toLong(); seekTarget = pos; isSeeking = true },
                        onSeekEnd       = { val pos = (it * duration).toLong(); player.seekTo(pos); isSeeking = false },
                        onSpeedClick    = viewModel::toggleSpeedMenu,
                        onSpeedSelect   = viewModel::setPlaybackSpeed,
                        onDismissSpeed  = viewModel::dismissSpeedMenu,
                        onFullscreen    = viewModel::toggleFullscreen,
                        onBack          = { viewModel.setFullscreen(false) },
                        modifier        = Modifier.fillMaxSize()
                    )
                }
            } else {
                // ── Portrait layout ──────────────────────────────────────────────
                Column(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                            .background(Color.Black)
                            .clickable(
                                indication        = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) { scheduleHide() }
                    ) {
                        AndroidView(factory = { playerView }, modifier = Modifier.fillMaxSize())
                        PlayerControlsOverlay(
                            title           = title,
                            isPlaying       = isPlaying,
                            position        = if (isSeeking) seekTarget else position,
                            duration        = duration,
                            showControls    = showControls,
                            isFullscreen    = false,
                            playbackSpeed   = playbackSpeed,
                            showSpeedMenu   = showSpeedMenu,
                            speedOptions    = viewModel.speedOptions,
                            onPlayPause     = { if (player.isPlaying) player.pause() else player.play() },
                            onSeekStart     = { val pos = (it * duration).toLong(); seekTarget = pos; isSeeking = true },
                            onSeekEnd       = { val pos = (it * duration).toLong(); player.seekTo(pos); isSeeking = false },
                            onSpeedClick    = viewModel::toggleSpeedMenu,
                            onSpeedSelect   = viewModel::setPlaybackSpeed,
                            onDismissSpeed  = viewModel::dismissSpeedMenu,
                            onFullscreen    = viewModel::toggleFullscreen,
                            onBack          = onBack,
                            modifier        = Modifier.fillMaxSize()
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text       = title,
                            color      = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize   = 17.sp,
                            lineHeight = 24.sp
                        )
                        Text(
                            text     = "Streaming via HLS",
                            color    = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

// ── Player controls overlay ────────────────────────────────────────────────────

@Composable
private fun PlayerControlsOverlay(
    title: String,
    isPlaying: Boolean,
    position: Long,
    duration: Long,
    showControls: Boolean,
    isFullscreen: Boolean,
    playbackSpeed: Float,
    showSpeedMenu: Boolean,
    speedOptions: List<Float>,
    onPlayPause: () -> Unit,
    onSeekStart: (Float) -> Unit,
    onSeekEnd: (Float) -> Unit,
    onSpeedClick: () -> Unit,
    onSpeedSelect: (Float) -> Unit,
    onDismissSpeed: () -> Unit,
    onFullscreen: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val fraction = if (duration > 0) (position.toFloat() / duration.toFloat()).coerceIn(0f, 1f) else 0f

    Box(modifier = modifier) {
        AnimatedVisibility(
            visible  = showControls,
            enter    = fadeIn(tween(200)),
            exit     = fadeOut(tween(300)),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0f   to Color.Black.copy(0.65f),
                            0.35f to Color.Transparent,
                            0.65f to Color.Transparent,
                            1f   to Color.Black.copy(0.80f)
                        )
                    )
            )
        }

        // ── Top bar ─────────────────────────────────────────────────────────────
        AnimatedVisibility(
            visible  = showControls,
            enter    = fadeIn(tween(200)),
            exit     = fadeOut(tween(300)),
            modifier = Modifier.align(Alignment.TopStart).fillMaxWidth()
        ) {
            Row(
                modifier          = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
                    Box(
                        modifier         = Modifier
                            .size(34.dp)
                            .background(Color.Black.copy(0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.ArrowBack, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
                Spacer(Modifier.width(6.dp))
                Text(
                    text       = title,
                    color      = Color.White,
                    fontSize   = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines   = 1,
                    modifier   = Modifier.weight(1f)
                )
            }
        }

        // ── Centre play/pause ────────────────────────────────────────────────────
        AnimatedVisibility(
            visible  = showControls,
            enter    = fadeIn(tween(200)),
            exit     = fadeOut(tween(300)),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(NeonGreen.copy(0.20f), CircleShape)
                    .border(1.5.dp, NeonGreen.copy(0.60f), CircleShape)
                    .clickable(
                        indication        = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { onPlayPause() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    null,
                    tint     = NeonGreen,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        // ── Bottom controls ──────────────────────────────────────────────────────
        AnimatedVisibility(
            visible  = showControls,
            enter    = fadeIn(tween(200)),
            exit     = fadeOut(tween(300)),
            modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
                // Seek bar
                Slider(
                    value         = fraction,
                    onValueChange = { onSeekStart(it) },
                    onValueChangeFinished = { onSeekEnd(fraction) },
                    modifier      = Modifier.fillMaxWidth(),
                    colors        = SliderDefaults.colors(
                        thumbColor            = NeonGreen,
                        activeTrackColor      = NeonGreen,
                        inactiveTrackColor    = Color.White.copy(0.25f)
                    )
                )

                // Time + buttons row
                Row(
                    modifier          = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text     = "${formatMs(position)} / ${formatMs(duration)}",
                        color    = Color.White.copy(0.85f),
                        fontSize = 11.sp
                    )
                    Spacer(Modifier.weight(1f))

                    // Speed button
                    Box {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.White.copy(0.15f))
                                .clickable { onSpeedClick() }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Rounded.Speed, null,
                                    tint     = NeonGreen,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text     = "${playbackSpeed}x".removeSuffix(".0x") + "x",
                                    color    = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Speed dropdown
                        if (showSpeedMenu) {
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .background(
                                        Color(0xFF1A1A2E),
                                        RoundedCornerShape(10.dp)
                                    )
                                    .border(1.dp, NeonGreen.copy(0.3f), RoundedCornerShape(10.dp))
                                    .padding(vertical = 4.dp)
                            ) {
                                speedOptions.forEach { speed ->
                                    val selected = speed == playbackSpeed
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                if (selected) NeonGreen.copy(0.15f) else Color.Transparent
                                            )
                                            .clickable { onSpeedSelect(speed) }
                                            .padding(horizontal = 18.dp, vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text       = "${speed}x".removeSuffix(".0x") + "x",
                                            color      = if (selected) NeonGreen else Color.White,
                                            fontSize   = 13.sp,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.width(8.dp))

                    // Fullscreen button
                    IconButton(onClick = onFullscreen, modifier = Modifier.size(32.dp)) {
                        Icon(
                            if (isFullscreen) Icons.Rounded.FullscreenExit else Icons.Rounded.Fullscreen,
                            null,
                            tint     = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

// ── YouTube fallback ───────────────────────────────────────────────────────────

@Composable
private fun YouTubeFallback(
    title: String,
    videoUrl: String,
    onBack: () -> Unit,
    onOpen: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector        = Icons.Rounded.OpenInNew,
                contentDescription = null,
                tint               = NeonGreen,
                modifier           = Modifier.size(64.dp)
            )
            Spacer(Modifier.height(20.dp))
            Text(
                text       = title,
                color      = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize   = 18.sp,
                textAlign  = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text       = "This lecture is hosted on YouTube. Tap below to watch it.",
                color      = TextSecondary,
                fontSize   = 13.sp,
                textAlign  = TextAlign.Center,
                lineHeight = 20.sp
            )

            if (videoUrl.isBlank()) {
                Spacer(Modifier.height(12.dp))
                Icon(Icons.Rounded.Error, null, tint = Color(0xFFFF6B6B), modifier = Modifier.size(24.dp))
                Text("No video URL found", color = TextMuted, fontSize = 12.sp)
            } else {
                Spacer(Modifier.height(28.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(NeonGreen.copy(0.12f))
                        .border(1.dp, NeonGreen.copy(0.5f), RoundedCornerShape(14.dp))
                        .clickable(onClick = onOpen),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.OpenInNew, null, tint = NeonGreen, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text       = "Open in YouTube",
                            color      = NeonGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize   = 15.sp
                        )
                    }
                }
            }
        }

        IconButton(
            onClick  = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 52.dp, start = 12.dp)
        ) {
            Box(
                modifier         = Modifier
                    .size(36.dp)
                    .background(SurfaceElevated.copy(0.9f), RoundedCornerShape(50.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.ArrowBack, null, tint = TextPrimary, modifier = Modifier.size(18.dp))
            }
        }
    }
}

// ── Helpers ────────────────────────────────────────────────────────────────────

private fun formatMs(ms: Long): String {
    val total = ms.coerceAtLeast(0L)
    val h = TimeUnit.MILLISECONDS.toHours(total)
    val m = TimeUnit.MILLISECONDS.toMinutes(total) % 60
    val s = TimeUnit.MILLISECONDS.toSeconds(total) % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s)
    else "%02d:%02d".format(m, s)
}
