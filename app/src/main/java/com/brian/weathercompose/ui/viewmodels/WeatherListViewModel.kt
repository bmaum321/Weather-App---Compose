package com.brian.weathercompose.ui.viewmodels

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.preference.PreferenceManager
import com.brian.weathercompose.data.BaseApplication
import com.brian.weathercompose.data.WeatherDao
import com.brian.weathercompose.domain.WeatherDomainObject
import com.brian.weathercompose.model.WeatherEntity
import com.brian.weathercompose.network.ApiResponse
import com.brian.weathercompose.repository.WeatherRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

/**
 * UI state for the Home screen
 */
sealed interface WeatherListState {
    data class Success(val weatherDomainObjects: List<WeatherDomainObject>) : WeatherListState
    object Error : WeatherListState
    object Loading : WeatherListState
}

class WeatherListViewModel(
    private val weatherRepository: WeatherRepository,
    private val weatherDao: WeatherDao,
    application: Application
) : AndroidViewModel(application) {
    /** The mutable State that stores the status of the most recent request */
    var weatherUiState: WeatherListState by mutableStateOf(WeatherListState.Loading)
        private set

    init {
        //TODO this is just initializing the data on home screen
      //  getForecast(
        //    sharedPreferences = PreferenceManager.getDefaultSharedPreferences(application),
      //      resources = Resources.getSystem()
      //  )
        viewModelScope.launch { insertWeather() }

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

    fun deleteWeather(weatherEntity: WeatherEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            // call the DAO method to delete a weather object to the database here
            weatherDao.delete(weatherEntity)
        }
    }

    /**
     * Gets Mars photos information from the Mars API Retrofit service and updates the
     * [MarsPhoto] [List] [MutableList].
     */
    fun getForecast(
        sharedPreferences: SharedPreferences,
        resources: Resources
    ) {

        viewModelScope.launch(Dispatchers.IO) {
            val zipcodes = weatherDao.getZipcodesStatic()
            weatherUiState = WeatherListState.Loading
            when (weatherRepository.getSearchResults("13088")) {
                is ApiResponse.Success -> weatherUiState = WeatherListState.Success(
                    weatherRepository.getWeatherListForZipCodes(
                        zipcodes,
                        resources,
                        sharedPreferences
                    )
                )
                is ApiResponse.Exception -> weatherUiState = WeatherListState.Error
                is ApiResponse.Failure -> weatherUiState = WeatherListState.Error

            }
        }
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
