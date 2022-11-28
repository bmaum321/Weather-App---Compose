package com.brian.weathercompose.di

import android.app.Application
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontVariation
import com.brian.weathercompose.data.local.WeatherDao
import com.brian.weathercompose.data.local.WeatherDatabase
import com.brian.weathercompose.data.remote.WeatherApi
import com.brian.weathercompose.presentation.screens.settings.SettingsDatastore
import com.brian.weathercompose.repository.WeatherRepository
import com.brian.weathercompose.repository.WeatherRepositoryImpl
import com.brian.weathercompose.presentation.viewmodels.*
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module

/**
 * An application class that inherits from [Application], allows for the creation of a singleton
 * instance of the [WeatherDatabase]
 */
class BaseApplication : Application() {

    // provide a WeatherDatabase value by lazy here
    // could we create a sinleton below in Koin? doesnt really maatter I think
    val database: WeatherDatabase by lazy { WeatherDatabase.getDatabase(this) }

    override fun onCreate() {
        super.onCreate()

        val appModule = module {
            /*
            single { Retrofit.Builder()
                .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
                .baseUrl(BASE_URL)
                .build()
                .create(WeatherApiService::class.java)
            }

             */
            single<WeatherRepository> {
                WeatherRepositoryImpl(get())
            }

            single {
                WeatherApi
            }

            single<WeatherDao> {
                database.getWeatherDao()
            }

            single<SettingsDatastore> {
                SettingsDatastore(androidContext())
            }

            // Use factory to create multiple instances for each viewmodel
            viewModel {
                MainViewModel()
            }
            viewModel {
                WeatherListViewModel(get(), get(), get())
            }
            viewModel {
                DailyForecastViewModel(get(), get(), get())
            }
            viewModel {
                HourlyForecastViewModel(get(), get(), get())
            }
            viewModel {
                AddWeatherLocationViewModel(get(), get(), get())
            }
        }

        startKoin {
            androidLogger()
            androidContext(this@BaseApplication)
            modules(appModule)
        }
    }
}