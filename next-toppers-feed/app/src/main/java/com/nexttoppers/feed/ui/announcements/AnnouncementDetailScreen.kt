package com.nexttoppers.feed.ui.announcements

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material3.CircularProgressIndicator
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
import com.nexttoppers.feed.ui.theme.BackgroundBlack
import com.nexttoppers.feed.ui.theme.ErrorRed
import com.nexttoppers.feed.ui.theme.NeonCyan
import com.nexttoppers.feed.ui.theme.NeonGreen
import com.nexttoppers.feed.ui.theme.PremiumGold
import com.nexttoppers.feed.ui.theme.SurfaceCard
import com.nexttoppers.feed.ui.theme.SurfaceElevated
import com.nexttoppers.feed.ui.theme.TextMuted
import com.nexttoppers.feed.ui.theme.TextPrimary
import com.nexttoppers.feed.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun AnnouncementDetailScreen(
    onBack: () -> Unit,
    viewModel: AnnouncementDetailViewModel = hiltViewModel()
) {

    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundBlack)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 12.dp,
                    vertical = 10.dp
                ),

            verticalAlignment =
            Alignment.CenterVertically
        ) {

            IconButton(
                onClick = onBack
            ) {

                Icon(
                    Icons.Rounded.ArrowBackIosNew,
                    null,

                    tint = TextSecondary,

                    modifier = Modifier.size(18.dp)
                )
            }

            Text(
                "Announcement",

                color = TextPrimary,

                fontWeight =
                FontWeight.ExtraBold,

                fontSize = 20.sp
            )
        }

        when (val state = uiState) {

            is AnnouncementDetailUiState.Loading -> {

                Box(
                    Modifier.fillMaxSize(),

                    contentAlignment =
                    Alignment.Center
                ) {

                    CircularProgressIndicator(
                        color = NeonGreen,

                        modifier =
                        Modifier.size(40.dp)
                    )
                }
            }

            is AnnouncementDetailUiState.Error -> {

                Box(
                    Modifier.fillMaxSize(),

                    contentAlignment =
                    Alignment.Center
                ) {

                    Column(
                        horizontalAlignment =
                        Alignment.CenterHorizontally,

                        verticalArrangement =
                        Arrangement.spacedBy(12.dp)
                    ) {

                        Icon(
                            Icons.Rounded.Error,
                            null,

                            tint = ErrorRed,

                            modifier =
                            Modifier.size(48.dp)
                        )

                        Text(
                            state.message,

                            color = TextSecondary,

                            fontSize = 14.sp
                        )
                    }
                }
            }

            is AnnouncementDetailUiState.Success -> {

                AnnouncementDetailContent(
                    state.announcement
                )
            }
        }
    }
}

@Composable
private fun AnnouncementDetailContent(
    announcement: Announcement
) {

    var visible by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {
        visible = true
    }

    val accentColor = when {

        announcement.isUrgent ->
            ErrorRed

        announcement.priority >= 5 ->
            PremiumGold

        else ->
            NeonGreen
    }

    AnimatedVisibility(
        visible = visible,

        enter =
        fadeIn(tween(350)) +
                slideInVertically(
                    tween(350)
                ) { it / 8 }
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(
                    rememberScrollState()
                )
                .padding(horizontal = 20.dp),

            verticalArrangement =
            Arrangement.spacedBy(16.dp)
        ) {

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
                        .height(200.dp)
                        .clip(
                            RoundedCornerShape(18.dp)
                        )
                )
            }

            if (announcement.isUrgent) {

                Row(
                    modifier = Modifier
                        .clip(
                            RoundedCornerShape(8.dp)
                        )
                        .background(
                            accentColor.copy(0.12f)
                        )
                        .border(
                            1.dp,
                            accentColor.copy(0.4f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(
                            horizontal = 10.dp,
                            vertical = 5.dp
                        ),

                    horizontalArrangement =
                    Arrangement.spacedBy(6.dp),

                    verticalAlignment =
                    Alignment.CenterVertically
                ) {

                    Icon(
                        Icons.Rounded.PushPin,
                        null,

                        tint = accentColor,

                        modifier =
                        Modifier.size(12.dp)
                    )

                    Text(
                        "Pinned · Important",

                        color = accentColor,

                        fontWeight =
                        FontWeight.Bold,

                        fontSize = 11.sp
                    )
                }
            }

            Text(
                announcement.title,

                color = TextPrimary,

                fontWeight =
                FontWeight.ExtraBold,

                fontSize = 22.sp,

                lineHeight = 30.sp
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(
                        RoundedCornerShape(12.dp)
                    )
                    .background(SurfaceCard)
                    .padding(12.dp),

                horizontalArrangement =
                Arrangement.spacedBy(16.dp),

                verticalAlignment =
                Alignment.CenterVertically
            ) {

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            accentColor.copy(0.12f),
                            RoundedCornerShape(10.dp)
                        ),

                    contentAlignment =
                    Alignment.Center
                ) {

                    Icon(
                        Icons.Rounded.Campaign,
                        null,

                        tint = accentColor,

                        modifier =
                        Modifier.size(18.dp)
                    )
                }

                Column(
                    verticalArrangement =
                    Arrangement.spacedBy(2.dp)
                ) {

                    Text(
                        "Posted by ${announcement.author}",

                        color = TextSecondary,

                        fontSize = 13.sp,

                        fontWeight =
                        FontWeight.SemiBold
                    )

                    val formatted =
                        SimpleDateFormat(
                            "EEEE, d MMMM yyyy • h:mm a",
                            Locale.getDefault()
                        ).format(
                            announcement.createdAt.toDate()
                        )

                    Text(
                        formatted,

                        color = TextMuted,

                        fontSize = 11.sp
                    )
                }
            }

            if (announcement.targetAudience != "all") {

                Row(
                    modifier = Modifier
                        .clip(
                            RoundedCornerShape(8.dp)
                        )
                        .background(
                            NeonCyan.copy(0.08f)
                        )
                        .border(
                            1.dp,
                            NeonCyan.copy(0.3f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(
                            horizontal = 10.dp,
                            vertical = 5.dp
                        ),

                    horizontalArrangement =
                    Arrangement.spacedBy(6.dp),

                    verticalAlignment =
                    Alignment.CenterVertically
                ) {

                    Text(
                        "👥 For: ${
                            announcement.targetAudience
                                .replaceFirstChar {
                                    it.uppercase()
                                }
                        } users",

                        color = NeonCyan,

                        fontSize = 11.sp,

                        fontWeight =
                        FontWeight.SemiBold
                    )
                }
            }

            Box(
                Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(SurfaceElevated)
            )

            val lines =
                announcement.message
                    .replace("\\n", "\n")
                    .split("\n")

            Column(
                verticalArrangement =
                Arrangement.spacedBy(6.dp)
            ) {

                lines.forEach { line ->

                    if (line.isEmpty()) {

                        Spacer(
                            Modifier.height(6.dp)
                        )

                    } else {

                        Text(
                            line,

                            color = TextSecondary,

                            fontSize = 14.sp,

                            lineHeight = 22.sp
                        )
                    }
                }
            }

            Spacer(
                Modifier.height(40.dp)
            )
        }
    }
}
