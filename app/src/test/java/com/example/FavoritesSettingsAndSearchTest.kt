package com.example

import android.app.Application
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
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppLanguage
import com.example.data.OromoContent
import com.example.ui.FavoritesScreen
import com.example.ui.HomeScreen
import com.example.ui.PhrasesScreen
import com.example.ui.PracticeQuizModal
import com.example.ui.QubeeUiState
import com.example.ui.QubeeViewModel
import com.example.ui.SettingsModal
import com.example.ui.theme.QubeeTheme
import kotlinx.coroutines.flow.first
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class FavoritesSettingsAndSearchTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `heart bookmarks toggle proverb and phrase favorites and appear in favorites screen`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = QubeeViewModel(app)
        var showFavorites by mutableStateOf(false)

        val toggledProverbIds = mutableListOf<String>()

        composeTestRule.setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            QubeeTheme {
                if (!showFavorites) {
                    HomeScreen(
                        uiState = uiState,
                        onRandomProverb = viewModel::randomProverb,
                        onNextProverb = viewModel::nextProverb,
                        onSelectProverbById = viewModel::selectProverbById,
                        onToggleFavoriteProverb = { id ->
                            toggledProverbIds.add(id)
                            viewModel.toggleFavoriteProverb(id)
                        },
                        onToggleFavoritePhrase = viewModel::toggleFavoritePhrase,
                        onProverbSearchChange = viewModel::updateProverbSearch,
                        onSelectProverbCategory = viewModel::setProverbFilter,
                        onSpeakProverb = {},
                        onCopyText = {},
                        onShareProverb = {},
                        onNavigateToQubee = {},
                        onOpenQubeeChart = {},
                        onOpenPracticeQuiz = {},
                        onNavigateToFavorites = { showFavorites = true },
                        onBrowsePhraseCategory = {},
                        onNavigateToPhrases = {}
                    )
                } else {
                    FavoritesScreen(
                        uiState = uiState,
                        onToggleFavoriteProverb = viewModel::toggleFavoriteProverb,
                        onToggleFavoritePhrase = viewModel::toggleFavoritePhrase,
                        onSelectProverbInHome = {},
                        onSpeakText = {},
                        onCopyText = {},
                        onShareProverb = {},
                        onExploreProverbs = { showFavorites = false },
                        onExplorePhrases = {}
                    )
                }
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            ShadowLooper.idleMainLooper()
            viewModel.uiState.value.allProverbs.size >= 500
        }
        composeTestRule.waitForIdle()
        val featuredId = viewModel.uiState.value.featuredProverb.id
        val firstGreetingPhraseId = OromoContent.phrases.first().id

        // Bookmark featured proverb
        composeTestRule.onNodeWithTag("favorite_featured_proverb_button")
            .performScrollTo()
            .performClick()
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            ShadowLooper.idleMainLooper()
            featuredId in viewModel.uiState.value.favoriteProverbIds
        }
        composeTestRule.waitForIdle()
        assertTrue(featuredId in viewModel.uiState.value.favoriteProverbIds)

        // Bookmark first daily phrase on Home
        composeTestRule.onNodeWithTag("home_lazy_column")
            .performScrollToNode(hasTestTag("home_favorite_phrase_$firstGreetingPhraseId"))
        composeTestRule.onNodeWithTag("home_favorite_phrase_$firstGreetingPhraseId").performClick()
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            ShadowLooper.idleMainLooper()
            firstGreetingPhraseId in viewModel.uiState.value.favoritePhraseIds
        }
        composeTestRule.waitForIdle()
        assertTrue(firstGreetingPhraseId in viewModel.uiState.value.favoritePhraseIds)

        // Switch to FavoritesScreen and verify both items appear
        showFavorites = true
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("favorites_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("favorite_proverb_card_$featuredId").assertIsDisplayed()

        // Filter to phrases only and verify saved phrase is shown
        composeTestRule.onNodeWithTag("favorites_filter_phrases").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("favorite_phrase_card_$firstGreetingPhraseId").assertIsDisplayed()

        // Switch back to All and unfavorite the proverb
        composeTestRule.onNodeWithTag("favorites_filter_all").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("unfavorite_proverb_$featuredId").performClick()
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            ShadowLooper.idleMainLooper()
            featuredId !in viewModel.uiState.value.favoriteProverbIds
        }
        composeTestRule.waitForIdle()
        assertFalse(featuredId in viewModel.uiState.value.favoriteProverbIds)
    }

    @Test
    fun `daily phrases top search bar filters by category keyword and phrase text`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = QubeeViewModel(app)
        viewModel.setLanguage(AppLanguage.ENGLISH)

        composeTestRule.setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            QubeeTheme {
                PhrasesScreen(
                    uiState = uiState,
                    onSearchChange = viewModel::updatePhraseSearch,
                    onSelectCategory = viewModel::setPhraseFilter,
                    onToggleFavoritePhrase = viewModel::toggleFavoritePhrase,
                    onCopyPhrase = {},
                    onSpeakPhrase = {}
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("phrase_search_input").assertIsDisplayed()

        // Filter by category keyword "Market"
        composeTestRule.onNodeWithTag("phrase_search_input").performTextInput("Market")
        composeTestRule.waitForIdle()
        assertTrue(viewModel.uiState.value.filteredPhrases.any { it.id == "m1" })
        composeTestRule.onNodeWithText("How much does this cost?").assertIsDisplayed()

        // Clear search and filter by phrase keyword "Coffee"
        composeTestRule.onNodeWithTag("clear_search_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("phrase_search_input").performTextInput("Coffee")
        composeTestRule.waitForIdle()
        assertTrue(viewModel.uiState.value.filteredPhrases.any { it.id == "f1" })
    }

    @Test
    fun `settings modal displays developer info and updates tts speed`() {
        var selectedSpeed by mutableStateOf(0.88f)
        var openedQuiz = false

        composeTestRule.setContent {
            QubeeTheme {
                SettingsModal(
                    uiState = QubeeUiState(
                        language = AppLanguage.ENGLISH,
                        ttsSpeed = selectedSpeed
                    ),
                    onSelectLanguage = {},
                    onSelectThemeOverride = {},
                    onSelectTtsSpeed = { selectedSpeed = it },
                    onClearFavorites = {},
                    onOpenQuiz = { openedQuiz = true },
                    onDismiss = {}
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("settings_modal").assertIsDisplayed()
        composeTestRule.onNodeWithTag("settings_list")
            .performScrollToNode(hasTestTag("tts_speed_slow"))
        composeTestRule.onNodeWithTag("tts_speed_slow").performClick()
        composeTestRule.waitForIdle()
        assertEquals(0.72f, selectedSpeed, 0.01f)

        composeTestRule.onNodeWithTag("settings_list")
            .performScrollToNode(hasTestTag("developer_info_card"))
        composeTestRule.onNodeWithTag("developer_info_card").assertIsDisplayed()
        composeTestRule.onNodeWithText("Aklilu Tibebu").assertIsDisplayed()
        composeTestRule.onNodeWithText("Madda Walabu University", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithTag("settings_list").performTouchInput { swipeUp() }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Aklilu Tibebu / Aki Tech", substring = true).assertIsDisplayed()

        composeTestRule.onNodeWithTag("settings_list")
            .performScrollToNode(hasTestTag("settings_open_quiz_button"))
        composeTestRule.onNodeWithTag("settings_open_quiz_button").performClick()
        composeTestRule.waitForIdle()
        assertTrue(openedQuiz)
    }

    @Test
    fun `practice quiz modal handles option selection and advances to next question`() {
        composeTestRule.setContent {
            QubeeTheme {
                PracticeQuizModal(
                    isOm = false,
                    onSpeakText = {},
                    onDismiss = {}
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("practice_quiz_modal").assertIsDisplayed()
        composeTestRule.onNodeWithTag("quiz_option_0").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("quiz_next_button")
            .performScrollTo()
            .assertIsDisplayed()
            .performClick()
        composeTestRule.waitForIdle()

        // Switch mode to Daily Phrases
        composeTestRule.onNodeWithTag("quiz_mode_phrases").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("What is the English meaning of this phrase?")
            .performScrollTo()
            .assertIsDisplayed()

        // Switch mode to Mixed
        composeTestRule.onNodeWithTag("quiz_mode_mix").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("quiz_option_0").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun `daily quiz home card answers question, records progress, and switches between qubee and phrase modes`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = QubeeViewModel(app)
        viewModel.setLanguage(AppLanguage.ENGLISH)

        composeTestRule.setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            QubeeTheme {
                HomeScreen(
                    uiState = uiState,
                    onRandomProverb = viewModel::randomProverb,
                    onNextProverb = viewModel::nextProverb,
                    onSelectProverbById = viewModel::selectProverbById,
                    onToggleFavoriteProverb = viewModel::toggleFavoriteProverb,
                    onToggleFavoritePhrase = viewModel::toggleFavoritePhrase,
                    onProverbSearchChange = viewModel::updateProverbSearch,
                    onSelectProverbCategory = viewModel::setProverbFilter,
                    onSpeakProverb = {},
                    onCopyText = {},
                    onShareProverb = {},
                    onNavigateToQubee = {},
                    onOpenQubeeChart = {},
                    onOpenPracticeQuiz = {},
                    onNavigateToFavorites = {},
                    onBrowsePhraseCategory = {},
                    onNavigateToPhrases = {},
                    onRecordQuizAnswer = { isCorrect ->
                        viewModel.recordQuizAnswer(isCorrect, markDailyCompleted = true)
                    }
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            ShadowLooper.idleMainLooper()
            viewModel.uiState.value.allPhrases.size >= 1000 &&
                viewModel.uiState.value.language == AppLanguage.ENGLISH
        }

        composeTestRule.onNodeWithTag("home_lazy_column")
            .performScrollToNode(hasTestTag("daily_quiz_home_card"))
        composeTestRule.onNodeWithTag("daily_quiz_home_card").assertIsDisplayed()

        composeTestRule.onNodeWithTag("home_lazy_column")
            .performScrollToNode(hasTestTag("daily_quiz_home_option_0"))
        composeTestRule.onNodeWithTag("daily_quiz_home_option_0")
            .performScrollTo()
            .performClick()

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            ShadowLooper.idleMainLooper()
            viewModel.uiState.value.quizProgress.totalAnswered >= 1
        }
        composeTestRule.waitForIdle()
        assertTrue(viewModel.uiState.value.quizProgress.totalAnswered >= 1)
        assertTrue(viewModel.uiState.value.quizProgress.lastCompletedDate.isNotBlank())
        composeTestRule.onNodeWithTag("daily_quiz_home_feedback")
            .performScrollTo()
            .assertIsDisplayed()

        // Switch to Daily Phrase mode inside the Daily Quiz home card
        composeTestRule.onNodeWithTag("daily_quiz_mode_phrase")
            .performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("What does this daily phrase mean?")
            .performScrollTo()
            .assertIsDisplayed()
    }
}
