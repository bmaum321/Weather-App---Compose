package com.brian.weathercompose.presentation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.preference.PreferenceManager
import com.brian.weathercompose.BuildConfig
import com.brian.weathercompose.R
import com.brian.weathercompose.presentation.navigation.*
import com.brian.weathercompose.presentation.screens.*
import com.brian.weathercompose.presentation.screens.settings.InterfaceSettingsScreen
import com.brian.weathercompose.presentation.screens.settings.NotificationSettingsScreen
import com.brian.weathercompose.presentation.screens.settings.UnitSettingsScreen
import com.brian.weathercompose.presentation.viewmodels.MainViewModel
import com.brian.weathercompose.presentation.viewmodels.WeatherListViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
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
@Composable
fun WeatherAppBar(
    canNavigateBack: Boolean,
    navigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    currentScreen: NavDestinations,
    actionBarOnClick: () -> Unit,
    title: String,
    menuOnClick: () -> Unit
) {
    TopAppBar(
        title = { Text(text = title, fontSize = 22.sp, fontWeight = FontWeight.Bold) },
        modifier = modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colors.primary),
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

    val ctx = LocalContext.current
    // Get current back stack entry
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentScreen =
        screens.find { it.route == backStackEntry?.destination?.route } ?: MainWeatherList

    val locationsInDatabase = weatherListViewModel.getZipCodesFromDatabase().collectAsState(initial = "")

    // Get the app bar title from the main view model
    val title by mainViewModel.title.collectAsState()
    val scaffoldState = rememberScaffoldState()
    val coroutineScope = rememberCoroutineScope()
    val openAboutDialog = remember { mutableStateOf(false) }
    val openTemperatureUnitDialog = remember { mutableStateOf(false) }
    val openClockFormatDialog = remember { mutableStateOf(false) }
    val openWindspeedDialog = remember { mutableStateOf(false) }
    val openMeasurementDialog = remember { mutableStateOf(false) }

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
            confirmButton = {},
            shape = RoundedCornerShape(size = 4.dp)
        )
    }

    Scaffold(
        scaffoldState = scaffoldState,
        drawerGesturesEnabled = scaffoldState.drawerState.isOpen,
        drawerElevation = 4.dp,
        drawerContent = {
            DrawerContent { itemLabel ->
                when (itemLabel) {
                    ctx.getString(R.string.about) -> {
                        openAboutDialog.value = true
                    }
                    ctx.getString(R.string.units) -> {
                        coroutineScope.launch {
                            delay(250)
                            scaffoldState.drawerState.close()
                        }
                        navController.navigate(UnitsMenu.route)
                    }
                    "Interface" -> {
                        coroutineScope.launch {
                            delay(250)
                            scaffoldState.drawerState.close()
                        }
                        navController.navigate(InterfaceMenu.route)
                    }
                    "Notifications" -> {
                        coroutineScope.launch {
                            delay(250)
                            scaffoldState.drawerState.close()
                        }
                        navController.navigate(NotificationsMenu.route)
                    }
                }
            }
        },
        topBar = {
            WeatherAppBar(
                canNavigateBack = navController.previousBackStackEntry != null,
                navigateUp = { navController.navigateUp() },
                currentScreen = currentScreen,
                actionBarOnClick = {
                    navController.navigate(
                        UnitsMenu.route
                    )
                },
                title = title,
                menuOnClick = {
                    coroutineScope.launch {
                        scaffoldState.drawerState.open()
                    }
                }
            )
        }
    ) { innerPadding ->

        val context = LocalContext.current
        NavHost(
            navController = navController,
            startDestination = MainWeatherList.route,
            modifier = modifier
        ) {
            composable(
                route = MainWeatherList.route,
                arguments = MainWeatherList.arguments
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    val weatherUiState by remember {
                        weatherListViewModel.getAllWeather(context.resources
                        )
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

            composable(
                route = DailyForecast.routeWithArgs,
            ) { navBackStackEntry ->
                Surface(
                    modifier = Modifier
                        .fillMaxSize(),
                    color = MaterialTheme.colors.background
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

            composable(route = UnitsMenu.route) {
                UnitSettingsScreen(
                    onDismissRequest = {openTemperatureUnitDialog.value = false},
                    openTemperatureDialog = openTemperatureUnitDialog,
                    openClockFormatDialog = openClockFormatDialog,
                    openMeasurementDialog = openMeasurementDialog,
                    openWindspeedDialog = openWindspeedDialog,
                    viewModel = mainViewModel,
                    coroutineScope = coroutineScope,
                    preferencesRepository = get(),
                    itemClick = { itemlabel ->
                        when(itemlabel) {
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
                val location = navBackStackEntry.arguments?.getString(MainWeatherList.locationArg)
                if (location != null) {
                    AlertsScreen(mainViewModel = mainViewModel, location = location)
                }
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











