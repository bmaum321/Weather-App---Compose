package com.brian.weather.domain.model

import androidx.compose.ui.graphics.Color
import com.brian.weather.data.remote.dto.Condition

data class DayDomainObject(
    val condition: Condition,
    val avgtemp_f: Double,
    val maxtemp_f: Double,
    val mintemp_f: Double,
    val avgtemp_c: Double,
    val maxtemp_c: Double,
    val mintemp_c: Double,
    val daily_chance_of_rain: Double,
    val backgroundColors: List<Color>,
    val textColor: Color,
    val daily_chance_of_snow: Double,
    val totalprecip_in: Double,
    val totalprecip_mm: Double,
    val avghumidity: Double
)

