package com.brian.weather.domain.usecase

import com.brian.weather.presentation.viewmodels.HourlyForecastState
import com.brian.weather.presentation.viewmodels.WeatherListState
import com.brian.weather.repository.EmptyPreferencesRepositoryImpl
import com.brian.weather.repository.FakeWeatherRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class CreateHourlyForecastStateUseCaseTest{

    private val location = "90210"
    private val fakeWeatherRepository = FakeWeatherRepository()
    private val emptyPreferencesRepository = EmptyPreferencesRepositoryImpl()
    private val usecase = CreateHourlyForecastStateUseCase(fakeWeatherRepository, emptyPreferencesRepository)

    @Test
    fun createHourlyForecastStateUsecase_repositoryReturnsSuccess_returnsSuccessState() = runBlocking{
        when(val state = usecase.invoke(location)){
            is HourlyForecastState.Success -> {
                assertEquals(HourlyForecastState.Success(state.forecastDomainObject), state)
            }
            else -> {}
        }
    }

    @Test
    fun createHourlyForecastStateUsecase_repositoryReturnsError_returnsErrorState() = runBlocking{
        fakeWeatherRepository.setShouldReturnNetworkError(true)
        when(val state = usecase.invoke(location)){
            is HourlyForecastState.Error -> {
                assertEquals(HourlyForecastState.Error(state.code, state.message), state)
            }
            else -> {}
        }
    }

    @Test
    fun createHourlyForecastStateUsecase_returnsErrorState() = runBlocking{
        fakeWeatherRepository.setShouldReturnException(true)
        when(val state = usecase.invoke(location)){
            is HourlyForecastState.Error -> {
                assertEquals(HourlyForecastState.Error(state.code, state.message), state)
            }
            else -> {}
        }
    }
}
