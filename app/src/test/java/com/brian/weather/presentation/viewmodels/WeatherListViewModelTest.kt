package com.brian.weather.presentation.viewmodels

import android.content.res.Resources
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.brian.weather.data.local.WeatherDao
import com.brian.weather.data.local.WeatherDatabase
import com.brian.weather.data.local.WeatherEntity
import com.brian.weather.domain.usecase.CreateWeatherListStateUsecase
import com.brian.weather.presentation.viewmodels.WeatherListViewModel
import com.brian.weather.repository.EmptyPreferencesRepositoryImpl
import com.brian.weather.repository.FakeWeatherRepository
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import org.junit.Before
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class WeatherListViewModelTest {

    private lateinit var weatherDao: WeatherDao
    private lateinit var weatherDatabase: WeatherDatabase
    private lateinit var viewModel: WeatherListViewModel
    private lateinit var resources: Resources


    @Before
    fun setup() {
        val fakeWeatherRepository = FakeWeatherRepository()
        val emptyPreferencesRepositoryImpl = EmptyPreferencesRepositoryImpl()
        viewModel = WeatherListViewModel(
            weatherRepository = fakeWeatherRepository,
            preferencesRepository = emptyPreferencesRepositoryImpl,
            createWeatherListStateUsecase = CreateWeatherListStateUsecase(fakeWeatherRepository, emptyPreferencesRepositoryImpl)
        )

    }


    private val weather1 = WeatherEntity(
        id = 1,
        cityName = "Beverly Hills",
        zipCode = "90210",
        sortOrder = 1
    )


    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `weather ticker emits data in correct order`() = runTest {

        val job = launch {
            viewModel.weatherTicker(
                time = "12:00PM",
                windspeed = "12 MPH",
                feelsLikeTemp = "32",
                humidity = 10
            ).test {
              for (i in listOf("12:00PM", "12 MPH", "32", 10)) {
                  val emission = awaitItem()
                  assertThat(emission).isEqualTo(i)
                 // advanceTimeBy(3000)
              }
            }
        }
        job.cancel()
    }

    @Test
    fun `viewmodel initially emits loading state`() = runBlocking {
        val job = launch {
            val state = viewModel.getAllWeather().test {
                val emission = awaitItem()
                assertThat(emission).isEqualTo(WeatherListState.Loading)
            }
        }
        job.cancel()
    }

    @Test
    fun `viewmodel emits success state`() = runBlocking {
        val job = launch {

            viewModel.getAllWeather().test {
                val emission = awaitComplete()
                assertThat(emission).isEqualTo(WeatherListState.Success())

            }
        }
        job.cancel()
    }

}






