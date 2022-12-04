package com.brian.weather.data.remote.dto

import kotlinx.serialization.Serializable


@Serializable
data class CurrentWeatherData(
    val temp_c: Double,
    val temp_f: Double,
    val is_day: Int,
    val wind_mph: Double,
    val wind_kph: Double,
    val wind_dir: String,
    val uv: Double,
    val humidity: Int,
    val feelslike_f: Double,
    val feelslike_c: Double,
    val condition: Condition,
)

@Serializable
data class Condition(
    val text: String,
    val icon: String,
    val code: Int
)