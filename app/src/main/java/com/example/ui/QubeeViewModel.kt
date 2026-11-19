package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AudioLearningManager
import com.example.audio.AudioPlaybackState
import com.example.data.AccessibilityAndGoalPrefs
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
import com.example.data.VocabularyCategory
import com.example.data.VocabularyItem
import com.example.data.local.DailyPointsSummary
import com.example.data.local.LearningHistoryEntity
import com.example.data.local.LearningRepository
import com.example.data.local.MasteredItemEntity
import com.example.data.local.PointsEventEntity
import com.example.data.local.QuizAttemptEntity
import com.example.data.local.SearchHistoryEntity
import com.example.data.local.UserProgressEntity
import com.example.firebase.AiModelState
import com.example.firebase.CloudSyncRepository
import com.example.firebase.PronunciationAiRepository
import com.example.firebase.PronunciationComparisonResult
import com.example.notifications.DailyReminderScheduler
import com.google.firebase.FirebaseApp
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

data class SpeakingTarget(
    val targetOromo: String = OromoContent.qubeeLetters.first().exampleWord,
    val targetEnglish: String = OromoContent.qubeeLetters.first().exampleMeaningEn,
    val phoneticTip: String = OromoContent.qubeeLetters.first().soundGuideEn
)

data class CloudSyncUiState(
    val isSignedIn: Boolean = false,
    val displayName: String? = null,
    val email: String? = null,
    val isSyncing: Boolean = false,
    val statusMessage: String? = null
)

data class QubeeUiState(
    val language: AppLanguage = AppLanguage.OROMO,
    val darkThemeOverride: Boolean? = null,
    val ttsSpeed: Float = 0.88f,
    val textScale: Float = 1.0f,
    val dailyGoalPoints: Int = 50,
    val remindersEnabled: Boolean = false,
    val cloudSyncEnabled: Boolean = false,
    val favoriteProverbIds: Set<String> = emptySet(),
    val favoritePhraseIds: Set<String> = emptySet(),
    val favoriteVocabIds: Set<String> = emptySet(),
    val quizProgress: QuizProgress = QuizProgress(),
    val allProverbs: List<ProverbItem> = OromoContent.proverbs,
    val allPhrases: List<PhraseItem> = OromoContent.phrases,
    val allVocabulary: List<VocabularyItem> = OromoContent.vocabularySeed,
    val featuredProverbIndex: Int = 0,
    val proverbSearchQuery: String = "",
    val proverbFilter: ProverbCategory? = null,
    val qubeeFilter: QubeeCategory? = null,
    val selectedLetter: QubeeLetter = OromoContent.qubeeLetters.first(),
    val phraseSearchQuery: String = "",
    val phraseFilter: PhraseCategory? = null,
    val vocabSearchQuery: String = "",
    val vocabFilter: VocabularyCategory? = null,
    val vocabLetterFilter: String? = null,
    val unifiedSearchQuery: String = "",
    val userProgress: UserProgressEntity = UserProgressEntity(),
    val recentPointsEvents: List<PointsEventEntity> = emptyList(),
    val weeklyPoints: List<DailyPointsSummary> = emptyList(),
    val monthlyPoints: List<DailyPointsSummary> = emptyList(),
    val learningHistory: List<LearningHistoryEntity> = emptyList(),
    val quizAttempts: List<QuizAttemptEntity> = emptyList(),
    val unlockedAchievementIds: Set<String> = emptySet(),
    val masteredItemKeys: Set<String> = emptySet(),
    val searchHistory: List<SearchHistoryEntity> = emptyList(),
    val audioState: AudioPlaybackState = AudioPlaybackState(),
    val speakingTarget: SpeakingTarget = SpeakingTarget(),
    val aiComparisonState: AiModelState<PronunciationComparisonResult> = AiModelState.Idle,
    val aiModelAudioState: AiModelState<Unit> = AiModelState.Idle,
    val cloudSyncState: CloudSyncUiState = CloudSyncUiState(),
    val lastPointsToastMessage: String? = null
) {
    val featuredProverb: ProverbItem
        get() = allProverbs[featuredProverbIndex.coerceIn(allProverbs.indices)]

    val favoriteProverbs: List<ProverbItem>
        get() = allProverbs.filter { it.id in favoriteProverbIds }

    val favoritePhrases: List<PhraseItem>
        get() = allPhrases.filter { it.id in favoritePhraseIds }

    val favoriteVocabulary: List<VocabularyItem>
        get() = allVocabulary.filter { it.id in favoriteVocabIds }

    val masteredQubeeCount: Int
        get() = masteredItemKeys.count { it.startsWith("QUBEE_") }

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

    val filteredVocabulary: List<VocabularyItem>
        get() {
            val query = vocabSearchQuery.trim().lowercase()
            val letterPrefix = vocabLetterFilter?.lowercase()
            return allVocabulary.filter { item ->
                val matchesCategory = vocabFilter == null || item.category == vocabFilter
                val matchesLetter = letterPrefix == null || item.oromo.lowercase().startsWith(letterPrefix)
                val matchesQuery = query.isEmpty() ||
                    item.oromo.lowercase().contains(query) ||
                    item.english.lowercase().contains(query) ||
                    item.exampleOm.lowercase().contains(query) ||
                    item.exampleEn.lowercase().contains(query)
                matchesCategory && matchesLetter && matchesQuery
            }
        }

    val recentlyViewedProverbs: List<ProverbItem>
        get() {
            val recentIds = learningHistory
                .filter { it.module == "PROVERB" }
                .map { it.itemId }
                .take(8)
            val byId = allProverbs.associateBy { it.id }
            return recentIds.mapNotNull { byId[it] }
        }

    val recentlyViewedVocab: List<VocabularyItem>
        get() {
            val recentIds = learningHistory
                .filter { it.module == "VOCAB" }
                .map { it.itemId }
                .take(8)
            val byId = allVocabulary.associateBy { it.id }
            return recentIds.mapNotNull { byId[it] }
        }
}

private data class LocalFilterState(
    val allProverbs: List<ProverbItem> = OromoContent.proverbs,
    val allPhrases: List<PhraseItem> = OromoContent.phrases,
    val allVocabulary: List<VocabularyItem> = OromoContent.vocabularySeed,
    val featuredProverbIndex: Int = Random.nextInt(OromoContent.proverbs.size),
    val proverbSearchQuery: String = "",
    val proverbFilter: ProverbCategory? = null,
    val qubeeFilter: QubeeCategory? = null,
    val selectedLetter: QubeeLetter = OromoContent.qubeeLetters.first(),
    val phraseSearchQuery: String = "",
    val phraseFilter: PhraseCategory? = null,
    val vocabSearchQuery: String = "",
    val vocabFilter: VocabularyCategory? = null,
    val vocabLetterFilter: String? = null,
    val unifiedSearchQuery: String = ""
)

private data class PrefsBundle(
    val language: AppLanguage,
    val darkOverride: Boolean?,
    val ttsSpeed: Float,
    val favProverbs: Set<String>,
    val favPhrases: Set<String>,
    val favVocab: Set<String>,
    val quizProgress: QuizProgress,
    val accessibility: AccessibilityAndGoalPrefs
)

private data class RoomBundle(
    val progress: UserProgressEntity = UserProgressEntity(),
    val recentPoints: List<PointsEventEntity> = emptyList(),
    val weeklyPoints: List<DailyPointsSummary> = emptyList(),
    val monthlyPoints: List<DailyPointsSummary> = emptyList(),
    val history: List<LearningHistoryEntity> = emptyList(),
    val quizAttempts: List<QuizAttemptEntity> = emptyList(),
    val achievements: Set<String> = emptySet(),
    val masteredKeys: Set<String> = emptySet(),
    val searchHistory: List<SearchHistoryEntity> = emptyList()
)

private data class LiveAiCloudState(
    val speakingTarget: SpeakingTarget = SpeakingTarget(),
    val aiComparisonState: AiModelState<PronunciationComparisonResult> = AiModelState.Idle,
    val aiModelAudioState: AiModelState<Unit> = AiModelState.Idle,
    val cloudSyncState: CloudSyncUiState = CloudSyncUiState(),
    val lastPointsToastMessage: String? = null
)

class QubeeViewModel(application: Application) : AndroidViewModel(application) {

    private val prefsRepo = AppPreferencesRepository(application.applicationContext)
    private val learningRepo = LearningRepository(application.applicationContext, prefsRepo)
    private val audioManager: AudioLearningManager? = runCatching {
        AudioLearningManager(application.applicationContext)
    }.getOrNull()
    private val pronunciationAiRepo: PronunciationAiRepository? = runCatching {
        if (FirebaseApp.getApps(application.applicationContext).isNotEmpty()) {
            PronunciationAiRepository()
        } else {
            null
        }
    }.getOrNull()
    private val cloudSyncRepo: CloudSyncRepository? = runCatching {
        if (FirebaseApp.getApps(application.applicationContext).isNotEmpty()) {
            CloudSyncRepository(application.applicationContext)
        } else {
            null
        }
    }.getOrNull()

    private val localState = MutableStateFlow(LocalFilterState())
    private val liveAiCloudState = MutableStateFlow(LiveAiCloudState())

    init {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                learningRepo.ensureInitializedAndMigrate()
                val appContext = getApplication<Application>().applicationContext
                val loadedProverbs = OromoContent.loadProverbs(appContext)
                val loadedPhrases = OromoContent.loadPhrases(appContext)
                val loadedVocab = OromoContent.loadVocabulary(appContext)
                localState.value = localState.value.copy(
                    allProverbs = if (loadedProverbs.isNotEmpty()) loadedProverbs else localState.value.allProverbs,
                    allPhrases = if (loadedPhrases.isNotEmpty()) loadedPhrases else localState.value.allPhrases,
                    allVocabulary = if (loadedVocab.isNotEmpty()) loadedVocab else localState.value.allVocabulary,
                    featuredProverbIndex = if (loadedProverbs.isNotEmpty()) Random.nextInt(loadedProverbs.size) else localState.value.featuredProverbIndex
                )
            }
        }

        cloudSyncRepo?.currentUser?.let { user ->
            liveAiCloudState.value = liveAiCloudState.value.copy(
                cloudSyncState = CloudSyncUiState(
                    isSignedIn = true,
                    displayName = user.displayName,
                    email = user.email
                )
            )
        }
    }

    private val corePrefsFlow = combine(
        prefsRepo.languageFlow,
        prefsRepo.darkThemeFlow,
        prefsRepo.ttsSpeedFlow,
        prefsRepo.favoriteProverbsFlow,
        prefsRepo.favoritePhrasesFlow
    ) { lang, dark, speed, favProv, favPhr ->
        Tuple5(lang, dark, speed, favProv, favPhr)
    }

    private val prefsBundleFlow = combine(
        corePrefsFlow,
        prefsRepo.favoriteVocabFlow,
        prefsRepo.quizProgressFlow,
        prefsRepo.accessibilityPrefsFlow
    ) { core, favVocab, quiz, acc ->
        PrefsBundle(
            language = core.a,
            darkOverride = core.b,
            ttsSpeed = core.c,
            favProverbs = core.d,
            favPhrases = core.e,
            favVocab = favVocab,
            quizProgress = quiz,
            accessibility = acc
        )
    }

    private val roomPartAFlow = combine(
        learningRepo.userProgressFlow,
        learningRepo.recentPointsEventsFlow,
        learningRepo.observeWeeklyPoints(),
        learningRepo.observeMonthlyPoints(),
        learningRepo.learningHistoryFlow
    ) { prog, events, week, month, hist ->
        Tuple5(prog, events, week, month, hist)
    }

    private val roomBundleFlow = combine(
        roomPartAFlow,
        learningRepo.quizAttemptsFlow,
        learningRepo.unlockedAchievementIdsFlow,
        learningRepo.masteredItemsFlow,
        learningRepo.searchHistoryFlow
    ) { partA, attempts, achievements, mastered, searches ->
        RoomBundle(
            progress = partA.a,
            recentPoints = partA.b,
            weeklyPoints = partA.c,
            monthlyPoints = partA.d,
            history = partA.e,
            quizAttempts = attempts,
            achievements = achievements,
            masteredKeys = mastered.map { it.itemKey }.toSet(),
            searchHistory = searches
        )
    }

    private val audioStateFlow: StateFlow<AudioPlaybackState> =
        audioManager?.state ?: MutableStateFlow(AudioPlaybackState())

    val uiState: StateFlow<QubeeUiState> = combine(
        prefsBundleFlow,
        roomBundleFlow,
        localState,
        audioStateFlow,
        liveAiCloudState
    ) { prefs, room, local, audio, live ->
        QubeeUiState(
            language = prefs.language,
            darkThemeOverride = prefs.darkOverride,
            ttsSpeed = prefs.ttsSpeed,
            textScale = prefs.accessibility.textScale,
            dailyGoalPoints = prefs.accessibility.dailyGoalPoints,
            remindersEnabled = prefs.accessibility.remindersEnabled,
            cloudSyncEnabled = prefs.accessibility.cloudSyncEnabled,
            favoriteProverbIds = prefs.favProverbs,
            favoritePhraseIds = prefs.favPhrases,
            favoriteVocabIds = prefs.favVocab,
            quizProgress = prefs.quizProgress,
            allProverbs = local.allProverbs,
            allPhrases = local.allPhrases,
            allVocabulary = local.allVocabulary,
            featuredProverbIndex = local.featuredProverbIndex,
            proverbSearchQuery = local.proverbSearchQuery,
            proverbFilter = local.proverbFilter,
            qubeeFilter = local.qubeeFilter,
            selectedLetter = local.selectedLetter,
            phraseSearchQuery = local.phraseSearchQuery,
            phraseFilter = local.phraseFilter,
            vocabSearchQuery = local.vocabSearchQuery,
            vocabFilter = local.vocabFilter,
            vocabLetterFilter = local.vocabLetterFilter,
            unifiedSearchQuery = local.unifiedSearchQuery,
            userProgress = room.progress,
            recentPointsEvents = room.recentPoints,
            weeklyPoints = room.weeklyPoints,
            monthlyPoints = room.monthlyPoints,
            learningHistory = room.history,
            quizAttempts = room.quizAttempts,
            unlockedAchievementIds = room.achievements,
            masteredItemKeys = room.masteredKeys,
            searchHistory = room.searchHistory,
            audioState = audio,
            speakingTarget = live.speakingTarget,
            aiComparisonState = live.aiComparisonState,
            aiModelAudioState = live.aiModelAudioState,
            cloudSyncState = live.cloudSyncState,
            lastPointsToastMessage = live.lastPointsToastMessage
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
        setLanguage(next)
    }

    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch {
            prefsRepo.setLanguage(language)
        }
    }

    fun toggleDarkTheme(currentIsDark: Boolean) {
        setDarkThemeOverride(!currentIsDark)
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

    fun setRepeatCount(count: Int) {
        audioManager?.setRepeatCount(count)
    }

    fun setTextScale(scale: Float) {
        viewModelScope.launch {
            prefsRepo.setTextScale(scale)
        }
    }

    fun setDailyGoalPoints(goal: Int) {
        viewModelScope.launch {
            prefsRepo.setDailyGoalPoints(goal)
            learningRepo.syncDailyGoal(goal)
        }
    }

    fun setRemindersEnabled(enabled: Boolean) {
        viewModelScope.launch {
            prefsRepo.setRemindersEnabled(enabled)
            runCatching {
                DailyReminderScheduler.scheduleDailyReminder(getApplication(), enabled)
            }
        }
    }

    fun toggleFavoriteProverb(id: String) {
        viewModelScope.launch {
            val wasFav = id in uiState.value.favoriteProverbIds
            prefsRepo.toggleFavoriteProverb(id)
            if (cloudSyncRepo?.currentUser != null) {
                cloudSyncRepo.syncFavorite("PROVERB", id, !wasFav)
            }
        }
    }

    fun toggleFavoritePhrase(id: String) {
        viewModelScope.launch {
            val wasFav = id in uiState.value.favoritePhraseIds
            prefsRepo.toggleFavoritePhrase(id)
            if (cloudSyncRepo?.currentUser != null) {
                cloudSyncRepo.syncFavorite("PHRASE", id, !wasFav)
            }
        }
    }

    fun toggleFavoriteVocab(id: String) {
        viewModelScope.launch {
            val wasFav = id in uiState.value.favoriteVocabIds
            prefsRepo.toggleFavoriteVocab(id)
            if (cloudSyncRepo?.currentUser != null) {
                cloudSyncRepo.syncFavorite("VOCAB", id, !wasFav)
            }
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
            val earned = if (isCorrect) 10 else 2
            val gained = learningRepo.awardPoints(
                source = "QUIZ",
                itemId = "q_${System.currentTimeMillis()}",
                label = if (isCorrect) "Quiz correct answer" else "Quiz practice attempt",
                points = earned,
                uniquePerItem = false,
                studySecondsDelta = 15L
            )
            if (gained > 0) {
                emitPointsFeedback("+$gained XP")
            }
        }
    }

    fun recordQuizSession(mode: String, score: Int, totalQuestions: Int) {
        viewModelScope.launch {
            val pointsEarned = (score * 10) + 15
            learningRepo.recordQuizSession(
                mode = mode,
                score = score,
                totalQuestions = totalQuestions,
                pointsEarned = pointsEarned
            )
            learningRepo.checkModuleCountAchievements(
                qubeeCount = uiState.value.masteredQubeeCount,
                vocabCount = uiState.value.learningHistory.count { it.module == "VOCAB" },
                proverbCount = uiState.value.learningHistory.count { it.module == "PROVERB" },
                quizCount = uiState.value.quizAttempts.size + 1
            )
            if (cloudSyncRepo?.currentUser != null) {
                cloudSyncRepo.uploadQuizAttempt(mode, score, totalQuestions, pointsEarned)
            }
            emitPointsFeedback("+$pointsEarned XP")
        }
    }

    fun awardPracticePoints(
        source: String,
        itemId: String,
        label: String,
        points: Int,
        uniquePerItem: Boolean = false
    ) {
        viewModelScope.launch {
            val gained = learningRepo.awardPoints(
                source = source,
                itemId = itemId,
                label = label,
                points = points,
                uniquePerItem = uniquePerItem,
                studySecondsDelta = 20L
            )
            if (gained > 0) {
                learningRepo.recordLearningView(source, itemId, label, "+$gained XP", awardPointsIfNew = 0)
                emitPointsFeedback("+$gained XP")
            }
        }
    }

    fun toggleMasteredItem(module: String, itemId: String, label: String = itemId) {
        viewModelScope.launch {
            val key = "${module}_${itemId}"
            val nextMastered = key !in uiState.value.masteredItemKeys
            learningRepo.setItemMastered(module, itemId, nextMastered, awardPoints = nextMastered)
            if (nextMastered) {
                learningRepo.recordLearningView(module, itemId, label, "Mastered", awardPointsIfNew = 0)
                emitPointsFeedback("+10 XP")
            }
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
        val selected = current.allProverbs[nextIndex]
        localState.value = current.copy(featuredProverbIndex = nextIndex)
        recordProverbDiscovery(selected)
    }

    fun nextProverb() {
        val current = localState.value
        if (current.allProverbs.isEmpty()) return
        val nextIndex = (current.featuredProverbIndex + 1) % current.allProverbs.size
        val selected = current.allProverbs[nextIndex]
        localState.value = current.copy(featuredProverbIndex = nextIndex)
        recordProverbDiscovery(selected)
    }

    fun selectProverb(index: Int) {
        val current = localState.value
        if (index in current.allProverbs.indices) {
            val selected = current.allProverbs[index]
            localState.value = current.copy(featuredProverbIndex = index)
            recordProverbDiscovery(selected)
        }
    }

    fun selectProverbById(id: String) {
        val current = localState.value
        val index = current.allProverbs.indexOfFirst { it.id == id }
        if (index >= 0) {
            val selected = current.allProverbs[index]
            localState.value = current.copy(featuredProverbIndex = index)
            recordProverbDiscovery(selected)
        }
    }

    private fun recordProverbDiscovery(proverb: ProverbItem) {
        viewModelScope.launch {
            val gained = learningRepo.recordLearningView(
                module = "PROVERB",
                itemId = proverb.id,
                title = proverb.oromo,
                subtitle = proverb.english,
                awardPointsIfNew = 5
            )
            if (gained > 0) {
                emitPointsFeedback("+5 XP")
            }
        }
    }

    fun recordVocabViewed(item: VocabularyItem) {
        viewModelScope.launch {
            val gained = learningRepo.recordLearningView(
                module = "VOCAB",
                itemId = item.id,
                title = item.oromo,
                subtitle = item.english,
                awardPointsIfNew = 5
            )
            if (gained > 0) {
                emitPointsFeedback("+5 XP")
            }
        }
    }

    fun recordPhraseViewed(item: PhraseItem) {
        viewModelScope.launch {
            val gained = learningRepo.recordLearningView(
                module = "PHRASE",
                itemId = item.id,
                title = item.oromo,
                subtitle = item.english,
                awardPointsIfNew = 3
            )
            if (gained > 0) {
                emitPointsFeedback("+$gained XP")
            }
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
        viewModelScope.launch {
            val gained = learningRepo.recordLearningView(
                module = "QUBEE",
                itemId = letter.name,
                title = "${letter.symbol} — ${letter.exampleWord}",
                subtitle = letter.exampleMeaningEn,
                awardPointsIfNew = 5
            )
            if (gained > 0) {
                emitPointsFeedback("+5 XP")
            }
        }
    }

    fun updatePhraseSearch(query: String) {
        localState.value = localState.value.copy(phraseSearchQuery = query)
    }

    fun setPhraseFilter(category: PhraseCategory?) {
        localState.value = localState.value.copy(phraseFilter = category)
    }

    fun updateVocabSearch(query: String) {
        localState.value = localState.value.copy(vocabSearchQuery = query)
    }

    fun setVocabFilter(category: VocabularyCategory?) {
        localState.value = localState.value.copy(vocabFilter = category)
    }

    fun setVocabLetterFilter(letterPrefix: String?) {
        localState.value = localState.value.copy(vocabLetterFilter = letterPrefix)
    }

    fun updateUnifiedSearch(query: String) {
        localState.value = localState.value.copy(unifiedSearchQuery = query)
    }

    fun saveSearchQuery(query: String) {
        viewModelScope.launch {
            learningRepo.saveSearchQuery(query)
        }
    }

    fun clearSearchHistory() {
        viewModelScope.launch {
            learningRepo.clearSearchHistory()
        }
    }

    fun speak(text: String, repeatOverride: Int? = null): Boolean {
        val manager = audioManager ?: return false
        return manager.speak(text, speechRate = uiState.value.ttsSpeed, repeatOverride = repeatOverride)
    }

    fun stopSpeaking() {
        audioManager?.stopAllPlayback()
    }

    fun setSpeakingTarget(targetOromo: String, targetEnglish: String, phoneticTip: String) {
        audioManager?.clearRecording()
        liveAiCloudState.value = liveAiCloudState.value.copy(
            speakingTarget = SpeakingTarget(targetOromo, targetEnglish, phoneticTip),
            aiComparisonState = AiModelState.Idle,
            aiModelAudioState = AiModelState.Idle
        )
    }

    fun startRecording(): Boolean {
        liveAiCloudState.value = liveAiCloudState.value.copy(aiComparisonState = AiModelState.Idle)
        return audioManager?.startRecording() ?: false
    }

    fun stopRecording(): Boolean {
        val recorded = audioManager?.stopRecording()
        return recorded != null
    }

    fun playRecordedAudio() {
        audioManager?.playRecordedAudio()
    }

    fun playAiModelPronunciation(targetOromo: String, phoneticGuide: String) {
        val repo = pronunciationAiRepo
        if (repo == null) {
            liveAiCloudState.value = liveAiCloudState.value.copy(
                aiModelAudioState = AiModelState.Error(
                    message = "Cloud AI audio model is unavailable offline. Using device pronunciation fallback.",
                    isOfflineOrAttestation = true
                )
            )
            speak(targetOromo)
            return
        }
        viewModelScope.launch {
            liveAiCloudState.value = liveAiCloudState.value.copy(aiModelAudioState = AiModelState.Loading)
            val result = repo.generateModelPronunciationAudio(targetOromo, phoneticGuide)
            result.onSuccess { pcmBytes ->
                liveAiCloudState.value = liveAiCloudState.value.copy(aiModelAudioState = AiModelState.Success(Unit))
                audioManager?.playAiPcmAudio(pcmBytes, speed = uiState.value.ttsSpeed)
            }.onFailure { err ->
                liveAiCloudState.value = liveAiCloudState.value.copy(
                    aiModelAudioState = AiModelState.Error(
                        message = err.localizedMessage ?: "AI pronunciation model unavailable.",
                        isOfflineOrAttestation = true
                    )
                )
                speak(targetOromo)
            }
        }
    }

    fun comparePronunciationWithAi(
        targetOromo: String,
        englishMeaning: String,
        phoneticTip: String
    ) {
        val manager = audioManager
        if (manager == null) {
            liveAiCloudState.value = liveAiCloudState.value.copy(
                aiComparisonState = AiModelState.Error("Audio manager unavailable on this device.")
            )
            return
        }
        val repo = pronunciationAiRepo
        if (repo == null) {
            liveAiCloudState.value = liveAiCloudState.value.copy(
                aiComparisonState = AiModelState.Error(
                    message = "AI pronunciation comparison requires an online Firebase connection. Replay your own recording above to self-check.",
                    isOfflineOrAttestation = true
                )
            )
            return
        }
        viewModelScope.launch {
            val bytes = manager.readRecordedAudioBytes()
            if (bytes == null || bytes.isEmpty()) {
                liveAiCloudState.value = liveAiCloudState.value.copy(
                    aiComparisonState = AiModelState.Error("Record your voice first before comparing with AI.")
                )
                return@launch
            }
            liveAiCloudState.value = liveAiCloudState.value.copy(aiComparisonState = AiModelState.Loading)
            val res = repo.comparePronunciation(
                audioBytes = bytes,
                targetOromo = targetOromo,
                englishMeaning = englishMeaning,
                phoneticTip = phoneticTip
            )
            res.onSuccess { feedback ->
                liveAiCloudState.value = liveAiCloudState.value.copy(
                    aiComparisonState = AiModelState.Success(feedback)
                )
                val bonus = if (feedback.accuracyScore >= 75) 15 else 8
                val gained = learningRepo.awardPoints(
                    source = "SPEAKING",
                    itemId = "speak_${targetOromo.take(24)}_${System.currentTimeMillis()}",
                    label = targetOromo,
                    points = bonus,
                    uniquePerItem = false,
                    studySecondsDelta = 25L
                )
                if (gained > 0) {
                    emitPointsFeedback("+$gained XP")
                }
            }.onFailure { err ->
                liveAiCloudState.value = liveAiCloudState.value.copy(
                    aiComparisonState = AiModelState.Error(
                        message = err.localizedMessage ?: "Unable to reach AI pronunciation coach right now. You can replay your local recording above.",
                        isOfflineOrAttestation = true
                    )
                )
            }
        }
    }

    fun clearSpeakingFeedback() {
        audioManager?.clearRecording()
        liveAiCloudState.value = liveAiCloudState.value.copy(
            aiComparisonState = AiModelState.Idle,
            aiModelAudioState = AiModelState.Idle
        )
    }

    fun signInWithGoogle(activityContext: Context) {
        val syncRepo = cloudSyncRepo ?: run {
            liveAiCloudState.value = liveAiCloudState.value.copy(
                cloudSyncState = liveAiCloudState.value.cloudSyncState.copy(
                    statusMessage = "Cloud backup requires an active Firebase configuration."
                )
            )
            return
        }
        viewModelScope.launch {
            liveAiCloudState.value = liveAiCloudState.value.copy(
                cloudSyncState = liveAiCloudState.value.cloudSyncState.copy(
                    isSyncing = true,
                    statusMessage = null
                )
            )
            val signInResult = syncRepo.signInWithGoogle(activityContext)
            signInResult.onSuccess { user ->
                prefsRepo.setCloudSyncEnabled(true)
                val backup = syncRepo.pullCloudBackup().getOrNull()
                if (backup != null) {
                    prefsRepo.restoreFavoritesFromCloud(
                        proverbs = backup.favoriteProverbIds,
                        phrases = backup.favoritePhraseIds,
                        vocab = backup.favoriteVocabIds
                    )
                    backup.profile?.let { profile ->
                        learningRepo.mergeCloudProgressIfHigher(
                            totalPoints = profile.totalPoints.toInt(),
                            bestStreak = profile.bestStreak.toInt(),
                            totalLearningSeconds = profile.totalLearningSeconds,
                            unlockedIds = profile.unlockedAchievementIds
                        )
                    }
                }
                liveAiCloudState.value = liveAiCloudState.value.copy(
                    cloudSyncState = CloudSyncUiState(
                        isSignedIn = true,
                        displayName = user.displayName,
                        email = user.email,
                        isSyncing = false,
                        statusMessage = "Signed in & cloud backup synced!"
                    )
                )
            }.onFailure { err ->
                liveAiCloudState.value = liveAiCloudState.value.copy(
                    cloudSyncState = liveAiCloudState.value.cloudSyncState.copy(
                        isSyncing = false,
                        statusMessage = err.localizedMessage ?: "Google Sign-In failed."
                    )
                )
            }
        }
    }

    fun syncCloudBackup() {
        val syncRepo = cloudSyncRepo ?: return
        viewModelScope.launch {
            liveAiCloudState.value = liveAiCloudState.value.copy(
                cloudSyncState = liveAiCloudState.value.cloudSyncState.copy(isSyncing = true)
            )
            val state = uiState.value
            val profileResult = syncRepo.syncUserProfile(
                displayName = state.cloudSyncState.displayName ?: "Aki Learner",
                totalPoints = state.userProgress.totalPoints,
                dailyGoalPoints = state.dailyGoalPoints,
                todayPoints = state.userProgress.todayPoints,
                currentStreak = state.userProgress.currentStreakDays,
                bestStreak = state.userProgress.bestStreakDays,
                totalLearningSeconds = state.userProgress.totalLearningSeconds,
                quizTotalAnswered = state.quizProgress.totalAnswered,
                quizTotalCorrect = state.quizProgress.totalCorrect,
                lastActiveDate = state.userProgress.lastActiveDate,
                unlockedAchievementIds = state.unlockedAchievementIds.toList()
            )
            state.favoriteProverbIds.take(40).forEach { syncRepo.syncFavorite("PROVERB", it, true) }
            state.favoritePhraseIds.take(40).forEach { syncRepo.syncFavorite("PHRASE", it, true) }
            state.favoriteVocabIds.take(40).forEach { syncRepo.syncFavorite("VOCAB", it, true) }

            val msg = if (profileResult.isSuccess) {
                "Cloud backup synced successfully."
            } else {
                profileResult.exceptionOrNull()?.localizedMessage ?: "Cloud sync failed."
            }
            liveAiCloudState.value = liveAiCloudState.value.copy(
                cloudSyncState = liveAiCloudState.value.cloudSyncState.copy(
                    isSyncing = false,
                    statusMessage = msg
                )
            )
        }
    }

    fun signOutCloud() {
        val syncRepo = cloudSyncRepo ?: return
        viewModelScope.launch {
            syncRepo.signOut(getApplication())
            prefsRepo.setCloudSyncEnabled(false)
            liveAiCloudState.value = liveAiCloudState.value.copy(
                cloudSyncState = CloudSyncUiState(
                    isSignedIn = false,
                    statusMessage = "Signed out of cloud backup."
                )
            )
        }
    }

    fun resetAllLearningData() {
        viewModelScope.launch {
            learningRepo.resetAllLocalLearningData()
            learningRepo.ensureInitializedAndMigrate()
            emitPointsFeedback(null)
        }
    }

    fun clearPointsToast() {
        liveAiCloudState.value = liveAiCloudState.value.copy(lastPointsToastMessage = null)
    }

    private fun emitPointsFeedback(msg: String?) {
        liveAiCloudState.value = liveAiCloudState.value.copy(lastPointsToastMessage = msg)
    }

    override fun onCleared() {
        audioManager?.release()
        super.onCleared()
    }
}

private data class Tuple5<A, B, C, D, E>(
    val a: A,
    val b: B,
    val c: C,
    val d: D,
    val e: E
)
