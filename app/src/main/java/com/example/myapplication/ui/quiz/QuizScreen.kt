package com.example.myapplication.ui.quiz

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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val screenBg = Color(0xFFFAFAF7)
private val cardBg = Color(0xFFFFFFFF)
private val cardBorder = Color(0xFFEEEEEE)
private val primaryText = Color(0xFF2E4A32)
private val mutedText = Color(0xFF8B9590)
private val accentGreen = Color(0xFF2E7D32)
private val mediumGreen = Color(0xFF43A047)
private val heroGradientStart = Color(0xFFE8F5E9)
private val heroGradientEnd = Color(0xFFD4EDDA)
private val correctBg = Color(0xFFE8F5E9)
private val correctText = Color(0xFF1B5E20)
private val wrongBg = Color(0xFFFFEBEE)
private val wrongText = Color(0xFFB71C1C)
private val wrongRed = Color(0xFFC62828)
private val labelCircleBg = Color(0xFFF5F5F5)
private val categoryEnvGreen = Color(0xFF2E7D32)
private val categoryHealthAmber = Color(0xFFF57C00)
private val categorySolutionGreen = Color(0xFF43A047)
private val questionTextColor = Color(0xFF1B5E20)

@Composable
fun QuizScreen(
    onNavigateBack: () -> Unit,
    viewModel: QuizViewModel
) {
    val state by viewModel.uiState.collectAsState()

    when {
        state.isLoading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(screenBg),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = accentGreen)
            }
        }
        state.error != null -> {
            ErrorContent(
                message = state.error!!,
                onRetry = { viewModel.resetSession() },
                onBack = onNavigateBack
            )
        }
        state.isSessionComplete -> {
            QuizSummaryScreen(
                score = state.score,
                totalPoints = QuizViewModel.QUESTIONS_PER_SESSION * QuizViewModel.POINTS_PER_CORRECT,
                answeredQuestions = state.answeredQuestions,
                treeScoreMessage = state.treeScoreMessage,
                alreadyPlayedToday = state.treePointsAwarded == 0,
                onTryAgain = { viewModel.resetSession() },
                onNavigateBack = onNavigateBack
            )
        }
        state.currentQuestion != null -> {
            QuestionContent(
                state = state,
                onAnswerSelected = { viewModel.selectAnswer(it) },
                onNext = { viewModel.nextQuestion() }
            )
        }
    }
}

@Composable
private fun QuestionContent(
    state: QuizUiState,
    onAnswerSelected: (Int) -> Unit,
    onNext: () -> Unit
) {
    val question = state.currentQuestion ?: return
    val isLastQuestion = state.questionNumber >= QuizViewModel.QUESTIONS_PER_SESSION

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(screenBg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 18.dp)
    ) {
        // A) Top header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Question ${state.questionNumber}/${QuizViewModel.QUESTIONS_PER_SESSION}",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = primaryText
            )
            CategoryBadge(category = question.category)
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Score: ${state.score}",
            fontSize = 11.sp,
            color = mutedText
        )

        Spacer(modifier = Modifier.height(18.dp))

        // B) Question card with mint gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(heroGradientStart, heroGradientEnd),
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    )
                )
                .padding(24.dp)
        ) {
            Text(
                text = question.text,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = questionTextColor,
                lineHeight = 25.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // C) Answer options
        val options = listOf(question.option1, question.option2, question.option3, question.option4)
        val labels = listOf("A", "B", "C", "D")

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            options.forEachIndexed { index, option ->
                AnswerCard(
                    label = labels[index],
                    text = option,
                    index = index,
                    selectedAnswer = state.selectedAnswer,
                    correctIndex = question.correctIndex,
                    showCorrectAnswer = state.showCorrectAnswer,
                    enabled = state.selectedAnswer == null,
                    onClick = { onAnswerSelected(index) }
                )
            }
        }

        // E) Feedback panel
        if (state.showCorrectAnswer) {
            Spacer(modifier = Modifier.height(18.dp))

            val isCorrect = state.selectedAnswer == question.correctIndex
            FeedbackPanel(
                isCorrect = isCorrect,
                explanation = question.explanation
            )

            Spacer(modifier = Modifier.height(20.dp))

            // F) Next button
            Button(
                onClick = onNext,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accentGreen),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text(
                    text = if (isLastQuestion) "See results" else "Next question",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun CategoryBadge(category: String) {
    val (bgColor, label) = when (category.uppercase()) {
        "RED" -> categoryEnvGreen to "Environment"
        "YELLOW" -> categoryHealthAmber to "Health"
        "GREEN" -> categorySolutionGreen to "Solutions"
        else -> mutedText to category
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color.White,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun AnswerCard(
    label: String,
    text: String,
    index: Int,
    selectedAnswer: Int?,
    correctIndex: Int,
    showCorrectAnswer: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val isCorrect = index == correctIndex
    val isSelected = index == selectedAnswer

    val (bgColor, borderColor, borderWidth) = when {
        !showCorrectAnswer -> Triple(cardBg, cardBorder, 0.5.dp)
        isCorrect -> Triple(correctBg, accentGreen, 2.dp)
        isSelected -> Triple(wrongBg, wrongRed, 2.dp)
        else -> Triple(cardBg, cardBorder, 0.5.dp)
    }

    val (circleColor, circleTextColor, textColor) = when {
        !showCorrectAnswer -> Triple(labelCircleBg, primaryText, primaryText)
        isCorrect -> Triple(accentGreen, Color.White, correctText)
        isSelected -> Triple(wrongRed, Color.White, wrongText)
        else -> Triple(labelCircleBg, primaryText, primaryText)
    }

    val opacity = if (showCorrectAnswer && !isCorrect && !isSelected) 0.5f else 1f
    val isMedium = showCorrectAnswer && (isCorrect || isSelected)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(opacity)
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .border(borderWidth, borderColor, RoundedCornerShape(14.dp))
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 18.dp, vertical = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Letter circle
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(circleColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = circleTextColor
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Text(
                text = text,
                fontSize = 14.sp,
                color = textColor,
                fontWeight = if (isMedium) FontWeight.Medium else FontWeight.Normal,
                modifier = Modifier.weight(1f)
            )

            // Checkmark or X
            if (showCorrectAnswer && isCorrect) {
                Text(text = "\u2713", fontSize = 18.sp, color = accentGreen)
            } else if (showCorrectAnswer && isSelected && !isCorrect) {
                Text(text = "\u2717", fontSize = 18.sp, color = wrongRed)
            }
        }
    }
}

@Composable
private fun FeedbackPanel(isCorrect: Boolean, explanation: String) {
    val bgColor = if (isCorrect) correctBg else wrongBg
    val accentColor = if (isCorrect) accentGreen else wrongRed
    val title = if (isCorrect) "Correct!" else "Not quite"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxWidth()
                    .background(accentColor)
                    .height(80.dp)
            )
            Column(
                modifier = Modifier
                    .padding(14.dp)
                    .weight(1f)
            ) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = accentColor
                )
                if (explanation.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = explanation,
                        fontSize = 13.sp,
                        color = primaryText,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(screenBg)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            fontSize = 14.sp,
            color = wrongRed,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = accentGreen),
            shape = RoundedCornerShape(24.dp)
        ) {
            Text("Try again", color = Color.White)
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(onClick = onBack, shape = RoundedCornerShape(24.dp)) {
            Text("Back", color = primaryText)
        }
    }
}
