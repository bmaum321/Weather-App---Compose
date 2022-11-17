package com.brian.weathercompose.repository

import android.content.SharedPreferences
import android.content.res.Resources
import com.brian.weathercompose.data.WeatherDatabase
import com.brian.weathercompose.domain.WeatherDomainObject
import com.brian.weathercompose.domain.asDomainModel
import com.brian.weathercompose.model.ForecastContainer
import com.brian.weathercompose.model.Search
import com.brian.weathercompose.network.*

interface WeatherRepository {

    // The only thing we should be storing into the database is zipcode and city name, everything
    // else is dynamic

    suspend fun getWeatherWithErrorHandling(zipcode: String): ApiResponse<WeatherContainer>

    suspend fun getForecast(zipcode: String): ApiResponse<ForecastContainer>

    suspend fun getSearchResults(location: String): ApiResponse<List<Search>>

    suspend fun getWeather(zipcode: String, resources: Resources, sharedPreferences: SharedPreferences): WeatherDomainObject

    suspend fun getWeatherListForZipCodes(zipcodes: List<String>, resources: Resources, sharedPreferences: SharedPreferences): List<WeatherDomainObject>

}