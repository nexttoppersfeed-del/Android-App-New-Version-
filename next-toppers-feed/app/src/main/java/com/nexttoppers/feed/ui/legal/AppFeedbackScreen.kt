package com.nexttoppers.feed.ui.legal

import android.content.Intent
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexttoppers.feed.ui.components.NtfCard
import com.nexttoppers.feed.ui.components.NtfPrimaryButton
import com.nexttoppers.feed.ui.theme.BackgroundBlack
import com.nexttoppers.feed.ui.theme.NeonCyan
import com.nexttoppers.feed.ui.theme.NeonGreen
import com.nexttoppers.feed.ui.theme.PremiumGold
import com.nexttoppers.feed.ui.theme.SurfaceCard
import com.nexttoppers.feed.ui.theme.TextMuted
import com.nexttoppers.feed.ui.theme.TextPrimary
import com.nexttoppers.feed.ui.theme.TextSecondary

@Composable
fun AppFeedbackScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var visible    by remember { mutableStateOf(false) }
    var rating     by remember { mutableIntStateOf(0) }
    var feedback   by remember { mutableStateOf("") }
    var submitted  by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { visible = true }

    val infinite = rememberInfiniteTransition(label = "feedbackGlow")
    val glowAlpha by infinite.animateFloat(
        0.05f, 0.12f,
        infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "glow"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundBlack)
    ) {
        Box(
            Modifier.fillMaxWidth().height(200.dp)
                .background(Brush.radialGradient(listOf(PremiumGold.copy(glowAlpha), Color.Transparent)))
        )

        AnimatedVisibility(
            visible = visible,
            enter   = fadeIn(tween(350)) + slideInVertically(tween(350)) { it / 8 }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(top = 52.dp, bottom = 48.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // ── Top bar ───────────────────────────────────────────────────
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
                        Icon(Icons.Rounded.ArrowBack, null, tint = TextSecondary)
                    }
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            "App Feedback",
                            style = TextStyle(
                                fontSize   = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                brush      = Brush.linearGradient(listOf(PremiumGold, NeonCyan)),
                                shadow     = Shadow(PremiumGold.copy(0.3f), Offset.Zero, 10f)
                            )
                        )
                        Text("Tell us what you think!", color = TextMuted, fontSize = 12.sp)
                    }
                }

                // ── Hero text ─────────────────────────────────────────────────
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("⭐", fontSize = 48.sp)
                    Text(
                        "How's your experience?",
                        color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 17.sp,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        "Your feedback helps us build a better app for everyone.",
                        color = TextSecondary, fontSize = 13.sp, textAlign = TextAlign.Center
                    )
                }

                // ── Star rating ────────────────────────────────────────────────
                NtfCard(modifier = Modifier.fillMaxWidth(), borderColor = PremiumGold.copy(0.2f)) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Rate the app", color = TextSecondary, fontSize = 13.sp)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            (1..5).forEach { star ->
                                Icon(
                                    imageVector = if (star <= rating) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                                    contentDescription = null,
                                    tint     = PremiumGold,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clickable { rating = star }
                                )
                            }
                        }
                        if (rating > 0) {
                            Text(
                                ratingLabel(rating),
                                color = PremiumGold, fontWeight = FontWeight.SemiBold, fontSize = 13.sp
                            )
                        }
                    }
                }

                // ── Written feedback ──────────────────────────────────────────
                Text(
                    "TELL US MORE (OPTIONAL)",
                    color = NeonGreen.copy(0.7f), fontSize = 10.sp,
                    fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp,
                    modifier = Modifier.padding(start = 4.dp)
                )
                OutlinedTextField(
                    value         = feedback,
                    onValueChange = { if (it.length <= 400) feedback = it },
                    modifier      = Modifier.fillMaxWidth().height(130.dp),
                    placeholder   = { Text("What do you love? What can we improve? Any feature requests?", color = TextMuted, fontSize = 13.sp) },
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = NeonGreen.copy(0.6f),
                        unfocusedBorderColor = NeonGreen.copy(0.2f),
                        focusedTextColor     = TextPrimary,
                        unfocusedTextColor   = TextPrimary,
                        cursorColor          = NeonGreen,
                        focusedContainerColor   = SurfaceCard,
                        unfocusedContainerColor = SurfaceCard
                    ),
                    shape     = RoundedCornerShape(16.dp),
                    textStyle = TextStyle(fontSize = 13.sp),
                    maxLines  = 5
                )
                Text(
                    "${feedback.length}/400",
                    color = TextMuted, fontSize = 11.sp,
                    modifier = Modifier.align(Alignment.End)
                )

                // ── Quick chips ────────────────────────────────────────────────
                Text(
                    "QUICK TAGS",
                    color = NeonGreen.copy(0.7f), fontSize = 10.sp,
                    fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp,
                    modifier = Modifier.padding(start = 4.dp)
                )
                val quickTags = listOf("Love the UI", "More quizzes", "Faster downloads", "Better community", "More subjects", "Premium worth it")
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    quickTags.chunked(3).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEach { tag ->
                                QuickTag(
                                    label     = tag,
                                    selected  = tag in feedback,
                                    onClick   = {
                                        feedback = if (tag in feedback) feedback.replace(tag, "").trim()
                                        else if (feedback.isEmpty()) tag else "$feedback, $tag"
                                    },
                                    modifier  = Modifier.weight(1f)
                                )
                            }
                            repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                        }
                    }
                }

                // ── Submit ────────────────────────────────────────────────────
                NtfPrimaryButton(
                    text    = if (submitted) "Feedback Sent ✓" else "Send Feedback",
                    enabled = rating > 0 && !submitted,
                    onClick = {
                        val subject = "App Feedback — ${"★".repeat(rating)} Rating"
                        val body    = buildFeedbackBody(rating, feedback)
                        val uri     = Uri.parse(
                            "mailto:nexttoppersfeed@gmail.com?subject=${Uri.encode(subject)}&body=${Uri.encode(body)}"
                        )
                        runCatching {
                            context.startActivity(Intent(Intent.ACTION_SENDTO, uri))
                            submitted = true
                        }
                    }
                )

            }
        }
    }
}

@Composable
private fun QuickTag(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) NeonGreen.copy(0.12f) else SurfaceCard)
            .border(1.dp, if (selected) NeonGreen.copy(0.5f) else NeonGreen.copy(0.15f), RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            color      = if (selected) NeonGreen else TextSecondary,
            fontSize   = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            textAlign  = TextAlign.Center
        )
    }
}

private fun ratingLabel(r: Int) = when (r) {
    1 -> "Very poor — we're sorry 😞"
    2 -> "Needs improvement"
    3 -> "It's okay"
    4 -> "Pretty good! 😊"
    5 -> "Absolutely love it! ⭐"
    else -> ""
}

private fun buildFeedbackBody(rating: Int, feedback: String): String = """
Rating: ${"★".repeat(rating)}${"☆".repeat(5 - rating)} ($rating/5)

Feedback:
${feedback.ifBlank { "(no written feedback)" }}

---
App: Next Toppers Feed v2.0.0
""".trimIndent()
