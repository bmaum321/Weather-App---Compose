package com.brian.weather.util.workers

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.work.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.brian.weather.data.mapper.asDomainModel
import com.brian.weather.data.remote.NetworkResult
import com.brian.weather.data.remote.WeatherApi
import com.brian.weather.repository.WeatherRepositoryImpl
import com.brian.weather.util.Constants
import com.brian.weather.util.sendPrecipitationNotification
import com.brian.weather.R
import com.brian.weather.data.local.WeatherDatabase
import com.brian.weather.data.settings.AppPreferences

class DailyPrecipitationWorker(
    ctx: Context,
    params: WorkerParameters
) : Worker(ctx, params) {
    private val TAGOUTPUT = "Daily API Call"
    val context = ctx

    /**
     * Call API here and send notifications based off local settings and precipitation values
     */

    override fun doWork(): Result {
        var workerResult = Result.success() // worker result is success by default
        val resources = applicationContext.resources
        var iconUrl = ""

        // Do some work

        /**
         * seems like I cant modify collection without creating issues. I need to make a copy of it
         * Issue documented here:
         * http://developer.android.com/reference/android/content/SharedPreferences.html#getStringSet%28java.lang.String,%20java.util.Set%3Cjava.lang.String%3E%29
         */

        var notificationBuilder = ""

        val weatherRepository = WeatherRepositoryImpl(WeatherApi, WeatherDatabase.getDatabase(context).getWeatherDao())
        val locations = inputData.getStringArray("locations") ?: emptyArray()
        val clockFormat = inputData.getString("clockFormat") ?: "hh:mm a"
        val dateFormat = inputData.getString("dateFormat") ?: "hh:mm a"

        CoroutineScope(Dispatchers.IO).launch {
            // Check if database is empty
            if (locations.isNotEmpty()) {
                locations.forEach { location -> // for each selected notification send a precipitation notification
                    when (val response =
                        weatherRepository.getForecast(location)) {
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
                                    precipitationLocations = setOf(),
                                    cardSize = ""
                                )
                            )
                            val willItRainToday = mutableListOf<Int>()
                            forecastDomainObject.days.first().hours.forEach { hour ->
                                willItRainToday.add(hour.will_it_rain)
                            }
                            val willItSnowToday = mutableListOf<Int>()
                            forecastDomainObject.days.first().hours.forEach { hour ->
                                willItRainToday.add(hour.will_it_snow)
                            }

                            /**
                             * If it will rain today, send notification for first matching time
                             * in hourly forecast. If it will snow more than rain, send notification
                             * for first matching time in hourly forecast.
                             */
                            if (willItRainToday.contains(1) || willItSnowToday.contains(1)) {
                                val timeOfRain =
                                    forecastDomainObject.days.first().hours.firstOrNull { it.will_it_rain == 1 }?.time
                                val hoursWithRain = willItRainToday.count { it == 1 }
                                val timeOfSnow =
                                    forecastDomainObject.days.first().hours.firstOrNull { it.will_it_snow == 1 }?.time
                                val hoursWithSnow = willItSnowToday.count { it == 1 }

                                notificationBuilder += if (hoursWithRain > hoursWithSnow) {
                                    "Expect Rain for $location around $timeOfRain\n "
                                } else {
                                    "Expect Snow for $location around $timeOfSnow\n"
                                }
                                iconUrl = if ((hoursWithRain > hoursWithSnow)) {
                                    Constants.rainIconUrl
                                } else Constants.snowIconUrl
                            }
                        }
                        is NetworkResult.Failure -> workerResult =
                            Result.failure() // return worker failure if api call fails
                        is NetworkResult.Exception -> workerResult = Result.failure()
                    }

                }
            }

            if (notificationBuilder.isNotEmpty()) {
                createChannel(
                    applicationContext.getString(R.string.precipitation_notification_channel_id),
                    applicationContext.getString(R.string.precipitation_notification_channel_description)
                )
                sendNotification(
                    applicationContext,
                    notificationBuilder,
                    iconUrl
                )
            }
        }

        return workerResult // can be success or failure depending on API call
    }

    private fun sendNotification(context: Context, text: String, iconUrl: String) {
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
            notificationManager.sendPrecipitationNotification(text, context, iconUrl)
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
            applicationContext.getString(R.string.precipitation_notification_channel_description)

        val notificationManager = applicationContext.getSystemService(
            NotificationManager::class.java
        )
        notificationManager.createNotificationChannel(notificationChannel)
    }

}
