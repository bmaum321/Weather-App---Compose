package com.brian.weathercompose.data.remote.dto

import com.brian.weathercompose.data.local.WeatherEntity
import kotlinx.serialization.Serializable


/**
 * DataTransferObjects go in this file. These are responsible for parsing responses from the server
 * or formatting objects to send to the server. You should convert these to domain objects before
 * using them.
 *
 * @see domain package for
 */


@Serializable
data class WeatherContainer(
    val location: LocationData,
    val current: CurrentWeatherData
)

/**
 * Convert Network results to database objects
 */
fun WeatherContainer.asDatabaseModel(
    zipcode: String,
    dbSortOrder: Int
): WeatherEntity {
    return WeatherEntity(
        zipCode = zipcode,
        cityName = location.name,
        sortOrder = dbSortOrder
    )
}







