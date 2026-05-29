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
import com.nexttoppers.feed.ui.achievements.AchievementsSection
import com.nexttoppers.feed.ui.components.NeonDivider
import com.nexttoppers.feed.ui.components.NtfCard
import com.nexttoppers.feed.ui.components.NtfGradientCard
import com.nexttoppers.feed.ui.components.NtfOutlinedButton
import com.nexttoppers.feed.ui.components.SkeletonCard
import com.nexttoppers.feed.ui.components.XpBadge
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
import com.nexttoppers.feed.ui.xp.GlowingLevelCard
import com.nexttoppers.feed.ui.xp.StreakCard
import com.nexttoppers.feed.ui.premium.CurrentMembershipCard
import com.nexttoppers.feed.ui.premium.PremiumBadge
import com.nexttoppers.feed.data.model.toPremiumMembership
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

    Box(
        modifier = Modifier.fillMaxSize().background(BackgroundBlack),
        contentAlignment = Alignment.Center
    ) {
        when (val state = uiState) {
            is ProfileUiState.Loading -> ProfileLoadingSkeleton()
            is ProfileUiState.Error   -> Text(state.message, color = TextSecondary)
            is ProfileUiState.Success -> {
                AnimatedVisibility(
                    visible = visible,
                    enter   = fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 8 }
                ) {
                    ProfileContent(
                        user                 = state.user,
                        userRank             = userRank,
                        onSignOut            = { viewModel.signOut(context) },
                        onNavigateToSettings = onNavigateToSettings,
                        onNavigateToAdmin    = onNavigateToAdmin
                    )
                }
            }
            else -> Unit
        }
    }
}

@Composable
private fun ProfileLoadingSkeleton() {
    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp).padding(top = 40.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SkeletonCard(height = 200.dp)
        SkeletonCard(height = 110.dp)
        SkeletonCard(height = 110.dp)
        SkeletonCard(height = 180.dp)
    }
}

@Composable
private fun ProfileContent(
    user: User,
    userRank: Int,
    onSignOut: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAdmin: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 52.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // ── Header bar ────────────────────────────────────────────────────────
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Profile",
                style = TextStyle(
                    fontSize   = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    brush      = Brush.linearGradient(listOf(NeonGreen, NeonCyan))
                )
            )
            IconButton(onClick = onNavigateToSettings) {
                Icon(Icons.Rounded.Settings, null, tint = TextSecondary)
            }
        }

        // ── Hero card ─────────────────────────────────────────────────────────
        NtfGradientCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .border(2.dp, Brush.linearGradient(listOf(NeonGreen, NeonCyan)), CircleShape)
                            .padding(3.dp)
                            .clip(CircleShape)
                            .background(SurfaceElevated),
                        contentAlignment = Alignment.Center
                    ) {
                        if (user.photoUrl.isNotEmpty()) {
                            AsyncImage(
                                model            = user.photoUrl,
                                contentDescription = "Profile photo",
                                contentScale     = ContentScale.Crop,
                                modifier         = Modifier.fillMaxSize().clip(CircleShape)
                            )
                        } else {
                            Text(
                                user.name.take(1).uppercase(),
                                color      = NeonGreen,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize   = 34.sp
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .background(NeonGreen, CircleShape)
                            .border(2.dp, SurfaceCard, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.Edit, null, tint = BackgroundBlack, modifier = Modifier.size(12.dp))
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        user.name,
                        style = TextStyle(
                            fontSize   = 20.sp,
                            fontWeight = FontWeight.Bold,
                            brush      = Brush.linearGradient(listOf(NeonGreen, NeonCyan))
                        )
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(user.email, color = TextMuted, fontSize = 12.sp)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        XpBadge(xp = user.xp)
                        if (user.isPremium) {
                            PremiumBadge(badge = user.toPremiumMembership().badge)
                        }
                        if (user.isAdmin) {
                            AdminBadge()
                        }
                    }
                }

                NeonDivider()

                // Stats row — 4 stats now
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    ProfileStat("XP",     "${user.xp}",                                   NeonGreen)
                    ProfileStat("Level",  "${user.level}",                                 NeonCyan)
                    ProfileStat("Streak", "${user.streak}d",                               Color(0xFFFF6B35))
                    ProfileStat("Rank",   if (userRank > 0) "#$userRank" else "–",         PremiumGold)
                }
            }
        }

        // ── Level card ────────────────────────────────────────────────────────
        GlowingLevelCard(xp = user.xp, modifier = Modifier.fillMaxWidth())

        // ── Membership card ───────────────────────────────────────────────────
        CurrentMembershipCard(
            membership    = user.toPremiumMembership(),
            onManageClick = onNavigateToSettings,
            modifier      = Modifier.fillMaxWidth()
        )

        // ── Streak card ───────────────────────────────────────────────────────
        StreakCard(streak = user.streak, modifier = Modifier.fillMaxWidth())

        // ── Rank card ─────────────────────────────────────────────────────────
        if (userRank > 0) {
            RankInfoCard(rank = userRank, xp = user.xp)
        }

        // ── Study stats ───────────────────────────────────────────────────────
        StudyStatsCard(user)

        // ── Achievements ──────────────────────────────────────────────────────
        NtfCard(modifier = Modifier.fillMaxWidth()) {
            AchievementsSection(
                quizzesCompleted = user.quizzesCompleted,
                streak           = user.streak,
                resourcesOpened  = user.resourcesOpened,
                level            = user.level
            )
        }

        // ── Info card ─────────────────────────────────────────────────────────
        NtfCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                ProfileInfoRow(Icons.Rounded.Email,               "Email",     user.email)
                ProfileInfoRow(Icons.Rounded.Star,                "XP",        "${user.xp} points",         NeonGreen)
                ProfileInfoRow(Icons.Rounded.LocalFireDepartment, "Streak",    "${user.streak} days",       Color(0xFFFF6B35))
                ProfileInfoRow(Icons.Rounded.EmojiEvents,         "Rank",      if (userRank > 0) "#$userRank globally" else "Unranked", PremiumGold)
                ProfileInfoRow(Icons.Rounded.WorkspacePremium,    "Plan",      if (user.isPremium) "${user.toPremiumMembership().type.displayName} Premium" else "Free", if (user.isPremium) PremiumGold else TextSecondary)
                val joined = SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(user.joinedAt.toDate())
                ProfileInfoRow(Icons.Rounded.CalendarMonth,       "Joined",    joined)
            }
        }

        // ── Settings shortcut ─────────────────────────────────────────────────
        NtfCard(
            modifier     = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
                .clickable(onClick = onNavigateToSettings),
            borderColor  = NeonCyan.copy(0.2f)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier.size(36.dp).background(NeonCyan.copy(0.12f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Settings, null, tint = NeonCyan, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(12.dp))
                Text("Settings", color = TextPrimary, fontSize = 14.sp, modifier = Modifier.weight(1f))
                Icon(Icons.Rounded.ChevronRight, null, tint = TextMuted, modifier = Modifier.size(18.dp))
            }
        }

        // ── Admin Panel shortcut (only shown when isAdmin) ────────────────────
        if (user.isAdmin) {
            NtfCard(
                modifier    = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
                    .clickable(onClick = onNavigateToAdmin),
                borderColor = AdminPurple.copy(0.35f)
            ) {
                Row(
                    Modifier.fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(listOf(AdminPurple.copy(0.08f), Color.Transparent))
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier.size(36.dp)
                            .background(AdminPurple.copy(0.15f), RoundedCornerShape(10.dp))
                            .border(1.dp, AdminPurple.copy(0.4f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.AdminPanelSettings, null, tint = AdminPurple, modifier = Modifier.size(18.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Admin Panel", color = AdminPurple, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text("Manage app content & users", color = TextMuted, fontSize = 11.sp)
                    }
                    Icon(Icons.Rounded.ChevronRight, null, tint = AdminPurple.copy(0.7f), modifier = Modifier.size(18.dp))
                }
            }
        }

        Spacer(Modifier.height(4.dp))
        NtfOutlinedButton("Sign Out", onSignOut, accentColor = ErrorRed)
    }
}

// ── Admin badge ────────────────────────────────────────────────────────────────
@Composable
private fun AdminBadge() {
    Box(
        modifier = Modifier
            .background(AdminPurple.copy(0.15f), RoundedCornerShape(50.dp))
            .border(1.dp, AdminPurple.copy(0.5f), RoundedCornerShape(50.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(Icons.Rounded.AdminPanelSettings, null, tint = AdminPurple, modifier = Modifier.size(11.dp))
            Text("ADMIN", color = AdminPurple, fontWeight = FontWeight.Bold, fontSize = 11.sp)
        }
    }
}

// ── Rank info card ─────────────────────────────────────────────────────────────
@Composable
private fun RankInfoCard(rank: Int, xp: Long) {
    val rankColor = when {
        rank == 1    -> PremiumGold
        rank <= 3    -> NeonCyan
        rank <= 10   -> NeonGreen
        else         -> TextSecondary
    }
    val badge = LevelUtils.rankBadgeEmoji(rank)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.horizontalGradient(listOf(rankColor.copy(0.10f), rankColor.copy(0.04f)))
            )
            .border(
                1.dp,
                Brush.horizontalGradient(listOf(rankColor.copy(0.5f), NeonCyan.copy(0.3f))),
                RoundedCornerShape(20.dp)
            )
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(badge, fontSize = 32.sp)
            Spacer(Modifier.width(14.dp))
            Column {
                Text(
                    "Global Rank #$rank",
                    style = TextStyle(
                        fontSize   = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        brush      = Brush.linearGradient(listOf(rankColor, NeonCyan)),
                        shadow     = Shadow(rankColor.copy(0.3f), Offset.Zero, 10f)
                    )
                )
                Text("$xp XP · ${LevelUtils.levelTitle(LevelUtils.levelForXp(xp))}", color = TextSecondary, fontSize = 12.sp)
            }
        }
    }
}

// ── Study stats card ───────────────────────────────────────────────────────────
@Composable
private fun StudyStatsCard(user: User) {
    NtfCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Study Stats", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            NeonDivider()
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                StudyStat(Icons.Rounded.Quiz,                 "Quizzes",   "${user.quizzesCompleted}",      NeonGreen)
                StudyStat(Icons.Rounded.LocalFireDepartment,  "Streak",    "${user.streak}d",               Color(0xFFFF6B35))
                StudyStat(Icons.Rounded.Star,                 "Resources", "${user.resourcesOpened}",       NeonCyan)
                StudyStat(Icons.Rounded.EmojiEvents,          "Level",     "${user.level}",                 PremiumGold)
            }
        }
    }
}

@Composable
private fun StudyStat(icon: ImageVector, label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(color.copy(0.12f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
        }
        Text(value,  color = color,        fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
        Text(label,  color = TextMuted,    fontSize   = 10.sp)
    }
}

@Composable
private fun ProfileStat(label: String, value: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(value, color = color, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text(label, color = TextMuted, fontSize = 10.sp)
    }
}

@Composable
private fun ProfileInfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    valueColor: Color = TextPrimary
) {
    Column {
        Row(
            Modifier.fillMaxWidth().padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = NeonGreen.copy(0.7f), modifier = Modifier.size(17.dp))
            Spacer(Modifier.width(12.dp))
            Text(label, color = TextSecondary, fontSize = 13.sp, modifier = Modifier.weight(1f))
            Text(value, color = valueColor, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
        NeonDivider()
    }
}

@Composable
private fun PremiumPill() {
    Box(
        modifier = Modifier
            .background(PremiumGold.copy(0.15f), RoundedCornerShape(50.dp))
            .border(1.dp, PremiumGold.copy(0.5f), RoundedCornerShape(50.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text("⭐ PRO", color = PremiumGold, fontWeight = FontWeight.Bold, fontSize = 11.sp)
    }
}
