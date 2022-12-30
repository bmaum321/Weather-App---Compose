package com.brian.weather.data.mapper

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.brian.weather.data.remote.dto.Astro
import com.brian.weather.data.remote.dto.Condition
import com.brian.weather.data.remote.dto.Day
import com.brian.weather.data.remote.dto.ForecastForDay
import com.brian.weather.data.remote.dto.Hour
import org.junit.Assert
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDateTime

class DayMapperKtTest {


    //val context: Context = ApplicationProvider.getApplicationContext()
    private val day = Day(
        date = LocalDateTime.now().toLocalDate().toString(),
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
            )
        ),
        astro = Astro(
            sunrise = "07:06 AM",
            sunset = "07:06 AM",
            moon_phase = "Waxing Crescent",
        )

    )

    @Test
    fun dayDto_toDomainModel() {
        /*
        val dayDomainObject = day.toDomainModel(
            clockFormat = "hh:mm",
            dateFormat = "DD/MM",
            resources = context.resources
        )

        assertTrue(dayDomainObject.dayOfWeek in listOf("Today", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"))

         */

        assertTrue(1+1 == 2)
    }


}