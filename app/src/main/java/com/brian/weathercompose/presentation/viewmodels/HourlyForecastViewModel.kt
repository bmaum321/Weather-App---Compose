package com.brian.weathercompose.presentation.viewmodels

import android.app.Application
import android.content.SharedPreferences
import android.content.res.Resources
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.*
import com.brian.weathercompose.data.local.WeatherDao
import com.brian.weathercompose.domain.model.ForecastDomainObject
import com.brian.weathercompose.data.mapper.asDomainModel
import com.brian.weathercompose.data.remote.dto.Hour
import com.brian.weathercompose.data.remote.NetworkResult
import com.brian.weathercompose.repository.WeatherRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*


sealed class HourlyForecastViewData {
    class Done(val forecastDomainObject: ForecastDomainObject) : HourlyForecastViewData()
    class Error(val code: Int, val message: String?) : HourlyForecastViewData()
    object Loading : HourlyForecastViewData()
}

/**
 * [ViewModel] to provide data to the [WeatherLocationDetailFragment]
 */

// Pass an application as a parameter to the viewmodel constructor which is the context passed to the singleton database object
class HourlyForecastViewModel(
    private val weatherRepository: WeatherRepository,
    private val weatherDao: WeatherDao,
    application: Application
) :
    AndroidViewModel(application) {

    var hourlyForecastUiState: HourlyForecastViewData by mutableStateOf(HourlyForecastViewData.Loading)

    private val refreshFlow = MutableSharedFlow<Unit>(1, 1, BufferOverflow.DROP_OLDEST).apply {
        tryEmit(Unit)
    }

    fun refresh() {
        refreshFlow.tryEmit(Unit)
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    fun getHourlyForecast(
        zipcode: String,
        sharedPreferences: SharedPreferences,
        resources: Resources
    ): StateFlow<HourlyForecastViewData> {
        return refreshFlow
            .flatMapLatest {
                flow {
                    emit(HourlyForecastViewData.Loading)
                    when (val response = weatherRepository.getForecast(zipcode)) {
                        is NetworkResult.Success -> emit(
                            HourlyForecastViewData.Done(
                                response.data.asDomainModel(sharedPreferences, resources)
                            )
                        )
                        is NetworkResult.Failure -> emit(
                            HourlyForecastViewData.Error(
                                message = response.message,
                                code = response.code
                            )
                        )
                        is NetworkResult.Exception -> emit(
                            HourlyForecastViewData.Error(
                                message = response.e.message,
                                code = response.e.hashCode()
                            )
                        )
                    }
                }
            }.stateIn(viewModelScope, SharingStarted.Lazily, HourlyForecastViewData.Loading)
    }

// create a view model factory that takes a WeatherDao as a property and
//  creates a WeatherViewModel

    class HourlyForecastViewModelFactory(
        private val weatherRepository: WeatherRepository,
        private val weatherDao: WeatherDao,
        val app: Application
    ) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HourlyForecastViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return HourlyForecastViewModel(weatherRepository, weatherDao, app) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}


data class HourlyForecastItemViewData(
    val hour: Hour,
    val hoursViewData: HoursViewData
)

/**
 * Get units from settings and format data for display
 */

fun HourlyForecastItemViewData.withPreferenceConversion(
    sharedPreferences: SharedPreferences,
    resources: Resources
): HourlyForecastItemViewData {
    /*
    val isFahrenheit =
        GetSettings().getTemperatureFormatFromPreferences(sharedPreferences, resources)
    val isMph = GetSettings().getWindSpeedFormatFromPreferences(sharedPreferences, resources)
    val isIn = GetSettings().getMeasurementFormatFromPreferences(sharedPreferences, resources)

     */

    val isFahrenheit = true
    val isMph = true
    val isIn = true

    return HourlyForecastItemViewData(
        hour = Hour(
            time_epoch = hour.time_epoch,
            time = hour.time,
            temp_f = hour.temp_f,
            temp_c = hour.temp_c,
            is_day = hour.is_day,
            condition = hour.condition,
            wind_mph = hour.wind_mph,
            wind_kph = hour.wind_kph,
            wind_dir = hour.wind_dir,
            chance_of_rain = hour.chance_of_rain,
            chance_of_snow = hour.chance_of_snow,
            feelslike_c = hour.feelslike_c,
            feelslike_f = hour.feelslike_f,
            precip_in = hour.precip_in,
            precip_mm = hour.precip_mm,
            pressure_in = hour.pressure_in,
            pressure_mb = hour.pressure_mb,
            will_it_rain = hour.will_it_rain,
            will_it_snow = hour.will_it_snow,
            windchill_c = hour.windchill_c,
            windchill_f = hour.windchill_f

        ),

        // Format data for display
        hoursViewData = HoursViewData(
            temperature = if (isFahrenheit) {
                "${hour.temp_f.toInt()}°"
            } else "${hour.temp_c.toInt()}°",
            condition = hour.condition.text,
            icon = hour.condition.icon,
            windSpeed = if (isMph) {
                "${hour.wind_mph} MPH"
            } else "${hour.wind_kph} KPH",
            feelsLike = if (isFahrenheit) {
                "Feels Like: ${hour.feelslike_f}°"
            } else "Feels Like: ${hour.feelslike_c}°",
            pressure = if (isIn) {
                "${hour.precip_in} IN"
            } else "${hour.precip_mm} MM",
            precipAmount = if (isIn) {
                "${hour.precip_in} IN"
            } else "${hour.precip_mm} MM",
            time = hour.time,
            windDirection = hour.wind_dir
        )
    )
}

data class HoursViewData(
    val temperature: String,
    val condition: String,
    val icon: String,
    val windSpeed: String,
    val feelsLike: String,
    val pressure: String,
    val precipAmount: String,
    val time: String,
    val windDirection: String
)





