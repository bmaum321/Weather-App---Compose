package com.brian.weathercompose.data.mapper

import com.brian.weathercompose.data.remote.dto.Search
import com.brian.weathercompose.domain.model.SearchDomainObject

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