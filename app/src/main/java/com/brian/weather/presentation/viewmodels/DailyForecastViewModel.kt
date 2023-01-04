package com.brian.weather.presentation.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.brian.weather.data.local.WeatherDao
import com.brian.weather.data.settings.AppPreferences
import com.brian.weather.domain.model.ForecastDomainObject
import com.brian.weather.data.settings.PreferencesRepository
import com.brian.weather.domain.usecase.CreateDailyForecastStateUseCase
import com.brian.weather.repository.WeatherRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*


/**
 * The classes contained need to be data classes in order to perform unit testing assertions
 */
sealed class ForecastState {
    object Loading : ForecastState()
    data class Error(val code: Int, val message: String?) : ForecastState()
    data class Success(val forecastDomainObject: ForecastDomainObject) : ForecastState()
}

/**
 * [ViewModel] to provide data to the WeatherLocationDetailFragment
 */

// Pass an application as a parameter to the viewmodel constructor which is the contect passed to the singleton database object
class DailyForecastViewModel(
    private val preferencesRepository: PreferencesRepository,
    private val createDailyForecastStateUseCase: CreateDailyForecastStateUseCase,
) : ViewModel() {

    fun dailyForecastTicker(
        chanceOfRain: Double,
        chanceOfSnow: Double,
        avgTemp: Double,
        sunrise: String,
        avgHumidity: Double,
        sunset: String
    ) =
        flow {
            while (currentCoroutineContext().isActive) {
                if (chanceOfRain > 0.0) {
                    emit("Rain: ${chanceOfRain.toInt()} %")
                    delay(3000)
                }
                if (chanceOfSnow > 0.0) {
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

    fun getPreferences() = preferencesRepository
        .getAllPreferences
        .stateIn(
            viewModelScope, SharingStarted.Lazily,
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
    fun getForecastForZipcode(zipcode: String)
            : StateFlow<ForecastState> {
        return refreshFlow
            .flatMapLatest {
                flow {
                    // We shouldnt need to emit a loading state when using the state in method
                    emit(ForecastState.Loading)
                    emit(createDailyForecastStateUseCase(zipcode))
                }
            }.stateIn(viewModelScope, SharingStarted.Lazily, ForecastState.Loading)
    }

}




