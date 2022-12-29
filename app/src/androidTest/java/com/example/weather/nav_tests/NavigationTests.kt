package com.example.weather.nav_tests

import android.app.Application
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.brian.weather.data.local.WeatherDatabase
import com.brian.weather.presentation.viewmodels.MainViewModel
import com.brian.weather.presentation.viewmodels.WeatherListViewModel
import com.brian.weather.R
import com.brian.weather.data.local.WeatherDao
import com.brian.weather.data.local.WeatherEntity
import com.brian.weather.data.settings.PreferencesRepositoryImpl
import com.brian.weather.presentation.WeatherApp
import com.brian.weather.presentation.navigation.*
import com.brian.weather.presentation.screens.AddWeatherScreen
import com.brian.weather.presentation.viewmodels.DailyForecastViewModel
import com.brian.weather.presentation.viewmodels.HourlyForecastViewModel
import com.brian.weather.repository.fakedata.FakeWeatherRepository
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
class NavigationTests {


    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()
    private val mainViewModel = MainViewModel()
    private val application = Application()
    private lateinit var navController: TestNavHostController
    private lateinit var weatherDao: WeatherDao
    private lateinit var weatherDatabase: WeatherDatabase


    /**
     * It seems like if I create a fake database, I cant insert anything into it reliably before
     * running a navigation test
     */

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
           // val weatherDao = WeatherDatabase.getDatabase(LocalContext.current).getWeatherDao()
            val fakeWeatherRepository = FakeWeatherRepository()
            val preferencesRepository = PreferencesRepositoryImpl(get())
            navController = TestNavHostController(LocalContext.current).apply {
                navigatorProvider.addNavigator(ComposeNavigator())
            }
            WeatherApp(
                weatherListViewModel = WeatherListViewModel(
                    application = application,
                    weatherDao = weatherDao,
                    weatherRepository = fakeWeatherRepository,
                    preferencesRepository = preferencesRepository
                ),
                dailyForecastViewModel = DailyForecastViewModel(
                    weatherRepository = fakeWeatherRepository,
                    preferencesRepository = preferencesRepository,
                    weatherDao = weatherDao,
                    application = application
                ),
                hourlyForecastViewModel = HourlyForecastViewModel(
                    weatherRepository = fakeWeatherRepository,
                    preferencesRepository = preferencesRepository,
                    weatherDao = weatherDao,
                    application = application
                ),
                mainViewModel = mainViewModel,
                navController = navController
            )
        }
    }




   //  private suspend fun insertWeatherIntoDb() {
    //    weatherDao.insert(WeatherEntity(id = 1, zipCode = "Miami, Florida", sortOrder = 1, cityName = "Miami"))
  //  }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        weatherDatabase.close()
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
        composeTestRule.waitUntilDoesNotExist(hasTestTag("Loading"))
        navigateToAddWeatherScreen()
        navController.assertCurrentRouteName(AddLocation.route)
    }

    /**
     * Verify navigation to [AlertsScreen]
     *  A good naming convention for test methods is the following: thingUnderTest_TriggerOfTest_ResultOfTest
     */
    @Test
    fun weatherNavHost_clickAlertFab_navigatesToAlertsScreen(): Unit = runBlocking {
        composeTestRule.waitUntilDoesNotExist(hasTestTag("Loading"))
        navigateToDailyForecastScreen()
        composeTestRule.waitUntilDoesNotExist(hasTestTag("Loading"))
        val buttonDescription = composeTestRule.activity.getString(R.string.alert_fab_description)
        composeTestRule.onNodeWithContentDescription(buttonDescription)
            .performClick()
        composeTestRule.waitUntilDoesNotExist(hasTestTag("Loading"))
        navController.assertCurrentRouteName(Alerts.routeWithArgs)
        val backText = composeTestRule.activity.getString(R.string.back_button)
        composeTestRule.onNodeWithContentDescription(backText).assertExists()
    }

    /**
     * Verify the back button exists on Add Weather Screen
     */
    @Test
    fun weatherNavHost_verifyBackNavigationShownOnAddWeatherScreen() {
        composeTestRule.waitUntilDoesNotExist(hasTestTag("Loading"))
        navigateToAddWeatherScreen()
        val backText = composeTestRule.activity.getString(R.string.back_button)
        composeTestRule.onNodeWithContentDescription(backText).assertExists()
    }

    /**
     * Verify the back button navigates back to home screen
     */
    @Test
    fun weatherNavHost_clickBackButtonOnAddWeatherScreen_navigatesToWeatherListScreen() {
        composeTestRule.waitUntilDoesNotExist(hasTestTag("Loading"))
        navigateToAddWeatherScreen()
        performNavigateUp()
        navController.assertCurrentRouteName(MainWeatherList.route)

    }

    /**
     * Verify navigation to daily forecast screen
     */
    @Test
    fun weatherNavHost_clickLocationCard_navigateToDailyForecastScreen() {
        composeTestRule.waitUntilDoesNotExist(hasTestTag("Loading"))
        navigateToDailyForecastScreen()
        navController.assertCurrentRouteName(DailyForecast.routeWithArgs)
    }

    /**
     * Verify Back Button on daily forecast screen navigates to home screen
     */
    @Test
    fun weatherNavHost_clickButtonOnDailyForecast_navigateToHomeScreen() {
        composeTestRule.waitUntilDoesNotExist(hasTestTag("Loading"))
        navigateToDailyForecastScreen()
        performNavigateUp()
        navController.assertCurrentRouteName(MainWeatherList.route)
    }

    /**
     * Verify navigation to Hourly forecast screen
     */
    @Test
    fun weatherNavHost_clickOnDailyForecast_navigateToHourlyForecast() {
        composeTestRule.waitUntilDoesNotExist(hasTestTag("Loading"))
        navigateToDailyForecastScreen()
        composeTestRule.waitUntilDoesNotExist(hasTestTag("Loading"))
        navigateToHourlyForecastScreen()
        composeTestRule.waitUntilDoesNotExist(hasTestTag("Loading"))
    }

    /**
     * Verify Back Button on hourly forecast screen navigates to daily forecast screen
     */
    @Test
    fun weatherNavHost_clickBackButtonHourlyForecast_navigateToDailyForecastScreen() {
        composeTestRule.waitUntilDoesNotExist(hasTestTag("Loading"))
        navigateToDailyForecastScreen()
        composeTestRule.waitUntilDoesNotExist(hasTestTag("Loading"))
        navigateToHourlyForecastScreen()
        performNavigateUp()
        navController.assertCurrentRouteName(DailyForecast.routeWithArgs)
    }

    /**
     * Verify title is updating correctly
     */
    @Test
    fun weatherNavHost_clickOnPlace_verifyTitleUpdate() {
        composeTestRule.waitUntilDoesNotExist(hasTestTag("Loading"))
        navigateToDailyForecastScreen()
        composeTestRule.waitUntilDoesNotExist(hasTestTag("Loading"))
        composeTestRule.onNodeWithText("Miami").assertExists()
    }

    /**
     * Verify correct title on hourly forecast screen
     */
    @Test
    fun weatherNavHost_clickOnDay_verifyTitleUpdateOnHourlyForecastScreen() {
        composeTestRule.waitUntilDoesNotExist(hasTestTag("Loading"))
        navigateToDailyForecastScreen()
        composeTestRule.waitUntilDoesNotExist(hasTestTag("Loading"))
        navigateToHourlyForecastScreen()
        composeTestRule.waitUntilDoesNotExist(hasTestTag("Loading"))
        composeTestRule.onNodeWithText("Today").assertExists()
    }

    /**
     * Verify navigate to Units screen
     */
    @Test
    fun weatherNavHost_clickOnSettings_verifyNavigateToSettingsScreen() {
        //
        val actionMenuText = composeTestRule.activity.getString(R.string.action_menu)
        composeTestRule.onNodeWithContentDescription(actionMenuText).performClick()
        composeTestRule.onNodeWithText("Units").assertExists()
        composeTestRule.onNodeWithText("Units").performClick()
        navController.assertCurrentRouteName(UnitsMenu.route)
    }

    /**
     * Verify navigate to Notifications screen
     */
    @Test
    fun weatherNavHost_clickOnSettings_verifyNavigateToNotificationsScreen() {
        //
        val actionMenuText = composeTestRule.activity.getString(R.string.action_menu)
        composeTestRule.onNodeWithContentDescription(actionMenuText).performClick()
        composeTestRule.onNodeWithText("Notifications").performClick()
        navController.assertCurrentRouteName(NotificationsMenu.route)
    }

    /**
     * Verify navigate to Interface Settings screen
     */
    @Test
    fun weatherNavHost_clickOnSettings_verifyNavigateToInterfaceSettingsScreen() {
        //
        val actionMenuText = composeTestRule.activity.getString(R.string.action_menu)
        composeTestRule.onNodeWithContentDescription(actionMenuText).performClick()
        composeTestRule.onNodeWithText("Interface").performClick()
        navController.assertCurrentRouteName(InterfaceMenu.route)
    }


    /**
     * Verify app about dialog
     */
    @Test
    fun weatherNavHost_clickOnSettings_verifyAppAboutDialogShows() {
        //
        val actionMenuText = composeTestRule.activity.getString(R.string.action_menu)
        composeTestRule.onNodeWithContentDescription(actionMenuText).performClick()
        composeTestRule.onNodeWithText("About").performClick()
        composeTestRule.waitUntilDoesNotExist(hasTestTag("Loading"))
        composeTestRule.onNodeWithTag(testTag = "About Dialog").assertExists()
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

}

