package com.brian.weathercompose.data.remote.dto

import kotlinx.serialization.Serializable

//TODO everything in data class should be vals, need to clean this up and do formatting
// at the last presentation layer

@Serializable
data class ForecastContainer(
    val location: LocationData,
    val forecast: ForecastDay,
    val alerts: AlertList
)

@Serializable
data class ForecastDay(
    val forecastday: List<Day>
)

@Serializable
data class AlertList(
    val alert: List<Alert>
)

@Serializable
data class Alert(
    val headline: String,
    val category: String,
    val severity: String,
    val event: String,
    val effective: String,
    val expires: String,
    val desc: String
)

@Serializable
data class Day(
    val date: String,
    val day: ForecastForDay,
    val hour: List<Hour>
)

@Serializable
data class ForecastForDay(
    val condition: Condition,
    val avgtemp_f: Double,
    val maxtemp_f: Double,
    val mintemp_f: Double,
    val avgtemp_c: Double,
    val maxtemp_c: Double,
    val mintemp_c: Double,
    val daily_chance_of_rain: Double,
    val daily_chance_of_snow: Double,
    val totalprecip_in: Double,
    val totalprecip_mm: Double
)

@Serializable
data class Hour(
    val time_epoch: Int,
    val time: String,
    val temp_f: Double,
    val temp_c: Double,
    val is_day: Int,
    val condition: Condition,
    val wind_mph: Double,
    val wind_kph: Double,
    val wind_dir: String,
    val chance_of_rain: Int,
    val pressure_mb: Double,
    val pressure_in: Double,
    val will_it_rain: Int,
    val chance_of_snow: Double,
    val will_it_snow: Int,
    val precip_mm: Double,
    val precip_in: Double,
    val feelslike_c: Double,
    val feelslike_f: Double,
    val windchill_c: Double,
    val windchill_f: Double
)