package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class AppLanguage(val code: String, val badge: String) {
    OROMO("om", "OM"),
    ENGLISH("en", "EN")
}

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "qubee_prefs")

class AppPreferencesRepository(private val context: Context) {

    private object Keys {
        val LANGUAGE = stringPreferencesKey("app_language")
        val DARK_THEME = booleanPreferencesKey("dark_theme")
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

    suspend fun setLanguage(language: AppLanguage) {
        context.dataStore.edit { prefs ->
            prefs[Keys.LANGUAGE] = language.code
        }
    }

    suspend fun setDarkTheme(isDark: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.DARK_THEME] = isDark
        }
    }
}
