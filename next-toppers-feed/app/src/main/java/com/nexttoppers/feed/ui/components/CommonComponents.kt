package com.nexttoppers.feed.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexttoppers.feed.ui.theme.BackgroundBlack
import com.nexttoppers.feed.ui.theme.GradientEnd
import com.nexttoppers.feed.ui.theme.GradientStart
import com.nexttoppers.feed.ui.theme.NeonCyan
import com.nexttoppers.feed.ui.theme.NeonGreen
import com.nexttoppers.feed.ui.theme.SurfaceCard
import com.nexttoppers.feed.ui.theme.SurfaceElevated
import com.nexttoppers.feed.ui.theme.TextPrimary
import com.nexttoppers.feed.ui.theme.TextSecondary

val neonGradient = Brush.linearGradient(colors = listOf(GradientStart, GradientEnd))

// ── Shimmer modifier ───────────────────────────────────────────────────────────
fun Modifier.shimmerEffect(): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )
    background(
        Brush.linearGradient(
            colors = listOf(
                SurfaceCard,
                SurfaceElevated,
                SurfaceCard
            ),
            start = Offset(translateAnim - 200f, 0f),
            end   = Offset(translateAnim, 0f)
        )
    )
}

// ── Skeleton card ──────────────────────────────────────────────────────────────
@Composable
fun SkeletonCard(modifier: Modifier = Modifier, height: Dp = 120.dp) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(20.dp))
            .shimmerEffect()
    )
}

@Composable
fun SkeletonRow(modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            Modifier
                .size(48.dp)
                .clip(CircleShape)
                .shimmerEffect()
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
            Box(Modifier.fillMaxWidth(0.6f).height(14.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
            Box(Modifier.fillMaxWidth(0.9f).height(12.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
        }
    }
}

// ── Primary neon button ────────────────────────────────────────────────────────
@Composable
fun NtfPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, disabledContainerColor = SurfaceElevated),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(
                    brush = if (enabled && !isLoading) neonGradient
                    else Brush.linearGradient(listOf(SurfaceElevated, SurfaceElevated)),
                    shape = RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = BackgroundBlack, strokeWidth = 2.dp)
            } else {
                Text(text, color = BackgroundBlack, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

// ── Outlined button ────────────────────────────────────────────────────────────
@Composable
fun NtfOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = NeonGreen,
    enabled: Boolean = true
) {
    Button(
        onClick  = onClick,
        enabled  = enabled,
        modifier = modifier.fillMaxWidth().height(56.dp)
            .border(
                1.dp,
                if (enabled) accentColor.copy(alpha = 0.6f) else accentColor.copy(0.2f),
                RoundedCornerShape(16.dp)
            ),
        shape  = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor         = accentColor.copy(alpha = 0.07f),
            disabledContainerColor = accentColor.copy(alpha = 0.03f)
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(
            text,
            color      = if (enabled) accentColor else accentColor.copy(0.4f),
            fontWeight = FontWeight.SemiBold,
            fontSize   = 16.sp
        )
    }
}

// ── Glass card ─────────────────────────────────────────────────────────────────
@Composable
fun NtfCard(
    modifier: Modifier = Modifier,
    borderColor: Color = NeonGreen.copy(alpha = 0.2f),
    cornerRadius: Dp = 20.dp,
    innerPadding: Dp = 16.dp,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(SurfaceCard)
            .border(1.dp, borderColor, RoundedCornerShape(cornerRadius))
            .padding(innerPadding)
    ) { content() }
}

// ── Gradient card border ───────────────────────────────────────────────────────
@Composable
fun NtfGradientCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    innerPadding: Dp = 16.dp,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(SurfaceCard)
            .border(
                1.dp,
                Brush.linearGradient(listOf(NeonGreen.copy(0.5f), NeonCyan.copy(0.5f))),
                RoundedCornerShape(cornerRadius)
            )
            .padding(innerPadding)
    ) { content() }
}

// ── Pulsing live dot ───────────────────────────────────────────────────────────
@Composable
fun PulsingDot(color: Color = NeonGreen, size: Dp = 10.dp) {
    val transition = rememberInfiniteTransition(label = "pulseDot")
    val alpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing), RepeatMode.Reverse),
        label = "dotAlpha"
    )
    Box(modifier = Modifier.size(size).background(color.copy(alpha = alpha), CircleShape))
}

// ── XP badge ──────────────────────────────────────────────────────────────────
@Composable
fun XpBadge(xp: Long, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(Brush.linearGradient(listOf(NeonGreen.copy(0.2f), NeonCyan.copy(0.2f))), RoundedCornerShape(50.dp))
            .border(1.dp, NeonGreen.copy(0.5f), RoundedCornerShape(50.dp))
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("⚡", fontSize = 14.sp)
            Spacer(Modifier.width(4.dp))
            Text("$xp XP", color = NeonGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
    }
}

// ── Section header ─────────────────────────────────────────────────────────────
@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 17.sp, modifier = modifier)
}

// ── Neon divider ───────────────────────────────────────────────────────────────
@Composable
fun NeonDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Brush.horizontalGradient(listOf(Color.Transparent, NeonGreen.copy(0.4f), Color.Transparent)))
    )
}

// ── Full-screen loading ────────────────────────────────────────────────────────
@Composable
fun NtfLoadingOverlay() {
    Box(modifier = Modifier.fillMaxWidth().background(BackgroundBlack.copy(0.7f)), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = NeonGreen, modifier = Modifier.size(48.dp), strokeWidth = 3.dp)
    }
}

// ── Stat chip ─────────────────────────────────────────────────────────────────
@Composable
fun StatChip(label: String, value: String, color: Color = NeonGreen, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(color.copy(0.08f))
            .border(1.dp, color.copy(0.25f), RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, color = color, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(Modifier.height(2.dp))
        Text(label, color = TextSecondary, fontSize = 11.sp)
    }
}
