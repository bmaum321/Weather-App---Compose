package com.brian.weathercompose.data.mapper

import androidx.compose.ui.graphics.Color
import com.brian.weathercompose.data.remote.dto.Astro
import com.brian.weathercompose.data.remote.dto.Day
import com.brian.weathercompose.data.remote.dto.ForecastForDay
import com.brian.weathercompose.domain.model.AstroDataDomainObject
import com.brian.weathercompose.domain.model.DayDomainObject
import com.brian.weathercompose.domain.model.DaysDomainObject

fun Day.toDomainModel(clockFormat: String): DaysDomainObject {
    return DaysDomainObject(
        date = date,
        day = day.toDomainModel(),
        hours = hour.map { it.toDomainModel() }.toMutableList(),
        astroData = astro.toDomainModel(clockFormat)
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
        totalprecip_mm = totalprecip_mm,
        avghumidity = avghumidity
    )
}

fun Astro.toDomainModel(clockFormat: String): AstroDataDomainObject {
    return AstroDataDomainObject(
        moon_phase = moon_phase,
       // sunrise = LocalTime.parse(sunrise.dropLast(3)).format(DateTimeFormatter.ofPattern(clockFormat)).removePrefix("0"),
       // sunset = LocalTime.parse(sunset.dropLast(3)).format(DateTimeFormatter.ofPattern(clockFormat)).removePrefix("0")
    sunrise = sunrise.removePrefix("0"),
        sunset = sunset.removePrefix("0")
    )
}