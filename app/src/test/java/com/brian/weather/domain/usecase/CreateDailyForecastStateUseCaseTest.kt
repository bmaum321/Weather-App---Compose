package com.brian.weather.domain.usecase

import com.brian.weather.presentation.viewmodels.ForecastState
import com.brian.weather.repository.EmptyPreferencesRepositoryImpl
import com.brian.weather.repository.FakeWeatherRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class CreateDailyForecastStateUseCaseTest{
    private val zipcode = "90210"
    private val fakeWeatherRepository = FakeWeatherRepository()
    private val emptyPreferencesRepository = EmptyPreferencesRepositoryImpl()
    private val usecase = CreateDailyForecastStateUseCase(fakeWeatherRepository, emptyPreferencesRepository)

    @Test
    fun createForecastStateUsecase_repositoryReturnsSuccess_returnsSuccessState() = runBlocking{
        when(val state = usecase.invoke(zipcode)){
            is ForecastState.Success -> {
                assertEquals(ForecastState.Success(state.forecastDomainObject), state)
            }
            else -> {}
        }

    }

    @Test
    fun createForecastStateUsecase_repositoryReturnsError_returnsErrorState() = runBlocking{
        fakeWeatherRepository.setShouldReturnNetworkError(true)
        when(val state = usecase.invoke(zipcode)){
            is ForecastState.Error -> {
                assertEquals(ForecastState.Error(state.code, state.message), state)
            }
            else -> {}
        }
    }

    @Test
    fun createForecastStateUsecase_returnsErrorState() = runBlocking{
        fakeWeatherRepository.setShouldReturnException(true)
        when(val state = usecase.invoke(zipcode)){
            is ForecastState.Error -> {
                assertEquals(ForecastState.Error(state.code, state.message), state)
            }
            else -> {}
        }
    }
}