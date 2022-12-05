package com.brian.weather.repository

import android.content.res.Resources
import com.brian.weather.domain.model.WeatherDomainObject
import com.brian.weather.data.mapper.asDomainModel
import com.brian.weather.data.remote.*
import com.brian.weather.data.remote.dto.ForecastContainer
import com.brian.weather.data.remote.dto.Search
import com.brian.weather.data.remote.dto.WeatherContainer
import com.brian.weather.data.settings.PreferencesRepository


class WeatherRepositoryImpl(private val weatherApi: WeatherApi) : WeatherRepository {

    override suspend fun getWeather(zipcode: String): NetworkResult<WeatherContainer> =
            weatherApi.retrofitService.getWeather(zipcode)

    override suspend fun getForecast(zipcode: String): NetworkResult<ForecastContainer> =
        try {
            weatherApi.retrofitService.getForecast(zipcode)
        } catch (e: Exception) {
            weatherApi.retrofitService.getForecast(zipcode, days = 3)
        }


    override suspend fun getSearchResults(location: String): NetworkResult<List<Search>> =
            weatherApi.retrofitService.locationSearch(location)


    override suspend fun getWeatherListForZipCodes(
        zipcodes: List<String>,
        resources: Resources,
        preferencesRepository: PreferencesRepository
    ): List<WeatherDomainObject> {
        val weatherDomainObjects = mutableListOf<WeatherDomainObject>()
        zipcodes.forEach { zipcode ->
            val response = getWeather(zipcode)
            response.onSuccess {
                weatherDomainObjects.add(it.asDomainModel(zipcode, resources, preferencesRepository))
            }.onError { code, message ->
                println(message)
            }.onException {
                println(it.message)
            }
        }
        return weatherDomainObjects

    }

}