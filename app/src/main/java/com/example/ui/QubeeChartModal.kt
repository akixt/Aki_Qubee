package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.OromoContent
import com.example.data.QubeeCategory
import com.example.data.QubeeLetter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QubeeChartModal(
    selectedLetter: QubeeLetter,
    isOm: Boolean,
    onLetterClick: (QubeeLetter) -> Unit,
    onReplaySound: (QubeeLetter) -> Unit,
    onDismiss: () -> Unit,
    onPracticeSpeaking: ((QubeeLetter) -> Unit)? = null
) {
    val vowels = OromoContent.qubeeLetters.filter { it.category == QubeeCategory.VOWEL }
    val consonants = OromoContent.qubeeLetters.filter { it.category == QubeeCategory.CONSONANT }
    val digraphs = OromoContent.qubeeLetters.filter { it.category == QubeeCategory.DIGRAPH }

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
                .testTag("qubee_chart_modal"),
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    ),
                    title = {
                        Column {
                            Text(
                                text = if (isOm) "Gabatee Qubee Guutuu" else "Interactive Qubee Chart",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = if (isOm) {
                                    "Sagalee dhaggeeffachuuf qubee kamiyyuu tuqi"
                                } else {
                                    "Tap any letter to hear TTS pronunciation"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("close_qubee_chart_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = if (isOm) "Cufi" else "Close chart"
                            )
                        }
                    }
                )
            },
            bottomBar = {
                Surface(
                    tonalElevation = 6.dp,
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier.size(54.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = selectedLetter.symbol.substringBefore(" "),
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = "${selectedLetter.name} • ${selectedLetter.exampleWord} (${selectedLetter.exampleMeaningEn})",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (isOm) selectedLetter.soundGuideOm else selectedLetter.soundGuideEn,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        if (onPracticeSpeaking != null) {
                            FilledTonalButton(
                                onClick = { onPracticeSpeaking(selectedLetter) },
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("chart_speak_compare_button")
                            ) {
                                Text(if (isOm) "Shaakali" else "Speak")
                            }
                        }

                        FilledTonalButton(
                            onClick = { onReplaySound(selectedLetter) },
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("chart_replay_sound_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isOm) "Dhaga'i" else "Play")
                        }
                    }
                }
            }
        ) { innerPadding ->
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 76.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .testTag("qubee_chart_grid")
            ) {
                item(span = { GridItemSpan(maxLineSpan) }, key = "header_vowels") {
                    ChartSectionHeader(
                        title = if (isOm) "Dubbachiiftuu (Vowels · 5)" else "Vowels · Dubbachiiftuu (5)",
                        badgeColor = MaterialTheme.colorScheme.primaryContainer,
                        textColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                items(vowels, key = { "chart_${it.symbol}" }) { letter ->
                    ChartLetterTile(
                        letter = letter,
                        isSelected = letter == selectedLetter,
                        onClick = { onLetterClick(letter) }
                    )
                }

                item(span = { GridItemSpan(maxLineSpan) }, key = "header_consonants") {
                    ChartSectionHeader(
                        title = if (isOm) "Dubbifamaa (Consonants · 21)" else "Consonants · Dubbifamaa (21)",
                        badgeColor = MaterialTheme.colorScheme.surfaceVariant,
                        textColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                items(consonants, key = { "chart_${it.symbol}" }) { letter ->
                    ChartLetterTile(
                        letter = letter,
                        isSelected = letter == selectedLetter,
                        onClick = { onLetterClick(letter) }
                    )
                }

                item(span = { GridItemSpan(maxLineSpan) }, key = "header_digraphs") {
                    ChartSectionHeader(
                        title = if (isOm) "Qubee Dachaa (Digraphs · 7)" else "Digraphs · Qubee Dachaa (7)",
                        badgeColor = MaterialTheme.colorScheme.tertiaryContainer,
                        textColor = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                items(digraphs, key = { "chart_${it.symbol}" }) { letter ->
                    ChartLetterTile(
                        letter = letter,
                        isSelected = letter == selectedLetter,
                        onClick = { onLetterClick(letter) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ChartSectionHeader(
    title: String,
    badgeColor: androidx.compose.ui.graphics.Color,
    textColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = badgeColor,
        shape = RoundedCornerShape(10.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = textColor,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun ChartLetterTile(
    letter: QubeeLetter,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val containerColor = when {
        isSelected -> MaterialTheme.colorScheme.secondaryContainer
        letter.category == QubeeCategory.VOWEL -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
        letter.category == QubeeCategory.DIGRAPH -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.45f)
        else -> MaterialTheme.colorScheme.surface
    }

    Surface(
        shape = MaterialTheme.shapes.medium,
        color = containerColor,
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
            .minimumInteractiveComponentSize()
            .clickable(onClick = onClick)
            .testTag("chart_letter_${letter.name.lowercase()}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = letter.symbol.substringBefore(" "),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onSecondaryContainer
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = "Pronounce ${letter.name}",
                    tint = if (isSelected) {
                        MaterialTheme.colorScheme.secondary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                    },
                    modifier = Modifier.size(14.dp)
                )
            }
            Text(
                text = letter.name,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                textAlign = TextAlign.Center
            )
        }
    }
}
