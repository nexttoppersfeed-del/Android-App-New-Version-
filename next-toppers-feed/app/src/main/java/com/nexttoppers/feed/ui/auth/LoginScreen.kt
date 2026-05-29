package com.nexttoppers.feed.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nexttoppers.feed.ui.components.NtfPrimaryButton
import com.nexttoppers.feed.ui.theme.AccentCyan
import com.nexttoppers.feed.ui.theme.AccentViolet
import com.nexttoppers.feed.ui.theme.BackgroundBlack
import com.nexttoppers.feed.ui.theme.NeonCyan
import com.nexttoppers.feed.ui.theme.TextMuted
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
        // Ambient glow — AccentCyan top, AccentViolet bottom-right
        Box(
            modifier = Modifier
                .size(520.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.radialGradient(
                        listOf(AccentCyan.copy(0.09f), Color.Transparent)
                    )
                )
        )
        Box(
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.BottomEnd)
                .background(
                    Brush.radialGradient(
                        listOf(AccentViolet.copy(0.07f), Color.Transparent)
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

                // ── Hero ────────────────────────────────────────────────────────
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // "NT" monogram with cyan→violet gradient
                    Text(
                        text = "NT",
                        style = TextStyle(
                            fontSize   = 80.sp,
                            fontWeight = FontWeight.ExtraBold,
                            brush      = Brush.linearGradient(
                                listOf(AccentCyan, AccentViolet)
                            ),
                            shadow = Shadow(
                                color      = AccentCyan.copy(0.6f),
                                offset     = Offset.Zero,
                                blurRadius = 28f
                            )
                        )
                    )

                    Text(
                        text = "Next Toppers Feed",
                        style = TextStyle(
                            fontSize   = 24.sp,
                            fontWeight = FontWeight.Bold,
                            brush      = Brush.linearGradient(
                                listOf(AccentCyan, AccentViolet)
                            )
                        )
                    )

                    Text(
                        text = "The premium platform for India's\nnext top achievers",
                        fontSize  = 15.sp,
                        color     = TextSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )

                    // Subtle stats row
                    Text(
                        text = "📚 Notes  ·  🏆 Leaderboards  ·  💬 Study Chats  ·  🧪 Tests",
                        fontSize  = 12.sp,
                        color     = NeonCyan.copy(0.55f),
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }

                // ── Sign-in ──────────────────────────────────────────────────────
                Column {
                    // Divider line
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color.Transparent,
                                        AccentCyan.copy(0.35f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    Spacer(Modifier.height(28.dp))

                    NtfPrimaryButton(
                        text      = "Continue with Google",
                        isLoading = isLoading,
                        onClick   = { viewModel.signInWithGoogle(context) }
                    )

                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = "By continuing you agree to our Terms of Service\nand Privacy Policy",
                        fontSize  = 11.sp,
                        color     = TextMuted,
                        textAlign = TextAlign.Center,
                        modifier  = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(24.dp))
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier  = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }
}
