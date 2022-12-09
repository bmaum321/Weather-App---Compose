package com.brian.weather.data.mapper

import android.content.res.Resources
import android.os.Build
import androidx.compose.ui.graphics.Color
import com.brian.weather.R
import com.brian.weather.data.remote.dto.ForecastContainer
import com.brian.weather.domain.model.ForecastDomainObject
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

fun ForecastContainer.asDomainModel(
    clockFormat: String,
    resources: Resources
): ForecastDomainObject {

    val forecastDayDomainObjectList = forecast.forecastday.map { it.toDomainModel(clockFormat, resources) }

    val alertsAsDomainObjectList = alerts.alert.map { it.asDomainModel() }


    return ForecastDomainObject(
        days = forecastDayDomainObjectList,
        alerts = alertsAsDomainObjectList
    )
}
