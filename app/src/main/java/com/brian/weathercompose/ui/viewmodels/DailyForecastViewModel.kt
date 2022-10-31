package com.brian.weathercompose.ui.viewmodels

import android.app.Application
import android.content.SharedPreferences
import android.content.res.Resources
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.*
import com.brian.weathercompose.data.WeatherDao
import com.brian.weathercompose.domain.ForecastDomainObject
import com.brian.weathercompose.domain.asDomainModel
import com.brian.weathercompose.model.WeatherEntity
import com.brian.weathercompose.network.ApiResponse
import com.brian.weathercompose.repository.WeatherRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow

sealed class ForecastViewData {
    object Loading : ForecastViewData()
    class Error(val code: Int, val message: String?) : ForecastViewData()
    class Done(val forecastDomainObject: ForecastDomainObject) : ForecastViewData()
}

/**
 * [ViewModel] to provide data to the WeatherLocationDetailFragment
 */

// Pass an application as a parameter to the viewmodel constructor which is the contect passed to the singleton database object
class DailyForecastViewModel(private val weatherDao: WeatherDao, application: Application) :
    AndroidViewModel(application) {

    var dailyForecastUiState: ForecastViewData by mutableStateOf(ForecastViewData.Loading)

    //The data source this viewmodel will fetch results from
    private val weatherRepository = WeatherRepository()

    private val refreshFlow = MutableSharedFlow<Unit>(1, 1, BufferOverflow.DROP_OLDEST)
        .apply {
            tryEmit(Unit)
        }

    fun refresh() {
        refreshFlow.tryEmit(Unit)
    }

    fun getWeatherByZipcode(zipcode: String): LiveData<WeatherEntity> {
        return weatherDao.getWeatherByZipcode(zipcode)
            .asLiveData()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getForecastForZipcode(zipcode: String,
                              sharedPreferences: SharedPreferences,
                              resources: Resources)
    : Flow<ForecastViewData> {
        return refreshFlow
            .flatMapLatest {
                flow {
                    emit(ForecastViewData.Loading)
                    when (val response = weatherRepository.getForecast(zipcode)) {
                        is ApiResponse.Success -> emit(
                            ForecastViewData.Done(
                                response.data.asDomainModel(sharedPreferences, resources)
                            )
                        )
                        is ApiResponse.Failure -> emit(
                            ForecastViewData.Error(
                                code = response.code,
                                message = response.message
                            )
                        )
                        is ApiResponse.Exception -> emit(
                            ForecastViewData.Error(
                                code = 0,
                                message = response.e.message
                            )
                        )
                    }
                }
            }
    }


// create a view model factory that takes a WeatherDao as a property and
//  creates a WeatherViewModel

    class DailyForecastViewModelFactory(private val weatherDao: WeatherDao, val app: Application) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DailyForecastViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return DailyForecastViewModel(weatherDao, app) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

/**
 * Use the Celsius temp for display if the setting is checked
 */

data class ForecastItemViewData(
    val day: com.brian.weathercompose.model.Day,
    val daysViewData: DaysViewData
)

data class DaysViewData(
    val minTemp: String,
    val maxTemp: String
)


fun ForecastItemViewData.withPreferenceConversion(sharedPreferences: SharedPreferences, resources: Resources): ForecastItemViewData {
    val isFahrenheit = true
       // GetSettings().getTemperatureFormatFromPreferences(sharedPreferences, resources)

    return ForecastItemViewData(
        day = com.brian.weathercompose.model.Day(
            date = day.date,
            day = day.day,
            hour = day.hour
        ),
        daysViewData = DaysViewData(
            maxTemp = if(isFahrenheit) {
                "${day.day.maxtemp_f.toInt()}°"
            } else "${day.day.maxtemp_c.toInt()}°",
            minTemp = if(isFahrenheit) {
                "${day.day.mintemp_f.toInt()}°"
            } else "${day.day.mintemp_c.toInt()}°"
        )
    )
}

