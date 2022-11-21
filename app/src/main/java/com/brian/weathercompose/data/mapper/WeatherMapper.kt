package com.brian.weathercompose.data.mapper

import android.content.SharedPreferences
import android.content.res.Resources
import android.os.Build
import androidx.compose.ui.graphics.Color
import com.brian.weathercompose.R
import com.brian.weathercompose.data.remote.dto.WeatherContainer
import com.brian.weathercompose.domain.model.WeatherDomainObject
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun WeatherContainer.asDomainModel(
    zipcode: String,
    resources: Resources,
    sharedPreferences: SharedPreferences
): WeatherDomainObject {

    val locationDataDomainModel = location.toDomainModel()
    // Get local time for display
    locationDataDomainModel.localtime = Instant
        .ofEpochSecond(location.localtime_epoch)
        .atZone(ZoneId.of(location.tz_id))
        .format(
            DateTimeFormatter
                .ofPattern("hh:mm a")
        )
        .removePrefix("0")


    /**
     * Change background color of card based off current condition code from API if setting is checked
     * otherwise, the background is transparent
     */

    //var backgroundColor: Int = R.color.material_dynamic_neutral_variant30
    //var textColor: Int = R.color.material_dynamic_neutral_variant80

    var backgroundColor = Color.White
    var textColor = Color.Black

    // Dynamic Material colors not supported on < API 31
    if(Build.VERSION.SDK_INT <= 31) {
        textColor = Color.Black
        backgroundColor = Color.Transparent
    }
//    if (sharedPreferences.getBoolean(
//            resources.getString(R.string.show_current_condition_color),
    //          true
    //      )
    //   ) {
    backgroundColor = when (current.condition.code) {
        1000 -> {
            if (current.condition.text == resources.getString(R.string.Sunny)) {
                Color.Yellow// sunny
            } else Color.Transparent // clear night
        }
        1003 -> if (current.is_day == 1) {
            Color.LightGray // partly cloudy day
        } else {
            Color.LightGray // partly cloud night
        } // partly cloudy night
        in 1006..1030 -> Color.Gray // clouds/overcast
        in 1063..1117 -> Color.Blue // rain
        in 1150..1207 -> Color.Blue // rain
        in 1210..1237 -> Color.White //snow
        in 1240..1282 -> Color.Blue // rain
        else -> Color.White
    }

    // Change text color to black for certain gradients for easier reading
    if (backgroundColor == Color.White ||
        backgroundColor == Color.Yellow ||
        backgroundColor == Color.White
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
        temp = current.temp_f.toInt().toString(),
        imgSrcUrl = current.condition.icon,
        conditionText = current.condition.text,
        windSpeed = current.wind_mph,
        windDirection = current.wind_dir,
        backgroundColor = backgroundColor,
        code = current.condition.code,
        textColor = textColor,
        country = locationDataDomainModel.country,
        feelsLikeTemp = current.feelslike_f.toInt().toString()
    )
}

