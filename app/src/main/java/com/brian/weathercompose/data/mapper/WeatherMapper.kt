package com.brian.weathercompose.data.mapper

import android.content.SharedPreferences
import android.content.res.Resources
import android.os.Build
import androidx.compose.ui.graphics.Color
import com.brian.weathercompose.R
import com.brian.weathercompose.data.remote.dto.WeatherContainer
import com.brian.weathercompose.data.settings.SettingsRepository
import com.brian.weathercompose.domain.model.WeatherDomainObject
import kotlinx.coroutines.flow.first
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

suspend fun WeatherContainer.asDomainModel(
    zipcode: String,
    resources: Resources,
    settingsRepository: SettingsRepository,
): WeatherDomainObject {

    //val settings = settingsRepository.fetchInitialPreferences()
    val temperatureUnit = settingsRepository.getTemperatureUnit.first().toString()
    val clockFormat = settingsRepository.getClockFormat.first().toString()


    val locationDataDomainModel = location.toDomainModel()
    // Get local time for display
    locationDataDomainModel.localtime = Instant
        .ofEpochSecond(location.localtime_epoch)
        .atZone(ZoneId.of(location.tz_id))
        .format(
            DateTimeFormatter
                .ofPattern(clockFormat)
        )
        .removePrefix("0")


    /**
     * Change background color of card based off current condition code from API if setting is checked
     * otherwise, the background is transparent
     */

    //var backgroundColor: Int = R.color.material_dynamic_neutral_variant30
    //var textColor: Int = R.color.material_dynamic_neutral_variant80

    var backgroundColor = listOf<Color>()
    var textColor = Color.White

    // Dynamic Material colors not supported on < API 31
    if(Build.VERSION.SDK_INT <= 31) {
        textColor = Color.Black
       // backgroundColor = Color.Transparent
    }
//    if (sharedPreferences.getBoolean(
//            resources.getString(R.string.show_current_condition_color),
    //          true
    //      )
    //   ) {
    backgroundColor = when (current.condition.code) {
        1000 -> {
            if (current.condition.text == resources.getString(R.string.Sunny)) {
                listOf(Color(0xfff5f242),Color(0xffff9100))// sunny
            } else listOf(Color(0xff000000),Color(0x472761CC)) // clear night
        }
        1003 -> if (current.is_day == 1) {
            listOf(Color(0xffffffff),Color(0xffffbb00)) // partly cloudy day
        } else {
            listOf(Color(0xff575757),Color(0x472761CC)) // partly cloud night
        } // partly cloudy night
        in 1006..1030 -> listOf(Color.Gray, Color.DarkGray) // clouds/overcast
        in 1063..1117 -> listOf(Color(0xff575757),Color(0xff1976d2)) // rain
        in 1150..1207 -> listOf(Color(0xff575757),Color(0xff1976d2))// rain
        in 1210..1237 -> listOf(Color.White, Color.Gray) //snow
        in 1240..1282 -> listOf(Color(0xff575757),Color(0xff1976d2)) // rain
        else -> listOf(Color.White, Color.Gray)
    }

    // Change text color to black for certain gradients for easier reading
    if (backgroundColor == listOf(Color(0xfff5f242),Color(0xffff9100)) ||
        backgroundColor == listOf(Color.Gray, Color.DarkGray) ||
        backgroundColor == listOf(Color.White, Color.Gray) ||
        backgroundColor == listOf(Color(0xffffffff),Color(0xffffbb00))
    ) {
        textColor = Color.Black
    }
    //  }


    /**
     * Country formatting
     * */

    when (locationDataDomainModel.country) {
        resources.getString(R.string.USA) -> locationDataDomainModel.country =
            resources.getString(R.string.USA_Acronym)
        resources.getString(R.string.USA2) -> locationDataDomainModel.country =
            resources.getString(R.string.USA_Acronym)
        resources.getString(R.string.UK) -> locationDataDomainModel.country =
            resources.getString(R.string.UK_Acronym)
    }





    return WeatherDomainObject(
        time = locationDataDomainModel.localtime,
        location = locationDataDomainModel.name,
        zipcode = zipcode,
        temp = if(temperatureUnit == "Fahrenheit")current.temp_f.toInt().toString() else current.temp_c.toInt().toString(),
        imgSrcUrl = current.condition.icon,
        conditionText = current.condition.text,
        windSpeed = current.wind_mph,
        windDirection = current.wind_dir,
        backgroundColors = backgroundColor,
        code = current.condition.code,
        textColor = textColor,
        country = locationDataDomainModel.country,
        feelsLikeTemp = current.feelslike_f.toInt().toString()
    )
}

