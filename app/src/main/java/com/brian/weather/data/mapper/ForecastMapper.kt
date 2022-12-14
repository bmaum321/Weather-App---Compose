package com.brian.weather.data.mapper

import com.brian.weather.data.remote.dto.ForecastContainer
import com.brian.weather.data.settings.AppPreferences
import com.brian.weather.domain.model.AlertDomainObject
import com.brian.weather.domain.model.ForecastDomainObject

fun ForecastContainer.asDomainModel(
    preferences: AppPreferences
): ForecastDomainObject {
    return ForecastDomainObject(
        days = forecast.dailyForecast
            .map { it.toDomainModel(
                        preferences
                    )
            },
        alerts = alerts.alert.map { AlertDomainObject.toDomainModel(it) }
    )
}
