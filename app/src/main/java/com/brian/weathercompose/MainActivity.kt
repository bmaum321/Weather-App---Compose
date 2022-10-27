package com.brian.weathercompose

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.viewmodel.compose.viewModel
import com.brian.weathercompose.data.BaseApplication
import com.brian.weathercompose.data.WeatherDatabase.Companion.getDatabase
import com.brian.weathercompose.network.ApiResponse
import com.brian.weathercompose.repository.WeatherRepository
import com.brian.weathercompose.ui.WeatherApp
import com.brian.weathercompose.ui.theme.WeatherComposeTheme
import com.brian.weathercompose.ui.viewmodels.WeatherListViewModel
import kotlinx.coroutines.coroutineScope

class MainActivity : ComponentActivity() {

    private val viewModel: WeatherListViewModel by viewModels {
        WeatherListViewModel.WeatherViewModelFactory(
            (this.application as BaseApplication).database.weatherDao(),
            Application(),
            WeatherRepository()
        )
    }
    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        setContent {
            WeatherComposeTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                   // WeatherApp(viewModel(factory = WeatherListViewModel.Factory))
                    WeatherApp(viewModel)
                }
            }
        }
    }
}
