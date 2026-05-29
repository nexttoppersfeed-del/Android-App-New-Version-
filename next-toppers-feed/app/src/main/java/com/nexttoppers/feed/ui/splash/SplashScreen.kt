package com.nexttoppers.feed.ui.splash

import android.net.Uri
import android.widget.VideoView
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.nexttoppers.feed.R
import com.nexttoppers.feed.ui.theme.BackgroundBlack
import com.nexttoppers.feed.ui.theme.NeonCyan
import com.nexttoppers.feed.ui.theme.NeonGreen
import com.nexttoppers.feed.ui.theme.TextSecondary

@Composable
fun SplashScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val videoFinished by viewModel.videoFinished.collectAsState()
    val context = LocalContext.current

    val hasVideo = remember {
        context.resources.getIdentifier("splash", "raw", context.packageName) != 0
    }

    LaunchedEffect(videoFinished, state) {
        if (videoFinished && state != SplashState.Loading) {
            when (state) {
                is SplashState.Authenticated   -> onNavigateToHome()
                is SplashState.Unauthenticated -> onNavigateToLogin()
                else -> Unit
            }
        }
    }

    if (hasVideo) {
        VideoSplash(
            onVideoComplete = { viewModel.onVideoFinished() }
        )
    } else {
        AnimatedLogoSplash(
            onComplete = { viewModel.onVideoFallback() }
        )
    }
}

// ── MP4 video splash ─────────────────────────────────────────────────────────
@Composable
private fun VideoSplash(onVideoComplete: () -> Unit) {
    val context = LocalContext.current
    var fadeOut by remember { mutableStateOf(false) }

    val alpha by animateFloatAsState(
        targetValue = if (fadeOut) 0f else 1f,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        finishedListener = { value -> if (value == 0f) onVideoComplete() },
        label = "videoFade"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundBlack)
            .alpha(alpha)
    ) {
        AndroidView(
            factory = { ctx ->
                VideoView(ctx).apply {
                    setOnPreparedListener { mp ->
                        mp.isLooping = false
                        mp.setVolume(1f, 1f)
                        start()
                    }
                    setOnCompletionListener {
                        fadeOut = true
                    }
                    setOnErrorListener { _, _, _ ->
                        fadeOut = true
                        true
                    }
                    val rawId = ctx.resources.getIdentifier("splash", "raw", ctx.packageName)
                    setVideoURI(Uri.parse("android.resource://${ctx.packageName}/$rawId"))
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}

// ── Animated logo fallback (used when no MP4 is bundled) ────────────────────
@Composable
private fun AnimatedLogoSplash(onComplete: () -> Unit) {
    var animationStarted by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        animationStarted = true
        kotlinx.coroutines.delay(2200)
        onComplete()
    }

    val logoAlpha by animateFloatAsState(
        targetValue  = if (animationStarted) 1f else 0f,
        animationSpec = tween(900, easing = FastOutSlowInEasing),
        label = "logoAlpha"
    )
    val logoScale by animateFloatAsState(
        targetValue  = if (animationStarted) 1f else 0.65f,
        animationSpec = tween(900, easing = FastOutSlowInEasing),
        label = "logoScale"
    )
    val taglineAlpha by animateFloatAsState(
        targetValue  = if (animationStarted) 1f else 0f,
        animationSpec = tween(700, delayMillis = 700, easing = FastOutSlowInEasing),
        label = "taglineAlpha"
    )

    Box(
        modifier = Modifier.fillMaxSize().background(BackgroundBlack),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(340.dp)
                .background(
                    Brush.radialGradient(
                        listOf(NeonGreen.copy(0.14f), NeonCyan.copy(0.07f), Color.Transparent)
                    )
                )
                .blur(50.dp)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .scale(logoScale)
                    .alpha(logoAlpha)
            ) {
                AsyncImage(
                    model              = R.drawable.ntf_logo,
                    contentDescription = "Next Toppers Feed",
                    contentScale       = ContentScale.Fit,
                    modifier           = Modifier.fillMaxSize()
                )
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text  = "Next Toppers Feed",
                style = TextStyle(
                    fontSize   = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    brush      = Brush.linearGradient(listOf(NeonGreen, NeonCyan)),
                    shadow     = Shadow(NeonCyan.copy(0.5f), Offset.Zero, 18f)
                ),
                modifier = Modifier.alpha(logoAlpha).scale(logoScale)
            )

            Spacer(Modifier.height(10.dp))

            Text(
                text      = "Your Premium Learning Universe",
                fontSize  = 14.sp,
                color     = TextSecondary,
                textAlign = TextAlign.Center,
                modifier  = Modifier.alpha(taglineAlpha)
            )
        }
    }
}
