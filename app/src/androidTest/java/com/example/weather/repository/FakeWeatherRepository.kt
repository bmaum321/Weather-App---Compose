package com.example.weather.repository

import android.content.res.Resources
import androidx.compose.ui.graphics.Color
import com.brian.weather.data.remote.NetworkResult
import com.brian.weather.data.remote.dto.*
import com.brian.weather.data.settings.PreferencesRepository
import com.brian.weather.domain.model.DayDomainObject
import com.brian.weather.domain.model.WeatherDomainObject
import com.brian.weather.repository.WeatherRepository

class FakeWeatherRepository: WeatherRepository {
    private var shouldReturnNetworkError = false

    private val locationData = LocationData(
        country = "United States of America",
        lat = 213.3,
        localtime = "2022-12-27 11:29",
        localtime_epoch = 1672158565,
        lon = 123.4,
        tz_id = "America/New_York",
        name = "Miami",
        region = "Florida"
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
        zipcode = "13088",
        time = "",
        country = "United States of America",
        windDirection = "WNW",
        code = 1,
        backgroundColors = emptyList(),
        conditionText = "Cloudy",
        feelsLikeTemp = "52",
        imgSrcUrl = "",
        location = "Miami",
        temp = "32",
        textColor = Color.Black,
        windSpeed = 12.2,
        humidity = 1
    ))

    private val forecastDay = ForecastDay(
        mutableListOf(Day(
            date = "Today", day = ForecastForDay(
                condition = Condition(code = 1, icon = "", text = "Cloudy"),
                avgtemp_f = 0.0,
                maxtemp_f = 0.0,
                mintemp_f = 0.0,
                avgtemp_c = 0.0,
                maxtemp_c = 0.0,
                mintemp_c = 0.0,
                daily_chance_of_rain = 0.0,
                daily_chance_of_snow = 0.0,
                totalprecip_in = 0.0,
                totalprecip_mm = 0.0,
                avghumidity = 0.0,
            ), hour = listOf(), astro = Astro(
                sunrise = "",
                sunset = "",
                moon_phase = "",
            )

        ))
    )

    private val alertList = AlertList(
        mutableListOf(Alert(
            headline = "",
            category = "",
            severity = "",
            event = "",
            effective = "",
            expires = "",
            desc = ""
        ))
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

    override suspend fun getWeatherListForZipCodes(
        zipcodes: List<String>,
        resources: Resources,
        preferencesRepository: PreferencesRepository
    ): List<WeatherDomainObject> {
        return weatherItems
    }

}
