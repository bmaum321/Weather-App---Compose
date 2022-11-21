package com.brian.weathercompose.domain.model

data class AlertDomainObject(
    val headline: String,
    val category: String,
    val severity: String,
    val event: String,
    val effective: String,
    val expires: String,
    var desc: String
)