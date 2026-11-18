package com.example.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.ProverbCategory
import com.example.data.ProverbItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.IconButtonDefaults

@Composable
fun ProverbOfTheDayCard(
    proverb: ProverbItem,
    proverbNumber: Int,
    totalCount: Int,
    isFavorite: Boolean,
    isOm: Boolean,
    onToggleFavorite: () -> Unit,
    onRandomProverb: () -> Unit,
    onNextProverb: () -> Unit,
    onSpeakProverb: (String) -> Unit,
    onCopyProverb: (String) -> Unit,
    onShareProverb: (ProverbItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val todayLabel = remember {
        SimpleDateFormat("MMM d", Locale.getDefault()).format(Date())
    }

    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.28f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("proverb_of_day_card")
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.FormatQuote,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Column {
                        Text(
                            text = if (isOm) "Mammaaksa Guyyaa" else "Proverbs of the Day",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "$todayLabel • #$proverbNumber / $totalCount",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                FilledTonalIconButton(
                    onClick = onToggleFavorite,
                    colors = if (isFavorite) {
                        IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.secondary
                        )
                    } else {
                        IconButtonDefaults.filledTonalIconButtonColors()
                    },
                    modifier = Modifier.testTag("favorite_featured_proverb_button")
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = if (isOm) {
                            if (isFavorite) "Filatamoo keessaa haqi" else "Filatamootti dabali"
                        } else {
                            if (isFavorite) "Remove proverb from favorites" else "Save proverb to favorites"
                        }
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = proverbCategoryLabel(proverb.category, isOm),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    FilledTonalIconButton(
                        onClick = { onSpeakProverb(proverb.oromo) },
                        modifier = Modifier.testTag("speak_proverb_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = if (isOm) "Dhaggeeffadhu" else "Listen to proverb"
                        )
                    }
                    FilledTonalIconButton(
                        onClick = {
                            onCopyProverb("${proverb.oromo}\n— ${proverb.english}")
                        },
                        modifier = Modifier.testTag("copy_proverb_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ContentCopy,
                            contentDescription = if (isOm) "Garagalchi" else "Copy proverb"
                        )
                    }
                    FilledTonalIconButton(
                        onClick = { onShareProverb(proverb) },
                        modifier = Modifier.testTag("share_proverb_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = if (isOm) "Qoodi" else "Share proverb"
                        )
                    }
                }
            }

            AnimatedContent(
                targetState = proverb,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "proverb_of_day_transition"
            ) { item ->
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "“${item.oromo}”",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.testTag("featured_proverb_oromo")
                    )
                    Text(
                        text = item.english,
                        style = MaterialTheme.typography.bodyLarge,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.testTag("featured_proverb_english")
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = if (isOm) "Hiika" else "Meaning",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = if (isOm) item.meaningOm else item.meaningEn,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onRandomProverb,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("random_proverb_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Shuffle,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isOm) "Mammaaksa Random" else "Random Proverb",
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                FilledTonalButton(
                    onClick = onNextProverb,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("next_proverb_button")
                ) {
                    Text(
                        text = if (isOm) "Itti Aanu" else "Next",
                        style = MaterialTheme.typography.labelLarge
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ProverbArchiveControls(
    searchQuery: String,
    selectedCategory: ProverbCategory?,
    filteredCount: Int,
    totalCount: Int,
    isOm: Boolean,
    onSearchChange: (String) -> Unit,
    onSelectCategory: (ProverbCategory?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isOm) "Kuusaa Mammaaksaa" else "Oromo Proverbs Library",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(50)
            ) {
                Text(
                    text = if (filteredCount == totalCount) {
                        "$totalCount"
                    } else {
                        "$filteredCount / $totalCount"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            singleLine = true,
            shape = MaterialTheme.shapes.medium,
            leadingIcon = {
                Icon(imageVector = Icons.Filled.Search, contentDescription = null)
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = { onSearchChange("") },
                        modifier = Modifier.testTag("clear_proverb_search_button")
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
                        "Mammaaksa $totalCount keessaa barbaadi..."
                    } else {
                        "Search $totalCount proverbs in Oromo or English..."
                    }
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("proverb_search_input")
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedCategory == null,
                onClick = { onSelectCategory(null) },
                label = { Text(if (isOm) "Hunda ($totalCount)" else "All ($totalCount)") },
                modifier = Modifier.testTag("proverb_filter_all")
            )
            ProverbCategory.entries.forEach { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { onSelectCategory(category) },
                    label = { Text(proverbCategoryLabel(category, isOm)) },
                    modifier = Modifier.testTag("proverb_filter_${category.name.lowercase()}")
                )
            }
        }
    }
}

@Composable
fun ProverbArchiveItemCard(
    item: ProverbItem,
    displayIndex: Int,
    isSelected: Boolean,
    isFavorite: Boolean,
    isOm: Boolean,
    onToggleFavorite: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
            }
        ),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("proverb_item_${item.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "$displayIndex",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = item.oromo,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = item.english,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = proverbCategoryLabel(item.category, isOm),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier.testTag("favorite_proverb_${item.id}")
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = if (isOm) {
                        if (isFavorite) "Filatamoo keessaa haqi" else "Filatamootti dabali"
                    } else {
                        if (isFavorite) "Remove proverb from favorites" else "Bookmark proverb"
                    },
                    tint = if (isFavorite) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

fun proverbCategoryLabel(category: ProverbCategory, isOm: Boolean): String {
    return when (category) {
        ProverbCategory.WISDOM -> if (isOm) "Ogummaa" else "Wisdom"
        ProverbCategory.UNITY -> if (isOm) "Tokkummaa" else "Unity"
        ProverbCategory.PATIENCE -> if (isOm) "Obsa" else "Patience"
        ProverbCategory.TRUTH -> if (isOm) "Dhugaa" else "Truth"
        ProverbCategory.WORK -> if (isOm) "Hojii" else "Hard Work"
        ProverbCategory.NATURE -> if (isOm) "Uumama" else "Nature"
        ProverbCategory.COMMUNITY -> if (isOm) "Hawaasa" else "Community"
        ProverbCategory.CHARACTER -> if (isOm) "Amala Gaarii" else "Character"
    }
}
