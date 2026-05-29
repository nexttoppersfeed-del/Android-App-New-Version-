package com.nexttoppers.feed.ui.settings

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.ClearAll
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Feedback
import androidx.compose.material.icons.rounded.HelpOutline
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Policy
import androidx.compose.material.icons.rounded.PrivacyTip
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nexttoppers.feed.ui.components.NeonDivider
import com.nexttoppers.feed.ui.components.NtfCard
import com.nexttoppers.feed.ui.components.NtfOutlinedButton
import com.nexttoppers.feed.ui.components.SuccessToast
import com.nexttoppers.feed.ui.theme.BackgroundBlack
import com.nexttoppers.feed.ui.theme.ErrorRed
import com.nexttoppers.feed.ui.theme.NeonCyan
import com.nexttoppers.feed.ui.theme.NeonGreen
import com.nexttoppers.feed.ui.theme.SurfaceCard
import com.nexttoppers.feed.ui.theme.TextMuted
import com.nexttoppers.feed.ui.theme.TextPrimary
import com.nexttoppers.feed.ui.theme.TextSecondary

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onSignOut: () -> Unit,
    onNavigateToPrivacyPolicy: () -> Unit = {},
    onNavigateToTerms: () -> Unit = {},
    onNavigateToAbout: () -> Unit = {},
    onNavigateToReportIssue: () -> Unit = {},
    onNavigateToFeedback: () -> Unit = {},
    onNavigateToDownloads: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    val pushEnabled         by viewModel.pushNotificationsEnabled.collectAsState()
    val announcementEnabled by viewModel.announcementAlertsEnabled.collectAsState()
    val quizReminders       by viewModel.quizRemindersEnabled.collectAsState()
    val streakReminders     by viewModel.streakRemindersEnabled.collectAsState()
    val wifiOnly            by viewModel.wifiOnlyDownloads.collectAsState()
    val analytics           by viewModel.analyticsEnabled.collectAsState()

    var visible                 by remember { mutableStateOf(false) }
    var showClearCacheDialog    by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var showSignOutDialog       by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { visible = true }
    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            kotlinx.coroutines.delay(2500)
            viewModel.clearMessages()
        }
    }

    val appVersion = remember {
        try { context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "2.0.0" }
        catch (_: PackageManager.NameNotFoundException) { "2.0.0" }
    }

    Box(modifier = Modifier.fillMaxSize().background(BackgroundBlack)) {
        Box(
            Modifier.fillMaxWidth().height(220.dp)
                .background(Brush.radialGradient(listOf(NeonCyan.copy(0.05f), Color.Transparent)))
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
                    .padding(top = 52.dp, bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // ── Top bar ───────────────────────────────────────────────────
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
                        Icon(Icons.Rounded.ArrowBack, null, tint = TextSecondary)
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Settings",
                        style = TextStyle(
                            fontSize   = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            brush      = Brush.linearGradient(listOf(NeonGreen, NeonCyan)),
                            shadow     = Shadow(NeonGreen.copy(0.3f), Offset.Zero, 10f)
                        )
                    )
                }

                // ── Notifications ─────────────────────────────────────────────
                SettingsSection("Notifications") {
                    SettingsToggleRow(
                        icon = Icons.Rounded.Notifications,
                        label = "Push Notifications",
                        subtitle = "Receive updates and alerts",
                        checked = pushEnabled,
                        onCheckedChange = viewModel::setPushNotifications
                    )
                    NeonDivider(Modifier.padding(vertical = 2.dp))
                    SettingsToggleRow(
                        icon = Icons.Rounded.Notifications,
                        label = "Announcements",
                        subtitle = "Important platform alerts",
                        checked = announcementEnabled,
                        onCheckedChange = viewModel::setAnnouncementAlerts
                    )
                    NeonDivider(Modifier.padding(vertical = 2.dp))
                    SettingsToggleRow(
                        icon = Icons.Rounded.Notifications,
                        label = "Quiz Reminders",
                        subtitle = "Daily study reminders",
                        checked = quizReminders,
                        onCheckedChange = viewModel::setQuizReminders
                    )
                    NeonDivider(Modifier.padding(vertical = 2.dp))
                    SettingsToggleRow(
                        icon = Icons.Rounded.Notifications,
                        label = "Streak Reminders",
                        subtitle = "Don't break your streak",
                        checked = streakReminders,
                        onCheckedChange = viewModel::setStreakReminders
                    )
                }

                // ── Downloads ─────────────────────────────────────────────────
                SettingsSection("Downloads & Storage") {
                    SettingsToggleRow(
                        icon = Icons.Rounded.Wifi,
                        label = "Wi-Fi Only Downloads",
                        subtitle = "Don't use mobile data",
                        checked = wifiOnly,
                        onCheckedChange = viewModel::setWifiOnlyDownloads,
                        iconTint = NeonCyan
                    )
                    NeonDivider(Modifier.padding(vertical = 2.dp))
                    SettingsNavRow(
                        icon = Icons.Rounded.Download,
                        label = "Offline Library",
                        subtitle = "Manage downloaded files",
                        iconTint = NeonCyan,
                        onClick = onNavigateToDownloads
                    )
                    NeonDivider(Modifier.padding(vertical = 2.dp))
                    SettingsNavRow(
                        icon = Icons.Rounded.ClearAll,
                        label = "Clear Cache",
                        subtitle = "Free up storage space",
                        iconTint = NeonCyan,
                        onClick = { showClearCacheDialog = true }
                    )
                }

                // ── Privacy ───────────────────────────────────────────────────
                SettingsSection("Privacy") {
                    SettingsToggleRow(
                        icon = Icons.Rounded.PrivacyTip,
                        label = "Usage Analytics",
                        subtitle = "Help us improve the app",
                        checked = analytics,
                        onCheckedChange = viewModel::setAnalyticsEnabled,
                        iconTint = TextSecondary
                    )
                }

                // ── Support ───────────────────────────────────────────────────
                SettingsSection("Support & Feedback") {
                    SettingsNavRow(
                        icon = Icons.Rounded.BugReport,
                        label = "Report an Issue",
                        subtitle = "Found a bug? Let us know",
                        iconTint = Color(0xFFFF4D6D),
                        onClick = onNavigateToReportIssue
                    )
                    NeonDivider(Modifier.padding(vertical = 2.dp))
                    SettingsNavRow(
                        icon = Icons.Rounded.Feedback,
                        label = "App Feedback",
                        subtitle = "Share your thoughts with us",
                        iconTint = Color(0xFFFFD700),
                        onClick = onNavigateToFeedback
                    )
                    NeonDivider(Modifier.padding(vertical = 2.dp))
                    SettingsNavRow(
                        icon = Icons.Rounded.HelpOutline,
                        label = "Help & FAQ",
                        iconTint = NeonGreen,
                        onClick = { openUrl(context, "https://nexttopper-feed.pages.dev") }
                    )
                    NeonDivider(Modifier.padding(vertical = 2.dp))
                    SettingsNavRow(
                        icon = Icons.Rounded.Share,
                        label = "Share App",
                        iconTint = NeonGreen,
                        onClick = { shareApp(context) }
                    )
                }

                // ── Legal ─────────────────────────────────────────────────────
                SettingsSection("Legal") {
                    SettingsNavRow(
                        icon = Icons.Rounded.Policy,
                        label = "Privacy Policy",
                        iconTint = TextSecondary,
                        onClick = onNavigateToPrivacyPolicy
                    )
                    NeonDivider(Modifier.padding(vertical = 2.dp))
                    SettingsNavRow(
                        icon = Icons.Rounded.Security,
                        label = "Terms of Service",
                        iconTint = TextSecondary,
                        onClick = onNavigateToTerms
                    )
                    NeonDivider(Modifier.padding(vertical = 2.dp))
                    SettingsNavRow(
                        icon = Icons.Rounded.Info,
                        label = "About Next Toppers Feed",
                        iconTint = NeonCyan,
                        onClick = onNavigateToAbout
                    )
                }

                // ── Account ───────────────────────────────────────────────────
                SettingsSection("Account") {
                    SettingsNavRow(
                        icon = Icons.Rounded.DeleteForever,
                        label = "Delete Account",
                        subtitle = "Permanently remove your data",
                        iconTint = ErrorRed,
                        onClick = { showDeleteAccountDialog = true }
                    )
                }

                // ── App version card ──────────────────────────────────────────
                NtfCard(modifier = Modifier.fillMaxWidth(), borderColor = NeonGreen.copy(0.12f)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier.size(36.dp).background(NeonGreen.copy(0.1f), RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Rounded.Info, null, tint = NeonGreen, modifier = Modifier.size(18.dp))
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("Next Toppers Feed", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Text("nexttopper-feed.pages.dev", color = TextMuted, fontSize = 10.sp)
                            }
                        }
                        Box(
                            Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(NeonGreen.copy(0.12f))
                                .border(1.dp, NeonGreen.copy(0.35f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("v$appVersion", color = NeonGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // ── Sign out ──────────────────────────────────────────────────
                NtfOutlinedButton(
                    text        = "Sign Out",
                    onClick     = { showSignOutDialog = true },
                    accentColor = ErrorRed
                )
            }
        }

        // ── Toast ─────────────────────────────────────────────────────────────
        Box(Modifier.align(Alignment.BottomCenter).padding(bottom = 100.dp)) {
            SuccessToast(message = uiState.successMessage, onDismiss = viewModel::clearMessages)
        }
    }

    // ── Clear cache dialog ────────────────────────────────────────────────────
    if (showClearCacheDialog) {
        AlertDialog(
            onDismissRequest = { showClearCacheDialog = false },
            title = { Text("Clear Cache?", color = NeonCyan, fontWeight = FontWeight.Bold) },
            text  = {
                Text(
                    "This will delete all cached images and temporarily downloaded data. Permanently downloaded offline files are kept.",
                    color = TextSecondary, fontSize = 13.sp
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearCache(context)
                    showClearCacheDialog = false
                }) { Text("Clear Cache", color = ErrorRed, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showClearCacheDialog = false }) { Text("Cancel", color = TextMuted) }
            },
            containerColor = SurfaceCard
        )
    }

    // ── Sign out dialog ───────────────────────────────────────────────────────
    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = { Text("Sign Out?", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text  = { Text("You'll be returned to the login screen. Your data and progress are safely stored.", color = TextSecondary, fontSize = 13.sp) },
            confirmButton = {
                TextButton(onClick = { showSignOutDialog = false; onSignOut() }) {
                    Text("Sign Out", color = ErrorRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) { Text("Cancel", color = TextMuted) }
            },
            containerColor = SurfaceCard
        )
    }

    // ── Delete account dialog ─────────────────────────────────────────────────
    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            title = { Text("Delete Account", color = ErrorRed, fontWeight = FontWeight.Bold) },
            text  = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Deleting your account will permanently erase all your data, XP, streaks, and premium access. This cannot be undone.",
                        color = TextSecondary, fontSize = 13.sp
                    )
                    Box(
                        Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(ErrorRed.copy(0.08f))
                            .border(1.dp, ErrorRed.copy(0.3f), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            "⚠️ To proceed, email us at nexttoppersfeed@gmail.com with subject: \"Account Deletion Request\".",
                            color = ErrorRed, fontSize = 12.sp, lineHeight = 18.sp
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteAccountDialog = false
                    openUrl(context, "mailto:nexttoppersfeed@gmail.com?subject=Account%20Deletion%20Request&body=Please%20delete%20my%20account%20and%20all%20associated%20data.")
                }) { Text("Email Support", color = ErrorRed, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }) { Text("Cancel", color = TextMuted) }
            },
            containerColor = SurfaceCard
        )
    }
}

// ── Section container ──────────────────────────────────────────────────────────
@Composable
private fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
        Text(
            title.uppercase(),
            color = NeonGreen.copy(0.7f), fontSize = 10.sp,
            fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        NtfCard(modifier = Modifier.fillMaxWidth(), innerPadding = 14.dp) {
            Column { content() }
        }
    }
}

// ── Toggle row ─────────────────────────────────────────────────────────────────
@Composable
private fun SettingsToggleRow(
    icon: ImageVector,
    label: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    iconTint: Color = NeonGreen
) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(34.dp).background(iconTint.copy(0.1f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = iconTint, modifier = Modifier.size(17.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(label, color = TextPrimary, fontSize = 13.sp)
            if (subtitle != null) Text(subtitle, color = TextMuted, fontSize = 11.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor    = BackgroundBlack,
                checkedTrackColor    = NeonGreen,
                uncheckedThumbColor  = TextSecondary,
                uncheckedTrackColor  = SurfaceCard
            )
        )
    }
}

// ── Navigation row ─────────────────────────────────────────────────────────────
@Composable
private fun SettingsNavRow(
    icon: ImageVector,
    label: String,
    subtitle: String? = null,
    iconTint: Color = NeonGreen,
    onClick: () -> Unit = {}
) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(34.dp).background(iconTint.copy(0.1f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = iconTint, modifier = Modifier.size(17.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(label, color = TextPrimary, fontSize = 13.sp)
            if (subtitle != null) Text(subtitle, color = TextMuted, fontSize = 11.sp)
        }
        Icon(Icons.Rounded.ChevronRight, null, tint = TextMuted, modifier = Modifier.size(18.dp))
    }
}

// ── Utility functions ──────────────────────────────────────────────────────────
private fun openUrl(context: Context, url: String) {
    runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
}

private fun shareApp(context: Context) {
    runCatching {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(
                Intent.EXTRA_TEXT,
                "📚 Study smarter with Next Toppers Feed — Notes, Quizzes & Rankings!\n" +
                "https://nexttopper-feed.pages.dev"
            )
        }
        context.startActivity(Intent.createChooser(intent, "Share Next Toppers Feed"))
    }
}
