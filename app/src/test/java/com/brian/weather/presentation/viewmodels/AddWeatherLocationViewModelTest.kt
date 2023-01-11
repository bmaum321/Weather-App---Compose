package com.brian.weather.presentation.viewmodels


import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.brian.weather.domain.usecase.CreateSearchStateUseCase
import com.brian.weather.repository.EmptyPreferencesRepositoryImpl
import com.brian.weather.repository.FakeWeatherRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class AddWeatherLocationViewModelTest {

    private lateinit var viewModel: AddWeatherLocationViewModel
    private val fakeWeatherRepository = FakeWeatherRepository()


    @Before
    fun before(){
        org.koin.core.context.stopKoin()
    }

    @Before
    fun setup() {
        val emptyPreferencesRepositoryImpl = EmptyPreferencesRepositoryImpl()
        viewModel = AddWeatherLocationViewModel(
            weatherRepository = fakeWeatherRepository,
            createSearchStateUseCase = CreateSearchStateUseCase(fakeWeatherRepository, emptyPreferencesRepositoryImpl)
        )

    }


    @Test
    fun `addWeatherLocationViewModel initially emits loading state`() = runBlocking {
        viewModel.getSearchResults.test {
        //viewModel.setQuery("Miami")
            assertThat(awaitItem()).isEqualTo(SearchState.Loading)
           // awaitItem()
        }
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `addWeatherLocationViewModel emits success state correctly`() = runTest {
        viewModel.getSearchResults.test {
            //viewModel.setQuery("")
            assertThat(awaitItem()).isEqualTo(SearchState.Loading)
            //The set query method will emit the query value after 500ms
           // viewModel.setQuery("Miami")
           // advanceTimeBy(1000)
            /**
             * Not sure how to make the state flow emit the next value with the set query method
             * It sets a job with a delay of 500ms
             *
             * Using clear query to force the query to emit an empty result immediately
             */
            viewModel.clearSearchQuery()
            assertThat(awaitItem()).isEqualTo(SearchState.Success(listOf("Miami, Florida")))
        }
    }


    @Test
    fun `addWeatherLocationViewModel emits failure state on repository failure`() = runBlocking {
        fakeWeatherRepository.setShouldReturnNetworkError(true)
        viewModel.getSearchResults.test {
            assertThat(awaitItem()).isEqualTo(SearchState.Loading)
            viewModel.clearSearchQuery()
            assertThat(awaitItem()).isEqualTo(SearchState.Error(message = "Error", code = 400))
        }
    }

    @Test
    fun `addWeatherLocationViewModel emits failure state on repository exception`() = runBlocking {
        fakeWeatherRepository.setShouldReturnException(true)
        viewModel.getSearchResults.test {
            assertThat(awaitItem()).isEqualTo(SearchState.Loading)
            viewModel.clearSearchQuery()
            assertThat(awaitItem()).isEqualTo(SearchState.Error(message = "Exception", code = 0))
        }
    }


}