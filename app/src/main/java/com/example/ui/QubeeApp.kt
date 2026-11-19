package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Abc
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Abc
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.R
import com.example.data.AppLanguage
import com.example.data.ProverbItem
import com.example.ui.theme.GoldAccent
import kotlinx.coroutines.launch

const val ROUTE_HOME = "home"
const val ROUTE_QUBEE = "qubee"
const val ROUTE_PHRASES = "phrases"
const val ROUTE_FAVORITES = "favorites"

private enum class LearnHubSubTab { QUBEE, VOCABULARY }

private data class NavDestination(
    val route: String,
    val labelOmRes: Int,
    val labelEnRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

private val destinations = listOf(
    NavDestination(
        route = ROUTE_HOME,
        labelOmRes = R.string.nav_home_om,
        labelEnRes = R.string.nav_home_en,
        selectedIcon = Icons.Filled.AutoStories,
        unselectedIcon = Icons.Outlined.AutoStories,
        testTag = "nav_home_tab"
    ),
    NavDestination(
        route = ROUTE_QUBEE,
        labelOmRes = R.string.nav_qubee_om,
        labelEnRes = R.string.nav_qubee_en,
        selectedIcon = Icons.Filled.Abc,
        unselectedIcon = Icons.Outlined.Abc,
        testTag = "nav_qubee_tab"
    ),
    NavDestination(
        route = ROUTE_PHRASES,
        labelOmRes = R.string.nav_phrases_om,
        labelEnRes = R.string.nav_phrases_en,
        selectedIcon = Icons.Filled.Forum,
        unselectedIcon = Icons.Outlined.Forum,
        testTag = "nav_phrases_tab"
    ),
    NavDestination(
        route = ROUTE_FAVORITES,
        labelOmRes = R.string.nav_favorites_om,
        labelEnRes = R.string.nav_favorites_en,
        selectedIcon = Icons.Filled.Favorite,
        unselectedIcon = Icons.Outlined.FavoriteBorder,
        testTag = "nav_favorites_tab"
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QubeeApp(
    viewModel: QubeeViewModel,
    isDarkTheme: Boolean
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: ROUTE_HOME

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val isOm = uiState.language == AppLanguage.OROMO

    var learnHubTab by remember { mutableStateOf(LearnHubSubTab.QUBEE) }
    var showQubeeChartModal by remember { mutableStateOf(false) }
    var showSettingsModal by remember { mutableStateOf(false) }
    var showPracticeQuizModal by remember { mutableStateOf(false) }
    var showSpeakingModal by remember { mutableStateOf(false) }
    var showFlashcardsModal by remember { mutableStateOf(false) }
    var showListeningModal by remember { mutableStateOf(false) }
    var showWritingModal by remember { mutableStateOf(false) }
    var showProgressModal by remember { mutableStateOf(false) }
    var showUnifiedSearchModal by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.lastPointsToastMessage) {
        val msg = uiState.lastPointsToastMessage
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearPointsToast()
        }
    }

    val copySuccessMessage = stringResource(if (isOm) R.string.copy_success_om else R.string.copy_success_en)
    val ttsUnavailableMessage = if (isOm) {
        "Sagaleen dubbisaa meeshaa kanarratti hin qophoofne."
    } else {
        "Device speech voice unavailable offline — check phonetic guide."
    }

    fun navigateTo(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun copyToClipboard(text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        clipboard?.setPrimaryClip(ClipData.newPlainText("Aki Qubee", text))
        scope.launch { snackbarHostState.showSnackbar(copySuccessMessage) }
    }

    fun shareProverb(proverb: ProverbItem) {
        val shareText = "“${proverb.oromo}”\n— ${proverb.english}\n\n(${if (isOm) proverb.meaningOm else proverb.meaningEn})"
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        context.startActivity(Intent.createChooser(intent, if (isOm) "Mammaaksa Qoodi" else "Share Proverb"))
    }

    fun handleSpeak(text: String) {
        val started = viewModel.speak(text)
        if (!started) {
            scope.launch { snackbarHostState.showSnackbar(ttsUnavailableMessage) }
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useRail = maxWidth >= 600.dp

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                CenterAlignedTopAppBar(
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    ),
                    navigationIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { viewModel.toggleDarkTheme(isDarkTheme) },
                                modifier = Modifier.minimumInteractiveComponentSize().testTag("theme_toggle_button")
                            ) {
                                Icon(
                                    imageVector = if (isDarkTheme) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                                    contentDescription = stringResource(id = R.string.theme_toggle_cd)
                                )
                            }
                            IconButton(
                                onClick = { showUnifiedSearchModal = true },
                                modifier = Modifier.minimumInteractiveComponentSize().testTag("unified_search_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Search,
                                    contentDescription = if (isOm) "Barbaadi" else "Global search"
                                )
                            }
                        }
                    },
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.aki_logo),
                                contentDescription = stringResource(id = R.string.brand_logo_cd),
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.size(30.dp).clip(CircleShape).testTag("topbar_brand_logo")
                            )
                            Text(
                                text = stringResource(id = R.string.app_name),
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    actions = {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(50),
                            onClick = { showProgressModal = true },
                            modifier = Modifier.testTag("topbar_xp_badge")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(Icons.Filled.Star, null, tint = GoldAccent, modifier = Modifier.size(14.dp))
                                Text(
                                    text = "${uiState.userProgress.totalPoints}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        TextButton(
                            onClick = viewModel::toggleLanguage,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                            modifier = Modifier.minimumInteractiveComponentSize().testTag("language_toggle_button")
                        ) {
                            Text(
                                text = if (isOm) "OM⇄EN" else "EN⇄OM",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        IconButton(
                            onClick = { showSettingsModal = true },
                            modifier = Modifier.minimumInteractiveComponentSize().testTag("settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = stringResource(id = R.string.settings_cd)
                            )
                        }
                    }
                )
            },
            bottomBar = {
                if (!useRail) {
                    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                        destinations.forEach { dest ->
                            val selected = currentRoute == dest.route
                            val label = stringResource(if (isOm) dest.labelOmRes else dest.labelEnRes)
                            NavigationBarItem(
                                selected = selected,
                                onClick = { navigateTo(dest.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                        contentDescription = label
                                    )
                                },
                                label = { Text(text = label) },
                                modifier = Modifier.testTag(dest.testTag)
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                horizontalArrangement = Arrangement.Center
            ) {
                if (useRail) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        Spacer(modifier = Modifier.weight(1f))
                        destinations.forEach { dest ->
                            val selected = currentRoute == dest.route
                            val label = stringResource(if (isOm) dest.labelOmRes else dest.labelEnRes)
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateTo(dest.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                        contentDescription = label
                                    )
                                },
                                label = { Text(text = label) },
                                modifier = Modifier.testTag(dest.testTag)
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Box(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = ROUTE_HOME,
                        modifier = Modifier.fillMaxSize().widthIn(max = 840.dp)
                    ) {
                        composable(ROUTE_HOME) {
                            HomeScreen(
                                uiState = uiState,
                                onRandomProverb = viewModel::randomProverb,
                                onNextProverb = viewModel::nextProverb,
                                onSelectProverbById = viewModel::selectProverbById,
                                onToggleFavoriteProverb = viewModel::toggleFavoriteProverb,
                                onToggleFavoritePhrase = viewModel::toggleFavoritePhrase,
                                onProverbSearchChange = viewModel::updateProverbSearch,
                                onSelectProverbCategory = viewModel::setProverbFilter,
                                onSpeakProverb = ::handleSpeak,
                                onCopyText = ::copyToClipboard,
                                onShareProverb = ::shareProverb,
                                onNavigateToQubee = {
                                    learnHubTab = LearnHubSubTab.QUBEE
                                    navigateTo(ROUTE_QUBEE)
                                },
                                onOpenQubeeChart = { showQubeeChartModal = true },
                                onOpenPracticeQuiz = { showPracticeQuizModal = true },
                                onNavigateToFavorites = { navigateTo(ROUTE_FAVORITES) },
                                onBrowsePhraseCategory = { category ->
                                    viewModel.setPhraseFilter(category)
                                    navigateTo(ROUTE_PHRASES)
                                },
                                onNavigateToPhrases = {
                                    viewModel.setPhraseFilter(null)
                                    navigateTo(ROUTE_PHRASES)
                                },
                                onRecordQuizAnswer = { isCorrect ->
                                    viewModel.recordQuizAnswer(isCorrect, markDailyCompleted = true)
                                },
                                onNavigateToVocabulary = {
                                    learnHubTab = LearnHubSubTab.VOCABULARY
                                    navigateTo(ROUTE_QUBEE)
                                },
                                onOpenSpeakingLab = { showSpeakingModal = true },
                                onOpenFlashcards = { showFlashcardsModal = true },
                                onOpenListeningPractice = { showListeningModal = true },
                                onOpenWritingPractice = { showWritingModal = true },
                                onOpenProgress = { showProgressModal = true }
                            )
                        }

                        composable(ROUTE_QUBEE) {
                            Column(modifier = Modifier.fillMaxSize()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    FilterChip(
                                        selected = learnHubTab == LearnHubSubTab.QUBEE,
                                        onClick = { learnHubTab = LearnHubSubTab.QUBEE },
                                        label = { Text(if (isOm) "Qubee (33)" else "Qubee (33)") },
                                        modifier = Modifier.testTag("learn_hub_qubee_tab")
                                    )
                                    FilterChip(
                                        selected = learnHubTab == LearnHubSubTab.VOCABULARY,
                                        onClick = { learnHubTab = LearnHubSubTab.VOCABULARY },
                                        label = { Text(if (isOm) "Jechoota (${uiState.allVocabulary.size}+)" else "Vocabulary (${uiState.allVocabulary.size}+)") },
                                        modifier = Modifier.testTag("learn_hub_vocab_tab")
                                    )
                                }

                                if (learnHubTab == LearnHubSubTab.QUBEE) {
                                    QubeeAlphabetScreen(
                                        uiState = uiState,
                                        onSelectFilter = viewModel::setQubeeFilter,
                                        onSelectLetter = viewModel::selectLetter,
                                        onSpeakWord = ::handleSpeak,
                                        onOpenFullChart = { showQubeeChartModal = true },
                                        onToggleMastered = { letter ->
                                            viewModel.toggleMasteredItem("QUBEE", letter.name, letter.symbol)
                                        },
                                        onPracticeWriting = { letter ->
                                            viewModel.selectLetter(letter)
                                            showWritingModal = true
                                        },
                                        onPracticeSpeaking = { letter ->
                                            viewModel.setSpeakingTarget(
                                                targetOromo = letter.exampleWord,
                                                targetEnglish = letter.exampleMeaningEn,
                                                phoneticTip = letter.soundGuideEn
                                            )
                                            showSpeakingModal = true
                                        },
                                        onOpenQubeeQuiz = { showPracticeQuizModal = true },
                                        modifier = Modifier.weight(1f)
                                    )
                                } else {
                                    VocabularyScreen(
                                        uiState = uiState,
                                        onSearchChange = viewModel::updateVocabSearch,
                                        onSelectCategory = viewModel::setVocabFilter,
                                        onSelectLetterPrefix = viewModel::setVocabLetterFilter,
                                        onToggleFavoriteVocab = viewModel::toggleFavoriteVocab,
                                        onToggleMasteredVocab = { word ->
                                            viewModel.toggleMasteredItem("VOCAB", word.id, word.oromo)
                                        },
                                        onVocabViewed = viewModel::recordVocabViewed,
                                        onSpeakText = ::handleSpeak,
                                        onPracticeSpeaking = { word ->
                                            viewModel.setSpeakingTarget(
                                                targetOromo = word.oromo,
                                                targetEnglish = word.english,
                                                phoneticTip = word.pronunciation
                                            )
                                            showSpeakingModal = true
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        composable(ROUTE_PHRASES) {
                            PhrasesScreen(
                                uiState = uiState,
                                onSearchChange = viewModel::updatePhraseSearch,
                                onSelectCategory = viewModel::setPhraseFilter,
                                onToggleFavoritePhrase = viewModel::toggleFavoritePhrase,
                                onCopyPhrase = ::copyToClipboard,
                                onSpeakPhrase = { text ->
                                    handleSpeak(text)
                                    uiState.allPhrases.firstOrNull { it.oromo == text }?.let {
                                        viewModel.recordPhraseViewed(it)
                                    }
                                },
                                onPracticeSpeaking = { phrase ->
                                    viewModel.setSpeakingTarget(
                                        targetOromo = phrase.oromo,
                                        targetEnglish = phrase.english,
                                        phoneticTip = phrase.pronunciationTip
                                    )
                                    showSpeakingModal = true
                                }
                            )
                        }

                        composable(ROUTE_FAVORITES) {
                            FavoritesScreen(
                                uiState = uiState,
                                onToggleFavoriteProverb = viewModel::toggleFavoriteProverb,
                                onToggleFavoritePhrase = viewModel::toggleFavoritePhrase,
                                onSelectProverbInHome = { proverbId ->
                                    viewModel.selectProverbById(proverbId)
                                    navigateTo(ROUTE_HOME)
                                },
                                onSpeakText = ::handleSpeak,
                                onCopyText = ::copyToClipboard,
                                onShareProverb = ::shareProverb,
                                onExploreProverbs = { navigateTo(ROUTE_HOME) },
                                onExplorePhrases = { navigateTo(ROUTE_PHRASES) },
                                onToggleFavoriteVocab = viewModel::toggleFavoriteVocab,
                                onExploreVocabulary = {
                                    learnHubTab = LearnHubSubTab.VOCABULARY
                                    navigateTo(ROUTE_QUBEE)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showQubeeChartModal) {
        QubeeChartModal(
            selectedLetter = uiState.selectedLetter,
            isOm = isOm,
            onLetterClick = { letter ->
                viewModel.selectLetter(letter)
                handleSpeak(letter.ttsPronunciation)
            },
            onReplaySound = { letter -> handleSpeak(letter.ttsPronunciation) },
            onDismiss = { showQubeeChartModal = false },
            onPracticeSpeaking = { letter ->
                showQubeeChartModal = false
                viewModel.setSpeakingTarget(
                    targetOromo = letter.exampleWord,
                    targetEnglish = letter.exampleMeaningEn,
                    phoneticTip = letter.soundGuideEn
                )
                showSpeakingModal = true
            }
        )
    }

    if (showSettingsModal) {
        SettingsModal(
            uiState = uiState,
            onSelectLanguage = viewModel::setLanguage,
            onSelectThemeOverride = viewModel::setDarkThemeOverride,
            onSelectTtsSpeed = viewModel::setTtsSpeed,
            onClearFavorites = viewModel::clearAllFavorites,
            onOpenQuiz = { showPracticeQuizModal = true },
            onDismiss = { showSettingsModal = false },
            onSelectTextScale = viewModel::setTextScale,
            onSelectRepeatCount = viewModel::setRepeatCount,
            onSelectDailyGoal = viewModel::setDailyGoalPoints,
            onToggleReminders = viewModel::setRemindersEnabled,
            onSignInGoogle = viewModel::signInWithGoogle,
            onSyncCloud = viewModel::syncCloudBackup,
            onSignOutCloud = viewModel::signOutCloud,
            onResetAllData = viewModel::resetAllLearningData
        )
    }

    if (showPracticeQuizModal) {
        PracticeQuizModal(
            isOm = isOm,
            onSpeakText = ::handleSpeak,
            onDismiss = { showPracticeQuizModal = false },
            allPhrases = uiState.allPhrases,
            quizProgress = uiState.quizProgress,
            onRecordAnswer = { isCorrect -> viewModel.recordQuizAnswer(isCorrect, markDailyCompleted = true) },
            allVocabulary = uiState.allVocabulary,
            quizAttempts = uiState.quizAttempts,
            onCompleteQuizSession = { mode, score, total ->
                viewModel.recordQuizSession(mode, score, total)
            }
        )
    }

    if (showSpeakingModal) {
        SpeakingPracticeModal(
            isOm = isOm,
            target = uiState.speakingTarget,
            audioState = uiState.audioState,
            aiComparisonState = uiState.aiComparisonState,
            aiModelAudioState = uiState.aiModelAudioState,
            onSelectTarget = { preset ->
                viewModel.setSpeakingTarget(preset.targetOromo, preset.targetEnglish, preset.phoneticTip)
            },
            onPlayAiModel = {
                viewModel.playAiModelPronunciation(
                    targetOromo = uiState.speakingTarget.targetOromo,
                    phoneticGuide = uiState.speakingTarget.phoneticTip
                )
            },
            onSpeakFallback = ::handleSpeak,
            onStartRecording = viewModel::startRecording,
            onStopRecording = viewModel::stopRecording,
            onPlayRecording = viewModel::playRecordedAudio,
            onCompareWithAi = {
                viewModel.comparePronunciationWithAi(
                    targetOromo = uiState.speakingTarget.targetOromo,
                    englishMeaning = uiState.speakingTarget.targetEnglish,
                    phoneticTip = uiState.speakingTarget.phoneticTip
                )
            },
            onDismiss = {
                viewModel.stopSpeaking()
                showSpeakingModal = false
            }
        )
    }

    if (showFlashcardsModal) {
        FlashcardsModal(
            isOm = isOm,
            allVocabulary = uiState.allVocabulary,
            allPhrases = uiState.allPhrases,
            onSpeakText = ::handleSpeak,
            onMarkCardMastered = { itemId, label ->
                viewModel.awardPracticePoints("FLASHCARD", itemId, label, points = 8, uniquePerItem = true)
            },
            onDismiss = { showFlashcardsModal = false }
        )
    }

    if (showListeningModal) {
        ListeningPracticeModal(
            isOm = isOm,
            allVocabulary = uiState.allVocabulary,
            onSpeakText = ::handleSpeak,
            onCompletedSession = { score, total ->
                viewModel.awardPracticePoints(
                    source = "LISTENING",
                    itemId = "listen_${System.currentTimeMillis()}",
                    label = "Listening $score/$total",
                    points = (score * 10) + 15,
                    uniquePerItem = false
                )
            },
            onDismiss = { showListeningModal = false }
        )
    }

    if (showWritingModal) {
        WritingPracticeModal(
            isOm = isOm,
            initialLetter = uiState.selectedLetter,
            allVocabulary = uiState.allVocabulary,
            onSpeakText = ::handleSpeak,
            onPracticeCompleted = { itemId, label ->
                viewModel.awardPracticePoints("WRITING", itemId, label, points = 10, uniquePerItem = true)
            },
            onDismiss = { showWritingModal = false }
        )
    }

    if (showProgressModal) {
        ProgressAchievementsModal(
            uiState = uiState,
            onDismiss = { showProgressModal = false }
        )
    }

    if (showUnifiedSearchModal) {
        UnifiedSearchModal(
            uiState = uiState,
            onQueryChange = viewModel::updateUnifiedSearch,
            onSaveSearch = viewModel::saveSearchQuery,
            onClearHistory = viewModel::clearSearchHistory,
            onSpeakText = ::handleSpeak,
            onSelectProverb = { proverbId ->
                viewModel.selectProverbById(proverbId)
                navigateTo(ROUTE_HOME)
            },
            onDismiss = { showUnifiedSearchModal = false }
        )
    }
}
