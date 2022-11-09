package com.brian.weathercompose.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.preference.PreferenceManager
import com.brian.weathercompose.ui.screens.AddWeatherScreen
import com.brian.weathercompose.ui.screens.DailyForecastScreen
import com.brian.weathercompose.ui.screens.HourlyForecastScreen
import com.brian.weathercompose.ui.screens.MainWeatherListScreen
import com.brian.weathercompose.ui.viewmodels.MainViewModel
import com.brian.weathercompose.ui.viewmodels.WeatherListViewModel

@Composable
fun WeatherNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    weatherListViewModel: WeatherListViewModel,
    mainViewModel: MainViewModel
) {

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
                        mainViewModel = mainViewModel
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

    }
}

private fun NavHostController.navigateToDailyForecast(location: String) {
    this.navigate("${DailyForecast.route}/$location")
}

private fun NavHostController.navigateToHourlyForecast(location: String, date:String) {
    this.navigate("${HourlyForecast.route}/$location/$date")
}
