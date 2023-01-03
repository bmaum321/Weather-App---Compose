package com.brian.weather.data.mapper


import android.content.res.Resources
import androidx.compose.ui.graphics.Color
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.brian.weather.data.remote.dto.Astro
import com.brian.weather.data.remote.dto.Condition
import com.brian.weather.data.remote.dto.Day
import com.brian.weather.data.remote.dto.ForecastForDay
import com.brian.weather.data.remote.dto.Hour
import com.brian.weather.data.settings.AppPreferences
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.mockito.Mockito.mock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.TextStyle
import java.util.*

@RunWith(AndroidJUnit4::class)
class DayMapperKtTest {

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
    private val forecastForDay = ForecastForDay(
        condition = Condition(code = 1000, icon = "//cdn.weatherapi.com/weather/64x64/day/116.png", text = "Sunny"),
        avgtemp_f = 1.5,
        maxtemp_f = 2.0,
        mintemp_f = 1.0,
        avgtemp_c = 0.5,
        maxtemp_c = 1.0,
        mintemp_c = 0.0,
        daily_chance_of_rain = 0.0,
        daily_chance_of_snow = 0.0,
        totalprecip_in = 1.0,
        totalprecip_mm = 2.0,
        avghumidity = 0.0,
    )
    private val day = Day(
        date = LocalDateTime.now().toLocalDate().toString(),
        day = forecastForDay,
        hour = listOf(
            Hour(
                time_epoch = System.currentTimeMillis() / 1000 + 3600,
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
            ),
            Hour(
                time_epoch = System.currentTimeMillis() / 1000 - 3600, // time in past should be filtered by mapper
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

    )
    var daysDomainObject = day.toDomainModel(
        preferences = preferences
    )

    private val dayDomainObject = forecastForDay.toDomainModel(preferences)

    @Test
    fun daysDto_toDomainModel_returnsCorrectDayOfWeek() {
        assertTrue(daysDomainObject.dayOfWeek in listOf("Today", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"))
    }

    @Test
    fun daysDto_ToDomainModel_returnsCorrectDateMMDD() {
        val month = LocalDate.parse(day.date).month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
        val dayOfMonth = LocalDate.parse(day.date).dayOfMonth.toString()
        assertTrue(daysDomainObject.date == "$month $dayOfMonth")
    }

    @Test
    fun daysDto_ToDomainModel_returnsCorrectDateDDMM() {
        daysDomainObject = day.toDomainModel(
            preferences = preferences2
        )
        val month = LocalDate.parse(day.date).month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
        val dayOfMonth = LocalDate.parse(day.date).dayOfMonth.toString()
        assertTrue(daysDomainObject.date == "$dayOfMonth $month")
    }

    /**
     * Hours with time epoch in past should be filtered by mapper
     */
    @Test
    fun daysDto_toDomainModel_filtersHoursCorrectly() {
       assertTrue(daysDomainObject.hours.all { it.time_epoch > System.currentTimeMillis() / 1000 })
    }

    /**
     * Code 1000 is sunny, yellow background colors, black text color
     */

    /**
     * I feel like there is a potential for a lot of tests here, test every color combination?
     * Would need to generate test data for every case
     */
    @Test
    fun dayDto_toDomainModel_returnsCorrectColors() {
        assertTrue(dayDomainObject.backgroundColors == listOf(Color(0xfff5f242), Color(0xffff9100)))
        assertTrue(dayDomainObject.textColor == Color.Black)
    }

}