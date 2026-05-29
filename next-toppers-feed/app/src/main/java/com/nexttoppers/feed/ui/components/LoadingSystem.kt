package com.nexttoppers.feed.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.nexttoppers.feed.ui.theme.BackgroundBlack
import com.nexttoppers.feed.ui.theme.NeonCyan
import com.nexttoppers.feed.ui.theme.NeonGreen
import com.nexttoppers.feed.ui.theme.SurfaceCard
import com.nexttoppers.feed.ui.theme.SurfaceElevated
import com.nexttoppers.feed.ui.theme.TextMuted
import com.nexttoppers.feed.ui.theme.TextPrimary
import com.nexttoppers.feed.ui.theme.TextSecondary

// ── Sealed state for universal loading/content/error lifecycle ─────────────────
sealed interface UiLoadState {
    data object Loading : UiLoadState
    data object Success : UiLoadState
    data class Error(val message: String, val retryable: Boolean = true) : UiLoadState
    data object Offline : UiLoadState
    data object Empty : UiLoadState
}

// ── 1. Full-screen blocking loading dialog ─────────────────────────────────────
@Composable
fun NtfLoadingDialog(
    visible: Boolean,
    message: String = "Processing…"
) {
    if (!visible) return
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(SurfaceCard)
                .border(1.dp, NeonGreen.copy(0.2f), RoundedCornerShape(24.dp))
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                NtfSpinner(size = 48.dp)
                Text(message, color = TextSecondary, fontSize = 14.sp, textAlign = TextAlign.Center)
            }
        }
    }
}

// ── 2. Fullscreen loading overlay (covers content) ────────────────────────────
@Composable
fun NtfFullscreenLoadingOverlay(visible: Boolean, message: String = "Loading…") {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(200)),
        exit  = fadeOut(tween(200))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundBlack.copy(0.85f)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                NtfSpinner(size = 56.dp)
                Text(message, color = TextSecondary, fontSize = 13.sp)
            }
        }
    }
}

// ── 3. Inline loading row (for list footers / sections) ───────────────────────
@Composable
fun InlineLoadingRow(message: String = "Loading more…", modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        NtfSpinner(size = 18.dp, strokeWidth = 2.dp)
        Spacer(Modifier.width(10.dp))
        Text(message, color = TextMuted, fontSize = 12.sp)
    }
}

// ── 4. Neon progress bar (linear) ─────────────────────────────────────────────
@Composable
fun NtfProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    trackColor: Color = SurfaceElevated,
    barColor: Color = NeonGreen,
    height: Dp = 4.dp
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(50))
            .background(trackColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .height(height)
                .background(
                    Brush.horizontalGradient(listOf(barColor, NeonCyan)),
                    RoundedCornerShape(50)
                )
        )
    }
}

// ── 5. Indeterminate neon linear progress ─────────────────────────────────────
@Composable
fun NtfLinearLoadingBar(modifier: Modifier = Modifier) {
    LinearProgressIndicator(
        modifier  = modifier.fillMaxWidth().clip(RoundedCornerShape(50)),
        color     = NeonGreen,
        trackColor = SurfaceElevated
    )
}

// ── 6. Pulsing skeleton block (generic) ───────────────────────────────────────
@Composable
fun PulsingSkeletonBlock(
    width: Dp = Dp.Unspecified,
    height: Dp,
    cornerRadius: Dp = 8.dp,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "skeleton")
    val alpha by transition.animateFloat(
        initialValue  = 0.4f,
        targetValue   = 0.9f,
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing), RepeatMode.Reverse),
        label         = "skeletonAlpha"
    )
    val base = if (width == Dp.Unspecified) modifier.fillMaxWidth() else modifier.width(width)
    Box(
        base
            .height(height)
            .clip(RoundedCornerShape(cornerRadius))
            .background(SurfaceElevated.copy(alpha = alpha))
    )
}

// ── 7. Chat / message shimmer row ─────────────────────────────────────────────
@Composable
fun ShimmerChatRow(isSelf: Boolean = false, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = if (isSelf) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isSelf) {
            ShimmerCircle(size = 32.dp)
            Spacer(Modifier.width(8.dp))
        }
        Column(
            horizontalAlignment = if (isSelf) Alignment.End else Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            ShimmerBox(
                width  = if (isSelf) 140.dp else 180.dp,
                height = 36.dp,
                cornerRadius = 14.dp
            )
            ShimmerBox(width = 60.dp, height = 8.dp, cornerRadius = 4.dp)
        }
    }
}

// ── 8. Leaderboard entry shimmer ──────────────────────────────────────────────
@Composable
fun ShimmerLeaderboardRow(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 5.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceCard)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ShimmerBox(width = 32.dp, height = 32.dp, cornerRadius = 10.dp)
        ShimmerCircle(size = 40.dp)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            ShimmerBox(height = 12.dp, cornerRadius = 6.dp, modifier = Modifier.fillMaxWidth(0.55f))
            ShimmerBox(height = 9.dp, cornerRadius = 5.dp, modifier = Modifier.fillMaxWidth(0.35f))
        }
        ShimmerBox(width = 40.dp, height = 14.dp, cornerRadius = 5.dp)
    }
}

// ── 9. Admin stat card shimmer ────────────────────────────────────────────────
@Composable
fun ShimmerAdminStatCard(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceCard)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        ShimmerBox(width = 44.dp, height = 22.dp, cornerRadius = 6.dp)
        ShimmerBox(width = 56.dp, height = 9.dp,  cornerRadius = 4.dp)
    }
}

// ── 10. Notification row shimmer ──────────────────────────────────────────────
@Composable
fun ShimmerNotificationRow(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceCard)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ShimmerCircle(size = 40.dp)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            ShimmerBox(height = 12.dp, cornerRadius = 6.dp, modifier = Modifier.fillMaxWidth(0.7f))
            ShimmerBox(height = 9.dp, cornerRadius = 5.dp, modifier = Modifier.fillMaxWidth(0.9f))
            ShimmerBox(height = 8.dp, cornerRadius = 4.dp, modifier = Modifier.fillMaxWidth(0.4f))
        }
    }
}

// ── 11. Premium plan card shimmer ─────────────────────────────────────────────
@Composable
fun ShimmerPremiumCard(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceCard)
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ShimmerBox(height = 16.dp, cornerRadius = 8.dp, modifier = Modifier.fillMaxWidth(0.5f))
        ShimmerBox(height = 22.dp, cornerRadius = 8.dp, modifier = Modifier.fillMaxWidth(0.35f))
        ShimmerBox(height = 10.dp, cornerRadius = 5.dp, modifier = Modifier.fillMaxWidth(0.8f))
        ShimmerBox(height = 10.dp, cornerRadius = 5.dp, modifier = Modifier.fillMaxWidth(0.65f))
    }
}

// ── 12. Download card shimmer ─────────────────────────────────────────────────
@Composable
fun ShimmerDownloadCard(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceCard)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ShimmerBox(width = 52.dp, height = 52.dp, cornerRadius = 13.dp)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            ShimmerBox(height = 13.dp, cornerRadius = 6.dp, modifier = Modifier.fillMaxWidth(0.75f))
            ShimmerBox(height = 10.dp, cornerRadius = 5.dp, modifier = Modifier.fillMaxWidth(0.5f))
        }
        ShimmerBox(width = 36.dp, height = 36.dp, cornerRadius = 10.dp)
    }
}

// ── 13. Full feed shimmer list (generic, flexible) ────────────────────────────
@Composable
fun NtfShimmerList(
    count: Int = 5,
    itemHeight: Dp = 80.dp,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        repeat(count) {
            ShimmerBox(height = itemHeight, cornerRadius = 16.dp)
        }
    }
}

// ── 14. Central spinner ────────────────────────────────────────────────────────
@Composable
fun NtfSpinner(
    size: Dp = 40.dp,
    color: Color = NeonGreen,
    strokeWidth: Dp = 3.dp
) {
    val transition = rememberInfiniteTransition(label = "spinnerGlow")
    val glowAlpha by transition.animateFloat(
        initialValue  = 0.3f,
        targetValue   = 0.8f,
        animationSpec = infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label         = "glowAlpha"
    )
    Box(contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(size + 8.dp)
                .background(color.copy(glowAlpha * 0.15f), CircleShape)
        )
        CircularProgressIndicator(
            modifier    = Modifier.size(size),
            color       = color,
            strokeWidth = strokeWidth
        )
    }
}

// ── 15. Floating offline chip ─────────────────────────────────────────────────
@Composable
fun OfflineChip(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(Color(0xFFFF6B35).copy(0.15f))
            .border(1.dp, Color(0xFFFF6B35).copy(0.4f), RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(Icons.Rounded.WifiOff, null, tint = Color(0xFFFF6B35), modifier = Modifier.size(14.dp))
        Text("Offline", color = Color(0xFFFF6B35), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

// ── 16. Content with loading/error/empty state wrapper ────────────────────────
@Composable
fun <T> NtfContentState(
    state: NtfState<T>,
    emptyTitle: String = "Nothing here yet",
    emptySubtitle: String? = null,
    loadingContent: @Composable () -> Unit = {
        NtfShimmerList(modifier = Modifier.padding(horizontal = 20.dp))
    },
    onRetry: (() -> Unit)? = null,
    content: @Composable (T) -> Unit
) {
    when (state) {
        is NtfState.Loading -> loadingContent()
        is NtfState.Error   -> FullScreenError(message = state.message, onRetry = onRetry)
        is NtfState.Offline -> FullScreenOffline(onRetry = onRetry)
        is NtfState.Empty   -> EmptyState(title = emptyTitle, subtitle = emptySubtitle)
        is NtfState.Success -> content(state.data)
    }
}

sealed interface NtfState<out T> {
    data object Loading : NtfState<Nothing>
    data class Success<T>(val data: T) : NtfState<T>
    data class Error(val message: String) : NtfState<Nothing>
    data object Offline : NtfState<Nothing>
    data object Empty : NtfState<Nothing>
}

// ── 17. Upload/download progress indicator ────────────────────────────────────
@Composable
fun NtfTransferProgress(
    label: String,
    progress: Float,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, color = TextPrimary, fontSize = 13.sp)
            Text("${(progress * 100).toInt()}%", color = NeonGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        NtfProgressBar(progress = progress)
    }
}

// ── 18. Pulsing badge (for pending counts) ────────────────────────────────────
@Composable
fun PulsingBadge(count: Int, color: Color = NeonGreen, modifier: Modifier = Modifier) {
    if (count <= 0) return
    val transition = rememberInfiniteTransition(label = "badge")
    val scale by transition.animateFloat(
        initialValue  = 0.92f,
        targetValue   = 1.08f,
        animationSpec = infiniteRepeatable(tween(700, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label         = "badgeScale"
    )
    Box(
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .size(18.dp)
            .background(color, CircleShape)
            .border(1.5.dp, BackgroundBlack, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            if (count > 9) "9+" else "$count",
            color      = BackgroundBlack,
            fontSize   = 9.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}
