package com.brian.weather.data.mapper

import android.content.res.Resources
import android.os.Build
import androidx.compose.ui.graphics.Color
import com.brian.weather.R
import com.brian.weather.data.remote.dto.WeatherContainer
import com.brian.weather.data.settings.AppPreferences
import com.brian.weather.data.settings.PreferencesRepository
import com.brian.weather.domain.model.WeatherDomainObject
import kotlinx.coroutines.flow.first
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun WeatherContainer.asDomainModel(
    zipcode: String,
    preferences: AppPreferences
): WeatherDomainObject {

   // val preferences = preferencesRepository.getAllPreferences.first()

    val locationDataDomainModel = location.toDomainModel()

    val localTime = Instant
        .ofEpochSecond(location.localtime_epoch)
        .atZone(ZoneId.of(location.tz_id))
        .format(
            DateTimeFormatter
                .ofPattern(preferences.clockFormat)
        )

    var textColor = Color.White

    // Dynamic Material colors not supported on < API 31
    if(Build.VERSION.SDK_INT <= 31) {
        textColor = Color.Black
       // backgroundColor = Color.Transparent
    }

    /**
     * Change background color of card based off current condition code from API if setting is checked
     * otherwise, the background is transparent
     */

    val backgroundColor: List<Color> = when (current.condition.code) {
        1000 -> {
            if (current.condition.text == "Sunny") {
                listOf(Color(0xfff5f242),Color(0xffff9100))// sunny
            } else listOf(Color(0xff000000),Color(0x472761CC)) // clear night
        }
        1003 -> if (current.is_day == 1) {
            listOf(Color(0xffffffff),Color(0xffffbb00)) // partly cloudy day
        } else {
            listOf(Color(0xff575757),Color(0x472761CC)) // partly cloud night
        } // partly cloudy night
        in 1006..1030 -> listOf(Color.Gray, Color.DarkGray) // clouds/overcast
        in 1063..1113 -> listOf(Color(0xff575757), Color(0xff1976d2)) // rain
        in 1114..1117 -> listOf(Color.White, Color.Gray) // Blizzard
        in 1150..1207 -> listOf(Color(0xff575757),Color(0xff1976d2))// rain
        in 1210..1237 -> listOf(Color.White, Color.Gray) //snow
        in 1255..1258 -> listOf(Color.White, Color.Gray) // moderate snow
        in 1240..1254 -> listOf(Color(0xff575757),Color(0xff1976d2)) // rain
        in 1260..1282 -> listOf(Color(0xff575757),Color(0xff1976d2)) // rain
        else -> listOf(Color.White, Color.Gray)
    }

    // Change text color to black for certain gradients for easier reading
    if (backgroundColor == listOf(Color(0xfff5f242),Color(0xffff9100)) ||
      //  backgroundColor == listOf(Color.Gray, Color.DarkGray) ||
        backgroundColor == listOf(Color.White, Color.Gray) ||
        backgroundColor == listOf(Color(0xffffffff),Color(0xffffbb00))
    ) {
        textColor = Color.Black
    }
    //  }


    /**
     * Country formatting
     * */
/*

    val formattedCountry = when (locationDataDomainModel.country) {
        resources.getString(R.string.USA) ->
            resources.getString(R.string.USA_Acronym)
        resources.getString(R.string.USA2) ->
            resources.getString(R.string.USA_Acronym)
        resources.getString(R.string.UK) ->
            resources.getString(R.string.UK_Acronym)
        else -> locationDataDomainModel.country
    }

 */

    return WeatherDomainObject(
        time = if(preferences.clockFormat == "hh:mm a") localTime.removePrefix("0") else localTime,
        location = locationDataDomainModel.name,
        zipcode = zipcode,
        temp = if(preferences.tempUnit == "Fahrenheit")current.temp_f.toInt().toString()
        else current.temp_c.toInt().toString(),
        imgSrcUrl = current.condition.icon,
        conditionText = current.condition.text,
        windSpeed = if(preferences.windUnit == "MPH")current.wind_mph
        else current.wind_kph,
        windDirection = current.wind_dir,
        backgroundColors = backgroundColor,
        code = current.condition.code,
        textColor = textColor,
        country = locationDataDomainModel.country,
        feelsLikeTemp = if(preferences.tempUnit == "Fahrenheit")current.feelslike_f.toInt().toString()
        else current.feelslike_c.toInt().toString(),
        humidity = current.humidity
    )
}

