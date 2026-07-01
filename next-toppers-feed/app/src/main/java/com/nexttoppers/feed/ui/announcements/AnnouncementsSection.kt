package com.nexttoppers.feed.ui.announcements

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.nexttoppers.feed.data.model.Announcement
import com.nexttoppers.feed.ui.components.SkeletonCard
import com.nexttoppers.feed.ui.theme.ErrorRed
import com.nexttoppers.feed.ui.theme.NeonGreen
import com.nexttoppers.feed.ui.theme.SurfaceCard
import com.nexttoppers.feed.ui.theme.TextMuted
import com.nexttoppers.feed.ui.theme.TextPrimary
import com.nexttoppers.feed.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun AnnouncementsSection(
    onAnnouncementClick: (String) -> Unit = {},
    viewModel: AnnouncementsViewModel = hiltViewModel()
) {

    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier.fillMaxWidth()
    ) {

        Column(
            modifier = Modifier.fillMaxWidth(),

            verticalArrangement =
            Arrangement.spacedBy(12.dp)
        ) {

            when (val state = uiState) {

                is AnnouncementsUiState.Loading -> {

                    repeat(3) {

                        SkeletonCard(
                            height = 100.dp
                        )
                    }
                }

                is AnnouncementsUiState.Empty -> {

                    EmptyAnnouncementsState()
                }

                is AnnouncementsUiState.Error -> {

                    AnnouncementsErrorState(
                        message = state.message,

                        onRetry = {
                            viewModel.refresh()
                        }
                    )
                }

                is AnnouncementsUiState.Success -> {

                    state.items.forEachIndexed {
                            index,
                            announcement ->

                        AnnouncementCard(
                            announcement =
                            announcement,

                            index = index,

                            onClick = {
                                onAnnouncementClick(
                                    announcement.id
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AnnouncementCard(
    announcement: Announcement,
    index: Int,
    onClick: () -> Unit = {}
) {

    var visible by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {
        visible = true
    }

    AnimatedVisibility(
        visible = visible,

        enter =
        fadeIn(
            tween(
                300,
                delayMillis = index * 80
            )
        ) +
                slideInVertically(
                    tween(
                        300,
                        delayMillis = index * 80
                    )
                ) {
                    it / 4
                }
    ) {

        val borderColor =

            if (announcement.isUrgent)

                ErrorRed.copy(0.5f)

            else

                NeonGreen.copy(0.18f)

        val bgGradient =

            if (announcement.isUrgent)

                Brush.horizontalGradient(
                    listOf(
                        ErrorRed.copy(0.06f),
                        SurfaceCard
                    )
                )

            else

                Brush.horizontalGradient(
                    listOf(
                        NeonGreen.copy(0.04f),
                        SurfaceCard
                    )
                )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(
                    RoundedCornerShape(18.dp)
                )
                .background(bgGradient)
                .border(
                    1.dp,
                    borderColor,
                    RoundedCornerShape(18.dp)
                )
                .clickable(
                    onClick = onClick
                )
                .padding(16.dp)
        ) {

            Column(
                verticalArrangement =
                Arrangement.spacedBy(8.dp)
            ) {

                Row(
                    verticalAlignment =
                    Alignment.CenterVertically,

                    modifier =
                    Modifier.fillMaxWidth()
                ) {

                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(

                                if (announcement.isUrgent)

                                    ErrorRed.copy(0.15f)

                                else

                                    NeonGreen.copy(0.12f),

                                RoundedCornerShape(10.dp)
                            ),

                        contentAlignment =
                        Alignment.Center
                    ) {

                        Icon(

                            if (announcement.isUrgent)

                                Icons.Rounded.Error

                            else

                                Icons.Rounded.Campaign,

                            contentDescription = null,

                            tint =

                            if (announcement.isUrgent)

                                ErrorRed

                            else

                                NeonGreen,

                            modifier =
                            Modifier.size(18.dp)
                        )
                    }

                    Spacer(
                        Modifier.width(10.dp)
                    )

                    Column(
                        modifier =
                        Modifier.weight(1f)
                    ) {

                        Text(
                            text =
                            announcement.title,

                            color = TextPrimary,

                            fontWeight =
                            FontWeight.Bold,

                            fontSize = 14.sp
                        )

                        Text(
                            text =
                            announcement.author,

                            color = TextMuted,

                            fontSize = 11.sp
                        )
                    }

                    if (announcement.isUrgent) {

                        Row(
                            verticalAlignment =
                            Alignment.CenterVertically,

                            modifier = Modifier
                                .background(
                                    ErrorRed.copy(0.15f),

                                    RoundedCornerShape(6.dp)
                                )
                                .border(
                                    1.dp,
                                    ErrorRed.copy(0.4f),

                                    RoundedCornerShape(6.dp)
                                )
                                .padding(
                                    horizontal = 6.dp,
                                    vertical = 2.dp
                                )
                        ) {

                            Icon(
                                Icons.Rounded.PushPin,

                                contentDescription = null,

                                tint = ErrorRed,

                                modifier =
                                Modifier.size(10.dp)
                            )

                            Spacer(
                                Modifier.width(3.dp)
                            )

                            Text(
                                text = "Pinned",

                                color = ErrorRed,

                                fontSize = 9.sp,

                                fontWeight =
                                FontWeight.Bold
                            )
                        }
                    }
                }

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
                            .height(140.dp)
                            .clip(
                                RoundedCornerShape(12.dp)
                            )
                    )
                }

                announcement.content
                    .replace("\\n", "\n")
                    .split("\n")
                    .forEach { line ->

                        Text(
                            text =
                            line.ifEmpty { " " },

                            color = TextSecondary,

                            fontSize = 13.sp,

                            lineHeight = 20.sp
                        )
                    }

                val formatted =
                    SimpleDateFormat(
                        "d MMM yyyy • h:mm a",
                        Locale.getDefault()
                    ).format(
                        announcement.createdAt
                            .toDate()
                    )

                Text(
                    text = formatted,

                    color = TextMuted,

                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
private fun EmptyAnnouncementsState() {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),

        horizontalAlignment =
        Alignment.CenterHorizontally,

        verticalArrangement =
        Arrangement.spacedBy(12.dp)
    ) {

        Icon(
            Icons.Rounded.NotificationsNone,

            contentDescription = null,

            tint =
            NeonGreen.copy(0.4f),

            modifier =
            Modifier.size(56.dp)
        )

        Text(
            text = "No announcements yet",

            color = TextSecondary,

            fontSize = 14.sp
        )

        Text(
            text =
            "Check back later for updates",

            color = TextMuted,

            fontSize = 12.sp
        )
    }
}

@Composable
private fun AnnouncementsErrorState(
    message: String,
    onRetry: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),

        horizontalAlignment =
        Alignment.CenterHorizontally,

        verticalArrangement =
        Arrangement.spacedBy(12.dp)
    ) {

        Icon(
            Icons.Rounded.Error,

            contentDescription = null,

            tint =
            ErrorRed.copy(0.6f),

            modifier =
            Modifier.size(40.dp)
        )

        Text(
            text =
            "Failed to load announcements",

            color = TextSecondary,

            fontSize = 13.sp
        )

        Text(
            text = message,

            color = TextMuted,

            fontSize = 11.sp
        )

        IconButton(
            onClick = onRetry
        ) {

            Icon(
                Icons.Rounded.Refresh,

                contentDescription = null,

                tint = NeonGreen
            )
        }
    }
}
