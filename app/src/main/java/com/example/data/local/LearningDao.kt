package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

data class DailyPointsSummary(
    val dateKey: String,
    val total: Int
)

@Dao
interface LearningDao {

    @Query("SELECT * FROM user_progress WHERE id = 1")
    fun observeUserProgress(): Flow<UserProgressEntity?>

    @Query("SELECT * FROM user_progress WHERE id = 1")
    suspend fun getUserProgressOnce(): UserProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertUserProgress(progress: UserProgressEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPointsEvent(event: PointsEventEntity)

    @Query("SELECT * FROM points_events ORDER BY createdAt DESC LIMIT 30")
    fun observeRecentPointsEvents(): Flow<List<PointsEventEntity>>

    @Query("SELECT COUNT(*) FROM points_events WHERE source = :source AND itemId = :itemId")
    suspend fun countPointsAwardedForItem(source: String, itemId: String): Int

    @Query("SELECT dateKey, SUM(pointsDelta) AS total FROM points_events WHERE createdAt >= :sinceMillis GROUP BY dateKey ORDER BY dateKey ASC")
    fun observePointsSince(sinceMillis: Long): Flow<List<DailyPointsSummary>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLearningHistory(entry: LearningHistoryEntity)

    @Query("SELECT * FROM learning_history ORDER BY viewedAt DESC LIMIT 40")
    fun observeLearningHistory(): Flow<List<LearningHistoryEntity>>

    @Query("SELECT * FROM learning_history WHERE module = :module ORDER BY viewedAt DESC LIMIT 20")
    fun observeModuleHistory(module: String): Flow<List<LearningHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuizAttempt(attempt: QuizAttemptEntity)

    @Query("SELECT * FROM quiz_attempts ORDER BY createdAt DESC LIMIT 25")
    fun observeQuizAttempts(): Flow<List<QuizAttemptEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun unlockAchievement(achievement: AchievementEntity)

    @Query("SELECT * FROM achievements ORDER BY unlockedAt ASC")
    fun observeAchievements(): Flow<List<AchievementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSearchQuery(entry: SearchHistoryEntity)

    @Query("SELECT * FROM search_history ORDER BY searchedAt DESC LIMIT 10")
    fun observeSearchHistory(): Flow<List<SearchHistoryEntity>>

    @Query("DELETE FROM search_history")
    suspend fun clearSearchHistory()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMasteredItem(item: MasteredItemEntity)

    @Query("SELECT * FROM mastered_items WHERE mastered = 1")
    fun observeMasteredItems(): Flow<List<MasteredItemEntity>>

    @Query("DELETE FROM user_progress")
    suspend fun clearProgress()

    @Query("DELETE FROM points_events")
    suspend fun clearPointsEvents()

    @Query("DELETE FROM learning_history")
    suspend fun clearLearningHistory()

    @Query("DELETE FROM quiz_attempts")
    suspend fun clearQuizAttempts()

    @Query("DELETE FROM achievements")
    suspend fun clearAchievements()

    @Query("DELETE FROM mastered_items")
    suspend fun clearMasteredItems()
}
