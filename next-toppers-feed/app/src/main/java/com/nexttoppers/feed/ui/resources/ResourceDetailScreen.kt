package com.nexttoppers.feed.ui.resources

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.RemoveRedEye
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.nexttoppers.feed.data.model.DownloadStatus
import com.nexttoppers.feed.data.model.Resource
import com.nexttoppers.feed.ui.components.NeonDivider
import com.nexttoppers.feed.ui.components.NtfCard
import com.nexttoppers.feed.ui.components.SkeletonCard
import com.nexttoppers.feed.ui.resources.components.TypeChip
import com.nexttoppers.feed.ui.resources.components.resourceTypeAccent
import com.nexttoppers.feed.ui.resources.components.resourceTypeIcon
import com.nexttoppers.feed.ui.theme.BackgroundBlack
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
fun ResourceDetailScreen(
    onBack: () -> Unit,
    onOpenPdf: (localPath: String, resourceId: String, title: String, fileUrl: String) -> Unit,
    onPlayLecture: (url: String, title: String) -> Unit,
    viewModel: ResourceDetailViewModel = hiltViewModel()
) {
    val uiState        by viewModel.uiState.collectAsState()
    val downloadStatus by viewModel.downloadStatus.collectAsState()
    val localPath      by viewModel.localPath.collectAsState()
    val isDownloaded   by viewModel.isDownloaded.collectAsState()

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundBlack)
    ) {
        when (val state = uiState) {
            is ResourceDetailUiState.Loading -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                        .padding(top = 60.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SkeletonCard(height = 220.dp)
                    SkeletonCard(height = 120.dp)
                    SkeletonCard(height = 180.dp)
                }
            }

            is ResourceDetailUiState.Error -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Failed to load resource", color = TextSecondary)
                    Text(state.message, color = TextMuted, fontSize = 12.sp)
                }
            }

            is ResourceDetailUiState.Success -> {
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(350)) + slideInVertically(tween(350)) { it / 8 }
                ) {
                    ResourceDetailContent(
                        resource       = state.resource,
                        downloadStatus = downloadStatus,
                        localPath      = localPath,
                        isDownloaded   = isDownloaded,
                        onStartDownload = viewModel::startDownload,
                        onOpenPdf      = { path -> onOpenPdf(path, state.resource.id, state.resource.title, state.resource.fileUrl) },
                        onPlayLecture  = { onPlayLecture(state.resource.fileUrl, state.resource.title) }
                    )
                }
            }
        }

        IconButton(
            onClick  = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 52.dp, start = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(SurfaceElevated.copy(alpha = 0.9f), RoundedCornerShape(50.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.ArrowBack,
                    contentDescription = null,
                    tint = TextPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun ResourceDetailContent(
    resource: Resource,
    downloadStatus: DownloadStatus,
    localPath: String?,
    isDownloaded: Boolean,
    onStartDownload: () -> Unit,
    onOpenPdf: (String) -> Unit,
    onPlayLecture: () -> Unit
) {
    val context    = LocalContext.current
    val typeAccent = resourceTypeAccent(resource.type)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 48.dp)
    ) {
        // ── Hero thumbnail / play button ─────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .background(typeAccent.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            if (resource.thumbnailUrl.isNotEmpty()) {
                AsyncImage(
                    model            = resource.thumbnailUrl,
                    contentDescription = null,
                    contentScale     = ContentScale.Crop,
                    modifier         = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, BackgroundBlack.copy(alpha = 0.85f)),
                                startY = 60f
                            )
                        )
                )
            } else {
                Icon(
                    imageVector = resourceTypeIcon(resource.type),
                    contentDescription = null,
                    tint     = typeAccent,
                    modifier = Modifier.size(80.dp)
                )
            }

            if (resource.isLecture()) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(BackgroundBlack.copy(alpha = 0.65f), RoundedCornerShape(50.dp))
                        .clickable(onClick = onPlayLecture),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PlayCircle,
                        contentDescription = "Play Lecture",
                        tint     = NeonGreen,
                        modifier = Modifier.size(56.dp)
                    )
                }
            }
        }

        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ── Title + description ─────────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                TypeChip(type = resource.type, accent = typeAccent)

                Text(
                    text = resource.title,
                    style = TextStyle(
                        fontSize   = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color      = TextPrimary,
                        shadow     = Shadow(typeAccent.copy(alpha = 0.2f), Offset.Zero, 8f)
                    )
                )

                if (resource.description.isNotEmpty()) {
                    Text(
                        text       = resource.description,
                        color      = TextSecondary,
                        fontSize   = 14.sp,
                        lineHeight = 22.sp
                    )
                }
            }

            NeonDivider()

            // ── Stats row ───────────────────────────────────────────────────
            NtfCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    DetailStat(
                        icon  = Icons.Rounded.RemoveRedEye,
                        value = "${resource.views}",
                        label = "Views",
                        color = NeonGreen
                    )
                    DetailStat(
                        icon  = Icons.Rounded.CalendarMonth,
                        value = SimpleDateFormat("d MMM yy", Locale.getDefault())
                            .format(resource.createdAt.toDate()),
                        label = "Uploaded",
                        color = NeonCyan
                    )
                    DetailStat(
                        icon  = Icons.Rounded.Person,
                        value = resource.uploadedBy,
                        label = "Author",
                        color = typeAccent
                    )
                }
            }

            if (resource.premium) { PremiumGateCard() }

            NeonDivider()

            // ── Action buttons ──────────────────────────────────────────────
            if (!resource.premium) {
                if (resource.isLecture()) {
                    // ── Lecture: Play button ──────────────────────────────
                    ActionButton(
                        text    = if (resource.isYouTube()) "▶ Watch on YouTube" else "▶ Play Lecture",
                        color   = NeonGreen,
                        enabled = resource.hasPlayableUrl(),
                        onClick = onPlayLecture
                    )
                    if (resource.duration.isNotEmpty()) {
                        Text(
                            "Duration: ${resource.duration}",
                            color    = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                } else {
                    // ── Non-lecture: Download progress ────────────────────
                    AnimatedVisibility(
                        visible = downloadStatus is DownloadStatus.Progress
                                || downloadStatus is DownloadStatus.Queued
                    ) {
                        DownloadProgressBar(status = downloadStatus, accent = typeAccent)
                    }

                    AnimatedVisibility(visible = isDownloaded) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(NeonGreen.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                                .border(1.dp, NeonGreen.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Rounded.CheckCircle, null, tint = NeonGreen, modifier = Modifier.size(22.dp))
                            Spacer(Modifier.width(10.dp))
                            Text("Downloaded — available offline", color = NeonGreen, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        }
                    }

                    // "Read PDF" — offline version (downloaded)
                    if (!localPath.isNullOrEmpty()) {
                        ActionButton(
                            text    = "Read PDF (Offline)",
                            color   = NeonGreen,
                            enabled = true,
                            onClick = { onOpenPdf(localPath) }
                        )
                    } else if (resource.fileUrl.isNotEmpty() && resource.isPdfResource()) {
                        // "Open PDF" — stream from URL even when not downloaded
                        ActionButton(
                            text    = "Open PDF",
                            color   = NeonGreen,
                            enabled = true,
                            onClick = { onOpenPdf("") }   // PdfViewerViewModel uses fileUrl
                        )
                    }

                    // "Open in Browser" — always available as secondary option
                    if (resource.fileUrl.isNotEmpty()) {
                        ActionButton(
                            text    = "Open in Browser",
                            color   = NeonCyan,
                            enabled = true,
                            outlined = true,
                            onClick = {
                                runCatching {
                                    context.startActivity(
                                        Intent(Intent.ACTION_VIEW, Uri.parse(resource.fileUrl))
                                    )
                                }
                            }
                        )
                    }

                    // "Download for Offline" button
                    val isDownloading =
                        downloadStatus is DownloadStatus.Progress ||
                        downloadStatus is DownloadStatus.Queued

                    if (resource.hasDownloadableUrl()) {
                        ActionButton(
                            text = when {
                                isDownloaded  -> "✓ Downloaded"
                                isDownloading -> "Downloading..."
                                else          -> "⬇ Download for Offline"
                            },
                            color   = if (isDownloaded) NeonGreen.copy(alpha = 0.6f) else NeonCyan,
                            enabled = !isDownloaded && !isDownloading,
                            onClick = onStartDownload,
                            outlined = true
                        )
                    }
                }
            }
        }
    }
}

// ── Download progress bar ──────────────────────────────────────────────────────
@Composable
private fun DownloadProgressBar(status: DownloadStatus, accent: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.CloudDownload, null, tint = accent, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                text = when (status) {
                    is DownloadStatus.Progress -> "Downloading... ${status.percent}%"
                    is DownloadStatus.Queued   -> "Queued for download..."
                    else                        -> ""
                },
                color      = accent,
                fontSize   = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
        val progress = if (status is DownloadStatus.Progress) status.percent / 100f else 0f
        val animatedProgress by animateFloatAsState(
            targetValue    = progress,
            animationSpec  = tween(300),
            label          = "download_progress"
        )
        LinearProgressIndicator(
            progress      = { animatedProgress },
            modifier      = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(50.dp)),
            color         = accent,
            trackColor    = accent.copy(alpha = 0.2f),
            strokeCap     = StrokeCap.Round
        )
    }
}

// ── Premium gate card ──────────────────────────────────────────────────────────
@Composable
private fun PremiumGateCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(listOf(PremiumGold.copy(alpha = 0.12f), SurfaceCard)),
                RoundedCornerShape(16.dp)
            )
            .border(1.dp, PremiumGold.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.WorkspacePremium, null, tint = PremiumGold, modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(12.dp))
            Column {
                Text("Premium Content", color = PremiumGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("Upgrade your plan to unlock this resource.", color = TextSecondary, fontSize = 12.sp)
            }
        }
    }
}

// ── Action button ──────────────────────────────────────────────────────────────
@Composable
private fun ActionButton(
    text: String,
    color: Color,
    enabled: Boolean,
    onClick: () -> Unit,
    outlined: Boolean = false
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (outlined) color.copy(alpha = 0.07f)
                else if (enabled) color else SurfaceElevated
            )
            .border(
                1.dp,
                if (enabled) color.copy(alpha = 0.6f) else color.copy(alpha = 0.2f),
                RoundedCornerShape(14.dp)
            )
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text       = text,
            color      = if (outlined) color else if (enabled) BackgroundBlack else TextMuted,
            fontWeight = FontWeight.Bold,
            fontSize   = 15.sp
        )
    }
}

// ── Stat chip ──────────────────────────────────────────────────────────────────
@Composable
private fun DetailStat(icon: ImageVector, value: String, label: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
        Text(value, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Text(label, color = TextMuted, fontSize = 10.sp)
    }
}
