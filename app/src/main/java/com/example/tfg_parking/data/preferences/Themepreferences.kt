package com.example.tfg_parking.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

object ThemePreferences {
    private val DARK_MODE    = booleanPreferencesKey("dark_mode")
    private val ACCENT_COLOR = stringPreferencesKey("accent_color")

    fun darkModeFlow(context: Context): Flow<Boolean> =
        context.dataStore.data.map { it[DARK_MODE] ?: false }

    fun accentColorFlow(context: Context): Flow<String> =
        context.dataStore.data.map { it[ACCENT_COLOR] ?: "blue" }

    suspend fun setDarkMode(context: Context, enabled: Boolean) {
        context.dataStore.edit { it[DARK_MODE] = enabled }
    }

    suspend fun setAccentColor(context: Context, color: String) {
        context.dataStore.edit { it[ACCENT_COLOR] = color }
    }
}