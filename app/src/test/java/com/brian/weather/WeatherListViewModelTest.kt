
package com.brian.weather
/*
import android.app.Application
import android.content.Context
import android.content.res.Resources
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.brian.weather.data.local.WeatherDao
import com.brian.weather.data.local.WeatherDatabase
import com.brian.weather.data.local.WeatherEntity
import com.brian.weather.presentation.viewmodels.WeatherListState
import com.brian.weather.presentation.viewmodels.WeatherListViewModel
import com.brian.weather.repository.FakePreferencesRepositoryImpl
import com.brian.weather.repository.FakeWeatherRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.IOException


@RunWith(RobolectricTestRunner::class)
class WeatherListViewModelTest {

    private lateinit var weatherDao: WeatherDao
    private lateinit var weatherDatabase: WeatherDatabase
    private lateinit var viewModel: WeatherListViewModel
    private lateinit var resources: Resources


    companion object {
        val context: Context = ApplicationProvider.getApplicationContext()
        val dataStore = PreferenceDataStoreFactory.create(
            corruptionHandler = ReplaceFileCorruptionHandler(
                produceNewData = { emptyPreferences() }
            ),
            migrations = listOf(SharedPreferencesMigration(context, "Preferences")),
            scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
            produceFile = { context.preferencesDataStoreFile("Preferences") })
    }


    @Before
    fun setupDb(){
        weatherDatabase = Room.inMemoryDatabaseBuilder(
            context, WeatherDatabase::class.java
        )
            // Allow main thread queries just for testing
            .allowMainThreadQueries()
            .build()
        weatherDao = weatherDatabase.getWeatherDao()
    }
    @Before
    fun setup() {
        val app = Application()
        //val mockContext = mock(Context::class.java)

        viewModel = WeatherListViewModel(
            weatherRepository = FakeWeatherRepository(),
            weatherDao = weatherDao,
            application = app,
            preferencesRepository = FakePreferencesRepositoryImpl(dataStore)
        )
        resources = context.resources
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        weatherDatabase.close()
    }

    private val weather1 = WeatherEntity(
        id = 1,
        cityName = "Beverly Hills",
        zipCode = "90210",
        sortOrder = 1
    )

    private suspend fun addOneItemToDb() {
        weatherDao.insert(weather1)
    }



    @Test
    fun `call get weather_success object contains data`() = runBlocking {
        addOneItemToDb()
        var response = MutableStateFlow<WeatherListState>(WeatherListState.Loading)
        val job = launch {
           response = viewModel.getAllWeather() as MutableStateFlow<WeatherListState>
        }

        assertEquals(response.value, WeatherListState.Loading)
        job.cancel()
      //  val failure = viewModel.getZipCodesFromDatabase()

    }
}

 */


