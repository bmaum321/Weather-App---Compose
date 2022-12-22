package com.brian.weather.util.workers

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.work.*
import com.brian.weather.data.settings.AppPreferences
import com.brian.weather.util.Constants
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Daily worker for precipitation notifications
 */
//TODO need to enque a new worker if preference is changed

// Only execute and schedule next job if show notifications is checked in preferences
class JobScheduler(private val preferences: AppPreferences) {
    fun schedulePrecipitationJob(
        context: Context) {

        if (preferences.showNotifications && preferences.showPrecipitationNotifications) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val currentDate = Calendar.getInstance()
            val dueDate = Calendar.getInstance()
            val data = Data.Builder()
            data.putStringArray("locations", preferences.precipitationLocations.toTypedArray())
            data.putString("clockFormat", preferences.clockFormat)
            data.putString("dateFormat", preferences.dateFormat)
            // Set Execution around 06:00:00 AM
            dueDate.set(Calendar.HOUR_OF_DAY, 7)
            dueDate.set(Calendar.MINUTE, 0)
            dueDate.set(Calendar.SECOND, 0)
            if (dueDate.before(currentDate)) {
                dueDate.add(Calendar.HOUR_OF_DAY, 24)
            }
            val timeDiff = dueDate.timeInMillis - currentDate.timeInMillis
            val precipitationRequest = PeriodicWorkRequest.Builder(
                /* workerClass = */ DailyPrecipitationWorker::class.java,
                /* repeatInterval = */ 12,
                /* repeatIntervalTimeUnit = */ TimeUnit.HOURS
            )
                .setConstraints(constraints)
                .setInitialDelay(timeDiff, TimeUnit.MILLISECONDS)
                .addTag(Constants.TAG_OUTPUT)
                .setInputData(data.build())
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "dailyApiCall",
                ExistingPeriodicWorkPolicy.REPLACE,
                precipitationRequest
            )
        }
    }

    fun scheduleForecastJob(context: Context){
         val fusedLocationClient: FusedLocationProviderClient

        if (preferences.showNotifications && preferences.showLocalForecast)
        {

            /**
             * Daily worker for local weather forecast notifications
             */
            //TODO there is a bug here in API 30, cant access location API
                fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                   // MainActivity().permissionLauncher.launch(MainActivity().permissions) //Does this create a new instance of main activity?
                    return
                }
                // Get phones location coordinates and pass to the worker as input data
                fusedLocationClient.lastLocation.addOnCompleteListener { task ->
                    val location = task.result
                    val data = Data.Builder()
                    data.putDoubleArray(
                        "location",
                        doubleArrayOf(location?.latitude ?: 0.0, location?.longitude ?: 0.0)
                    )
                    data.putString("clockFormat", preferences.clockFormat)
                    data.putString("dateFormat", preferences.dateFormat)
                    data.putString("tempUnit", preferences.tempUnit)
                    // Set Execution around 06:00:00 AM
                    val forecastDueDate = Calendar.getInstance()
                    val currentDate = Calendar.getInstance()
                    val constraints = Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                    forecastDueDate.set(Calendar.HOUR_OF_DAY, 7)
                    forecastDueDate.set(Calendar.MINUTE, 0)
                    forecastDueDate.set(Calendar.SECOND, 0)
                    if (forecastDueDate.before(currentDate)) {
                        forecastDueDate.add(Calendar.HOUR_OF_DAY, 24)
                    }
                    val timeDiffForecast = forecastDueDate.timeInMillis - currentDate.timeInMillis
                    val forecastRequest = PeriodicWorkRequest.Builder(
                        /* workerClass = */ DailyLocalWeatherWorker::class.java,
                        /* repeatInterval = */ 12,
                        /* repeatIntervalTimeUnit = */ TimeUnit.HOURS
                    )
                        .setConstraints(constraints)
                        .setInitialDelay(timeDiffForecast, TimeUnit.MILLISECONDS)
                        .addTag(Constants.TAG_OUTPUT)
                        .setInputData(data.build())
                        .build()
                    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                        "dailyForecast",
                        ExistingPeriodicWorkPolicy.REPLACE,
                        forecastRequest
                    )
                }

        }
    }

}

