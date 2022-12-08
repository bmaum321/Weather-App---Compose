package com.brian.weather.data.mapper

import androidx.compose.ui.graphics.Color
import com.brian.weather.data.remote.dto.Astro
import com.brian.weather.data.remote.dto.Day
import com.brian.weather.data.remote.dto.ForecastForDay
import com.brian.weather.domain.model.AstroDataDomainObject
import com.brian.weather.domain.model.DayDomainObject
import com.brian.weather.domain.model.DaysDomainObject
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*

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

        // TODO If time format is 12 hour remove 0 prefix
        sunrise = LocalTime
            .parse(sunrise, DateTimeFormatter.ofPattern("hh:mm a" , Locale.US))
            .format(DateTimeFormatter.ofPattern(clockFormat)),
        sunset = LocalTime
            .parse(sunset, DateTimeFormatter.ofPattern("hh:mm a" , Locale.US))
            .format(DateTimeFormatter.ofPattern(clockFormat))
    )
}