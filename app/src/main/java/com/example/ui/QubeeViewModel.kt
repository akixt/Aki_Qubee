package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppLanguage
import com.example.data.AppPreferencesRepository
import com.example.data.OromoContent
import com.example.data.PhraseCategory
import com.example.data.PhraseItem
import com.example.data.ProverbCategory
import com.example.data.ProverbItem
import com.example.data.QubeeCategory
import com.example.data.QubeeLetter
import com.example.data.QuizProgress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

data class QubeeUiState(
    val language: AppLanguage = AppLanguage.OROMO,
    val darkThemeOverride: Boolean? = null,
    val ttsSpeed: Float = 0.88f,
    val favoriteProverbIds: Set<String> = emptySet(),
    val favoritePhraseIds: Set<String> = emptySet(),
    val quizProgress: QuizProgress = QuizProgress(),
    val allProverbs: List<ProverbItem> = OromoContent.proverbs,
    val allPhrases: List<PhraseItem> = OromoContent.phrases,
    val featuredProverbIndex: Int = 0,
    val proverbSearchQuery: String = "",
    val proverbFilter: ProverbCategory? = null,
    val qubeeFilter: QubeeCategory? = null,
    val selectedLetter: QubeeLetter = OromoContent.qubeeLetters.first(),
    val phraseSearchQuery: String = "",
    val phraseFilter: PhraseCategory? = null
) {
    val featuredProverb: ProverbItem
        get() = allProverbs[featuredProverbIndex.coerceIn(allProverbs.indices)]

    val favoriteProverbs: List<ProverbItem>
        get() = allProverbs.filter { it.id in favoriteProverbIds }

    val favoritePhrases: List<PhraseItem>
        get() = allPhrases.filter { it.id in favoritePhraseIds }

    val filteredProverbs: List<ProverbItem>
        get() {
            val query = proverbSearchQuery.trim().lowercase()
            return allProverbs.filter { item ->
                val matchesCategory = proverbFilter == null || item.category == proverbFilter
                val matchesQuery = query.isEmpty() ||
                    item.oromo.lowercase().contains(query) ||
                    item.english.lowercase().contains(query) ||
                    item.meaningOm.lowercase().contains(query) ||
                    item.meaningEn.lowercase().contains(query)
                matchesCategory && matchesQuery
            }
        }

    val filteredLetters: List<QubeeLetter>
        get() = if (qubeeFilter == null) {
            OromoContent.qubeeLetters
        } else {
            OromoContent.qubeeLetters.filter { it.category == qubeeFilter }
        }

    val filteredPhrases: List<PhraseItem>
        get() {
            val query = phraseSearchQuery.trim().lowercase()
            return allPhrases.filter { item ->
                val matchesCategory = phraseFilter == null || item.category == phraseFilter
                val catEn = phraseCategoryLabel(item.category, isOm = false).lowercase()
                val catOm = phraseCategoryLabel(item.category, isOm = true).lowercase()
                val matchesQuery = query.isEmpty() ||
                    item.oromo.lowercase().contains(query) ||
                    item.english.lowercase().contains(query) ||
                    item.pronunciationTip.lowercase().contains(query) ||
                    catEn.contains(query) ||
                    catOm.contains(query)
                matchesCategory && matchesQuery
            }
        }
}

private data class LocalFilterState(
    val allProverbs: List<ProverbItem> = OromoContent.proverbs,
    val allPhrases: List<PhraseItem> = OromoContent.phrases,
    val featuredProverbIndex: Int = Random.nextInt(OromoContent.proverbs.size),
    val proverbSearchQuery: String = "",
    val proverbFilter: ProverbCategory? = null,
    val qubeeFilter: QubeeCategory? = null,
    val selectedLetter: QubeeLetter = OromoContent.qubeeLetters.first(),
    val phraseSearchQuery: String = "",
    val phraseFilter: PhraseCategory? = null
)

class QubeeViewModel(application: Application) : AndroidViewModel(application) {

    private val prefsRepo = AppPreferencesRepository(application.applicationContext)
    private val localState = MutableStateFlow(LocalFilterState())

    init {
        viewModelScope.launch {
            val (loadedProverbs, loadedPhrases) = withContext(Dispatchers.IO) {
                val appContext = getApplication<Application>().applicationContext
                OromoContent.loadProverbs(appContext) to OromoContent.loadPhrases(appContext)
            }
            localState.value = localState.value.copy(
                allProverbs = if (loadedProverbs.isNotEmpty()) loadedProverbs else localState.value.allProverbs,
                allPhrases = if (loadedPhrases.isNotEmpty()) loadedPhrases else localState.value.allPhrases,
                featuredProverbIndex = if (loadedProverbs.isNotEmpty()) Random.nextInt(loadedProverbs.size) else localState.value.featuredProverbIndex
            )
        }
    }

    val uiState: StateFlow<QubeeUiState> = combine(
        prefsRepo.languageFlow,
        prefsRepo.darkThemeFlow,
        prefsRepo.ttsSpeedFlow,
        prefsRepo.favoriteProverbsFlow,
        prefsRepo.favoritePhrasesFlow,
        prefsRepo.quizProgressFlow,
        localState
    ) { values ->
        val language = values[0] as AppLanguage
        val darkOverride = values[1] as Boolean?
        val ttsSpeed = values[2] as Float
        @Suppress("UNCHECKED_CAST")
        val favProverbs = values[3] as Set<String>
        @Suppress("UNCHECKED_CAST")
        val favPhrases = values[4] as Set<String>
        val quizProgress = values[5] as QuizProgress
        val local = values[6] as LocalFilterState
        QubeeUiState(
            language = language,
            darkThemeOverride = darkOverride,
            ttsSpeed = ttsSpeed,
            favoriteProverbIds = favProverbs,
            favoritePhraseIds = favPhrases,
            quizProgress = quizProgress,
            allProverbs = local.allProverbs,
            allPhrases = local.allPhrases,
            featuredProverbIndex = local.featuredProverbIndex,
            proverbSearchQuery = local.proverbSearchQuery,
            proverbFilter = local.proverbFilter,
            qubeeFilter = local.qubeeFilter,
            selectedLetter = local.selectedLetter,
            phraseSearchQuery = local.phraseSearchQuery,
            phraseFilter = local.phraseFilter
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = QubeeUiState(featuredProverbIndex = localState.value.featuredProverbIndex)
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

    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch {
            prefsRepo.setLanguage(language)
        }
    }

    fun toggleDarkTheme(currentIsDark: Boolean) {
        viewModelScope.launch {
            prefsRepo.setDarkTheme(!currentIsDark)
        }
    }

    fun setDarkThemeOverride(darkTheme: Boolean?) {
        viewModelScope.launch {
            prefsRepo.setDarkTheme(darkTheme)
        }
    }

    fun setTtsSpeed(speed: Float) {
        viewModelScope.launch {
            prefsRepo.setTtsSpeed(speed)
        }
    }

    fun toggleFavoriteProverb(id: String) {
        viewModelScope.launch {
            prefsRepo.toggleFavoriteProverb(id)
        }
    }

    fun toggleFavoritePhrase(id: String) {
        viewModelScope.launch {
            prefsRepo.toggleFavoritePhrase(id)
        }
    }

    fun clearAllFavorites() {
        viewModelScope.launch {
            prefsRepo.clearAllFavorites()
        }
    }

    fun recordQuizAnswer(isCorrect: Boolean, markDailyCompleted: Boolean = true) {
        val completedDate = if (markDailyCompleted) {
            SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        } else {
            null
        }
        viewModelScope.launch {
            prefsRepo.recordQuizAnswer(isCorrect = isCorrect, completedDate = completedDate)
        }
    }

    fun randomProverb() {
        val current = localState.value
        val size = current.allProverbs.size
        if (size <= 1) return
        var nextIndex = Random.nextInt(size)
        if (nextIndex == current.featuredProverbIndex) {
            nextIndex = (nextIndex + 1) % size
        }
        localState.value = current.copy(featuredProverbIndex = nextIndex)
    }

    fun nextProverb() {
        val current = localState.value
        if (current.allProverbs.isEmpty()) return
        localState.value = current.copy(
            featuredProverbIndex = (current.featuredProverbIndex + 1) % current.allProverbs.size
        )
    }

    fun selectProverb(index: Int) {
        val current = localState.value
        if (index in current.allProverbs.indices) {
            localState.value = current.copy(featuredProverbIndex = index)
        }
    }

    fun selectProverbById(id: String) {
        val current = localState.value
        val index = current.allProverbs.indexOfFirst { it.id == id }
        if (index >= 0) {
            localState.value = current.copy(featuredProverbIndex = index)
        }
    }

    fun updateProverbSearch(query: String) {
        localState.value = localState.value.copy(proverbSearchQuery = query)
    }

    fun setProverbFilter(category: ProverbCategory?) {
        localState.value = localState.value.copy(proverbFilter = category)
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
