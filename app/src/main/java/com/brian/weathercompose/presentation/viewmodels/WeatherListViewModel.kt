package com.brian.weathercompose.presentation.viewmodels

import android.app.Application
import android.content.res.Resources
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.brian.weathercompose.data.local.WeatherDao
import com.brian.weathercompose.domain.model.WeatherDomainObject
import com.brian.weathercompose.data.local.WeatherEntity
import com.brian.weathercompose.data.remote.NetworkResult
import com.brian.weathercompose.data.settings.AppPreferences
import com.brian.weathercompose.data.settings.PreferencesRepository
import com.brian.weathercompose.repository.WeatherRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * UI state for the Home screen
 */
sealed interface WeatherListState {
    data class Success(val weatherDomainObjects: List<WeatherDomainObject>) : WeatherListState
    object Error : WeatherListState
    object Loading : WeatherListState
    object Empty: WeatherListState
}

class WeatherListViewModel(
    private val weatherRepository: WeatherRepository,
    private val preferencesRepository: PreferencesRepository,
    private val weatherDao: WeatherDao,
    application: Application
) : AndroidViewModel(application) {
    /** The mutable State that stores the status of the most recent request */
    // Turn this into a flow?
    var weatherUiState: WeatherListState by mutableStateOf(WeatherListState.Empty)


    fun weatherTicker(
        windspeed: String,
        time: String,
        feelsLikeTemp: String,
        humidity: Int
    ) =
        flow {
            while (true)  {
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

    private val refreshFlow = MutableSharedFlow<Unit>(1, 1, BufferOverflow.DROP_OLDEST).apply {
        tryEmit(Unit)
    }

    fun getZipCodesFromDatabase(): Flow<List<String>> {
        return weatherDao.getZipcodesFlow()
    }

    fun getWeatherByZipcode(location: String): WeatherEntity {
        return weatherDao.getWeatherByLocation(location)
    }

    fun deleteWeather(weatherEntity: WeatherEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            // call the DAO method to delete a weather object to the database here
            weatherDao.delete(weatherEntity)
        }
    }

    fun updateLocations(locations: Set<String>) {
        viewModelScope.launch {
            preferencesRepository.savePrecipitationLocations(locations)
        }
    }

    //private val _allPreferences = preferencesRepository.getAllPreferences.stateIn(viewModelScope, SharingStarted.Eagerly, null)
    //val allPreferences = _allPreferences.value
    val allPreferences = preferencesRepository.getAllPreferences.stateIn(viewModelScope, SharingStarted.Eagerly, null)


    fun refresh() {
        refreshFlow.tryEmit(Unit)
    }

    /**
     * Gets Weather info for a list of zipcodes
     */
    fun getAllWeather(
        resources: Resources
    ): StateFlow<WeatherListState> {
        return refreshFlow
            .flatMapLatest {
                getZipCodesFromDatabase()
                    .flatMapLatest { zipcodes ->
                        flow {
                            if (zipcodes.isNotEmpty()) {
                                //weatherUiState = WeatherListState.Loading
                                emit(WeatherListState.Loading)
                                when (weatherRepository.getWeather(zipcodes.first())) {
                                    is NetworkResult.Success -> emit(
                                        WeatherListState.Success(
                                            weatherRepository.getWeatherListForZipCodes(
                                                zipcodes,
                                                resources,
                                                preferencesRepository
                                            )
                                        )
                                    )
                                    is NetworkResult.Failure -> emit(WeatherListState.Error)
                                    is NetworkResult.Exception -> emit(WeatherListState.Error)
                                }
                            } else emit(WeatherListState.Empty)
                        }

                    }
            }.stateIn(viewModelScope, SharingStarted.Lazily, WeatherListState.Loading)
    }

}
