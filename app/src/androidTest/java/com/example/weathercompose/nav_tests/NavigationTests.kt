package com.example.weathercompose.nav_tests

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.brian.weathercompose.data.local.WeatherDatabase
import com.brian.weathercompose.data.remote.WeatherApi
import com.brian.weathercompose.presentation.navigation.MainWeatherList
import com.brian.weathercompose.presentation.viewmodels.MainViewModel
import com.brian.weathercompose.presentation.viewmodels.WeatherListViewModel
import com.brian.weathercompose.repository.WeatherRepositoryImpl
import com.brian.weathercompose.R
import com.brian.weathercompose.presentation.WeatherApp
import com.brian.weathercompose.presentation.navigation.AddLocation
import com.brian.weathercompose.presentation.navigation.Alerts
import com.brian.weathercompose.presentation.navigation.DailyForecast
import com.brian.weathercompose.presentation.screens.AddWeatherScreen
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
@LargeTest
class NavigationTests {
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
                weatherRepository = WeatherRepositoryImpl(WeatherApi)
            ),
                mainViewModel = mainViewModel,
                navController = navController
            )
        }
    }

    /**
     * Verify the start destination
     */
    @Test
    fun weatherNavHost_verifyStartDestination() {
        navController.assertCurrentRouteName(MainWeatherList.route)
    }


    /**
     * Verify the start destination title
     */
    @Test
    fun weatherNavHost_verifyStartDestinationTitle() {
        composeTestRule.onNodeWithStringId(R.string.places).assertExists()
    }

    /**
     * Verify the start screen doesn't have a navigate back button
     */
    @Test
    fun weatherNavHost_verifyBackNavigationNotShownOnStartScreen() {
        val backText = composeTestRule.activity.getString(R.string.back_button)
        composeTestRule.onNodeWithContentDescription(backText).assertDoesNotExist()
    }

    /**
     * Verify navigation to [AddWeatherScreen]
     *  A good naming convention for test methods is the following: thingUnderTest_TriggerOfTest_ResultOfTest
     */
    @Test
    fun weatherNavHost_clickAddWeatherFab_navigatesToAddWeatherScreen() {
        Thread.sleep(2000)
        navigateToAddWeatherScreen()
        navController.assertCurrentRouteName(AddLocation.route)
    }

    /**
     * Verify navigation to [AlertsScreen]
     *  A good naming convention for test methods is the following: thingUnderTest_TriggerOfTest_ResultOfTest
     */
    @Test
    fun weatherNavHost_clickAlertFab_navigatesToAlertsScreen() {
        Thread.sleep(2000)
        navigateToDailyForecastScreen()
        Thread.sleep(2000)
        val buttonDescription = composeTestRule.activity.getString(R.string.alert_fab_description)
        composeTestRule.onNodeWithContentDescription(buttonDescription)
            .performClick()
        Thread.sleep(2000)
        navController.assertCurrentRouteName(Alerts.routeWithArgs)
        val backText = composeTestRule.activity.getString(R.string.back_button)
        composeTestRule.onNodeWithContentDescription(backText).assertExists()
    }

    /**
     * Verify the back button exists on Add Weather Screen
     */
    @Test
    fun weatherNavHost_verifyBackNavigationShownOnAddWeatherScreen() {
        Thread.sleep(2000)
        navigateToAddWeatherScreen()
        val backText = composeTestRule.activity.getString(R.string.back_button)
        composeTestRule.onNodeWithContentDescription(backText).assertExists()
    }

    /**
     * Verify the back button navigates back to home screen
     */
    @Test
    fun weatherNavHost_clickBackButtonOnAddWeatherScreen_navigatesToWeatherListScreen() {
        Thread.sleep(2000)
        navigateToAddWeatherScreen()
        performNavigateUp()
        navController.assertCurrentRouteName(MainWeatherList.route)

    }

    /**
     * Verify navigation to daily forecast screen
     */
    @Test
    fun weatherNavHost_clickLocationCard_navigateToDailyForecastScreen(){
        Thread.sleep(2000)
        navigateToDailyForecastScreen()
        navController.assertCurrentRouteName(DailyForecast.routeWithArgs)
    }

    /**
     * Verify Back Button on daily forecast screen navigates to home screen
     */
    @Test
    fun weatherNavHost_clickButtonOnDailyForecast_navigateToHomeScreen() {
        Thread.sleep(2000)
        navigateToDailyForecastScreen()
        performNavigateUp()
        navController.assertCurrentRouteName(MainWeatherList.route)
    }

    /**
     * Verify navigation to Hourly forecast screen
     */
    @Test
    fun weatherNavHost_clickOnDailyForecast_navigateToHourlyForecast(){
        Thread.sleep(2000)
        navigateToDailyForecastScreen()
        Thread.sleep(2000)
        navigateToHourlyForecastScreen()
        Thread.sleep(2000)
    }

    /**
     * Verify Back Button on hourly forecast screen navigates to daily forecast screen
     */
    @Test
    fun weatherNavHost_clickBackButtonHourlyForecast_navigateToDailyForecastScreen() {
        Thread.sleep(2000)
        navigateToDailyForecastScreen()
        Thread.sleep(2000)
        navigateToHourlyForecastScreen()
        performNavigateUp()
        navController.assertCurrentRouteName(DailyForecast.routeWithArgs)
    }

    /**
     * Verify title is updating correctly
     */
    @Test
    fun weatherNavHost_clickOnPlace_verifyTitleUpdate(){
        Thread.sleep(2000)
        navigateToDailyForecastScreen()
        composeTestRule.onNodeWithText("Syracuse").assertExists()
    }

    /**
     * Verify correct title on hourly forecast screen
     */
    @Test
    fun weatherNavHost_clickOnDay_verifyTitleUpdateOnHourlyForecastScreen(){
        Thread.sleep(2000)
        navigateToDailyForecastScreen()
        Thread.sleep(3000)
        navigateToHourlyForecastScreen()
        Thread.sleep(2000)
        composeTestRule.onNodeWithText("Today").assertExists()
    }

    private fun navigateToAddWeatherScreen(){
        val buttonDescription = composeTestRule.activity.getString(R.string.add_weather_fab_description)
        composeTestRule.onNodeWithContentDescription(buttonDescription)
            .performClick()
    }

    private fun performNavigateUp() {
        val backText = composeTestRule.activity.getString(R.string.back_button)
        composeTestRule.onNodeWithContentDescription(backText).performClick()
    }

    private fun navigateToDailyForecastScreen() {
        composeTestRule.onNodeWithText("Miami" , ignoreCase = true)
            .performClick()
    }

    private fun navigateToHourlyForecastScreen() {
        composeTestRule.onNodeWithText("Today" , ignoreCase = true)
            .performClick()
    }

}

