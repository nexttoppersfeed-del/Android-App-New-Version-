package com.nexttoppers.feed.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nexttoppers.feed.R
import com.nexttoppers.feed.ui.components.NtfPrimaryButton
import com.nexttoppers.feed.ui.components.NeonDivider
import com.nexttoppers.feed.ui.theme.BackgroundBlack
import com.nexttoppers.feed.ui.theme.NeonCyan
import com.nexttoppers.feed.ui.theme.NeonGreen
import com.nexttoppers.feed.ui.theme.SurfaceCard
import com.nexttoppers.feed.ui.theme.TextMuted
import com.nexttoppers.feed.ui.theme.TextPrimary
import com.nexttoppers.feed.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { visible = true }

    LaunchedEffect(uiState) {
        when (uiState) {
            is AuthUiState.Success -> onLoginSuccess()
            is AuthUiState.Error -> {
                scope.launch {
                    snackbarHostState.showSnackbar((uiState as AuthUiState.Error).message)
                }
                viewModel.resetState()
            }
            else -> Unit
        }
    }

    val isLoading = uiState is AuthUiState.Loading

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundBlack)
    ) {
        // Ambient glow
        Box(
            modifier = Modifier
                .size(500.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.radialGradient(
                        listOf(NeonGreen.copy(0.07f), Color.Transparent)
                    )
                )
        )

        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(600)) + slideInVertically(
                tween(600, easing = FastOutSlowInEasing)
            ) { it / 4 },
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(28.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(80.dp))

                // Hero section
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "NT",
                        style = TextStyle(
                            fontSize = 80.sp,
                            fontWeight = FontWeight.ExtraBold,
                            brush = Brush.linearGradient(listOf(NeonGreen, NeonCyan)),
                            shadow = Shadow(
                                color = NeonGreen.copy(0.7f),
                                offset = Offset.Zero,
                                blurRadius = 24f
                            )
                        )
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "Next Toppers Feed",
                        style = TextStyle(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            brush = Brush.linearGradient(listOf(NeonGreen, NeonCyan))
                        )
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "The premium platform for India's\nnext top achievers",
                        fontSize = 15.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                }

                // Features teaser
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    FeaturePill("📚  Premium Notes & Lectures")
                    FeaturePill("🏆  Live Leaderboards & XP")
                    FeaturePill("💬  Real-time Study Chats")
                    FeaturePill("🧪  Adaptive Tests & Quizzes")
                }

                // Sign-in
                Column {
                    NeonDivider(Modifier.padding(bottom = 24.dp))

                    NtfPrimaryButton(
                        text = "Continue with Google",
                        isLoading = isLoading,
                        onClick = { viewModel.signInWithGoogle(context) }
                    )

                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = "By continuing you agree to our Terms of Service\nand Privacy Policy",
                        fontSize = 11.sp,
                        color = TextMuted,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(24.dp))
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }
}

@Composable
private fun FeaturePill(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceCard, RoundedCornerShape(14.dp))
            .border(1.dp, NeonGreen.copy(0.18f), RoundedCornerShape(14.dp))
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Text(text = text, color = TextPrimary, fontSize = 14.sp)
    }
}
