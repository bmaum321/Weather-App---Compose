package com.example.weathercompose.settings_tests

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
import com.brian.weathercompose.data.local.WeatherDatabase
import com.brian.weathercompose.data.remote.WeatherApi
import com.brian.weathercompose.presentation.viewmodels.MainViewModel
import com.brian.weathercompose.presentation.viewmodels.WeatherListViewModel
import com.brian.weathercompose.repository.WeatherRepositoryImpl
import com.brian.weathercompose.R
import com.brian.weathercompose.data.settings.PreferencesRepositoryImpl
import com.brian.weathercompose.presentation.WeatherApp
import com.brian.weathercompose.presentation.navigation.*
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
    private val mainViewModel = MainViewModel()
    private lateinit var navController: TestNavHostController

    @Before
    fun setupWeatherNavHost() {
        composeTestRule.setContent {
            navController = TestNavHostController(LocalContext.current).apply {
                navigatorProvider.addNavigator(ComposeNavigator())
            }
            WeatherApp(
                weatherListViewModel = WeatherListViewModel(
                    application = Application(),
                    weatherDao = WeatherDatabase.getDatabase(LocalContext.current).getWeatherDao(),
                    weatherRepository = WeatherRepositoryImpl(WeatherApi),
                    preferencesRepository = PreferencesRepositoryImpl(get())
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
        composeTestRule.waitUntilDoesNotExist(hasTestTag("Loading"))
        composeTestRule.onAllNodesWithTag(testTag = "Celsius", true).onFirst().assertExists()
    }

    /**
     * Verify Show Alerts Setting
     */
    @Test
    fun interfaceSettings_enableAlerts_ShowsAlertFab() {
        navigateToInterfaceScreen()
        composeTestRule.onNodeWithTag("Show Weather Alerts?").performClick()
        composeTestRule.onNodeWithTag("Show Weather Alerts?").performClick()
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
        navigateToInterfaceScreen()
        composeTestRule.onNodeWithTag("Show Weather Alerts?").performClick()
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

    fun ComposeContentTestRule.waitUntilDoesNotExist(
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

