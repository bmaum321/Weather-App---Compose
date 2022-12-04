package com.brian.weather.data.mapper

import com.brian.weather.data.remote.dto.LocationData
import com.brian.weather.domain.model.LocationDataDomainModel

fun LocationData.toDomainModel(): LocationDataDomainModel {
    return LocationDataDomainModel(
        name,
        region,
        country,
        lat,
        lon,
        tz_id,
        localtime_epoch,
        localtime
    )
}