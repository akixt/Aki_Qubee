package com.example.ui

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.outlined.Abc
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.AppLanguage
import com.example.data.PhraseCategory
import com.example.data.ProverbCategory
import com.example.data.ProverbItem
import com.example.ui.theme.GoldAccent
import kotlinx.coroutines.launch
import java.util.Calendar

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
    onRecordQuizAnswer: (Boolean) -> Unit = {},
    onNavigateToVocabulary: () -> Unit = {},
    onOpenSpeakingLab: () -> Unit = {},
    onOpenFlashcards: () -> Unit = {},
    onOpenListeningPractice: () -> Unit = {},
    onOpenWritingPractice: () -> Unit = {},
    onOpenProgress: () -> Unit = {}
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
    val progress = uiState.userProgress
    val goalRatio = (progress.todayPoints.toFloat() / uiState.dailyGoalPoints.coerceAtLeast(1)).coerceIn(0f, 1f)
    val recentActivities = uiState.learningHistory.take(4)

    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .testTag("home_lazy_column"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Hero Editorial Banner (Aki Qubee + Powered by AKI TECH + Slogan)
        item(key = "hero_banner") {
            Card(
                shape = MaterialTheme.shapes.large,
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(156.dp)
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
                                        Color.Black.copy(alpha = 0.22f),
                                        Color(0xFF0E2920).copy(alpha = 0.9f)
                                    )
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = GoldAccent.copy(alpha = 0.94f),
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    text = "Powered by AKI TECH",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color(0xFF1A1400),
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                                )
                            }

                            Image(
                                painter = painterResource(id = R.drawable.aki_logo),
                                contentDescription = stringResource(id = R.string.brand_logo_cd),
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .testTag("hero_brand_logo")
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = greeting,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = stringResource(id = if (isOm) R.string.brand_tagline_om else R.string.brand_tagline_en),
                                style = MaterialTheme.typography.labelLarge,
                                color = GoldAccent
                            )
                            Text(
                                text = if (isOm) {
                                    "Qubee 33 • Jechoota ${uiState.allVocabulary.size}+ • Haasawa ${uiState.allPhrases.size}+ • Mammaaksa ${uiState.allProverbs.size}+"
                                } else {
                                    "33 Qubee • ${uiState.allVocabulary.size}+ Words • ${uiState.allPhrases.size}+ Phrases • ${uiState.allProverbs.size}+ Proverbs"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
            }
        }

        // 2. Proverbs of the Day Card (+XP on discover)
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

        // 3. Learning Progress Overview & Continue Learning
        item(key = "progress_overview") {
            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                onClick = onOpenProgress,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_progress_overview_card")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Filled.Star, null, tint = GoldAccent, modifier = Modifier.size(22.dp))
                            Column {
                                Text(
                                    text = "${progress.totalPoints} XP",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = if (isOm) "Galma Har'aa: ${progress.todayPoints}/${uiState.dailyGoalPoints} XP" else "Today: ${progress.todayPoints}/${uiState.dailyGoalPoints} XP goal",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(color = MaterialTheme.colorScheme.tertiaryContainer, shape = RoundedCornerShape(50)) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Filled.LocalFireDepartment, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onTertiaryContainer)
                                    Text("${progress.currentStreakDays}d", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                }
                            }
                            Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(50)) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Filled.EmojiEvents, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                                    Text("${uiState.unlockedAchievementIds.size}/6", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    LinearProgressIndicator(
                        progress = { goalRatio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val lastItem = recentActivities.firstOrNull()
                        Text(
                            text = if (lastItem != null) {
                                if (isOm) "Itti fufi: ${lastItem.title}" else "Continue: ${lastItem.title}"
                            } else {
                                if (isOm) "Barumsa har'aa eegali" else "Start today's study session"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = onOpenProgress) {
                            Text(if (isOm) "Bal'ina" else "Stats")
                            Spacer(modifier = Modifier.width(2.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, modifier = Modifier.size(15.dp))
                        }
                    }
                }
            }
        }

        // 4. Quick Access Learning Modules Grid
        item(key = "quick_shortcuts") {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
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
                        subtitle = if (isOm) "Tuqii dhaggeeffadhu" else "Tap letters for audio",
                        icon = Icons.Filled.GridView,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        onClick = onOpenQubeeChart,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("open_qubee_chart_button")
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickModuleCard(
                        title = if (isOm) "Jechoota (${uiState.allVocabulary.size}+)" else "Vocabulary",
                        subtitle = if (isOm) "OM ↔ EN kuusaa" else "${uiState.allVocabulary.size}+ OM ↔ EN words",
                        icon = Icons.AutoMirrored.Filled.MenuBook,
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                        onClick = onNavigateToVocabulary,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("quick_vocab_card")
                    )
                    QuickModuleCard(
                        title = if (isOm) "Shaakala Dubbii" else "Speaking AI",
                        subtitle = if (isOm) "Waraabi & madaali" else "Record & compare",
                        icon = Icons.Filled.Mic,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        onClick = onOpenSpeakingLab,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("quick_speaking_card")
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledTonalButton(
                        onClick = onOpenFlashcards,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f).height(48.dp).testTag("home_flashcards_button")
                    ) {
                        Icon(Icons.Filled.Style, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isOm) "Kaardii" else "Cards", maxLines = 1)
                    }
                    FilledTonalButton(
                        onClick = onOpenListeningPractice,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f).height(48.dp).testTag("home_listening_button")
                    ) {
                        Icon(Icons.Filled.Headphones, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isOm) "Dhaga'i" else "Listen", maxLines = 1)
                    }
                    FilledTonalButton(
                        onClick = onOpenWritingPractice,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f).height(48.dp).testTag("home_writing_button")
                    ) {
                        Icon(Icons.Filled.Draw, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isOm) "Barreessi" else "Write", maxLines = 1)
                    }
                }

                val favCount = uiState.favoriteProverbIds.size + uiState.favoritePhraseIds.size + uiState.favoriteVocabIds.size
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
                        Icon(Icons.Filled.Quiz, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.size(6.dp))
                        Text(if (isOm) "Qormaata (8)" else "8-Mode Quiz")
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
                        Text(if (isOm) "Filatamoo ($favCount)" else "Favorites ($favCount)")
                    }
                }
            }
        }

        // 5. Daily Quiz Interactive Multiple-Choice Progress Card
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

        // 6. Daily Phrases Interactive Contextual Section
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

        // 7. Recent Learning Activities Card
        if (recentActivities.isNotEmpty()) {
            item(key = "recent_learning_activities") {
                Card(
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Filled.History, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Text(
                                text = if (isOm) "Sochii Barumsaa Dhiyeenyaa" else "Recent Learning Activity",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(recentActivities, key = { it.historyKey }) { entry ->
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(12.dp),
                                    onClick = { onSpeakProverb(entry.title) }
                                ) {
                                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                                        Text(entry.module, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                        Text(entry.title, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, maxLines = 1)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 8. Proverbs Library Archive & Filters
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
                            listState.animateScrollToItem(2)
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
    icon: ImageVector,
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
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, null, tint = contentColor, modifier = Modifier.size(24.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = contentColor.copy(alpha = 0.75f),
                    modifier = Modifier.size(16.dp)
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = contentColor)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = contentColor.copy(alpha = 0.85f))
            }
        }
    }
}
