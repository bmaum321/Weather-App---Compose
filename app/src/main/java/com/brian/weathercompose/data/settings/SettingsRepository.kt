package com.brian.weathercompose.data.settings

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {

     val getTemperatureUnit: Flow<String?>
     suspend fun setTempertatureUnit(): Unit
}