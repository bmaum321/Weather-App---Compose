package com.brian.weathercompose

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import com.brian.weathercompose.data.BaseApplication
import com.brian.weathercompose.repository.WeatherRepository
import com.brian.weathercompose.ui.WeatherApp
import com.brian.weathercompose.ui.theme.WeatherComposeTheme
import com.brian.weathercompose.ui.viewmodels.MainViewModel
import com.brian.weathercompose.ui.viewmodels.WeatherListViewModel
import org.koin.androidx.viewmodel.ext.android.getViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WeatherComposeTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    val mainViewModel = getViewModel<MainViewModel>()
                    val weatherListViewModel = getViewModel<WeatherListViewModel>()
                    WeatherApp(weatherListViewModel, mainViewModel)
                }
            }
        }
    }
}
