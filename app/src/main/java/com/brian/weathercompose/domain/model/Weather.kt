package com.brian.weathercompose.domain.model

import androidx.compose.ui.graphics.Color

/**
 * Domain objects are plain Kotlin data classes that represent the things in our app. These are the
 * objects that should be displayed on screen, or manipulated by the app.
 *
 */


data class WeatherDomainObject(
    val location: String,
    val temp: String,
    val zipcode: String,
    val imgSrcUrl: String,
    val conditionText: String,
    val windSpeed: Double,
    val windDirection: String,
    val time: String,
    val backgroundColors: List<Color>,
    val code: Int,
    val textColor: Color,
    val country: String,
    val feelsLikeTemp: String
)

data class ForecastDomainObject(
    val days: List<DaysDomainObject>,
    val alerts: List<AlertDomainObject>
)














