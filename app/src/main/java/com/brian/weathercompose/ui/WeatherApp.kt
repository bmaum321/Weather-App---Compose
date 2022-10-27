package com.brian.weathercompose.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.brian.weathercompose.R
import com.brian.weathercompose.ui.screens.HomeScreen
import com.brian.weathercompose.ui.viewmodels.WeatherListViewModel

/**
 * Main entry point composable for app
 */

@Composable
fun WeatherApp(weatherListViewModel: WeatherListViewModel, modifier: Modifier = Modifier) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { TopAppBar(title = { Text(stringResource(R.string.app_name)) }) }
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            color = MaterialTheme.colors.background
        ) {
            HomeScreen(
                marsUiState = weatherListViewModel.marsUiState,
                retryAction = weatherListViewModel::getForecast,
                modifier = modifier
            )
        }
    }
}
