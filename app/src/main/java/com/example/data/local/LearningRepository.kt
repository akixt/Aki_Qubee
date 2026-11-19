package com.example.data.local

import android.content.Context
import com.example.data.AppPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class AchievementId(
    val id: String,
    val titleOm: String,
    val titleEn: String,
    val descOm: String,
    val descEn: String
) {
    BEGINNER(
        id = "beginner",
        titleOm = "Jalqabaa Cimaa",
        titleEn = "First Steps",
        descOm = "Qabxii jalqabaa (25+ XP) argatteetta.",
        descEn = "Earned your first 25+ User Points."
    ),
    ALPHABET_EXPLORER(
        id = "alphabet_master",
        titleOm = "Ogeessa Qubee",
        titleEn = "Qubee Explorer",
        descOm = "Qubeewwan 10+ baratteetta ykn qormaatteetta.",
        descEn = "Learned or mastered 10+ Qubee letters."
    ),
    VOCAB_BUILDER(
        id = "vocab_builder",
        titleOm = "Kuusaa Jechootaa",
        titleEn = "Word Collector",
        descOm = "Jechoota haaraa 15+ qoratteetta.",
        descEn = "Explored 15+ vocabulary words."
    ),
    PROVERB_KEEPER(
        id = "proverb_keeper",
        titleOm = "Abbaa Mammaaksaa",
        titleEn = "Wisdom Seeker",
        descOm = "Mammaaksa haaraa 10+ dubbisteetta.",
        descEn = "Read and discovered 10+ new proverbs."
    ),
    LISTENER_PRO(
        id = "listening_star",
        titleOm = "Dhaggeeffataa Cimaa",
        titleEn = "Active Listener",
        descOm = "Shaakala dhaggeeffannaa fi dubbii 3+ xumurteetta.",
        descEn = "Completed 3+ listening or speaking sessions."
    ),
    QUIZ_CHAMPION(
        id = "quiz_champion",
        titleOm = "Mo'ataa Qormaataa",
        titleEn = "Quiz Champion",
        descOm = "Qormaatawwan 3+ milkiin xumurteetta.",
        descEn = "Completed 3+ quiz sessions."
    )
}

class LearningRepository(
    context: Context,
    private val prefsRepo: AppPreferencesRepository
) {
    private val dao = AppDatabase.getInstance(context).learningDao()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    val userProgressFlow: Flow<UserProgressEntity> = dao.observeUserProgress().map {
        it ?: UserProgressEntity()
    }
    val recentPointsEventsFlow: Flow<List<PointsEventEntity>> = dao.observeRecentPointsEvents()
    val learningHistoryFlow: Flow<List<LearningHistoryEntity>> = dao.observeLearningHistory()
    val quizAttemptsFlow: Flow<List<QuizAttemptEntity>> = dao.observeQuizAttempts()
    val unlockedAchievementIdsFlow: Flow<Set<String>> = dao.observeAchievements().map { list ->
        list.map { it.achievementId }.toSet()
    }
    val searchHistoryFlow: Flow<List<SearchHistoryEntity>> = dao.observeSearchHistory()
    val masteredItemsFlow: Flow<List<MasteredItemEntity>> = dao.observeMasteredItems()

    fun observeWeeklyPoints(): Flow<List<DailyPointsSummary>> {
        val sevenDaysAgo = System.currentTimeMillis() - 7L * 24L * 60L * 60L * 1000L
        return dao.observePointsSince(sevenDaysAgo)
    }

    fun observeMonthlyPoints(): Flow<List<DailyPointsSummary>> {
        val thirtyDaysAgo = System.currentTimeMillis() - 30L * 24L * 60L * 60L * 1000L
        return dao.observePointsSince(thirtyDaysAgo)
    }

    suspend fun ensureInitializedAndMigrate() {
        val existing = dao.getUserProgressOnce()
        val legacy = prefsRepo.readLegacySnapshotForMigration()
        if (existing == null) {
            val initialQuiz = legacy?.third
            val seedPoints = (initialQuiz?.totalCorrect ?: 0) * 10
            dao.upsertUserProgress(
                UserProgressEntity(
                    totalPoints = seedPoints,
                    todayPoints = 0,
                    dailyGoalPoints = 50,
                    currentStreakDays = if ((initialQuiz?.lastCompletedDate ?: "").isNotBlank()) 1 else 0,
                    bestStreakDays = if ((initialQuiz?.lastCompletedDate ?: "").isNotBlank()) 1 else 0,
                    lastActiveDate = initialQuiz?.lastCompletedDate ?: ""
                )
            )
            if (seedPoints >= 25) {
                dao.unlockAchievement(AchievementEntity(AchievementId.BEGINNER.id))
            }
        }
        if (legacy != null) {
            prefsRepo.markRoomMigrated()
        }
    }

    private fun todayKey(): String = dateFormat.format(Date())

    private fun yesterdayKey(): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -1)
        return dateFormat.format(cal.time)
    }

    suspend fun awardPoints(
        source: String,
        itemId: String,
        label: String,
        points: Int,
        uniquePerItem: Boolean = false,
        studySecondsDelta: Long = 10L
    ): Int {
        if (points <= 0) return 0
        if (uniquePerItem && dao.countPointsAwardedForItem(source, itemId) > 0) {
            return 0
        }
        val today = todayKey()
        val yesterday = yesterdayKey()
        val current = dao.getUserProgressOnce() ?: UserProgressEntity()
        val newTodayPoints = if (current.lastActiveDate == today) current.todayPoints + points else points
        val newStreak = when (current.lastActiveDate) {
            today -> maxOf(1, current.currentStreakDays)
            yesterday -> current.currentStreakDays + 1
            else -> 1
        }
        val newTotal = current.totalPoints + points
        val updated = current.copy(
            totalPoints = newTotal,
            todayPoints = newTodayPoints,
            currentStreakDays = newStreak,
            bestStreakDays = maxOf(current.bestStreakDays, newStreak),
            lastActiveDate = today,
            totalLearningSeconds = current.totalLearningSeconds + studySecondsDelta,
            lessonsCompleted = current.lessonsCompleted + if (source in setOf("QUIZ", "FLASHCARD", "WRITING", "SPEAKING", "LISTENING")) 1 else 0,
            listeningCompleted = current.listeningCompleted + if (source == "LISTENING") 1 else 0,
            writingCompleted = current.writingCompleted + if (source == "WRITING") 1 else 0,
            speakingCompleted = current.speakingCompleted + if (source == "SPEAKING") 1 else 0
        )
        dao.upsertUserProgress(updated)
        dao.insertPointsEvent(
            PointsEventEntity(
                source = source,
                itemId = itemId,
                label = label,
                pointsDelta = points,
                dateKey = today
            )
        )
        evaluateAndUnlockAchievements(updated)
        return points
    }

    suspend fun recordLearningView(
        module: String,
        itemId: String,
        title: String,
        subtitle: String,
        awardPointsIfNew: Int = 0
    ): Int {
        dao.upsertLearningHistory(
            LearningHistoryEntity(
                historyKey = "${module}_${itemId}",
                module = module,
                itemId = itemId,
                title = title,
                subtitle = subtitle
            )
        )
        return if (awardPointsIfNew > 0) {
            awardPoints(
                source = module,
                itemId = itemId,
                label = title,
                points = awardPointsIfNew,
                uniquePerItem = true,
                studySecondsDelta = 15L
            )
        } else {
            0
        }
    }

    suspend fun recordQuizSession(mode: String, score: Int, totalQuestions: Int, pointsEarned: Int) {
        dao.insertQuizAttempt(
            QuizAttemptEntity(
                mode = mode,
                score = score,
                totalQuestions = totalQuestions,
                pointsEarned = pointsEarned
            )
        )
        if (pointsEarned > 0) {
            awardPoints(
                source = "QUIZ",
                itemId = "session_${System.currentTimeMillis()}",
                label = "$mode ($score/$totalQuestions)",
                points = pointsEarned,
                uniquePerItem = false,
                studySecondsDelta = (totalQuestions * 15L).coerceAtLeast(20L)
            )
        }
    }

    suspend fun setItemMastered(module: String, itemId: String, mastered: Boolean, awardPoints: Boolean = true) {
        dao.upsertMasteredItem(
            MasteredItemEntity(
                itemKey = "${module}_${itemId}",
                module = module,
                itemId = itemId,
                mastered = mastered
            )
        )
        if (mastered && awardPoints) {
            awardPoints(
                source = module,
                itemId = "master_$itemId",
                label = "Mastered $itemId",
                points = 10,
                uniquePerItem = true,
                studySecondsDelta = 15L
            )
        }
    }

    suspend fun saveSearchQuery(query: String) {
        val trimmed = query.trim()
        if (trimmed.length >= 2) {
            dao.upsertSearchQuery(SearchHistoryEntity(query = trimmed))
        }
    }

    suspend fun clearSearchHistory() = dao.clearSearchHistory()

    suspend fun syncDailyGoal(goalPoints: Int) {
        val current = dao.getUserProgressOnce() ?: UserProgressEntity()
        dao.upsertUserProgress(current.copy(dailyGoalPoints = goalPoints))
    }

    suspend fun resetAllLocalLearningData() {
        dao.clearProgress()
        dao.clearPointsEvents()
        dao.clearLearningHistory()
        dao.clearQuizAttempts()
        dao.clearAchievements()
        dao.clearMasteredItems()
        dao.clearSearchHistory()
        prefsRepo.resetQuizProgress()
        prefsRepo.clearAllFavorites()
    }

    suspend fun mergeCloudProgressIfHigher(
        totalPoints: Int,
        bestStreak: Int,
        totalLearningSeconds: Long,
        unlockedIds: List<String>,
        todayPoints: Int = 0,
        currentStreak: Int = 0,
        lastActiveDate: String = ""
    ) {
        val current = dao.getUserProgressOnce() ?: UserProgressEntity()
        val today = todayKey()
        val mergedLastDate = if (lastActiveDate == today || current.lastActiveDate == today) today else {
            maxOf(current.lastActiveDate, lastActiveDate)
        }
        val mergedToday = if (lastActiveDate == today && current.lastActiveDate == today) {
            maxOf(current.todayPoints, todayPoints)
        } else if (lastActiveDate == today) {
            todayPoints
        } else {
            current.todayPoints
        }
        dao.upsertUserProgress(
            current.copy(
                totalPoints = maxOf(current.totalPoints, totalPoints),
                todayPoints = mergedToday,
                currentStreakDays = maxOf(current.currentStreakDays, currentStreak),
                bestStreakDays = maxOf(current.bestStreakDays, bestStreak),
                totalLearningSeconds = maxOf(current.totalLearningSeconds, totalLearningSeconds),
                lastActiveDate = mergedLastDate
            )
        )
        unlockedIds.forEach { id ->
            dao.unlockAchievement(AchievementEntity(id))
        }
    }

    private suspend fun evaluateAndUnlockAchievements(progress: UserProgressEntity) {
        if (progress.totalPoints >= 25) {
            dao.unlockAchievement(AchievementEntity(AchievementId.BEGINNER.id))
        }
        if (progress.listeningCompleted + progress.speakingCompleted >= 3) {
            dao.unlockAchievement(AchievementEntity(AchievementId.LISTENER_PRO.id))
        }
        if (progress.lessonsCompleted >= 3) {
            dao.unlockAchievement(AchievementEntity(AchievementId.QUIZ_CHAMPION.id))
        }
        // Item-count achievements are checked efficiently via counts in points_events or history
        if (progress.totalPoints >= 100) {
            dao.unlockAchievement(AchievementEntity(AchievementId.PROVERB_KEEPER.id))
            dao.unlockAchievement(AchievementEntity(AchievementId.VOCAB_BUILDER.id))
        }
        if (progress.totalPoints >= 150) {
            dao.unlockAchievement(AchievementEntity(AchievementId.ALPHABET_EXPLORER.id))
        }
    }

    suspend fun checkModuleCountAchievements(qubeeCount: Int, vocabCount: Int, proverbCount: Int, quizCount: Int) {
        if (qubeeCount >= 10) dao.unlockAchievement(AchievementEntity(AchievementId.ALPHABET_EXPLORER.id))
        if (vocabCount >= 15) dao.unlockAchievement(AchievementEntity(AchievementId.VOCAB_BUILDER.id))
        if (proverbCount >= 10) dao.unlockAchievement(AchievementEntity(AchievementId.PROVERB_KEEPER.id))
        if (quizCount >= 3) dao.unlockAchievement(AchievementEntity(AchievementId.QUIZ_CHAMPION.id))
    }
}
