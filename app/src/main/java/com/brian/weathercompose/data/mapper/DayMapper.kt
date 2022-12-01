package com.brian.weathercompose.data.mapper

import androidx.compose.material.MaterialTheme
import androidx.compose.ui.graphics.Color
import com.brian.weathercompose.data.remote.dto.Day
import com.brian.weathercompose.data.remote.dto.ForecastForDay
import com.brian.weathercompose.domain.model.DayDomainObject
import com.brian.weathercompose.domain.model.DaysDomainObject

fun Day.toDomainModel(): DaysDomainObject {
    return DaysDomainObject(
        date = date,
        day = day.toDomainModel(),
        hours = hour.map { it.toDomainModel() }.toMutableList()
    )
}

fun ForecastForDay.toDomainModel(): DayDomainObject {
    return DayDomainObject(
        condition = condition,
        avgtemp_c = avgtemp_c,
        avgtemp_f = avgtemp_f,
        maxtemp_c = maxtemp_c,
        maxtemp_f = maxtemp_f,
        mintemp_c = mintemp_c,
        mintemp_f = mintemp_f,
        daily_chance_of_rain = daily_chance_of_rain,
        backgroundColors = emptyList(),
        textColor = Color.White,
        daily_chance_of_snow = daily_chance_of_snow,
        totalprecip_in = totalprecip_in,
        totalprecip_mm = totalprecip_mm
    )
}