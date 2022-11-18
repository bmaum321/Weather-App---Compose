package com.brian.weathercompose.repository

import android.content.SharedPreferences
import android.content.res.Resources
import com.brian.weathercompose.domain.WeatherDomainObject
import com.brian.weathercompose.domain.asDomainModel
import com.brian.weathercompose.data.remote.dto.ForecastContainer
import com.brian.weathercompose.data.remote.dto.Search
import com.brian.weathercompose.data.remote.dto.WeatherContainer
import com.brian.weathercompose.network.ApiResponse
import com.brian.weathercompose.network.WeatherApi
import com.brian.weathercompose.network.handleApi


class WeatherRepositoryImpl(private val weatherApi: WeatherApi) : WeatherRepository {

    override suspend fun getWeatherWithErrorHandling(zipcode: String): ApiResponse<WeatherContainer> =
        handleApi {
            weatherApi.retrofitService.getWeatherWithErrorHandling(zipcode)
        }

    override suspend fun getForecast(zipcode: String): ApiResponse<ForecastContainer> =
        handleApi {
            weatherApi.retrofitService.getForecast(zipcode)
        }

    override suspend fun getSearchResults(location: String): ApiResponse<List<Search>> =
        handleApi {
            weatherApi.retrofitService.locationSearch(location)
        }

    override suspend fun getWeather(
        zipcode: String,
        resources: Resources,
        sharedPreferences: SharedPreferences
    ): WeatherDomainObject {
        val weatherData = weatherApi.retrofitService.getWeather(zipcode)
        return weatherData
            .asDomainModel(zipcode, resources, sharedPreferences)
    }

    override suspend fun getWeatherListForZipCodes(
        zipcodes: List<String>,
        resources: Resources,
        sharedPreferences: SharedPreferences
    ): List<WeatherDomainObject> {
        val weatherDomainObjects = mutableListOf<WeatherDomainObject>()
        zipcodes.forEach { zipcode ->
            weatherDomainObjects.add(getWeather(zipcode, resources, sharedPreferences)) // this should use error handling and do the when block here
        }
        return weatherDomainObjects
    }

}