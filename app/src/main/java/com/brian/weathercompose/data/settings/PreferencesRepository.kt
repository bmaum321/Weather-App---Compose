package com.brian.weathercompose.data.settings

import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {

     val TEMPERATURE_UNIT: Preferences.Key<String>
     val WINDSPEED_UNIT: Preferences.Key<String>
     val MEASUREMENT_UNIT: Preferences.Key<String>
     val DYNAMIC_COLORS: Preferences.Key<Boolean>
     val SHOW_WEATHER_ALERTS: Preferences.Key<Boolean>
     val CLOCK_FORMAT: Preferences.Key<String>

     val preferencesFlow: Flow<AppPreferences>
     val getClockFormat: Flow<String?>
     val getTemperatureUnit: Flow<String?>
     val getWindspeedUnit: Flow<String?>
     val getMeasurementUnit: Flow<String?>
     val getWeatherAlertsSetting: Flow<String?>
     val getDynamicColorsSetting: Flow<String?>

     suspend fun saveTemperatureSetting(unit: String)
     suspend fun saveMeasurementSetting(unit: String)
     suspend fun saveWindspeedSetting(unit: String)
     suspend fun saveClockFormatSetting(unit: String)
     suspend fun saveWeatherAlertSetting(unit: String)
     suspend fun saveDynamicColorSetting(unit: String)
     suspend fun fetchInitialPreferences(): Preferences
}