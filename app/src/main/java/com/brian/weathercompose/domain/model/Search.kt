package com.brian.weathercompose.domain.model

data class SearchDomainObject(
    val id: Int,
    val name: String,
    val region: String,
    val country: String,
    val lat: Double,
    val lon: Double,
    val url: String,
)
