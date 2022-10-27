package com.brian.weathercompose.ui

import android.content.SharedPreferences
import android.content.res.Resources
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.preference.PreferenceManager
import com.brian.weathercompose.R
import com.brian.weathercompose.ui.screens.AddWeatherFab
import com.brian.weathercompose.ui.screens.AddWeatherScreen
import com.brian.weathercompose.ui.screens.MainWeatherListScreen
import com.brian.weathercompose.ui.viewmodels.WeatherListViewModel

/**
 * Enum class that holds all the screens
 */
enum class WeatherScreen {
    WeatherList,
    AddLocation,
    DailyForecast,
    HourlyForecast,
    SettingsMenu
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
    currentScreen: WeatherScreen,
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
    val currentScreen = WeatherScreen.valueOf(
        backStackEntry?.destination?.route ?: WeatherScreen.WeatherList.name
    )

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
                        WeatherScreen.SettingsMenu.name
                    )
                }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = WeatherScreen.WeatherList.name,
            modifier = modifier.padding(innerPadding)
        ) {
            composable(route = WeatherScreen.WeatherList.name) {
                Scaffold(
                    floatingActionButton = {
                        AddWeatherFab(onClick = {
                            navController.navigate(WeatherScreen.AddLocation.name)
                          //  weatherListViewModel.getForecast(pref, context.resources)
                        }
                        )
                    }
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(it),
                        color = MaterialTheme.colors.background
                    ) {
                        MainWeatherListScreen(
                            weatherUiState = weatherListViewModel.weatherUiState,
                            //  retryAction = weatherListViewModel::getForecast,
                            retryAction = {},
                            modifier = modifier,
                            onClick = {
                                navController.navigate(
                                    WeatherScreen.DailyForecast.name
                                )
                            }
                        )
                    }
                }
            }

            composable(route = WeatherScreen.AddLocation.name) {
                AddWeatherScreen(
                    value = "",
                    onValueChange = { },
                    navAction = { navController.popBackStack() }
                )
            }

            composable(route = WeatherScreen.DailyForecast.name) {

            }

            composable(route = WeatherScreen.HourlyForecast.name) {

            }

            composable(route = WeatherScreen.SettingsMenu.name) {

            }

        }

    }
}
