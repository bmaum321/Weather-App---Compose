package com.brian.weather.data.remote.dto

import com.brian.weather.data.local.WeatherEntity
import kotlinx.serialization.SerialName
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
    @SerialName("location")val locationData: LocationData,
    @SerialName("current")val currentWeatherData: CurrentWeatherData
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
        cityName = locationData.name,
        sortOrder = dbSortOrder
    )
}







