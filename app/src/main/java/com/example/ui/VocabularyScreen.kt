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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.AppLanguage
import com.example.data.VocabularyCategory
import com.example.data.VocabularyItem

private val vocabAlphabetIndex = listOf(
    "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L",
    "M", "N", "O", "P", "Q", "R", "S", "T", "U", "W", "X", "Y"
)

private val vocabSuggestions = listOf("Nagaa", "Buna", "Water", "Kitaaba", "Family", "Gaara")

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VocabularyScreen(
    uiState: QubeeUiState,
    onSearchChange: (String) -> Unit,
    onSelectCategory: (VocabularyCategory?) -> Unit,
    onSelectLetterPrefix: (String?) -> Unit,
    onToggleFavoriteVocab: (String) -> Unit,
    onToggleMasteredVocab: (VocabularyItem) -> Unit,
    onVocabViewed: (VocabularyItem) -> Unit,
    onSpeakText: (String) -> Unit,
    onPracticeSpeaking: (VocabularyItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val isOm = uiState.language == AppLanguage.OROMO
    val filtered = uiState.filteredVocabulary
    val recentlyViewed = remember(uiState.recentlyViewedVocab) { uiState.recentlyViewedVocab }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("vocabulary_screen"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Spacer(modifier = Modifier.height(2.dp))

        OutlinedTextField(
            value = uiState.vocabSearchQuery,
            onValueChange = onSearchChange,
            singleLine = true,
            shape = MaterialTheme.shapes.medium,
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            trailingIcon = {
                if (uiState.vocabSearchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = { onSearchChange("") },
                        modifier = Modifier.testTag("clear_vocab_search_button")
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
                    if (isOm) {
                        "Jechoota ${uiState.allVocabulary.size}+ OM ↔ EN barbaadi..."
                    } else {
                        "Search ${uiState.allVocabulary.size}+ Oromo ↔ English words..."
                    }
                )
            },
            supportingText = {
                Text(
                    if (isOm) {
                        "Bu'aa: ${filtered.size} / ${uiState.allVocabulary.size}"
                    } else {
                        "Showing ${filtered.size} of ${uiState.allVocabulary.size} entries"
                    }
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("vocab_search_input")
        )

        // Search Suggestions when query is empty
        if (uiState.vocabSearchQuery.isBlank() && uiState.vocabFilter == null && uiState.vocabLetterFilter == null) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(vocabSuggestions) { suggestion ->
                    SuggestionChip(
                        onClick = { onSearchChange(suggestion) },
                        label = { Text(suggestion) }
                    )
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
                selected = uiState.vocabFilter == null,
                onClick = { onSelectCategory(null) },
                label = { Text(if (isOm) "Hunda (${uiState.allVocabulary.size})" else "All (${uiState.allVocabulary.size})") },
                modifier = Modifier.testTag("vocab_filter_all")
            )
            VocabularyCategory.entries.forEach { category ->
                val count = uiState.allVocabulary.count { it.category == category }
                FilterChip(
                    selected = uiState.vocabFilter == category,
                    onClick = { onSelectCategory(category) },
                    label = { Text("${vocabCategoryLabel(category, isOm)} ($count)") },
                    modifier = Modifier.testTag("vocab_filter_${category.name.lowercase()}")
                )
            }
        }

        // A–Z Alphabetical Index Bar
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            item {
                FilterChip(
                    selected = uiState.vocabLetterFilter == null,
                    onClick = { onSelectLetterPrefix(null) },
                    label = { Text("A–Z") }
                )
            }
            items(vocabAlphabetIndex) { letter ->
                FilterChip(
                    selected = uiState.vocabLetterFilter == letter,
                    onClick = {
                        onSelectLetterPrefix(if (uiState.vocabLetterFilter == letter) null else letter)
                    },
                    label = { Text(letter) }
                )
            }
        }

        if (recentlyViewed.isNotEmpty() && uiState.vocabSearchQuery.isBlank()) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = if (isOm) "Dhiyeenya Kan Ilaalame" else "Recently Viewed Words",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(recentlyViewed, key = { "recent_vocab_${it.id}" }) { word ->
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                            shape = RoundedCornerShape(12.dp),
                            onClick = { onSpeakText(word.oromo) }
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                                Text(
                                    text = word.oromo,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = word.english,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        if (filtered.isEmpty()) {
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
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = if (isOm) "Jechi barbaadde hin argamne." else "No vocabulary words matched.",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = if (isOm) {
                        "Jecha akka 'Nagaa', 'Buna' ykn 'Water' jedhu yaali."
                    } else {
                        "Try searching 'Nagaa', 'Buna', or 'Water', or reset filters."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                FilledTonalButton(
                    onClick = {
                        onSearchChange("")
                        onSelectCategory(null)
                        onSelectLetterPrefix(null)
                    }
                ) {
                    Text(if (isOm) "Calaltuu Haqi" else "Clear Filters")
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .testTag("vocab_lazy_column"),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filtered, key = { it.id }) { item ->
                    val isFav = item.id in uiState.favoriteVocabIds
                    val isMastered = "VOCAB_${item.id}" in uiState.masteredItemKeys
                    Card(
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(
                            1.dp,
                            if (isMastered) {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
                            } else {
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                            }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onVocabViewed(item)
                                onSpeakText(item.oromo)
                            }
                            .testTag("vocab_card_${item.id}")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = RoundedCornerShape(50)
                                    ) {
                                        Text(
                                            text = vocabCategoryLabel(item.category, isOm),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                    Surface(
                                        color = MaterialTheme.colorScheme.tertiaryContainer,
                                        shape = RoundedCornerShape(50)
                                    ) {
                                        Text(
                                            text = item.partOfSpeech,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    FilledTonalIconButton(
                                        onClick = { onToggleFavoriteVocab(item.id) },
                                        colors = if (isFav) {
                                            IconButtonDefaults.filledTonalIconButtonColors(
                                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                                contentColor = MaterialTheme.colorScheme.secondary
                                            )
                                        } else {
                                            IconButtonDefaults.filledTonalIconButtonColors()
                                        },
                                        modifier = Modifier.testTag("favorite_vocab_${item.id}")
                                    ) {
                                        Icon(
                                            imageVector = if (isFav) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                            contentDescription = if (isOm) "Filadhu" else "Favorite word"
                                        )
                                    }
                                    FilledTonalIconButton(
                                        onClick = {
                                            onVocabViewed(item)
                                            onSpeakText("${item.oromo}. ${item.exampleOm}")
                                        },
                                        modifier = Modifier.testTag("speak_vocab_${item.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                            contentDescription = if (isOm) "Dhaggeeffadhu" else "Listen to word"
                                        )
                                    }
                                    FilledTonalIconButton(
                                        onClick = { onPracticeSpeaking(item) }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Mic,
                                            contentDescription = if (isOm) "Shaakali" else "Practice speaking"
                                        )
                                    }
                                    FilledTonalIconButton(
                                        onClick = { onToggleMasteredVocab(item) }
                                    ) {
                                        Icon(
                                            imageVector = if (isMastered) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircleOutline,
                                            contentDescription = if (isOm) "Baratame" else "Mark word learned",
                                            tint = if (isMastered) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            Text(
                                text = "${item.oromo} — ${item.english}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            if (item.pronunciation.isNotBlank()) {
                                Text(
                                    text = "/${item.pronunciation}/",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (item.exampleOm.isNotBlank()) {
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Text(
                                            text = item.exampleOm,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = item.exampleEn,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontStyle = FontStyle.Italic,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
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

fun vocabCategoryLabel(category: VocabularyCategory, isOm: Boolean): String = when (category) {
    VocabularyCategory.QUBEE_CORE -> if (isOm) "Qubee Bu'uuraa" else "Core Words"
    VocabularyCategory.NUMBERS_TIME -> if (isOm) "Lakkoofsa & Yeroo" else "Numbers & Time"
    VocabularyCategory.FAMILY_PEOPLE -> if (isOm) "Maatii & Namoota" else "Family & People"
    VocabularyCategory.FOOD_DRINK -> if (isOm) "Nyaata & Dhugaatii" else "Food & Drink"
    VocabularyCategory.NATURE_ANIMALS -> if (isOm) "Uumama & Bineensota" else "Nature & Animals"
    VocabularyCategory.BODY_HEALTH -> if (isOm) "Qaama & Fayyaa" else "Body & Health"
    VocabularyCategory.EDUCATION_TECH -> if (isOm) "Barnoota & Teeknoolojii" else "Education & Tech"
    VocabularyCategory.ACTIONS_VERBS -> if (isOm) "Gochima" else "Actions & Verbs"
    VocabularyCategory.ADJECTIVES_CULTURE -> if (isOm) "Ibsa & Aadaa" else "Culture & Adjectives"
}
