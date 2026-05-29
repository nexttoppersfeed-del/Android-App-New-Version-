package com.nexttoppers.feed.ui.quiz

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.nexttoppers.feed.data.model.Question
import com.nexttoppers.feed.data.model.Quiz
import com.nexttoppers.feed.ui.quiz.components.CircularTimer
import com.nexttoppers.feed.ui.quiz.components.LinearTimer
import com.nexttoppers.feed.ui.theme.BackgroundBlack
import com.nexttoppers.feed.ui.theme.NeonCyan
import com.nexttoppers.feed.ui.theme.NeonGreen
import com.nexttoppers.feed.ui.theme.SurfaceCard
import com.nexttoppers.feed.ui.theme.SurfaceElevated
import com.nexttoppers.feed.ui.theme.TextMuted
import com.nexttoppers.feed.ui.theme.TextPrimary
import com.nexttoppers.feed.ui.theme.TextSecondary

@Composable
fun QuizPlayerScreen(
    onBack: () -> Unit,
    onNavigateToResult: (quizId: String, score: Int, total: Int, timeTaken: Int, xpEarned: Int, title: String) -> Unit,
    viewModel: QuizPlayerViewModel = hiltViewModel()
) {
    val uiState        by viewModel.uiState.collectAsState()
    val currentIndex   by viewModel.currentIndex.collectAsState()
    val answers        by viewModel.answers.collectAsState()
    val timeRemaining  by viewModel.timeRemaining.collectAsState()
    val showDialog     by viewModel.showSubmitDialog.collectAsState()
    val result         by viewModel.result.collectAsState()

    // Navigate when quiz is submitted
    LaunchedEffect(uiState, result) {
        if (uiState is QuizPlayerUiState.Submitted && result != null) {
            val r = result!!
            onNavigateToResult(r.quizId, r.score, r.total, r.timeTaken, r.xpEarned, r.quizTitle)
        }
    }

    Box(Modifier.fillMaxSize().background(BackgroundBlack)) {
        when (val state = uiState) {
            is QuizPlayerUiState.Loading -> {
                Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    CircularProgressIndicator(color = NeonGreen, modifier = Modifier.size(52.dp), strokeWidth = 3.dp)
                    Spacer(Modifier.height(16.dp))
                    Text("Loading quiz…", color = NeonGreen)
                }
            }
            is QuizPlayerUiState.Error -> {
                Column(Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Text("Failed to load quiz", color = TextSecondary, fontSize = 18.sp)
                    Text(state.message, color = TextMuted, fontSize = 12.sp)
                }
                IconButton(onClick = onBack, modifier = Modifier.align(Alignment.TopStart).padding(top = 52.dp, start = 12.dp)) {
                    Icon(Icons.Rounded.ArrowBack, null, tint = TextPrimary)
                }
            }
            is QuizPlayerUiState.Submitted -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = NeonGreen)
                }
            }
            is QuizPlayerUiState.Ready -> {
                QuizPlayerContent(
                    quiz          = state.quiz,
                    questions     = state.questions,
                    currentIndex  = currentIndex,
                    answers       = answers,
                    timeRemaining = timeRemaining,
                    onBack        = onBack,
                    onSelectAnswer = viewModel::selectAnswer,
                    onPrev        = viewModel::goToPrev,
                    onNext        = viewModel::goToNext,
                    onSubmit      = viewModel::requestSubmit
                )
            }
        }

        // Submit confirmation dialog
        if (showDialog) {
            val ready = uiState as? QuizPlayerUiState.Ready
            val answered = answers.size
            val total = ready?.questions?.size ?: 0
            AlertDialog(
                onDismissRequest = viewModel::dismissSubmitDialog,
                title = { Text("Submit Quiz?", color = TextPrimary, fontWeight = FontWeight.Bold) },
                text  = {
                    Column {
                        Text("You have answered $answered out of $total questions.", color = TextSecondary)
                        if (answered < total) {
                            Spacer(Modifier.height(8.dp))
                            Text("${total - answered} question(s) unanswered.", color = Color(0xFFFFB347))
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = viewModel::confirmSubmit) {
                        Text("Submit", color = NeonGreen, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = viewModel::dismissSubmitDialog) {
                        Text("Cancel", color = TextMuted)
                    }
                },
                containerColor = SurfaceCard
            )
        }
    }
}

// ── Quiz content ───────────────────────────────────────────────────────────────
@Composable
private fun QuizPlayerContent(
    quiz: Quiz,
    questions: List<Question>,
    currentIndex: Int,
    answers: Map<Int, Int>,
    timeRemaining: Int,
    onBack: () -> Unit,
    onSelectAnswer: (Int, Int) -> Unit,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onSubmit: () -> Unit
) {
    val totalTime = quiz.durationSeconds()
    val progress  = if (questions.isNotEmpty()) (currentIndex + 1).toFloat() / questions.size else 0f
    val answered  = answers.size

    Column(Modifier.fillMaxSize()) {
        // ── Top bar ───────────────────────────────────────────────────────────
        Column(
            Modifier
                .fillMaxWidth()
                .background(BackgroundBlack)
                .padding(horizontal = 16.dp)
                .padding(top = 52.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Rounded.Close, null, tint = TextSecondary)
                }
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Text(quiz.title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 1)
                    Text("${answered}/${questions.size} answered", color = TextMuted, fontSize = 11.sp)
                }
                CircularTimer(
                    timeRemaining = timeRemaining,
                    totalTime     = totalTime,
                    size          = 64.dp,
                    strokeWidth   = 5.dp
                )
            }

            // Quiz progress bar
            val animProgress by animateFloatAsState(progress, tween(300), label = "progress")
            LinearProgressIndicator(
                progress     = { animProgress },
                modifier     = Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(50.dp)),
                color        = NeonGreen,
                trackColor   = SurfaceElevated,
                strokeCap    = StrokeCap.Round
            )

            // Timer bar
            LinearTimer(
                timeRemaining = timeRemaining,
                totalTime     = totalTime,
                modifier      = Modifier.fillMaxWidth().height(4.dp)
            )
        }

        // ── Question pager ────────────────────────────────────────────────────
        AnimatedContent(
            targetState = currentIndex,
            transitionSpec = {
                if (targetState > initialState)
                    slideInHorizontally(tween(280)) { it / 2 } + fadeIn(tween(280)) togetherWith
                    slideOutHorizontally(tween(200)) { -it / 2 } + fadeOut(tween(200))
                else
                    slideInHorizontally(tween(280)) { -it / 2 } + fadeIn(tween(280)) togetherWith
                    slideOutHorizontally(tween(200)) { it / 2 } + fadeOut(tween(200))
            },
            modifier = Modifier.weight(1f),
            label = "questionAnim"
        ) { qIndex ->
            val question = questions.getOrNull(qIndex)
            if (question != null) {
                QuestionView(
                    questionNumber  = qIndex + 1,
                    total           = questions.size,
                    question        = question,
                    selectedOption  = answers[qIndex],
                    onSelectOption  = { opt -> onSelectAnswer(qIndex, opt) }
                )
            }
        }

        // ── Bottom nav ────────────────────────────────────────────────────────
        Row(
            Modifier
                .fillMaxWidth()
                .background(BackgroundBlack)
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick  = onPrev,
                enabled  = currentIndex > 0,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceCard)
                    .size(48.dp)
            ) {
                Icon(Icons.Rounded.ChevronLeft, null, tint = if (currentIndex > 0) NeonGreen else TextMuted)
            }

            if (currentIndex == questions.size - 1) {
                // Submit button on last question
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp)
                        .height(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Brush.linearGradient(listOf(NeonGreen, NeonCyan)))
                        .clickable(onClick = onSubmit),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Submit Quiz", color = BackgroundBlack, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            } else {
                // Question dot navigator
                Row(
                    Modifier.weight(1f),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val dotsToShow = minOf(questions.size, 7)
                    val start = maxOf(0, minOf(currentIndex - 3, questions.size - dotsToShow))
                    (start until start + dotsToShow).forEach { idx ->
                        val isSelected = idx == currentIndex
                        val hasAnswer  = answers.containsKey(idx)
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 3.dp)
                                .size(if (isSelected) 10.dp else 7.dp)
                                .background(
                                    when {
                                        isSelected -> NeonGreen
                                        hasAnswer  -> NeonCyan.copy(0.6f)
                                        else       -> SurfaceElevated
                                    },
                                    RoundedCornerShape(50.dp)
                                )
                                .clickable { /* TODO: goToQuestion(idx) */ }
                        )
                    }
                }
            }

            IconButton(
                onClick  = onNext,
                enabled  = currentIndex < questions.size - 1,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceCard)
                    .size(48.dp)
            ) {
                Icon(Icons.Rounded.ChevronRight, null, tint = if (currentIndex < questions.size - 1) NeonGreen else TextMuted)
            }
        }
    }
}

// ── Individual question view ───────────────────────────────────────────────────
@Composable
private fun QuestionView(
    questionNumber: Int,
    total: Int,
    question: Question,
    selectedOption: Int?,
    onSelectOption: (Int) -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Question number
        Text(
            "Q${questionNumber} of $total",
            color = NeonCyan, fontSize = 13.sp, fontWeight = FontWeight.Medium
        )

        // Question text
        Box(
            Modifier
                .fillMaxWidth()
                .background(SurfaceCard, RoundedCornerShape(18.dp))
                .border(1.dp, NeonGreen.copy(0.18f), RoundedCornerShape(18.dp))
                .padding(20.dp)
        ) {
            Text(
                question.question,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                lineHeight = 26.sp
            )
        }

        // Optional image
        if (question.imageUrl.isNotEmpty()) {
            AsyncImage(
                model = question.imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(SurfaceCard)
            )
        }

        // Answer options
        val optionLabels = listOf("A", "B", "C", "D")
        question.options.forEachIndexed { index, optionText ->
            val isSelected = selectedOption == index
            val optionAccent = if (isSelected) NeonGreen else NeonCyan.copy(0.5f)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isSelected) NeonGreen.copy(0.1f) else SurfaceCard)
                    .border(
                        1.5.dp,
                        if (isSelected) NeonGreen else SurfaceElevated,
                        RoundedCornerShape(14.dp)
                    )
                    .clickable { onSelectOption(index) }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Option letter circle
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            if (isSelected) NeonGreen else SurfaceElevated,
                            RoundedCornerShape(50.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        optionLabels.getOrElse(index) { "?" },
                        color = if (isSelected) BackgroundBlack else TextSecondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    optionText,
                    color = if (isSelected) TextPrimary else TextSecondary,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Spacer(Modifier.height(80.dp))
    }
}
