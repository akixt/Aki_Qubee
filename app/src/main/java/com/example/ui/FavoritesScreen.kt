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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.AppLanguage
import com.example.data.PhraseItem
import com.example.data.ProverbItem
import com.example.data.VocabularyItem

private enum class FavoriteTabFilter {
    ALL,
    PROVERBS,
    PHRASES,
    VOCABULARY
}

@Composable
fun FavoritesScreen(
    uiState: QubeeUiState,
    onToggleFavoriteProverb: (String) -> Unit,
    onToggleFavoritePhrase: (String) -> Unit,
    onSelectProverbInHome: (String) -> Unit,
    onSpeakText: (String) -> Unit,
    onCopyText: (String) -> Unit,
    onShareProverb: (ProverbItem) -> Unit,
    onExploreProverbs: () -> Unit,
    onExplorePhrases: () -> Unit,
    modifier: Modifier = Modifier,
    onToggleFavoriteVocab: (String) -> Unit = {},
    onExploreVocabulary: () -> Unit = {}
) {
    val isOm = uiState.language == AppLanguage.OROMO
    var filter by remember { mutableStateOf(FavoriteTabFilter.ALL) }
    var searchQuery by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(filter) {
        listState.scrollToItem(0)
    }

    val q = searchQuery.trim().lowercase()
    val savedProverbs = remember(uiState.favoriteProverbs, q) {
        uiState.favoriteProverbs.filter {
            q.isEmpty() || it.oromo.lowercase().contains(q) || it.english.lowercase().contains(q)
        }
    }
    val savedPhrases = remember(uiState.favoritePhrases, q) {
        uiState.favoritePhrases.filter {
            q.isEmpty() || it.oromo.lowercase().contains(q) || it.english.lowercase().contains(q)
        }
    }
    val savedVocab = remember(uiState.favoriteVocabulary, q) {
        uiState.favoriteVocabulary.filter {
            q.isEmpty() || it.oromo.lowercase().contains(q) || it.english.lowercase().contains(q)
        }
    }
    val totalSaved = uiState.favoriteProverbs.size + uiState.favoritePhrases.size + uiState.favoriteVocabulary.size

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("favorites_screen"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Spacer(modifier = Modifier.height(2.dp))

        // Summary Header
        Card(
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f)),
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
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = if (isOm) "Filatamoo Koo" else "My Favorites",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = if (isOm) {
                            "Mammaaksa ${uiState.favoriteProverbs.size} • Haasawa ${uiState.favoritePhrases.size} • Jechoota ${uiState.favoriteVocabulary.size}"
                        } else {
                            "${uiState.favoriteProverbs.size} proverbs • ${uiState.favoritePhrases.size} phrases • ${uiState.favoriteVocabulary.size} words"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.85f)
                    )
                }

                Surface(
                    color = MaterialTheme.colorScheme.secondary,
                    shape = RoundedCornerShape(50)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "$totalSaved",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSecondary
                        )
                    }
                }
            }
        }

        if (totalSaved > 0) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                leadingIcon = { Icon(Icons.Filled.Search, null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Clear")
                        }
                    }
                },
                placeholder = { Text(if (isOm) "Filatamoo keessaa barbaadi..." else "Filter your saved bookmarks...") },
                modifier = Modifier.fillMaxWidth().testTag("favorites_search_input")
            )
        }

        // Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = filter == FavoriteTabFilter.ALL,
                onClick = { filter = FavoriteTabFilter.ALL },
                label = { Text(if (isOm) "Hunda ($totalSaved)" else "All ($totalSaved)") },
                modifier = Modifier.testTag("favorites_filter_all")
            )
            FilterChip(
                selected = filter == FavoriteTabFilter.PROVERBS,
                onClick = { filter = FavoriteTabFilter.PROVERBS },
                label = { Text(if (isOm) "Mammaaksa (${uiState.favoriteProverbs.size})" else "Proverbs (${uiState.favoriteProverbs.size})") },
                modifier = Modifier.testTag("favorites_filter_proverbs")
            )
            FilterChip(
                selected = filter == FavoriteTabFilter.PHRASES,
                onClick = { filter = FavoriteTabFilter.PHRASES },
                label = { Text(if (isOm) "Haasawa (${uiState.favoritePhrases.size})" else "Phrases (${uiState.favoritePhrases.size})") },
                modifier = Modifier.testTag("favorites_filter_phrases")
            )
            FilterChip(
                selected = filter == FavoriteTabFilter.VOCABULARY,
                onClick = { filter = FavoriteTabFilter.VOCABULARY },
                label = { Text(if (isOm) "Jechoota (${uiState.favoriteVocabulary.size})" else "Words (${uiState.favoriteVocabulary.size})") },
                modifier = Modifier.testTag("favorites_filter_vocab")
            )
        }

        val showProverbs = (filter == FavoriteTabFilter.ALL || filter == FavoriteTabFilter.PROVERBS) && savedProverbs.isNotEmpty()
        val showPhrases = (filter == FavoriteTabFilter.ALL || filter == FavoriteTabFilter.PHRASES) && savedPhrases.isNotEmpty()
        val showVocab = (filter == FavoriteTabFilter.ALL || filter == FavoriteTabFilter.VOCABULARY) && savedVocab.isNotEmpty()

        if (!showProverbs && !showPhrases && !showVocab) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.FavoriteBorder,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(56.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = if (isOm) "Wanti filatame hin jiru." else "No favorites found here.",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (isOm) {
                        "Mammaaksa, haasawa ykn jechoota jaallattu mallattoo onnee tuquun asitti kuusi."
                    } else {
                        "Tap the heart button on any Proverb, Daily Phrase, or Vocabulary word to bookmark it here."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onExploreProverbs,
                        modifier = Modifier.testTag("favorites_explore_proverbs_button")
                    ) {
                        Text(if (isOm) "Mammaaksa" else "Proverbs")
                    }
                    FilledTonalButton(
                        onClick = onExplorePhrases,
                        modifier = Modifier.testTag("favorites_explore_phrases_button")
                    ) {
                        Text(if (isOm) "Haasawa" else "Phrases")
                    }
                    FilledTonalButton(onClick = onExploreVocabulary) {
                        Text(if (isOm) "Jechoota" else "Vocab")
                    }
                }
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .testTag("favorites_lazy_column"),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (showProverbs) {
                    item(key = "header_saved_proverbs") {
                        Text(
                            text = if (isOm) "Mammaaksa Filatamoo" else "Saved Proverbs",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    items(savedProverbs, key = { "fav_prov_${it.id}" }) { proverb ->
                        FavoriteProverbCard(
                            proverb = proverb,
                            isOm = isOm,
                            onRemoveFavorite = { onToggleFavoriteProverb(proverb.id) },
                            onSpotlight = { onSelectProverbInHome(proverb.id) },
                            onSpeak = { onSpeakText(proverb.oromo) },
                            onCopy = { onCopyText("${proverb.oromo}\n— ${proverb.english}") },
                            onShare = { onShareProverb(proverb) }
                        )
                    }
                }

                if (showPhrases) {
                    item(key = "header_saved_phrases") {
                        Text(
                            text = if (isOm) "Haasawa Filatamoo" else "Saved Daily Phrases",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                    items(savedPhrases, key = { "fav_phrase_${it.id}" }) { phrase ->
                        FavoritePhraseCard(
                            phrase = phrase,
                            isOm = isOm,
                            onRemoveFavorite = { onToggleFavoritePhrase(phrase.id) },
                            onSpeak = { onSpeakText(phrase.oromo) },
                            onCopy = { onCopyText("${phrase.oromo} — ${phrase.english}") }
                        )
                    }
                }

                if (showVocab) {
                    item(key = "header_saved_vocab") {
                        Text(
                            text = if (isOm) "Jechoota Filatamoo" else "Saved Vocabulary Words",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                    items(savedVocab, key = { "fav_vocab_${it.id}" }) { word ->
                        FavoriteVocabCard(
                            word = word,
                            isOm = isOm,
                            onRemoveFavorite = { onToggleFavoriteVocab(word.id) },
                            onSpeak = { onSpeakText(word.oromo) },
                            onCopy = { onCopyText("${word.oromo} — ${word.english}") }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FavoriteProverbCard(
    proverb: ProverbItem,
    isOm: Boolean,
    onRemoveFavorite: () -> Unit,
    onSpotlight: () -> Unit,
    onSpeak: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit
) {
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)),
        onClick = onSpotlight,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("favorite_proverb_card_${proverb.id}")
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(50)) {
                    Text(
                        text = proverbCategoryLabel(proverb.category, isOm),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
                FilledTonalIconButton(
                    onClick = onRemoveFavorite,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.secondary
                    ),
                    modifier = Modifier.testTag("unfavorite_proverb_${proverb.id}")
                ) {
                    Icon(Icons.Filled.Favorite, contentDescription = if (isOm) "Haqi" else "Remove from favorites")
                }
            }
            Text("“${proverb.oromo}”", style = MaterialTheme.typography.titleMedium)
            Text(proverb.english, style = MaterialTheme.typography.bodyMedium, fontStyle = FontStyle.Italic, color = MaterialTheme.colorScheme.secondary)
            Text(if (isOm) proverb.meaningOm else proverb.meaningEn, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    FilledTonalIconButton(onClick = onSpeak) { Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Listen") }
                    FilledTonalIconButton(onClick = onCopy) { Icon(Icons.Filled.ContentCopy, contentDescription = "Copy") }
                    FilledTonalIconButton(onClick = onShare) { Icon(Icons.Filled.Share, contentDescription = "Share") }
                }
            }
        }
    }
}

@Composable
private fun FavoritePhraseCard(
    phrase: PhraseItem,
    isOm: Boolean,
    onRemoveFavorite: () -> Unit,
    onSpeak: () -> Unit,
    onCopy: () -> Unit
) {
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)),
        modifier = Modifier.fillMaxWidth().testTag("favorite_phrase_card_${phrase.id}")
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Surface(color = MaterialTheme.colorScheme.tertiaryContainer, shape = RoundedCornerShape(50)) {
                    Text(
                        text = phraseCategoryLabel(phrase.category, isOm),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
                FilledTonalIconButton(
                    onClick = onRemoveFavorite,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.secondary
                    ),
                    modifier = Modifier.testTag("unfavorite_phrase_${phrase.id}")
                ) {
                    Icon(Icons.Filled.Favorite, contentDescription = "Remove from favorites")
                }
            }
            Text(phrase.oromo, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Text(phrase.english, style = MaterialTheme.typography.bodyLarge)
            Text(phrase.pronunciationTip, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    FilledTonalIconButton(onClick = onSpeak) { Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Speak") }
                    FilledTonalIconButton(onClick = onCopy) { Icon(Icons.Filled.ContentCopy, contentDescription = "Copy") }
                }
            }
        }
    }
}

@Composable
private fun FavoriteVocabCard(
    word: VocabularyItem,
    isOm: Boolean,
    onRemoveFavorite: () -> Unit,
    onSpeak: () -> Unit,
    onCopy: () -> Unit
) {
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)),
        modifier = Modifier.fillMaxWidth().testTag("favorite_vocab_card_${word.id}")
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(50)) {
                    Text(
                        text = vocabCategoryLabel(word.category, isOm),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
                FilledTonalIconButton(
                    onClick = onRemoveFavorite,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Icon(Icons.Filled.Favorite, contentDescription = "Remove word")
                }
            }
            Text("${word.oromo} — ${word.english}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Text(word.exampleOm, style = MaterialTheme.typography.bodyMedium)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    FilledTonalIconButton(onClick = onSpeak) { Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Listen") }
                    FilledTonalIconButton(onClick = onCopy) { Icon(Icons.Filled.ContentCopy, contentDescription = "Copy") }
                }
            }
        }
    }
}
