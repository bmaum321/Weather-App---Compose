package com.brian.weathercompose.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private const val TWELVE_HOUR = "hh:mm a"
private const val TWENTY_FOUR_HOUR = "kk:mm"

class SettingsRepositoryImpl(
    private val dataStore: DataStore<Preferences>
): SettingsRepository {

    override val TEMPERATURE_UNIT = stringPreferencesKey("temperature_unit")
    override val WINDSPEED_UNIT = stringPreferencesKey("windspeed_unit")
    override val MEASUREMENT_UNIT = stringPreferencesKey("measurement_unit")
    override val DYNAMIC_COLORS = booleanPreferencesKey("dynamic_colors")
    override val CLOCK_FORMAT = stringPreferencesKey("clock_format")

    override val getTemperatureUnit: Flow<String?> = dataStore.data
        .catch { exception ->
            // dataStore.data throws an IOException when an error is encountered when reading data
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            preferences[TEMPERATURE_UNIT] ?: "Fahrenheit" // Default value
        }

    override val getWindspeedUnit: Flow<String?> = dataStore.data
    .catch { exception ->
        // dataStore.data throws an IOException when an error is encountered when reading data
        if (exception is IOException) {
            emit(emptyPreferences())
        } else {
            throw exception
        }
    }.map { preferences ->
        preferences[WINDSPEED_UNIT] ?: "MPH" // default value
    }

    override val getMeasurementUnit: Flow<String?> = dataStore.data
    .catch { exception ->
        // dataStore.data throws an IOException when an error is encountered when reading data
        if (exception is IOException) {
            emit(emptyPreferences())
        } else {
            throw exception
        }
    }.map { preferences ->
        preferences[MEASUREMENT_UNIT] ?: "IN" // default value
    }

    override val getClockFormat: Flow<String?> = dataStore.data
        .catch { exception ->
            // dataStore.data throws an IOException when an error is encountered when reading data
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            preferences[CLOCK_FORMAT] ?: TWELVE_HOUR // default value
        }

    override val preferencesFlow: Flow<AppPreferences> = dataStore.data
        .map { preferences ->
            val tempUnit = preferences[TEMPERATURE_UNIT] ?: "Fahrenheit"
            val dynamicColors = preferences[DYNAMIC_COLORS] ?: true

            AppPreferences(
                tempUnit = tempUnit,
                dynamicColors = dynamicColors
            )
        }

    //save setting into datastore
    override suspend fun saveTemperatureSetting(unit: String) {
        dataStore.edit { preferences ->
            preferences[TEMPERATURE_UNIT] = unit
        }
    }

    override suspend fun saveMeasurementSetting(unit: String) {
        dataStore.edit { preferences ->
            preferences[MEASUREMENT_UNIT] = unit
        }
    }

    override suspend fun saveWindspeedSetting(unit: String) {
        dataStore.edit { preferences ->
            preferences[WINDSPEED_UNIT] = unit
        }
    }

    override suspend fun saveClockFormatSetting(unit: String) {
        dataStore.edit { preferences ->
            preferences[CLOCK_FORMAT] = unit
        }
    }

    // Function to return preferences
    override suspend fun fetchInitialPreferences() = dataStore.data.first().toPreferences()


}