package com.brian.weather.domain.usecase

import com.brian.weather.data.mapper.asDomainModel
import com.brian.weather.data.remote.NetworkResult
import com.brian.weather.data.settings.PreferencesRepository
import com.brian.weather.presentation.viewmodels.HourlyForecastState
import com.brian.weather.repository.WeatherRepository
import kotlinx.coroutines.flow.first

class CreateHourlyForecastStateUseCase(
    private val weatherRepository: WeatherRepository,
    private val preferencesRepository: PreferencesRepository
) {
    suspend operator fun invoke(location: String): HourlyForecastState {
        return when (val response = weatherRepository.getForecast(location)) {
            is NetworkResult.Success ->
                HourlyForecastState.Success(
                    response.data
                        .asDomainModel(
                            preferencesRepository.getAllPreferences.first()
                        )
                )
            is NetworkResult.Failure ->
                HourlyForecastState.Error(
                    message = response.message,
                    code = response.code
                )

            is NetworkResult.Exception ->
                HourlyForecastState.Error(
                    message = response.e.message,
                    code = response.e.hashCode()
                )

        }
    }
}