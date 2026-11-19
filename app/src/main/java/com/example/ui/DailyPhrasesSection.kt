package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.OromoContent
import com.example.data.PhraseCategory
import com.example.data.PhraseItem

import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.IconButtonDefaults

@Composable
fun DailyPhrasesHomeSection(
    isOm: Boolean,
    favoritePhraseIds: Set<String>,
    onToggleFavoritePhrase: (String) -> Unit,
    onSpeakPhrase: (String) -> Unit,
    onCopyPhrase: (String) -> Unit,
    onBrowseCategory: (PhraseCategory) -> Unit,
    onBrowseAllPhrases: () -> Unit,
    modifier: Modifier = Modifier,
    allPhrases: List<PhraseItem> = OromoContent.phrases
) {
    var activeCategory by remember { mutableStateOf(PhraseCategory.GREETINGS) }
    val previewPhrases = remember(activeCategory, allPhrases) {
        allPhrases.filter { it.category == activeCategory }.take(3)
    }

    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("daily_phrases_home_section")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isOm) "Haasawa Guyyaa" else "Daily Phrases",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isOm) {
                            "Haasawa guyyaa guyyaa akka haalaatti"
                        } else {
                            "Conversational Afaan Oromo by context"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                TextButton(
                    onClick = onBrowseAllPhrases,
                    modifier = Modifier.testTag("browse_all_phrases_button")
                ) {
                    Text(
                        text = if (isOm) "Hunda (${allPhrases.size})" else "All (${allPhrases.size})",
                        style = MaterialTheme.typography.labelLarge
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PhraseCategory.entries.forEach { category ->
                    FilterChip(
                        selected = activeCategory == category,
                        onClick = { activeCategory = category },
                        label = { Text(phraseCategoryLabel(category, isOm)) },
                        modifier = Modifier.testTag("home_phrase_chip_${category.name.lowercase()}")
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                previewPhrases.forEach { phrase ->
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("home_phrase_card_${phrase.id}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = phrase.oromo,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = phrase.english,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = phrase.pronunciationTip,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                val isFav = phrase.id in favoritePhraseIds
                                FilledTonalIconButton(
                                    onClick = { onToggleFavoritePhrase(phrase.id) },
                                    colors = if (isFav) {
                                        IconButtonDefaults.filledTonalIconButtonColors(
                                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                            contentColor = MaterialTheme.colorScheme.secondary
                                        )
                                    } else {
                                        IconButtonDefaults.filledTonalIconButtonColors()
                                    },
                                    modifier = Modifier.testTag("home_favorite_phrase_${phrase.id}")
                                ) {
                                    Icon(
                                        imageVector = if (isFav) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                        contentDescription = if (isOm) {
                                            if (isFav) "Filatamoo keessaa haqi" else "Filatamootti dabali"
                                        } else {
                                            if (isFav) "Remove phrase from favorites" else "Bookmark phrase"
                                        }
                                    )
                                }
                                FilledTonalIconButton(
                                    onClick = { onSpeakPhrase(phrase.oromo) },
                                    modifier = Modifier.testTag("home_speak_phrase_${phrase.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                        contentDescription = if (isOm) "Dhaggeeffadhu" else "Speak phrase"
                                    )
                                }
                                FilledTonalIconButton(
                                    onClick = { onCopyPhrase("${phrase.oromo} — ${phrase.english}") },
                                    modifier = Modifier.testTag("home_copy_phrase_${phrase.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.ContentCopy,
                                        contentDescription = if (isOm) "Garagalchi" else "Copy phrase"
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Surface(
                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp),
                onClick = { onBrowseCategory(activeCategory) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("open_category_phrases_button")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isOm) {
                            "Haasawa '${phraseCategoryLabel(activeCategory, true)}' hunda ilaali"
                        } else {
                            "Explore all '${phraseCategoryLabel(activeCategory, false)}' phrases"
                        },
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

fun phraseCategoryLabel(category: PhraseCategory, isOm: Boolean): String {
    return when (category) {
        PhraseCategory.GREETINGS -> if (isOm) "Nagaa" else "Greetings"
        PhraseCategory.INTRODUCTIONS -> if (isOm) "Walbarsiisuu" else "Introductions"
        PhraseCategory.FAMILY -> if (isOm) "Maatii fi Mana" else "Family & Home"
        PhraseCategory.EDUCATION -> if (isOm) "Barnoota" else "Education"
        PhraseCategory.MARKET -> if (isOm) "Gabaa fi Bittaa" else "Shopping & Market"
        PhraseCategory.TRANSPORTATION -> if (isOm) "Geejjiba" else "Transportation"
        PhraseCategory.FOOD -> if (isOm) "Nyaata, Dhugaatii fi Buna" else "Food, Drinks & Coffee"
        PhraseCategory.DAILY_ACTIVITIES -> if (isOm) "Hojii Guyyaa" else "Daily Activities"
        PhraseCategory.WORKPLACE -> if (isOm) "Bakka Hojii" else "Workplace"
        PhraseCategory.TRAVEL -> if (isOm) "Imala" else "Travel"
        PhraseCategory.ESSENTIALS -> if (isOm) "Fayyaa fi Barbaachisoo" else "Health & Essentials"
    }
}
