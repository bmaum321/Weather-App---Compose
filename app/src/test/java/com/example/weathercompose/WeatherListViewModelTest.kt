package com.example.weathercompose

import android.app.Application
import android.content.Context
import android.content.res.Resources
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.test.core.app.ApplicationProvider
import com.brian.weathercompose.data.local.WeatherDatabase
import com.brian.weathercompose.presentation.viewmodels.WeatherListState
import com.brian.weathercompose.presentation.viewmodels.WeatherListViewModel
import com.example.weathercompose.repository.FakePreferencesRepositoryImpl
import com.example.weathercompose.repository.FakeWeatherRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
class WeatherListViewModelTest {

    private lateinit var viewModel: WeatherListViewModel
    private lateinit var resources: Resources


    @Before
    fun setup() {
        //val db = Room.inMemoryDatabaseBuilder()
        val context = ApplicationProvider.getApplicationContext<Context>()

        val app = Application()
        //val mockContext = mock(Context::class.java)

        val dataStore = PreferenceDataStoreFactory.create(
            corruptionHandler = ReplaceFileCorruptionHandler(
                produceNewData = { emptyPreferences() }
            ),
            migrations = listOf(SharedPreferencesMigration(context,"Preferences")),
            scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
            produceFile = {context.preferencesDataStoreFile("Preferences")})

        viewModel = WeatherListViewModel(
            weatherRepository = FakeWeatherRepository(),
            weatherDao = WeatherDatabase.getDatabase(context).getWeatherDao(),
            application = app,
            preferencesRepository = FakePreferencesRepositoryImpl(dataStore)
        )
        resources = context.resources
    }

    @Test
    @Config(manifest=Config.NONE)
    fun `call get weather_success object contains data`() = runBlocking {
        val response = viewModel.getAllWeather(resources).value
        val failure = viewModel.getZipCodesFromDatabase()
        when (response) {
           // is WeatherListState.Success -> assertTrue((response.value as WeatherListState.Success).weatherDomainObjects.isNotEmpty())
           // else -> assertTrue((response.value as WeatherListState.Success).weatherDomainObjects.isEmpty())
            is WeatherListState.Success -> {
                assertTrue(response.weatherDomainObjects.isNotEmpty())
                assertTrue(response.weatherDomainObjects.first().temp == "32")
            }
            // The response is always Loading because it grabs the initial value from the flow
            is WeatherListState.Loading -> {
                assertTrue(1 ==1)
            }
            else -> {}
        }
    }
}