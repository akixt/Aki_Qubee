package com.example

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.test.core.app.ApplicationProvider
import com.example.audio.AudioPlaybackState
import com.example.data.AppLanguage
import com.example.data.OromoContent
import com.example.data.PhraseCategory
import com.example.data.VocabularyCategory
import com.example.firebase.AiModelState
import com.example.ui.FavoritesScreen
import com.example.ui.PracticeQuizModal
import com.example.ui.QuizMode
import com.example.ui.QubeeViewModel
import com.example.ui.SpeakingPracticeModal
import com.example.ui.SpeakingTarget
import com.example.ui.VocabularyScreen
import com.example.ui.generateDailyQuizQuestion
import com.example.ui.theme.QubeeTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper
import kotlin.random.Random

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AkiQubeeUpgradesTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `datasets satisfy expanded content requirements across vocabulary, haasawa, and mammaaksa`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val vocabulary = OromoContent.loadVocabulary(context)
        val phrases = OromoContent.loadPhrases(context)
        val proverbs = OromoContent.loadProverbs(context)

        assertTrue("Expected at least 1200 vocabulary entries, got ${vocabulary.size}", vocabulary.size >= 1200)
        assertTrue("Expected all 9 vocabulary categories", vocabulary.map { it.category }.toSet().size == VocabularyCategory.entries.size)

        assertTrue("Expected at least 1200 Haasawa phrases, got ${phrases.size}", phrases.size >= 1200)
        assertTrue("Expected all 11 Haasawa categories", phrases.map { it.category }.toSet().size == PhraseCategory.entries.size)

        assertTrue("Expected at least 1200 Mammaaksa proverbs, got ${proverbs.size}", proverbs.size >= 1200)
    }

    @Test
    fun `user points xp, mastered items, and quiz attempts accrue cleanly in Room`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = QubeeViewModel(app)

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            ShadowLooper.idleMainLooper()
            viewModel.uiState.value.allVocabulary.size >= 1200 &&
                viewModel.uiState.value.allProverbs.size >= 1200
        }

        val initialPoints = viewModel.uiState.value.userProgress.totalPoints

        // Discovery XP from browsing a new proverb
        viewModel.randomProverb()
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            ShadowLooper.idleMainLooper()
            viewModel.uiState.value.userProgress.totalPoints >= initialPoints + 5
        }
        val afterProverbPoints = viewModel.uiState.value.userProgress.totalPoints
        assertTrue(afterProverbPoints >= initialPoints + 5)

        // Mastering a Qubee item awards +10 XP
        viewModel.toggleMasteredItem("QUBEE", "A", "Aa — Abbaa")
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            ShadowLooper.idleMainLooper()
            viewModel.uiState.value.userProgress.totalPoints >= afterProverbPoints + 10
        }
        assertTrue("QUBEE_A" in viewModel.uiState.value.masteredItemKeys)
        assertTrue(viewModel.uiState.value.userProgress.currentStreakDays >= 1)

        // Recording a quiz session persists quiz attempt and awards XP
        val beforeQuizPoints = viewModel.uiState.value.userProgress.totalPoints
        viewModel.recordQuizSession(mode = QuizMode.VOCABULARY.name, score = 4, totalQuestions = 5)
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            ShadowLooper.idleMainLooper()
            viewModel.uiState.value.quizAttempts.isNotEmpty() &&
                viewModel.uiState.value.userProgress.totalPoints > beforeQuizPoints
        }
        assertEquals(4, viewModel.uiState.value.quizAttempts.first().score)
    }

    @Test
    fun `all 8 quiz modes generate valid questions and practice quiz modal switches modes`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val phrases = OromoContent.loadPhrases(context)
        val vocabulary = OromoContent.loadVocabulary(context)

        QuizMode.entries.forEach { mode ->
            val q = generateDailyQuizQuestion(
                mode = mode,
                allPhrases = phrases,
                seedRandom = Random(42),
                allVocabulary = vocabulary
            )
            assertTrue("Prompt empty for $mode", q.prompt.isNotBlank())
            if (mode == QuizMode.MATCHING) {
                assertEquals(3, q.matchingPairs.size)
            } else {
                assertTrue("Expected options for $mode", q.options.size >= 2)
                assertTrue("Correct answer missing in options for $mode", q.correctAnswer in q.options)
            }
        }

        composeTestRule.setContent {
            QubeeTheme {
                PracticeQuizModal(
                    isOm = false,
                    onSpeakText = {},
                    onDismiss = {},
                    allPhrases = phrases,
                    allVocabulary = vocabulary
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("quiz_mode_vocab").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Translate this Afaan Oromoo vocabulary word:")
            .performScrollTo()
            .assertIsDisplayed()

        composeTestRule.onNodeWithTag("quiz_mode_tf").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("True").performScrollTo().assertIsDisplayed()

        composeTestRule.onNodeWithTag("quiz_mode_fill_blank").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Fill in the blank to complete the sentence:")
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun `vocabulary search filters entries and bookmarks sync with favorites words tab`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = QubeeViewModel(app)
        viewModel.setLanguage(AppLanguage.ENGLISH)
        var showFavorites by mutableStateOf(false)

        composeTestRule.setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            QubeeTheme {
                if (!showFavorites) {
                    VocabularyScreen(
                        uiState = uiState,
                        onSearchChange = viewModel::updateVocabSearch,
                        onSelectCategory = viewModel::setVocabFilter,
                        onSelectLetterPrefix = viewModel::setVocabLetterFilter,
                        onToggleFavoriteVocab = viewModel::toggleFavoriteVocab,
                        onToggleMasteredVocab = {},
                        onVocabViewed = viewModel::recordVocabViewed,
                        onSpeakText = {},
                        onPracticeSpeaking = {}
                    )
                } else {
                    FavoritesScreen(
                        uiState = uiState,
                        onToggleFavoriteProverb = viewModel::toggleFavoriteProverb,
                        onToggleFavoritePhrase = viewModel::toggleFavoritePhrase,
                        onToggleFavoriteVocab = viewModel::toggleFavoriteVocab,
                        onSelectProverbInHome = {},
                        onSpeakText = {},
                        onCopyText = {},
                        onShareProverb = {},
                        onExploreProverbs = {},
                        onExplorePhrases = {},
                        onExploreVocabulary = { showFavorites = false }
                    )
                }
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            ShadowLooper.idleMainLooper()
            viewModel.uiState.value.allVocabulary.size >= 1200 &&
                viewModel.uiState.value.language == AppLanguage.ENGLISH
        }

        composeTestRule.onNodeWithTag("vocab_search_input").performTextInput("Buna")
        composeTestRule.waitForIdle()
        val matched = viewModel.uiState.value.filteredVocabulary.first {
            it.oromo.equals("Buna", ignoreCase = true)
        }
        composeTestRule.onNodeWithTag("vocab_lazy_column")
            .performScrollToNode(hasTestTag("vocab_card_${matched.id}"))
        composeTestRule.onNodeWithTag("vocab_card_${matched.id}").assertIsDisplayed()
        composeTestRule.onNodeWithTag("favorite_vocab_${matched.id}").performClick()

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            ShadowLooper.idleMainLooper()
            matched.id in viewModel.uiState.value.favoriteVocabIds
        }

        showFavorites = true
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("favorites_filter_vocab").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("favorites_lazy_column")
            .performScrollToNode(hasTestTag("favorite_vocab_card_${matched.id}"))
        composeTestRule.onNodeWithTag("favorite_vocab_card_${matched.id}").assertIsDisplayed()
    }

    @Test
    fun `speaking practice modal displays offline ai error transparently without fake scores`() {
        val offlineMessage = "AI pronunciation comparison requires an online Firebase connection. Replay your own recording above to self-check."
        composeTestRule.setContent {
            QubeeTheme {
                SpeakingPracticeModal(
                    isOm = false,
                    target = SpeakingTarget("Akkam jirta?", "How are you? (singular)", "ah-kahm jeer-tah"),
                    audioState = AudioPlaybackState(hasRecordedAudio = true),
                    aiComparisonState = AiModelState.Error(offlineMessage, isOfflineOrAttestation = true),
                    aiModelAudioState = AiModelState.Idle,
                    onSelectTarget = {},
                    onPlayAiModel = {},
                    onSpeakFallback = {},
                    onStartRecording = { true },
                    onStopRecording = { true },
                    onPlayRecording = {},
                    onCompareWithAi = {},
                    onDismiss = {}
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("speaking_practice_modal").assertIsDisplayed()
        composeTestRule.onNodeWithTag("speaking_ai_error_card")
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule.onNodeWithText(offlineMessage, substring = true)
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag("speaking_ai_result_card").assertDoesNotExist()
    }

    @Test
    fun `room learning repository merges higher firebase cloud progress safely`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefsRepo = com.example.data.AppPreferencesRepository(context)
        val learningRepo = com.example.data.local.LearningRepository(context, prefsRepo)
        learningRepo.ensureInitializedAndMigrate()
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())

        learningRepo.mergeCloudProgressIfHigher(
            totalPoints = 320,
            bestStreak = 9,
            totalLearningSeconds = 1800L,
            unlockedIds = listOf("beginner", "quiz_champion"),
            todayPoints = 45,
            currentStreak = 4,
            lastActiveDate = today
        )

        val progress = learningRepo.userProgressFlow.first()
        val achievements = learningRepo.unlockedAchievementIdsFlow.first()

        assertTrue(progress.totalPoints >= 320)
        assertTrue(progress.bestStreakDays >= 9)
        assertTrue(progress.currentStreakDays >= 4)
        assertTrue(progress.todayPoints >= 45)
        assertTrue("beginner" in achievements)
        assertTrue("quiz_champion" in achievements)
    }
}
