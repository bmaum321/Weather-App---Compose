package com.brian.weather.data.mapper

import android.content.res.Resources
import com.brian.weather.data.remote.dto.ForecastContainer
import com.brian.weather.domain.model.ForecastDomainObject

fun ForecastContainer.asDomainModel(
    clockFormat: String,
    dateFormat: String,
    resources: Resources
): ForecastDomainObject {
    return ForecastDomainObject(
        days = forecast.forecastday.map { it.toDomainModel(clockFormat, dateFormat, resources) },
        alerts = alerts.alert.map { it.asDomainModel() }
    )
}
