package com.nexttoppers.feed.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
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
import com.nexttoppers.feed.ui.theme.NeonGreen
import com.nexttoppers.feed.ui.theme.SurfaceCard
import com.nexttoppers.feed.ui.theme.SurfaceElevated

// ── Shimmer modifier ───────────────────────────────────────────────────────────
fun Modifier.shimmerEffect(): Modifier = composed {
    val shimmerColors = listOf(
        SurfaceCard,
        SurfaceElevated,
        SurfaceCard
    )
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
            colors = shimmerColors,
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
            .clip(RoundedCornerShape(16.dp))
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

// ── Primary button (Material 3 FilledButton) ───────────────────────────────────
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
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            Text(text, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

// ── Outlined button (Material 3 OutlinedButton) ────────────────────────────────
@Composable
fun NtfOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick  = onClick,
        enabled  = enabled,
        modifier = modifier.fillMaxWidth().height(56.dp),
        shape    = RoundedCornerShape(16.dp),
        colors   = ButtonDefaults.outlinedButtonColors(contentColor = accentColor),
        border   = BorderStroke(1.dp, if (enabled) accentColor else accentColor.copy(0.3f))
    ) {
        Text(text, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
    }
}

// ── Card (Material 3 Card) ─────────────────────────────────────────────────────
@Composable
fun NtfCard(
    modifier: Modifier = Modifier,
    borderColor: Color = MaterialTheme.colorScheme.outlineVariant,
    cornerRadius: Dp = 16.dp,
    innerPadding: Dp = 16.dp,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier,
        shape    = RoundedCornerShape(cornerRadius),
        colors   = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        border   = BorderStroke(1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(modifier = Modifier.padding(innerPadding)) { content() }
    }
}

// ── Gradient card → ElevatedCard (Material 3) ─────────────────────────────────
@Composable
fun NtfGradientCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    innerPadding: Dp = 16.dp,
    content: @Composable () -> Unit
) {
    ElevatedCard(
        modifier  = modifier,
        shape     = RoundedCornerShape(cornerRadius),
        colors    = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.padding(innerPadding)) { content() }
    }
}

// ── Pulsing live dot ───────────────────────────────────────────────────────────
@Composable
fun PulsingDot(color: Color = MaterialTheme.colorScheme.primary, size: Dp = 10.dp) {
    val transition = rememberInfiniteTransition(label = "pulseDot")
    val alpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing), RepeatMode.Reverse),
        label = "dotAlpha"
    )
    Box(modifier = Modifier.size(size).background(color.copy(alpha = alpha), CircleShape))
}

// ── XP badge (Material 3 SuggestionChip) ──────────────────────────────────────
@Composable
fun XpBadge(xp: Long, modifier: Modifier = Modifier) {
    SuggestionChip(
        onClick = {},
        modifier = modifier,
        label = {
            Text(
                "$xp XP",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        },
        icon = {
            Icon(
                Icons.Rounded.Bolt,
                contentDescription = null,
                modifier = Modifier.size(14.dp)
            )
        },
        colors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            labelColor = MaterialTheme.colorScheme.onSecondaryContainer,
            iconContentColor = MaterialTheme.colorScheme.onSecondaryContainer
        ),
        border = SuggestionChipDefaults.suggestionChipBorder(
            enabled = true,
            borderColor = MaterialTheme.colorScheme.outline.copy(0.3f)
        )
    )
}

// ── Section header (Material 3 typography) ────────────────────────────────────
@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        title,
        style    = MaterialTheme.typography.titleMedium,
        color    = MaterialTheme.colorScheme.onSurface,
        modifier = modifier
    )
}

// ── Divider (Material 3 HorizontalDivider) ────────────────────────────────────
@Composable
fun NeonDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier  = modifier,
        thickness = 1.dp,
        color     = MaterialTheme.colorScheme.outlineVariant
    )
}

// ── Full-screen loading ────────────────────────────────────────────────────────
@Composable
fun NtfLoadingOverlay() {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            strokeWidth = 3.dp
        )
    }
}

// ── Stat chip (Material 3 surface) ────────────────────────────────────────────
@Composable
fun StatChip(label: String, value: String, color: Color = NeonGreen, modifier: Modifier = Modifier) {
    Surface(
        modifier  = modifier.clip(RoundedCornerShape(14.dp)),
        shape     = RoundedCornerShape(14.dp),
        color     = color.copy(0.08f),
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .border(1.dp, color.copy(0.2f), RoundedCornerShape(14.dp))
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, color = color, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(Modifier.height(2.dp))
            Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
        }
    }
}

// ── NtfSpinner (shared loading indicator) ────────────────────────────────────
@Composable
fun NtfSpinner(modifier: Modifier = Modifier, size: Dp = 40.dp) {
    CircularProgressIndicator(
        modifier    = modifier.size(size),
        strokeWidth = (size.value / 14f).dp
    )
}
