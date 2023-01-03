package com.brian.weather.util.workers

import android.Manifest
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.work.*
import com.brian.weather.data.mapper.asDomainModel
import com.brian.weather.data.remote.NetworkResult
import com.brian.weather.util.sendForecastNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.brian.weather.R
import com.brian.weather.data.local.WeatherDatabase
import com.brian.weather.data.remote.WeatherApi
import com.brian.weather.data.remote.onSuccess
import com.brian.weather.data.settings.AppPreferences
import com.brian.weather.repository.WeatherRepository
import com.brian.weather.repository.WeatherRepositoryImpl


/**
 * I dont think I can pass in vals into the constructor here as its invoked by the Job Scheduler
 */
class DailyLocalWeatherWorker(
    ctx: Context,
    params: WorkerParameters
) : Worker(ctx, params) {
    private val TAGOUTPUT = "Daily Local Weather Call"
    val context = ctx
    /**
     * Send a daily notification for the weather of the phone's current location
     */

    override fun doWork(): Result {

        val weatherRepository = WeatherRepositoryImpl(WeatherApi, WeatherDatabase.getDatabase(context).getWeatherDao())
        var workerResult = Result.success() // worker result is success by default
        var city = ""
        var imgUrl = ""
        var notificationBuilder = ""

        val resources = applicationContext.resources

        // Do some work
        // Only execute and schedule next job if checked in preferences
        val location = inputData.getDoubleArray("location")
        val clockFormat = inputData.getString("clockFormat") ?: "hh:mm a"
        val dateFormat = inputData.getString("dateFormat") ?: "MM/DD"
        val tempUnit = inputData.getString("tempUnit") ?: "Fahrenheit"

        // Only do work if location returned is not null
        if (location != null) {
            // API can take latitude and longitude values separated by comma
            val coordinates = location[0].toString() + "," + location[1].toString()

            CoroutineScope(Dispatchers.IO).launch {



                when (val response =
                    weatherRepository.getForecast(coordinates)) {
                    is NetworkResult.Success -> {
                        val forecastDomainObject = response.data.asDomainModel(
                            preferences = AppPreferences(
                                tempUnit = "Fahrenheit",
                                clockFormat = "hh:mm a",
                                dateFormat = "MM/DD",
                                windUnit = "MPH",
                                dynamicColors = false,
                                showAlerts = true,
                                measurementUnit = "IN",
                                showNotifications = true,
                                showLocalForecast = true,
                                showPrecipitationNotifications = true,
                                precipitationLocations = setOf()
                            )
                        )
                        weatherRepository.getSearchResults(coordinates).onSuccess { city = it.first().name }

                        //TODO this no longer observes the temperature unit setting because I passed
                        //the whole preferences object to the mapper function
                        var maxTemp = ""
                        var minTemp = ""
                        if (tempUnit == "Fahrenheit") {
                            maxTemp =
                                forecastDomainObject.days[0].day.maxTemp.toInt().toString()
                            minTemp =
                                forecastDomainObject.days[0].day.minTemp.toInt().toString()
                        } else {
                            maxTemp =
                                forecastDomainObject.days[0].day.maxTemp.toInt().toString()
                            minTemp =
                                forecastDomainObject.days[0].day.minTemp.toInt().toString()
                        }
                        val condition = forecastDomainObject.days[0].day.condition.text
                        imgUrl = forecastDomainObject.days[0].day.condition.icon
                        notificationBuilder += "$minTemp\u00B0 / $maxTemp\u00B0 \u00B7 $condition"


                    }
                    is NetworkResult.Failure -> workerResult =
                        Result.failure() // return worker failure if api call fails
                    is NetworkResult.Exception -> workerResult = Result.failure()
                }


                if (notificationBuilder.isNotEmpty()) {
                    createChannel(
                        applicationContext.getString(R.string.forecast_notification_channel_id),
                        applicationContext.getString(R.string.forecast_notification_channel_name)
                    )
                    sendNotification(
                        applicationContext,
                        notificationBuilder,
                        "Today's forecast for $city",
                        imgUrl
                    )
                }
            }

        }

        return workerResult // can be success or failure depending on API call
    }

    private fun sendNotification(context: Context, text: String, title: String, imgUrl: String) {
        // New instance of notification manager
        val notificationManager = context.let {
            ContextCompat.getSystemService(
                it,
                NotificationManager::class.java
            )
        } as NotificationManager


        if (applicationContext.let {
                PermissionChecker.checkSelfPermission(
                    it,
                    Manifest.permission.POST_NOTIFICATIONS
                )
            } == PackageManager.PERMISSION_GRANTED) {
            notificationManager.sendForecastNotification(text, context, title, imgUrl)
        }

    }

    private fun createChannel(channelId: String, channelName: String) {
        val notificationChannel = NotificationChannel(
            channelId,
            channelName,
            NotificationManager.IMPORTANCE_HIGH
        )
            .apply {
                setShowBadge(false)
            }

        notificationChannel.enableLights(true)
        notificationChannel.lightColor = Color.RED
        notificationChannel.enableVibration(true)
        notificationChannel.description =
            applicationContext.getString(R.string.local_notification_channel_description)

        val notificationManager = applicationContext.getSystemService(
            NotificationManager::class.java
        )
        notificationManager.createNotificationChannel(notificationChannel)
    }

}
