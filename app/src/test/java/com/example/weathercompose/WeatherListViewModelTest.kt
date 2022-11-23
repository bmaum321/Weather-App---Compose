package com.example.weathercompose

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.preference.PreferenceManager
import androidx.room.Room
import com.brian.weathercompose.data.local.WeatherDao
import com.brian.weathercompose.data.local.WeatherDatabase
import com.brian.weathercompose.presentation.viewmodels.WeatherListState
import com.brian.weathercompose.presentation.viewmodels.WeatherListViewModel
import com.example.weathercompose.repository.FakeWeatherRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.mock

@RunWith(MockitoJUnitRunner::class)
class WeatherListViewModelTest {

    private lateinit var viewModel: WeatherListViewModel
    private lateinit var pref: SharedPreferences
    private lateinit var resources: Resources


   val db = Room.in




    @Before
    fun setup() {
        val app = Application()
        val mockContext = mock(Context::class.java)
        viewModel = WeatherListViewModel(
            weatherRepository = FakeWeatherRepository(),
            weatherDao = WeatherDatabase.,
            application = app
        )
        pref = PreferenceManager.getDefaultSharedPreferences(mockContext)
        resources = mockContext.resources
    }

    @Test
    fun `insert weather item with empty field, returns error`() = runBlocking {
        val response = viewModel.getAllWeather(pref, resources)
        when (response.value) {
            is WeatherListState.Success -> assertTrue((response.value as WeatherListState.Success).weatherDomainObjects.isNotEmpty())
            else -> assertTrue((response.value as WeatherListState.Success).weatherDomainObjects.isEmpty())
        }
        
        // Assert that it returns the correct class here
    }
}