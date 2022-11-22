package com.brian.weathercompose.presentation

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.preference.PreferenceManager
import com.brian.weathercompose.R
import com.brian.weathercompose.presentation.navigation.*
import com.brian.weathercompose.presentation.screens.*
import com.brian.weathercompose.presentation.viewmodels.MainViewModel
import com.brian.weathercompose.presentation.viewmodels.WeatherListViewModel

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
    currentScreen: NavDestinations,
    actionBarOnClick: () -> Unit,
    title: String
) {
    TopAppBar(
        title = { Text(text = title, fontSize = 22.sp, fontWeight = FontWeight.Bold) },
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
    mainViewModel: MainViewModel,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    // Create nav controller
   // val navController = rememberNavController()

    // Get current back stack entry
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentScreen = screens.find { it.route == backStackEntry?.destination?.route } ?: MainWeatherList

    // Get the app bar title from the main view model
    val title by mainViewModel.title.collectAsState()

    Scaffold(
        topBar = {
            WeatherAppBar(
                canNavigateBack = navController.previousBackStackEntry != null,
                navigateUp = { navController.navigateUp() },
                currentScreen = currentScreen,
                actionBarOnClick = {
                    navController.navigate(
                       SettingsMenu.route
                    )
                },
                title = title
            )
        }
    ) { innerPadding ->

        val context = LocalContext.current
        val pref = PreferenceManager.getDefaultSharedPreferences(context)
        NavHost(
            navController = navController,
            startDestination = MainWeatherList.route,
            modifier = modifier
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
                        onClick = { location -> navController.navigateToDailyForecast(location) },
                        addWeatherFabAction = { navController.navigate(AddLocation.route) },
                        weatherListViewModel = weatherListViewModel,
                        mainViewModel = mainViewModel
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
                    val location = navBackStackEntry.arguments?.getString(MainWeatherList.locationArg)
                    if (location != null) {
                        DailyForecastScreen(
                            modifier = modifier,
                            onClick = { date -> navController.navigateToHourlyForecast(location, date) },
                            location = location,
                            mainViewModel = mainViewModel,
                            alertFabOnClick = { navController.navigateToAlertsScreen(location) }
                        )
                    }
                }
            }

            composable(route = HourlyForecast.routeWithArgs) { navBackStackEntry ->
                val location = navBackStackEntry.arguments?.getString(MainWeatherList.locationArg)
                val date = navBackStackEntry.arguments?.getString(MainWeatherList.dateArg)
                if (date != null && location != null) {
                    HourlyForecastScreen(
                        modifier = modifier,
                        date = date,
                        location = location,
                        mainViewModel = mainViewModel
                    )
                }

            }

            composable(route = SettingsMenu.route) {

            }

            composable(route = Alerts.routeWithArgs) { navBackStackEntry ->
                val location = navBackStackEntry.arguments?.getString(MainWeatherList.locationArg)
                if (location != null) {
                    AlertsScreen(mainViewModel = mainViewModel, location = location)
                }
            }

        }
    }
}

fun NavHostController.navigateToDailyForecast(location: String) {
    this.navigate("${DailyForecast.route}/$location")
}

fun NavHostController.navigateToHourlyForecast(location: String, date:String) {
    this.navigate("${HourlyForecast.route}/$location/$date")
}

fun NavHostController.navigateToAlertsScreen(location: String) {
    this.navigate("${Alerts.route}/$location")
}







