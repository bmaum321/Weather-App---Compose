
package com.brian.weathercompose.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.brian.weathercompose.data.BaseApplication
import com.brian.weathercompose.domain.WeatherDomainObject
import com.brian.weathercompose.repository.WeatherRepository
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

class WeatherListViewModel(private val weatherRepository: WeatherRepository) : ViewModel() {
    /** The mutable State that stores the status of the most recent request */
    var marsUiState: WeatherListState by mutableStateOf(WeatherListState.Loading)
        private set


    /**
     * Gets Mars photos information from the Mars API Retrofit service and updates the
     * [MarsPhoto] [List] [MutableList].
     */
    fun getForecast() {
        viewModelScope.launch {
            marsUiState = WeatherListState.Loading
            marsUiState = try {
                WeatherListState.Success(weatherRepository.getForecast("13088"))
            } catch (e: IOException) {
                WeatherListState.Error
            } catch (e: HttpException) {
                WeatherListState.Error
            }
        }
    }

    /**
     * Factory for [WeatherListViewModel] that takes [MarsPhotosRepository] as a dependency
     */
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as BaseApplication)
                val weatherRepository = WeatherRepository()
                WeatherListViewModel(weatherRepository = weatherRepository)
            }
        }
    }
}
