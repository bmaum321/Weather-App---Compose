package com.brian.weather.presentation.viewmodels

import android.app.Application
import android.content.res.Resources
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.brian.weather.data.local.WeatherDao
import com.brian.weather.domain.model.WeatherDomainObject
import com.brian.weather.data.local.WeatherEntity
import com.brian.weather.data.remote.NetworkResult
import com.brian.weather.data.settings.PreferencesRepository
import com.brian.weather.repository.WeatherRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * UI state for the Home screen
 */
sealed interface WeatherListState {
    data class Success(val weatherDomainObjects: List<WeatherDomainObject>) : WeatherListState
    data class Error(val message: String?) : WeatherListState
    object Loading : WeatherListState
    object Empty : WeatherListState
}

class WeatherListViewModel(
    private val weatherRepository: WeatherRepository,
    private val preferencesRepository: PreferencesRepository,
    private val weatherDao: WeatherDao,
    application: Application
) : AndroidViewModel(application) {

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


    private val refreshFlow = MutableSharedFlow<Unit>(1, 1, BufferOverflow.DROP_OLDEST).apply {
        tryEmit(Unit)
    }

    fun getZipCodesFromDatabase() = weatherDao.getZipcodesFlow()

    fun getWeatherByZipcode(location: String) = weatherDao.getWeatherByLocation(location)

    fun getAllWeatherEntities() = weatherDao.getAllWeatherEntities()


    fun deleteWeather(weatherEntity: WeatherEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            // call the DAO method to delete a weather object to the database here
            weatherDao.delete(weatherEntity)
        }
    }

    fun updatePrecipitationLocations(locations: Set<String>) {
        viewModelScope.launch {
            preferencesRepository.savePrecipitationLocations(locations)
        }
    }

    // private val _allPreferences = preferencesRepository.getAllPreferences.stateIn(viewModelScope, SharingStarted.Eagerly, null)
    // val allPreferences = _allPreferences.value
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
    @OptIn(ExperimentalCoroutinesApi::class)
    fun getAllWeather(
        resources: Resources
    ): StateFlow<WeatherListState> {
        return refreshFlow
            .flatMapLatest { getZipCodesFromDatabase()
                    .flatMapLatest { zipcodes ->
                        flow {
                            if (zipcodes.isNotEmpty()) {
                                emit(WeatherListState.Loading)
                                when (val response = weatherRepository.getWeather(zipcodes.first())) {
                                    is NetworkResult.Success -> emit(
                                        WeatherListState.Success(
                                            weatherRepository.getWeatherListForZipCodes(
                                                zipcodes,
                                                resources,
                                                preferencesRepository
                                            )
                                        )
                                    )
                                    is NetworkResult.Failure -> emit(WeatherListState.Error(
                                        message = response.message)
                                    )
                                    is NetworkResult.Exception -> emit(WeatherListState.Error(
                                        message = response.e.message)
                                    )
                                }
                            } else emit(WeatherListState.Empty)
                        }

                    }
            }.stateIn(viewModelScope, SharingStarted.Lazily, WeatherListState.Loading)
    }

    fun updateWeather(
        id: Long,
        name: String,
        zipcode: String,
        sortOrder: Int
    ) {
        val weatherEntity = WeatherEntity(
            id = id,
            cityName = name,
            zipCode = zipcode,
            sortOrder = sortOrder
        )
        viewModelScope.launch(Dispatchers.IO) {
            // call the DAO method to update a weather object to the database here
            weatherDao.update(weatherEntity)
        }
    }

}
