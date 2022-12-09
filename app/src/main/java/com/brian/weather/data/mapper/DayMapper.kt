package com.brian.weather.data.mapper

import androidx.compose.ui.graphics.Color
import com.brian.weather.data.remote.dto.Astro
import com.brian.weather.data.remote.dto.Day
import com.brian.weather.data.remote.dto.ForecastForDay
import com.brian.weather.domain.model.AstroDataDomainObject
import com.brian.weather.domain.model.DayDomainObject
import com.brian.weather.domain.model.DaysDomainObject
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

fun Day.toDomainModel(clockFormat: String): DaysDomainObject {

    val today = LocalDate.now().dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
    val dayOfWeek = LocalDate.parse(date).dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
    return DaysDomainObject(
        date = if(dayOfWeek == today) "Today" else dayOfWeek,
        day = day.toDomainModel(),
        hours = hour.map { it.toDomainModel(clockFormat) }.toMutableList(),
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

    val sunrise = LocalTime
        .parse(sunrise, DateTimeFormatter.ofPattern("hh:mm a" , Locale.US))
        .format(DateTimeFormatter.ofPattern(clockFormat))
    val sunset = LocalTime
        .parse(sunset, DateTimeFormatter.ofPattern("hh:mm a" , Locale.US))
        .format(DateTimeFormatter.ofPattern(clockFormat))


    return AstroDataDomainObject(
        moon_phase = moon_phase,
        sunrise = if(clockFormat == "hh:mm a") sunrise.removePrefix("0") else sunrise,
        sunset = if(clockFormat == "hh:mm a") sunset.removePrefix("0") else sunset
    )
}