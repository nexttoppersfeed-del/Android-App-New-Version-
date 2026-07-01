package com.nexttoppers.feed.ui.profile

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material.icons.rounded.Quiz
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.TrackChanges
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.nexttoppers.feed.data.model.User
import com.nexttoppers.feed.data.model.toPremiumMembership
import com.nexttoppers.feed.ui.achievements.AchievementsSection
import com.nexttoppers.feed.ui.components.NeonDivider
import com.nexttoppers.feed.ui.components.NtfCard
import com.nexttoppers.feed.ui.components.NtfGradientCard
import com.nexttoppers.feed.ui.components.NtfOutlinedButton
import com.nexttoppers.feed.ui.components.SkeletonCard
import com.nexttoppers.feed.ui.components.XpBadge
import com.nexttoppers.feed.ui.premium.CurrentMembershipCard
import com.nexttoppers.feed.ui.premium.PremiumBadge
import com.nexttoppers.feed.ui.theme.AccentCyan
import com.nexttoppers.feed.ui.theme.AccentViolet
import com.nexttoppers.feed.ui.theme.BackgroundBlack
import com.nexttoppers.feed.ui.theme.ErrorRed
import com.nexttoppers.feed.ui.theme.NeonGreen
import com.nexttoppers.feed.ui.theme.PremiumGold
import com.nexttoppers.feed.ui.theme.SurfaceCard
import com.nexttoppers.feed.ui.theme.SurfaceElevated
import com.nexttoppers.feed.ui.theme.TextMuted
import com.nexttoppers.feed.ui.theme.TextPrimary
import com.nexttoppers.feed.ui.theme.TextSecondary
import com.nexttoppers.feed.ui.xp.GlowingLevelCard
import com.nexttoppers.feed.ui.xp.StreakCard
import com.nexttoppers.feed.util.LevelUtils
import java.text.SimpleDateFormat
import java.util.Locale

private val AdminPurple = Color(0xFFB388FF)

@Composable
fun ProfileScreen(
    onSignedOut: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAdmin: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState  by viewModel.uiState.collectAsState()
    val userRank by viewModel.userRank.collectAsState()
    val context  = LocalContext.current
    var visible  by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { visible = true }
    LaunchedEffect(uiState) {
        if (uiState is ProfileUiState.SignedOut) onSignedOut()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = uiState) {
            is ProfileUiState.Loading -> ProfileLoadingSkeleton()
            is ProfileUiState.Error   -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(state.message, color = TextSecondary)
            }
            is ProfileUiState.Success -> {
                AnimatedVisibility(
                    visible = visible,
                    enter   = fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 8 }
                ) {
                    ProfileContent(
                        user              = state.user,
                        userRank          = userRank,
                        onNavigateToSettings = onNavigateToSettings,
                        onNavigateToAdmin    = onNavigateToAdmin,
                        onSignOut            = { viewModel.signOut(context) }
                    )
                }
            }
            is ProfileUiState.SignedOut -> Unit
        }
    }
}

@Composable
private fun ProfileLoadingSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .padding(top = 60.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SkeletonCard(height = 200.dp)
        SkeletonCard(height = 100.dp)
        SkeletonCard(height = 100.dp)
        SkeletonCard(height = 140.dp)
    }
}

@Composable
private fun ProfileContent(
    user: User,
    userRank: Int,
    onNavigateToSettings: () -> Unit,
    onNavigateToAdmin: () -> Unit,
    onSignOut: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 52.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // ── Header row ───────────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Profile",
                fontSize   = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color      = TextPrimary
            )
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onNavigateToSettings, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Rounded.Settings, null, tint = TextSecondary, modifier = Modifier.size(22.dp))
                }
            }
        }

        // ── Hero card ────────────────────────────────────────────────────────
        NtfGradientCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    // Avatar
                    Box(modifier = Modifier.size(72.dp)) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(SurfaceElevated)
                                .border(2.dp, AccentCyan.copy(0.6f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (user.photoURL.isNotEmpty()) {
                                AsyncImage(
                                    model              = user.photoURL,
                                    contentDescription = null,
                                    contentScale       = ContentScale.Crop,
                                    modifier           = Modifier.fillMaxSize().clip(CircleShape)
                                )
                            } else {
                                Text(
                                    user.name.firstOrNull()?.uppercaseChar()?.toString() ?: "U",
                                    fontSize   = 28.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color      = TextPrimary
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .align(Alignment.BottomEnd)
                                .background(AccentCyan, CircleShape)
                                .border(1.5.dp, SurfaceCard, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.Edit, null, tint = Color.White, modifier = Modifier.size(11.dp))
                        }
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(user.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            if (user.isPremium) {
                                Spacer(Modifier.width(6.dp))
                                PremiumBadge(badge = user.toPremiumMembership().badge)
                            }
                        }
                        Text(user.email, fontSize = 11.sp, color = TextMuted)
                        Spacer(Modifier.height(2.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            XpBadge(xp = user.xp)
                            if (user.isAdmin) {
                                Box(
                                    modifier = Modifier
                                        .background(AdminPurple.copy(0.15f), RoundedCornerShape(6.dp))
                                        .border(1.dp, AdminPurple.copy(0.4f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 7.dp, vertical = 3.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Rounded.AdminPanelSettings, null, tint = AdminPurple, modifier = Modifier.size(11.dp))
                                        Spacer(Modifier.width(3.dp))
                                        Text("Admin", fontSize = 10.sp, color = AdminPurple, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
                // Trophy illustration area
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(AccentCyan.copy(0.10f), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.TrackChanges, null, tint = AccentCyan, modifier = Modifier.size(26.dp))
                }
            }
        }

        // ── Stats grid ───────────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ProfileStatTile(Icons.Rounded.LocalFireDepartment, "${user.streak}", "Streak", PremiumGold, Modifier.weight(1f))
            ProfileStatTile(Icons.Rounded.Bolt, "${user.xp}", "XP", AccentCyan, Modifier.weight(1f))
            ProfileStatTile(Icons.Rounded.EmojiEvents, "#$userRank", "Global Rank", AccentViolet, Modifier.weight(1f))
        }

        // ── Level progress ───────────────────────────────────────────────────
        GlowingLevelCard(xp = user.xp, modifier = Modifier.fillMaxWidth())

        // ── Streak card ───────────────────────────────────────────────────────
        StreakCard(streak = user.streak, modifier = Modifier.fillMaxWidth())

        // ── Membership card ───────────────────────────────────────────────────
        CurrentMembershipCard(membership = user.toPremiumMembership(), onManageClick = {})

        // ── Achievements ─────────────────────────────────────────────────────
        AchievementsSection(
            quizzesCompleted = user.totalQuizzes,
            streak           = user.streak,
            resourcesOpened  = user.lecturesWatched + user.pdfsRead,
            level            = user.level
        )

        // ── Study stats ───────────────────────────────────────────────────────
        NtfCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("Study Stats", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StudyStatItem(Icons.Rounded.Quiz,                 "${user.totalQuizzes}", "Quizzes",    AccentCyan)
                    StudyStatItem(Icons.Rounded.LocalFireDepartment,  "${user.streak}",            "Streak",     PremiumGold)
                    StudyStatItem(Icons.Rounded.Star,                 "${user.lecturesWatched + user.pdfsRead}",   "Resources",  AccentViolet)
                    StudyStatItem(Icons.Rounded.EmojiEvents,         "Lv.${user.level}",          "Level",      NeonGreen)
                }
            }
        }

        // ── Account details ───────────────────────────────────────────────────
        NtfCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("Account", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                NeonDivider()
                ProfileInfoRow(Icons.Rounded.Email,         "Email",          user.email)
                ProfileInfoRow(Icons.Rounded.EmojiEvents,   "XP Points",      "${user.xp} XP")
                ProfileInfoRow(Icons.Rounded.LocalFireDepartment, "Streak",   "${user.streak} days")
                val joinedStr = try {
                    SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(user.createdAt.toDate())
                } catch (_: Exception) { "—" }
                ProfileInfoRow(Icons.Rounded.CalendarMonth, "Joined On", joinedStr)
            }
        }

        // ── Navigation shortcuts ──────────────────────────────────────────────
        NtfCard(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onNavigateToSettings),
            borderColor = AccentCyan.copy(0.2f)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Settings, null, tint = AccentCyan, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(12.dp))
                    Text("Settings & Preferences", fontSize = 14.sp, color = TextPrimary)
                }
                Icon(Icons.Rounded.ChevronRight, null, tint = TextMuted, modifier = Modifier.size(18.dp))
            }
        }

        if (user.isAdmin) {
            NtfCard(
                modifier = Modifier.fillMaxWidth().clickable(onClick = onNavigateToAdmin),
                borderColor = AdminPurple.copy(0.3f)
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.AdminPanelSettings, null, tint = AdminPurple, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Admin Panel", fontSize = 14.sp, color = AdminPurple, fontWeight = FontWeight.SemiBold)
                            Text("Manage app content & users", fontSize = 11.sp, color = TextMuted)
                        }
                    }
                    Icon(Icons.Rounded.ChevronRight, null, tint = AdminPurple.copy(0.7f), modifier = Modifier.size(18.dp))
                }
            }
        }

        // ── Sign out ──────────────────────────────────────────────────────────
        NtfOutlinedButton(text = "Sign Out", onClick = onSignOut, accentColor = ErrorRed)
    }
}

@Composable
private fun ProfileStatTile(icon: ImageVector, value: String, label: String, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(color.copy(0.07f), RoundedCornerShape(14.dp))
            .border(1.dp, color.copy(0.2f), RoundedCornerShape(14.dp))
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            Text(value, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = color)
            Text(label, fontSize = 10.sp, color = TextMuted)
        }
    }
}

@Composable
private fun StudyStatItem(icon: ImageVector, value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(
            modifier = Modifier.size(36.dp).background(color.copy(0.12f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(17.dp))
        }
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
        Text(label, fontSize = 9.sp, color = TextMuted)
    }
}

@Composable
private fun ProfileInfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = AccentCyan.copy(0.7f), modifier = Modifier.size(15.dp))
            Spacer(Modifier.width(8.dp))
            Text(label, fontSize = 12.sp, color = TextSecondary)
        }
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
    }
}
