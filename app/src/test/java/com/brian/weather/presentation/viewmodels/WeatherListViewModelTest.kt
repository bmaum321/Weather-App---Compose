package com.brian.weather.presentation.viewmodels

import androidx.compose.runtime.collectAsState
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.brian.weather.domain.usecase.CreateWeatherListStateUsecase
import com.brian.weather.repository.EmptyPreferencesRepositoryImpl
import com.brian.weather.repository.FakeWeatherRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class WeatherListViewModelTest {

    private lateinit var viewModel: WeatherListViewModel
    private val fakeWeatherRepository = FakeWeatherRepository()


    @Before
    fun before(){
        org.koin.core.context.stopKoin()
    }

    @Before
    fun setup() {
        val emptyPreferencesRepositoryImpl = EmptyPreferencesRepositoryImpl()
        viewModel = WeatherListViewModel(
            weatherRepository = fakeWeatherRepository,
            preferencesRepository = emptyPreferencesRepositoryImpl,
            createWeatherListStateUsecase = CreateWeatherListStateUsecase(fakeWeatherRepository, emptyPreferencesRepositoryImpl)
        )

    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `weather ticker emits data in correct order`() = runTest {
            viewModel.weatherTicker(
                time = "12:00PM",
                windspeed = "12 MPH",
                feelsLikeTemp = "32",
                humidity = 10
            ).test {
              for (i in listOf("12:00PM", "Wind: 12 MPH", "Feels Like: 32°", "Humidity: 10 %")) {
                  val emission = awaitItem()
                  assertThat(emission).isEqualTo(i)
              }
            }
    }

    @Test
    fun `weatherListViewModel initially emits loading state`() = runBlocking {
            viewModel.getAllWeather().test {
                val emission = awaitItem()
                assertThat(emission).isEqualTo(WeatherListState.Loading)
                awaitItem()
            }
    }

    @Test
    fun `weatherListViewModel emits success state correctly`() = runBlocking {
        viewModel.getAllWeather().test {
            assertThat(awaitItem()).isEqualTo(WeatherListState.Loading)
            assertThat(awaitItem()).isEqualTo(WeatherListState.Success(fakeWeatherRepository.weatherItems))
        }
    }

    @Test
    fun `weatherListViewModel emits failure state on repository failure`() = runBlocking {
            fakeWeatherRepository.setShouldReturnNetworkError(true)
            viewModel.getAllWeather().test {
                assertThat(awaitItem()).isEqualTo(WeatherListState.Loading)
                assertThat(awaitItem()).isEqualTo(WeatherListState.Error(message = "Error"))
            }
    }

    @Test
    fun `weatherListViewModel emits empty state correctly`() = runBlocking {
        fakeWeatherRepository.setShouldReturnEmptyList(true)
            viewModel.getAllWeather().test {
                assertThat(awaitItem()).isEqualTo(WeatherListState.Loading)
                assertThat(awaitItem()).isEqualTo(WeatherListState.Empty)
            }
    }

    @Test
    fun `weatherListViewModel refresh state method works correctly`()= runBlocking {
        viewModel.getAllWeather().test {
            assertThat(awaitItem()).isEqualTo(WeatherListState.Loading)
            assertThat(awaitItem()).isEqualTo(WeatherListState.Success(fakeWeatherRepository.weatherItems))
            fakeWeatherRepository.setShouldReturnNetworkError(true)
            viewModel.refresh()
            assertThat(awaitItem()).isEqualTo(WeatherListState.Error(message = "Error"))
        }
    }

}






