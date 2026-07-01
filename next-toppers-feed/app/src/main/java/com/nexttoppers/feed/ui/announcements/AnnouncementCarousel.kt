package com.nexttoppers.feed.ui.announcements

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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.nexttoppers.feed.data.model.Announcement
import com.nexttoppers.feed.ui.theme.ErrorRed
import com.nexttoppers.feed.ui.theme.NeonGreen
import com.nexttoppers.feed.ui.theme.PremiumGold
import com.nexttoppers.feed.ui.theme.SurfaceCard
import com.nexttoppers.feed.ui.theme.SurfaceElevated
import com.nexttoppers.feed.ui.theme.TextMuted
import com.nexttoppers.feed.ui.theme.TextPrimary
import com.nexttoppers.feed.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun PinnedAnnouncementBanner(
    announcement: Announcement,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    val transition =
        rememberInfiniteTransition(
            label = "pinnedBanner"
        )

    val pulseAlpha by transition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.9f,

        animationSpec =
        infiniteRepeatable(
            tween(
                1100,
                easing = LinearEasing
            ),
            RepeatMode.Reverse
        ),

        label = "pinnedPulse"
    )

    androidx.compose.material3.OutlinedCard(
        onClick   = onClick,
        modifier  = modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(14.dp),
        colors    = androidx.compose.material3.CardDefaults.outlinedCardColors(
            containerColor = ErrorRed.copy(0.10f)
        ),
        border    = androidx.compose.foundation.BorderStroke(1.5.dp, ErrorRed.copy(pulseAlpha))
    ) {

        Row(
            modifier              = Modifier.padding(12.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            Icon(
                Icons.Rounded.PushPin,
                null,

                tint = ErrorRed,

                modifier =
                Modifier.size(16.dp)
            )

            Column(
                Modifier.weight(1f)
            ) {

                Text(
                    announcement.title,

                    color = ErrorRed,

                    fontWeight =
                    FontWeight.ExtraBold,

                    fontSize = 13.sp,

                    maxLines = 1,

                    overflow =
                    TextOverflow.Ellipsis
                )

                Text(
                    announcement.content
                        .replace("\\n", " ")
                        .take(80),

                    color = TextSecondary,

                    fontSize = 11.sp,

                    maxLines = 1,

                    overflow =
                    TextOverflow.Ellipsis
                )
            }

            Icon(
                Icons.Rounded.ChevronRight,
                null,

                tint =
                ErrorRed.copy(0.6f),

                modifier =
                Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun AnnouncementCarouselSection(
    announcements: List<Announcement>,
    onAnnouncementClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {

    if (announcements.isEmpty()) return

    val listState =
        rememberLazyListState()

    val carouselItems =
        announcements.take(8)

    LaunchedEffect(carouselItems.size) {

        var currentIndex = 0

        while (carouselItems.size > 1) {

            delay(3500L)

            currentIndex =
                (currentIndex + 1) %
                        carouselItems.size

            listState.animateScrollToItem(
                currentIndex
            )
        }
    }

    Column(
        modifier = modifier,

        verticalArrangement =
        Arrangement.spacedBy(10.dp)
    ) {

        LazyRow(
            state = listState,

            contentPadding =
            PaddingValues(horizontal = 20.dp),

            horizontalArrangement =
            Arrangement.spacedBy(12.dp)
        ) {

            itemsIndexed(carouselItems) {
                    _, announcement ->

                AnnouncementCarouselCard(
                    announcement =
                    announcement,

                    onClick = {
                        onAnnouncementClick(
                            announcement.id
                        )
                    },

                    modifier =
                    Modifier.width(280.dp)
                )
            }
        }

        if (carouselItems.size > 1) {

            Row(
                Modifier.fillMaxWidth(),

                horizontalArrangement =
                Arrangement.Center,

                verticalAlignment =
                Alignment.CenterVertically
            ) {

                carouselItems.forEachIndexed {
                        i, _ ->

                    val isVisible =
                        listState.firstVisibleItemIndex == i

                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .size(
                                if (isVisible)
                                    8.dp
                                else
                                    5.dp
                            )
                            .background(
                                if (isVisible)
                                    androidx.compose.material3.MaterialTheme.colorScheme.primary
                                else
                                    SurfaceElevated,

                                CircleShape
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun AnnouncementCarouselCard(
    announcement: Announcement,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    val accentColor = when {

        announcement.isUrgent ->
            ErrorRed

        announcement.priority >= 5 ->
            PremiumGold

        else ->
            NeonGreen
    }

    androidx.compose.material3.Card(
        onClick   = onClick,
        modifier  = modifier.height(130.dp),
        shape     = RoundedCornerShape(18.dp),
        colors    = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = accentColor.copy(0.10f)
        ),
        elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 0.dp),
        border    = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(0.35f))
    ) {
    Box {

        if (!announcement.imageUrl.isNullOrEmpty()) {

            AsyncImage(
                model =
                announcement.imageUrl ?: "",

                contentDescription =
                null,

                contentScale =
                ContentScale.Crop,

                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(
                        RoundedCornerShape(18.dp)
                    )
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(
                        Color.Black.copy(0.55f)
                    )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .padding(14.dp),

            verticalArrangement =
            Arrangement.SpaceBetween
        ) {

            Row(
                verticalAlignment =
                Alignment.CenterVertically,

                horizontalArrangement =
                Arrangement.spacedBy(6.dp)
            ) {

                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(
                            accentColor.copy(0.2f),
                            CircleShape
                        )
                        .border(
                            1.dp,
                            accentColor.copy(0.5f),
                            CircleShape
                        ),

                    contentAlignment =
                    Alignment.Center
                ) {

                    Icon(
                        if (announcement.isUrgent)
                            Icons.Rounded.PushPin
                        else
                            Icons.Rounded.Campaign,

                        null,

                        tint = accentColor,

                        modifier =
                        Modifier.size(12.dp)
                    )
                }

                Text(
                    if (announcement.isUrgent)
                        "Pinned"
                    else
                        "Announcement",

                    color = accentColor,

                    fontSize = 10.sp,

                    fontWeight =
                    FontWeight.Bold
                )
            }

            Column(
                verticalArrangement =
                Arrangement.spacedBy(4.dp)
            ) {

                Text(
                    announcement.title,

                    color =
                    if (!announcement.imageUrl.isNullOrEmpty())
                        Color.White
                    else
                        TextPrimary,

                    fontWeight =
                    FontWeight.ExtraBold,

                    fontSize = 14.sp,

                    maxLines = 2,

                    overflow =
                    TextOverflow.Ellipsis,

                    lineHeight = 19.sp
                )

                Text(
                    announcement.content
                        .replace("\\n", " ")
                        .take(80),

                    color =
                    if (!announcement.imageUrl.isNullOrEmpty())
                        Color.White.copy(0.8f)
                    else
                        TextMuted,

                    fontSize = 11.sp,

                    maxLines = 1,

                    overflow =
                    TextOverflow.Ellipsis
                )
            }
        }
    }
    }
}

@Composable
fun UrgentAnnouncementTicker(
    urgentAnnouncements: List<Announcement>,
    onItemClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {

    if (urgentAnnouncements.isEmpty()) return

    var tickerIndex by remember {
        mutableStateOf(0)
    }

    val current =
        urgentAnnouncements[
                tickerIndex %
                        urgentAnnouncements.size
        ]

    val transition =
        rememberInfiniteTransition(
            label = "ticker"
        )

    val dotAlpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,

        animationSpec =
        infiniteRepeatable(
            tween(
                600,
                easing = LinearEasing
            ),
            RepeatMode.Reverse
        ),

        label = "tickerDot"
    )

    LaunchedEffect(
        urgentAnnouncements.size
    ) {

        while (
            urgentAnnouncements.size > 1
        ) {

            delay(5000L)
            tickerIndex++
        }
    }

    androidx.compose.material3.Surface(
        onClick   = { onItemClick(current.id) },
        modifier  = modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(10.dp),
        color     = ErrorRed.copy(0.08f),
        border    = androidx.compose.foundation.BorderStroke(1.dp, ErrorRed.copy(0.35f))
    ) {
    Row(
        modifier              = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        Box(
            modifier = Modifier
                .size(7.dp)
                .background(
                    ErrorRed.copy(dotAlpha),
                    CircleShape
                )
        )

        Text(
            "LIVE",

            color = ErrorRed,

            fontSize = 9.sp,

            fontWeight =
            FontWeight.ExtraBold
        )

        Text(
            "·",

            color = TextMuted,

            fontSize = 11.sp
        )

        Text(
            current.title,

            color = TextSecondary,

            fontSize = 12.sp,

            maxLines = 1,

            overflow =
            TextOverflow.Ellipsis,

            modifier =
            Modifier.weight(1f)
        )

        Icon(
            Icons.Rounded.ChevronRight,
            null,

            tint = TextMuted,

            modifier =
            Modifier.size(14.dp)
        )
    }
    }
}

@Composable
fun AnnouncementCarouselWithViewModel(
    onAnnouncementClick: (String) -> Unit,
    viewModel: AnnouncementsViewModel = hiltViewModel()
) {

    val uiState by viewModel.uiState.collectAsState()

    val items =

        if (uiState is AnnouncementsUiState.Success)

            (uiState as AnnouncementsUiState.Success)
                .items

        else

            return

    if (items.isEmpty()) return

    val urgentItems =
        items.filter {
            it.isUrgent
        }

    Column(
        verticalArrangement =
        Arrangement.spacedBy(10.dp)
    ) {

        if (urgentItems.isNotEmpty()) {

            UrgentAnnouncementTicker(
                urgentAnnouncements =
                urgentItems,

                onItemClick =
                onAnnouncementClick,

                modifier =
                Modifier.padding(
                    horizontal = 20.dp
                )
            )
        }

        AnnouncementCarouselSection(
            announcements = items,

            onAnnouncementClick =
            onAnnouncementClick
        )
    }
}
