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
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.preference.PreferenceManager
import com.brian.weathercompose.R
import com.brian.weathercompose.ui.navigation.*
import com.brian.weathercompose.ui.screens.*
import com.brian.weathercompose.ui.viewmodels.MainViewModel
import com.brian.weathercompose.ui.viewmodels.WeatherListViewModel

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
        title = { Text(text = title) },
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

    val currentScreen = screens.find { it.route == backStackEntry?.destination?.route } ?: MainWeatherList
    val mainViewModel: MainViewModel = viewModel()
    //val topBarTitle = mainViewModel.title.collectAsState()
    val topBarTitle = mainViewModel.title.observeAsState()
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
                title = topBarTitle.value ?: ""
            )
        }
    ) { innerPadding ->
        WeatherNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding),
            weatherListViewModel = weatherListViewModel
        )
    }
}


