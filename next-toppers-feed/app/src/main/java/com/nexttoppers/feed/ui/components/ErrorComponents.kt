package com.nexttoppers.feed.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexttoppers.feed.ui.theme.BackgroundBlack
import com.nexttoppers.feed.ui.theme.ErrorRed
import com.nexttoppers.feed.ui.theme.NeonCyan
import com.nexttoppers.feed.ui.theme.NeonGreen
import com.nexttoppers.feed.ui.theme.SurfaceCard
import com.nexttoppers.feed.ui.theme.TextMuted
import com.nexttoppers.feed.ui.theme.TextPrimary
import com.nexttoppers.feed.ui.theme.TextSecondary

private val WarnOrange = Color(0xFFFF6B35)

// ── Full-screen error state ────────────────────────────────────────────────────
@Composable
fun FullScreenError(
    message: String,
    onRetry: (() -> Unit)? = null,
    icon: ImageVector = Icons.Rounded.ErrorOutline,
    iconTint: Color = ErrorRed
) {
    val transition = rememberInfiniteTransition(label = "errPulse")
    val iconScale by transition.animateFloat(
        0.95f, 1.05f,
        infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "errScale"
    )

    Box(
        Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(40.dp)
        ) {
            Box(
                Modifier
                    .size(96.dp)
                    .scale(iconScale)
                    .background(
                        Brush.radialGradient(listOf(iconTint.copy(0.1f), Color.Transparent)),
                        androidx.compose.foundation.shape.CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = iconTint.copy(0.6f), modifier = Modifier.size(52.dp))
            }
            Text(
                "Something went wrong",
                color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp
            )
            Text(
                message,
                color = TextMuted, fontSize = 13.sp,
                textAlign = TextAlign.Center, lineHeight = 20.sp
            )
            if (onRetry != null) {
                Spacer(Modifier.height(4.dp))
                RetryButton(onRetry)
            }
        }
    }
}

// ── Full-screen offline state ──────────────────────────────────────────────────
@Composable
fun FullScreenOffline(onRetry: (() -> Unit)? = null) {
    val transition = rememberInfiniteTransition(label = "offlinePulse")
    val iconAlpha by transition.animateFloat(
        0.4f, 0.9f,
        infiniteRepeatable(tween(1400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "offlineAlpha"
    )

    Box(
        Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(40.dp)
        ) {
            Box(
                Modifier
                    .size(96.dp)
                    .background(
                        Brush.radialGradient(listOf(WarnOrange.copy(0.10f), Color.Transparent)),
                        androidx.compose.foundation.shape.CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.WifiOff, null,
                    tint     = WarnOrange.copy(iconAlpha),
                    modifier = Modifier.size(52.dp)
                )
            }
            Text("You're Offline", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(
                "Showing cached content where available.\nCheck your internet connection and try again.",
                color = TextMuted, fontSize = 13.sp,
                textAlign = TextAlign.Center, lineHeight = 20.sp
            )
            if (onRetry != null) {
                Spacer(Modifier.height(4.dp))
                RetryButton(onRetry, tint = WarnOrange)
            }
        }
    }
}

// ── Full-screen loading ────────────────────────────────────────────────────────
@Composable
fun FullScreenLoading(message: String = "Loading…") {
    Box(
        Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            NtfSpinner(size = 56.dp)
            Text(message, color = TextSecondary, fontSize = 13.sp)
        }
    }
}

// ── Empty state ────────────────────────────────────────────────────────────────
@Composable
fun EmptyState(
    title: String,
    subtitle: String? = null,
    icon: ImageVector = Icons.Rounded.SearchOff,
    iconTint: Color = TextMuted,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Column(
        modifier            = Modifier.fillMaxWidth().padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            Modifier
                .size(80.dp)
                .background(
                    Brush.radialGradient(listOf(iconTint.copy(0.08f), Color.Transparent)),
                    androidx.compose.foundation.shape.CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = iconTint.copy(0.45f), modifier = Modifier.size(44.dp))
        }
        Text(title, color = TextSecondary, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, textAlign = TextAlign.Center)
        if (subtitle != null) {
            Text(subtitle, color = TextMuted, fontSize = 12.sp, textAlign = TextAlign.Center, lineHeight = 18.sp)
        }
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(4.dp))
            RetryButton(onAction, label = actionLabel)
        }
    }
}

// ── Inline error banner ────────────────────────────────────────────────────────
@Composable
fun ErrorBanner(
    message: String,
    onDismiss: (() -> Unit)? = null,
    onRetry: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(ErrorRed.copy(0.1f))
            .border(1.dp, ErrorRed.copy(0.3f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Rounded.ErrorOutline, null, tint = ErrorRed, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(10.dp))
        Text(message, color = ErrorRed, fontSize = 12.sp, modifier = Modifier.weight(1f), lineHeight = 16.sp)
        if (onRetry != null) {
            TextButton(onClick = onRetry) {
                Text("Retry", color = NeonGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
        if (onDismiss != null) {
            TextButton(onClick = onDismiss) {
                Text("✕", color = TextMuted, fontSize = 12.sp)
            }
        }
    }
}

// ── Success toast (auto-dismiss, Material 3 ElevatedCard) ─────────────────────
@Composable
fun SuccessToast(message: String?, onDismiss: () -> Unit) {
    AnimatedVisibility(
        visible = message != null,
        enter   = fadeIn(tween(250)) + slideInVertically(tween(300)) { it / 3 },
        exit    = fadeOut(tween(300))
    ) {
        if (message != null) {
            LaunchedEffect(message) {
                kotlinx.coroutines.delay(2500)
                onDismiss()
            }
            androidx.compose.material3.ElevatedCard(
                shape = RoundedCornerShape(16.dp),
                elevation = androidx.compose.material3.CardDefaults.elevatedCardElevation(defaultElevation = 3.dp)
            ) {
                Row(
                    modifier              = Modifier.padding(horizontal = 22.dp, vertical = 13.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(Modifier.size(8.dp).background(NeonGreen, androidx.compose.foundation.shape.CircleShape))
                    Text(message, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

// ── Offline indicator banner (top strip) ─────────────────────────────────────
@Composable
fun OfflineBanner(visible: Boolean) {
    AnimatedVisibility(
        visible = visible,
        enter   = fadeIn(tween(300)) + slideInVertically(tween(300)) { -it },
        exit    = fadeOut(tween(300))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(WarnOrange.copy(0.12f))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Rounded.CloudOff, null, tint = WarnOrange, modifier = Modifier.size(16.dp))
            Text("You're offline — showing cached data", color = WarnOrange, fontSize = 12.sp)
        }
    }
}

// ── Premium gate (Material 3 OutlinedCard) ────────────────────────────────────
private val PremiumGate = Color(0xFFFFD700)

@Composable
fun PremiumGateCard(onUpgrade: () -> Unit, modifier: Modifier = Modifier) {
    androidx.compose.material3.OutlinedCard(
        onClick   = onUpgrade,
        modifier  = modifier,
        shape     = RoundedCornerShape(18.dp),
        colors    = androidx.compose.material3.CardDefaults.outlinedCardColors(
            containerColor = PremiumGate.copy(0.06f)
        ),
        border    = androidx.compose.foundation.BorderStroke(1.dp, PremiumGate.copy(0.4f))
    ) {
        Column(
            modifier            = Modifier.padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(Icons.Rounded.Lock, null, tint = PremiumGate.copy(0.7f), modifier = Modifier.size(44.dp))
            Text("Premium Content", color = PremiumGate, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
            Text("Upgrade to access all premium materials", color = TextMuted, fontSize = 12.sp, textAlign = TextAlign.Center)
            androidx.compose.material3.FilledTonalButton(
                onClick = onUpgrade,
                shape   = RoundedCornerShape(12.dp),
                colors  = androidx.compose.material3.ButtonDefaults.filledTonalButtonColors(
                    containerColor = PremiumGate.copy(0.15f),
                    contentColor   = PremiumGate
                )
            ) {
                Text("Upgrade Now", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
            }
        }
    }
}

// ── Error dialog ──────────────────────────────────────────────────────────────
@Composable
fun NtfErrorDialog(
    title: String = "Error",
    message: String,
    onDismiss: () -> Unit,
    onRetry: (() -> Unit)? = null
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Rounded.ErrorOutline, null, tint = ErrorRed, modifier = Modifier.size(20.dp))
                Text(title, color = ErrorRed, fontWeight = FontWeight.Bold)
            }
        },
        text  = { Text(message, color = TextSecondary, fontSize = 13.sp, lineHeight = 19.sp) },
        confirmButton = {
            if (onRetry != null) {
                TextButton(onClick = onRetry) {
                    Text("Try Again", color = NeonGreen, fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Dismiss", color = TextMuted) }
        },
        containerColor = SurfaceCard
    )
}

// ── Network error snack-style banner ──────────────────────────────────────────
@Composable
fun NetworkErrorStrip(
    visible: Boolean,
    message: String = "Connection failed. Check your internet.",
    onRetry: (() -> Unit)? = null
) {
    AnimatedVisibility(
        visible = visible,
        enter   = fadeIn(tween(250)) + slideInVertically(tween(280)) { it },
        exit    = fadeOut(tween(250))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(ErrorRed.copy(0.12f))
                .border(
                    width  = 0.dp,
                    color  = Color.Transparent,
                    shape  = RoundedCornerShape(0.dp)
                )
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Rounded.WifiOff, null, tint = ErrorRed, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text(message, color = ErrorRed, fontSize = 12.sp, modifier = Modifier.weight(1f))
            if (onRetry != null) {
                TextButton(onClick = onRetry, modifier = Modifier.height(32.dp)) {
                    Text("Retry", color = NeonGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ── Shared retry button (Material 3 FilledTonalButton) ────────────────────────
@Composable
internal fun RetryButton(
    onClick: () -> Unit,
    label: String = "Try Again",
    tint: Color = NeonGreen
) {
    androidx.compose.material3.FilledTonalButton(
        onClick = onClick,
        shape   = RoundedCornerShape(14.dp),
        colors  = androidx.compose.material3.ButtonDefaults.filledTonalButtonColors(
            containerColor = tint.copy(0.12f),
            contentColor   = tint
        )
    ) {
        Icon(Icons.Rounded.Refresh, null, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text(label, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
    }
}

