package com.brian.weathercompose.ui.screens

import androidx.navigation.NavType
import androidx.navigation.navArgument

interface NavDestinations {
    val route: String
}

object MainWeatherList: NavDestinations {
    override val route = "weatherList"
    val arguments = listOf(
        navArgument(HourlyForecast.locationArg) { type = NavType.StringType },
        navArgument(HourlyForecast.dateArg) { type = NavType.StringType }
    )
}

object AddLocation: NavDestinations {
    override val route = "addLocation"
}

object DailyForecast: NavDestinations {
    override val route = "dailyForecast"
    const val locationArg = "location"
    val routeWithArgs = "${route}/{${locationArg}}"


}

object HourlyForecast: NavDestinations {
    override val route = "hourlyForecast"
    const val locationArg = "location"
    const val dateArg = "date"
    val routeWithArgs = "${route}/{${locationArg}}/{${dateArg}}"
}

object SettingsMenu: NavDestinations {
    override val route = "settingsMenu"

}