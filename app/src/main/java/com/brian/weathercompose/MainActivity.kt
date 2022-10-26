package com.brian.weathercompose

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.brian.weathercompose.data.WeatherDatabase.Companion.getDatabase
import com.brian.weathercompose.network.ApiResponse
import com.brian.weathercompose.repository.WeatherRepository
import com.brian.weathercompose.theme.WeatherComposeTheme
import kotlinx.coroutines.coroutineScope

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
                    Greeting()
                }
            }
        }
    }
}

@Composable
fun Greeting() {
    val application = Application()
    val weatherRepository = WeatherRepository()
    var y = ""
    LaunchedEffect(key1 = "",) {
        coroutineScope {
            val x = weatherRepository.getForecast("13088")

            when(x) {
                is ApiResponse.Success ->  y = x.data.forecast.forecastday[0].day.avgtemp_f.toString()
                is ApiResponse.Exception -> y = x.e.toString()
            }
        }
    }

    Text(text = y)

}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    WeatherComposeTheme {
        Greeting()
    }
}