package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.AppLanguage
import com.example.data.PhraseCategory

@Composable
fun PhrasesScreen(
    uiState: QubeeUiState,
    onSearchChange: (String) -> Unit,
    onSelectCategory: (PhraseCategory?) -> Unit,
    onCopyPhrase: (String) -> Unit,
    onSpeakPhrase: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isOm = uiState.language == AppLanguage.OROMO

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        OutlinedTextField(
            value = uiState.phraseSearchQuery,
            onValueChange = onSearchChange,
            singleLine = true,
            shape = MaterialTheme.shapes.medium,
            leadingIcon = {
                Icon(imageVector = Icons.Filled.Search, contentDescription = null)
            },
            trailingIcon = {
                if (uiState.phraseSearchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = { onSearchChange("") },
                        modifier = Modifier.testTag("clear_search_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Clear,
                            contentDescription = if (isOm) "Haqi" else "Clear search"
                        )
                    }
                }
            },
            placeholder = {
                Text(
                    text = if (isOm) {
                        "Jechoota Afaan Oromoo ykn Ingiliffaan barbaadi..."
                    } else {
                        "Search phrases in Oromo or English..."
                    }
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("phrase_search_input")
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = uiState.phraseFilter == null,
                onClick = { onSelectCategory(null) },
                label = { Text(if (isOm) "Hunda" else "All") },
                modifier = Modifier.testTag("phrase_filter_all")
            )
            PhraseCategory.entries.forEach { category ->
                FilterChip(
                    selected = uiState.phraseFilter == category,
                    onClick = { onSelectCategory(category) },
                    label = { Text(categoryLabel(category, isOm)) },
                    modifier = Modifier.testTag("phrase_filter_${category.name.lowercase()}")
                )
            }
        }

        if (uiState.filteredPhrases.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.SearchOff,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(56.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = if (isOm) "Haasawni barbaadde hin argamne." else "No matching phrases found.",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isOm) "Jecha biraa yaali ykn gita jijjiiri." else "Try another keyword or clear your filter.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(uiState.filteredPhrases, key = { it.id }) { phrase ->
                    Card(
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = MaterialTheme.colorScheme.tertiaryContainer,
                                    shape = MaterialTheme.shapes.small
                                ) {
                                    Text(
                                        text = categoryLabel(phrase.category, isOm),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    FilledTonalIconButton(
                                        onClick = { onSpeakPhrase(phrase.oromo) },
                                        modifier = Modifier.testTag("speak_phrase_${phrase.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                            contentDescription = if (isOm) "Dhaggeeffadhu" else "Speak phrase"
                                        )
                                    }
                                    FilledTonalIconButton(
                                        onClick = { onCopyPhrase("${phrase.oromo} — ${phrase.english}") },
                                        modifier = Modifier.testTag("copy_phrase_${phrase.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.ContentCopy,
                                            contentDescription = if (isOm) "Garagalchi" else "Copy phrase"
                                        )
                                    }
                                }
                            }

                            Text(
                                text = phrase.oromo,
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = phrase.english,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isOm) {
                                    "Dubbisa: ${phrase.pronunciationTip}"
                                } else {
                                    "Pronunciation: ${phrase.pronunciationTip}"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun categoryLabel(category: PhraseCategory, isOm: Boolean): String {
    return when (category) {
        PhraseCategory.GREETINGS -> if (isOm) "Nagaa" else "Greetings"
        PhraseCategory.ESSENTIALS -> if (isOm) "Barbaachisoo" else "Essentials"
        PhraseCategory.TRAVEL -> if (isOm) "Imala" else "Travel"
        PhraseCategory.MARKET -> if (isOm) "Gabaa" else "Market"
        PhraseCategory.FOOD -> if (isOm) "Nyaata" else "Food"
    }
}
