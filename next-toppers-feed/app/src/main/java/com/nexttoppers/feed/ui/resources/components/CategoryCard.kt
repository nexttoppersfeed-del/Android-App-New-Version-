package com.nexttoppers.feed.ui.resources.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
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
import androidx.compose.material.icons.rounded.ChevronRight
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexttoppers.feed.data.model.ResourceSubject
import com.nexttoppers.feed.ui.resources.SubjectCount
import com.nexttoppers.feed.ui.theme.NeonCyan
import com.nexttoppers.feed.ui.theme.NeonGreen
import com.nexttoppers.feed.ui.theme.PremiumGold
import com.nexttoppers.feed.ui.theme.SurfaceCard
import com.nexttoppers.feed.ui.theme.SurfaceElevated
import com.nexttoppers.feed.ui.theme.TextMuted
import com.nexttoppers.feed.ui.theme.TextPrimary
import com.nexttoppers.feed.ui.theme.TextSecondary

@Composable
fun CategoryCard(
    subjectCount: SubjectCount,
    index: Int,
    onClick: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(300, delayMillis = index * 70)) +
                scaleIn(tween(300, delayMillis = index * 70), initialScale = 0.92f)
    ) {
        val accent = subjectAccentColor(subjectCount.subject)
        val isPremium = subjectCount.subject == ResourceSubject.PREMIUM

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(
                    if (isPremium)
                        Brush.horizontalGradient(listOf(PremiumGold.copy(0.15f), SurfaceCard))
                    else
                        Brush.horizontalGradient(listOf(accent.copy(0.1f), SurfaceCard))
                )
                .border(
                    1.dp,
                    if (isPremium) PremiumGold.copy(0.4f) else accent.copy(0.25f),
                    RoundedCornerShape(20.dp)
                )
                .clickable(onClick = onClick)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Emoji icon box
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(accent.copy(0.15f), RoundedCornerShape(16.dp))
                    .border(1.dp, accent.copy(0.3f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(subjectCount.subject.emoji, fontSize = 24.sp)
            }

            Spacer(Modifier.width(14.dp))

            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        subjectCount.subject.displayName,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    if (isPremium) {
                        Spacer(Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .background(PremiumGold.copy(0.2f), RoundedCornerShape(6.dp))
                                .border(1.dp, PremiumGold.copy(0.5f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("PRO", color = PremiumGold, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    if (subjectCount.count == 0) "No resources yet"
                    else "${subjectCount.count} resource${if (subjectCount.count != 1) "s" else ""}",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }

            Icon(
                Icons.Rounded.ChevronRight,
                null,
                tint = accent.copy(0.7f),
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

// ── Compact 2-column grid variant ────────────────────────────────────────────
@Composable
fun CategoryGridCard(
    subjectCount: SubjectCount,
    index: Int,
    onClick: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(280, delayMillis = index * 60)) +
                scaleIn(tween(280, delayMillis = index * 60), initialScale = 0.9f)
    ) {
        val accent = subjectAccentColor(subjectCount.subject)
        val isPremium = subjectCount.subject == ResourceSubject.PREMIUM
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(18.dp))
                .background(SurfaceCard)
                .border(1.dp, accent.copy(0.25f), RoundedCornerShape(18.dp))
                .clickable(onClick = onClick)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(accent.copy(0.12f), RoundedCornerShape(18.dp))
                    .border(1.dp, accent.copy(0.3f), RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(subjectCount.subject.emoji, fontSize = 26.sp)
            }
            Text(subjectCount.subject.displayName, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(
                "${subjectCount.count} items",
                color = TextMuted,
                fontSize = 11.sp
            )
            if (isPremium) {
                Box(
                    modifier = Modifier
                        .background(PremiumGold.copy(0.15f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("PRO", color = PremiumGold, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}

fun subjectAccentColor(subject: ResourceSubject): Color = when (subject) {
    ResourceSubject.MATHS   -> NeonCyan
    ResourceSubject.SCIENCE -> Color(0xFF80CBC4)
    ResourceSubject.SST     -> Color(0xFFFFB347)
    ResourceSubject.ENGLISH -> NeonGreen
    ResourceSubject.HINDI   -> Color(0xFFFF8A65)
    ResourceSubject.PREMIUM -> PremiumGold
}
