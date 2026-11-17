package com.example

import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.QubeeApp
import com.example.ui.QubeeViewModel
import com.example.ui.theme.QubeeTheme
import java.util.Locale

class MainActivity : ComponentActivity(), TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var ttsReady: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        tts = TextToSpeech(this, this)

        setContent {
            val viewModel: QubeeViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val systemDark = isSystemInDarkTheme()
            val isDarkTheme = uiState.darkThemeOverride ?: systemDark

            QubeeTheme(darkTheme = isDarkTheme) {
                QubeeApp(
                    uiState = uiState,
                    isDarkTheme = isDarkTheme,
                    onToggleLanguage = viewModel::toggleLanguage,
                    onToggleTheme = { viewModel.toggleDarkTheme(isDarkTheme) },
                    onNextProverb = viewModel::nextProverb,
                    onSelectProverb = viewModel::selectProverb,
                    onSelectQubeeFilter = viewModel::setQubeeFilter,
                    onSelectLetter = viewModel::selectLetter,
                    onSearchPhrases = viewModel::updatePhraseSearch,
                    onSelectPhraseCategory = viewModel::setPhraseFilter,
                    onSpeakText = ::speakOut
                )
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val oromoLocale = Locale.Builder().setLanguage("om").build()
            val result = tts?.setLanguage(oromoLocale) ?: TextToSpeech.LANG_MISSING_DATA
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.setLanguage(Locale.US)
            }
            tts?.setSpeechRate(0.88f)
            ttsReady = true
        } else {
            ttsReady = false
        }
    }

    private fun speakOut(text: String): Boolean {
        val engine = tts ?: return false
        if (!ttsReady) return false
        engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, text.hashCode().toString())
        return true
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        super.onDestroy()
    }
}
