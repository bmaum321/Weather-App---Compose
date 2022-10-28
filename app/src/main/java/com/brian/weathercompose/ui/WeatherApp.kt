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
import com.brian.weathercompose.ui.screens.ForecastScreen
import com.brian.weathercompose.ui.screens.MainWeatherListScreen
import com.brian.weathercompose.ui.viewmodels.WeatherListState
import com.brian.weathercompose.ui.viewmodels.WeatherListViewModel

/**
 * Enum class that holds all the screens
 */
/*
enum class Screens {
    WeatherList,
    AddLocation,
    DailyForecast,
    HourlyForecast,
    SettingsMenu
}

 */


sealed class Screens(val route: String){
    object WeatherList: Screens("weatherList")
    object AddLocation: Screens("addLocation")
    object DailyForecast: Screens("dailyForecast/{location}")
    object HourlyForecast: Screens("hourlyForecast")
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


//TODO pass all viewmodels here? and pass along the composables? or create viewmodel in the composable itself
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
                arguments = listOf(navArgument("location") {type = NavType.StringType})) {
                val location = it.arguments?.getString("location")
                    Surface(
                        modifier = Modifier
                            .fillMaxSize(),
                        color = MaterialTheme.colors.background
                    ) {
                        MainWeatherListScreen(
                            weatherUiState = weatherListViewModel.weatherUiState,
                            retryAction = { weatherListViewModel.refresh() },
                            modifier = modifier,
                            onClick = { navController.navigate(Screens.DailyForecast.route) },
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

            composable(route = Screens.DailyForecast.route) {
                Surface(
                    modifier = Modifier
                        .fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    ForecastScreen(
                        modifier = modifier,
                        onClick = { navController.navigate(Screens.HourlyForecast.route) },
                    )
                }
            }

            composable(route = Screens.HourlyForecast.route) {

            }

            composable(route = Screens.SettingsMenu.route) {

            }

        }

    }
}