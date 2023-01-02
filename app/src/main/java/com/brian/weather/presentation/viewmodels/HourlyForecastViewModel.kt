package com.brian.weather.presentation.viewmodels

import android.app.Application
import android.content.res.Resources
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.*
import com.brian.weather.data.local.WeatherDao
import com.brian.weather.domain.model.ForecastDomainObject
import com.brian.weather.data.mapper.asDomainModel
import com.brian.weather.data.remote.NetworkResult
import com.brian.weather.data.settings.PreferencesRepository
import com.brian.weather.repository.WeatherRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch


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
    private val preferencesRepository: PreferencesRepository,
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

    fun getTempUnit(): String {
        var unit = ""
        viewModelScope.launch {
            unit = preferencesRepository.getTemperatureUnit.first().toString()
        }
        return unit
    }

    fun getWindUnit(): String {
        var unit = ""
        viewModelScope.launch {
            unit = preferencesRepository.getWindspeedUnit.first().toString()
        }
        return unit
    }

    fun getMeasurement(): String {
        var unit = ""
        viewModelScope.launch {
            unit = preferencesRepository.getMeasurementUnit.first().toString()
        }
        return unit
    }

    fun getDynamicColorSetting(): Boolean {
        var setting = true
        viewModelScope.launch {
            setting = preferencesRepository.getDynamicColorsSetting.first() ?: true
        }
        return setting
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getHourlyForecast(
        zipcode: String,
        resources: Resources
    ): StateFlow<HourlyForecastViewData> {
        return refreshFlow
            .flatMapLatest {
                flow {
                    emit(HourlyForecastViewData.Loading)
                    when (val response = weatherRepository.getForecast(zipcode)) {
                        is NetworkResult.Success -> emit(
                            HourlyForecastViewData.Done(
                                response.data
                                    .asDomainModel(
                                        resources,
                                        preferencesRepository.getAllPreferences.first()
                                    )
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
        private val preferencesRepository: PreferencesRepository,
        private val weatherDao: WeatherDao,
        val app: Application
    ) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HourlyForecastViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return HourlyForecastViewModel(
                    weatherRepository,
                    preferencesRepository,
                    weatherDao,
                    app
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}






