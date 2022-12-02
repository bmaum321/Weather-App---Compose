package com.brian.weathercompose.domain.model

import androidx.compose.ui.graphics.Color
import com.brian.weathercompose.data.remote.dto.Astro
import com.brian.weathercompose.data.remote.dto.ForecastForDay

data class DaysDomainObject(
    var date: String,
    val day: DayDomainObject,
    val hours: MutableList<HoursDomainObject>,
    val astro: AstroDomainObject
)
