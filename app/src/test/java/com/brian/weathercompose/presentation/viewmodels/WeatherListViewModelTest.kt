package com.brian.weathercompose.presentation.viewmodels

import android.app.Application
import androidx.compose.ui.platform.LocalContext
import com.brian.weathercompose.data.local.WeatherDatabase
import com.example.weathercompose.repository.FakeWeatherRepository
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

class WeatherListViewModelTest {

    private lateinit var viewModel: WeatherListViewModel

    @Before
    fun setup() {
        val app = Application()
        viewModel = WeatherListViewModel(
            weatherRepository = FakeWeatherRepository(),
            weatherDao = WeatherDatabase.getDatabase(app.applicationContext).getWeatherDao(),
            application = app
        )
    }

    @Test
    fun `insert weather item with empty field, returns error`() {
        //val value = viewModel.getAllWeather()
        
        // Assert that it returns the correct class here
    }
}