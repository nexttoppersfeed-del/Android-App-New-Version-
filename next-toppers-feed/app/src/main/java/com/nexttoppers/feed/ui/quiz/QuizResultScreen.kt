package com.nexttoppers.feed.ui.quiz

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nexttoppers.feed.data.model.Question
import com.nexttoppers.feed.ui.components.NeonDivider
import com.nexttoppers.feed.ui.theme.BackgroundBlack
import com.nexttoppers.feed.ui.theme.NeonCyan
import com.nexttoppers.feed.ui.theme.NeonGreen
import com.nexttoppers.feed.ui.theme.PremiumGold
import com.nexttoppers.feed.ui.theme.SurfaceCard
import com.nexttoppers.feed.ui.theme.SurfaceElevated
import com.nexttoppers.feed.ui.theme.TextMuted
import com.nexttoppers.feed.ui.theme.TextPrimary
import com.nexttoppers.feed.ui.theme.TextSecondary

@Composable
fun QuizResultScreen(
    onBack: () -> Unit,
    onRetakeQuiz: (String) -> Unit,
    playerViewModel: QuizPlayerViewModel,
    viewModel: QuizResultViewModel = hiltViewModel()
) {

    val result = playerViewModel.result.value

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundBlack)
    ) {

        if (result == null) {

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                CircularProgressIndicator(
                    color = NeonGreen
                )
            }

        } else {

            ResultContent(
                result = result,
                quizId = viewModel.quizId,
                onBack = onBack,
                onRetake = {
                    onRetakeQuiz(viewModel.quizId)
                }
            )
        }
    }
}

@Composable
private fun ResultContent(
    result: QuizResult,
    quizId: String,
    onBack: () -> Unit,
    onRetake: () -> Unit
) {

    LazyColumn(
        modifier = Modifier.fillMaxSize(),

        contentPadding = PaddingValues(
            start = 20.dp,
            top = 0.dp,
            end = 20.dp,
            bottom = 48.dp
        )
    ) {

        item {
            Spacer(modifier = Modifier.height(52.dp))
        }

        item {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                IconButton(
                    onClick = onBack
                ) {

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                SurfaceElevated.copy(alpha = 0.9f),
                                RoundedCornerShape(50.dp)
                            ),

                        contentAlignment = Alignment.Center
                    ) {

                        Icon(
                            imageVector = Icons.Rounded.ArrowBack,
                            contentDescription = null,
                            tint = TextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(
                modifier = Modifier.height(8.dp)
            )
        }

        item {

            ScoreRing(
                accuracy = result.accuracy,
                score = result.score,
                total = result.total
            )

            Spacer(
                modifier = Modifier.height(20.dp)
            )
        }

        item {

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = result.quizTitle,

                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,

                        brush = Brush.linearGradient(
                            listOf(
                                NeonGreen,
                                NeonCyan
                            )
                        ),

                        shadow = Shadow(
                            color = NeonGreen.copy(alpha = 0.3f),
                            offset = Offset.Zero,
                            blurRadius = 10f
                        )
                    ),

                    textAlign = TextAlign.Center
                )

                Spacer(
                    modifier = Modifier.height(10.dp)
                )

                Text(
                    text = result.motivationalMessage(),
                    color = TextSecondary,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(
                modifier = Modifier.height(20.dp)
            )
        }

        item {

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    NeonGreen,
                                    NeonCyan
                                )
                            )
                        )
                        .clickable(
                            onClick = onRetake
                        ),

                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = "🔁 Retake Quiz",
                        color = BackgroundBlack,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(SurfaceCard)
                        .border(
                            1.dp,
                            NeonCyan.copy(alpha = 0.4f),
                            RoundedCornerShape(14.dp)
                        )
                        .clickable(
                            onClick = onBack
                        ),

                    contentAlignment = Alignment.Center
                ) {

                    Text(
                        text = "← Back",
                        color = NeonCyan,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            NeonDivider()

            Spacer(
                modifier = Modifier.height(16.dp)
            )
        }

        itemsIndexed(result.questions) { index, question ->

            AnswerReviewCard(
                index = index,
                question = question,
                selected = result.answers[index]
            )

            Spacer(
                modifier = Modifier.height(10.dp)
            )
        }
    }
}

@Composable
private fun ScoreRing(
    accuracy: Float,
    score: Int,
    total: Int
) {

    var triggered by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {
        triggered = true
    }

    val animAcc by animateFloatAsState(
        targetValue =
            if (triggered) accuracy else 0f,

        animationSpec = tween(
            durationMillis = 1000,
            easing = FastOutSlowInEasing
        ),

        label = "scoreAnim"
    )

    val arcColor =
        when {
            accuracy >= 0.75f -> NeonGreen
            accuracy >= 0.5f -> NeonCyan
            else -> Color(0xFFFF6B6B)
        }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {

        Box(
            modifier = Modifier.size(160.dp),
            contentAlignment = Alignment.Center
        ) {

            CircularProgressIndicator(
                progress = { animAcc },
                modifier = Modifier.size(160.dp),
                color = arcColor,
                trackColor = SurfaceElevated,
                strokeWidth = 10.dp,
                strokeCap = StrokeCap.Round
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = "${(animAcc * 100).toInt()}%",

                    style = TextStyle(
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold,

                        brush = Brush.linearGradient(
                            listOf(
                                arcColor,
                                NeonCyan
                            )
                        )
                    )
                )

                Text(
                    text = "$score / $total",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
            }
        }

        if (accuracy >= 0.8f) {

            Box(
                modifier = Modifier.size(200.dp),
                contentAlignment = Alignment.TopEnd
            ) {

                Icon(
                    imageVector = Icons.Rounded.EmojiEvents,
                    contentDescription = null,
                    tint = PremiumGold,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
private fun AnswerReviewCard(
    index: Int,
    question: Question,
    selected: Int?
) {

    val isCorrect =
        selected == question.correctAnswerIndex

    val accent =
        if (isCorrect) {
            NeonGreen
        } else {
            Color(0xFFFF6B6B)
        }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceCard)
            .border(
                1.dp,
                accent.copy(alpha = 0.25f),
                RoundedCornerShape(16.dp)
            )
            .padding(14.dp),

        verticalArrangement =
            Arrangement.spacedBy(10.dp)
    ) {

        Text(
            text = question.question,
            color = TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
