package com.brian.weathercompose.repository

import android.content.SharedPreferences
import android.content.res.Resources
import com.brian.weathercompose.domain.model.WeatherDomainObject
import com.brian.weathercompose.data.mapper.asDomainModel
import com.brian.weathercompose.data.remote.*
import com.brian.weathercompose.data.remote.dto.ForecastContainer
import com.brian.weathercompose.data.remote.dto.Search
import com.brian.weathercompose.data.remote.dto.WeatherContainer
import com.brian.weathercompose.data.settings.PreferencesRepository


class WeatherRepositoryImpl(private val weatherApi: WeatherApi) : WeatherRepository {

    override suspend fun getWeather(zipcode: String): NetworkResult<WeatherContainer> =
            weatherApi.retrofitService.getWeather(zipcode)

    override suspend fun getForecast(zipcode: String): NetworkResult<ForecastContainer> =
            weatherApi.retrofitService.getForecast(zipcode)

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