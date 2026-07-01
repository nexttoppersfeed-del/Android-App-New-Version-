package com.nexttoppers.feed.ui.activity

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.nexttoppers.feed.data.model.ActivityFeedItem
import com.nexttoppers.feed.data.model.ActivityType
import com.nexttoppers.feed.data.model.FeedTab
import com.nexttoppers.feed.ui.notifications.relativeTime
import com.nexttoppers.feed.ui.theme.NeonCyan
import com.nexttoppers.feed.ui.theme.NeonGreen
import com.nexttoppers.feed.ui.theme.PremiumGold
import com.nexttoppers.feed.ui.theme.PremiumViolet
import com.nexttoppers.feed.ui.theme.SurfaceCard
import com.nexttoppers.feed.ui.theme.SurfaceElevated
import com.nexttoppers.feed.ui.theme.TextMuted
import com.nexttoppers.feed.ui.theme.TextPrimary
import com.nexttoppers.feed.ui.theme.TextSecondary

private fun activityColor(type: ActivityType): Color = when (type) {
    ActivityType.QUIZ_COMPLETED -> NeonGreen
    ActivityType.XP_EARNED -> NeonCyan
    ActivityType.RANK_ACHIEVED -> PremiumGold
    ActivityType.ACHIEVEMENT_UNLOCKED -> PremiumGold
    ActivityType.STREAK_MAINTAINED -> Color(0xFFFF6B35)
    ActivityType.RESOURCE_OPENED -> NeonCyan
    ActivityType.PREMIUM_ACTIVATED -> PremiumViolet
    ActivityType.LEVEL_UP -> NeonGreen
}

@Composable
fun ActivityFeedCard(
    item: ActivityFeedItem,
    isFirst: Boolean = false,
    isLast: Boolean = false,
    modifier: Modifier = Modifier
) {

    val color = activityColor(item.activityType)

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(36.dp)
        ) {

            if (!isFirst) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(8.dp)
                        .background(SurfaceElevated)
                )
            }

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        color.copy(alpha = 0.12f),
                        CircleShape
                    )
                    .border(
                        1.5.dp,
                        color.copy(alpha = 0.5f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item.activityType.emoji,
                    fontSize = 16.sp
                )
            }

            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(48.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    SurfaceElevated,
                                    Color.Transparent
                                )
                            )
                        )
                )
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 10.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            color.copy(alpha = 0.05f),
                            SurfaceCard
                        )
                    )
                )
                .border(
                    1.dp,
                    color.copy(alpha = 0.2f),
                    RoundedCornerShape(14.dp)
                )
                .padding(12.dp)
        ) {

            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {

                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(SurfaceElevated),
                            contentAlignment = Alignment.Center
                        ) {

                            if (item.photoUrl.isNotEmpty()) {

                                AsyncImage(
                                    model = item.photoUrl,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(24.dp)
                                        .clip(CircleShape)
                                )

                            } else {

                                Text(
                                    text = item.username.take(1).uppercase(),
                                    color = color,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Text(
                            text = item.username,
                            color = color,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                    }

                    Text(
                        text = relativeTime(item.timestamp.toDate()),
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                }

                Text(
                    text = item.description,
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (item.xpEarned > 0) {

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                NeonGreen.copy(alpha = 0.08f)
                            )
                            .border(
                                1.dp,
                                NeonGreen.copy(alpha = 0.25f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(
                                horizontal = 8.dp,
                                vertical = 4.dp
                            ),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Icon(Icons.Rounded.Bolt, null, tint = NeonGreen, modifier = Modifier.size(10.dp))

                        Text(
                            text = "+${item.xpEarned} XP",
                            color = NeonGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FeedTabRow(
    selectedTab: FeedTab,
    onTabSelected: (FeedTab) -> Unit,
    modifier: Modifier = Modifier
) {

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        FeedTab.values().forEach { tab ->

            val isActive = tab == selectedTab

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isActive) {
                            Brush.horizontalGradient(
                                listOf(
                                    NeonGreen.copy(alpha = 0.2f),
                                    NeonCyan.copy(alpha = 0.15f)
                                )
                            )
                        } else {
                            Brush.horizontalGradient(
                                listOf(
                                    SurfaceCard,
                                    SurfaceCard
                                )
                            )
                        }
                    )
                    .border(
                        1.dp,
                        if (isActive) {
                            NeonGreen.copy(alpha = 0.5f)
                        } else {
                            SurfaceElevated
                        },
                        RoundedCornerShape(12.dp)
                    )
                    .clickable {
                        onTabSelected(tab)
                    }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {

                Text(
                    text = tab.label,
                    color = if (isActive) NeonGreen else TextMuted,
                    fontWeight = if (isActive) {
                        FontWeight.Bold
                    } else {
                        FontWeight.Normal
                    },
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun FriendsFeedPlaceholder(
    modifier: Modifier = Modifier
) {

    Column(
        modifier = modifier.padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text(
            text = "👫",
            fontSize = 56.sp
        )

        Text(
            text = "Friends Feed",
            color = TextPrimary,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 18.sp
        )

        Text(
            text = "Coming soon — connect with friends and see their progress",
            color = TextSecondary,
            fontSize = 13.sp
        )
    }
}

@Composable
fun EmptyFeedState(
    isPersonal: Boolean,
    modifier: Modifier = Modifier
) {

    Column(
        modifier = modifier.padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Icon(
            if (isPersonal) Icons.Rounded.MenuBook else Icons.Rounded.Public,
            null,
            tint     = TextSecondary,
            modifier = Modifier.size(56.dp)
        )

        Text(
            text = if (isPersonal) {
                "No activity yet"
            } else {
                "No global activity"
            },
            color = TextSecondary,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp
        )

        Text(
            text = if (isPersonal) {
                "Complete quizzes, earn XP, and unlock achievements to see your activity here."
            } else {
                "Be the first topper to log some activity!"
            },
            color = TextMuted,
            fontSize = 12.sp
        )
    }
}
