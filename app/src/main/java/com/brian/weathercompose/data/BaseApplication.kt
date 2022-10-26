package com.brian.weathercompose.data

import android.app.Application
//import com.google.android.material.color.DynamicColors




/**
 * An application class that inherits from [Application], allows for the creation of a singleton
 * instance of the [WeatherDatabase]
 */
class BaseApplication : Application() {

    // TODO: provide a WeatherDatabase value by lazy here
    val database: WeatherDatabase by lazy { WeatherDatabase.getDatabase(this) }

    override fun onCreate() {
        super.onCreate()
        // Dynamic Coloring for Material 3
       // DynamicColors.applyToActivitiesIfAvailable(this)
    }
}