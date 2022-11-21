package com.brian.weathercompose.domain.model

import com.brian.weathercompose.data.remote.dto.ForecastForDay

data class DayDomainObject(
    var date: String,
    val day: ForecastForDay,
    val hours: MutableList<HoursDomainObject>
)
