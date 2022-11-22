package com.brian.weathercompose.domain.model

import com.brian.weathercompose.data.remote.dto.Condition

data class HoursDomainObject(
    val time_epoch: Int,
    var time: String, //TODO make this a val, modify it in the mapper(can use copy function on data class too)
    val temp_f: Double,
    val temp_c: Double,
    val is_day: Int,
    val condition: Condition,
    val wind_mph: Double,
    val wind_kph: Double,
    val wind_dir: String,
    val chance_of_rain: Int,
    val pressure_mb: Double,
    val pressure_in: Double,
    val will_it_rain: Int,
    val chance_of_snow: Double,
    val will_it_snow: Int,
    val precip_mm: Double,
    val precip_in: Double,
    val feelslike_c: Double,
    val feelslike_f: Double,
    val windchill_c: Double,
    val windchill_f: Double
)
