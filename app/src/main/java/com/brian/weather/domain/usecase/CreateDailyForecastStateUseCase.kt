package com.brian.weather.domain.usecase

import com.brian.weather.data.mapper.asDomainModel
import com.brian.weather.data.remote.NetworkResult
import com.brian.weather.data.settings.PreferencesRepository
import com.brian.weather.presentation.viewmodels.ForecastState
import com.brian.weather.repository.WeatherRepository
import kotlinx.coroutines.flow.first

class CreateDailyForecastStateUseCase(
    val weatherRepository: WeatherRepository,
    val preferencesRepository: PreferencesRepository
) {
    suspend operator fun invoke(zipcode: String): ForecastState {
        return when (val response = weatherRepository.getForecast(zipcode)) {
            is NetworkResult.Success ->
                ForecastState.Success(
                    response.data
                        .asDomainModel(
                            preferencesRepository.getAllPreferences.first()
                        )
                )

            is NetworkResult.Failure ->
                ForecastState.Error(
                    code = response.code,
                    message = response.message
                )

            is NetworkResult.Exception ->
                ForecastState.Error(
                    code = 0,
                    message = response.e.message
                )

        }
    }
}