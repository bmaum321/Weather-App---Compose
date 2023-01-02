package com.brian.weather.domain.model

import androidx.compose.ui.graphics.Color
import com.brian.weather.data.remote.dto.Condition

data class HoursDomainObject(
    val time_epoch: Long,
    val time: String,
    val temp: Double,
    val is_day: Int,
    val condition: Condition,
    val windspeed: String,
    val wind_dir: String,
    val chance_of_rain: Int,
    val pressure: String,
    val will_it_rain: Int,
    val chance_of_snow: Double,
    val will_it_snow: Int,
    val precip: String,
    val feelslike: Double,
    val windchill: Double,
    val colors: List<Color>,
    val textColor: Color
)
