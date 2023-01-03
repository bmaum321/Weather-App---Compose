package com.brian.weather.domain.usecase

import com.brian.weather.presentation.viewmodels.SearchState
import com.brian.weather.presentation.viewmodels.WeatherListState
import com.brian.weather.repository.EmptyPreferencesRepositoryImpl
import com.brian.weather.repository.FakeWeatherRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class CreateSearchStateUseCaseTest{

    private val query = "90210"
    private val fakeWeatherRepository = FakeWeatherRepository()
    private val emptyPreferencesRepository = EmptyPreferencesRepositoryImpl()
    private val usecase = CreateSearchStateUseCase(fakeWeatherRepository, emptyPreferencesRepository)

    @Test
    fun createWeatherListStateUsecase_repositoryReturnsSuccess_returnsSuccessState() = runBlocking{
        when(val state = usecase.invoke(query)){
            is SearchState.Success -> {
                assertEquals(SearchState.Success(state.searchResults), state)
            }
            else -> {}
        }
    }

    @Test
    fun createWeatherListStateUsecase_repositoryReturnsError_returnsErrorState() = runBlocking{
        fakeWeatherRepository.setShouldReturnNetworkError(true)
        when(val state = usecase.invoke(query)){
            is SearchState.Error -> {
                assertEquals(SearchState.Error(state.code, state.message), state)
            }
            else -> {}
        }
    }

    @Test
    fun createWeatherListStateUsecase_returnsErrorState() = runBlocking{
        fakeWeatherRepository.setShouldReturnException(true)
        when(val state = usecase.invoke(query)){
            is SearchState.Error -> {
                assertEquals(SearchState.Error(state.code, state.message), state)
            }
            else -> {}
        }
    }
}