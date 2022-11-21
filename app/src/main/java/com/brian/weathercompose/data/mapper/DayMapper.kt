package com.brian.weathercompose.data.mapper

import com.brian.weathercompose.data.remote.dto.Day
import com.brian.weathercompose.domain.model.DayDomainObject

fun Day.toDomainModel(): DayDomainObject {
    return DayDomainObject(
        date = date,
        day = day,
        hours = hour.map { it.toDomainModel() }.toMutableList()
    )
}