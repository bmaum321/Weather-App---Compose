package com.brian.weather.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class Location(val location: LocationData)

@Serializable
data class LocationData(
    val name: String,
    val region: String,
    val country: String,
    val lat: Double,
    val lon: Double,
    val tz_id: String,
    val localtime_epoch: Long,
    val localtime: String
)