package com.example.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.OromoContent
import com.example.data.PhraseItem
import com.example.data.VocabularyItem

private enum class FlashcardDeck { VOCABULARY, QUBEE, PHRASES }

private data class StudyCard(
    val id: String,
    val front: String,
    val hint: String,
    val back: String,
    val example: String,
    val speakText: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardsModal(
    isOm: Boolean,
    allVocabulary: List<VocabularyItem>,
    allPhrases: List<PhraseItem>,
    onSpeakText: (String) -> Unit,
    onMarkCardMastered: (itemId: String, label: String) -> Unit,
    onDismiss: () -> Unit
) {
    var deck by remember { mutableStateOf(FlashcardDeck.VOCABULARY) }
    var cardIndex by remember { mutableIntStateOf(0) }
    var flipped by remember { mutableStateOf(false) }

    val cards = remember(deck, allVocabulary, allPhrases) {
        when (deck) {
            FlashcardDeck.VOCABULARY -> allVocabulary.shuffled().take(15).map {
                StudyCard(
                    id = it.id,
                    front = it.oromo,
                    hint = "/${it.pronunciation}/ • ${it.partOfSpeech}",
                    back = it.english,
                    example = "${it.exampleOm}\n${it.exampleEn}",
                    speakText = it.oromo
                )
            }
            FlashcardDeck.QUBEE -> OromoContent.qubeeLetters.shuffled().take(15).map {
                StudyCard(
                    id = it.name,
                    front = it.symbol,
                    hint = it.name,
                    back = "${it.exampleWord} (${it.exampleMeaningEn})",
                    example = if (isOm) it.soundGuideOm else it.soundGuideEn,
                    speakText = it.ttsPronunciation
                )
            }
            FlashcardDeck.PHRASES -> allPhrases.shuffled().take(15).map {
                StudyCard(
                    id = it.id,
                    front = it.oromo,
                    hint = it.pronunciationTip,
                    back = it.english,
                    example = phraseCategoryLabel(it.category, isOm),
                    speakText = it.oromo
                )
            }
        }
    }

    val current = cards[cardIndex.coerceIn(cards.indices)]

    fun moveNext() {
        flipped = false
        cardIndex = (cardIndex + 1) % cards.size
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
                .testTag("flashcards_modal"),
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                    title = {
                        Text(
                            text = if (isOm) "Kaardiiwwan Yaadannoo" else "Study Flashcards",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    actions = {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.minimumInteractiveComponentSize()
                        ) {
                            Icon(Icons.Filled.Close, contentDescription = if (isOm) "Cufi" else "Close flashcards")
                        }
                    }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = deck == FlashcardDeck.VOCABULARY,
                        onClick = { deck = FlashcardDeck.VOCABULARY; cardIndex = 0; flipped = false },
                        label = { Text(if (isOm) "Jechoota" else "Vocabulary") }
                    )
                    FilterChip(
                        selected = deck == FlashcardDeck.QUBEE,
                        onClick = { deck = FlashcardDeck.QUBEE; cardIndex = 0; flipped = false },
                        label = { Text("Qubee") }
                    )
                    FilterChip(
                        selected = deck == FlashcardDeck.PHRASES,
                        onClick = { deck = FlashcardDeck.PHRASES; cardIndex = 0; flipped = false },
                        label = { Text(if (isOm) "Haasawa" else "Phrases") }
                    )
                }

                LinearProgressIndicator(
                    progress = { (cardIndex + 1f) / cards.size.toFloat() },
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "${cardIndex + 1} / ${cards.size}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Card(
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = CardDefaults.cardColors(
                        containerColor = if (flipped) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                        } else {
                            MaterialTheme.colorScheme.surface
                        }
                    ),
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clickable { flipped = !flipped }
                        .testTag("flashcard_surface")
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AnimatedContent(targetState = flipped, label = "flashcard_flip") { isBack ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = if (isBack) {
                                        if (isOm) "HIIKA (Tap to flip)" else "MEANING (Tap to flip)"
                                    } else {
                                        if (isOm) "AFAAN OROMOO (Tuqi)" else "TAP TO REVEAL"
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = if (isBack) current.back else current.front,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = if (isBack) current.example else current.hint,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                                FilledTonalButton(
                                    onClick = { onSpeakText(current.speakText) },
                                    modifier = Modifier.height(48.dp)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.VolumeUp, null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (isOm) "Dhaggeeffadhu" else "Listen")
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { moveNext() },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    ) {
                        Icon(Icons.Filled.Refresh, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isOm) "Irra Deebi'i" else "Review Again")
                    }

                    Button(
                        onClick = {
                            onMarkCardMastered(current.id, current.front)
                            moveNext()
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("flashcard_mastered_button")
                    ) {
                        Icon(Icons.Filled.CheckCircle, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isOm) "Baradheera (+XP)" else "Got It (+XP)")
                    }
                }

                OutlinedButton(
                    onClick = { flipped = !flipped },
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Icon(Icons.Filled.Flip, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isOm) "Kaardii Garagalchi" else "Flip Card")
                }
            }
        }
    }
}
