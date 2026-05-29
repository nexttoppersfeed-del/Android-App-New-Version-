package com.nexttoppers.feed.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nexttoppers.feed.ui.theme.SurfaceCard

private val ShimmerBase      = Color(0xFF1C2132)
private val ShimmerHighlight = Color(0xFF2A3248)

// ── Core shimmer brush factory ────────────────────────────────────────────────

@Composable
fun rememberShimmerBrush(
    widthPx: Float = 1000f,
    durationMs: Int = 1200
): Brush {
    val infinite = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by infinite.animateFloat(
        initialValue  = 0f,
        targetValue   = widthPx,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMs, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )
    return Brush.linearGradient(
        colors = listOf(ShimmerBase, ShimmerHighlight, ShimmerBase),
        start  = Offset(translateAnim - widthPx / 2, 0f),
        end    = Offset(translateAnim + widthPx / 2, 0f)
    )
}

// ── Primitive shimmer shapes ──────────────────────────────────────────────────

@Composable
fun ShimmerBox(
    width: Dp = Dp.Infinity,
    height: Dp,
    cornerRadius: Dp = 12.dp,
    modifier: Modifier = Modifier
) {
    val brush = rememberShimmerBrush()
    val boxMod = if (width == Dp.Infinity) {
        modifier.fillMaxWidth().height(height).clip(RoundedCornerShape(cornerRadius)).background(brush)
    } else {
        modifier.width(width).height(height).clip(RoundedCornerShape(cornerRadius)).background(brush)
    }
    Box(boxMod)
}

@Composable
fun ShimmerCircle(size: Dp, modifier: Modifier = Modifier) {
    val brush = rememberShimmerBrush()
    Box(
        modifier.size(size).clip(CircleShape).background(brush)
    )
}

// ── Compound shimmer layouts ──────────────────────────────────────────────────

/** Shimmer placeholder for a resource/content card */
@Composable
fun ShimmerResourceCard(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(SurfaceCard)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().background(SurfaceCard),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            ShimmerBox(height = 120.dp, cornerRadius = 18.dp)
            Column(
                modifier = Modifier.fillMaxWidth().background(SurfaceCard).then(
                    Modifier.background(SurfaceCard)
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Spacer(Modifier.height(12.dp))
                ShimmerBox(height = 14.dp, cornerRadius = 6.dp, modifier = Modifier.fillMaxWidth(0.7f))
                ShimmerBox(height = 10.dp, cornerRadius = 6.dp, modifier = Modifier.fillMaxWidth(0.4f))
                Spacer(Modifier.height(4.dp))
            }
        }
    }
}

/** Shimmer placeholder for a user / list row */
@Composable
fun ShimmerListRow(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ShimmerCircle(44.dp)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            ShimmerBox(height = 13.dp, cornerRadius = 6.dp, modifier = Modifier.fillMaxWidth(0.6f))
            ShimmerBox(height = 10.dp, cornerRadius = 5.dp, modifier = Modifier.fillMaxWidth(0.4f))
        }
        ShimmerBox(width = 48.dp, height = 12.dp, cornerRadius = 5.dp)
    }
}

/** Shimmer for announcement/news card */
@Composable
fun ShimmerAnnouncementCard(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(SurfaceCard),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Spacer(Modifier.width(0.dp))
        ShimmerBox(width = 4.dp, height = 56.dp, cornerRadius = 2.dp)
        Column(Modifier.weight(1f).background(SurfaceCard), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Spacer(Modifier.height(10.dp))
            ShimmerBox(height = 13.dp, cornerRadius = 6.dp, modifier = Modifier.fillMaxWidth(0.65f))
            ShimmerBox(height = 10.dp, cornerRadius = 5.dp, modifier = Modifier.fillMaxWidth(0.85f))
            ShimmerBox(height = 9.dp,  cornerRadius = 5.dp, modifier = Modifier.fillMaxWidth(0.45f))
            Spacer(Modifier.height(8.dp))
        }
    }
}

/** Generic shimmer stat card */
@Composable
fun ShimmerStatCard(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceCard),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Spacer(Modifier.height(12.dp))
        ShimmerBox(width = 40.dp, height = 24.dp, cornerRadius = 8.dp)
        ShimmerBox(width = 56.dp, height = 10.dp, cornerRadius = 5.dp)
        Spacer(Modifier.height(8.dp))
    }
}

/** Full screen loading shimmer — 3 stacked cards */
@Composable
fun ShimmerFeedList(count: Int = 4) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(count) {
            ShimmerBox(height = 88.dp, cornerRadius = 16.dp)
        }
    }
}
