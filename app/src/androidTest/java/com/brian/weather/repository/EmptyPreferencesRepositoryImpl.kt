package com.brian.weather.repository

import androidx.datastore.preferences.core.*
import com.brian.weather.data.settings.AppPreferences
import com.brian.weather.data.settings.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow

private const val TWELVE_HOUR = "hh:mm a"
private const val TWENTY_FOUR_HOUR = "kk:mm"

class EmptyPreferencesRepositoryImpl(
) : PreferencesRepository {
    override val TEMPERATURE_UNIT: Preferences.Key<String> = stringPreferencesKey("")
    override val WINDSPEED_UNIT: Preferences.Key<String> = stringPreferencesKey("")
    override val MEASUREMENT_UNIT: Preferences.Key<String> = stringPreferencesKey("")
    override val DYNAMIC_COLORS: Preferences.Key<Boolean> = booleanPreferencesKey("")
    override val SHOW_WEATHER_ALERTS: Preferences.Key<Boolean> = booleanPreferencesKey("")
    override val CLOCK_FORMAT: Preferences.Key<String>
        get() = TODO("Not yet implemented")
    override val DATE_FORMAT: Preferences.Key<String> = stringPreferencesKey("")
    override val SHOW_NOTIFICATIONS: Preferences.Key<Boolean> = booleanPreferencesKey("")
    override val SHOW_LOCAL_FORECAST: Preferences.Key<Boolean> = booleanPreferencesKey("")
    override val SHOW_PRECIPITATION_NOTIFICATIONS: Preferences.Key<Boolean> =
        booleanPreferencesKey("")
    override val PRECIPITATION_LOCATIONS: Preferences.Key<Set<String>> = stringSetPreferencesKey("")
    override val CARD_SIZE: Preferences.Key<String>
        get() = TODO("Not yet implemented")
    override val getAllPreferences: Flow<AppPreferences> = flow {
        emit(
            AppPreferences(
                tempUnit = "",
                clockFormat = "",
                dateFormat = "",
                windUnit = "",
                dynamicColors = false,
                showAlerts = false,
                measurementUnit = "",
                showNotifications = false,
                showLocalForecast = false,
                showPrecipitationNotifications = false,
                precipitationLocations = setOf(),
                cardSize = ""
            )
        )
    }
    override val getClockFormat: Flow<String?> = emptyFlow()
    override val getDateFormat: Flow<String?> = emptyFlow()
    override val getTemperatureUnit: Flow<String?> = emptyFlow()
    override val getWindspeedUnit: Flow<String?> = emptyFlow()
    override val getMeasurementUnit: Flow<String?> = emptyFlow()
    override val getWeatherAlertsSetting: Flow<Boolean?> = emptyFlow()
    override val getDynamicColorsSetting: Flow<Boolean?> = emptyFlow()
    override val getNotificationSetting: Flow<Boolean?> = emptyFlow()
    override val getLocalForecastSetting: Flow<Boolean?> = emptyFlow()
    override val getPrecipitationSetting: Flow<Boolean?> = emptyFlow()
    override val getPrecipitationLocations: Flow<Set<String>?> = emptyFlow()
    override val getCardSize: Flow<String?>
        get() = TODO("Not yet implemented")

    override suspend fun saveTemperatureSetting(value: String) {
    }

    override suspend fun saveMeasurementSetting(value: String) {
    }

    override suspend fun saveWindspeedSetting(value: String) {
    }

    override suspend fun saveClockFormatSetting(value: String) {
    }

    override suspend fun saveDateFormatSetting(value: String) {
    }

    override suspend fun saveWeatherAlertSetting(value: Boolean) {
    }

    override suspend fun saveDynamicColorSetting(value: Boolean) {
    }

    override suspend fun saveNotificationSetting(value: Boolean) {
    }

    override suspend fun saveLocalForecastSetting(value: Boolean) {
    }

    override suspend fun savePrecipitationSetting(value: Boolean) {

    }

    override suspend fun savePrecipitationLocations(values: Set<String>) {

    }

    override suspend fun fetchInitialPreferences(): Preferences {
        return emptyPreferences()
    }

    override suspend fun saveCardSizeSetting(value: String) {
        TODO("Not yet implemented")
    }


}