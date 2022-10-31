package com.brian.weathercompose.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.preference.PreferenceManager
import com.brian.weathercompose.R
import com.brian.weathercompose.ui.screens.AddWeatherScreen
import com.brian.weathercompose.ui.screens.DailyForecastScreen
import com.brian.weathercompose.ui.screens.HourlyForecastScreen
import com.brian.weathercompose.ui.screens.MainWeatherListScreen
import com.brian.weathercompose.ui.viewmodels.WeatherListState
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
  //  currentScreen: Screens,
    actionBarOnClick: () -> Unit
) {
    TopAppBar(
        title = { Text(stringResource(id = R.string.app_name)) },
        modifier = modifier,
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

    // Get the name of the current screen
   // val currentScreen = Screens.valueOf(
  //      backStackEntry?.destination?.route ?: Screens.WeatherList.name
  //  )
    val currentScreen = backStackEntry?.destination?.route ?: Screens.WeatherList.route

    val context = LocalContext.current
    val pref = PreferenceManager.getDefaultSharedPreferences(context)


    // Get weather on compose
    LaunchedEffect(key1 = true) {
        weatherListViewModel.getAllWeather(pref, context.resources).collect{
            when(it) {
                is WeatherListState.Success -> weatherListViewModel.weatherUiState = WeatherListState.Success(it.weatherDomainObjects)
                is WeatherListState.Empty -> weatherListViewModel.weatherUiState = WeatherListState.Empty
                is WeatherListState.Error -> weatherListViewModel.weatherUiState = WeatherListState.Error
                is WeatherListState.Loading -> weatherListViewModel.weatherUiState = WeatherListState.Loading
            }
        }

    }

    Scaffold(
        topBar = {
            WeatherAppBar(
                canNavigateBack = navController.previousBackStackEntry != null,
                navigateUp = { navController.navigateUp() },
               // currentScreen = currentScreen,
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
            startDestination = Screens.WeatherList.route,
            modifier = modifier.padding(innerPadding)
        ) {
            composable(route = Screens.WeatherList.route,
                arguments = listOf(navArgument("location") {type = NavType.StringType},
                navArgument("date") {type = NavType.StringType}
                )) {

                    Surface(
                        modifier = Modifier
                            .fillMaxSize(),
                        color = MaterialTheme.colors.background
                    ) {
                        MainWeatherListScreen(
                            weatherUiState = weatherListViewModel.weatherUiState,
                            retryAction = { weatherListViewModel.refresh() },
                            modifier = modifier,
                            onClick = { location -> navController.navigate("dailyForecast/$location") }, //TODO figure this shit out
                            addWeatherFabAction = { navController.navigate(Screens.AddLocation.route) },
                            weatherListViewModel = weatherListViewModel
                        )
                    }
            }

            composable(route = Screens.AddLocation.route) {
                AddWeatherScreen(
                    value = "",
                    onValueChange = { },
                    navAction = { navController.popBackStack() }
                )
            }

            composable(route = Screens.DailyForecast.route,
                ) {
                Surface(
                    modifier = Modifier
                        .fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    val location = it.arguments?.getString("location")
                    if (location != null) {
                        DailyForecastScreen(
                            modifier = modifier,
                            onClick = { date -> navController.navigate("hourlyForecast/$location/$date") },
                            location = location
                        )
                    }
                }
            }

            composable(route = Screens.HourlyForecast.route) {
                val location = it.arguments?.getString("location")
                val date = it.arguments?.getString("date")
                if (date != null && location != null) {
                    HourlyForecastScreen(
                        modifier = modifier,
                        date = date,
                        location = location
                    )
                }

            }

            composable(route = Screens.SettingsMenu.route) {

            }

        }

    }
}