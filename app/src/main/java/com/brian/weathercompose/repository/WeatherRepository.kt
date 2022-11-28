package com.brian.weathercompose.repository

import android.content.SharedPreferences
import android.content.res.Resources
import com.brian.weathercompose.data.remote.NetworkResult
import com.brian.weathercompose.domain.model.WeatherDomainObject
import com.brian.weathercompose.data.remote.dto.ForecastContainer
import com.brian.weathercompose.data.remote.dto.Search
import com.brian.weathercompose.data.remote.dto.WeatherContainer
import com.brian.weathercompose.presentation.screens.settings.SettingsDatastore

interface WeatherRepository {

    // The only thing we should be storing into the database is zipcode and city name, everything
    // else is dynamic

    suspend fun getWeather(zipcode: String): NetworkResult<WeatherContainer>

    suspend fun getForecast(zipcode: String): NetworkResult<ForecastContainer>

    suspend fun getSearchResults(location: String): NetworkResult<List<Search>>

    suspend fun getWeatherListForZipCodes(
        zipcodes: List<String>,
        resources: Resources,
        sharedPreferences: SharedPreferences,
        settingsDatastore: SettingsDatastore
    ): List<WeatherDomainObject>

}