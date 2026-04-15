package com.example.myapplication.ui.quiz

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun QuizSummaryScreen(
    score: Int,
    totalPoints: Int,
    answeredQuestions: List<AnsweredQuestion>,
    onTryAgain: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val percentage = if (totalPoints > 0) (score * 100) / totalPoints else 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Quiz Complete!",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Score display
        Text(
            text = "$score/$totalPoints points ($percentage%)",
            style = MaterialTheme.typography.headlineMedium,
            color = when {
                percentage >= 70 -> Color(0xFF4CAF50)
                percentage >= 40 -> MaterialTheme.colorScheme.primary
                else -> Color(0xFFF44336)
            },
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Question review
        answeredQuestions.forEachIndexed { index, answered ->
            QuestionReviewCard(
                questionNumber = index + 1,
                answered = answered
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onNavigateBack,
                modifier = Modifier.weight(1f)
            ) {
                Text("Back to Home")
            }
            Button(
                onClick = onTryAgain,
                modifier = Modifier.weight(1f)
            ) {
                Text("Try Again")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun QuestionReviewCard(
    questionNumber: Int,
    answered: AnsweredQuestion
) {
    val question = answered.question
    val options = listOf(question.option1, question.option2, question.option3, question.option4)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (answered.wasCorrect) {
                Color(0xFF4CAF50).copy(alpha = 0.1f)
            } else {
                Color(0xFFF44336).copy(alpha = 0.1f)
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Question $questionNumber",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = question.text,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))

            // User's answer
            Text(
                text = "Your answer: ${options[answered.selectedIndex]}",
                style = MaterialTheme.typography.bodyMedium,
                color = if (answered.wasCorrect) Color(0xFF4CAF50) else Color(0xFFF44336),
                fontWeight = FontWeight.SemiBold
            )

            // Show correct answer if wrong
            if (!answered.wasCorrect) {
                Text(
                    text = "Correct answer: ${options[question.correctIndex]}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF4CAF50),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
