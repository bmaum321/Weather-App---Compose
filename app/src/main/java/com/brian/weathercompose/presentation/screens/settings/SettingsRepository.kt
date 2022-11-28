package com.brian.weathercompose.presentation.screens.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch

import kotlinx.coroutines.flow.map

class SettingsRepository(
    private val dataStore: DataStore<Preferences>
) {

    data class UserPreferences(val showCompleted: Boolean)

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "Settings")
    val TEMPERATURE_UNIT = stringPreferencesKey("temperature_unit")
    val DYNAMIC_COLORS = booleanPreferencesKey("dynamic_colors")

    val getTemperatureUnit: Flow<String?> = dataStore.data // TODO There is a different way to do this without requiring context https://developer.android.com/codelabs/android-preferences-datastore#5
        .catch { exception ->
            // dataStore.data throws an IOException when an error is encountered when reading data
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            preferences[SettingsDatastore.TEMPERATURE_UNIT] ?: "Fahrenheit"
        }
}