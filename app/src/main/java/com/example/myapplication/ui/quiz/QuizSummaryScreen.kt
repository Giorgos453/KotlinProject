package com.example.myapplication.ui.quiz

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val sumScreenBg = Color(0xFFFAFAF7)
private val sumCardBg = Color(0xFFFFFFFF)
private val sumCardBorder = Color(0xFFEEEEEE)
private val sumPrimary = Color(0xFF2E4A32)
private val sumMuted = Color(0xFF8B9590)
private val sumAccent = Color(0xFF2E7D32)
private val sumLightGreen = Color(0xFFE8F5E9)
private val sumWrongRed = Color(0xFFC62828)
private val sumLightRed = Color(0xFFFFEBEE)
private val sumAmberBg = Color(0xFFFFF8E1)
private val sumAmber = Color(0xFFF57C00)
private val sumAmberText = Color(0xFFE65100)

@Composable
fun QuizSummaryScreen(
    score: Int,
    totalPoints: Int,
    answeredQuestions: List<AnsweredQuestion>,
    treeScoreMessage: String? = null,
    alreadyPlayedToday: Boolean = false,
    onTryAgain: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val correctCount = answeredQuestions.count { it.wasCorrect }
    val totalCount = answeredQuestions.size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(sumScreenBg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(28.dp))

        // A) Top celebration
        Text(
            text = "\uD83C\uDF89",
            fontSize = 48.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Quiz complete",
            fontSize = 24.sp,
            fontWeight = FontWeight.Medium,
            color = sumPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "You got $correctCount out of $totalCount correct",
            fontSize = 14.sp,
            color = sumMuted,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Big score reveal
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "+$score",
                fontSize = 48.sp,
                fontWeight = FontWeight.Light,
                color = sumAccent
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "points earned",
                fontSize = 14.sp,
                color = sumMuted,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // B) Already played notice
        if (alreadyPlayedToday && treeScoreMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(sumAmberBg)
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(48.dp)
                            .background(sumAmber)
                    )
                    Box(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                        Text(
                            text = "You already played today. Come back tomorrow for more points.",
                            fontSize = 12.sp,
                            color = sumAmberText,
                            lineHeight = 17.sp
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
        }

        // C) YOUR ANSWERS section label
        Text(
            text = "YOUR ANSWERS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = sumMuted,
            letterSpacing = 1.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // D) Question review cards
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            answeredQuestions.forEachIndexed { index, answered ->
                QuestionReviewCard(
                    questionNumber = index + 1,
                    answered = answered
                )
            }
        }

        // E) Bottom actions
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onNavigateBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = sumAccent),
            shape = RoundedCornerShape(24.dp)
        ) {
            Text(
                text = "Back to home",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = onTryAgain) {
            Text(
                text = "Play again",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = sumAccent
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun QuestionReviewCard(
    questionNumber: Int,
    answered: AnsweredQuestion
) {
    val question = answered.question
    val options = listOf(question.option1, question.option2, question.option3, question.option4)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(sumCardBg)
            .border(0.5.dp, sumCardBorder, RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "QUESTION $questionNumber",
                    fontSize = 11.sp,
                    color = sumMuted,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.5.sp
                )
                ResultPill(isCorrect = answered.wasCorrect)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = question.text,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = sumPrimary,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Your answer:",
                fontSize = 11.sp,
                color = sumMuted
            )
            Text(
                text = options[answered.selectedIndex],
                fontSize = 13.sp,
                color = if (answered.wasCorrect) sumAccent else sumWrongRed,
                fontWeight = FontWeight.Medium
            )

            if (!answered.wasCorrect) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Correct answer:",
                    fontSize = 11.sp,
                    color = sumMuted
                )
                Text(
                    text = options[question.correctIndex],
                    fontSize = 13.sp,
                    color = sumAccent,
                    fontWeight = FontWeight.Medium
                )
            }

            if (question.explanation.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = question.explanation,
                    fontSize = 12.sp,
                    color = sumMuted,
                    lineHeight = 17.sp
                )
            }
        }
    }
}

@Composable
private fun ResultPill(isCorrect: Boolean) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isCorrect) sumLightGreen else sumLightRed)
            .padding(horizontal = 10.dp, vertical = 3.dp)
    ) {
        Text(
            text = if (isCorrect) "Correct" else "Wrong",
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = if (isCorrect) sumAccent else sumWrongRed
        )
    }
}
