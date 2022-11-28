package com.brian.weathercompose.data.mapper

import android.content.res.Resources
import android.os.Build
import androidx.compose.ui.graphics.Color
import com.brian.weathercompose.R
import com.brian.weathercompose.data.remote.dto.ForecastContainer
import com.brian.weathercompose.data.settings.SettingsRepository
import com.brian.weathercompose.domain.model.ForecastDomainObject
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

suspend fun ForecastContainer.asDomainModel(
    settingsRepository: SettingsRepository,
    resources: Resources
): ForecastDomainObject {

    /**
     * Remove any hours that are in the past
     * Convert daily timestamp from API into day of week for the daily forecast
     * Convert hourly timestamp from API from 24hr format to 12hr format
     */

    val clockFormat = settingsRepository.getClockFormat.first().toString()

    // Subtracting an hour from current time to see the current hour in the forecast
    val forecastDayDomainObjectList = forecast.forecastday.map { it.toDomainModel() }
    val currentEpochTime = System
        .currentTimeMillis() / 1000 - 3600

    var textColor = Color.White
    forecastDayDomainObjectList.forEach { day ->

            /**
             * Remove all method is used to avoid concurrent modification error on collections. Lets you
             * delete items from a collection as you iterate through it
             */
            day.hours.removeAll { hours ->
                    hours.time_epoch < currentEpochTime
                }
            day.date = LocalDate
                .parse(day.date)
                .dayOfWeek
                .getDisplayName(
                    TextStyle.FULL,
                    Locale.ENGLISH
                ) // Convert to day of week
            forecastDayDomainObjectList.first().date = resources.getString(R.string.today)
            day.hours.forEach { hour ->
                    hour.time = LocalTime.parse(
                        hour.time.substring(11) // Remove date from time
                    )
                        .format(DateTimeFormatter.ofPattern(clockFormat))// Add AM/PM postfix
                        .removePrefix("0") // Remove 0 prefix, Ex: Turn 01:00 PM into 1:00PM
                }




        // Dynamic Material colors not supported on < API 31
        if(Build.VERSION.SDK_INT <= 31) {
            day.day.textColor = Color.Black
            // backgroundColor = Color.Transparent
        }
        day.day.backgroundColors = when (day.day.condition.code) {
            1000 -> listOf(Color(0xfff5f242), Color(0xffff9100))// sunny
            1003 -> listOf(Color(0xffffffff), Color(0xffffbb00)) // partly cloudy day
            in 1006..1030 -> listOf(Color.Gray, Color.DarkGray) // clouds/overcast
            in 1063..1116 -> listOf(Color(0xff575757), Color(0xff1976d2)) // rain
            in 1150..1207 -> listOf(Color(0xff575757), Color(0xff1976d2))// rain
            in 1210..1237 -> listOf(Color.White, Color.Gray) //snow
            in 1240..1282 -> listOf(Color(0xff575757), Color(0xff1976d2)) // rain
            else -> listOf(Color.White, Color.Gray)
        }

        // Change text color to black for certain gradients for easier reading
        if (day.day.backgroundColors == listOf(Color(0xfff5f242), Color(0xffff9100)) ||
            day.day.backgroundColors == listOf(Color.Gray, Color.DarkGray) ||
            day.day.backgroundColors == listOf(Color.White, Color.Gray) ||
            day.day.backgroundColors == listOf(Color(0xffffffff), Color(0xffffbb00))
        ) {
            day.day.textColor = Color.Black
        }

    }

    // Format the alert text from API
    val alertsAsDomainObjectList = alerts.alert.map { it.asDomainModel() }


    return ForecastDomainObject(
        days = forecastDayDomainObjectList,
        alerts = alertsAsDomainObjectList
    )
}
