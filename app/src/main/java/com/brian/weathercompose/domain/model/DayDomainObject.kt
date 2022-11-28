package com.brian.weathercompose.domain.model

import androidx.compose.ui.graphics.Color
import com.brian.weathercompose.data.remote.dto.Condition

data class DayDomainObject(
    val condition: Condition,
    val avgtemp_f: Double,
    val maxtemp_f: Double,
    val mintemp_f: Double,
    val avgtemp_c: Double,
    val maxtemp_c: Double,
    val mintemp_c: Double,
    val daily_chance_of_rain: Double,
    var backgroundColors: List<Color>,
    var textColor: Color
)

