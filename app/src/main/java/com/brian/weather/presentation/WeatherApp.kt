package com.brian.weather.presentation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
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
import com.brian.weather.presentation.reusablecomposables.CustomAlertDialog
import com.brian.weather.presentation.reusablecomposables.DeleteDropDownMenuItem
import com.brian.weather.presentation.reusablecomposables.OverflowMenu
import com.brian.weather.presentation.screens.*
import com.brian.weather.presentation.screens.settings.InterfaceSettingsScreen
import com.brian.weather.presentation.screens.settings.NotificationSettingsScreen
import com.brian.weather.presentation.screens.settings.UnitSettingsScreen
import com.brian.weather.presentation.viewmodels.AddWeatherLocationViewModel
import com.brian.weather.presentation.viewmodels.DailyForecastViewModel
import com.brian.weather.presentation.viewmodels.HourlyForecastViewModel
import com.brian.weather.presentation.viewmodels.MainViewModel
import com.brian.weather.presentation.viewmodels.WeatherListViewModel
import com.brian.weather.repository.WeatherRepository
import kotlinx.coroutines.flow.firstOrNull
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
    menuOnClick: () -> Unit,
    openDeleteMenu: MutableState<Boolean>,
    showMenu: Boolean,
    setShowMenu: (Boolean) -> Unit

) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
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

           // val (showMenu, setShowMenu) = remember { mutableStateOf(false) }
            if (currentScreen == DailyForecast.routeWithArgs) {
                OverflowMenu(
                    setShowMenu = setShowMenu,
                    showMenu = showMenu
                ) {
                    DeleteDropDownMenuItem(
                        onClick = {
                            setShowMenu(false)
                            openDeleteMenu.value = true
                        }
                    )
                    // EditDropDownMenuItem(onClick = editOnClick)
                }
            }
        }
    )
}

/**
 * Main entry point composable for app
 */

/**
 * I could pass all my viewmodels here, this would allow me to inject fake repositories for testing
 * to all screens
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun WeatherApp(
    weatherListViewModel: WeatherListViewModel,
    dailyForecastViewModel: DailyForecastViewModel,
    hourlyForecastViewModel: HourlyForecastViewModel,
    addWeatherLocationViewModel: AddWeatherLocationViewModel,
    mainViewModel: MainViewModel,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    weatherRepository: WeatherRepository
) {

    //We remember the state to show snackbars across compositions
    val snackbarHostState = remember { SnackbarHostState() }
    val ctx = LocalContext.current
    // Get current back stack entry
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentScreen = backStackEntry?.destination?.route


    // Get the app bar title from the main view model
    val title by mainViewModel.title.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    /**
     * Was collecting as state before, but now this method returns a list instead of a flow. Is there
     * a better way to do this?
     */

    val locationsInDatabase = weatherRepository.getZipcodesFromDatabaseAsFlow().collectAsState(
        initial = ""
    )
    

    /**
     * These dialog states should live in the screens themeselves?
     * otherwise this seems to be like a lot states to track here.
     * Initial goal was to hoist as many states as possible to this location to keep sub
     * composables stateless
      */

    val openAboutDialog = remember { mutableStateOf(false) }
    val openTemperatureUnitDialog = remember { mutableStateOf(false) }
    val openDateUnitDialog = remember { mutableStateOf(false) }
    val openClockFormatDialog = remember { mutableStateOf(false) }
    val openWindspeedDialog = remember { mutableStateOf(false) }
    val openMeasurementDialog = remember { mutableStateOf(false) }
    val openLocationOverflowMenu = remember { mutableStateOf(false) }
    /**
     * I haven't seen this declaration used before, but it seems like the second value
     * is acting as a setter function for the remembered value
     */
    val (showMenu, setShowMenu) = remember { mutableStateOf(false) }

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
                /**
                 * We don't need to run this coroutine on a different thread because the database
                 * is returning the object via Flow. Flow return type always run on the Room
                 * executors, so they are always main-safe.
                 */
                coroutineScope.launch {
                        val weatherEntity =
                            location?.let { weatherRepository.getWeatherByZipcode(it).firstOrNull() }
                        if (weatherEntity != null) {
                            weatherRepository.deleteWeather(weatherEntity)
                        }

                        //TODO the logic here needs some work, the list getting passed to the worker is not updated
                        // When a location is deleted from the database
                        weatherListViewModel.updatePrecipitationLocations(
                            weatherListViewModel.allPreferences.value?.precipitationLocations
                                ?: emptySet()
                        )

                      //  withContext(Dispatchers.Main) {
                            navController.popBackStack()
                            openLocationOverflowMenu.value = false

                            /**
                             * Show snackbar to allow undo action of deleting location from database
                             */
                            val snackbarResult = snackbarHostState.showSnackbar(
                                message = "${weatherEntity?.cityName} deleted",
                                duration = SnackbarDuration.Short,
                                withDismissAction = true,
                                actionLabel = "Undo"
                            )

                            when(snackbarResult) {
                                SnackbarResult.ActionPerformed -> {
                                    weatherEntity?.let {
                                        weatherRepository.insert(weatherEntity)
                                        weatherListViewModel.refresh()
                                    }
                                }
                                SnackbarResult.Dismissed -> {}
                            }

                      //  }
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
                deleteOnClick = {

                    openLocationOverflowMenu.value = true
                },
                title = title,
                menuOnClick = {
                    navController.navigate(SettingsMenu.route)
                },
                editOnClick = {
                    navController.navigateToEditScreen(
                        backStackEntry?.arguments?.getString(
                            MainWeatherList.locationArg
                        ) ?: ""
                    )
                },
                openDeleteMenu = openLocationOverflowMenu,
                showMenu = showMenu,
                setShowMenu = setShowMenu
            )
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.animateContentSize().semantics { testTag = "Undo Snackbar" },
            ) { data ->
                Snackbar(
                    snackbarData = data,
                    modifier = Modifier,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    containerColor = MaterialTheme.colorScheme.primary,
                    actionColor = MaterialTheme.colorScheme.onError,
                )
            }
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
                    val weatherUiState by remember { weatherListViewModel.getAllWeather() }
                        .collectAsState()

                    MainWeatherListScreen(
                        weatherUiState = weatherUiState,
                        retryAction = { weatherListViewModel.refresh() },
                        modifier = modifier,
                        onClick = { location -> navController.navigateToDailyForecast(location) },
                        addWeatherFabAction = { navController.navigate(AddLocation.route) },
                        weatherListViewModel = weatherListViewModel,
                        mainViewModel = mainViewModel,
                        weatherRepository = weatherRepository
                    )
                }
            }

            composable(route = AddLocation.route) {
                val location = it.arguments?.getString("location")
                AddWeatherScreen(
                    value = location ?: "",
                    onValueChange = { },
                    navAction = { navController.popBackStack() },
                    addWeatherLocationViewModel = addWeatherLocationViewModel
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
                    location?.let {
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
                            dailyForecastViewModel = dailyForecastViewModel,
                            weatherRepository = weatherRepository
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
                        mainViewModel = mainViewModel,
                        hourlyForecastViewModel = hourlyForecastViewModel
                    )
                }

            }

            /**
             * Not sure if better to create states above and pass to the screen composable,
             * or to create the states in the screen composable itself
             */
            composable(route = UnitsMenu.route) {
                UnitSettingsScreen(
                    openDateFormatDialog = openDateUnitDialog,
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
                            "Precipitation / Pressure" -> {
                                openMeasurementDialog.value = true
                            }
                            "Wind" -> {
                                openWindspeedDialog.value = true
                            }
                            "Clock Format" -> {
                                openClockFormatDialog.value = true
                            }
                            "Date Format" -> {
                                openDateUnitDialog.value = true
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
                location?.let {
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












