package com.brian.weathercompose.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.preference.PreferenceManager
import com.brian.weathercompose.R
import com.brian.weathercompose.ui.screens.*
import com.brian.weathercompose.ui.viewmodels.WeatherListViewModel


/**
 * Sealed class that holds all the screens
 */

sealed class Screens(val route: String){
    object WeatherList: Screens("weatherList")
    object AddLocation: Screens("addLocation")
    object DailyForecast: Screens("dailyForecast/{location}")
    object HourlyForecast: Screens("hourlyForecast/{location}/{date}")
    object SettingsMenu: Screens("settingsMenu")
}



sealed class MenuAction(
    @StringRes val label: Int,
    @DrawableRes val icon: Int
) {

    object Share : MenuAction(R.string.action_menu, R.drawable.ic_baseline_more_vert_24)
}

/**
 * Composable that displays the topBar and displays back button if back navigation is possible.
 */
@Composable
fun WeatherAppBar(
    canNavigateBack: Boolean,
    navigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    currentScreen: Screens,
    actionBarOnClick: () -> Unit
) {
    TopAppBar(
        title = { Text(stringResource(id = R.string.app_name)) },
        modifier = modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colors.primary),

        actions = {
            IconButton(onClick = actionBarOnClick) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_baseline_more_vert_24),
                    contentDescription = stringResource(R.string.action_menu)
                )
            }
        },
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back_button)
                    )
                }
            }
        },

        )
}

/**
 * Main entry point composable for app
 */


@Composable
fun WeatherApp(
    weatherListViewModel: WeatherListViewModel,
    modifier: Modifier = Modifier
) {
    // Create nav controller
    val navController = rememberNavController()

    // Get current back stack entry
    val backStackEntry by navController.currentBackStackEntryAsState()

    val screens = listOf(Screens.WeatherList, Screens.AddLocation, Screens.DailyForecast, Screens.HourlyForecast, Screens.SettingsMenu)
    val currentScreen = screens.find { it.route == backStackEntry?.destination?.route } ?: Screens.WeatherList

    val context = LocalContext.current
    val pref = PreferenceManager.getDefaultSharedPreferences(context)

    Scaffold(
        topBar = {
            WeatherAppBar(
                canNavigateBack = navController.previousBackStackEntry != null,
                navigateUp = { navController.navigateUp() },
                currentScreen = currentScreen,
                actionBarOnClick = {
                    navController.navigate(
                        Screens.SettingsMenu.route
                    )
                }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = MainWeatherList.route,
            modifier = modifier.padding(innerPadding)
        ) {
            composable(route = MainWeatherList.route,
                arguments = MainWeatherList.arguments) {
                    Surface(
                        modifier = Modifier
                            .fillMaxSize(),
                        color = MaterialTheme.colors.background
                    ) {
                        val weatherUiState by remember {
                            weatherListViewModel.getAllWeather(pref, resources = context.resources)
                        }.collectAsState()

                        MainWeatherListScreen(
                            weatherUiState = weatherUiState,
                            retryAction = { weatherListViewModel.refresh() },
                            modifier = modifier,
                            onClick = { location -> navController.navigateToDailyForecast(location) }, //TODO figure this shit out
                            addWeatherFabAction = { navController.navigate(AddLocation.route) },
                            weatherListViewModel = weatherListViewModel
                        )
                    }
            }

            composable(route = AddLocation.route) {
                AddWeatherScreen(
                    value = "",
                    onValueChange = { },
                    navAction = { navController.popBackStack() }
                )
            }

            composable(route = DailyForecast.routeWithArgs,
                ) { navBackStackEntry ->
                Surface(
                    modifier = Modifier
                        .fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    val location = navBackStackEntry.arguments?.getString("location")
                    if (location != null) {
                        DailyForecastScreen(
                            modifier = modifier,
                            onClick = { date -> navController.navigateToHourlyForecast(location, date) },
                            location = location
                        )
                    }
                }
            }

            composable(route = HourlyForecast.routeWithArgs) { navBackStackEntry ->
                val location = navBackStackEntry.arguments?.getString("location")
                val date = navBackStackEntry.arguments?.getString("date")
                if (date != null && location != null) {
                    HourlyForecastScreen(
                        modifier = modifier,
                        date = date,
                        location = location
                    )
                }

            }

            composable(route = SettingsMenu.route) {

            }

        }

    }
}

private fun NavHostController.navigateToDailyForecast(location: String) {
    this.navigate("${DailyForecast.route}/$location")
}

private fun NavHostController.navigateToHourlyForecast(location: String, date:String) {
    this.navigate("${HourlyForecast.route}/$location/$date")
}

