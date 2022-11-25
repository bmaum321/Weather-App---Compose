package com.example.weathercompose.repository

import android.content.SharedPreferences
import android.content.res.Resources
import androidx.compose.ui.graphics.Color
import com.brian.weathercompose.data.remote.NetworkResult
import com.brian.weathercompose.data.remote.dto.*
import com.brian.weathercompose.domain.model.WeatherDomainObject
import com.brian.weathercompose.repository.WeatherRepository

class FakeWeatherRepository: WeatherRepository {
    private var shouldReturnNetworkError = false

    private val locationData = LocationData(
        country = "",
        lat = 213.3,
        localtime = "",
        localtime_epoch = 123,
        lon = 123.4,
        tz_id = "",
        name = "",
        region = ""
    )

    private val currentWeatherData = CurrentWeatherData(
        condition = Condition(code = 1, icon = "", text = ""),
        feelslike_c = 123.2,
        feelslike_f = 12.2,
        humidity = 1,
        is_day = 1,
        temp_c = 23.3,
        temp_f = 12.2,
        uv = 12.3,
        wind_dir = "",
        wind_kph = 12.2,
        wind_mph = 12.2
    )

    private val weatherItems = mutableListOf(WeatherDomainObject(
        zipcode = "",
        time = "",
        country = "",
        windDirection = "",
        code = 1,
        backgroundColors = emptyList(),
        conditionText = "",
        feelsLikeTemp = "",
        imgSrcUrl = "",
        location = "",
        temp = "",
        textColor = Color.Black,
        windSpeed = 12.2
    ))

    private val forecastDay = ForecastDay(
        emptyList()
    )

    private val alertList = AlertList(
        emptyList()
    )

    fun setShouldReturnNetworkError(value: Boolean) {
        shouldReturnNetworkError = value
    }

    override suspend fun getWeather(zipcode: String): NetworkResult<WeatherContainer> {
        return if(shouldReturnNetworkError) {
            NetworkResult.Failure(code = 400, message = "Error")
        } else {
            NetworkResult.Success(data = WeatherContainer(locationData, currentWeatherData))
        }
    }

    override suspend fun getForecast(zipcode: String): NetworkResult<ForecastContainer> {
        return if(shouldReturnNetworkError) {
            NetworkResult.Failure(code = 400, message = "Error")
        } else {
            NetworkResult.Success(data = ForecastContainer(locationData, forecastDay, alertList))
        }
    }


    override suspend fun getSearchResults(location: String): NetworkResult<List<Search>> {
        return if(shouldReturnNetworkError) {
            NetworkResult.Failure(code = 400, message = "Error")
        } else {
            NetworkResult.Success(data = emptyList())
        }
    }

    override suspend fun getWeather(
        zipcode: String,
        resources: Resources,
        sharedPreferences: SharedPreferences
    ): WeatherDomainObject {
        return weatherItems.random()
    }

    override suspend fun getWeatherListForZipCodes(
        zipcodes: List<String>,
        resources: Resources,
        sharedPreferences: SharedPreferences
    ): List<WeatherDomainObject> {
        return weatherItems
    }
}
