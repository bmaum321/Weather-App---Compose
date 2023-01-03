package com.brian.weather.presentation.viewmodels

import android.app.Application
import androidx.datastore.preferences.core.emptyPreferences
import androidx.lifecycle.*
import com.brian.weather.data.settings.AppPreferences
import com.brian.weather.domain.model.ForecastDomainObject
import com.brian.weather.data.settings.PreferencesRepository
import com.brian.weather.domain.usecase.CreateHourlyForecastStateUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch


sealed class HourlyForecastState {
    data class Success(val forecastDomainObject: ForecastDomainObject) : HourlyForecastState()
    data class Error(val code: Int, val message: String?) : HourlyForecastState()
    object Loading : HourlyForecastState()
}

/**
 * [ViewModel] to provide data to the [WeatherLocationDetailFragment]
 */

// Pass an application as a parameter to the viewmodel constructor which is the context passed to the singleton database object
class HourlyForecastViewModel(
    private val createHourlyForecastStateUseCase: CreateHourlyForecastStateUseCase,
    private val preferencesRepository: PreferencesRepository,
) : ViewModel() {


    private val refreshFlow = MutableSharedFlow<Unit>(1, 1, BufferOverflow.DROP_OLDEST).apply {
        tryEmit(Unit)
    }

    fun refresh() {
        refreshFlow.tryEmit(Unit)
    }

    fun getPreferences() = preferencesRepository.getAllPreferences.stateIn(viewModelScope, SharingStarted.Lazily,
        AppPreferences(
        tempUnit = "",
        clockFormat = "",
        dateFormat = "",
        windUnit = "",
        dynamicColors = false,
        showAlerts = false,
        measurementUnit = "",
        showNotifications = false,
        showLocalForecast = false,
        showPrecipitationNotifications = false,
        precipitationLocations = setOf()
    )
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getHourlyForecast(
        location: String,
    ): StateFlow<HourlyForecastState> {
        return refreshFlow
            .flatMapLatest {
                flow {
                    //emit(HourlyForecastState.Loading)
                    emit(createHourlyForecastStateUseCase(location))
                }
            }.stateIn(viewModelScope, SharingStarted.Lazily, HourlyForecastState.Loading)
    }

    /*
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

     */
}






