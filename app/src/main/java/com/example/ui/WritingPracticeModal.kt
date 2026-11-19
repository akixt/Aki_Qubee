package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.OromoContent
import com.example.data.QubeeLetter
import com.example.data.VocabularyItem
import com.example.ui.theme.QuizCorrectGreen
import com.example.ui.theme.QuizIncorrectRed

private enum class WritingTab { LETTER_TRACE, WORD_SPELLING, SENTENCE_COMPLETE }

private data class SentenceExercise(
    val promptBefore: String,
    val promptAfter: String,
    val answer: String,
    val meaningEn: String
)

private val sentenceExercises = listOf(
    SentenceExercise("Akkam ", "? (How are you?)", "jirta", "How are you?"),
    SentenceExercise("Nagaa koo dha, ", ". (I am fine, thank you.)", "galatoomi", "Thank you"),
    SentenceExercise("Gatiin isaa ", "? (How much does it cost?)", "meeqa", "How much?"),
    SentenceExercise("Bishaan dhugaatii naaf ", ". (Bring me water.)", "fidi", "Bring"),
    SentenceExercise("Afaan ", " barachaan jira. (I am learning Afaan Oromo.)", "Oromoo", "Oromo")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WritingPracticeModal(
    isOm: Boolean,
    initialLetter: QubeeLetter = OromoContent.qubeeLetters.first(),
    allVocabulary: List<VocabularyItem> = OromoContent.vocabularySeed,
    onSpeakText: (String) -> Unit,
    onPracticeCompleted: (itemId: String, label: String) -> Unit,
    onDismiss: () -> Unit
) {
    var activeTab by remember { mutableStateOf(WritingTab.LETTER_TRACE) }
    var letterIndex by remember {
        mutableIntStateOf(OromoContent.qubeeLetters.indexOfFirst { it.symbol == initialLetter.symbol }.coerceAtLeast(0))
    }
    val currentLetter = OromoContent.qubeeLetters[letterIndex]
    val strokes = remember { mutableStateListOf<List<Offset>>() }
    var activeStroke by remember { mutableStateOf<List<Offset>>(emptyList()) }

    val spellingPool = remember(allVocabulary) { allVocabulary.filter { it.oromo.length in 3..10 }.shuffled().take(12) }
    var spellIdx by remember { mutableIntStateOf(0) }
    var spellInput by remember { mutableStateOf("") }
    var spellChecked by remember { mutableStateOf<Boolean?>(null) }

    var sentIdx by remember { mutableIntStateOf(0) }
    var sentInput by remember { mutableStateOf("") }
    var sentChecked by remember { mutableStateOf<Boolean?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            containerColor = MaterialTheme.colorScheme.background,
            modifier = Modifier
                .fillMaxSize()
                .testTag("writing_practice_modal"),
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                    title = {
                        Text(
                            text = if (isOm) "Shaakala Barreessuu" else "Writing Practice",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    actions = {
                        IconButton(onClick = onDismiss, modifier = Modifier.minimumInteractiveComponentSize()) {
                            Icon(Icons.Filled.Close, contentDescription = if (isOm) "Cufi" else "Close writing")
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = activeTab == WritingTab.LETTER_TRACE,
                        onClick = { activeTab = WritingTab.LETTER_TRACE },
                        label = { Text(if (isOm) "Qubee Kaasi" else "Trace Qubee") }
                    )
                    FilterChip(
                        selected = activeTab == WritingTab.WORD_SPELLING,
                        onClick = { activeTab = WritingTab.WORD_SPELLING },
                        label = { Text(if (isOm) "Qubeeffannoo" else "Spelling") }
                    )
                    FilterChip(
                        selected = activeTab == WritingTab.SENTENCE_COMPLETE,
                        onClick = { activeTab = WritingTab.SENTENCE_COMPLETE },
                        label = { Text(if (isOm) "Hima Guuti" else "Sentences") }
                    )
                }

                when (activeTab) {
                    WritingTab.LETTER_TRACE -> {
                        Card(
                            shape = MaterialTheme.shapes.large,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${currentLetter.symbol} (${currentLetter.name})",
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    FilledTonalButton(onClick = { onSpeakText(currentLetter.ttsPronunciation) }) {
                                        Icon(Icons.AutoMirrored.Filled.VolumeUp, null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(currentLetter.exampleWord)
                                    }
                                }

                                val strokeColor = MaterialTheme.colorScheme.primary
                                val guideColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(220.dp)
                                        .background(
                                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                            RoundedCornerShape(16.dp)
                                        )
                                        .pointerInput(currentLetter) {
                                            detectDragGestures(
                                                onDragStart = { start -> activeStroke = listOf(start) },
                                                onDrag = { change, _ ->
                                                    change.consume()
                                                    activeStroke = activeStroke + change.position
                                                },
                                                onDragEnd = {
                                                    if (activeStroke.isNotEmpty()) {
                                                        strokes.add(activeStroke)
                                                        activeStroke = emptyList()
                                                    }
                                                }
                                            )
                                        }
                                        .testTag("writing_trace_canvas"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = currentLetter.symbol,
                                        style = MaterialTheme.typography.displayLarge,
                                        color = guideColor,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        val allPaths = strokes + listOf(activeStroke)
                                        allPaths.forEach { pts ->
                                            if (pts.size >= 2) {
                                                val path = Path().apply {
                                                    moveTo(pts.first().x, pts.first().y)
                                                    for (i in 1 until pts.size) {
                                                        lineTo(pts[i].x, pts[i].y)
                                                    }
                                                }
                                                drawPath(
                                                    path = path,
                                                    color = strokeColor,
                                                    style = Stroke(width = 14f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                                                )
                                            }
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { strokes.clear(); activeStroke = emptyList() },
                                        modifier = Modifier.weight(1f).height(48.dp)
                                    ) {
                                        Icon(Icons.Filled.DeleteOutline, null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(if (isOm) "Haqi" else "Clear")
                                    }
                                    Button(
                                        onClick = {
                                            onPracticeCompleted("trace_${currentLetter.name}", "Traced ${currentLetter.symbol}")
                                            strokes.clear()
                                            letterIndex = (letterIndex + 1) % OromoContent.qubeeLetters.size
                                        },
                                        modifier = Modifier.weight(1f).height(48.dp)
                                    ) {
                                        Icon(Icons.Filled.CheckCircle, null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(if (isOm) "Xumuri (+XP)" else "Next Letter (+XP)")
                                    }
                                }
                            }
                        }
                    }

                    WritingTab.WORD_SPELLING -> {
                        val word = spellingPool[spellIdx.coerceIn(spellingPool.indices)]
                        Card(
                            shape = MaterialTheme.shapes.large,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = if (isOm) "Jecha Afaan Oromoo kana barreessi:" else "Spell this Afaan Oromoo word:",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = word.english,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                FilledTonalButton(onClick = { onSpeakText(word.oromo) }) {
                                    Icon(Icons.AutoMirrored.Filled.VolumeUp, null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (isOm) "Dhaggeeffadhu" else "Hear Word")
                                }
                                OutlinedTextField(
                                    value = spellInput,
                                    onValueChange = { spellInput = it; spellChecked = null },
                                    singleLine = true,
                                    label = { Text(if (isOm) "Afaan Oromootiin barreessi" else "Type in Afaan Oromoo") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                spellChecked?.let { ok ->
                                    Text(
                                        text = if (ok) {
                                            if (isOm) "Sirriidha! +10 XP" else "Correct spelling! +10 XP"
                                        } else {
                                            if (isOm) "Qubee sirriin: ${word.oromo}" else "Correct spelling: ${word.oromo}"
                                        },
                                        color = if (ok) QuizCorrectGreen else QuizIncorrectRed,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Button(
                                        onClick = {
                                            val ok = spellInput.trim().equals(word.oromo, ignoreCase = true)
                                            spellChecked = ok
                                            if (ok) {
                                                onPracticeCompleted("spell_${word.id}", "Spelled ${word.oromo}")
                                            }
                                        },
                                        modifier = Modifier.weight(1f).height(48.dp)
                                    ) {
                                        Text(if (isOm) "Mirkaneessi" else "Check")
                                    }
                                    OutlinedButton(
                                        onClick = {
                                            spellIdx = (spellIdx + 1) % spellingPool.size
                                            spellInput = ""
                                            spellChecked = null
                                        },
                                        modifier = Modifier.weight(1f).height(48.dp)
                                    ) {
                                        Text(if (isOm) "Itti Aanu" else "Next")
                                    }
                                }
                            }
                        }
                    }

                    WritingTab.SENTENCE_COMPLETE -> {
                        val ex = sentenceExercises[sentIdx.coerceIn(sentenceExercises.indices)]
                        Card(
                            shape = MaterialTheme.shapes.large,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = if (isOm) "Bakka duwwaa guuti:" else "Fill in the missing Oromo word:",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "${ex.promptBefore}____${ex.promptAfter}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                OutlinedTextField(
                                    value = sentInput,
                                    onValueChange = { sentInput = it; sentChecked = null },
                                    singleLine = true,
                                    label = { Text(if (isOm) "Jecha hir'ate" else "Missing word") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                sentChecked?.let { ok ->
                                    Text(
                                        text = if (ok) {
                                            if (isOm) "Baay'ee gaarii! +10 XP" else "Well done! +10 XP"
                                        } else {
                                            if (isOm) "Deebiin sirriin: ${ex.answer}" else "Expected word: ${ex.answer}"
                                        },
                                        color = if (ok) QuizCorrectGreen else QuizIncorrectRed,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Button(
                                        onClick = {
                                            val ok = sentInput.trim().equals(ex.answer, ignoreCase = true)
                                            sentChecked = ok
                                            if (ok) {
                                                onPracticeCompleted("sent_$sentIdx", "Completed sentence")
                                            }
                                        },
                                        modifier = Modifier.weight(1f).height(48.dp)
                                    ) {
                                        Text(if (isOm) "Mirkaneessi" else "Check")
                                    }
                                    OutlinedButton(
                                        onClick = {
                                            sentIdx = (sentIdx + 1) % sentenceExercises.size
                                            sentInput = ""
                                            sentChecked = null
                                        },
                                        modifier = Modifier.weight(1f).height(48.dp)
                                    ) {
                                        Text(if (isOm) "Itti Aanu" else "Next")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
