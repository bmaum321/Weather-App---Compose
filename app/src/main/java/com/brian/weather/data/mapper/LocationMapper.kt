package com.brian.weather.data.mapper

import com.brian.weather.data.remote.dto.LocationData
import com.brian.weather.domain.model.LocationDataDomainModel

fun LocationData.toDomainModel(): LocationDataDomainModel {
    return LocationDataDomainModel(
        name = name,
        region = region,
        country = country,
        lat = lat,
        lon = lon,
        tz_id = tz_id,
        localtime_epoch = localtime_epoch,
        localtime = localtime
    )
}