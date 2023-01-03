package com.brian.weather.domain.usecase

import android.content.res.Resources
import com.brian.weather.data.remote.NetworkResult
import com.brian.weather.data.settings.PreferencesRepository
import com.brian.weather.presentation.viewmodels.WeatherListState
import com.brian.weather.repository.WeatherRepository
import kotlinx.coroutines.flow.first


class CreateWeatherListStateUsecase(
    val weatherRepository: WeatherRepository,
    val preferencesRepository: PreferencesRepository
) {
    suspend operator fun invoke(zipcodes: List<String>): WeatherListState {
        return when (val response = weatherRepository.getWeather(zipcodes.first())) {
            is NetworkResult.Success ->
                WeatherListState.Success(
                    weatherRepository.getWeatherListForZipCodes(
                        zipcodes,
                        preferencesRepository.getAllPreferences.first()
                    )
                )

            is NetworkResult.Failure ->
                WeatherListState.Error(
                    message = response.message
                )

            is NetworkResult.Exception ->
                WeatherListState.Error(
                    message = response.e.message
                )

        }
    }
}
