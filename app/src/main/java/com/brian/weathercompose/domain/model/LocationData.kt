package com.brian.weathercompose.domain.model

data class LocationDataDomainModel(
    val name: String,
    val region: String,
    var country: String,
    val lat: Double,
    val lon: Double,
    val tz_id: String,
    val localtime_epoch: Long,
    var localtime: String
)
