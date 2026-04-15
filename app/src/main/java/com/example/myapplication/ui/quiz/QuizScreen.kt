package com.example.myapplication.ui.quiz

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun QuizScreen(
    onNavigateBack: () -> Unit,
    viewModel: QuizViewModel
) {
    val state by viewModel.uiState.collectAsState()

    when {
        state.isLoading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
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
                onTryAgain = { viewModel.resetSession() },
                onNavigateBack = onNavigateBack
            )
        }
        state.currentQuestion != null -> {
            QuestionContent(
                state = state,
                onAnswerSelected = { viewModel.selectAnswer(it) }
            )
        }
    }
}

@Composable
private fun QuestionContent(
    state: QuizUiState,
    onAnswerSelected: (Int) -> Unit
) {
    val question = state.currentQuestion ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Header: question counter + score
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Question ${state.questionNumber}/${QuizViewModel.QUESTIONS_PER_SESSION}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Score: ${state.score}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Category chip
        Text(
            text = question.category,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Question card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Text(
                text = question.text,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(24.dp),
                textAlign = TextAlign.Start
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Answer buttons
        val options = listOf(question.option1, question.option2, question.option3, question.option4)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            options.forEachIndexed { index, option ->
                AnswerButton(
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

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun AnswerButton(
    text: String,
    index: Int,
    selectedAnswer: Int?,
    correctIndex: Int,
    showCorrectAnswer: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val correctGreen = Color(0xFF4CAF50)
    val wrongRed = Color(0xFFF44336)
    val defaultColor = MaterialTheme.colorScheme.primary

    val targetColor = when {
        !showCorrectAnswer -> defaultColor
        index == correctIndex -> correctGreen
        index == selectedAnswer -> wrongRed
        else -> defaultColor
    }

    val animatedColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(durationMillis = 500),
        label = "answerColor"
    )

    val contentColor = if (showCorrectAnswer && (index == correctIndex || index == selectedAnswer)) {
        Color.White
    } else {
        MaterialTheme.colorScheme.onPrimary
    }

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .semantics { contentDescription = "Answer option: $text" },
        colors = ButtonDefaults.buttonColors(
            containerColor = animatedColor,
            contentColor = contentColor,
            disabledContainerColor = animatedColor,
            disabledContentColor = contentColor
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge
        )
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
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(onClick = onBack) {
            Text("Back")
        }
    }
}
