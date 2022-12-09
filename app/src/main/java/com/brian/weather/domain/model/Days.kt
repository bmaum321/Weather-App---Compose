package com.brian.weather.domain.model

data class DaysDomainObject(
    val date: String,
    val day: DayDomainObject,
    val hours: List<HoursDomainObject>,
    val astroData: AstroDataDomainObject
)
