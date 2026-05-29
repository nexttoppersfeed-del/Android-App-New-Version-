package com.nexttoppers.feed.ui.resources.components

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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Quiz
import androidx.compose.material.icons.rounded.RemoveRedEye
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.nexttoppers.feed.data.model.Resource
import com.nexttoppers.feed.data.model.ResourceType
import com.nexttoppers.feed.ui.components.shimmerEffect
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

// ── List card (default) ────────────────────────────────────────────────────────
@Composable
fun ResourceListCard(
    resource: Resource,
    index: Int = 0,
    onClick: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(280, delayMillis = index * 60)) +
                slideInVertically(tween(280, delayMillis = index * 60)) { it / 4 }
    ) {
        val typeAccent = resourceTypeAccent(resource.type)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceCard)
                .border(1.dp, typeAccent.copy(0.2f), RoundedCornerShape(16.dp))
                .clickable(onClick = onClick)
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail / type icon box
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(typeAccent.copy(0.12f)),
                contentAlignment = Alignment.Center
            ) {
                if (resource.thumbnailUrl.isNotEmpty()) {
                    AsyncImage(
                        model = resource.thumbnailUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(14.dp))
                    )
                } else {
                    Icon(
                        imageVector = resourceTypeIcon(resource.type),
                        contentDescription = null,
                        tint = typeAccent,
                        modifier = Modifier.size(28.dp)
                    )
                }
                if (resource.premium) {
                    Box(
                        modifier = Modifier.align(Alignment.TopEnd)
                            .size(16.dp)
                            .background(PremiumGold, RoundedCornerShape(4.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.Lock, null, tint = BackgroundBlack, modifier = Modifier.size(10.dp))
                    }
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                // Type chip
                TypeChip(type = resource.type, accent = typeAccent)
                Text(
                    resource.title,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (resource.isLecture() && resource.duration.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.AccessTime, null, tint = TextMuted, modifier = Modifier.size(11.dp))
                            Spacer(Modifier.width(3.dp))
                            Text(resource.duration, color = TextMuted, fontSize = 11.sp)
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.RemoveRedEye, null, tint = TextMuted, modifier = Modifier.size(11.dp))
                        Spacer(Modifier.width(3.dp))
                        Text("${resource.views}", color = TextMuted, fontSize = 11.sp)
                    }
                    Text(
                        SimpleDateFormat("d MMM", Locale.getDefault()).format(resource.createdAt.toDate()),
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            if (resource.isLecture()) {
                Icon(Icons.Rounded.PlayCircle, null, tint = typeAccent, modifier = Modifier.size(28.dp))
            }
        }
    }
}

// ── Grid card ──────────────────────────────────────────────────────────────────
@Composable
fun ResourceGridCard(resource: Resource, index: Int = 0, onClick: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(300, delayMillis = index * 50))
    ) {
        val typeAccent = resourceTypeAccent(resource.type)
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(18.dp))
                .background(SurfaceCard)
                .border(1.dp, typeAccent.copy(0.2f), RoundedCornerShape(18.dp))
                .clickable(onClick = onClick)
        ) {
            // Thumbnail area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .background(typeAccent.copy(0.1f)),
                contentAlignment = Alignment.Center
            ) {
                if (resource.thumbnailUrl.isNotEmpty()) {
                    AsyncImage(
                        model = resource.thumbnailUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(resourceTypeIcon(resource.type), null, tint = typeAccent, modifier = Modifier.size(34.dp))
                }
                if (resource.premium) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .background(PremiumGold, RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("PRO", color = BackgroundBlack, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                    }
                }
                if (resource.isLecture()) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(BackgroundBlack.copy(0.6f), RoundedCornerShape(50.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.PlayCircle, null, tint = NeonGreen, modifier = Modifier.size(28.dp))
                    }
                }
            }
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                TypeChip(type = resource.type, accent = typeAccent)
                Text(resource.title, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text("${resource.views} views", color = TextMuted, fontSize = 10.sp)
            }
        }
    }
}

// ── Recent resource mini-card (horizontal scroll) ──────────────────────────────
@Composable
fun ResourceMiniCard(resource: Resource, onClick: () -> Unit) {
    val typeAccent = resourceTypeAccent(resource.type)
    Column(
        modifier = Modifier
            .width(150.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceCard)
            .border(1.dp, typeAccent.copy(0.2f), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().height(80.dp).background(typeAccent.copy(0.1f)),
            contentAlignment = Alignment.Center
        ) {
            if (resource.thumbnailUrl.isNotEmpty()) {
                AsyncImage(
                    model = resource.thumbnailUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(resourceTypeIcon(resource.type), null, tint = typeAccent, modifier = Modifier.size(28.dp))
            }
        }
        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(resource.type.lowercase().replaceFirstChar { it.uppercase() }, color = typeAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text(resource.title, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

// ── Type chip ──────────────────────────────────────────────────────────────────
@Composable
fun TypeChip(type: String, accent: Color) {
    val typeEnum = ResourceType.values().firstOrNull { it.name.equals(type, ignoreCase = true) }
    val label = typeEnum?.let { "${it.emoji} ${it.displayName}" } ?: type
    Box(
        modifier = Modifier
            .background(accent.copy(0.12f), RoundedCornerShape(6.dp))
            .border(1.dp, accent.copy(0.3f), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(label, color = accent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────
fun resourceTypeIcon(type: String): ImageVector = when (type.uppercase()) {
    "LECTURE"  -> Icons.Rounded.PlayCircle
    "PDF"      -> Icons.Rounded.PictureAsPdf
    "NOTES"    -> Icons.Rounded.AutoStories
    "MODULE"   -> Icons.Rounded.MenuBook
    "DPP"      -> Icons.Rounded.Quiz
    "PRACTICE" -> Icons.Rounded.Description
    else       -> Icons.Rounded.AutoStories
}

fun resourceTypeAccent(type: String): Color = when (type.uppercase()) {
    "LECTURE"  -> NeonCyan
    "PDF"      -> Color(0xFFFF6B6B)
    "NOTES"    -> NeonGreen
    "MODULE"   -> Color(0xFFB388FF)
    "DPP"      -> Color(0xFFFFB347)
    "PRACTICE" -> Color(0xFF80CBC4)
    else       -> NeonGreen
}
