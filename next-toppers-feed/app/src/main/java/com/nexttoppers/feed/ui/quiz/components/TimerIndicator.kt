package com.nexttoppers.feed.ui.quiz.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexttoppers.feed.ui.theme.NeonCyan
import com.nexttoppers.feed.ui.theme.NeonGreen
import com.nexttoppers.feed.ui.theme.SurfaceElevated
import com.nexttoppers.feed.ui.theme.TextPrimary
import com.nexttoppers.feed.ui.theme.TextSecondary

@Composable
fun CircularTimer(
    timeRemaining: Int,
    totalTime: Int,
    modifier: Modifier = Modifier,
    size: Dp = 80.dp,
    strokeWidth: Dp = 6.dp
) {
    val fraction = if (totalTime > 0) timeRemaining.toFloat() / totalTime else 0f

    val trackColor = SurfaceElevated
    val arcColor by animateColorAsState(
        targetValue = when {
            fraction > 0.5f -> NeonGreen
            fraction > 0.25f -> NeonCyan
            else            -> Color(0xFFFF6B6B)
        },
        animationSpec = tween(500),
        label = "timerColor"
    )
    val animFraction by animateFloatAsState(
        targetValue = fraction,
        animationSpec = tween(300),
        label = "timerFraction"
    )

    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(size)) {
            val strokePx = strokeWidth.toPx()
            val diameter = size.toPx() - strokePx
            val topLeft  = Offset(strokePx / 2f, strokePx / 2f)
            val arcSize  = Size(diameter, diameter)

            // Track
            drawArc(
                color      = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter  = false,
                topLeft    = topLeft,
                size       = arcSize,
                style      = Stroke(strokePx, cap = StrokeCap.Round)
            )
            // Progress
            drawArc(
                color      = arcColor,
                startAngle = -90f,
                sweepAngle = 360f * animFraction,
                useCenter  = false,
                topLeft    = topLeft,
                size       = arcSize,
                style      = Stroke(strokePx, cap = StrokeCap.Round)
            )
        }

        // Time label
        val minutes = timeRemaining / 60
        val seconds = timeRemaining % 60
        val label   = if (minutes > 0) "%d:%02d".format(minutes, seconds) else "$seconds"
        Text(
            text       = label,
            color      = if (fraction <= 0.25f) Color(0xFFFF6B6B) else TextPrimary,
            fontWeight = FontWeight.ExtraBold,
            fontSize   = if (size >= 80.dp) 18.sp else 13.sp
        )
    }
}

// ── Linear timer bar (compact, for question header) ────────────────────────────
@Composable
fun LinearTimer(
    timeRemaining: Int,
    totalTime: Int,
    modifier: Modifier = Modifier,
    height: Dp = 4.dp
) {
    val fraction = if (totalTime > 0) timeRemaining.toFloat() / totalTime else 0f
    val color by animateColorAsState(
        targetValue = when {
            fraction > 0.5f  -> NeonGreen
            fraction > 0.25f -> NeonCyan
            else             -> Color(0xFFFF6B6B)
        },
        animationSpec = tween(400),
        label = "linearTimerColor"
    )
    Canvas(modifier = modifier) {
        val h = height.toPx()
        drawRoundRect(color = SurfaceElevated, size = Size(this.size.width, h), cornerRadius = androidx.compose.ui.geometry.CornerRadius(h / 2))
        drawRoundRect(color = color, size = Size(this.size.width * fraction, h), cornerRadius = androidx.compose.ui.geometry.CornerRadius(h / 2))
    }
}
