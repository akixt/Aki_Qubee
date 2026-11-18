package com.example

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import com.example.data.OromoContent
import com.example.data.PhraseCategory
import com.example.data.QubeeLetter
import com.example.ui.DailyPhrasesHomeSection
import com.example.ui.QubeeChartModal
import com.example.ui.theme.QubeeTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DailyPhrasesAndQubeeChartTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `daily phrases section switches contexts and displays english translations`() {
        var browsedCategory: PhraseCategory? = null
        composeTestRule.setContent {
            QubeeTheme {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    DailyPhrasesHomeSection(
                        isOm = false,
                        favoritePhraseIds = emptySet(),
                        onToggleFavoritePhrase = {},
                        onSpeakPhrase = {},
                        onCopyPhrase = {},
                        onBrowseCategory = { browsedCategory = it },
                        onBrowseAllPhrases = {}
                    )
                }
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("daily_phrases_home_section").assertIsDisplayed()
        composeTestRule.onNodeWithText("How are you? (singular)").assertIsDisplayed()

        // Switch context to Market
        composeTestRule.onNodeWithTag("home_phrase_chip_market").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("How much does this cost?").assertIsDisplayed()

        // Switch context to Travel
        composeTestRule.onNodeWithTag("home_phrase_chip_travel").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Where is the road to ...?").assertIsDisplayed()

        composeTestRule.onNodeWithTag("open_category_phrases_button")
            .performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()
        assertEquals(PhraseCategory.TRAVEL, browsedCategory)
    }

    @Test
    fun `interactive qubee chart modal plays tts pronunciation on letter click`() {
        val spokenPrompts = mutableListOf<String>()
        var selected by mutableStateOf(OromoContent.qubeeLetters.first())

        composeTestRule.setContent {
            QubeeTheme {
                QubeeChartModal(
                    selectedLetter = selected,
                    isOm = false,
                    onLetterClick = { letter: QubeeLetter ->
                        selected = letter
                        spokenPrompts.add(letter.ttsPronunciation)
                    },
                    onReplaySound = { letter ->
                        spokenPrompts.add(letter.ttsPronunciation)
                    },
                    onDismiss = {}
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("qubee_chart_modal").assertIsDisplayed()

        // Click vowel 'A'
        composeTestRule.onNodeWithTag("chart_letter_a").performClick()
        composeTestRule.waitForIdle()
        assertTrue(spokenPrompts.contains("A. Abbaa"))

        // Scroll grid to digraph 'Dha' and click
        composeTestRule.onNodeWithTag("qubee_chart_grid")
            .performScrollToNode(hasTestTag("chart_letter_dha"))
        composeTestRule.onNodeWithTag("chart_letter_dha").performClick()
        composeTestRule.waitForIdle()
        assertEquals("Dha", selected.name)
        assertTrue(spokenPrompts.contains("Dha. Dhugaa"))
    }
}
