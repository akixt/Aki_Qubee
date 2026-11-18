package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.PhraseItem
import com.example.data.QuizProgress
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

@Composable
fun DailyQuizHomeCard(
    isOm: Boolean,
    allPhrases: List<PhraseItem>,
    quizProgress: QuizProgress,
    onSpeakText: (String) -> Unit,
    onRecordAnswer: (Boolean) -> Unit,
    onOpenFullQuiz: () -> Unit,
    modifier: Modifier = Modifier
) {
    val todayKey = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) }
    val completedToday = quizProgress.lastCompletedDate == todayKey

    var focusMode by remember { mutableStateOf(QuizMode.QUBEE_WORDS) }
    var question by remember {
        val dailySeed = Random(todayKey.hashCode())
        mutableStateOf(generateDailyQuizQuestion(QuizMode.QUBEE_WORDS, allPhrases, dailySeed))
    }
    var selectedOption by remember { mutableStateOf<String?>(null) }

    fun loadQuestion(newMode: QuizMode = focusMode) {
        focusMode = newMode
        question = generateDailyQuizQuestion(newMode, allPhrases)
        selectedOption = null
    }

    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("daily_quiz_home_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Quiz,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(22.dp)
                    )
                    Column {
                        Text(
                            text = if (isOm) "Qormaata Guyyaa" else "Daily Quiz",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isOm) {
                                "Sirrii ${quizProgress.accuracyPercent}% • Walitti fufaa ${quizProgress.currentStreak}"
                            } else {
                                "${quizProgress.accuracyPercent}% accuracy • ${quizProgress.currentStreak} streak"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    color = if (completedToday) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.secondaryContainer
                    },
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.testTag("daily_quiz_status_badge")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (completedToday) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Text(
                            text = if (completedToday) {
                                if (isOm) "Har'aaf Hojjatame" else "Done Today"
                            } else {
                                if (isOm) "Shaakala Har'aa" else "Today's Q"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (completedToday) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSecondaryContainer
                            }
                        )
                    }
                }
            }

            // Switch between Qubee Character vs Daily Phrase question
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = focusMode == QuizMode.QUBEE_WORDS,
                    onClick = { loadQuestion(QuizMode.QUBEE_WORDS) },
                    label = { Text(if (isOm) "Qubee" else "Qubee Character") },
                    modifier = Modifier.testTag("daily_quiz_mode_qubee")
                )
                FilterChip(
                    selected = focusMode == QuizMode.DAILY_PHRASES,
                    onClick = { loadQuestion(QuizMode.DAILY_PHRASES) },
                    label = { Text(if (isOm) "Haasawa" else "Daily Phrase") },
                    modifier = Modifier.testTag("daily_quiz_mode_phrase")
                )
            }

            // Prompt Box
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = if (question.isQubeeQuestion) {
                                if (isOm) "Jechi fakkeenya qubee kanaa kami?" else "Which word matches this Qubee letter?"
                            } else {
                                if (isOm) "Hiikni haasawa kanaa maali?" else "What does this daily phrase mean?"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = question.prompt,
                            style = if (question.isQubeeQuestion) {
                                MaterialTheme.typography.headlineMedium
                            } else {
                                MaterialTheme.typography.titleMedium
                            },
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.testTag("daily_quiz_home_prompt")
                        )
                        Text(
                            text = question.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    FilledTonalIconButton(
                        onClick = { onSpeakText(question.speakText) },
                        modifier = Modifier.testTag("daily_quiz_home_hear")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = if (isOm) "Dhaggeeffadhu" else "Hear question audio"
                        )
                    }
                }
            }

            // 4 Multiple Choice Options
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                question.options.forEachIndexed { idx, option ->
                    val isAnswered = selectedOption != null
                    val isCorrect = option == question.correctAnswer
                    val isPicked = option == selectedOption

                    val containerColor = when {
                        !isAnswered -> MaterialTheme.colorScheme.surface
                        isCorrect -> MaterialTheme.colorScheme.primaryContainer
                        isPicked -> MaterialTheme.colorScheme.secondaryContainer
                        else -> MaterialTheme.colorScheme.surface
                    }
                    val borderColor = when {
                        !isAnswered -> MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)
                        isCorrect -> MaterialTheme.colorScheme.primary
                        isPicked -> MaterialTheme.colorScheme.secondary
                        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    }

                    OutlinedButton(
                        onClick = {
                            if (selectedOption == null) {
                                selectedOption = option
                                onRecordAnswer(option == question.correctAnswer)
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(if (isAnswered && (isCorrect || isPicked)) 2.dp else 1.dp, borderColor),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = containerColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .minimumInteractiveComponentSize()
                            .testTag("daily_quiz_home_option_$idx")
                    ) {
                        Text(
                            text = option,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                        )
                    }
                }
            }

            if (selectedOption != null) {
                val gotIt = selectedOption == question.correctAnswer
                Text(
                    text = if (gotIt) {
                        if (isOm) "Sirriidha! Baay'ee gaarii." else "Correct! Great progress."
                    } else {
                        if (isOm) "Deebiin sirriin: ${question.correctAnswer}" else "Correct answer: ${question.correctAnswer}"
                    },
                    style = MaterialTheme.typography.labelLarge,
                    color = if (gotIt) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.testTag("daily_quiz_home_feedback")
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalButton(
                    onClick = { loadQuestion() },
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("daily_quiz_home_next")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isOm) "Gaaffii Haaraa" else "Next Question")
                }

                TextButton(
                    onClick = onOpenFullQuiz,
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("open_daily_quiz_modal_button")
                ) {
                    Text(if (isOm) "Qormaata Guutuu" else "Full Quiz")
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
