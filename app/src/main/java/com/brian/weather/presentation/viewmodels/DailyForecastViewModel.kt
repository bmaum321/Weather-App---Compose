package com.brian.weather.presentation.viewmodels

import android.app.Application
import android.content.res.Resources
import androidx.lifecycle.*
import com.brian.weather.data.local.WeatherDao
import com.brian.weather.domain.model.ForecastDomainObject
import com.brian.weather.data.mapper.asDomainModel
import com.brian.weather.data.local.WeatherEntity
import com.brian.weather.data.remote.NetworkResult
import com.brian.weather.data.settings.PreferencesRepository
import com.brian.weather.repository.WeatherRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*

sealed class ForecastViewData {
    object Loading : ForecastViewData()
    class Error(val code: Int, val message: String?) : ForecastViewData()
    class Done(val forecastDomainObject: ForecastDomainObject) : ForecastViewData()
}

/**
 * [ViewModel] to provide data to the WeatherLocationDetailFragment
 */

// Pass an application as a parameter to the viewmodel constructor which is the contect passed to the singleton database object
class DailyForecastViewModel(
    private val weatherRepository: WeatherRepository,
    private val preferencesRepository: PreferencesRepository,
    private val weatherDao: WeatherDao,
    application: Application) :
    AndroidViewModel(application) {

    //The data source this viewmodel will fetch results from


    fun dailyForecastTicker(
        chanceOfRain: Double,
        chanceOfSnow: Double,
        avgTemp: Double,
        sunrise: String,
        avgHumidity: Double,
        sunset: String) =
        flow {
            while (currentCoroutineContext().isActive)  {
                if(chanceOfRain > 0.0) {
                    emit("Rain: ${chanceOfRain.toInt()} %")
                    delay(3000)
                }
                if(chanceOfSnow > 0.0) {
                    emit("Snow: ${chanceOfSnow.toInt()} %")
                    delay(3000)
                }
                emit("Avg Temp: ${avgTemp.toInt()}° ")
                delay(3000)
                emit("Avg Humidity: ${avgHumidity.toInt()} %")
                delay(3000)
                emit("Sunrise: $sunrise ")
                delay(3000)
                emit("Sunset: $sunset ")
                delay(3000)

            }
        }

    private val refreshFlow = MutableSharedFlow<Unit>(1, 1, BufferOverflow.DROP_OLDEST)
        .apply {
            tryEmit(Unit)
        }

    fun refresh() {
        refreshFlow.tryEmit(Unit)
    }

    fun getWeatherByZipcode(zipcode: String) = weatherDao.getWeatherByZipcode(zipcode)


     fun getTemperatureUnit(): String {
         var unit = ""
         viewModelScope.launch {
            unit = preferencesRepository.getTemperatureUnit.first().toString()
         }
         return unit
     }

    fun getAlertsSetting(): Boolean {
        var setting = true
        viewModelScope.launch {
            setting = preferencesRepository.getWeatherAlertsSetting.first() ?: true
        }
        return setting
    }

    fun getDynamicColorSetting(): Boolean {
        var setting = true
        viewModelScope.launch {
            setting = preferencesRepository.getDynamicColorsSetting.first() ?: true
        }
        return setting
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getForecastForZipcode(zipcode: String,
                              resources: Resources)
    : StateFlow<ForecastViewData> {
        return refreshFlow
            .flatMapLatest {
                flow {
                    emit(ForecastViewData.Loading)
                    when (val response = weatherRepository.getForecast(zipcode)) {
                        is NetworkResult.Success -> emit(
                            ForecastViewData.Done(
                                response.data
                                    .asDomainModel(
                                        preferencesRepository.getClockFormat.first().toString(),
                                        preferencesRepository.getDateFormat.first().toString(),
                                        resources
                                    )
                            )
                        )
                        is NetworkResult.Failure -> emit(
                            ForecastViewData.Error(
                                code = response.code,
                                message = response.message
                            )
                        )
                        is NetworkResult.Exception -> emit(
                            ForecastViewData.Error(
                                code = 0,
                                message = response.e.message
                            )
                        )
                    }
                }
            }.stateIn(viewModelScope, SharingStarted.Lazily, ForecastViewData.Loading)
    }


// create a view model factory that takes a WeatherDao as a property and
//  creates a WeatherViewModel

    class DailyForecastViewModelFactory
        (private val weatherRepository: WeatherRepository,
         private val preferencesRepository: PreferencesRepository,
         private val weatherDao: WeatherDao,
         val app: Application) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DailyForecastViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return DailyForecastViewModel(weatherRepository, preferencesRepository, weatherDao, app) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}




