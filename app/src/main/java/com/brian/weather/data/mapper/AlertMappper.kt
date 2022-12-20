package com.brian.weather.data.mapper

import com.brian.weather.data.remote.dto.Alert
import com.brian.weather.domain.model.AlertDomainObject

/**
 * You can either create these mappers as extensions functions such as this, or you can
 * create a companion object in the Data class itself that is passed a DTO object and returns
 * a domain object
 */

fun Alert.asDomainModel(): AlertDomainObject {
    return AlertDomainObject(
        category = category,
        desc = desc
            .replace("\n", " ")
            .replace("*", "\n**")
            .substringBefore("Experimental"), // removing the experimental polygon data from response
        effective = effective,
        event = event,
        expires = expires,
        headline = headline,
        severity = severity,
    )
}
