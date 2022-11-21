package com.brian.weathercompose.data.mapper

import com.brian.weathercompose.data.remote.dto.Alert
import com.brian.weathercompose.domain.model.AlertDomainObject

fun Alert.asDomainModel(): AlertDomainObject {
    return AlertDomainObject(
        category = category,
        desc = desc,
        effective = effective,
        event = event,
        expires = expires,
        headline = headline,
        severity = severity,
    )
}
