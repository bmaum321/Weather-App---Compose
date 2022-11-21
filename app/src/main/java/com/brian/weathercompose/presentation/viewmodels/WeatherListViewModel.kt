package com.brian.weathercompose.presentation.viewmodels

import android.app.Application
import android.content.SharedPreferences
import android.content.res.Resources
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.brian.weathercompose.data.local.WeatherDao
import com.brian.weathercompose.domain.model.WeatherDomainObject
import com.brian.weathercompose.data.local.WeatherEntity
import com.brian.weathercompose.data.remote.ApiResponse
import com.brian.weathercompose.repository.WeatherRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
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
    private val weatherDao: WeatherDao,
    application: Application
) : AndroidViewModel(application) {
    /** The mutable State that stores the status of the most recent request */
    // Turn this into a flow?
    var weatherUiState: WeatherListState by mutableStateOf(WeatherListState.Empty)


    private val refreshFlow = MutableSharedFlow<Unit>(1, 1, BufferOverflow.DROP_OLDEST).apply {
        tryEmit(Unit)
    }

    private fun getZipCodesFromDatabase(): Flow<List<String>> {
        return weatherDao.getZipcodesFlow()
    }

    suspend fun insertWeather() {
        weatherDao.insert(
            WeatherEntity
            (id= 0,
            cityName = "Liverpool",
            zipCode = "13088",
            sortOrder = 0)
        )
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

    fun refresh() {
        refreshFlow.tryEmit(Unit)
    }

    /**
     * Gets Weather info for a list of zipcodes
     */
    fun getAllWeather(
        sharedPreferences: SharedPreferences,
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
                                when (weatherRepository.getWeatherWithErrorHandling(zipcodes.first())) {
                                    is ApiResponse.Success -> emit(
                                        WeatherListState.Success(
                                            weatherRepository.getWeatherListForZipCodes(
                                                zipcodes,
                                                resources,
                                                sharedPreferences
                                            )
                                        )
                                    )
                                    is ApiResponse.Failure -> emit(WeatherListState.Error)
                                    is ApiResponse.Exception -> emit(WeatherListState.Error)
                                }
                            } else emit(WeatherListState.Empty)
                        }

                    }
            }.stateIn(viewModelScope, SharingStarted.Lazily, WeatherListState.Loading)
    }

    /*
    /**
     * Factory for [WeatherListViewModel] that takes repository as a dependency
     */
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as BaseApplication)
                val weatherRepository = WeatherRepository()
                WeatherListViewModel(
                    weatherRepository = weatherRepository,
                    application = application,
                    weatherDao = WeatherDao
                )
            }
        }
    }

     */

    class WeatherViewModelFactory(
        private val weatherDao: WeatherDao,
        val app: Application,
        private val weatherRepository: WeatherRepository) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(WeatherListViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return WeatherListViewModel(weatherRepository, weatherDao, app) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
