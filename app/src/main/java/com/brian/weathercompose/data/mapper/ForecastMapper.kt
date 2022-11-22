package com.brian.weathercompose.data.mapper

import android.content.SharedPreferences
import android.content.res.Resources
import com.brian.weathercompose.R
import com.brian.weathercompose.data.remote.dto.ForecastContainer
import com.brian.weathercompose.domain.model.ForecastDomainObject
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

fun ForecastContainer.asDomainModel(
    sharedPreferences: SharedPreferences,
    resources: Resources
): ForecastDomainObject {

    /**
     * Remove any hours that are in the past
     * Convert daily timestamp from API into day of week for the daily forecast
     * Convert hourly timestamp from API from 24hr format to 12hr format
     */

    // Subtracting an hour from current time to see the current hour in the forecast
    val forecastDayDomainObjectList = forecast.forecastday.map { it.toDomainModel() }
    val currentEpochTime = System
        .currentTimeMillis() / 1000 - 3600
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
                        .format(DateTimeFormatter.ofPattern("hh:mm a"))// Add AM/PM postfix
                        .removePrefix("0") // Remove 0 prefix, Ex: Turn 01:00 PM into 1:00PM
                }
        }

    // Format the alert text from API
    val alertsAsDomainObjectList = alerts.alert.map { it.asDomainModel() }


    return ForecastDomainObject(
        days = forecastDayDomainObjectList,
        alerts = alertsAsDomainObjectList
    )
}
