package com.brian.weathercompose.presentation.viewmodels

import android.app.Application
import android.content.SharedPreferences
import android.content.res.Resources
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.lifecycle.*
import com.brian.weathercompose.data.local.WeatherDao
import com.brian.weathercompose.domain.model.ForecastDomainObject
import com.brian.weathercompose.data.mapper.asDomainModel
import com.brian.weathercompose.data.local.WeatherEntity
import com.brian.weathercompose.data.remote.dto.Day
import com.brian.weathercompose.data.remote.NetworkResult
import com.brian.weathercompose.data.settings.SettingsRepository
import com.brian.weathercompose.repository.WeatherRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

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
    private val settingsRepository: SettingsRepository,
    private val weatherDao: WeatherDao,
    application: Application) :
    AndroidViewModel(application) {

    //The data source this viewmodel will fetch results from

    private val refreshFlow = MutableSharedFlow<Unit>(1, 1, BufferOverflow.DROP_OLDEST)
        .apply {
            tryEmit(Unit)
        }

    fun refresh() {
        refreshFlow.tryEmit(Unit)
    }

    fun getWeatherByZipcode(zipcode: String): WeatherEntity {
        return weatherDao.getWeatherByZipcode(zipcode)
    }

     fun getTemperatureUnit(): String {
         var unit = ""
         viewModelScope.launch {
            unit = settingsRepository.getTemperatureUnit.first().toString()
         }
         return unit
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
                                response.data.asDomainModel(settingsRepository, resources)
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
         private val settingsRepository: SettingsRepository,
         private val weatherDao: WeatherDao,
         val app: Application) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DailyForecastViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return DailyForecastViewModel(weatherRepository, settingsRepository, weatherDao, app) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}




