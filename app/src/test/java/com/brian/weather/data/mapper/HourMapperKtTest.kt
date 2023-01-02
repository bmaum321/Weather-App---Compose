package com.brian.weather.data.mapper


import android.content.res.Resources
import androidx.compose.ui.graphics.Color
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.brian.weather.data.remote.dto.Condition
import com.brian.weather.data.remote.dto.Hour
import com.brian.weather.data.settings.AppPreferences
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.mockito.Mockito.mock
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*

@RunWith(AndroidJUnit4::class)
class HourMapperKtTest {

    /**
     * Getting koin error when trying to run unit tests, this seems to be needed
     */
    @Before
    fun before() {
        stopKoin()
    }


    //val context: Context = ApplicationProvider.getApplicationContext()
    //val context = mock(Context::class.java)
    private val resources: Resources = mock(Resources::class.java)

    private val preferencesImperial = AppPreferences(
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

    private val preferencesMetric = AppPreferences(
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

    private val hour = Hour(
        time_epoch = 2102194181, // change this
        time = "2022-12-28 00:00",
        temp_f = 10.0,
        temp_c = 20.0,
        is_day = 1,
        condition = Condition(code = 1003, icon = "//cdn.weatherapi.com/weather/64x64/day/116.png", text = "Cloudy"),
        wind_mph = 10.0,
        wind_kph = 20.0,
        wind_dir = "SW",
        chance_of_rain = 0,
        pressure_mb = 20.0,
        pressure_in = 10.0,
        will_it_rain = 0,
        chance_of_snow = 0.0,
        will_it_snow = 0,
        precip_mm = 20.0,
        precip_in = 10.0,
        feelslike_c = 20.0,
        feelslike_f = 10.0,
        windchill_c = 20.0,
        windchill_f = 10.0
    )

    private val hoursDomainObjectImperialUnits = hour.toDomainModel(preferencesImperial, resources)
    private val hoursDomainObjectMetricUnits = hour.toDomainModel(preferencesMetric, resources)

    @Test
    fun hourDto_toDomainModel_returnsCorrectTemperature() {
        assertEquals(hoursDomainObjectImperialUnits.temp, "10")
        assertEquals(hoursDomainObjectMetricUnits.temp, "20")
    }

    @Test
    fun hourDto_toDomainModel_returnsCorrectWindspeed() {
        assertEquals(hoursDomainObjectImperialUnits.windspeed, "10")
        assertEquals(hoursDomainObjectMetricUnits.windspeed, "20")
    }

    @Test
    fun hourDto_toDomainModel_returnsCorrectPressure() {
        assertEquals(hoursDomainObjectImperialUnits.pressure, "10")
        assertEquals(hoursDomainObjectMetricUnits.pressure, "20")
    }

    @Test
    fun hourDto_toDomainModel_returnsCorrectPrecipitation() {
        assertEquals(hoursDomainObjectImperialUnits.precip, "10")
        assertEquals(hoursDomainObjectMetricUnits.precip, "20")
    }

    @Test
    fun hourDto_toDomainModel_returnsCorrectFeelsLikeTemperature() {
        assertEquals(hoursDomainObjectImperialUnits.feelslike, 10.0, 0.0)
        assertEquals(hoursDomainObjectMetricUnits.feelslike, 20.0, 0.0)
    }

    @Test
    fun hourDto_toDomainModel_returnsCorrectWindChill() {
        assertEquals(hoursDomainObjectImperialUnits.windchill, 10.0, 0.0)
        assertEquals(hoursDomainObjectMetricUnits.windchill, 20.0, 0.0)
    }

    @Test
    fun hourDto_toDomainModel_returnsCorrectConditionColors() {
        assertEquals(hoursDomainObjectImperialUnits.textColor, Color.Black)
        assertEquals(hoursDomainObjectImperialUnits.colors, listOf(Color(0xffffffff),Color(0xffffbb00)))
    }

    @Test
    fun hourDto_toDomainModel_returnsCorrectTime() {
        val formattedTime = LocalTime.parse(hour.time.substring(11))
            .format(DateTimeFormatter.ofPattern(preferencesImperial.clockFormat))
        val formatted24Time = LocalTime.parse(hour.time.substring(11))
            .format(DateTimeFormatter.ofPattern(preferencesMetric.clockFormat))
        assertEquals(hoursDomainObjectImperialUnits.time, formattedTime.removePrefix("0"))
        assertEquals(hoursDomainObjectMetricUnits.time, formatted24Time)
    }
}