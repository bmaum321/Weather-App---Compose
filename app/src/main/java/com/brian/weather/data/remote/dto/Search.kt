package com.brian.weather.data.remote.dto

import kotlinx.serialization.Serializable


@Serializable
data class Search(
    val id: Int,
    val name: String,
    val region: String,
    val country: String,
    val lat: Double,
    val lon: Double,
    val url: String,
)
