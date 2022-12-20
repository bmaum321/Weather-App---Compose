package com.brian.weather.domain.model

import com.brian.weather.data.remote.dto.Alert

data class AlertDomainObject(
    val headline: String,
    val category: String,
    val severity: String,
    val event: String,
    val effective: String,
    val expires: String,
    val desc: String
) {
    companion object {
        fun toDomainModel(alert: Alert): AlertDomainObject {
            return AlertDomainObject(
                category = alert.category,
                desc = alert.desc
                    .replace("\n", " ")
                    .replace("*", "\n**")
                    .substringBefore("Experimental"), // removing the experimental polygon data from response
                effective = alert.effective,
                event = alert.event,
                expires = alert.expires,
                headline = alert.headline,
                severity = alert.severity,
            )
        }
    }
}