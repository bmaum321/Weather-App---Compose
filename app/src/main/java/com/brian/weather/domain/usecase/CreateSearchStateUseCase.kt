package com.brian.weather.domain.usecase

import com.brian.weather.data.mapper.toDomainModel
import com.brian.weather.data.remote.NetworkResult
import com.brian.weather.data.settings.PreferencesRepository
import com.brian.weather.presentation.viewmodels.SearchState
import com.brian.weather.repository.WeatherRepository

class CreateSearchStateUseCase(
    val weatherRepository: WeatherRepository,
    val preferencesRepository: PreferencesRepository
) {
    suspend operator fun invoke(currentQuery: String): SearchState {
        return when (val response = weatherRepository.getSearchResults(currentQuery)) {
            is NetworkResult.Success -> {
                val newSearchResults =
                    response.data.map { it.toDomainModel() }
                        .map { searchDomainObject ->
                            searchDomainObject.name + "," + " " + searchDomainObject.region
                        }
                SearchState.Success(newSearchResults)
            }
            is NetworkResult.Failure -> {

                SearchState.Error(
                    code = response.code,
                    message = response.message
                )

            }
            is NetworkResult.Exception -> {

                SearchState.Error(
                    code = response.e.hashCode(),
                    message = response.e.message
                )

            }
        }
    }
}