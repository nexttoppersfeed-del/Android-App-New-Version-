package com.nexttoppers.feed.ui.legal

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.HelpOutline
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexttoppers.feed.ui.components.NeonDivider
import com.nexttoppers.feed.ui.components.NtfCard
import com.nexttoppers.feed.ui.components.NtfPrimaryButton
import com.nexttoppers.feed.ui.theme.BackgroundBlack
import com.nexttoppers.feed.ui.theme.ErrorRed
import com.nexttoppers.feed.ui.theme.NeonCyan
import com.nexttoppers.feed.ui.theme.NeonGreen
import com.nexttoppers.feed.ui.theme.SurfaceCard
import com.nexttoppers.feed.ui.theme.TextMuted
import com.nexttoppers.feed.ui.theme.TextPrimary
import com.nexttoppers.feed.ui.theme.TextSecondary

private data class IssueCategory(
    val label: String,
    val icon: ImageVector,
    val color: Color,
    val emailSubject: String
)

private val issueCategories = listOf(
    IssueCategory("App Crash / Bug",       Icons.Rounded.BugReport,    Color(0xFFFF4D6D), "Bug Report: App Crash"),
    IssueCategory("Performance Issue",     Icons.Rounded.Speed,         Color(0xFFFF6B35), "Performance Issue Report"),
    IssueCategory("Content Error",         Icons.Rounded.ErrorOutline,  Color(0xFFFFB347), "Content Error Report"),
    IssueCategory("Network / Offline",     Icons.Rounded.WifiOff,       Color(0xFF00E5FF), "Network Issue Report"),
    IssueCategory("Other / General",       Icons.Rounded.HelpOutline,   Color(0xFFB388FF), "General Issue Report")
)

@Composable
fun ReportIssueScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var visible          by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<IssueCategory?>(null) }
    var description      by remember { mutableStateOf("") }
    var submitted        by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { visible = true }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundBlack)
    ) {
        Box(
            Modifier.fillMaxWidth().height(200.dp)
                .background(Brush.radialGradient(listOf(ErrorRed.copy(0.06f), Color.Transparent)))
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
                            "Report an Issue",
                            style = TextStyle(
                                fontSize   = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                brush      = Brush.linearGradient(listOf(ErrorRed, Color(0xFFFF6B35))),
                                shadow     = Shadow(ErrorRed.copy(0.3f), Offset.Zero, 10f)
                            )
                        )
                        Text("Help us fix it faster", color = TextMuted, fontSize = 12.sp)
                    }
                }

                // ── Category selector ─────────────────────────────────────────
                Text(
                    "ISSUE TYPE",
                    color = NeonGreen.copy(0.7f), fontSize = 10.sp,
                    fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp,
                    modifier = Modifier.padding(start = 4.dp)
                )
                NtfCard(modifier = Modifier.fillMaxWidth(), innerPadding = 14.dp) {
                    Column {
                        issueCategories.forEachIndexed { idx, category ->
                            IssueCategoryRow(
                                category = category,
                                isSelected = selectedCategory == category,
                                onClick = { selectedCategory = category }
                            )
                            if (idx < issueCategories.lastIndex) {
                                NeonDivider(Modifier.padding(vertical = 2.dp))
                            }
                        }
                    }
                }

                // ── Description ────────────────────────────────────────────────
                Text(
                    "DESCRIPTION",
                    color = NeonGreen.copy(0.7f), fontSize = 10.sp,
                    fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp,
                    modifier = Modifier.padding(start = 4.dp)
                )
                OutlinedTextField(
                    value         = description,
                    onValueChange = { if (it.length <= 500) description = it },
                    modifier      = Modifier.fillMaxWidth().height(140.dp),
                    placeholder   = { Text("Describe the issue — steps to reproduce, what you expected, what happened…", color = TextMuted, fontSize = 13.sp) },
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = NeonGreen.copy(0.6f),
                        unfocusedBorderColor = NeonGreen.copy(0.2f),
                        focusedTextColor     = TextPrimary,
                        unfocusedTextColor   = TextPrimary,
                        cursorColor          = NeonGreen,
                        focusedContainerColor   = SurfaceCard,
                        unfocusedContainerColor = SurfaceCard
                    ),
                    shape         = RoundedCornerShape(16.dp),
                    textStyle     = TextStyle(fontSize = 13.sp, color = TextPrimary),
                    maxLines      = 6
                )
                Text(
                    "${description.length}/500",
                    color    = TextMuted,
                    fontSize = 11.sp,
                    modifier = Modifier.align(Alignment.End)
                )

                // ── Submit hint ────────────────────────────────────────────────
                Box(
                    Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(NeonGreen.copy(0.06f))
                        .border(1.dp, NeonGreen.copy(0.15f), RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Rounded.Email, null, tint = NeonGreen.copy(0.6f), modifier = Modifier.size(16.dp))
                        Text(
                            "Tapping \"Send Report\" will open your email app pre-filled with your issue details. Send it to nexttoppersfeed@gmail.com for fastest resolution.",
                            color = TextSecondary, fontSize = 12.sp, lineHeight = 18.sp
                        )
                    }
                }

                // ── Send button ────────────────────────────────────────────────
                NtfPrimaryButton(
                    text    = if (submitted) "Report Sent ✓" else "Send Report",
                    enabled = selectedCategory != null && description.trim().length >= 10,
                    onClick = {
                        val cat     = selectedCategory ?: return@NtfPrimaryButton
                        val subject = cat.emailSubject
                        val body    = buildReportBody(cat.label, description)
                        val uri     = Uri.parse(
                            "mailto:nexttoppersfeed@gmail.com?subject=${Uri.encode(subject)}&body=${Uri.encode(body)}"
                        )
                        runCatching {
                            context.startActivity(Intent(Intent.ACTION_SENDTO, uri))
                            submitted = true
                        }
                    }
                )

                if (selectedCategory == null || description.trim().length < 10) {
                    Text(
                        "Select a category and describe the issue (min 10 chars) to enable submit.",
                        color    = TextMuted,
                        fontSize = 11.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // ── Alternative contact ────────────────────────────────────────
                NtfCard(modifier = Modifier.fillMaxWidth(), borderColor = NeonCyan.copy(0.2f)) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Other ways to reach us", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        ContactRow("📧", "Email", "nexttoppersfeed@gmail.com") {
                            runCatching {
                                context.startActivity(
                                    Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:nexttoppersfeed@gmail.com"))
                                )
                            }
                        }
                        ContactRow("🌐", "Website", "nexttopper-feed.pages.dev") {
                            runCatching {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://nexttopper-feed.pages.dev")))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun IssueCategoryRow(
    category: IssueCategory,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) category.color.copy(0.08f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(32.dp).background(category.color.copy(0.12f), RoundedCornerShape(9.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(category.icon, null, tint = category.color, modifier = Modifier.size(16.dp))
        }
        Spacer(Modifier.width(10.dp))
        Text(category.label, color = if (isSelected) category.color else TextPrimary, fontSize = 13.sp, modifier = Modifier.weight(1f))
        if (isSelected) {
            Box(Modifier.size(8.dp).background(category.color, RoundedCornerShape(50)))
        }
    }
}

@Composable
private fun ContactRow(emoji: String, label: String, value: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(emoji, fontSize = 14.sp)
        Spacer(Modifier.width(8.dp))
        Text("$label: ", color = TextSecondary, fontSize = 12.sp)
        Text(value, color = NeonCyan, fontSize = 12.sp)
    }
}

private fun buildReportBody(category: String, description: String): String = """
Issue Category: $category

Description:
$description

---
App: Next Toppers Feed v2.0.0
""".trimIndent()
