package com.brian.weather.data.mapper

import android.content.res.Resources
import androidx.compose.ui.graphics.Color
import com.brian.weather.R
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

fun Day.toDomainModel(
    clockFormat: String,
    dateFormat: String,
    resources: Resources
): DaysDomainObject {

    /**
     * Remove any hours that are in the past
     * Convert daily timestamp from API into day of week for the daily forecast
     * Convert hourly timestamp from API from 24hr format to 12hr format
     */

    val currentEpochTime = System.currentTimeMillis() / 1000 - 3600
    val today = LocalDate.now().dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
    val dayOfWeek = LocalDate.parse(date).dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
    val month = LocalDate.parse(date).month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
    val dayOfMonth = LocalDate.parse(date).dayOfMonth.toString()
    val shortDate =
        if (dateFormat == "MM/DD") "$month $dayOfMonth"
        else "$dayOfMonth $month"

    return DaysDomainObject(
        date = if (dayOfWeek == today) "Today"
        else "$dayOfWeek \n$shortDate",
        day = day
            .toDomainModel(),
        hours = hour
            .filter { it.time_epoch > currentEpochTime }
            .map { it.toDomainModel(clockFormat, resources) },
        astroData = astro
            .toDomainModel(clockFormat)
    )
}

fun ForecastForDay.toDomainModel(): DayDomainObject {

    var textColor = Color.White

    val conditionColors = when (condition.code) {
        1000 -> listOf(Color(0xfff5f242), Color(0xffff9100))// sunny
        1003 -> listOf(Color(0xffffffff), Color(0xffffbb00)) // partly cloudy day
        in 1006..1030 -> listOf(Color.Gray, Color.DarkGray) // clouds/overcast
        in 1063..1117 -> listOf(Color(0xff575757), Color(0xff1976d2)) // rain
        in 1150..1207 -> listOf(Color(0xff575757), Color(0xff1976d2))// rain
        in 1210..1237 -> listOf(Color.White, Color.Gray) //snow
        in 1255..1258 -> listOf(Color.White, Color.Gray) // moderate snow
        in 1240..1254 -> listOf(Color(0xff575757), Color(0xff1976d2)) // rain
        in 1260..1282 -> listOf(Color(0xff575757), Color(0xff1976d2)) // rain
        else -> listOf(Color.White, Color.Gray)
    }
    if (conditionColors == listOf(Color(0xfff5f242), Color(0xffff9100)) ||
        // day.day.backgroundColors == listOf(Color.Gray, Color.DarkGray) ||
        conditionColors == listOf(Color.White, Color.Gray) ||
        conditionColors == listOf(Color(0xffffffff), Color(0xffffbb00))
    ) {
        textColor = Color.Black
    }


    return DayDomainObject(
        condition = condition,
        avgtemp_c = avgtemp_c,
        avgtemp_f = avgtemp_f,
        maxtemp_c = maxtemp_c,
        maxtemp_f = maxtemp_f,
        mintemp_c = mintemp_c,
        mintemp_f = mintemp_f,
        daily_chance_of_rain = daily_chance_of_rain,
        backgroundColors = conditionColors,
        textColor = textColor,
        daily_chance_of_snow = daily_chance_of_snow,
        totalprecip_in = totalprecip_in,
        totalprecip_mm = totalprecip_mm,
        avghumidity = avghumidity
    )
}

fun Astro.toDomainModel(clockFormat: String): AstroDataDomainObject {
    // These need to observe the clock format setting
    val sunrise = LocalTime
        .parse(sunrise, DateTimeFormatter.ofPattern("hh:mm a", Locale.US))
        .format(DateTimeFormatter.ofPattern(clockFormat))
    val sunset = LocalTime
        .parse(sunset, DateTimeFormatter.ofPattern("hh:mm a", Locale.US))
        .format(DateTimeFormatter.ofPattern(clockFormat))


    // Remove 0 prefix if using 12 hour time format
    return AstroDataDomainObject(
        moon_phase = moon_phase,
        sunrise = if (clockFormat == "hh:mm a") sunrise.removePrefix("0") else sunrise,
        sunset = if (clockFormat == "hh:mm a") sunset.removePrefix("0") else sunset
    )
}