package com.brian.weather.data.mapper

import com.brian.weather.data.remote.dto.Hour
import com.brian.weather.domain.model.HoursDomainObject

fun Hour.toDomainModel(): HoursDomainObject {
    return HoursDomainObject(
        time_epoch = time_epoch,
        time = time,
        temp_f = temp_f,
        temp_c = temp_c,
        is_day = is_day,
        condition = condition,
        wind_mph = wind_mph,
        wind_kph = wind_kph,
        wind_dir = wind_dir,
        chance_of_rain = chance_of_rain,
        pressure_mb = pressure_mb,
        pressure_in = pressure_in,
        will_it_rain = will_it_rain,
        chance_of_snow = chance_of_snow,
        will_it_snow = will_it_snow,
        precip_mm = precip_mm,
        precip_in = precip_in,
        feelslike_c = feelslike_c,
        feelslike_f = feelslike_f,
        windchill_c = windchill_c,
        windchill_f = windchill_f
    )
}