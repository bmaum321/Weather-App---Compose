package com.brian.weather.data.settings

data class AppPreferences (
    val tempUnit: String,
    val clockFormat: String,
    val windUnit: String,
    val dynamicColors: Boolean,
    val showAlerts: Boolean,
    val measurementUnit: String,
    val showNotifications: Boolean,
    val showLocalForecast: Boolean,
    val showPrecipitationNotifications: Boolean,
    val precipitationLocations: Set<String>
)