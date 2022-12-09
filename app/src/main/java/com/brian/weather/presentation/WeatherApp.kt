package com.brian.weather.presentation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.brian.weather.BuildConfig
import com.brian.weather.R
import com.brian.weather.presentation.navigation.*
import com.brian.weather.presentation.screens.*
import com.brian.weather.presentation.reusablecomposables.CustomAlertDialog
import com.brian.weather.presentation.reusablecomposables.OverflowMenu
import com.brian.weather.presentation.reusablecomposables.DeleteDropDownMenuItem
import com.brian.weather.presentation.reusablecomposables.EditDropDownMenuItem
import com.brian.weather.presentation.screens.settings.InterfaceSettingsScreen
import com.brian.weather.presentation.screens.settings.NotificationSettingsScreen
import com.brian.weather.presentation.screens.settings.UnitSettingsScreen
import com.brian.weather.presentation.viewmodels.MainViewModel
import com.brian.weather.presentation.viewmodels.WeatherListViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.compose.get

sealed class MenuAction(
    @StringRes val label: Int,
    @DrawableRes val icon: Int
) {

    object Share : MenuAction(R.string.action_menu, R.drawable.ic_baseline_more_vert_24)
}

/**
 * Composable that displays the topBar and displays back button if back navigation is possible.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherAppBar(
    canNavigateBack: Boolean,
    navigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    currentScreen: String,
    deleteOnClick: () -> Unit,
    editOnClick: () -> Unit,
    title: String,
    menuOnClick: () -> Unit

) {
    TopAppBar(
        colors = TopAppBarDefaults.smallTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
        actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
        titleContentColor = MaterialTheme.colorScheme.onPrimary
        ),
        title = { Text(text = title, fontSize = 22.sp, fontWeight = FontWeight.Bold) },
        modifier = modifier
            .fillMaxWidth(),
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back_button)
                    )
                }
            } else {
                IconButton(onClick = menuOnClick) {
                    Icon(
                        imageVector = Icons.Filled.Menu,
                        contentDescription = stringResource(R.string.action_menu)
                    )
                }
            }
        },
        actions = {
            if (currentScreen == DailyForecast.routeWithArgs) {
                OverflowMenu {
                    DeleteDropDownMenuItem(onClick = deleteOnClick)
                   // EditDropDownMenuItem(onClick = editOnClick)
                }
            }
        }
    )
}

/**
 * Main entry point composable for app
 */


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherApp(
    weatherListViewModel: WeatherListViewModel,
    mainViewModel: MainViewModel,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {

    val preferences = weatherListViewModel.allPreferences.collectAsState()
    val ctx = LocalContext.current
    // Get current back stack entry
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentScreen = backStackEntry?.destination?.route
    val locationsInDatabase =
        weatherListViewModel.getZipCodesFromDatabase().collectAsState(initial = "")

    // Get the app bar title from the main view model
    val title by mainViewModel.title.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    // TODO these dialog states should live in the screens themeselves? otherwise this seems to be like a lot states to track here
    val openAboutDialog = remember { mutableStateOf(false) }
    val openTemperatureUnitDialog = remember { mutableStateOf(false) }
    val openClockFormatDialog = remember { mutableStateOf(false) }
    val openWindspeedDialog = remember { mutableStateOf(false) }
    val openMeasurementDialog = remember { mutableStateOf(false) }
    val openLocationOverflowMenu = remember { mutableStateOf(false) }


    if (openAboutDialog.value) {
        AlertDialog(
            // This test tag is used for semantics matching in UI testing
            modifier = Modifier.semantics { testTag = "About Dialog" },
            title = {
                Text(
                    text = "Version: ${BuildConfig.VERSION_NAME} \n",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = { Text(text = "Thanks for trying my app! \n bmaum1@gmail.com") },
            onDismissRequest = { openAboutDialog.value = false },
            dismissButton = {},
            confirmButton = {}
        )
    }

    if (openLocationOverflowMenu.value) {
        val location =
            backStackEntry?.arguments?.getString(MainWeatherList.locationArg)
        CustomAlertDialog(
            tag = "Location Delete Dialog",
            title = "Delete $location?",
            text = stringResource(R.string.confirm_deletion),
            onDismissRequest = { openLocationOverflowMenu.value = false },
            dismissButtonOnClick = { openLocationOverflowMenu.value = false },
            confirmButtonOnClick = {
                coroutineScope.launch {
                    withContext(Dispatchers.IO) {
                        val weatherEntity =
                            location?.let { weatherListViewModel.getWeatherByZipcode(it) }
                        if (weatherEntity != null) {
                            weatherListViewModel.deleteWeather(weatherEntity)
                        }

                        //TODO the logic here needs some work, the list getting passed to the worker is not updated
                        // When a location is deleted from the database
                        weatherListViewModel.updatePrecipitationLocations(weatherListViewModel.allPreferences.value?.precipitationLocations
                            ?: emptySet()
                        )
                        withContext(Dispatchers.Main) {
                            navController.popBackStack()
                            openLocationOverflowMenu.value = false
                        }

                    }
                }
            },
            confirmText = stringResource(R.string.ok)

        )
    }

    Scaffold(
        topBar = {
            WeatherAppBar(
                canNavigateBack = navController.previousBackStackEntry != null,
                navigateUp = {
                    navController.navigateUp()
                },
                currentScreen = currentScreen ?: MainWeatherList.route,
                deleteOnClick = { openLocationOverflowMenu.value = true },
                title = title,
                menuOnClick = {
                    navController.navigate(SettingsMenu.route)
                },
                editOnClick = { navController.navigateToEditScreen(backStackEntry?.arguments?.getString(MainWeatherList.locationArg) ?: "") }
            )
        }
    ) { innerPadding ->

        val context = LocalContext.current
        NavHost(
            navController = navController,
            startDestination = MainWeatherList.route,
            modifier = modifier.padding(innerPadding)
        ) {
            composable(
                route = MainWeatherList.route,
                arguments = MainWeatherList.arguments
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val weatherUiState by remember {
                        weatherListViewModel.getAllWeather(
                            context.resources
                        )
                    }.collectAsState()

                    MainWeatherListScreen(
                        weatherUiState = weatherUiState,
                        retryAction = { weatherListViewModel.refresh() },
                        modifier = modifier,
                        onClick = { location -> navController.navigateToDailyForecast(location) },
                        addWeatherFabAction = { navController.navigate(AddLocation.routeWithArgs) },
                        weatherListViewModel = weatherListViewModel,
                        mainViewModel = mainViewModel
                    )
                }
            }

            composable(route = AddLocation.routeWithArgs) {
                val location = it.arguments?.getString("location")
                AddWeatherScreen(
                    value = location ?: "",
                    onValueChange = { },
                    navAction = { navController.popBackStack() }
                )
            }

            composable(
                route = DailyForecast.routeWithArgs,
            ) { navBackStackEntry ->
                Surface(
                    modifier = Modifier
                        .fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val location =
                        navBackStackEntry.arguments?.getString(MainWeatherList.locationArg)
                    if (location != null) {
                        DailyForecastScreen(
                            modifier = modifier,
                            onClick = { date ->
                                navController.navigateToHourlyForecast(
                                    location,
                                    date
                                )
                            },
                            location = location,
                            mainViewModel = mainViewModel,
                            alertFabOnClick = { navController.navigateToAlertsScreen(location) },

                            )
                    }
                }
            }

            composable(route = HourlyForecast.routeWithArgs) { navBackStackEntry ->
                val location =
                    navBackStackEntry.arguments?.getString(MainWeatherList.locationArg)
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

            composable(route = UnitsMenu.route) {
                UnitSettingsScreen(
                    onDismissRequest = { openTemperatureUnitDialog.value = false },
                    openTemperatureDialog = openTemperatureUnitDialog,
                    openClockFormatDialog = openClockFormatDialog,
                    openMeasurementDialog = openMeasurementDialog,
                    openWindspeedDialog = openWindspeedDialog,
                    viewModel = mainViewModel,
                    coroutineScope = coroutineScope,
                    preferencesRepository = get(),
                    itemClick = { itemlabel ->
                        when (itemlabel) {
                            "Temperature" -> {
                                openTemperatureUnitDialog.value = true
                            }
                            "Pressure" -> {
                                openMeasurementDialog.value = true
                            }
                            "Wind" -> {
                                openWindspeedDialog.value = true
                            }
                            "Clock Format" -> {
                                openClockFormatDialog.value = true
                            }
                        }
                    })
            }

            composable(route = InterfaceMenu.route) {
                InterfaceSettingsScreen(
                    viewModel = mainViewModel,
                    coroutineScope = coroutineScope,
                    preferencesRepository = get()
                )
            }

            composable(route = NotificationsMenu.route) {
                NotificationSettingsScreen(
                    viewModel = mainViewModel,
                    coroutineScope = coroutineScope,
                    preferencesRepository = get(),
                    locations = locationsInDatabase.value as List<String>
                )
            }

            composable(route = Alerts.routeWithArgs) { navBackStackEntry ->
                val location =
                    navBackStackEntry.arguments?.getString(MainWeatherList.locationArg)
                if (location != null) {
                    AlertsScreen(mainViewModel = mainViewModel, location = location)
                }
            }

            composable(route = SettingsMenu.route) {
                SettingsMenu(viewmodel = mainViewModel,
                itemClick = { itemLabel ->
                    when (itemLabel) {
                        ctx.getString(R.string.about) -> {
                            openAboutDialog.value = true
                        }
                        ctx.getString(R.string.units) -> {
                            navController.navigate(UnitsMenu.route)
                        }
                        "Interface" -> {
                            navController.navigate(InterfaceMenu.route)
                        }
                        "Notifications" -> {
                            navController.navigate(NotificationsMenu.route)
                        }
                    }
                }
                )
            }
        }
    }


}

private fun NavHostController.navigateToDailyForecast(location: String) {
    this.navigate("${DailyForecast.route}/$location")
}

private fun NavHostController.navigateToHourlyForecast(location: String, date: String) {
    this.navigate("${HourlyForecast.route}/$location/$date")
}

private fun NavHostController.navigateToAlertsScreen(location: String) {
    this.navigate("${Alerts.route}/$location")
}

private fun NavHostController.navigateToEditScreen(location: String) {
    this.navigate("${AddLocation.route}/$location")
}











