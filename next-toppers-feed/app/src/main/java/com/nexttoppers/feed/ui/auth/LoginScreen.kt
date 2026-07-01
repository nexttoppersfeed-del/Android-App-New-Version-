package com.nexttoppers.feed.ui.auth

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Quiz
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nexttoppers.feed.ui.theme.AccentCyan
import com.nexttoppers.feed.ui.theme.AccentEmerald
import com.nexttoppers.feed.ui.theme.AccentViolet
import com.nexttoppers.feed.ui.theme.BackgroundBlack
import com.nexttoppers.feed.ui.theme.NeonCyan
import com.nexttoppers.feed.ui.theme.SurfaceCard
import com.nexttoppers.feed.ui.theme.SurfaceElevated
import com.nexttoppers.feed.ui.theme.TextMuted
import com.nexttoppers.feed.ui.theme.TextPrimary
import com.nexttoppers.feed.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState.collectAsState().value
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

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
        // Ambient radial glows
        Box(
            modifier = Modifier
                .size(400.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.radialGradient(
                        listOf(AccentCyan.copy(0.08f), Color.Transparent)
                    )
                )
        )
        Box(
            modifier = Modifier
                .size(350.dp)
                .align(Alignment.BottomStart)
                .background(
                    Brush.radialGradient(
                        listOf(AccentViolet.copy(0.06f), Color.Transparent)
                    )
                )
        )
        Box(
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.BottomEnd)
                .background(
                    Brush.radialGradient(
                        listOf(AccentEmerald.copy(0.05f), Color.Transparent)
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp)
                .padding(top = 72.dp, bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {

            // ── Logo with pulsing ring ────────────────────────────────────────
            PulsingLogoSection()

            Spacer(Modifier.height(28.dp))

            // ── Brand name ───────────────────────────────────────────────────
            Text(
                text = "Next Toppers Feed",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "The premium learning platform\nfor India's next achievers",
                fontSize = 15.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            Spacer(Modifier.height(32.dp))

            // ── Feature tiles ────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FeatureTile(icon = Icons.Rounded.AutoStories, label = "Notes",       accentColor = AccentCyan)
                FeatureTile(icon = Icons.Rounded.EmojiEvents, label = "Leaderboard", accentColor = AccentEmerald)
                FeatureTile(icon = Icons.Rounded.Forum,       label = "Study Chats", accentColor = AccentViolet)
                FeatureTile(icon = Icons.Rounded.Quiz,        label = "Tests",       accentColor = AccentCyan)
            }

            Spacer(Modifier.height(28.dp))

            // ── Trust badges row ─────────────────────────────────────────────
            TrustBadgesRow()

            Spacer(Modifier.height(36.dp))

            // ── Google sign-in button ────────────────────────────────────────
            GoogleSignInButton(
                isLoading = isLoading,
                onClick   = { viewModel.signInWithGoogle(context) }
            )

            Spacer(Modifier.height(24.dp))

            // ── Social proof ─────────────────────────────────────────────────
            SocialProofSection()

            Spacer(Modifier.height(16.dp))

            Text(
                text = "By continuing you agree to our Terms of Service and Privacy Policy",
                fontSize = 11.sp,
                color = TextMuted,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier  = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }
}

@Composable
private fun PulsingLogoSection() {
    val infiniteTransition = rememberInfiniteTransition(label = "ring_pulse")
    val ringScale by infiniteTransition.animateFloat(
        initialValue  = 0.94f,
        targetValue   = 1.06f,
        animationSpec = infiniteRepeatable(
            animation  = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ring_scale"
    )
    val ringAlpha by infiniteTransition.animateFloat(
        initialValue  = 0.6f,
        targetValue   = 1f,
        animationSpec = infiniteRepeatable(
            animation  = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ring_alpha"
    )

    Box(
        modifier = Modifier.size(120.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer glow ring
        Box(
            modifier = Modifier
                .size(120.dp)
                .scale(ringScale)
                .clip(CircleShape)
                .border(
                    2.dp,
                    Brush.sweepGradient(
                        listOf(
                            AccentCyan.copy(ringAlpha),
                            AccentEmerald.copy(ringAlpha * 0.8f),
                            AccentViolet.copy(ringAlpha * 0.5f),
                            AccentCyan.copy(ringAlpha)
                        )
                    ),
                    CircleShape
                )
        )
        // Inner logo circle
        Box(
            modifier = Modifier
                .size(88.dp)
                .background(
                    Brush.radialGradient(
                        listOf(SurfaceElevated, SurfaceCard)
                    ),
                    CircleShape
                )
                .border(1.dp, AccentCyan.copy(0.3f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "NT",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary
            )
        }
    }
}

@Composable
private fun FeatureTile(icon: ImageVector, label: String, accentColor: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .background(accentColor.copy(0.10f), RoundedCornerShape(16.dp))
                .border(1.dp, accentColor.copy(0.28f), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = accentColor, modifier = Modifier.size(26.dp))
        }
        Text(label, color = TextSecondary, fontSize = 11.sp, textAlign = TextAlign.Center)
        // Colored underline dot
        Box(
            modifier = Modifier
                .size(4.dp)
                .background(accentColor, CircleShape)
        )
    }
}

@Composable
private fun TrustBadgesRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceCard)
            .border(1.dp, SurfaceElevated, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TrustBadge(icon = Icons.Rounded.Shield, label = "Secure",  sub = "Your data is\nalways safe",    color = AccentEmerald)
        VerticalDivider()
        TrustBadge(icon = Icons.Rounded.Lock,   label = "Private", sub = "100% privacy\nguaranteed",      color = AccentCyan)
        VerticalDivider()
        TrustBadge(icon = Icons.Rounded.Star,   label = "Ad-free", sub = "Uninterrupted\nlearning",       color = AccentViolet)
    }
}

@Composable
private fun TrustBadge(icon: ImageVector, label: String, sub: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
        Text(label, color = color, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
        Text(sub, color = TextMuted, fontSize = 9.sp, textAlign = TextAlign.Center, lineHeight = 13.sp)
    }
}

@Composable
private fun VerticalDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(40.dp)
            .background(SurfaceElevated)
    )
}

@Composable
private fun GoogleSignInButton(isLoading: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .clickable(enabled = !isLoading, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color    = Color(0xFF4285F4),
                strokeWidth = 2.dp
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Google G — multicolor
                Text(
                    text = "G",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4285F4)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "Continue with Google",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1F2937)
                )
            }
        }
    }
}

@Composable
private fun SocialProofSection() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Avatar stack
        Row(horizontalArrangement = Arrangement.spacedBy((-10).dp)) {
            repeat(4) { idx ->
                val colors = listOf(AccentCyan, AccentEmerald, AccentViolet, NeonCyan)
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(colors[idx], CircleShape)
                        .border(1.5.dp, BackgroundBlack, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = listOf("A", "B", "C", "D")[idx],
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
        Text(
            text = "Trusted by 50K+ Students",
            color = TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
        // Stars
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            repeat(5) {
                Icon(Icons.Rounded.Star, null, tint = PremiumGold, modifier = Modifier.size(14.dp))
            }
        }
    }
}
