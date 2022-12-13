package com.brian.weather


import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.brian.weather.presentation.WeatherApp
import com.brian.weather.presentation.reusablecomposables.CustomAlertDialog
import com.brian.weather.presentation.theme.WeatherComposeTheme
import com.brian.weather.presentation.viewmodels.MainViewModel
import com.brian.weather.presentation.viewmodels.WeatherListViewModel
import com.brian.weather.util.workers.JobScheduler
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.shouldShowRationale
import org.koin.androidx.viewmodel.ext.android.getViewModel


class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WeatherComposeTheme {

                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

                    val showNotificationPermissionRationale = remember { mutableStateOf(false) }
                    val showLocationPermissionRationale = remember { mutableStateOf(false) }
                    val showBackgroundLocationRationale = remember { mutableStateOf(false) }
                    val showLocationServicesDialog = remember { mutableStateOf(false) }
                    val permissionState = rememberMultiplePermissionsState(
                        permissions = listOf(
                            Manifest.permission.POST_NOTIFICATIONS,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            // Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        )
                    )
                    val lifecycleOwner = LocalLifecycleOwner.current
                    DisposableEffect(
                        key1 = lifecycleOwner,
                        effect = {
                            val observer = LifecycleEventObserver { _, event ->
                                if (event == Lifecycle.Event.ON_RESUME) {
                                    permissionState.launchMultiplePermissionRequest()
                                }
                            }
                            lifecycleOwner.lifecycle.addObserver(observer)

                            onDispose {
                                lifecycleOwner.lifecycle.removeObserver(observer)
                            }
                        }
                    )
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        permissionState.permissions.forEach { perm ->
                            when (perm.permission) {
                                Manifest.permission.POST_NOTIFICATIONS -> {
                                    when {
                                        perm.status.isGranted -> {
                                            // This should be a dialog
                                        }
                                        perm.status.shouldShowRationale -> {
                                            // Text(text = "Permission is needed for notifications")
                                            showNotificationPermissionRationale.value = true
                                        }
                                        !perm.status.isGranted && !perm.status.shouldShowRationale -> {
                                            // User declined permission for second time
                                        }
                                    }
                                }
                                Manifest.permission.ACCESS_COARSE_LOCATION -> {
                                    when {
                                        perm.status.isGranted -> {
                                            // This should be a dialog
                                            Text(text = "Location permission granted")
                                        }
                                        perm.status.shouldShowRationale -> {
                                            showLocationPermissionRationale.value = true
                                        }
                                        !perm.status.isGranted && !perm.status.shouldShowRationale -> {
                                            // User declined permission for second time
                                            Text(text = "Location permission was permanently  denied, you can enable in app settings ")
                                        }
                                    }
                                }
                                Manifest.permission.ACCESS_BACKGROUND_LOCATION -> {
                                    when {
                                        perm.status.isGranted -> {
                                            // This should be a dialog
                                            Text(text = "Background permission granted")
                                        }
                                        perm.status.shouldShowRationale -> {
                                            Text(text = "Background permission is needed for notifications")
                                        }
                                        !perm.status.isGranted && !perm.status.shouldShowRationale -> {
                                            // User declined permission for second time
                                            Text(text = "Background location permission was permanently denied, you can enable in app settings ")
                                        }
                                    }
                                }
                            }
                        }

                    }

                    if (showNotificationPermissionRationale.value) {
                        CustomAlertDialog(
                            tag = "Notification Rationale",
                            title = "Notifications",
                            text = "Notifications permission needed to show precipitation alerts and local forecast notifications",
                            onDismissRequest = {
                                showNotificationPermissionRationale.value = false
                            },
                            dismissButtonOnClick = { showNotificationPermissionRationale.value = false },
                            confirmButtonOnClick = {
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                intent.data = Uri.parse("package:$packageName")
                                startActivity(intent)
                            },
                            confirmText = "Ok"
                        )
                    }

                    if (showLocationPermissionRationale.value) {
                        CustomAlertDialog(
                            tag = "Location Rationale",
                            title = "Location",
                            text = "Location permission needed to show local forecast notifications",
                            onDismissRequest = {
                                showLocationPermissionRationale.value = false
                            },
                            dismissButtonOnClick = { showLocationPermissionRationale.value = false },
                            confirmButtonOnClick = {
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                intent.data = Uri.parse("package:$packageName")
                                startActivity(intent)
                            },
                            confirmText = "Ok"
                        )
                    }

                    if (showBackgroundLocationRationale.value) {
                        CustomAlertDialog(
                            tag = "Background Location Rationale",
                            title = "Background Location Needed",
                            text = "Weather Tracker collects location data to enable local forecast notifications,\n" +
                                    "even when the app is closed or not in use. Please allow background location in settings.",
                            onDismissRequest = {
                                showBackgroundLocationRationale.value = false
                            },
                            dismissButtonOnClick = { showBackgroundLocationRationale.value = false },
                            confirmButtonOnClick = {
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                intent.data = Uri.parse("package:$packageName")
                                startActivity(intent)
                                showBackgroundLocationRationale.value = false
                            },
                            confirmText = "Ok"
                        )
                    }

                    if (showLocationServicesDialog.value) {
                        CustomAlertDialog(
                            tag = "Location Services",
                            title = "Location Services Required",
                            text = "Turn on Location services in Settings",
                            onDismissRequest = {
                                showLocationServicesDialog.value = false
                            },
                            dismissButtonOnClick = { showLocationServicesDialog.value = false },
                            confirmButtonOnClick = {
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                intent.data = Uri.parse("package:$packageName")
                                startActivity(intent)
                            },
                            confirmText = "Ok"
                        )
                    }
                    val mainViewModel = getViewModel<MainViewModel>()
                    val weatherListViewModel = getViewModel<WeatherListViewModel>()

                    val preferences by remember {
                        weatherListViewModel.allPreferences
                    }.collectAsState()

                    preferences?.let {
                        val jobScheduler = JobScheduler(it)
                        jobScheduler.schedulePrecipitationJob(LocalContext.current)
                        jobScheduler.scheduleForecastJob(LocalContext.current) }

                    if(preferences?.showLocalForecast == true) {
                        if (!checkBackgroundLocationPermissions()) {
                                showBackgroundLocationRationale.value = true
                        } else if (!isLocationEnabled()) {
                            showLocationServicesDialog.value = true
                        }

                    }

                    /**
                     * Main Entry Point
                     */
                    WeatherApp(weatherListViewModel, mainViewModel)

                }
            }
        }
    }
    // Check if background location is enabled for forecast alerts
    private fun checkBackgroundLocationPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED)  {
            return true
        }
        return false
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }


}

