package com.brian.weather.data.mapper

import android.content.res.Resources
import androidx.compose.ui.graphics.Color
import com.brian.weather.R
import com.brian.weather.data.remote.dto.Hour
import com.brian.weather.data.settings.AppPreferences
import com.brian.weather.domain.model.HoursDomainObject
import java.time.LocalTime
import java.time.format.DateTimeFormatter

fun Hour.toDomainModel(
    preferences: AppPreferences,
    resources: Resources
): HoursDomainObject {


    val formattedTime = LocalTime.parse(
        time.substring(11) // Remove date from time
    ).format(DateTimeFormatter.ofPattern(preferences.clockFormat))// Add AM/PM postfix
    var textColor = Color.White

    val conditionColors = when(condition.code) {
        1000 -> {
            if (condition.text == resources.getString(R.string.Sunny)) {
                listOf(Color(0xfff5f242),Color(0xffff9100))// sunny
            } else listOf(Color(0xff000000),Color(0x472761CC)) // clear night
        }
        1003 -> if (is_day == 1) {
            listOf(Color(0xffffffff),Color(0xffffbb00)) // partly cloudy day
        } else {
            listOf(Color(0xff575757),Color(0x472761CC)) // partly cloud night
        } // partly cloudy night
        in 1006..1030 -> listOf(Color.Gray, Color.DarkGray) // clouds/overcast
        in 1063..1113 -> listOf(Color(0xff575757), Color(0xff1976d2)) // rain
        in 1114..1117 -> listOf(Color.White, Color.Gray) // Blizzard
        in 1150..1207 -> listOf(Color(0xff575757),Color(0xff1976d2))// rain
        in 1210..1237 -> listOf(Color.White, Color.Gray) //snow
        in 1255..1258 -> listOf(Color.White, Color.Gray) // moderate snow
        in 1240..1254 -> listOf(Color(0xff575757),Color(0xff1976d2)) // rain
        in 1260..1282 -> listOf(Color(0xff575757),Color(0xff1976d2)) // rain
        else -> listOf(Color.White, Color.Gray)
    }

    if (conditionColors == listOf(Color(0xfff5f242), Color(0xffff9100)) ||
        // day.day.backgroundColors == listOf(Color.Gray, Color.DarkGray) ||
        conditionColors == listOf(Color.White, Color.Gray) ||
        conditionColors== listOf(Color(0xffffffff), Color(0xffffbb00))
    ) {
        textColor = Color.Black
    }



    return HoursDomainObject(
        time_epoch = time_epoch,
        time = if(preferences.clockFormat == "hh:mm a") formattedTime.removePrefix("0")
        else formattedTime,
        temp = if(preferences.tempUnit == "Fahrenheit")temp_f else temp_c,
        is_day = is_day,
        condition = condition,
        windspeed = if(preferences.windUnit == "MPH") "$wind_mph MPH" else "$wind_kph KPH",
        wind_dir = wind_dir,
        chance_of_rain = chance_of_rain,
        pressure = if(preferences.measurementUnit == "IN")"$pressure_in IN" else "$pressure_mb MB",
        will_it_rain = will_it_rain,
        chance_of_snow = chance_of_snow,
        will_it_snow = will_it_snow,
        precip = if(preferences.measurementUnit == "IN")"$precip_in IN" else "$precip_mm MM",
        feelslike = if(preferences.tempUnit == "Fahrenheit")feelslike_f else feelslike_c,
        windchill = if(preferences.tempUnit == "Fahrenheit")windchill_f else windchill_c,
        colors = conditionColors,
        textColor = textColor
    )
}