package com.nexttoppers.feed.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Construction
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Quiz
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.nexttoppers.feed.ui.theme.PremiumGold
import com.nexttoppers.feed.ui.theme.SurfaceCard
import com.nexttoppers.feed.ui.theme.TextMuted
import com.nexttoppers.feed.ui.theme.TextPrimary
import com.nexttoppers.feed.ui.theme.TextSecondary

private val QuizOrange = Color(0xFFFF6B35)

@Composable
fun AdminQuizManagementScreen(
    onBack: () -> Unit
) {
    Box(Modifier.fillMaxSize().background(BackgroundBlack)) {
        Box(
            Modifier.fillMaxWidth().height(200.dp)
                .background(Brush.radialGradient(listOf(QuizOrange.copy(0.07f), Color.Transparent)))
        )

        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 56.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            item {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
                        Icon(Icons.Rounded.ArrowBack, null, tint = TextSecondary)
                    }
                    Spacer(Modifier.width(8.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            "Quiz Management",
                            style = TextStyle(
                                fontSize = 22.sp, fontWeight = FontWeight.ExtraBold,
                                brush = Brush.linearGradient(listOf(QuizOrange, PremiumGold)),
                                shadow = Shadow(QuizOrange.copy(0.4f), Offset.Zero, 12f)
                            )
                        )
                        Text("Create & manage quizzes", color = TextMuted, fontSize = 12.sp)
                    }
                    Icon(Icons.Rounded.Quiz, null, tint = QuizOrange.copy(0.6f), modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.height(16.dp))
            }

            // Coming soon
            item {
                Row(
                    Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(QuizOrange.copy(0.08f))
                        .border(1.dp, QuizOrange.copy(0.25f), RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.Construction, null, tint = QuizOrange, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Quiz Editor Coming Soon", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Full quiz management in the next update", color = TextMuted, fontSize = 11.sp)
                    }
                }
                Spacer(Modifier.height(24.dp))
            }

            // Feature cards
            item {
                Text("Planned Features", color = TextSecondary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(Modifier.height(12.dp))
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    PlannedFeatureCard("Create Quiz", "Build new MCQ/short answer quizzes", Icons.Rounded.Add, NeonGreen)
                    PlannedFeatureCard("Assign XP Rewards", "Set XP points for quiz completions", Icons.Rounded.Star, PremiumGold)
                    PlannedFeatureCard("Premium Quizzes", "Lock quizzes for premium members only", Icons.Rounded.Lock, QuizOrange)
                    PlannedFeatureCard("Leaderboard Quizzes", "Boost XP with competitive quiz events", Icons.Rounded.EmojiEvents, NeonCyan)
                }
            }
        }
    }
}

@Composable
private fun PlannedFeatureCard(title: String, desc: String, icon: ImageVector, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.horizontalGradient(listOf(color.copy(0.08f), SurfaceCard)))
            .border(1.dp, color.copy(0.2f), RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(38.dp)
                .background(color.copy(0.12f), RoundedCornerShape(10.dp))
                .border(1.dp, color.copy(0.25f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Text(desc, color = TextMuted, fontSize = 11.sp)
        }
        Box(
            Modifier.background(color.copy(0.12f), RoundedCornerShape(6.dp))
                .padding(horizontal = 7.dp, vertical = 3.dp)
        ) {
            Text("SOON", color = color, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        }
    }
}
