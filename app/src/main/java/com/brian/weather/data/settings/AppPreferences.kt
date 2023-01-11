package com.brian.weather.data.settings

data class AppPreferences (
    val tempUnit: String = "",
    val clockFormat: String = "",
    val dateFormat: String = "",
    val windUnit: String = "",
    val dynamicColors: Boolean = true,
    val showAlerts: Boolean = true,
    val measurementUnit: String = "",
    val showNotifications: Boolean = true,
    val showLocalForecast: Boolean = false,
    val showPrecipitationNotifications: Boolean = true,
    val precipitationLocations: Set<String> = emptySet(),
    val cardSize :String = ""
)