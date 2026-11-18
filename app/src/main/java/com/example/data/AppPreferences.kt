package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class AppLanguage(val code: String, val badge: String) {
    OROMO("om", "OM"),
    ENGLISH("en", "EN")
}

data class QuizProgress(
    val totalAnswered: Int = 0,
    val totalCorrect: Int = 0,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val lastCompletedDate: String = ""
) {
    val accuracyPercent: Int
        get() = if (totalAnswered == 0) 0 else ((totalCorrect * 100f) / totalAnswered).toInt()
}

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "qubee_prefs")

class AppPreferencesRepository(private val context: Context) {

    private object Keys {
        val LANGUAGE = stringPreferencesKey("app_language")
        val DARK_THEME = booleanPreferencesKey("dark_theme")
        val FAVORITE_PROVERBS = stringSetPreferencesKey("favorite_proverb_ids")
        val FAVORITE_PHRASES = stringSetPreferencesKey("favorite_phrase_ids")
        val TTS_SPEED = floatPreferencesKey("tts_speed")
        val QUIZ_TOTAL_ANSWERED = intPreferencesKey("quiz_total_answered")
        val QUIZ_TOTAL_CORRECT = intPreferencesKey("quiz_total_correct")
        val QUIZ_CURRENT_STREAK = intPreferencesKey("quiz_current_streak")
        val QUIZ_BEST_STREAK = intPreferencesKey("quiz_best_streak")
        val DAILY_QUIZ_LAST_DATE = stringPreferencesKey("daily_quiz_last_date")
    }

    val languageFlow: Flow<AppLanguage> = context.dataStore.data.map { prefs ->
        when (prefs[Keys.LANGUAGE]) {
            AppLanguage.ENGLISH.code -> AppLanguage.ENGLISH
            else -> AppLanguage.OROMO
        }
    }

    val darkThemeFlow: Flow<Boolean?> = context.dataStore.data.map { prefs ->
        if (prefs.contains(Keys.DARK_THEME)) prefs[Keys.DARK_THEME] else null
    }

    val favoriteProverbsFlow: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        prefs[Keys.FAVORITE_PROVERBS] ?: emptySet()
    }

    val favoritePhrasesFlow: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        prefs[Keys.FAVORITE_PHRASES] ?: emptySet()
    }

    val ttsSpeedFlow: Flow<Float> = context.dataStore.data.map { prefs ->
        prefs[Keys.TTS_SPEED] ?: 0.88f
    }

    val quizProgressFlow: Flow<QuizProgress> = context.dataStore.data.map { prefs ->
        QuizProgress(
            totalAnswered = prefs[Keys.QUIZ_TOTAL_ANSWERED] ?: 0,
            totalCorrect = prefs[Keys.QUIZ_TOTAL_CORRECT] ?: 0,
            currentStreak = prefs[Keys.QUIZ_CURRENT_STREAK] ?: 0,
            bestStreak = prefs[Keys.QUIZ_BEST_STREAK] ?: 0,
            lastCompletedDate = prefs[Keys.DAILY_QUIZ_LAST_DATE] ?: ""
        )
    }

    suspend fun setLanguage(language: AppLanguage) {
        context.dataStore.edit { prefs ->
            prefs[Keys.LANGUAGE] = language.code
        }
    }

    suspend fun setDarkTheme(isDark: Boolean?) {
        context.dataStore.edit { prefs ->
            if (isDark == null) {
                prefs.remove(Keys.DARK_THEME)
            } else {
                prefs[Keys.DARK_THEME] = isDark
            }
        }
    }

    suspend fun toggleFavoriteProverb(id: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.FAVORITE_PROVERBS]?.toMutableSet() ?: mutableSetOf()
            if (!current.add(id)) {
                current.remove(id)
            }
            prefs[Keys.FAVORITE_PROVERBS] = current
        }
    }

    suspend fun toggleFavoritePhrase(id: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.FAVORITE_PHRASES]?.toMutableSet() ?: mutableSetOf()
            if (!current.add(id)) {
                current.remove(id)
            }
            prefs[Keys.FAVORITE_PHRASES] = current
        }
    }

    suspend fun clearAllFavorites() {
        context.dataStore.edit { prefs ->
            prefs.remove(Keys.FAVORITE_PROVERBS)
            prefs.remove(Keys.FAVORITE_PHRASES)
        }
    }

    suspend fun setTtsSpeed(speed: Float) {
        context.dataStore.edit { prefs ->
            prefs[Keys.TTS_SPEED] = speed
        }
    }

    suspend fun recordQuizAnswer(isCorrect: Boolean, completedDate: String? = null) {
        context.dataStore.edit { prefs ->
            val total = (prefs[Keys.QUIZ_TOTAL_ANSWERED] ?: 0) + 1
            val correct = (prefs[Keys.QUIZ_TOTAL_CORRECT] ?: 0) + if (isCorrect) 1 else 0
            val streak = if (isCorrect) (prefs[Keys.QUIZ_CURRENT_STREAK] ?: 0) + 1 else 0
            val best = maxOf(prefs[Keys.QUIZ_BEST_STREAK] ?: 0, streak)
            prefs[Keys.QUIZ_TOTAL_ANSWERED] = total
            prefs[Keys.QUIZ_TOTAL_CORRECT] = correct
            prefs[Keys.QUIZ_CURRENT_STREAK] = streak
            prefs[Keys.QUIZ_BEST_STREAK] = best
            if (!completedDate.isNullOrBlank()) {
                prefs[Keys.DAILY_QUIZ_LAST_DATE] = completedDate
            }
        }
    }
}
