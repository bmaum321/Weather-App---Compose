/*
package com.brian.weathercompose.data.settings


import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class SettingsDatastore(
    private val context: Context,
) {

    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "Preferences")
        val TEMPERATURE_UNIT = stringPreferencesKey("temperature_unit")
        val DYNAMIC_COLORS = booleanPreferencesKey("dynamic_colors")

    }

    //get the saved setting
    // Maybe either in a repository or a viewmodel
    val getTemperatureUnit: Flow<String?> = context.dataStore.data // TODO There is a different way to do this without requiring context https://developer.android.com/codelabs/android-preferences-datastore#5
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

    val preferencesFlow: Flow<AppPreferences> = context.dataStore.data
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
        context.dataStore.edit { preferences ->
            preferences[TEMPERATURE_UNIT] = unit
        }
    }

    // Function to return preferences
    suspend fun fetchInitialPreferences() = context.dataStore.data.first().toPreferences()


}

data class AppPreferences (
    val tempUnit: String,
    val dynamicColors: Boolean
        )

 */