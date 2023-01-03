package com.brian.weather.domain.usecase

import com.brian.weather.presentation.viewmodels.WeatherListState
import com.brian.weather.repository.EmptyPreferencesRepositoryImpl
import com.brian.weather.repository.FakeWeatherRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class CreateWeatherListStateUsecaseTest {

    private val zipcodes = listOf("90210")
    private val fakeWeatherRepository = FakeWeatherRepository()
    private val emptyPreferencesRepository = EmptyPreferencesRepositoryImpl()
    private val usecase = CreateWeatherListStateUsecase(fakeWeatherRepository, emptyPreferencesRepository)

    @Test
    fun createWeatherListStateUsecase_repositoryReturnsSuccess_returnsSuccessState() = runBlocking{
        when(val state = usecase.invoke(zipcodes)){
            is WeatherListState.Success -> {
                assertEquals(WeatherListState.Success(state.weatherDomainObjects), state)
            }
            else -> {}
        }
    }

    @Test
    fun createWeatherListStateUsecase_repositoryReturnsError_returnsErrorState() = runBlocking{
        fakeWeatherRepository.setShouldReturnNetworkError(true)
        when(val state = usecase.invoke(zipcodes)){
            is WeatherListState.Error -> {
                assertEquals(WeatherListState.Error(state.message), state)
            }
            else -> {}
        }
    }

    @Test
    fun createWeatherListStateUsecase_returnsErrorState() = runBlocking{
        fakeWeatherRepository.setShouldReturnException(true)
        when(val state = usecase.invoke(zipcodes)){
            is WeatherListState.Error -> {
                assertEquals(WeatherListState.Error(state.message), state)
            }
            else -> {}
        }
    }
}