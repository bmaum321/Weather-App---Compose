package com.brian.weather.domain.model

import androidx.compose.ui.graphics.Color
import com.brian.weather.data.remote.dto.Condition

data class DayDomainObject(
    val condition: Condition,
    val avgtemp: Double,
    val maxtemp: Double,
    val mintemp: Double,
    val daily_chance_of_rain: Double,
    val backgroundColors: List<Color>,
    val textColor: Color,
    val daily_chance_of_snow: Double,
    val totalprecip: Double,
    val avghumidity: Double
)

