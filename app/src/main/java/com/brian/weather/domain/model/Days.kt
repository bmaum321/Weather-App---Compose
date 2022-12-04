package com.brian.weather.domain.model

data class DaysDomainObject(
    var date: String,
    val day: DayDomainObject,
    val hours: MutableList<HoursDomainObject>,
    val astroData: AstroDataDomainObject
)
