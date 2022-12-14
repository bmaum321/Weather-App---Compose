package com.brian.weather.repository

import android.app.Application
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.work.ListenableWorker.Result.retry
import com.brian.weather.R
import com.brian.weather.data.local.WeatherDao
import com.brian.weather.data.local.WeatherDatabase
import com.brian.weather.data.local.WeatherEntity
import com.brian.weather.data.settings.PreferencesRepositoryImpl
import com.brian.weather.domain.usecase.CreateDailyForecastStateUseCase
import com.brian.weather.domain.usecase.CreateHourlyForecastStateUseCase
import com.brian.weather.domain.usecase.CreateSearchStateUseCase
import com.brian.weather.domain.usecase.CreateWeatherListStateUsecase
import com.brian.weather.presentation.WeatherApp
import com.brian.weather.presentation.viewmodels.AddWeatherLocationViewModel
import com.brian.weather.presentation.viewmodels.DailyForecastViewModel
import com.brian.weather.presentation.viewmodels.HourlyForecastViewModel
import com.brian.weather.presentation.viewmodels.MainViewModel
import com.brian.weather.presentation.viewmodels.WeatherListViewModel
import com.brian.weather.util.Constants.ERRORTEXT
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.compose.get
import java.io.IOException

@RunWith(AndroidJUnit4::class)
@LargeTest
class RepositoryTests {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val mainViewModel = MainViewModel()
    lateinit var navController: TestNavHostController
    lateinit var weatherDatabase: WeatherDatabase
    private lateinit var fakeWeatherRepository: FakeWeatherRepository
    private lateinit var weatherDao: WeatherDao


    @Before
    fun createDb() {
        val context: Context = ApplicationProvider.getApplicationContext()
        weatherDatabase = Room.inMemoryDatabaseBuilder(
            context, WeatherDatabase::class.java
        )
            // Allow main thread queries just for testing
            .allowMainThreadQueries()
            .build()
        weatherDao = weatherDatabase.getWeatherDao()


        runBlocking {
            weatherDao.insert(WeatherEntity(id = 1, zipCode = "Miami, Florida", sortOrder = 1, cityName = "Miami"))
        }
    }

    @Before
    fun setupWeatherNavHost() {

        composeTestRule.setContent {
            fakeWeatherRepository = FakeWeatherRepository(get())
            val preferencesRepository = PreferencesRepositoryImpl(get())
            fakeWeatherRepository.setShouldReturnNetworkError(true)
            navController = TestNavHostController(LocalContext.current).apply {
                navigatorProvider.addNavigator(ComposeNavigator())
            }
            WeatherApp(
                weatherListViewModel = WeatherListViewModel(
                    weatherRepository = fakeWeatherRepository,
                    preferencesRepository = preferencesRepository,
                    createWeatherListStateUsecase = CreateWeatherListStateUsecase(fakeWeatherRepository, preferencesRepository)
                ),
                dailyForecastViewModel = DailyForecastViewModel(
                    preferencesRepository = preferencesRepository,
                    createDailyForecastStateUseCase = CreateDailyForecastStateUseCase(fakeWeatherRepository,preferencesRepository)
                ),
                hourlyForecastViewModel = HourlyForecastViewModel(
                    preferencesRepository = preferencesRepository,
                    createHourlyForecastStateUseCase = CreateHourlyForecastStateUseCase(fakeWeatherRepository,preferencesRepository)
                ),
                addWeatherLocationViewModel = AddWeatherLocationViewModel(
                    weatherRepository = fakeWeatherRepository,
                    createSearchStateUseCase = CreateSearchStateUseCase(fakeWeatherRepository,preferencesRepository)
                ),
                mainViewModel = mainViewModel,
                navController = navController,
                weatherRepository = fakeWeatherRepository
            )
        }
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        weatherDatabase.close()
    }


    /**
     * Verify error screen is shown when repository returns a network failure result
     * Confirm retry button will reload content after repository returns a success result
     */
    @Test
    fun weatherRepository_repositoryReturnsFailure_mainScreenShowsError() {
        composeTestRule.onNodeWithText("Retry").assertExists()
        fakeWeatherRepository.setShouldReturnNetworkError(false)
        retry()
        composeTestRule.onNodeWithText("Miami").assertExists()
    }

    /**
     * Verify error screen is shown on daily forecast screen when repository returns a network failure result
     * Confirm retry button will reload content after repository returns a success result
     */
    @Test
    fun weatherRepository_repositoryReturnsFailure_dailyForecastShowsError() {
        fakeWeatherRepository.setShouldReturnNetworkError(false)
        retry()
        navigateToDailyForecastScreen()
        navigateToHourlyForecastScreen()
        fakeWeatherRepository.setShouldReturnNetworkError(true)
        performNavigateUp()
        composeTestRule.onNodeWithText("Retry").assertExists()
        fakeWeatherRepository.setShouldReturnNetworkError(false)
        retry()
        composeTestRule.onNodeWithText("Today").assertExists()
    }


    /**
     * Verify error screen is shown on hourly forecast screen when repository returns a network failure result
     * Confirm retry button will reload content after repository returns a success result
     */
    @Test
    fun weatherRepository_repositoryReturnsFailure_hourlyForecastShowsError() {
        fakeWeatherRepository.setShouldReturnNetworkError(false)
        retry()
        composeTestRule.waitUntilDoesNotExist(hasTestTag("Loading"))
        navigateToDailyForecastScreen()
        composeTestRule.waitUntilDoesNotExist(hasTestTag("Loading"))
        fakeWeatherRepository.setShouldReturnNetworkError(true)
        navigateToHourlyForecastScreen()
        composeTestRule.onNodeWithText("Retry").assertExists()
        fakeWeatherRepository.setShouldReturnNetworkError(false)
        retry()
        composeTestRule.onNodeWithText("12:00 AM").assertExists()
    }


    private fun ComposeContentTestRule.waitUntilDoesNotExist(
        matcher: SemanticsMatcher,
        timeoutMillis: Long = 3_000L
    ) {
        return this.waitUntilNodeCount(matcher, 0, timeoutMillis)
    }

    private fun ComposeContentTestRule.waitUntilNodeCount(
        matcher: SemanticsMatcher,
        count: Int,
        timeoutMillis: Long = 3_000L
    ) {
        this.waitUntil(timeoutMillis) {
            this.onAllNodes(matcher).fetchSemanticsNodes().size == count
        }
    }

    private fun navigateToAddWeatherScreen() {
        val buttonDescription =
            composeTestRule.activity.getString(R.string.add_weather_fab_description)
        composeTestRule.onNodeWithContentDescription(buttonDescription)
            .performClick()
    }

    private fun performNavigateUp() {
        val backText = composeTestRule.activity.getString(R.string.back_button)
        composeTestRule.onNodeWithContentDescription(backText).performClick()
    }

    private fun navigateToDailyForecastScreen() {
        composeTestRule.onNodeWithText("Miami", ignoreCase = true)
            .performClick()
    }

    private fun navigateToHourlyForecastScreen() {
        composeTestRule.onNodeWithText("Today", ignoreCase = true)
            .performClick()
    }

    private fun retry(){
        composeTestRule.onNodeWithText("Retry").performClick()
    }
}


