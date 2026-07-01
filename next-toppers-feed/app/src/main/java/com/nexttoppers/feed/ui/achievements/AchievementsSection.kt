package com.nexttoppers.feed.ui.achievements

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexttoppers.feed.data.model.Achievement
import com.nexttoppers.feed.ui.theme.BackgroundBlack
import com.nexttoppers.feed.ui.theme.NeonCyan
import com.nexttoppers.feed.ui.theme.NeonGreen
import com.nexttoppers.feed.ui.theme.PremiumGold
import com.nexttoppers.feed.ui.theme.SurfaceCard
import com.nexttoppers.feed.ui.theme.SurfaceElevated
import com.nexttoppers.feed.ui.theme.TextMuted
import com.nexttoppers.feed.ui.theme.TextPrimary
import com.nexttoppers.feed.ui.theme.TextSecondary

// ── Achievements preview section (profile + home) ─────────────────────────────
@Composable
fun AchievementsSection(
    quizzesCompleted: Int = 0,
    streak: Int = 0,
    resourcesOpened: Int = 0,
    level: Int = 1,
    modifier: Modifier = Modifier
) {
    val achievements = Achievement.allAchievements().map { a ->
        a.copy(
            unlocked = when (a.id) {
                "first_quiz"    -> quizzesCompleted >= 1
                "quiz_master"   -> quizzesCompleted >= 10
                "streak_7"      -> streak >= 7
                "streak_30"     -> streak >= 30
                "resource_buff" -> resourcesOpened >= 20
                "level_5"       -> level >= 5
                "level_10"      -> level >= 10
                else            -> false
            }
        )
    }

    val unlocked = achievements.count { it.unlocked }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text("Achievements", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            androidx.compose.material3.Surface(
                shape = RoundedCornerShape(50.dp),
                color = NeonGreen.copy(0.12f)
            ) {
                Text(
                    "$unlocked / ${achievements.size}",
                    color      = NeonGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 11.sp,
                    modifier   = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        val rows = achievements.chunked(2)
        rows.forEach { pair ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                pair.forEach { achievement ->
                    AchievementChip(achievement, Modifier.weight(1f))
                }
                if (pair.size < 2) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun AchievementChip(achievement: Achievement, modifier: Modifier = Modifier) {
    val accentColor = when {
        achievement.unlocked && achievement.xpReward >= 300 -> PremiumGold
        achievement.unlocked                                 -> NeonGreen
        else                                                 -> TextMuted
    }

    androidx.compose.material3.Card(
        modifier  = modifier.alpha(if (achievement.unlocked) 1f else 0.5f),
        shape     = RoundedCornerShape(16.dp),
        colors    = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = if (achievement.unlocked)
                accentColor.copy(0.08f)
            else
                SurfaceCard
        ),
        elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 0.dp),
        border    = if (achievement.unlocked)
            androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(0.5f))
        else
            androidx.compose.foundation.BorderStroke(1.dp, SurfaceElevated)
    ) {
        Column(
            modifier            = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(achievement.icon, fontSize = 20.sp)
                if (achievement.unlocked) {
                    androidx.compose.material3.Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = NeonGreen.copy(0.15f)
                    ) {
                        Text(
                            "✓",
                            color      = NeonGreen,
                            fontSize   = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier   = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                } else {
                    androidx.compose.material3.Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = SurfaceElevated
                    ) {
                        Icon(Icons.Rounded.Lock, null, tint = TextMuted, modifier = Modifier.size(10.dp).padding(1.dp))
                    }
                }
            }
            Text(
                achievement.title,
                color      = if (achievement.unlocked) accentColor else TextSecondary,
                fontWeight = FontWeight.Bold,
                fontSize   = 12.sp
            )
            Text(
                achievement.description,
                color      = TextMuted,
                fontSize   = 10.sp,
                lineHeight = 14.sp
            )
            Text(
                "+${achievement.xpReward} XP",
                color      = if (achievement.unlocked) accentColor else TextMuted,
                fontWeight = FontWeight.SemiBold,
                fontSize   = 10.sp
            )
        }
    }
}

// ── Mini preview row for home screen ─────────────────────────────────────────
@Composable
fun AchievementsMiniRow(
    quizzesCompleted: Int = 0,
    streak: Int = 0,
    resourcesOpened: Int = 0,
    level: Int = 1,
    modifier: Modifier = Modifier
) {
    val achievements = Achievement.allAchievements().map { a ->
        a.copy(
            unlocked = when (a.id) {
                "first_quiz"    -> quizzesCompleted >= 1
                "quiz_master"   -> quizzesCompleted >= 10
                "streak_7"      -> streak >= 7
                "streak_30"     -> streak >= 30
                "resource_buff" -> resourcesOpened >= 20
                "level_5"       -> level >= 5
                "level_10"      -> level >= 10
                else            -> false
            }
        )
    }
    val unlocked   = achievements.filter { it.unlocked }.take(4)
    val unlockedCnt = achievements.count { it.unlocked }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        unlocked.forEach { a ->
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(NeonGreen.copy(0.1f), RoundedCornerShape(12.dp))
                    .border(1.dp, NeonGreen.copy(0.3f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(a.icon, fontSize = 18.sp)
            }
        }
        if (unlockedCnt == 0) {
            Text("No achievements yet — complete quizzes to unlock!", color = TextMuted, fontSize = 11.sp)
        } else if (achievements.size > 4) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(SurfaceElevated, RoundedCornerShape(12.dp))
                    .border(1.dp, TextMuted.copy(0.3f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("+${achievements.size - 4}", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
