package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_progress")
data class UserProgressEntity(
    @PrimaryKey val id: Int = 1,
    val totalPoints: Int = 0,
    val todayPoints: Int = 0,
    val dailyGoalPoints: Int = 50,
    val currentStreakDays: Int = 0,
    val bestStreakDays: Int = 0,
    val lastActiveDate: String = "",
    val totalLearningSeconds: Long = 0L,
    val lessonsCompleted: Int = 0,
    val listeningCompleted: Int = 0,
    val writingCompleted: Int = 0,
    val speakingCompleted: Int = 0
)

@Entity(tableName = "points_events")
data class PointsEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val source: String, // QUIZ, PROVERB, VOCAB, QUBEE, SPEAKING, FLASHCARD, LISTENING, WRITING
    val itemId: String,
    val label: String,
    val pointsDelta: Int,
    val dateKey: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "learning_history")
data class LearningHistoryEntity(
    @PrimaryKey val historyKey: String, // "${module}_${itemId}"
    val module: String, // QUBEE, VOCAB, PHRASE, PROVERB, QUIZ, SPEAKING
    val itemId: String,
    val title: String,
    val subtitle: String,
    val viewedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "quiz_attempts")
data class QuizAttemptEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val mode: String,
    val score: Int,
    val totalQuestions: Int,
    val pointsEarned: Int,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey val achievementId: String,
    val unlockedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "search_history")
data class SearchHistoryEntity(
    @PrimaryKey val query: String,
    val searchedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "mastered_items")
data class MasteredItemEntity(
    @PrimaryKey val itemKey: String, // "${module}_${itemId}"
    val module: String, // QUBEE, VOCAB, FLASHCARD
    val itemId: String,
    val mastered: Boolean,
    val updatedAt: Long = System.currentTimeMillis()
)
