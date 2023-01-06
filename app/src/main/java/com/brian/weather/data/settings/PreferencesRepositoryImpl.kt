package com.brian.weather.data.settings


import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private const val TWELVE_HOUR = "hh:mm a"
private const val TWENTY_FOUR_HOUR = "kk:mm"

class PreferencesRepositoryImpl(
    private val dataStore: DataStore<Preferences>
): PreferencesRepository {

    override val TEMPERATURE_UNIT = stringPreferencesKey("temperature_unit")
    override val WINDSPEED_UNIT = stringPreferencesKey("windspeed_unit")
    override val MEASUREMENT_UNIT = stringPreferencesKey("measurement_unit")
    override val DYNAMIC_COLORS = booleanPreferencesKey("dynamic_colors")
    override val SHOW_WEATHER_ALERTS = booleanPreferencesKey("show_weather_alerts")
    override val CLOCK_FORMAT = stringPreferencesKey("clock_format")
    override val DATE_FORMAT = stringPreferencesKey("calendar_format")
    override val SHOW_NOTIFICATIONS = booleanPreferencesKey("show_notifications")
    override val SHOW_LOCAL_FORECAST = booleanPreferencesKey("show_local_forecast")
    override val SHOW_PRECIPITATION_NOTIFICATIONS = booleanPreferencesKey("show_precipitation_notifications")
    override val PRECIPITATION_LOCATIONS: Preferences.Key<Set<String>> = stringSetPreferencesKey("precipitation_locations")
    override val CARD_SIZE = stringPreferencesKey("card_size")

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
    override val getWeatherAlertsSetting: Flow<Boolean?> = dataStore.data
        .catch { exception ->
            // dataStore.data throws an IOException when an error is encountered when reading data
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            preferences[SHOW_WEATHER_ALERTS] ?: true // default value
        }

    override val getDynamicColorsSetting: Flow<Boolean?> = dataStore.data
    .catch { exception ->
        // dataStore.data throws an IOException when an error is encountered when reading data
        if (exception is IOException) {
            emit(emptyPreferences())
        } else {
            throw exception
        }
    }.map { preferences ->
        preferences[DYNAMIC_COLORS] ?: true // default value
    }
    override val getNotificationSetting: Flow<Boolean?> = dataStore.data
    .catch { exception ->
        // dataStore.data throws an IOException when an error is encountered when reading data
        if (exception is IOException) {
            emit(emptyPreferences())
        } else {
            throw exception
        }
    }.map { preferences ->
        preferences[SHOW_NOTIFICATIONS] ?: true // default value
    }

    override val getLocalForecastSetting: Flow<Boolean?> = dataStore.data
    .catch { exception ->
        // dataStore.data throws an IOException when an error is encountered when reading data
        if (exception is IOException) {
            emit(emptyPreferences())
        } else {
            throw exception
        }
    }.map { preferences ->
        preferences[SHOW_LOCAL_FORECAST] ?: false // default value
    }

    override val getPrecipitationSetting: Flow<Boolean?> = dataStore.data
    .catch { exception ->
        // dataStore.data throws an IOException when an error is encountered when reading data
        if (exception is IOException) {
            emit(emptyPreferences())
        } else {
            throw exception
        }
    }.map { preferences ->
        preferences[SHOW_PRECIPITATION_NOTIFICATIONS] ?: true // default value
    }

    override val getPrecipitationLocations: Flow<Set<String>> = dataStore.data
    .catch { exception ->
        // dataStore.data throws an IOException when an error is encountered when reading data
        if (exception is IOException) {
            emit(emptyPreferences())
        } else {
            throw exception
        }
    }.map { preferences ->
        preferences[PRECIPITATION_LOCATIONS] ?: emptySet() // default value
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
    override val getDateFormat: Flow<String?> = dataStore.data
    .catch { exception ->
        // dataStore.data throws an IOException when an error is encountered when reading data
        if (exception is IOException) {
            emit(emptyPreferences())
        } else {
            throw exception
        }
    }.map { preferences ->
        preferences[DATE_FORMAT] ?: "MM/DD"// default value
    }

    override val getCardSize: Flow<String?> = dataStore.data
        .catch { exception ->
            // dataStore.data throws an IOException when an error is encountered when reading data
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            preferences[DATE_FORMAT] ?: "MM/DD"// default value
        }

    override val getAllPreferences: Flow<AppPreferences> = dataStore.data
        .map { preferences ->
            val tempUnit = preferences[TEMPERATURE_UNIT] ?: "Fahrenheit"
            val clockFormat = preferences[CLOCK_FORMAT] ?: "hh:mm a"
            val dateFormat = preferences[DATE_FORMAT] ?: "MM/DD"
            val dynamicColors = preferences[DYNAMIC_COLORS] ?: true
            val windSpeedUnit = preferences[WINDSPEED_UNIT] ?: "MPH"
            val showAlerts = preferences[SHOW_WEATHER_ALERTS] ?: true
            val precipLocations = preferences[PRECIPITATION_LOCATIONS] ?: emptySet()
            val measurmentUnit = preferences[MEASUREMENT_UNIT] ?: "IN"
            val showLocalForecast = preferences[SHOW_LOCAL_FORECAST] ?: false
            val showNotifications = preferences[SHOW_NOTIFICATIONS] ?: true
            val showPrecipNotifications = preferences[SHOW_PRECIPITATION_NOTIFICATIONS
            ] ?: true
            val cardSize = preferences[CARD_SIZE] ?: "Medium"

            AppPreferences(
                tempUnit = tempUnit,
                dynamicColors = dynamicColors,
                measurementUnit = measurmentUnit,
                windUnit = windSpeedUnit,
                clockFormat = clockFormat,
                dateFormat = dateFormat,
                showAlerts = showAlerts,
                showLocalForecast = showLocalForecast ,
                showNotifications = showNotifications,
                showPrecipitationNotifications = showPrecipNotifications,
                precipitationLocations = precipLocations,
                cardSize = cardSize

            )
        }

    //save setting into datastore
    override suspend fun saveTemperatureSetting(value: String) {
        dataStore.edit { preferences ->
            preferences[TEMPERATURE_UNIT] = value
        }
    }

    override suspend fun saveMeasurementSetting(value: String) {
        dataStore.edit { preferences ->
            preferences[MEASUREMENT_UNIT] = value
        }
    }

    override suspend fun saveWindspeedSetting(value: String) {
        dataStore.edit { preferences ->
            preferences[WINDSPEED_UNIT] = value
        }
    }

    override suspend fun saveClockFormatSetting(value: String) {
        dataStore.edit { preferences ->
            preferences[CLOCK_FORMAT] = value
        }
    }

    override suspend fun saveDateFormatSetting(value: String) {
        dataStore.edit { preferences ->
            preferences[DATE_FORMAT] = value
        }
    }

    override suspend fun saveWeatherAlertSetting(value: Boolean) {
        dataStore.edit { preferences ->
            preferences[SHOW_WEATHER_ALERTS] = value
        }
    }


    override suspend fun saveDynamicColorSetting(value: Boolean) {
        dataStore.edit { preferences ->
            preferences[DYNAMIC_COLORS] = value
        }
    }

    override suspend fun saveNotificationSetting(value: Boolean) {
        dataStore.edit { preferences ->
            preferences[SHOW_NOTIFICATIONS] = value
        }
    }

    override suspend fun saveLocalForecastSetting(value: Boolean) {
        dataStore.edit { preferences ->
            preferences[SHOW_LOCAL_FORECAST] = value
        }
    }

    override suspend fun savePrecipitationSetting(value: Boolean) {
        dataStore.edit { preferences ->
            preferences[SHOW_PRECIPITATION_NOTIFICATIONS] = value
        }
    }

    override suspend fun savePrecipitationLocations(values: Set<String>) {
        dataStore.edit { preferences ->
            preferences[PRECIPITATION_LOCATIONS] = values
        }
    }

    override suspend fun saveCardSizeSetting(value: String) {
        dataStore.edit { preferences ->
            preferences[CARD_SIZE] = value
        }
    }




    // Function to return preferences
    override suspend fun fetchInitialPreferences() = dataStore.data.first().toPreferences()


}