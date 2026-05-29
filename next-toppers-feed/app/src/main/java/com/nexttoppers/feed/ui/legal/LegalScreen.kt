package com.nexttoppers.feed.ui.legal

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Policy
import androidx.compose.material.icons.rounded.Security
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexttoppers.feed.ui.theme.BackgroundBlack
import com.nexttoppers.feed.ui.theme.NeonCyan
import com.nexttoppers.feed.ui.theme.NeonGreen
import com.nexttoppers.feed.ui.theme.SurfaceCard
import com.nexttoppers.feed.ui.theme.TextMuted
import com.nexttoppers.feed.ui.theme.TextPrimary
import com.nexttoppers.feed.ui.theme.TextSecondary

enum class LegalType { PRIVACY_POLICY, TERMS_OF_SERVICE }

@Composable
fun LegalScreen(
    type: LegalType,
    onBack: () -> Unit
) {
    val title  = if (type == LegalType.PRIVACY_POLICY) "Privacy Policy" else "Terms of Service"
    val icon   = if (type == LegalType.PRIVACY_POLICY) Icons.Rounded.Policy else Icons.Rounded.Security
    val accent = if (type == LegalType.PRIVACY_POLICY) NeonCyan else NeonGreen

    val sections = if (type == LegalType.PRIVACY_POLICY) privacySections() else termsSections()

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Box(Modifier.fillMaxSize().background(BackgroundBlack)) {
        Box(
            Modifier.fillMaxWidth().height(200.dp)
                .background(Brush.radialGradient(listOf(accent.copy(0.06f), Color.Transparent)))
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
                        Column(Modifier.weight(1f)) {
                            Text(
                                title,
                                style = TextStyle(
                                    fontSize = 22.sp, fontWeight = FontWeight.ExtraBold,
                                    brush = Brush.linearGradient(listOf(accent, NeonCyan)),
                                    shadow = Shadow(accent.copy(0.4f), Offset.Zero, 12f)
                                )
                            )
                            Text(
                                "Last updated: May 2025",
                                color = TextMuted, fontSize = 12.sp
                            )
                        }
                        Icon(icon, null, tint = accent.copy(0.5f), modifier = Modifier.size(24.dp))
                    }
                    Spacer(Modifier.height(20.dp))
                }

                // Intro
                item {
                    Box(
                        Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(accent.copy(0.07f))
                            .padding(14.dp)
                    ) {
                        Text(
                            if (type == LegalType.PRIVACY_POLICY)
                                "Next Toppers Feed (\"we\", \"us\", \"our\") is committed to protecting your personal information. This Privacy Policy explains how we collect, use, and safeguard your data when you use our educational app."
                            else
                                "By using Next Toppers Feed, you agree to these Terms of Service. Please read them carefully. If you do not agree, please discontinue use of the app immediately.",
                            color = TextSecondary, fontSize = 13.sp, lineHeight = 20.sp
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                }

                // Sections
                sections.forEach { section ->
                    item {
                        LegalSection(title = section.first, body = section.second, accent = accent)
                        Spacer(Modifier.height(12.dp))
                    }
                }

                // Contact
                item {
                    Spacer(Modifier.height(8.dp))
                    Box(
                        Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(SurfaceCard)
                            .padding(16.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Contact Us", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(
                                "For any questions or concerns regarding this policy, please contact us at:\n\n📧 nexttoppersfeed@gmail.com\n🌐 nexttopper-feed.pages.dev",
                                color = TextSecondary, fontSize = 12.sp, lineHeight = 18.sp
                            )
                        }
                    }
                    Spacer(Modifier.height(40.dp))
                }
            }
        }
    }
}

@Composable
private fun LegalSection(title: String, body: String, accent: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                Modifier.width(3.dp).height(18.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(accent)
            )
            Text(title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
        Text(body, color = TextSecondary, fontSize = 12.sp, lineHeight = 19.sp)
    }
}

// ── Privacy Policy content ────────────────────────────────────────────────────
private fun privacySections(): List<Pair<String, String>> = listOf(
    "Information We Collect" to
        "• Account Information: Name, email address, and profile photo (provided via Google Sign-In).\n" +
        "• Usage Data: Quiz completions, resource views, streak data, and XP points.\n" +
        "• Device Information: Device type, OS version, and app version for diagnostics.\n" +
        "• Communication Data: Messages sent through the in-app chat (encrypted in transit).",

    "How We Use Your Information" to
        "• To provide and personalise the educational experience.\n" +
        "• To calculate XP, streaks, and leaderboard rankings.\n" +
        "• To send relevant notifications (quiz reminders, announcements).\n" +
        "• To improve app performance and diagnose technical issues.\n" +
        "• To process premium membership requests and verify payments.",

    "Data Storage & Security" to
        "Your data is stored in Firebase (Google Cloud), protected by industry-standard encryption in transit (TLS) and at rest. We implement Firestore security rules to prevent unauthorized access. Offline data is stored in your device's private internal storage.",

    "Third-Party Services" to
        "Next Toppers Feed uses the following third-party services:\n" +
        "• Firebase (Google) — Authentication, database, storage, and analytics.\n" +
        "• Google Sign-In — Secure account creation.\n" +
        "• Coil — Image loading (no personal data transmitted).",

    "Data Retention" to
        "We retain your data as long as your account is active. You can request deletion of your account and all associated data by contacting nexttoppersfeed@gmail.com. Data deletion requests are processed within 30 days.",

    "Children's Privacy" to
        "Next Toppers Feed is designed for students aged 12 and above. We do not knowingly collect personal information from children under 12. If you believe a child under 12 has provided us with personal data, please contact us immediately.",

    "Your Rights" to
        "You have the right to:\n" +
        "• Access your personal data.\n" +
        "• Request correction of inaccurate data.\n" +
        "• Request deletion of your account and data.\n" +
        "• Opt out of analytics collection (via Settings → Privacy).",

    "Changes to This Policy" to
        "We may update this Privacy Policy from time to time. We will notify you of significant changes through an in-app announcement. Continued use of the app after changes constitutes acceptance of the updated policy."
)

// ── Terms of Service content ──────────────────────────────────────────────────
private fun termsSections(): List<Pair<String, String>> = listOf(
    "Acceptance of Terms" to
        "By downloading, installing, or using Next Toppers Feed, you agree to be bound by these Terms of Service. If you do not agree, you must not use the app.",

    "User Accounts" to
        "• You must be at least 12 years old to create an account.\n" +
        "• You are responsible for maintaining the security of your account.\n" +
        "• You must provide accurate information during registration.\n" +
        "• One account per person — multiple accounts are not permitted.",

    "Acceptable Use" to
        "You agree NOT to:\n" +
        "• Share, distribute, or reproduce premium content outside the app.\n" +
        "• Attempt to reverse-engineer, hack, or tamper with the app.\n" +
        "• Harass, abuse, or threaten other users in community features.\n" +
        "• Upload illegal, harmful, or infringing content.\n" +
        "• Create fake reviews, spam, or misleading content.",

    "Premium Membership" to
        "• Premium access is activated upon manual review and approval of payment proof by our admin team.\n" +
        "• Payments are non-refundable unless otherwise required by applicable law.\n" +
        "• Premium features may change over time with reasonable notice.\n" +
        "• Fraudulent payment claims will result in account termination.",

    "Intellectual Property" to
        "All content in Next Toppers Feed — including study materials, PDFs, quizzes, branding, and design — is owned by Next Toppers or its content partners. You may not reproduce, distribute, or create derivative works without prior written permission.",

    "Content Moderation" to
        "We reserve the right to remove any user-generated content that violates these Terms. Repeated violations may result in account suspension or permanent banning. Moderation decisions are at our sole discretion.",

    "Disclaimer of Warranties" to
        "Next Toppers Feed is provided \"as is\" without warranties of any kind. We do not guarantee uninterrupted availability, accuracy of all content, or specific educational outcomes. Educational content should be used as a supplement to formal education.",

    "Limitation of Liability" to
        "To the fullest extent permitted by law, Next Toppers shall not be liable for any indirect, incidental, or consequential damages arising from your use of the app.",

    "Termination" to
        "We may terminate or suspend your account at any time for violations of these Terms. You may delete your account by contacting support. Upon termination, your right to use the app ceases immediately.",

    "Governing Law" to
        "These Terms shall be governed by the laws of India. Any disputes shall be subject to the exclusive jurisdiction of the courts of India."
)
