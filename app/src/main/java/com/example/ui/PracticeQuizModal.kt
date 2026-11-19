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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import com.example.data.VocabularyItem
import com.example.data.local.QuizAttemptEntity
import com.example.ui.theme.QuizCorrectContainerLight
import com.example.ui.theme.QuizCorrectGreen
import com.example.ui.theme.QuizIncorrectContainerLight
import com.example.ui.theme.QuizIncorrectRed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

enum class QuizMode {
    DAILY_MIX,
    QUBEE_WORDS,
    DAILY_PHRASES,
    VOCABULARY,
    TRUE_FALSE,
    LISTENING,
    MATCHING,
    FILL_BLANK
}

data class DailyQuizQuestion(
    val isQubeeQuestion: Boolean,
    val isListeningOnly: Boolean = false,
    val isFillBlank: Boolean = false,
    val isMatching: Boolean = false,
    val matchingPairs: List<Pair<String, String>> = emptyList(),
    val instructionEn: String,
    val instructionOm: String,
    val prompt: String,
    val subtitle: String,
    val speakText: String,
    val options: List<String>,
    val correctAnswer: String,
    val explanationEn: String = "",
    val explanationOm: String = ""
)

fun generateDailyQuizQuestion(
    mode: QuizMode,
    allPhrases: List<PhraseItem>,
    seedRandom: Random = Random.Default,
    allVocabulary: List<VocabularyItem> = OromoContent.vocabularySeed
): DailyQuizQuestion {
    val effectiveMode = if (mode == QuizMode.DAILY_MIX) {
        listOf(
            QuizMode.QUBEE_WORDS,
            QuizMode.DAILY_PHRASES,
            QuizMode.VOCABULARY,
            QuizMode.TRUE_FALSE,
            QuizMode.LISTENING,
            QuizMode.FILL_BLANK
        )[seedRandom.nextInt(6)]
    } else {
        mode
    }

    val vocabPool = if (allVocabulary.size >= 4) allVocabulary else OromoContent.vocabularySeed
    return when (effectiveMode) {
        QuizMode.QUBEE_WORDS, QuizMode.DAILY_MIX -> {
            val letters = OromoContent.qubeeLetters
            val target = letters[seedRandom.nextInt(letters.size)]
            val distractors = letters.filter { it.symbol != target.symbol }.shuffled(seedRandom).take(3)
                .map { "${it.exampleWord} (${it.exampleMeaningEn})" }
            val correct = "${target.exampleWord} (${target.exampleMeaningEn})"
            DailyQuizQuestion(
                isQubeeQuestion = true,
                instructionEn = "Which example matches this Qubee letter?",
                instructionOm = "Jechi qubee kanaaf fakkeenya ta'u kami?",
                prompt = target.symbol.substringBefore(" "),
                subtitle = "${target.name} • ${target.soundGuideEn}",
                speakText = target.ttsPronunciation,
                options = (distractors + correct).shuffled(seedRandom),
                correctAnswer = correct,
                explanationEn = "'${target.exampleWord}' starts with Qubee '${target.name}' (${target.soundGuideEn}).",
                explanationOm = "'${target.exampleWord}' qubee '${target.name}' jedhuun jalqaba."
            )
        }
        QuizMode.DAILY_PHRASES -> {
            val pool = if (allPhrases.size >= 4) allPhrases else OromoContent.phrases
            val target = pool[seedRandom.nextInt(pool.size)]
            val distractors = pool.asSequence().filter { it.id != target.id && it.english != target.english }
                .shuffled(seedRandom).distinctBy { it.english }.take(3).map { it.english }.toList()
            DailyQuizQuestion(
                isQubeeQuestion = false,
                instructionEn = "What is the English meaning of this phrase?",
                instructionOm = "Hiikni haasawa kanaa Ingiliffaan maali?",
                prompt = target.oromo,
                subtitle = target.pronunciationTip,
                speakText = target.oromo,
                options = (distractors + target.english).shuffled(seedRandom),
                correctAnswer = target.english,
                explanationEn = "\"${target.oromo}\" means \"${target.english}\".",
                explanationOm = "\"${target.oromo}\" jechuun \"${target.english}\" jechuudha."
            )
        }
        QuizMode.VOCABULARY -> {
            val target = vocabPool[seedRandom.nextInt(vocabPool.size)]
            val distractors = vocabPool.asSequence().filter { it.id != target.id && it.english != target.english }
                .shuffled(seedRandom).distinctBy { it.english }.take(3).map { it.english }.toList()
            DailyQuizQuestion(
                isQubeeQuestion = false,
                instructionEn = "Translate this Afaan Oromoo vocabulary word:",
                instructionOm = "Hiika jecha kanaa Ingiliffaan fili:",
                prompt = target.oromo,
                subtitle = "/${target.pronunciation}/ • ${target.partOfSpeech}",
                speakText = target.oromo,
                options = (distractors + target.english).shuffled(seedRandom),
                correctAnswer = target.english,
                explanationEn = "${target.oromo} = ${target.english}. Example: ${target.exampleOm}",
                explanationOm = "${target.oromo} = ${target.english}. Fakkeenya: ${target.exampleOm}"
            )
        }
        QuizMode.TRUE_FALSE -> {
            val target = vocabPool[seedRandom.nextInt(vocabPool.size)]
            val showTruth = seedRandom.nextBoolean()
            val candidateEn = if (showTruth) target.english else {
                vocabPool.first { it.id != target.id && it.english != target.english }.english
            }
            val correct = if (showTruth) "True" else "False"
            DailyQuizQuestion(
                isQubeeQuestion = false,
                instructionEn = "True or False: Does this word match the translation?",
                instructionOm = "Dhugaa moo Soba: Jechi kun hiika sirrii qabaa?",
                prompt = "${target.oromo} = $candidateEn",
                subtitle = "/${target.pronunciation}/",
                speakText = target.oromo,
                options = listOf("True", "False"),
                correctAnswer = correct,
                explanationEn = "${target.oromo} translates to '${target.english}'.",
                explanationOm = "Hiikni '${target.oromo}' jedhu '${target.english}' dha."
            )
        }
        QuizMode.LISTENING -> {
            val target = vocabPool[seedRandom.nextInt(vocabPool.size)]
            val distractors = vocabPool.asSequence().filter { it.id != target.id && it.english != target.english }
                .shuffled(seedRandom).distinctBy { it.english }.take(3).map { it.english }.toList()
            DailyQuizQuestion(
                isQubeeQuestion = false,
                isListeningOnly = true,
                instructionEn = "Listen and select the matching English word:",
                instructionOm = "Dhaggeeffadhuu hiika sirrii fili:",
                prompt = "Tap audio button to listen",
                subtitle = "Phonetic clue: /${target.pronunciation}/",
                speakText = target.oromo,
                options = (distractors + target.english).shuffled(seedRandom),
                correctAnswer = target.english,
                explanationEn = "The spoken word was '${target.oromo}' (${target.english}).",
                explanationOm = "Jechi dubbatame '${target.oromo}' (${target.english}) ture."
            )
        }
        QuizMode.MATCHING -> {
            val picked = vocabPool.shuffled(seedRandom).take(3)
            val pairs = picked.map { it.oromo to it.english }
            DailyQuizQuestion(
                isQubeeQuestion = false,
                isMatching = true,
                matchingPairs = pairs,
                instructionEn = "Match each Oromo word on the left to its English meaning on the right:",
                instructionOm = "Jechoota bitaa fi mirgaa walitti firoomsi:",
                prompt = "Match 3 pairs",
                subtitle = "Tap one word left, then its match right.",
                speakText = pairs.firstOrNull()?.first ?: "Nagaa",
                options = emptyList(),
                correctAnswer = "ALL_MATCHED",
                explanationEn = pairs.joinToString(" • ") { "${it.first} = ${it.second}" },
                explanationOm = pairs.joinToString(" • ") { "${it.first} = ${it.second}" }
            )
        }
        QuizMode.FILL_BLANK -> {
            val templates = listOf(
                Triple("Akkam _____?", "jirta", "How are you? (singular)"),
                Triple("Nagaa koo dha, _____.", "galatoomi", "I am fine, thank you."),
                Triple("Gatiin isaa _____?", "meeqa", "How much does this cost?"),
                Triple("Bishaan dhugaatii naaf _____.", "fidi", "Bring me drinking water."),
                Triple("Afaan _____ barachaan jira.", "Oromoo", "I am learning Afaan Oromo.")
            )
            val chosen = templates[seedRandom.nextInt(templates.size)]
            val bank = listOf("jirta", "galatoomi", "meeqa", "fidi", "Oromoo", "nagaa")
                .filter { it != chosen.second }.shuffled(seedRandom).take(3)
            DailyQuizQuestion(
                isQubeeQuestion = false,
                isFillBlank = true,
                instructionEn = "Fill in the blank to complete the sentence:",
                instructionOm = "Bakka duwwaa guuti:",
                prompt = chosen.first,
                subtitle = chosen.third,
                speakText = chosen.first.replace("_____", chosen.second),
                options = (bank + chosen.second).shuffled(seedRandom),
                correctAnswer = chosen.second,
                explanationEn = "Complete sentence: ${chosen.first.replace("_____", chosen.second)}",
                explanationOm = "Hima guutuu: ${chosen.first.replace("_____", chosen.second)}"
            )
        }
    }
}

private const val ROUND_LENGTH = 5

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PracticeQuizModal(
    isOm: Boolean,
    onSpeakText: (String) -> Unit,
    onDismiss: () -> Unit,
    allPhrases: List<PhraseItem> = OromoContent.phrases,
    quizProgress: QuizProgress = QuizProgress(),
    onRecordAnswer: (Boolean) -> Unit = {},
    allVocabulary: List<VocabularyItem> = OromoContent.vocabularySeed,
    quizAttempts: List<QuizAttemptEntity> = emptyList(),
    onCompleteQuizSession: (mode: String, score: Int, totalQuestions: Int) -> Unit = { _, _, _ -> }
) {
    var mode by remember { mutableStateOf(QuizMode.QUBEE_WORDS) }
    var question by remember {
        mutableStateOf(generateDailyQuizQuestion(QuizMode.QUBEE_WORDS, allPhrases, allVocabulary = allVocabulary))
    }
    var selectedOption by remember { mutableStateOf<String?>(null) }
    var fillText by remember { mutableStateOf("") }
    var roundQuestionNum by remember { mutableIntStateOf(1) }
    var sessionScore by remember { mutableIntStateOf(0) }
    var sessionAnswered by remember { mutableIntStateOf(0) }
    var roundComplete by remember { mutableStateOf(false) }

    // Matching state
    var selectedLeft by remember { mutableStateOf<String?>(null) }
    var matchedLefts by remember { mutableStateOf<Set<String>>(emptySet()) }
    var matchingError by remember { mutableStateOf(false) }

    val todayKey = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) }
    val completedToday = quizProgress.lastCompletedDate == todayKey

    fun resetQuestionState(nextMode: QuizMode = mode) {
        mode = nextMode
        question = generateDailyQuizQuestion(nextMode, allPhrases, allVocabulary = allVocabulary)
        selectedOption = null
        fillText = ""
        selectedLeft = null
        matchedLefts = emptySet()
        matchingError = false
    }

    fun startFreshRound(nextMode: QuizMode = mode) {
        roundQuestionNum = 1
        sessionScore = 0
        sessionAnswered = 0
        roundComplete = false
        resetQuestionState(nextMode)
    }

    fun submitAnswer(answer: String) {
        if (selectedOption != null) return
        selectedOption = answer
        sessionAnswered++
        val gotRight = answer.trim().equals(question.correctAnswer.trim(), ignoreCase = true)
        if (gotRight) sessionScore++
        onRecordAnswer(gotRight)
    }

    fun advanceOrFinish() {
        if (roundQuestionNum >= ROUND_LENGTH) {
            onCompleteQuizSession(mode.name, sessionScore, ROUND_LENGTH)
            roundComplete = true
        } else {
            roundQuestionNum++
            resetQuestionState()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            containerColor = MaterialTheme.colorScheme.background,
            modifier = Modifier
                .fillMaxSize()
                .testTag("practice_quiz_modal"),
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                    title = {
                        Text(
                            text = if (isOm) "Qormaata Guyyaa" else "Interactive Quiz",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    actions = {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.minimumInteractiveComponentSize().testTag("close_quiz_button")
                        ) {
                            Icon(Icons.Filled.Close, contentDescription = if (isOm) "Cufi" else "Close quiz")
                        }
                    }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 8 Quiz Modes
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val chips = listOf(
                        Triple(QuizMode.DAILY_MIX, if (isOm) "Walitti Makaa" else "Mixed", "quiz_mode_mix"),
                        Triple(QuizMode.QUBEE_WORDS, if (isOm) "Qubee" else "Qubee Words", "quiz_mode_qubee"),
                        Triple(QuizMode.DAILY_PHRASES, if (isOm) "Haasawa" else "Daily Phrases", "quiz_mode_phrases"),
                        Triple(QuizMode.VOCABULARY, if (isOm) "Jechoota" else "Vocabulary", "quiz_mode_vocab"),
                        Triple(QuizMode.TRUE_FALSE, if (isOm) "Dhugaa/Soba" else "True/False", "quiz_mode_tf"),
                        Triple(QuizMode.LISTENING, if (isOm) "Dhaggeeffannaa" else "Listening", "quiz_mode_listening"),
                        Triple(QuizMode.MATCHING, if (isOm) "Walitti Firoomsi" else "Matching", "quiz_mode_matching"),
                        Triple(QuizMode.FILL_BLANK, if (isOm) "Bakka Duwwaa" else "Fill Blank", "quiz_mode_fill_blank")
                    )
                    chips.forEach { (m, label, tag) ->
                        FilterChip(
                            selected = mode == m,
                            onClick = { startFreshRound(m) },
                            label = { Text(label) },
                            modifier = Modifier.testTag(tag)
                        )
                    }
                }

                // Round Progress Bar
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(
                            text = if (isOm) "Marsaa: $roundQuestionNum / $ROUND_LENGTH" else "Question $roundQuestionNum of $ROUND_LENGTH",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = if (completedToday) (if (isOm) "Har'aaf Hojjatame" else "Done Today") else "${quizProgress.accuracyPercent}% Acc.",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    LinearProgressIndicator(
                        progress = { roundQuestionNum.toFloat() / ROUND_LENGTH.toFloat() },
                        modifier = Modifier.fillMaxWidth().height(6.dp)
                    )
                }

                // Summary Counters
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(1f)) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Filled.CheckCircle, null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(18.dp))
                            Text("$sessionScore/$sessionAnswered", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                    Surface(color = MaterialTheme.colorScheme.tertiaryContainer, shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(1f)) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Filled.LocalFireDepartment, null, tint = MaterialTheme.colorScheme.onTertiaryContainer, modifier = Modifier.size(18.dp))
                            Text("${quizProgress.currentStreak}d", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                    Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(1f)) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Filled.EmojiEvents, null, tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(18.dp))
                            Text("${quizProgress.accuracyPercent}%", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }

                if (roundComplete) {
                    val earnedXp = (sessionScore * 10) + 15
                    Card(
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxWidth().testTag("quiz_final_results_card")
                    ) {
                        Column(
                            modifier = Modifier.padding(22.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Filled.EmojiEvents, null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
                            Text(
                                text = if (isOm) "Qormaanni Xumurame!" else "Quiz Round Complete!",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isOm) "Deebii sirrii: $sessionScore / $ROUND_LENGTH (+${earnedXp} XP)" else "Score: $sessionScore / $ROUND_LENGTH (+$earnedXp XP)",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Button(
                                onClick = { startFreshRound() },
                                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("quiz_retry_button")
                            ) {
                                Icon(Icons.Filled.Refresh, null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isOm) "Irra Deebi'ii Qorami" else "Try Another Round")
                            }
                        }
                    }
                } else {
                    // Prompt Card
                    Card(
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.28f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = if (isOm) question.instructionOm else question.instructionEn,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            if (!question.isListeningOnly) {
                                Text(
                                    text = question.prompt,
                                    style = if (question.isQubeeQuestion) MaterialTheme.typography.displaySmall else MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = question.subtitle,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            FilledTonalButton(
                                onClick = { onSpeakText(question.speakText) },
                                modifier = Modifier.height(48.dp).testTag("quiz_hear_button")
                            ) {
                                Icon(Icons.AutoMirrored.Filled.VolumeUp, null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isOm) "Sagalee Dhaggeeffadhu" else "Hear Pronunciation")
                            }
                        }
                    }

                    if (question.isMatching) {
                        val rightSide = remember(question.matchingPairs) {
                            question.matchingPairs.map { it.second }.shuffled()
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                question.matchingPairs.forEach { (om, _) ->
                                    val isMatched = om in matchedLefts
                                    val isSelected = selectedLeft == om
                                    OutlinedButton(
                                        onClick = { if (!isMatched) { selectedLeft = om; matchingError = false } },
                                        enabled = !isMatched,
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            containerColor = when {
                                                isMatched -> QuizCorrectContainerLight
                                                isSelected -> MaterialTheme.colorScheme.primaryContainer
                                                else -> MaterialTheme.colorScheme.surface
                                            }
                                        ),
                                        modifier = Modifier.fillMaxWidth().minimumInteractiveComponentSize()
                                    ) {
                                        Text(om)
                                    }
                                }
                            }
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                rightSide.forEach { en ->
                                    val alreadyMatched = question.matchingPairs.any { it.second == en && it.first in matchedLefts }
                                    OutlinedButton(
                                        onClick = {
                                            val left = selectedLeft
                                            if (left != null && !alreadyMatched) {
                                                val expected = question.matchingPairs.firstOrNull { it.first == left }?.second
                                                if (expected == en) {
                                                    val nextSet = matchedLefts + left
                                                    matchedLefts = nextSet
                                                    selectedLeft = null
                                                    matchingError = false
                                                    if (nextSet.size == question.matchingPairs.size) {
                                                        submitAnswer("ALL_MATCHED")
                                                    }
                                                } else {
                                                    matchingError = true
                                                    selectedLeft = null
                                                }
                                            }
                                        },
                                        enabled = !alreadyMatched,
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            containerColor = if (alreadyMatched) QuizCorrectContainerLight else MaterialTheme.colorScheme.surface
                                        ),
                                        modifier = Modifier.fillMaxWidth().minimumInteractiveComponentSize()
                                    ) {
                                        Text(en)
                                    }
                                }
                            }
                        }
                        if (matchingError) {
                            Text(
                                text = if (isOm) "Wal hin fakkaanne, irra deebi'ii yaali!" else "Not a match — try another pair!",
                                color = QuizIncorrectRed,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    } else {
                        if (question.isFillBlank && selectedOption == null) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = fillText,
                                    onValueChange = { fillText = it },
                                    singleLine = true,
                                    label = { Text(if (isOm) "Jecha barreessi..." else "Type answer...") },
                                    modifier = Modifier.weight(1f)
                                )
                                Button(
                                    onClick = { if (fillText.isNotBlank()) submitAnswer(fillText) },
                                    enabled = fillText.isNotBlank(),
                                    modifier = Modifier.height(52.dp)
                                ) {
                                    Text(if (isOm) "Ergi" else "Check")
                                }
                            }
                        }

                        // Multiple choice / Word chips (answer never shown before click)
                        question.options.forEachIndexed { idx, option ->
                            val isAnswered = selectedOption != null
                            val isCorrect = option.equals(question.correctAnswer, ignoreCase = true)
                            val isPicked = option.equals(selectedOption, ignoreCase = true)

                            val containerColor = when {
                                !isAnswered -> MaterialTheme.colorScheme.surface
                                isCorrect -> QuizCorrectContainerLight
                                isPicked -> QuizIncorrectContainerLight
                                else -> MaterialTheme.colorScheme.surface
                            }
                            val borderColor = when {
                                !isAnswered -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                isCorrect -> QuizCorrectGreen
                                isPicked -> QuizIncorrectRed
                                else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                            }

                            OutlinedButton(
                                onClick = { submitAnswer(option) },
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(if (isAnswered && (isCorrect || isPicked)) 2.dp else 1.dp, borderColor),
                                colors = ButtonDefaults.outlinedButtonColors(containerColor = containerColor),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .minimumInteractiveComponentSize()
                                    .testTag("quiz_option_$idx")
                            ) {
                                Text(
                                    text = option,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                )
                            }
                        }
                    }

                    if (selectedOption != null) {
                        val gotIt = selectedOption.equals(question.correctAnswer, ignoreCase = true)
                        Surface(
                            color = if (gotIt) QuizCorrectContainerLight else QuizIncorrectContainerLight,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = if (gotIt) {
                                        if (isOm) "Baay'ee gaarii! +10 XP" else "Correct! +10 XP"
                                    } else {
                                        if (isOm) "Deebiin sirriin: ${question.correctAnswer}" else "Correct answer: ${question.correctAnswer}"
                                    },
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (gotIt) QuizCorrectGreen else QuizIncorrectRed
                                )
                                val exp = if (isOm) question.explanationOm else question.explanationEn
                                if (exp.isNotBlank()) {
                                    Text(exp, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }

                        Button(
                            onClick = { advanceOrFinish() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("quiz_next_button")
                        ) {
                            Text(
                                text = if (roundQuestionNum >= ROUND_LENGTH) {
                                    if (isOm) "Bu'aa Ilaali" else "See Final Results"
                                } else {
                                    if (isOm) "Gaaffii Itti Aanu" else "Next Question"
                                }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                if (quizAttempts.isNotEmpty()) {
                    Text(
                        text = if (isOm) "Seenaa Qormaata Dhiyeenyaa" else "Recent Quiz History",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                    quizAttempts.take(4).forEach { attempt ->
                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("${attempt.mode} (${attempt.score}/${attempt.totalQuestions})", style = MaterialTheme.typography.bodySmall)
                                Text("+${attempt.pointsEarned} XP", style = MaterialTheme.typography.labelMedium, color = QuizCorrectGreen)
                            }
                        }
                    }
                }
            }
        }
    }
}
