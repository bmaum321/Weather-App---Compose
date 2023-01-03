package com.brian.weather.repository

import android.content.res.Resources
import com.brian.weather.data.local.WeatherEntity
import com.brian.weather.data.remote.NetworkResult
import com.brian.weather.domain.model.WeatherDomainObject
import com.brian.weather.data.remote.dto.ForecastContainer
import com.brian.weather.data.remote.dto.Search
import com.brian.weather.data.remote.dto.WeatherContainer
import com.brian.weather.data.settings.AppPreferences
import com.brian.weather.data.settings.PreferencesRepository
import kotlinx.coroutines.flow.Flow

interface WeatherRepository {

    // The only thing we should be storing into the database is zipcode and city name, everything
    // else is dynamic

    suspend fun getWeather(zipcode: String): NetworkResult<WeatherContainer>

    suspend fun getForecast(zipcode: String): NetworkResult<ForecastContainer>

    suspend fun getSearchResults(location: String): NetworkResult<List<Search>>

    suspend fun getWeatherListForZipCodes(
        zipcodes: List<String>,
        preferences: AppPreferences
    ): List<WeatherDomainObject>

    fun getZipCodesFromDatabase(): List<String>

    fun getZipcodesFromDatabaseAsFlow(): Flow<List<String>>

    fun getWeatherByZipcode(location: String): Flow<WeatherEntity?>

    fun getAllWeatherEntities(): Flow<List<WeatherEntity>>

    suspend fun updateWeather(
        id: Long,
        name: String,
        zipcode: String,
        sortOrder: Int
    )

    suspend fun deleteWeather(weatherEntity: WeatherEntity)

    fun selectLastEntryInDb(): WeatherEntity?

    fun isDbEmpty(): Boolean

    suspend fun insert(weatherEntity: WeatherEntity)

}