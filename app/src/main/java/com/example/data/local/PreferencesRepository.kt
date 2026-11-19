package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.labPrefsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "atharv_lab_calculator_prefs"
)

data class LabPreferences(
    val isDarkMode: Boolean = true,
    val isDegreeMode: Boolean = true,
    val lastMode: String = "BASIC"
)

class PreferencesRepository(private val context: Context) {

    private object Keys {
        val DARK_MODE = booleanPreferencesKey("is_dark_mode")
        val DEGREE_MODE = booleanPreferencesKey("is_degree_mode")
        val LAST_MODE = stringPreferencesKey("last_calculator_mode")
    }

    val preferencesFlow: Flow<LabPreferences> = context.labPrefsDataStore.data.map { prefs ->
        LabPreferences(
            isDarkMode = prefs[Keys.DARK_MODE] ?: true,
            isDegreeMode = prefs[Keys.DEGREE_MODE] ?: true,
            lastMode = prefs[Keys.LAST_MODE] ?: "BASIC"
        )
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.labPrefsDataStore.edit { prefs ->
            prefs[Keys.DARK_MODE] = enabled
        }
    }

    suspend fun setDegreeMode(enabled: Boolean) {
        context.labPrefsDataStore.edit { prefs ->
            prefs[Keys.DEGREE_MODE] = enabled
        }
    }

    suspend fun setLastMode(mode: String) {
        context.labPrefsDataStore.edit { prefs ->
            prefs[Keys.LAST_MODE] = mode
        }
    }
}
