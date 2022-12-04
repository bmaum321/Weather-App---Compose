package com.brian.weathercompose.presentation.navigation

import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.brian.weathercompose.presentation.navigation.MainWeatherList.dateArg
import com.brian.weathercompose.presentation.navigation.MainWeatherList.locationArg


/**
 * Contract for information needed on every navigation destination
 */
interface NavDestinations {
    val route: String
}

object MainWeatherList: NavDestinations {
    override val route = "weatherList"
    const val locationArg = "location"
    const val dateArg = "date"
    val arguments = listOf(
        navArgument(locationArg) { type = NavType.StringType },
        navArgument(dateArg) { type = NavType.StringType }
    )
}

object AddLocation: NavDestinations {
    override val route = "addLocation"
}

object DailyForecast: NavDestinations {
    override val route = "dailyForecast"
    val routeWithArgs = "$route/{${locationArg}}"
}

object HourlyForecast: NavDestinations {
    override val route = "hourlyForecast"
    val routeWithArgs = "$route/{${locationArg}}/{${dateArg}}"
}

object UnitsMenu: NavDestinations {
    override val route = "unitsMenu"
}

object InterfaceMenu: NavDestinations {
    override val route = "InterfaceMenu"
}

object Alerts: NavDestinations {
    override val route = "alerts"
    val routeWithArgs = "$route/{${locationArg}}"
}

object NotificationsMenu: NavDestinations {
    override val route = "NotificationsMenu"
}

object SettingsMenu: NavDestinations {
    override val route: String = "SettingsMenu"
}

val screens = listOf(
    MainWeatherList.route,
    DailyForecast.route,
    HourlyForecast.route,
    AddLocation.route,
    UnitsMenu.route,
    InterfaceMenu.route,
    Alerts.route,
    NotificationsMenu.route
)
