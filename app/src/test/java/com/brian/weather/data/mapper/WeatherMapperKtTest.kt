package com.brian.weather.data.mapper

import android.content.res.Resources
import androidx.compose.ui.graphics.Color
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.brian.weather.data.remote.dto.Condition
import com.brian.weather.data.remote.dto.CurrentWeatherData
import com.brian.weather.data.remote.dto.LocationData
import com.brian.weather.data.remote.dto.WeatherContainer
import com.brian.weather.data.settings.AppPreferences
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.mockito.Mockito.mock
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@RunWith(AndroidJUnit4::class)
class WeatherMapperKtTest {

    /**
     * Getting koin error when trying to run unit tests, this seems to be needed
     */

    @Before
    fun before() {
        stopKoin()
    }

    private val resources: Resources = mock(Resources::class.java)
    private val preferences = AppPreferences(
        tempUnit = "Fahrenheit",
        clockFormat = "hh:mm a",
        dateFormat = "MM/DD",
        windUnit = "MPH",
        dynamicColors = false,
        showAlerts = false,
        measurementUnit = "IN",
        showNotifications = false,
        showLocalForecast = false,
        showPrecipitationNotifications = false,
        precipitationLocations = setOf()
    )

    private val preferences2 = AppPreferences(
        tempUnit = "Celsius",
        clockFormat = "kk:mm",
        dateFormat = "DD/MM",
        windUnit = "KPH",
        dynamicColors = false,
        showAlerts = false,
        measurementUnit = "MM",
        showNotifications = false,
        showLocalForecast = false,
        showPrecipitationNotifications = false,
        precipitationLocations = setOf()
    )

    private val weather = WeatherContainer(
        location = LocationData(
            name = "Liverpool",
            region = "New York",
            country = "United States of America",
            lat = 0.0,
            lon = 0.0,
            tz_id = "America/New_York",
            localtime_epoch = System.currentTimeMillis() / 1000 + 3600,
            localtime = "2022-12-30 10:29",
        ), current = CurrentWeatherData(
            temp_c = 10.0,
            temp_f = 32.0,
            is_day = 1,
            wind_mph = 20.0,
            wind_kph = 10.0,
            wind_dir = "NE",
            uv = 0.0,
            humidity = 0,
            feelslike_f = 20.0,
            feelslike_c = 10.0,
            condition = Condition(
                code = 1003,
                icon = "//cdn.weatherapi.com/weather/64x64/day/116.png",
                text = "Partly Cloudy"
            ),
        )
    )

    private var weatherDomainObject =
        weather.asDomainModel(
            zipcode = "13088",
            preferences = preferences
        )



    @Test
    fun weatherDto_toDomainModel_returnsCorrectColors(){
        assertEquals(Color.Black, weatherDomainObject.textColor)
        assertEquals(listOf(Color(0xffffffff),Color(0xffffbb00)), weatherDomainObject.backgroundColors)
    }

    @Test
    fun weatherDto_toDomainModel_returnsCorrectTemperatureF() {
        assertEquals("32", weatherDomainObject.temp)
        assertEquals("20", weatherDomainObject.feelsLikeTemp)
    }

    @Test
    fun weatherDto_toDomainModel_returnsCorrectTemperatureC() {
            weatherDomainObject = weather.asDomainModel(
                zipcode = "13088",
                preferences = preferences2
            )

        assertEquals("10", weatherDomainObject.temp)
        assertEquals("10", weatherDomainObject.feelsLikeTemp)

    }

    @Test
    fun weatherDto_toDomainModel_returnsCorrectWindMph() {
        assertEquals(20.0, weatherDomainObject.windSpeed, 0.0)
    }


    @Test
    fun weatherDto_toDomainModel_returnsCorrectWindKph() {

            weatherDomainObject = weather.asDomainModel(
                zipcode = "13088",
                preferences = preferences2
            )

        assertEquals(10.0, weatherDomainObject.windSpeed, 0.0)

    }

    @Test
    fun weatherDto_toDomainModel_returnsCorrectTime() {
        val time = Instant
            .ofEpochSecond(weather.location.localtime_epoch)
            .atZone(ZoneId.of(weather.location.tz_id))
            .format(
                DateTimeFormatter
                    .ofPattern(preferences.clockFormat)
            )
        assertEquals(time.removePrefix("0"), weatherDomainObject.time)
    }

    @Test
    fun weatherDto_toDomainModel_returnsCorrect24HrTime() {

        weatherDomainObject = weather.asDomainModel(
            zipcode = "13088",
            preferences = preferences2
        )
        val time = Instant
            .ofEpochSecond(weather.location.localtime_epoch)
            .atZone(ZoneId.of(weather.location.tz_id))
            .format(
                DateTimeFormatter
                    .ofPattern(preferences2.clockFormat)
            )
        assertEquals(time, weatherDomainObject.time)
    }

}