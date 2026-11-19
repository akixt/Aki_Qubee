package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.AppLanguage
import com.example.data.OromoContent
import com.example.data.QubeeCategory
import com.example.data.QubeeLetter

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QubeeAlphabetScreen(
    uiState: QubeeUiState,
    onSelectFilter: (QubeeCategory?) -> Unit,
    onSelectLetter: (QubeeLetter) -> Unit,
    onSpeakWord: (String) -> Unit,
    onOpenFullChart: () -> Unit,
    modifier: Modifier = Modifier,
    onToggleMastered: (QubeeLetter) -> Unit = {},
    onPracticeWriting: (QubeeLetter) -> Unit = {},
    onPracticeSpeaking: (QubeeLetter) -> Unit = {},
    onOpenQubeeQuiz: () -> Unit = {}
) {
    val isOm = uiState.language == AppLanguage.OROMO
    val selected = uiState.selectedLetter
    val isSelectedMastered = "QUBEE_${selected.name}" in uiState.masteredItemKeys
    val masteredCount = uiState.masteredQubeeCount

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Spacer(modifier = Modifier.height(2.dp))

        // Qubee Mastery Progress Strip
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = MaterialTheme.shapes.medium,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isOm) {
                            "Dandeettii Qubee: $masteredCount / ${OromoContent.qubeeLetters.size}"
                        } else {
                            "Qubee Mastery: $masteredCount / ${OromoContent.qubeeLetters.size} letters"
                        },
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${((masteredCount * 100f) / OromoContent.qubeeLetters.size).toInt()}%",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold
                    )
                }
                LinearProgressIndicator(
                    progress = { (masteredCount.toFloat() / OromoContent.qubeeLetters.size).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                )
            }
        }

        // Spotlight Detail Card for Selected Qubee Letter
        Card(
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(72.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = selected.symbol.substringBefore(" "),
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = selected.symbol.substringAfter(" ", ""),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${selected.name} • ${selected.exampleWord}",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            FilterChip(
                                selected = isSelectedMastered,
                                onClick = { onToggleMastered(selected) },
                                leadingIcon = if (isSelectedMastered) {
                                    {
                                        Icon(
                                            imageVector = Icons.Filled.CheckCircle,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                } else null,
                                label = {
                                    Text(
                                        text = if (isSelectedMastered) {
                                            if (isOm) "Baratame" else "Mastered"
                                        } else {
                                            if (isOm) "+10 XP" else "Mark Learned"
                                        },
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                modifier = Modifier.testTag("qubee_master_chip")
                            )
                        }
                        Text(
                            text = "${selected.exampleWord} = ${selected.exampleMeaningEn}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = if (isOm) selected.soundGuideOm else selected.soundGuideEn,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                        )
                    }
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledTonalButton(
                        onClick = { onSpeakWord(selected.ttsPronunciation) },
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("speak_qubee_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.VolumeUp, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isOm) "Dhaggeeffadhu" else "Listen")
                    }

                    OutlinedButton(
                        onClick = { onPracticeSpeaking(selected) },
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("speak_compare_qubee_button")
                    ) {
                        Icon(Icons.Filled.Mic, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isOm) "Shaakali (AI)" else "Speak & Compare")
                    }

                    OutlinedButton(
                        onClick = { onPracticeWriting(selected) },
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("write_qubee_button")
                    ) {
                        Icon(Icons.Filled.Draw, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isOm) "Barreessi" else "Write")
                    }

                    OutlinedButton(
                        onClick = onOpenFullChart,
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("open_full_chart_button")
                    ) {
                        Icon(Icons.Filled.GridView, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isOm) "Gabatee Guutuu" else "Full Chart")
                    }

                    OutlinedButton(
                        onClick = onOpenQubeeQuiz,
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("qubee_quiz_shortcut_button")
                    ) {
                        Icon(Icons.Filled.Quiz, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isOm) "Qormaata" else "Quiz")
                    }
                }
            }
        }

        // Category Filter Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = uiState.qubeeFilter == null,
                onClick = { onSelectFilter(null) },
                label = { Text(if (isOm) "Hunda (33)" else "All (33)") },
                modifier = Modifier.testTag("qubee_filter_all")
            )
            FilterChip(
                selected = uiState.qubeeFilter == QubeeCategory.VOWEL,
                onClick = { onSelectFilter(QubeeCategory.VOWEL) },
                label = { Text(if (isOm) "Dubbachiiftuu (5)" else "Vowels (5)") },
                modifier = Modifier.testTag("qubee_filter_vowels")
            )
            FilterChip(
                selected = uiState.qubeeFilter == QubeeCategory.CONSONANT,
                onClick = { onSelectFilter(QubeeCategory.CONSONANT) },
                label = { Text(if (isOm) "Dubbifamaa (21)" else "Consonants (21)") },
                modifier = Modifier.testTag("qubee_filter_consonants")
            )
            FilterChip(
                selected = uiState.qubeeFilter == QubeeCategory.DIGRAPH,
                onClick = { onSelectFilter(QubeeCategory.DIGRAPH) },
                label = { Text(if (isOm) "Qubee Dachaa (7)" else "Digraphs (7)") },
                modifier = Modifier.testTag("qubee_filter_digraphs")
            )
        }

        // Grid of Qubee Tiles
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 96.dp),
            contentPadding = PaddingValues(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(uiState.filteredLetters, key = { it.symbol }) { letter ->
                val isSelected = letter == selected
                val isMastered = "QUBEE_${letter.name}" in uiState.masteredItemKeys
                Card(
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            isSelected -> MaterialTheme.colorScheme.secondaryContainer
                            isMastered -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                            else -> MaterialTheme.colorScheme.surface
                        }
                    ),
                    border = BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.secondary
                        } else {
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)
                        }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onSelectLetter(letter)
                            onSpeakWord(letter.ttsPronunciation)
                        }
                        .testTag("qubee_tile_${letter.name.lowercase()}")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = letter.symbol,
                                style = MaterialTheme.typography.titleLarge,
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.onSecondaryContainer
                                } else {
                                    MaterialTheme.colorScheme.primary
                                },
                                textAlign = TextAlign.Center
                            )
                            Icon(
                                imageVector = if (isMastered) Icons.Filled.CheckCircle else Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Pronounce ${letter.name}",
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Text(
                            text = letter.exampleWord,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
