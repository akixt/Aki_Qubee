package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
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
import com.example.data.VocabularyItem
import com.example.ui.theme.QuizCorrectContainerLight
import com.example.ui.theme.QuizCorrectGreen
import com.example.ui.theme.QuizIncorrectContainerLight
import com.example.ui.theme.QuizIncorrectRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListeningPracticeModal(
    isOm: Boolean,
    allVocabulary: List<VocabularyItem>,
    onSpeakText: (String) -> Unit,
    onCompletedSession: (score: Int, total: Int) -> Unit,
    onDismiss: () -> Unit
) {
    val roundItems = remember(allVocabulary) { allVocabulary.shuffled().take(5) }
    var index by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var selected by remember { mutableStateOf<String?>(null) }
    var finished by remember { mutableStateOf(false) }

    val target = roundItems[index.coerceIn(roundItems.indices)]
    val options = remember(index, roundItems) {
        val distractors = allVocabulary
            .asSequence()
            .filter { it.id != target.id && it.english != target.english }
            .shuffled()
            .take(3)
            .map { it.english }
            .toList()
        (distractors + target.english).shuffled()
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
                .testTag("listening_practice_modal"),
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                    title = {
                        Text(
                            text = if (isOm) "Shaakala Dhaggeeffannaa" else "Listening Comprehension",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    actions = {
                        IconButton(onClick = onDismiss, modifier = Modifier.minimumInteractiveComponentSize()) {
                            Icon(Icons.Filled.Close, contentDescription = if (isOm) "Cufi" else "Close listening")
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
                if (finished) {
                    Card(
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Filled.Headphones, null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
                            Text(
                                text = if (isOm) "Shaakalli Xumurame!" else "Listening Session Complete!",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isOm) "Qabxii: $score / ${roundItems.size}" else "Score: $score / ${roundItems.size}",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Button(
                                onClick = {
                                    index = 0; score = 0; selected = null; finished = false
                                },
                                modifier = Modifier.fillMaxWidth().height(48.dp)
                            ) {
                                Icon(Icons.Filled.Refresh, null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isOm) "Irra Deebi'i" else "Practice Again")
                            }
                        }
                    }
                } else {
                    LinearProgressIndicator(
                        progress = { (index + 1f) / roundItems.size.toFloat() },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = if (isOm) "Gaaffii ${index + 1} / ${roundItems.size}" else "Question ${index + 1} of ${roundItems.size}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Card(
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = if (isOm) {
                                    "Jecha armaan gadii dhaggeeffadhuu hiika sirrii fili:"
                                } else {
                                    "Listen to the Afaan Oromoo word and pick its meaning:"
                                },
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Button(
                                onClick = { onSpeakText(target.oromo) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("listening_play_audio_button")
                            ) {
                                Icon(Icons.AutoMirrored.Filled.VolumeUp, null, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (isOm) "Sagalee Taphachiisi" else "Play Word Audio")
                            }
                            if (selected != null) {
                                Text(
                                    text = "${target.oromo} — /${target.pronunciation}/",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Text(
                                    text = if (isOm) "Gargaarsa sagalee: /${target.pronunciation}/" else "Phonetic hint: /${target.pronunciation}/",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    options.forEachIndexed { idx, opt ->
                        val isAnswered = selected != null
                        val isCorrect = opt == target.english
                        val isPicked = opt == selected
                        val bg = when {
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
                            onClick = {
                                if (selected == null) {
                                    selected = opt
                                    if (opt == target.english) score++
                                }
                            },
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(if (isAnswered && (isCorrect || isPicked)) 2.dp else 1.dp, borderColor),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = bg),
                            modifier = Modifier
                                .fillMaxWidth()
                                .minimumInteractiveComponentSize()
                                .testTag("listening_option_$idx")
                        ) {
                            Text(
                                text = opt,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            )
                        }
                    }

                    if (selected != null) {
                        FilledTonalButton(
                            onClick = {
                                if (index + 1 < roundItems.size) {
                                    index++
                                    selected = null
                                } else {
                                    onCompletedSession(score, roundItems.size)
                                    finished = true
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Text(if (index + 1 < roundItems.size) (if (isOm) "Itti Aanu" else "Next") else (if (isOm) "Xumuri (+XP)" else "Finish (+XP)"))
                        }
                    }
                }
            }
        }
    }
}
