package com.brian.weather.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brian.weather.data.settings.AppPreferences
import com.brian.weather.data.settings.PreferencesRepository
import com.brian.weather.domain.model.WeatherDomainObject
import com.brian.weather.domain.usecase.CreateWeatherListStateUsecase
import com.brian.weather.repository.WeatherRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.withContext

/**
 * UI state for the Home screen
 */
sealed class WeatherListState {
    data class Success(val weatherDomainObjects: List<WeatherDomainObject>) : WeatherListState()
    data class Error(val message: String?) : WeatherListState()
    object Loading : WeatherListState()
    object Empty : WeatherListState()
}

sealed class WeatherListUIEvent {
    object Refresh: WeatherListUIEvent()
    data class Delete(val location: String): WeatherListUIEvent()
    data class Undo(val location: String): WeatherListUIEvent()
}

class WeatherListViewModel(
    private val weatherRepository: WeatherRepository,
    private val preferencesRepository: PreferencesRepository,
    private val createWeatherListStateUsecase: CreateWeatherListStateUsecase,
) : ViewModel() {

    fun weatherTicker(
        windspeed: String,
        time: String,
        feelsLikeTemp: String,
        humidity: Int
    ) =
        flow {
            while (currentCoroutineContext().isActive) {
                emit(time)
                delay(3000)
                emit("Wind: $windspeed")
                delay(3000)
                emit("Feels Like: ${feelsLikeTemp}°")
                delay(3000)
                emit("Humidity: $humidity %")
                delay(3000)
            }
        }

    fun onEvent(event: WeatherListUIEvent) {
        when (event){
            is WeatherListUIEvent.Refresh -> {
                refreshFlow.tryEmit(Unit)
            }
            is WeatherListUIEvent.Delete -> {
                viewModelScope.launch {
                    val entity = weatherRepository.getWeatherByZipcode(event.location).firstOrNull()
                    entity?.let {
                        weatherRepository.deleteWeather(entity)
                    }
                }

              //
            }
            is WeatherListUIEvent.Undo -> {
                viewModelScope.launch {
                    TODO()
                }

            }
        }
    }


    private val refreshFlow = MutableSharedFlow<Unit>(1, 1, BufferOverflow.DROP_OLDEST).apply {
        tryEmit(Unit)
    }

    /**
     * Modified this to return a List instead of a flow to support drag and drop to reorder. Previously,
     * on drag and drop, the database would be updated, and the zipcodes flow would emit a new value of
     * the reordered locations, which would trigger the state to emit a new flow which always emits
     * loading to begin. So on one drag and drop, the screen would reload
     *
     * To support this, had to build a new scope to launch the state flow in
     */


    fun updatePrecipitationLocations(locations: Set<String>) {
        viewModelScope.launch {
            preferencesRepository.savePrecipitationLocations(locations)
        }
    }

    val allPreferences = preferencesRepository.getAllPreferences.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        null
    )

    fun refresh() {
        refreshFlow.tryEmit(Unit)
    }

    /**
     * Gets Weather info for a list of zipcodes
     */

    //TODO move resources out of this function to better facilitate unit tests
    @OptIn(ExperimentalCoroutinesApi::class)
    fun getAllWeather(): StateFlow<WeatherListState> {
        val scope = viewModelScope + Dispatchers.IO
        return refreshFlow
            .flatMapLatest {
                  val zipcodes = weatherRepository.getZipCodesFromDatabase()
                        flow {
                            if (zipcodes.isNotEmpty()) {
                                /**
                                Need to emit a loading state here to properly refresh screen when refresh flow emits a new value from the refresh method
                                Otherwise, the screen will not recompose because the state never receives a new value, because we changed the above zipcodes
                                method to return a list instead of a flow
                                 */
                                emit(WeatherListState.Loading)
                                emit(createWeatherListStateUsecase(zipcodes))
                            } else emit(WeatherListState.Empty)
                        }

            }.stateIn(scope, SharingStarted.Lazily, WeatherListState.Loading)
    }


}
