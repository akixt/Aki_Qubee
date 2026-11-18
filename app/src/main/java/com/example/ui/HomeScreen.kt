package com.example.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.Abc
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.AppLanguage
import com.example.data.ProverbCategory
import com.example.data.ProverbItem
import com.example.ui.theme.GoldAccent
import kotlinx.coroutines.launch
import java.util.Calendar

import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.OutlinedButton
import com.example.data.OromoContent
import com.example.data.PhraseCategory

@Composable
fun HomeScreen(
    uiState: QubeeUiState,
    onRandomProverb: () -> Unit,
    onNextProverb: () -> Unit,
    onSelectProverbById: (String) -> Unit,
    onToggleFavoriteProverb: (String) -> Unit,
    onToggleFavoritePhrase: (String) -> Unit,
    onProverbSearchChange: (String) -> Unit,
    onSelectProverbCategory: (ProverbCategory?) -> Unit,
    onSpeakProverb: (String) -> Unit,
    onCopyText: (String) -> Unit,
    onShareProverb: (ProverbItem) -> Unit,
    onNavigateToQubee: () -> Unit,
    onOpenQubeeChart: () -> Unit,
    onOpenPracticeQuiz: () -> Unit,
    onNavigateToFavorites: () -> Unit,
    onBrowsePhraseCategory: (PhraseCategory) -> Unit,
    onNavigateToPhrases: () -> Unit,
    modifier: Modifier = Modifier,
    onRecordQuizAnswer: (Boolean) -> Unit = {}
) {
    val isOm = uiState.language == AppLanguage.OROMO
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val greeting = remember(isOm) {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when {
            hour < 12 -> if (isOm) "Akkam bultan!" else "Good morning!"
            hour < 17 -> if (isOm) "Akkam ooltan!" else "Good afternoon!"
            else -> if (isOm) "Akkam galgaltan!" else "Good evening!"
        }
    }

    val filteredProverbs = uiState.filteredProverbs
    val featured = uiState.featuredProverb

    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .testTag("home_lazy_column"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Editorial Banner
        item(key = "hero_banner") {
            Card(
                shape = MaterialTheme.shapes.large,
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_hero_highlands),
                        contentDescription = stringResource(id = R.string.hero_banner_cd),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.15f),
                                        Color(0xFF0E2920).copy(alpha = 0.86f)
                                    )
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = GoldAccent.copy(alpha = 0.92f),
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    text = if (isOm) "Baga Nagaan Dhuftan" else "Afaan Oromo Companion",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color(0xFF1A1400),
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }

                            Image(
                                painter = painterResource(id = R.drawable.aki_logo),
                                contentDescription = stringResource(id = R.string.brand_logo_cd),
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .testTag("hero_brand_logo")
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = greeting,
                                style = MaterialTheme.typography.displaySmall,
                                color = Color.White
                            )
                            Text(
                                text = if (isOm) {
                                    "Qubee, haasawa ${uiState.allPhrases.size}+ fi mammaaksa ${uiState.allProverbs.size}+ bakka tokkotti."
                                } else {
                                    "Explore Qubee, ${uiState.allPhrases.size}+ daily phrases, and ${uiState.allProverbs.size}+ Oromo proverbs."
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
            }
        }

        // Proverbs of the Day Card (random Mammaaksa + English translation)
        item(key = "proverb_of_day") {
            ProverbOfTheDayCard(
                proverb = featured,
                proverbNumber = uiState.featuredProverbIndex + 1,
                totalCount = uiState.allProverbs.size,
                isFavorite = featured.id in uiState.favoriteProverbIds,
                isOm = isOm,
                onToggleFavorite = { onToggleFavoriteProverb(featured.id) },
                onRandomProverb = onRandomProverb,
                onNextProverb = onNextProverb,
                onSpeakProverb = onSpeakProverb,
                onCopyProverb = onCopyText,
                onShareProverb = onShareProverb
            )
        }

        // Quick Exploration Shortcuts (Qubee Alphabet + Interactive TTS Chart)
        item(key = "quick_shortcuts") {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickModuleCard(
                        title = if (isOm) "Qubee Baradhu" else "Learn Qubee",
                        subtitle = if (isOm) "Qubee 33 & sagalee" else "33 letters & sounds",
                        icon = Icons.Outlined.Abc,
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        onClick = onNavigateToQubee,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("quick_qubee_card")
                    )
                    QuickModuleCard(
                        title = if (isOm) "Gabatee Qubee" else "Qubee Chart",
                        subtitle = if (isOm) "Tuqii dhaggeeffadhu" else "Tap letters for TTS",
                        icon = Icons.Filled.GridView,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        onClick = onOpenQubeeChart,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("open_qubee_chart_button")
                    )
                }

                val favCount = uiState.favoriteProverbIds.size + uiState.favoritePhraseIds.size
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FilledTonalButton(
                        onClick = onOpenPracticeQuiz,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("home_practice_quiz_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Quiz,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.size(6.dp))
                        Text(if (isOm) "Qormaata Guyyaa" else "Daily Quiz")
                    }
                    OutlinedButton(
                        onClick = onNavigateToFavorites,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("home_favorites_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.size(6.dp))
                        Text(
                            if (isOm) "Filatamoo ($favCount)" else "Favorites ($favCount)"
                        )
                    }
                }
            }
        }

        // Daily Quiz Interactive Multiple-Choice Progress Card
        item(key = "daily_quiz_section") {
            DailyQuizHomeCard(
                isOm = isOm,
                allPhrases = uiState.allPhrases,
                quizProgress = uiState.quizProgress,
                onSpeakText = onSpeakProverb,
                onRecordAnswer = onRecordQuizAnswer,
                onOpenFullQuiz = onOpenPracticeQuiz
            )
        }

        // Daily Phrases Interactive Contextual Section
        item(key = "daily_phrases_section") {
            DailyPhrasesHomeSection(
                isOm = isOm,
                favoritePhraseIds = uiState.favoritePhraseIds,
                onToggleFavoritePhrase = onToggleFavoritePhrase,
                onSpeakPhrase = onSpeakProverb,
                onCopyPhrase = onCopyText,
                onBrowseCategory = onBrowsePhraseCategory,
                onBrowseAllPhrases = onNavigateToPhrases,
                allPhrases = uiState.allPhrases
            )
        }

        // Search and Category Filters for the 550+ Proverbs Library
        item(key = "proverb_archive_controls") {
            ProverbArchiveControls(
                searchQuery = uiState.proverbSearchQuery,
                selectedCategory = uiState.proverbFilter,
                filteredCount = filteredProverbs.size,
                totalCount = uiState.allProverbs.size,
                isOm = isOm,
                onSearchChange = onProverbSearchChange,
                onSelectCategory = onSelectProverbCategory,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        if (filteredProverbs.isEmpty()) {
            item(key = "proverb_empty_state") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.SearchOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = if (isOm) "Mammaaksi barbaadde hin argamne." else "No matching proverbs found.",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isOm) "Jecha biraa yaali ykn calaltuu haqi." else "Try another keyword or clear filters.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FilledTonalButton(
                        onClick = {
                            onProverbSearchChange("")
                            onSelectProverbCategory(null)
                        },
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(if (isOm) "Hunda Agarsiisi" else "Show All Proverbs")
                    }
                }
            }
        } else {
            itemsIndexed(filteredProverbs, key = { _, item -> item.id }) { index, item ->
                ProverbArchiveItemCard(
                    item = item,
                    displayIndex = index + 1,
                    isSelected = item.id == featured.id,
                    isFavorite = item.id in uiState.favoriteProverbIds,
                    isOm = isOm,
                    onToggleFavorite = { onToggleFavoriteProverb(item.id) },
                    onClick = {
                        onSelectProverbById(item.id)
                        scope.launch {
                            listState.animateScrollToItem(1)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun QuickModuleCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(26.dp)
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = contentColor.copy(alpha = 0.75f),
                    modifier = Modifier.size(18.dp)
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = contentColor
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor.copy(alpha = 0.85f)
                )
            }
        }
    }
}
