package com.brian.weather.data.mapper

import com.brian.weather.data.remote.dto.Search
import com.brian.weather.domain.model.SearchDomainObject

fun Search.toDomainModel(): SearchDomainObject {
    return SearchDomainObject(
        id = id,
        country = country,
        lat = lat,
        lon = lon,
        name = name,
        region = region,
        url = url
    )
}