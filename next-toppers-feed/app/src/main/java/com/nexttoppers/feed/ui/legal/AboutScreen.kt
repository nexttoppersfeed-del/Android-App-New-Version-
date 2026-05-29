package com.nexttoppers.feed.ui.legal

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.RateReview
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.nexttoppers.feed.R
import com.nexttoppers.feed.ui.theme.BackgroundBlack
import com.nexttoppers.feed.ui.theme.NeonCyan
import com.nexttoppers.feed.ui.theme.NeonGreen
import com.nexttoppers.feed.ui.theme.PremiumGold
import com.nexttoppers.feed.ui.theme.SurfaceCard
import com.nexttoppers.feed.ui.theme.TextMuted
import com.nexttoppers.feed.ui.theme.TextPrimary
import com.nexttoppers.feed.ui.theme.TextSecondary

@Composable
fun AboutScreen(onBack: () -> Unit) {
    val context    = LocalContext.current
    var visible    by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val appVersion = remember {
        try { context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0.0" }
        catch (_: PackageManager.NameNotFoundException) { "1.0.0" }
    }

    val infinite = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infinite.animateFloat(
        0.15f, 0.35f,
        animationSpec = infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "glowAlpha"
    )

    Box(Modifier.fillMaxSize().background(BackgroundBlack)) {
        Box(
            Modifier.fillMaxWidth().height(280.dp)
                .background(
                    Brush.radialGradient(
                        listOf(NeonGreen.copy(0.1f), NeonCyan.copy(0.05f), Color.Transparent)
                    )
                )
        )

        AnimatedVisibility(
            visible = visible,
            enter   = fadeIn(tween(350)) + slideInVertically(tween(350)) { it / 8 }
        ) {
            LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 56.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                // Header
                item {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
                            Icon(Icons.Rounded.ArrowBack, null, tint = TextSecondary)
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "About",
                            style = TextStyle(
                                fontSize = 22.sp, fontWeight = FontWeight.ExtraBold,
                                brush = Brush.linearGradient(listOf(NeonGreen, NeonCyan)),
                                shadow = Shadow(NeonGreen.copy(0.4f), Offset.Zero, 12f)
                            )
                        )
                    }
                    Spacer(Modifier.height(24.dp))
                }

                // Logo + app name
                item {
                    Column(
                        Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .background(
                                    Brush.radialGradient(listOf(NeonGreen.copy(glowAlpha), Color.Transparent))
                                )
                                .clip(RoundedCornerShape(28.dp))
                                .border(
                                    1.dp,
                                    Brush.linearGradient(listOf(NeonGreen.copy(0.5f), NeonCyan.copy(0.3f))),
                                    RoundedCornerShape(28.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = R.drawable.ntf_logo,
                                contentDescription = "App Logo",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.size(88.dp)
                            )
                        }

                        Text(
                            "Next Toppers Feed",
                            style = TextStyle(
                                fontSize = 24.sp, fontWeight = FontWeight.ExtraBold,
                                brush = Brush.linearGradient(listOf(NeonGreen, NeonCyan)),
                                shadow = Shadow(NeonGreen.copy(0.3f), Offset.Zero, 14f)
                            )
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            VersionChip("v$appVersion", NeonGreen)
                            VersionChip("Firebase", NeonCyan)
                            VersionChip("Jetpack Compose", PremiumGold)
                        }

                        Text(
                            "Your Premium Learning Universe",
                            color = TextSecondary, fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                    Spacer(Modifier.height(28.dp))
                }

                // Mission
                item {
                    Box(
                        Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(NeonGreen.copy(0.07f), SurfaceCard)
                                )
                            )
                            .border(1.dp, NeonGreen.copy(0.2f), RoundedCornerShape(18.dp))
                            .padding(18.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("🎯 Our Mission", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(
                                "Next Toppers Feed is built to make quality education accessible to every student. We combine curated study resources, gamified learning (XP, streaks, leaderboards), and an active community to help you reach your full potential.",
                                color = TextSecondary, fontSize = 13.sp, lineHeight = 20.sp
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }

                // Tech stack
                item {
                    AboutSection("Built With") {
                        val stack = listOf(
                            "Kotlin & Jetpack Compose" to NeonGreen,
                            "Firebase Firestore + Storage" to NeonCyan,
                            "Hilt Dependency Injection" to PremiumGold,
                            "MVVM + Repository Pattern" to Color(0xFFB388FF),
                            "Coil Image Loading" to Color(0xFFFF6B35)
                        )
                        stack.forEach { (item, color) ->
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Box(Modifier.size(6.dp).clip(CircleShape).background(color))
                                Text(item, color = TextSecondary, fontSize = 13.sp)
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }

                // Links
                item {
                    AboutSection("Connect With Us") {
                        AboutLink(Icons.Rounded.Language, "Website", "nexttopper-feed.pages.dev", NeonGreen) {
                            openUrl(context, "https://nexttopper-feed.pages.dev")
                        }
                        AboutLink(Icons.Rounded.Email, "Support", "nexttoppersfeed@gmail.com", NeonCyan) {
                            openUrl(context, "mailto:nexttoppersfeed@gmail.com")
                        }
                        AboutLink(Icons.Rounded.Share, "Share App", "Tell your friends", Color(0xFFB388FF)) {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, "📚 Next Toppers Feed — Study smarter!\nhttps://nexttopper-feed.pages.dev")
                            }
                            context.startActivity(Intent.createChooser(intent, "Share"))
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                }

                // Footer
                item {
                    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Version $appVersion", color = TextMuted, fontSize = 11.sp)
                        Spacer(Modifier.height(4.dp))
                        Text("Made with ❤️ for students", color = NeonGreen.copy(0.6f), fontSize = 12.sp)
                        Text("© 2025 Next Toppers Feed. All rights reserved.", color = TextMuted, fontSize = 10.sp)
                    }
                    Spacer(Modifier.height(40.dp))
                }
            }
        }
    }
}

@Composable
private fun VersionChip(label: String, color: Color) {
    Box(
        Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(color.copy(0.12f))
            .border(1.dp, color.copy(0.35f), RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(label, color = color, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun AboutSection(title: String, content: @Composable () -> Unit) {
    Column(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceCard)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        content()
    }
}

@Composable
private fun AboutLink(
    icon: ImageVector,
    label: String,
    subtitle: String,
    color: Color,
    onClick: () -> Unit
) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(32.dp).background(color.copy(0.1f), RoundedCornerShape(9.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(label, color = TextPrimary, fontSize = 13.sp)
            Text(subtitle, color = TextMuted, fontSize = 11.sp)
        }
        Icon(Icons.Rounded.ChevronRight, null, tint = TextMuted, modifier = Modifier.size(16.dp))
    }
}

private fun openUrl(context: android.content.Context, url: String) {
    runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
}
