package com.brian.weather.settings_tests

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.brian.weather.data.local.WeatherDatabase
import com.brian.weather.presentation.viewmodels.MainViewModel
import com.brian.weather.presentation.viewmodels.WeatherListViewModel
import com.brian.weather.R
import com.brian.weather.data.settings.PreferencesRepositoryImpl
import com.brian.weather.presentation.WeatherApp
import com.brian.weather.presentation.navigation.*
import com.brian.weather.presentation.viewmodels.AddWeatherLocationViewModel
import com.brian.weather.presentation.viewmodels.DailyForecastViewModel
import com.brian.weather.presentation.viewmodels.HourlyForecastViewModel
import com.brian.weather.repository.FakeWeatherRepository
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.compose.get


@RunWith(AndroidJUnit4::class)
@LargeTest
class SettingsTests {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()
    private val application = Application()
    private val mainViewModel = MainViewModel()
    private lateinit var navController: TestNavHostController

    @Before
    fun setupWeatherNavHost() {
        composeTestRule.setContent {
            val fakeWeatherRepository = FakeWeatherRepository()
            val preferencesRepository = PreferencesRepositoryImpl(get())
            val weatherDao = WeatherDatabase.getDatabase(LocalContext.current).getWeatherDao()
            navController = TestNavHostController(LocalContext.current).apply {
                navigatorProvider.addNavigator(ComposeNavigator())
            }
            WeatherApp(
                weatherListViewModel = WeatherListViewModel(
                    application = application,
                    weatherDao = weatherDao,
                    weatherRepository = fakeWeatherRepository,
                    //weatherRepository = WeatherRepositoryImpl(WeatherApi),
                    preferencesRepository = preferencesRepository
                ),
                dailyForecastViewModel = DailyForecastViewModel(
                    weatherRepository = fakeWeatherRepository,
                    preferencesRepository =  preferencesRepository,
                    weatherDao = weatherDao,
                    application = application
                ),
                hourlyForecastViewModel = HourlyForecastViewModel(
                    weatherRepository = fakeWeatherRepository,
                    preferencesRepository = preferencesRepository,
                    weatherDao = weatherDao,
                    application = application
                ),
                addWeatherLocationViewModel = AddWeatherLocationViewModel(
                    weatherRepository = fakeWeatherRepository,
                    weatherDao = weatherDao,
                    application = application
                ),
                mainViewModel = mainViewModel,
                navController = navController
            )
        }
    }

    /**
     * Verify 24 hour clock format setting
     */
    @Test
    fun unitSettings_click24HourFormat_ChangesTimeFormatCorrectly() {
        navigateToUnitsScreen()
        composeTestRule.onNodeWithText("Clock Format").performClick()
        composeTestRule.onNodeWithText("24 hour").performClick()
        composeTestRule.onNodeWithText("Ok").performClick()
        performNavigateUp()
        performNavigateUp()
        composeTestRule.waitUntilDoesNotExist(hasTestTag("Loading"))
        composeTestRule.onAllNodesWithTag(
            testTag = composeTestRule.activity.getString(R.string.twenty_four_hour_clock_format),
            useUnmergedTree = true
        ).onFirst().assertExists()
    }

    /**
     * Verify 12 hour clock format setting
     */
    @Test
    fun unitSettings_click12HourFormat_changesTimeFormatCorrectly() {
        navigateToUnitsScreen()
        composeTestRule.onNodeWithText("Clock Format").performClick()
        composeTestRule.onNodeWithText("12 hour").performClick()
        composeTestRule.onNodeWithText("Ok").performClick()
        performNavigateUp()
        performNavigateUp()
        composeTestRule.waitUntilDoesNotExist(hasTestTag("Loading"))
        composeTestRule.onAllNodesWithTag(
            testTag = composeTestRule.activity.getString(R.string.twelve_hour_clock_format),
            useUnmergedTree = true
        ).onFirst().assertExists()
    }

    /**
     * Verify Fahrenheit setting
     */
    @Test
    fun unitSettings_clickUseF_changesTemperatureCorrectly() {
        navigateToUnitsScreen()
        composeTestRule.onNodeWithText("Temperature").performClick()
        composeTestRule.onNodeWithText("Fahrenheit").performClick()
        composeTestRule.onNodeWithText("Ok").performClick()
        performNavigateUp()
        performNavigateUp()
        composeTestRule.waitUntilDoesNotExist(hasTestTag("Loading"))
        composeTestRule.onAllNodesWithTag(testTag = "Fahrenheit", true).onFirst().assertExists()
    }


    /**
     * Verify Celsius setting
     */
    @Test
    fun unitSettings_clickUseC_changesTemperatureCorrectly() {
        navigateToUnitsScreen()
        composeTestRule.onNodeWithText("Temperature").performClick()
        composeTestRule.onNodeWithText("Celsius").performClick()
        composeTestRule.onNodeWithText("Ok").performClick()
        performNavigateUp()
        performNavigateUp()
        composeTestRule.waitUntilDoesNotExist(hasTestTag("Loading"))
        composeTestRule.onAllNodesWithTag(testTag = "Celsius", true).onFirst().assertExists()

    }

    /**
     * Verify wind unit settings
     */
    @Test
    fun unitSettings_clickUseMph_changesUnitCorrectly() {
        navigateToUnitsScreen()
        composeTestRule.onNodeWithText("Wind").performClick()
        composeTestRule.onNodeWithText("MPH").performClick()
        composeTestRule.onNodeWithText("Ok").performClick()
        performNavigateUp()
        performNavigateUp()
        composeTestRule.waitUntilDoesNotExist(hasTestTag("Loading"))
        navigateToDailyForecastScreen()
        composeTestRule.waitUntilDoesNotExist(hasTestTag("Loading"))
        navigateToHourlyForecastScreen()
        composeTestRule.waitUntilDoesNotExist(hasTestTag("Loading"))
        composeTestRule.onAllNodesWithContentDescription(composeTestRule.activity.getString(R.string.expand_button_content_description)).onFirst().performClick()
        composeTestRule.onAllNodesWithTag(testTag = "MPH", true).onFirst().assertExists()
    }


    /**
     * Verify wind unit settings
     */
    @Test
    fun unitSettings_clickUseKph_changesUnitCorrectly() {
        navigateToUnitsScreen()
        composeTestRule.onNodeWithText("Wind").performClick()
        composeTestRule.onNodeWithText("KPH").performClick()
        composeTestRule.onNodeWithText("Ok").performClick()
        performNavigateUp()
        performNavigateUp()
        composeTestRule.waitUntilDoesNotExist(hasTestTag("Loading"))
        navigateToDailyForecastScreen()
        composeTestRule.waitUntilDoesNotExist(hasTestTag("Loading"))
        navigateToHourlyForecastScreen()
        composeTestRule.waitUntilDoesNotExist(hasTestTag("Loading"))
        composeTestRule.onAllNodesWithContentDescription(composeTestRule.activity.getString(R.string.expand_button_content_description)).onFirst().performClick()
        composeTestRule.onAllNodesWithTag(testTag = "KPH", true).onFirst().assertExists()
    }

    /**
     * Verify pressure unit settings
     */
    @Test
    fun unitSettings_clickUseIn_changesUnitCorrectly() {
        navigateToUnitsScreen()
        composeTestRule.onNodeWithText("Pressure").performClick()
        composeTestRule.onNodeWithText("IN").performClick()
        composeTestRule.onNodeWithText("Ok").performClick()
        performNavigateUp()
        performNavigateUp()
        composeTestRule.waitUntilDoesNotExist(hasTestTag("Loading"))
        navigateToDailyForecastScreen()
        composeTestRule.waitUntilDoesNotExist(hasTestTag("Loading"))
        navigateToHourlyForecastScreen()
        composeTestRule.waitUntilDoesNotExist(hasTestTag("Loading"))
        composeTestRule.onAllNodesWithContentDescription(composeTestRule.activity.getString(R.string.expand_button_content_description)).onFirst().performClick()
        composeTestRule.onAllNodesWithTag(testTag = "IN", true).onFirst().assertExists()
    }



    /**
     * Verify pressure unit settings
     */
    @Test
    fun unitSettings_clickUseMm_changesUnitCorrectly() {
        navigateToUnitsScreen()
        composeTestRule.onNodeWithText("Pressure").performClick()
        composeTestRule.onNodeWithText("MM").performClick()
        composeTestRule.onNodeWithText("Ok").performClick()
        performNavigateUp()
        performNavigateUp()
        composeTestRule.waitUntilDoesNotExist(hasTestTag("Loading"))
        navigateToDailyForecastScreen()
        composeTestRule.waitUntilDoesNotExist(hasTestTag("Loading"))
        navigateToHourlyForecastScreen()
        composeTestRule.waitUntilDoesNotExist(hasTestTag("Loading"))
        composeTestRule.onAllNodesWithContentDescription(composeTestRule.activity.getString(R.string.expand_button_content_description)).onFirst().performClick()
        composeTestRule.onAllNodesWithTag(testTag = "MM", true).onFirst().assertExists()
    }



    /**
     * Verify Show Alerts Setting
     */
    @Test
    fun interfaceSettings_enableAlerts_ShowsAlertFab() {
        //TODO this test needs to verify that that setting is on, either pass a fake preferences repo or find some other method
        navigateToInterfaceScreen()
        composeTestRule.onNodeWithTag("Show Weather Alerts?").performClick()
        composeTestRule.onNodeWithTag("Show Weather Alerts?").performClick()
        performNavigateUp()
        performNavigateUp()
        composeTestRule.waitUntilDoesNotExist(hasTestTag("Loading"))
        navigateToDailyForecastScreen()
        composeTestRule.waitUntilDoesNotExist(hasTestTag("Loading"))
        composeTestRule
            .onNodeWithContentDescription(composeTestRule.activity.getString(R.string.alert_fab_description))
            .assertExists()
    }

    /**
     * Verify Show Alerts is disabled
     */
    @Test
    fun interfaceSettings_disableAlerts_ShowsAlertFab() {
        //TODO this test needs to verify that that setting is on, either pass a fake preferences repo or find some other method
        navigateToInterfaceScreen()
        composeTestRule.onNodeWithTag("Show Weather Alerts?").performClick()
        performNavigateUp()
        performNavigateUp()
        composeTestRule.waitUntilDoesNotExist(hasTestTag("Loading"))
        navigateToDailyForecastScreen()
        composeTestRule.waitUntilDoesNotExist(hasTestTag("Loading"))
        composeTestRule
            .onNodeWithContentDescription(composeTestRule.activity.getString(R.string.alert_fab_description))
            .assertDoesNotExist()
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

    private fun ComposeContentTestRule.waitUntilDoesNotExist(
        matcher: SemanticsMatcher,
        timeoutMillis: Long = 3_000L
    ) {
        return this.waitUntilNodeCount(matcher, 0, timeoutMillis)
    }

    private fun navigateToUnitsScreen() {
        composeTestRule.onNodeWithContentDescription(composeTestRule.activity.getString(R.string.action_menu))
            .performClick()
        composeTestRule.onNodeWithText(composeTestRule.activity.getString(R.string.units))
            .performClick()
    }

    private fun navigateToInterfaceScreen() {
        composeTestRule.onNodeWithContentDescription(composeTestRule.activity.getString(R.string.action_menu))
            .performClick()
        composeTestRule.onNodeWithText("Interface")
            .performClick()
    }

    private fun performNavigateUp() {
        val backText = composeTestRule.activity.getString(R.string.back_button)
        composeTestRule.onNodeWithContentDescription(backText).performClick()
    }

    private fun navigateToAddWeatherScreen() {
        val buttonDescription =
            composeTestRule.activity.getString(R.string.add_weather_fab_description)
        composeTestRule.onNodeWithContentDescription(buttonDescription)
            .performClick()
    }

    private fun navigateToDailyForecastScreen() {
        composeTestRule.onNodeWithText("Miami", ignoreCase = true)
            .performClick()
    }

    private fun navigateToHourlyForecastScreen() {
        composeTestRule.onNodeWithText("Today", ignoreCase = true)
            .performClick()
    }


}

