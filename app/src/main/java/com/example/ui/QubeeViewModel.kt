package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppLanguage
import com.example.data.AppPreferencesRepository
import com.example.data.OromoContent
import com.example.data.PhraseCategory
import com.example.data.PhraseItem
import com.example.data.ProverbItem
import com.example.data.QubeeCategory
import com.example.data.QubeeLetter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class QubeeUiState(
    val language: AppLanguage = AppLanguage.OROMO,
    val darkThemeOverride: Boolean? = null,
    val featuredProverbIndex: Int = 0,
    val qubeeFilter: QubeeCategory? = null,
    val selectedLetter: QubeeLetter = OromoContent.qubeeLetters.first(),
    val phraseSearchQuery: String = "",
    val phraseFilter: PhraseCategory? = null
) {
    val featuredProverb: ProverbItem
        get() = OromoContent.proverbs[featuredProverbIndex % OromoContent.proverbs.size]

    val filteredLetters: List<QubeeLetter>
        get() = if (qubeeFilter == null) {
            OromoContent.qubeeLetters
        } else {
            OromoContent.qubeeLetters.filter { it.category == qubeeFilter }
        }

    val filteredPhrases: List<PhraseItem>
        get() {
            val query = phraseSearchQuery.trim().lowercase()
            return OromoContent.phrases.filter { item ->
                val matchesCategory = phraseFilter == null || item.category == phraseFilter
                val matchesQuery = query.isEmpty() ||
                    item.oromo.lowercase().contains(query) ||
                    item.english.lowercase().contains(query) ||
                    item.pronunciationTip.lowercase().contains(query)
                matchesCategory && matchesQuery
            }
        }
}

private data class LocalFilterState(
    val featuredProverbIndex: Int = 0,
    val qubeeFilter: QubeeCategory? = null,
    val selectedLetter: QubeeLetter = OromoContent.qubeeLetters.first(),
    val phraseSearchQuery: String = "",
    val phraseFilter: PhraseCategory? = null
)

class QubeeViewModel(application: Application) : AndroidViewModel(application) {

    private val prefsRepo = AppPreferencesRepository(application.applicationContext)
    private val localState = MutableStateFlow(LocalFilterState())

    val uiState: StateFlow<QubeeUiState> = combine(
        prefsRepo.languageFlow,
        prefsRepo.darkThemeFlow,
        localState
    ) { language, darkOverride, local ->
        QubeeUiState(
            language = language,
            darkThemeOverride = darkOverride,
            featuredProverbIndex = local.featuredProverbIndex,
            qubeeFilter = local.qubeeFilter,
            selectedLetter = local.selectedLetter,
            phraseSearchQuery = local.phraseSearchQuery,
            phraseFilter = local.phraseFilter
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = QubeeUiState()
    )

    fun toggleLanguage() {
        val next = if (uiState.value.language == AppLanguage.OROMO) {
            AppLanguage.ENGLISH
        } else {
            AppLanguage.OROMO
        }
        viewModelScope.launch {
            prefsRepo.setLanguage(next)
        }
    }

    fun toggleDarkTheme(currentIsDark: Boolean) {
        viewModelScope.launch {
            prefsRepo.setDarkTheme(!currentIsDark)
        }
    }

    fun nextProverb() {
        val current = localState.value
        localState.value = current.copy(
            featuredProverbIndex = (current.featuredProverbIndex + 1) % OromoContent.proverbs.size
        )
    }

    fun selectProverb(index: Int) {
        if (index in OromoContent.proverbs.indices) {
            localState.value = localState.value.copy(featuredProverbIndex = index)
        }
    }

    fun setQubeeFilter(category: QubeeCategory?) {
        val updatedLetters = if (category == null) {
            OromoContent.qubeeLetters
        } else {
            OromoContent.qubeeLetters.filter { it.category == category }
        }
        val currentSelected = localState.value.selectedLetter
        val nextSelected = if (currentSelected in updatedLetters) {
            currentSelected
        } else {
            updatedLetters.firstOrNull() ?: currentSelected
        }
        localState.value = localState.value.copy(
            qubeeFilter = category,
            selectedLetter = nextSelected
        )
    }

    fun selectLetter(letter: QubeeLetter) {
        localState.value = localState.value.copy(selectedLetter = letter)
    }

    fun updatePhraseSearch(query: String) {
        localState.value = localState.value.copy(phraseSearchQuery = query)
    }

    fun setPhraseFilter(category: PhraseCategory?) {
        localState.value = localState.value.copy(phraseFilter = category)
    }
}
