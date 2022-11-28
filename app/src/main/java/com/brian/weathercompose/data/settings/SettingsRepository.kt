package com.brian.weathercompose.data.settings

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {

     val TEMPERATURE_UNIT: Preferences.Key<String>
     val WINDSPEED_UNIT: Preferences.Key<String>
     val MEASUREMENT_UNIT: Preferences.Key<String>
     val DYNAMIC_COLORS: Preferences.Key<Boolean>
     val CLOCK_FORMAT: Preferences.Key<String>

     val preferencesFlow: Flow<AppPreferences>
     val getClockFormat: Flow<String?>
     val getTemperatureUnit: Flow<String?>
     val getWindspeedUnit: Flow<String?>
     val getMeasurementUnit: Flow<String?>
     suspend fun saveTemperatureSetting(unit: String)
     suspend fun saveMeasurementSetting(unit: String)
     suspend fun saveWindspeedSetting(unit: String)
     suspend fun saveClockFormatSetting(unit: String)
     suspend fun fetchInitialPreferences(): Preferences
}