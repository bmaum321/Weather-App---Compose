package com.brian.weather.repository

import android.content.res.Resources
import androidx.compose.ui.graphics.Color
import com.brian.weather.data.local.WeatherEntity
import com.brian.weather.data.mapper.asDomainModel
import com.brian.weather.data.mapper.toDomainModel
import com.brian.weather.data.remote.NetworkResult
import com.brian.weather.data.remote.dto.*
import com.brian.weather.data.settings.AppPreferences
import com.brian.weather.data.settings.PreferencesRepository
import com.brian.weather.domain.model.DayDomainObject
import com.brian.weather.domain.model.ForecastDomainObject
import com.brian.weather.domain.model.WeatherDomainObject
import com.brian.weather.repository.WeatherRepository
import kotlinx.coroutines.flow.Flow

class FakeWeatherRepository: WeatherRepository {
    private var shouldReturnNetworkError = false
    private var shouldReturnException = false
    private var shouldReturnEmptyLocationsList = false

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
        condition = Condition(code = 1003, icon = "//cdn.weatherapi.com/weather/64x64/day/116.png", text = "Cloudy"),
        feelslike_c = 123.2,
        feelslike_f = 12.2,
        humidity = 1,
        is_day = 1,
        temp_c = 23.3,
        temp_f = 12.2,
        uv = 12.3,
        wind_dir = "SW",
        wind_kph = 12.2,
        wind_mph = 12.2
    )

    val weatherItems = mutableListOf(WeatherDomainObject(
        zipcode = "Miami",
        time = "11:00AM",
        country = "United States of America",
        windDirection = "WNW",
        code = 1003,
        backgroundColors = listOf(Color.White,Color.White),
        conditionText = "Cloudy",
        feelsLikeTemp = "52",
        imgSrcUrl = "//cdn.weatherapi.com/weather/64x64/day/116.png",
        location = "Miami",
        temp = "32",
        textColor = Color.Black,
        windSpeed = 12.2,
        humidity = 1
    ))

    private val forecastDay = ForecastDay(
        mutableListOf(Day(
            date = "2022-12-28",
            day = ForecastForDay(
                condition = Condition(code = 1003, icon = "//cdn.weatherapi.com/weather/64x64/day/116.png", text = "Cloudy"),
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
            ),
            hour = listOf(
                Hour(
                    time_epoch = 2102194181, // change this
                    time = "2022-12-28 00:00",
                    temp_f = 0.0,
                    temp_c = 0.0,
                    is_day = 0,
                    condition = Condition(code = 1003, icon = "//cdn.weatherapi.com/weather/64x64/day/116.png", text = "Cloudy"),
                    wind_mph = 0.0,
                    wind_kph = 0.0,
                    wind_dir = "SW",
                    chance_of_rain = 0,
                    pressure_mb = 0.0,
                    pressure_in = 0.0,
                    will_it_rain = 0,
                    chance_of_snow = 0.0,
                    will_it_snow = 0,
                    precip_mm = 0.0,
                    precip_in = 0.0,
                    feelslike_c = 0.0,
                    feelslike_f = 0.0,
                    windchill_c = 0.0,
                    windchill_f = 0.0
                )
            ),
            astro = Astro(
                sunrise = "07:06 AM",
                sunset = "07:06 AM",
                moon_phase = "Waxing Crescent",
            )

        ))
    )

    private val alertList = AlertList(
        mutableListOf(Alert(
            headline = "Flood",
            category = "Flood",
            severity = "3",
            event = "Flood",
            effective = "",
            expires = "",
            desc = "Flood"
        ))
    )

    val forecastContainer = ForecastContainer(locationData, forecastDay, alertList)
    val forecastDomainObject = forecastContainer.asDomainModel(preferences = AppPreferences(
        tempUnit = "",
        clockFormat = "",
        dateFormat = "",
        windUnit = "",
        dynamicColors = false,
        showAlerts = false,
        measurementUnit = "",
        showNotifications = false,
        showLocalForecast = false,
        showPrecipitationNotifications = false,
        precipitationLocations = setOf(),
        cardSize = ""
    )
    )

    fun setShouldReturnNetworkError(value: Boolean) {
        shouldReturnNetworkError = value
    }

    fun setShouldReturnException(value: Boolean){
        shouldReturnException = value
    }

    fun setShouldReturnEmptyList(value: Boolean) {
        shouldReturnEmptyLocationsList = value
    }

    override suspend fun getWeather(zipcode: String): NetworkResult<WeatherContainer> {
        return if(shouldReturnNetworkError) {
            NetworkResult.Failure(code = 400, message = "Error")
        } else if(shouldReturnException) {
           NetworkResult.Exception(e = Throwable(message = "Exception"))
        }  else {
            NetworkResult.Success(data = WeatherContainer(locationData, currentWeatherData))
        }
    }

    override suspend fun getForecast(zipcode: String): NetworkResult<ForecastContainer> {
        return if(shouldReturnNetworkError) {
            NetworkResult.Failure(code = 400, message = "Error")
        }else if(shouldReturnException) {
            NetworkResult.Exception(e = Throwable(message = "Exception"))
        } else {
           // NetworkResult.Success(data = ForecastContainer(locationData, forecastDay, alertList))
            NetworkResult.Success(data = forecastContainer)

        }
    }


    override suspend fun getSearchResults(location: String): NetworkResult<List<Search>> {
        return if(shouldReturnNetworkError) {
            NetworkResult.Failure(code = 400, message = "Error")
        }else if(shouldReturnException) {
            NetworkResult.Exception(e = Throwable(message = "Exception"))
        } else {
            NetworkResult.Success(data = listOf(
                Search(
                    id = 2557386,
                    name = "Miami",
                    region = "Florida",
                    country = "United States of America",
                    lat = 25.77,
                    lon = -80.19,
                    url = "miami-florida-united-states-of-america"
                )
            ))
        }
    }

    override suspend fun getWeatherListForZipCodes(
        zipcodes: List<String>,
        preferences: AppPreferences
    ): List<WeatherDomainObject> {
        return weatherItems
    }

    override fun getZipCodesFromDatabase(): List<String> {
        return if(shouldReturnEmptyLocationsList) {
            emptyList()
        } else listOf("13088")
    }

    override fun getZipcodesFromDatabaseAsFlow(): Flow<List<String>> {
        TODO("Not yet implemented")
    }

    override fun getWeatherByZipcode(location: String): Flow<WeatherEntity?> {
        TODO("Not yet implemented")
    }

    override fun getAllWeatherEntities(): Flow<List<WeatherEntity>> {
        TODO("Not yet implemented")
    }

    override suspend fun updateWeather(id: Long, name: String, zipcode: String, sortOrder: Int) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteWeather(weatherEntity: WeatherEntity) {
        TODO("Not yet implemented")
    }

    override fun selectLastEntryInDb(): WeatherEntity? {
        TODO("Not yet implemented")
    }

    override fun isDbEmpty(): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun insert(weatherEntity: WeatherEntity) {
        TODO("Not yet implemented")
    }

}
