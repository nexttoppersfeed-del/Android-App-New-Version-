package com.nexttoppers.feed.ui.downloads

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.LibraryBooks
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nexttoppers.feed.data.model.DownloadedResource
import com.nexttoppers.feed.data.model.RecentlyOpened
import com.nexttoppers.feed.ui.components.NeonDivider
import com.nexttoppers.feed.ui.components.SectionHeader
import com.nexttoppers.feed.ui.resources.components.resourceTypeAccent
import com.nexttoppers.feed.ui.resources.components.resourceTypeIcon
import com.nexttoppers.feed.ui.theme.BackgroundBlack
import com.nexttoppers.feed.ui.theme.NeonCyan
import com.nexttoppers.feed.ui.theme.NeonGreen
import com.nexttoppers.feed.ui.theme.SurfaceCard
import com.nexttoppers.feed.ui.theme.TextMuted
import com.nexttoppers.feed.ui.theme.TextPrimary
import com.nexttoppers.feed.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DownloadsScreen(
    onOpenPdf: (
        localPath: String,
        resourceId: String,
        title: String
    ) -> Unit,

    onNavigateToDetail: (String) -> Unit,

    viewModel: DownloadsViewModel = hiltViewModel()
) {

    val downloads by viewModel.downloads.collectAsState()
    val recents by viewModel.recents.collectAsState()
    val deleteMsg by viewModel.deleteSuccess.collectAsState()

    var showClearDialog by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(deleteMsg) {

        if (deleteMsg != null) {
            delay(2000)
            viewModel.clearDeleteSuccess()
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(BackgroundBlack)
    ) {

        LazyColumn(
            modifier = Modifier.fillMaxSize(),

            contentPadding = PaddingValues(
                horizontal = 20.dp,
                vertical = 100.dp
            ),

            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            item {

                Text(
                    text = "Offline Library",

                    style = TextStyle(
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,

                        brush = Brush.linearGradient(
                            listOf(
                                NeonGreen,
                                NeonCyan
                            )
                        ),

                        shadow = Shadow(
                            NeonGreen.copy(0.3f),
                            Offset.Zero,
                            14f
                        )
                    )
                )

                Text(
                    "Study anywhere, anytime",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            }

            if (recents.isNotEmpty()) {

                item {

                    SectionHeader(
                        "Recently Opened"
                    )

                    Spacer(Modifier.height(10.dp))

                    LazyRow(
                        horizontalArrangement =
                        Arrangement.spacedBy(10.dp)
                    ) {

                        itemsIndexed(recents) { _, recent ->

                            RecentMiniCard(
                                item = recent,
                                onClick = {

                                    if (recent.localPath.isNotEmpty()) {

                                        onOpenPdf(
                                            recent.localPath,
                                            recent.resourceId,
                                            recent.title
                                        )

                                    } else {

                                        onNavigateToDetail(
                                            recent.resourceId
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
            }

            item {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    SectionHeader(
                        if (downloads.isEmpty())
                            "No Downloads Yet"
                        else
                            "Downloaded Files"
                    )

                    Spacer(Modifier.weight(1f))

                    if (downloads.isNotEmpty()) {

                        IconButton(
                            onClick = {
                                showClearDialog = true
                            }
                        ) {

                            Icon(
                                Icons.Rounded.DeleteSweep,
                                contentDescription = null,
                                tint = Color.Red
                            )
                        }
                    }
                }

                NeonDivider()
            }

            if (downloads.isEmpty()) {

                item {
                    EmptyDownloadsState()
                }
            }

            itemsIndexed(downloads) { index, resource ->

                DownloadedResourceCard(
                    resource = resource,
                    index = index,

                    onOpen = {

                        onOpenPdf(
                            resource.localPath,
                            resource.id,
                            resource.title
                        )
                    },

                    onDelete = {
                        viewModel.deleteDownload(resource)
                    }
                )
            }
        }

        AnimatedVisibility(
            visible = deleteMsg != null,

            enter = fadeIn(
                animationSpec = tween(200)
            ),

            exit = fadeOut(
                animationSpec = tween(200)
            ),

            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 110.dp)
        ) {

            Box(
                modifier = Modifier
                    .background(
                        SurfaceCard,
                        RoundedCornerShape(12.dp)
                    )
                    .padding(
                        horizontal = 20.dp,
                        vertical = 12.dp
                    )
            ) {

                Text(
                    text = deleteMsg ?: "",
                    color = TextPrimary
                )
            }
        }

        if (showClearDialog) {

            AlertDialog(
                onDismissRequest = {
                    showClearDialog = false
                },

                title = {
                    Text(
                        "Clear All Downloads?",
                        color = TextPrimary
                    )
                },

                text = {
                    Text(
                        "Delete all downloaded files?",
                        color = TextSecondary
                    )
                },

                confirmButton = {

                    TextButton(
                        onClick = {

                            viewModel.clearAllDownloads()
                            showClearDialog = false
                        }
                    ) {

                        Text(
                            "Clear",
                            color = Color.Red
                        )
                    }
                },

                dismissButton = {

                    TextButton(
                        onClick = {
                            showClearDialog = false
                        }
                    ) {

                        Text(
                            "Cancel",
                            color = TextMuted
                        )
                    }
                }
            )
        }
    }
}

@Composable
private fun DownloadedResourceCard(
    resource: DownloadedResource,
    index: Int,
    onOpen: () -> Unit,
    onDelete: () -> Unit
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
                280,
                delayMillis = index * 50
            )
        ) +
                slideInVertically(
                    tween(
                        280,
                        delayMillis = index * 50
                    )
                ) {
                    it / 4
                }
    ) {

        val accent =
            resourceTypeAccent(resource.type)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceCard)
                .border(
                    1.dp,
                    accent.copy(0.2f),
                    RoundedCornerShape(16.dp)
                )
                .padding(14.dp),

            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(
                        accent.copy(0.12f),
                        RoundedCornerShape(13.dp)
                    ),

                contentAlignment = Alignment.Center
            ) {

                Icon(
                    resourceTypeIcon(resource.type),
                    contentDescription = null,
                    tint = accent
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = resource.title,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = resource.subject,
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }

            IconButton(
                onClick = onOpen
            ) {

                Icon(
                    Icons.Rounded.OpenInNew,
                    contentDescription = null,
                    tint = NeonGreen
                )
            }

            IconButton(
                onClick = onDelete
            ) {

                Icon(
                    Icons.Rounded.Delete,
                    contentDescription = null,
                    tint = Color.Red
                )
            }
        }
    }
}

@Composable
private fun RecentMiniCard(
    item: RecentlyOpened,
    onClick: () -> Unit
) {

    Column(
        modifier = Modifier
            .width(130.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceCard)
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {

        Text(
            text = item.title,
            color = TextPrimary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = SimpleDateFormat(
                "h:mm a",
                Locale.getDefault()
            ).format(Date(item.openedAt)),

            color = TextMuted,
            fontSize = 10.sp
        )
    }
}

@Composable
private fun EmptyDownloadsState() {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),

        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Icon(
            Icons.Rounded.LibraryBooks,
            contentDescription = null,
            tint = NeonGreen.copy(0.3f),
            modifier = Modifier.size(72.dp)
        )

        Spacer(Modifier.height(12.dp))

        Text(
            "No offline files yet",
            color = TextSecondary,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.height(8.dp))

        Text(
            "Download notes and PDFs to access offline.",
            color = TextMuted,
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )
    }
}
