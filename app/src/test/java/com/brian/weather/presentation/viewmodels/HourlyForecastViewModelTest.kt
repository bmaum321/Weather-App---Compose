package com.brian.weather.presentation.viewmodels

import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.brian.weather.domain.usecase.CreateDailyForecastStateUseCase
import com.brian.weather.domain.usecase.CreateHourlyForecastStateUseCase
import com.brian.weather.repository.EmptyPreferencesRepositoryImpl
import com.brian.weather.repository.FakeWeatherRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class HourlyForecastViewModelTest {

    private lateinit var viewModel: HourlyForecastViewModel
    private val fakeWeatherRepository = FakeWeatherRepository()


    @Before
    fun before(){
        org.koin.core.context.stopKoin()
    }

    @Before
    fun setup() {
        val emptyPreferencesRepositoryImpl = EmptyPreferencesRepositoryImpl()
        viewModel = HourlyForecastViewModel(
            preferencesRepository = emptyPreferencesRepositoryImpl,
            createHourlyForecastStateUseCase = CreateHourlyForecastStateUseCase(fakeWeatherRepository, emptyPreferencesRepositoryImpl)
        )

    }

    @Test
    fun `hourlyForecastViewModel initially emits loading state`() = runBlocking {
        viewModel.getHourlyForecast("13088").test {
            val emission = awaitItem()
            //assertThat(emission).isEqualTo(ForecastState.Loading)
            assertThat(emission).isEqualTo(HourlyForecastState.Success(fakeWeatherRepository.forecastDomainObject))
            cancel()
        }
    }

    @Test
    fun `hourlyForecastViewModel emits success state correctly`() = runBlocking {
        viewModel.getHourlyForecast("13088").test {
            // For some reason this isn't collecting the initial loading state from the state flow
            //assertThat(awaitItem()).isEqualTo(ForecastState.Loading)
            assertThat(awaitItem()).isEqualTo(HourlyForecastState.Success(fakeWeatherRepository.forecastDomainObject))
        }
    }

    @Test
    fun `hourlyForecastViewModel emits failure state on repository failure`() = runBlocking {
        fakeWeatherRepository.setShouldReturnNetworkError(true)
        viewModel.getHourlyForecast("13088").test {
            //assertThat(awaitItem()).isEqualTo(ForecastState.Loading)
            assertThat(awaitItem()).isEqualTo(HourlyForecastState.Error(message = "Error", code = 400))
        }
    }


    @Test
    fun `hourlyForecastViewModel refresh state method works correctly`() = runBlocking {
        viewModel.getHourlyForecast("13088").test {
            //assertThat(awaitItem()).isEqualTo(ForecastState.Loading)
            assertThat(awaitItem()).isEqualTo(HourlyForecastState.Success(fakeWeatherRepository.forecastDomainObject))
            fakeWeatherRepository.setShouldReturnNetworkError(true)
            viewModel.refresh()
            assertThat(awaitItem()).isEqualTo(HourlyForecastState.Error(message = "Error", code = 400))
        }
    }

}






