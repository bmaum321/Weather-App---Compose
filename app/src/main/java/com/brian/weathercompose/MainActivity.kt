package com.brian.weathercompose

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontVariation
import androidx.lifecycle.lifecycleScope
import com.brian.weathercompose.data.settings.SettingsRepository
import com.brian.weathercompose.presentation.WeatherApp
import com.brian.weathercompose.presentation.theme.WeatherComposeTheme
import com.brian.weathercompose.presentation.viewmodels.MainViewModel
import com.brian.weathercompose.presentation.viewmodels.WeatherListViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.getViewModel
import org.koin.androidx.compose.get



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
