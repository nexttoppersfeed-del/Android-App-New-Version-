package com.nexttoppers.feed.ui.notifications

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexttoppers.feed.data.model.NotificationFilter
import com.nexttoppers.feed.data.model.NotificationType
import com.nexttoppers.feed.data.model.NtfNotification
import com.nexttoppers.feed.ui.theme.BackgroundBlack
import com.nexttoppers.feed.ui.theme.ErrorRed
import com.nexttoppers.feed.ui.theme.NeonCyan
import com.nexttoppers.feed.ui.theme.NeonGreen
import com.nexttoppers.feed.ui.theme.PremiumGold
import com.nexttoppers.feed.ui.theme.PremiumViolet
import com.nexttoppers.feed.ui.theme.SurfaceCard
import com.nexttoppers.feed.ui.theme.SurfaceElevated
import com.nexttoppers.feed.ui.theme.TextMuted
import com.nexttoppers.feed.ui.theme.TextPrimary
import com.nexttoppers.feed.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ── Notification color by type ─────────────────────────────────────────────────
fun notificationColor(type: NotificationType): Color = when (type) {
    NotificationType.XP_EARNED           -> NeonGreen
    NotificationType.LEVEL_UP            -> NeonCyan
    NotificationType.STREAK_REMINDER     -> Color(0xFFFF6B35)
    NotificationType.RANK_CHANGE         -> PremiumGold
    NotificationType.PREMIUM_EXPIRY      -> PremiumViolet
    NotificationType.NEW_RESOURCE        -> NeonCyan
    NotificationType.QUIZ_REMINDER       -> NeonGreen.copy(0.8f)
    NotificationType.ANNOUNCEMENT        -> NeonCyan
    NotificationType.ACHIEVEMENT_UNLOCKED-> PremiumGold
    NotificationType.SYSTEM              -> TextSecondary
    NotificationType.PRIVATE_MESSAGE     -> NeonCyan
    NotificationType.COMMUNITY_MENTION   -> NeonGreen
    NotificationType.NEW_LECTURE         -> NeonCyan
    NotificationType.NEW_PDF             -> NeonCyan
    NotificationType.NEW_NOTES           -> NeonGreen
    NotificationType.PREMIUM_ACTIVATED   -> PremiumGold
    NotificationType.TEST_PUBLISHED      -> PremiumViolet
}

// ── Bell icon with animated badge count ───────────────────────────────────────
@Composable
fun NotificationBellBadge(
    unreadCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "bell")
    val bellScale by transition.animateFloat(
        initialValue  = 1f,
        targetValue   = if (unreadCount > 0) 1.06f else 1f,
        animationSpec = infiniteRepeatable(tween(800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label         = "bellScale"
    )

    Box(
        modifier = modifier
            .size(38.dp)
            .scale(bellScale)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Rounded.NotificationsNone, null,
            tint     = if (unreadCount > 0) NeonGreen else TextSecondary,
            modifier = Modifier.size(24.dp)
        )
        if (unreadCount > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 2.dp, y = (-2).dp)
                    .size(if (unreadCount < 10) 16.dp else 20.dp)
                    .background(ErrorRed, CircleShape)
                    .border(1.dp, BackgroundBlack, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text     = if (unreadCount > 99) "99+" else unreadCount.toString(),
                    color    = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

// ── Single notification card ───────────────────────────────────────────────────
@Composable
fun NotificationCard(
    notification: NtfNotification,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val typeColor = notificationColor(notification.notificationType)
    val bgAlpha   = if (!notification.read) 0.07f else 0.02f

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.horizontalGradient(listOf(typeColor.copy(bgAlpha), SurfaceCard.copy(if (!notification.read) 0.95f else 0.85f)))
            )
            .border(
                width  = if (!notification.read) 1.dp else 0.5.dp,
                color  = typeColor.copy(if (!notification.read) 0.45f else 0.15f),
                shape  = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            // Type icon bubble
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(typeColor.copy(0.12f), RoundedCornerShape(12.dp))
                    .border(1.dp, typeColor.copy(0.3f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(notification.notificationType.emoji, fontSize = 18.sp)
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        notification.title,
                        color      = if (!notification.read) TextPrimary else TextSecondary,
                        fontWeight = if (!notification.read) FontWeight.Bold else FontWeight.Normal,
                        fontSize   = 13.sp,
                        modifier   = Modifier.weight(1f),
                        maxLines   = 1,
                        overflow   = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        relativeTime(notification.timestamp.toDate()),
                        color    = TextMuted,
                        fontSize = 10.sp
                    )
                }
                Text(
                    notification.message,
                    color    = TextSecondary,
                    fontSize = 12.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 17.sp
                )
                if (!notification.read) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(6.dp).background(typeColor, CircleShape))
                        Text("New", color = typeColor, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

// ── Notification group header ──────────────────────────────────────────────────
@Composable
fun NotificationGroupHeader(label: String, modifier: Modifier = Modifier) {
    Row(
        modifier          = modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(modifier = Modifier.height(1.dp).weight(0.1f).background(SurfaceElevated))
        Text(label, color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        Box(modifier = Modifier.height(1.dp).weight(1f).background(SurfaceElevated))
    }
}

// ── Filter chips ───────────────────────────────────────────────────────────────
@Composable
fun NotificationFilterChips(
    activeFilter: NotificationFilter,
    onFilterSelected: (NotificationFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    androidx.compose.foundation.lazy.LazyRow(
        modifier               = modifier,
        horizontalArrangement  = Arrangement.spacedBy(8.dp),
        contentPadding         = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp)
    ) {
        items(NotificationFilter.values()) { filter ->
            val isActive = filter == activeFilter
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(
                        if (isActive) Brush.horizontalGradient(listOf(NeonGreen, NeonCyan))
                        else Brush.horizontalGradient(listOf(SurfaceCard, SurfaceCard))
                    )
                    .border(1.dp, if (isActive) NeonGreen.copy(0f) else SurfaceElevated, RoundedCornerShape(50.dp))
                    .clickable { onFilterSelected(filter) }
                    .padding(horizontal = 14.dp, vertical = 7.dp)
            ) {
                Text(
                    filter.label,
                    color      = if (isActive) BackgroundBlack else TextSecondary,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                    fontSize   = 12.sp
                )
            }
        }
    }
}

// ── Empty notification state ───────────────────────────────────────────────────
@Composable
fun EmptyNotificationsState(onSeedClick: (() -> Unit)? = null, modifier: Modifier = Modifier) {
    Column(
        modifier            = modifier.padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("🔔", fontSize = 56.sp)
        Text("No notifications", color = TextSecondary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        Text("You're all caught up!", color = TextMuted, fontSize = 13.sp)
        if (onSeedClick != null) {
            Text(
                "Load sample notifications",
                color    = NeonGreen.copy(0.7f),
                fontSize = 12.sp,
                modifier = Modifier.clickable(onClick = onSeedClick)
            )
        }
    }
}

// ── Mark all read button ───────────────────────────────────────────────────────
@Composable
fun MarkAllReadButton(unreadCount: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
    if (unreadCount == 0) return
    Row(
        modifier  = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(NeonGreen.copy(0.1f))
            .border(1.dp, NeonGreen.copy(0.3f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(Icons.Rounded.CheckCircle, null, tint = NeonGreen, modifier = Modifier.size(14.dp))
        Text("Mark all read", color = NeonGreen, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

// ── Relative time formatter ────────────────────────────────────────────────────
fun relativeTime(date: Date): String {
    val diffMs  = System.currentTimeMillis() - date.time
    val diffMin = diffMs / (1000 * 60)
    return when {
        diffMin < 1    -> "just now"
        diffMin < 60   -> "${diffMin}m ago"
        diffMin < 1440 -> "${diffMin / 60}h ago"
        diffMin < 10080 -> "${diffMin / 1440}d ago"
        else -> SimpleDateFormat("d MMM", Locale.getDefault()).format(date)
    }
}

// ── Items extension for LazyRow ─────────────────────────────────────────────────
private fun <T> androidx.compose.foundation.lazy.LazyListScope.items(
    items: Array<T>,
    itemContent: @Composable androidx.compose.foundation.lazy.LazyItemScope.(item: T) -> Unit
) {
    items(items.size) { index -> itemContent(items[index]) }
}
