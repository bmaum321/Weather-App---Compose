package com.brian.weathercompose.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first

import kotlinx.coroutines.flow.map

class SettingsRepositoryImpl(
    private val dataStore: DataStore<Preferences>
) {

   // private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "Settings")

    val TEMPERATURE_UNIT = stringPreferencesKey("temperature_unit")
    val DYNAMIC_COLORS = booleanPreferencesKey("dynamic_colors")
    val CLOCK_FORMAT = stringPreferencesKey("clock_format")

    val getTemperatureUnit: Flow<String?> = dataStore.data
        .catch { exception ->
            // dataStore.data throws an IOException when an error is encountered when reading data
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            preferences[TEMPERATURE_UNIT] ?: "Fahrenheit"
        }

    val getClockFormat: Flow<String?> = dataStore.data
        .catch { exception ->
            // dataStore.data throws an IOException when an error is encountered when reading data
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            preferences[CLOCK_FORMAT] ?: "Fahrenheit"
        }

    val preferencesFlow: Flow<AppPreferences> = dataStore.data
        .map { preferences ->
            val tempUnit = preferences[TEMPERATURE_UNIT] ?: "Fahrenheit"
            val dynamicColors = preferences[DYNAMIC_COLORS] ?: true

            AppPreferences(
                tempUnit = tempUnit,
                dynamicColors = dynamicColors
            )
        }

    //save setting into datastore
    suspend fun saveTempSetting(unit: String) {
        dataStore.edit { preferences ->
            preferences[TEMPERATURE_UNIT] = unit
        }
    }

    // Function to return preferences
    suspend fun fetchInitialPreferences() = dataStore.data.first().toPreferences()

    data class AppPreferences (
        val tempUnit: String,
        val dynamicColors: Boolean
    )
}