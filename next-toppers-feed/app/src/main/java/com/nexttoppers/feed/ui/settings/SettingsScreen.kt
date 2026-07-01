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
import com.nexttoppers.feed.ui.theme.AccentCyan
import com.nexttoppers.feed.ui.theme.AccentEmerald
import com.nexttoppers.feed.ui.theme.AccentIndigo
import com.nexttoppers.feed.ui.theme.AccentViolet
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

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = visible,
            enter   = fadeIn(tween(300)) + slideInVertically(tween(300)) { it / 8 }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(top = 48.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // ── Top bar ───────────────────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
                        Icon(Icons.Rounded.ArrowBack, null, tint = TextSecondary)
                    }
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            "Settings",
                            fontSize   = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color      = TextPrimary
                        )
                        Text("Manage your preferences & app experience", fontSize = 11.sp, color = TextMuted)
                    }
                }

                // ── Notifications ─────────────────────────────────────────────
                SettingsSection(title = "Notifications", accentColor = AccentCyan) {
                    SettingsToggleRow("Push Notifications",  "Receive all app push alerts", pushEnabled,         viewModel::setPushNotifications)
                    NeonDivider()
                    SettingsToggleRow("Announcements",       "Teacher & school updates",    announcementEnabled, viewModel::setAnnouncementAlerts)
                    NeonDivider()
                    SettingsToggleRow("Quiz Reminders",      "Never miss a scheduled quiz", quizReminders,       viewModel::setQuizReminders)
                    NeonDivider()
                    SettingsToggleRow("Streak Reminders",    "Keep your streak alive",      streakReminders,     viewModel::setStreakReminders)
                }

                // ── Downloads & Storage ───────────────────────────────────────
                SettingsSection(title = "Downloads & Storage", accentColor = AccentEmerald) {
                    SettingsToggleRow("Wi-Fi Only Downloads", "Save mobile data",           wifiOnly, viewModel::setWifiOnlyDownloads)
                    NeonDivider()
                    SettingsNavRow(Icons.Rounded.Download, "Offline Library", "View downloaded files", onClick = onNavigateToDownloads)
                    NeonDivider()
                    SettingsNavRow(Icons.Rounded.ClearAll, "Clear Cache", "Free up space · tap to clear",
                        onClick = { showClearCacheDialog = true }, tintColor = ErrorRed.copy(0.8f))
                }

                // ── Privacy ───────────────────────────────────────────────────
                SettingsSection(title = "Privacy", accentColor = AccentViolet) {
                    SettingsToggleRow("Usage Analytics", "Help improve the app (anonymous)", analytics, viewModel::setAnalyticsEnabled)
                }

                // ── Support & Feedback ────────────────────────────────────────
                SettingsSection(title = "Support & Feedback", accentColor = PremiumGold) {
                    SettingsNavRow(Icons.Rounded.BugReport,   "Report an Issue",  "Found a bug?",               onClick = onNavigateToReportIssue)
                    NeonDivider()
                    SettingsNavRow(Icons.Rounded.Feedback,    "App Feedback",     "Share your thoughts",         onClick = onNavigateToFeedback)
                    NeonDivider()
                    SettingsNavRow(Icons.Rounded.HelpOutline, "Help & FAQ",       "Guides & answers",
                        onClick = {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://nexttoppers.in/help")))
                        })
                    NeonDivider()
                    SettingsNavRow(Icons.Rounded.Share, "Share App", "Tell your friends",
                        onClick = {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, "Check out Next Toppers Feed — https://nexttoppers.in")
                            }
                            context.startActivity(Intent.createChooser(intent, "Share via"))
                        })
                }

                // ── About & Legal ─────────────────────────────────────────────
                SettingsSection(title = "About & Legal", accentColor = AccentIndigo) {
                    SettingsNavRow(Icons.Rounded.Policy,       "Privacy Policy",    "How we handle your data",  onClick = onNavigateToPrivacyPolicy)
                    NeonDivider()
                    SettingsNavRow(Icons.Rounded.Security,     "Terms of Service",  "Usage rules & guidelines", onClick = onNavigateToTerms)
                    NeonDivider()
                    SettingsNavRow(Icons.Rounded.Info,         "About",             "Version & credits",         onClick = onNavigateToAbout)
                }

                // ── App version card ──────────────────────────────────────────
                val versionName = try {
                    context.packageManager.getPackageInfo(context.packageName, 0).versionName
                } catch (_: PackageManager.NameNotFoundException) { "2.0.0" }

                NtfCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Next Toppers Feed", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("nexttoppers.in", fontSize = 11.sp, color = TextMuted)
                        }
                        androidx.compose.material3.Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = AccentCyan.copy(0.10f)
                        ) {
                            Text(
                                "v$versionName",
                                fontSize   = 11.sp,
                                color      = AccentCyan,
                                fontWeight = FontWeight.Bold,
                                modifier   = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // ── Account actions ───────────────────────────────────────────
                SettingsSection(title = "Account", accentColor = ErrorRed) {
                    SettingsNavRow(
                        Icons.Rounded.DeleteForever, "Delete Account",
                        "Permanently remove your account",
                        onClick = { showDeleteAccountDialog = true },
                        tintColor = ErrorRed
                    )
                }

                NtfOutlinedButton(
                    text       = "Sign Out",
                    onClick    = { showSignOutDialog = true },
                    accentColor = ErrorRed,
                    modifier   = Modifier.fillMaxWidth()
                )
            }
        }

        // ── Success toast ─────────────────────────────────────────────────────
        SuccessToast(
            message  = uiState.successMessage,
            onDismiss = viewModel::clearMessages
        )
    }

    // ── Dialogs ───────────────────────────────────────────────────────────────
    if (showClearCacheDialog) {
        NtfAlertDialog(
            title   = "Clear Cache?",
            body    = "This will delete all temporary files. Your downloads and data won't be affected.",
            confirm = "Clear",
            dismiss = "Cancel",
            onConfirm = {
                viewModel.clearCache(context)
                showClearCacheDialog = false
            },
            onDismiss = { showClearCacheDialog = false }
        )
    }

    if (showSignOutDialog) {
        NtfAlertDialog(
            title   = "Sign Out?",
            body    = "You'll need to sign in again to access your account.",
            confirm = "Sign Out",
            dismiss = "Stay",
            onConfirm = {
                showSignOutDialog = false
                onSignOut()
            },
            onDismiss = { showSignOutDialog = false }
        )
    }

    if (showDeleteAccountDialog) {
        NtfAlertDialog(
            title   = "Delete Account",
            body    = "This action cannot be undone. All your data, XP, and progress will be permanently deleted. Contact support to proceed.",
            confirm = "Email Support",
            dismiss = "Cancel",
            isDestructive = true,
            onConfirm = {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:nexttoppersfeed@gmail.com")
                    putExtra(Intent.EXTRA_SUBJECT, "Account Deletion Request")
                }
                context.startActivity(intent)
                showDeleteAccountDialog = false
            },
            onDismiss = { showDeleteAccountDialog = false }
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    accentColor: Color,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
        Text(
            title,
            fontSize   = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            color      = accentColor,
            modifier   = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        NtfCard(
            modifier    = Modifier.fillMaxWidth(),
            borderColor = accentColor.copy(0.12f)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun SettingsToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 14.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
            Text(subtitle, fontSize = 11.sp, color = TextMuted)
        }
        Switch(
            checked         = checked,
            onCheckedChange = onCheckedChange,
            colors          = SwitchDefaults.colors(
                checkedThumbColor       = Color.White,
                checkedTrackColor       = AccentCyan,
                uncheckedThumbColor     = TextMuted,
                uncheckedTrackColor     = SurfaceElevated,
                uncheckedBorderColor    = SurfaceElevated
            )
        )
    }
}

@Composable
private fun SettingsNavRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    tintColor: Color = AccentCyan
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        androidx.compose.material3.Surface(
            shape    = RoundedCornerShape(10.dp),
            color    = tintColor.copy(0.10f),
            modifier = Modifier.size(36.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(36.dp)) {
                Icon(icon, null, tint = tintColor, modifier = Modifier.size(18.dp))
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 14.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
            Text(subtitle, fontSize = 11.sp, color = TextMuted)
        }
        Icon(Icons.Rounded.ChevronRight, null, tint = TextMuted, modifier = Modifier.size(16.dp))
    }
}

@Composable
private fun NtfAlertDialog(
    title: String,
    body: String,
    confirm: String,
    dismiss: String,
    isDestructive: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest   = onDismiss,
        containerColor     = SurfaceCard,
        titleContentColor  = TextPrimary,
        textContentColor   = TextSecondary,
        title  = { Text(title, fontWeight = FontWeight.Bold) },
        text   = { Text(body, lineHeight = 20.sp) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirm, color = if (isDestructive) ErrorRed else AccentCyan, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismiss, color = TextSecondary)
            }
        }
    )
}
