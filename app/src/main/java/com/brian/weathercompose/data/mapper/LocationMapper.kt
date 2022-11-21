package com.brian.weathercompose.data.mapper

import com.brian.weathercompose.data.remote.dto.LocationData
import com.brian.weathercompose.domain.model.LocationDataDomainModel

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