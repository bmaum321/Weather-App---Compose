package com.brian.weather.repository

import android.content.res.Resources
import com.brian.weather.data.local.WeatherDao
import com.brian.weather.data.local.WeatherEntity
import com.brian.weather.domain.model.WeatherDomainObject
import com.brian.weather.data.mapper.asDomainModel
import com.brian.weather.data.remote.*
import com.brian.weather.data.remote.dto.ForecastContainer
import com.brian.weather.data.remote.dto.Search
import com.brian.weather.data.remote.dto.WeatherContainer
import com.brian.weather.data.settings.AppPreferences
import com.brian.weather.data.settings.PreferencesRepository
import kotlinx.coroutines.flow.Flow


class WeatherRepositoryImpl(
    private val weatherApi: WeatherApi,
    private val weatherDao: WeatherDao
) : WeatherRepository {

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
        preferences: AppPreferences
    ): List<WeatherDomainObject> {
        val weatherDomainObjects = mutableListOf<WeatherDomainObject>()
        zipcodes.forEach { zipcode ->
            val response = getWeather(zipcode)
            response.onSuccess {
                weatherDomainObjects.add(it.asDomainModel(zipcode, preferences))
            }.onError { code, message ->
                println(message)
            }.onException {
                println(it.message)
            }
        }
        return weatherDomainObjects

    }

    override fun getZipCodesFromDatabase() = weatherDao.getZipcodes()

    override fun getZipcodesFromDatabaseAsFlow() = weatherDao.getZipcodesFlow()

    override fun getWeatherByZipcode(location: String) = weatherDao.getWeatherByZipcode(location)

    override fun getAllWeatherEntities(): Flow<List<WeatherEntity>> = weatherDao.getAllWeatherEntities()

    override suspend fun updateWeather(id: Long, name: String, zipcode: String, sortOrder: Int) = weatherDao
        .update(WeatherEntity(
            id = id,
            cityName = name,
            zipCode = zipcode,
            sortOrder = sortOrder
        ))

    override suspend fun deleteWeather(weatherEntity: WeatherEntity) = weatherDao.delete(weatherEntity)

    override fun selectLastEntryInDb(): WeatherEntity? = weatherDao.selectLastEntry()

    override fun isDbEmpty() = weatherDao.isEmpty()

    override suspend fun insert(weatherEntity: WeatherEntity) = weatherDao.insert(weatherEntity)
}