package com.example

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.test.core.app.ApplicationProvider
import com.example.data.OromoContent
import com.example.ui.HomeScreen
import com.example.ui.QubeeViewModel
import com.example.ui.theme.QubeeTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ProverbsRobolectricTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `mammaaksa and haasawa json assets each contain 1000 to 2000 unique valid items`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val proverbs = OromoContent.loadProverbs(context)
        val phrases = OromoContent.loadPhrases(context)

        assertTrue("Expected 1000..2000 proverbs, got ${proverbs.size}", proverbs.size in 1000..2000)
        assertTrue("Expected 1000..2000 phrases, got ${phrases.size}", phrases.size in 1000..2000)

        val uniqueProverbIds = proverbs.map { it.id }.toSet()
        assertEquals("Proverb IDs must be unique", proverbs.size, uniqueProverbIds.size)

        val uniqueProverbOromo = proverbs.map { it.oromo.trim().lowercase() }.toSet()
        assertEquals("Proverb Oromo texts must be unique", proverbs.size, uniqueProverbOromo.size)

        proverbs.forEach { item ->
            assertTrue("Oromo proverb must not be blank: ${item.id}", item.oromo.isNotBlank())
            assertTrue("English translation must not be blank: ${item.id}", item.english.isNotBlank())
            assertTrue("Oromo meaning must not be blank: ${item.id}", item.meaningOm.isNotBlank())
            assertTrue("English meaning must not be blank: ${item.id}", item.meaningEn.isNotBlank())
        }

        val uniquePhraseIds = phrases.map { it.id }.toSet()
        assertEquals("Phrase IDs must be unique", phrases.size, uniquePhraseIds.size)

        val uniquePhraseOromo = phrases.map { it.oromo.trim().lowercase() }.toSet()
        assertEquals("Phrase Oromo texts must be unique", phrases.size, uniquePhraseOromo.size)

        phrases.forEach { item ->
            assertTrue("Oromo phrase must not be blank: ${item.id}", item.oromo.isNotBlank())
            assertTrue("English phrase must not be blank: ${item.id}", item.english.isNotBlank())
            assertTrue("Pronunciation tip must not be blank: ${item.id}", item.pronunciationTip.isNotBlank())
        }
    }

    @Test
    fun `proverbs of the day card displays proverb with english translation and randomizes`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val viewModel = QubeeViewModel(app)

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
                    onNavigateToPhrases = {}
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            org.robolectric.shadows.ShadowLooper.idleMainLooper()
            viewModel.uiState.value.allProverbs.size >= 1000
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("proverb_of_day_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("featured_proverb_oromo").assertIsDisplayed()
        composeTestRule.onNodeWithTag("featured_proverb_english").assertIsDisplayed()

        val initialProverbId = viewModel.uiState.value.featuredProverb.id
        composeTestRule.onNodeWithTag("random_proverb_button")
            .performScrollTo()
            .assertIsDisplayed()
            .performClick()
        composeTestRule.waitForIdle()

        val randomizedProverbId = viewModel.uiState.value.featuredProverb.id
        assertNotEquals(initialProverbId, randomizedProverbId)

        // Verify search across the 550 proverbs works
        composeTestRule.onNodeWithTag("home_lazy_column")
            .performScrollToNode(androidx.compose.ui.test.hasTestTag("proverb_search_input"))
        composeTestRule.onNodeWithTag("proverb_search_input").performTextInput("Mixiin")
        composeTestRule.waitForIdle()
        assertTrue(viewModel.uiState.value.filteredProverbs.any { it.id == "p9" })
        composeTestRule.onNodeWithTag("home_lazy_column")
            .performScrollToNode(androidx.compose.ui.test.hasTestTag("proverb_item_p9"))
        composeTestRule.onNodeWithText("United ants can tie up an elephant.", substring = true).assertIsDisplayed()
    }
}
