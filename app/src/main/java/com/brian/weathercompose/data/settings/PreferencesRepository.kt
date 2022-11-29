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
     val SHOW_NOTIFICATIONS: Preferences.Key<Boolean>
     val SHOW_LOCAL_FORECAST: Preferences.Key<Boolean>
     val SHOW_PRECIPITATION_NOTIFICATIONS: Preferences.Key<Boolean>
     val PRECIPITATION_LOCATIONS: Preferences.Key<Set<String>>

     val getAllPreferences: Flow<AppPreferences>
     val getClockFormat: Flow<String?>
     val getTemperatureUnit: Flow<String?>
     val getWindspeedUnit: Flow<String?>
     val getMeasurementUnit: Flow<String?>
     val getWeatherAlertsSetting: Flow<Boolean?>
     val getDynamicColorsSetting: Flow<Boolean?>
     val getNotificationSetting: Flow<Boolean?>
     val getLocalForecastSetting: Flow<Boolean?>
     val getPrecipitationSetting: Flow<Boolean?>
     val getPrecipitationLocations: Flow<Set<String?>>

     suspend fun saveTemperatureSetting(value: String)
     suspend fun saveMeasurementSetting(value: String)
     suspend fun saveWindspeedSetting(value: String)
     suspend fun saveClockFormatSetting(value: String)
     suspend fun saveWeatherAlertSetting(value: Boolean)
     suspend fun saveDynamicColorSetting(value: Boolean)
     suspend fun saveNotificationSetting(value: Boolean)
     suspend fun saveLocalForecastSetting(value: Boolean)
     suspend fun savePrecipitationSetting(value: Boolean)
     suspend fun savePrecipitationLocations(values: Set<String>)
     suspend fun fetchInitialPreferences(): Preferences
}