package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.OromoContent
import com.example.data.PhraseItem
import com.example.data.QuizProgress
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

enum class QuizMode {
    DAILY_MIX,
    QUBEE_WORDS,
    DAILY_PHRASES
}

data class DailyQuizQuestion(
    val isQubeeQuestion: Boolean,
    val prompt: String,
    val subtitle: String,
    val speakText: String,
    val options: List<String>,
    val correctAnswer: String
)

fun generateDailyQuizQuestion(
    mode: QuizMode,
    allPhrases: List<PhraseItem>,
    seedRandom: Random = Random.Default
): DailyQuizQuestion {
    val pickQubee = when (mode) {
        QuizMode.QUBEE_WORDS -> true
        QuizMode.DAILY_PHRASES -> false
        QuizMode.DAILY_MIX -> seedRandom.nextBoolean()
    }
    return if (pickQubee || allPhrases.size < 4) {
        val letters = OromoContent.qubeeLetters
        val target = letters[seedRandom.nextInt(letters.size)]
        val distractors = letters
            .filter { it.symbol != target.symbol }
            .shuffled(seedRandom)
            .take(3)
            .map { "${it.exampleWord} (${it.exampleMeaningEn})" }
        val correct = "${target.exampleWord} (${target.exampleMeaningEn})"
        DailyQuizQuestion(
            isQubeeQuestion = true,
            prompt = target.symbol.substringBefore(" "),
            subtitle = "${target.name} • ${target.soundGuideEn}",
            speakText = target.ttsPronunciation,
            options = (distractors + correct).shuffled(seedRandom),
            correctAnswer = correct
        )
    } else {
        val target = allPhrases[seedRandom.nextInt(allPhrases.size)]
        val distractors = allPhrases
            .asSequence()
            .filter { it.id != target.id && it.english != target.english }
            .shuffled(seedRandom)
            .distinctBy { it.english }
            .take(3)
            .map { it.english }
            .toList()
        DailyQuizQuestion(
            isQubeeQuestion = false,
            prompt = target.oromo,
            subtitle = target.pronunciationTip,
            speakText = target.oromo,
            options = (distractors + target.english).shuffled(seedRandom),
            correctAnswer = target.english
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PracticeQuizModal(
    isOm: Boolean,
    onSpeakText: (String) -> Unit,
    onDismiss: () -> Unit,
    allPhrases: List<PhraseItem> = OromoContent.phrases,
    quizProgress: QuizProgress = QuizProgress(),
    onRecordAnswer: (Boolean) -> Unit = {}
) {
    var mode by remember { mutableStateOf(QuizMode.QUBEE_WORDS) }
    var question by remember {
        mutableStateOf(generateDailyQuizQuestion(QuizMode.QUBEE_WORDS, allPhrases))
    }
    var selectedOption by remember { mutableStateOf<String?>(null) }
    var sessionScore by remember { mutableIntStateOf(0) }
    var sessionAnswered by remember { mutableIntStateOf(0) }

    val todayKey = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) }
    val completedToday = quizProgress.lastCompletedDate == todayKey

    fun loadNext(nextMode: QuizMode = mode) {
        mode = nextMode
        question = generateDailyQuizQuestion(nextMode, allPhrases)
        selectedOption = null
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            containerColor = MaterialTheme.colorScheme.background,
            modifier = Modifier
                .fillMaxSize()
                .testTag("practice_quiz_modal"),
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    ),
                    title = {
                        Text(
                            text = if (isOm) "Qormaata Guyyaa" else "Daily Quiz",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    actions = {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("close_quiz_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = if (isOm) "Cufi" else "Close quiz"
                            )
                        }
                    }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Mode Toggle
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    FilterChip(
                        selected = mode == QuizMode.DAILY_MIX,
                        onClick = { loadNext(QuizMode.DAILY_MIX) },
                        label = { Text(if (isOm) "Walitti Makaa" else "Mixed") },
                        modifier = Modifier.testTag("quiz_mode_mix")
                    )
                    FilterChip(
                        selected = mode == QuizMode.QUBEE_WORDS,
                        onClick = { loadNext(QuizMode.QUBEE_WORDS) },
                        label = { Text(if (isOm) "Qubee & Jechoota" else "Qubee Words") },
                        modifier = Modifier.testTag("quiz_mode_qubee")
                    )
                    FilterChip(
                        selected = mode == QuizMode.DAILY_PHRASES,
                        onClick = { loadNext(QuizMode.DAILY_PHRASES) },
                        label = { Text(if (isOm) "Haasawa Guyyaa" else "Daily Phrases") },
                        modifier = Modifier.testTag("quiz_mode_phrases")
                    )
                }

                // Persistent Progress Summary Strip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = if (isOm) {
                                    "Har'a: $sessionScore/$sessionAnswered"
                                } else {
                                    "Session: $sessionScore/$sessionAnswered"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    Surface(
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.LocalFireDepartment,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = if (isOm) {
                                    "Walitti: ${quizProgress.currentStreak}"
                                } else {
                                    "Streak: ${quizProgress.currentStreak}"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.EmojiEvents,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "${quizProgress.accuracyPercent}%",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }

                // Overall lifetime progress banner
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isOm) {
                                "Ida'ama: ${quizProgress.totalCorrect}/${quizProgress.totalAnswered} • Rikoordii: ${quizProgress.bestStreak}"
                            } else {
                                "Lifetime: ${quizProgress.totalCorrect}/${quizProgress.totalAnswered} • Best streak: ${quizProgress.bestStreak}"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (completedToday) {
                                if (isOm) "Har'aaf xumurame" else "Completed Today"
                            } else {
                                if (isOm) "Qormaata Har'aa" else "Today's Quiz"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (completedToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                        )
                    }
                }

                // Question Prompt Card
                Card(
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.28f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = if (question.isQubeeQuestion) {
                                if (isOm) "Jechi qubee kanaaf fakkeenya ta'u kami?" else "Which example matches this Qubee letter?"
                            } else {
                                if (isOm) "Hiikni haasawa kanaa Ingiliffaan maali?" else "What is the English meaning of this phrase?"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Text(
                            text = question.prompt,
                            style = if (question.isQubeeQuestion) {
                                MaterialTheme.typography.displaySmall
                            } else {
                                MaterialTheme.typography.headlineSmall
                            },
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = question.subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        FilledTonalButton(
                            onClick = { onSpeakText(question.speakText) },
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("quiz_hear_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isOm) "Sagalee Dhaggeeffadhu" else "Hear Pronunciation")
                        }
                    }
                }

                // Options
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
                        !isAnswered -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        isCorrect -> MaterialTheme.colorScheme.primary
                        isPicked -> MaterialTheme.colorScheme.secondary
                        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    }

                    OutlinedButton(
                        onClick = {
                            if (selectedOption == null) {
                                selectedOption = option
                                sessionAnswered++
                                val gotRight = option == question.correctAnswer
                                if (gotRight) {
                                    sessionScore++
                                }
                                onRecordAnswer(gotRight)
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(if (isAnswered && (isCorrect || isPicked)) 2.dp else 1.dp, borderColor),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = containerColor
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .minimumInteractiveComponentSize()
                            .testTag("quiz_option_$idx")
                    ) {
                        Text(
                            text = option,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        )
                    }
                }

                if (selectedOption != null) {
                    val gotIt = selectedOption == question.correctAnswer
                    Text(
                        text = if (gotIt) {
                            if (isOm) "Baay'ee gaarii! Deebii sirriidha." else "Great job! That is correct."
                        } else {
                            if (isOm) "Deebiin sirriin: ${question.correctAnswer}" else "Correct answer: ${question.correctAnswer}"
                        },
                        style = MaterialTheme.typography.titleSmall,
                        color = if (gotIt) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                    )

                    Button(
                        onClick = { loadNext() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("quiz_next_button")
                    ) {
                        Text(if (isOm) "Gaaffii Itti Aanu" else "Next Question")
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
