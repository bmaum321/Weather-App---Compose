package com.brian.weathercompose.data

import android.app.Application
import androidx.room.Room
import com.brian.weathercompose.network.BASE_URL
import com.brian.weathercompose.network.WeatherApiService
import com.brian.weathercompose.network.json
import com.brian.weathercompose.repository.WeatherRepository
import com.brian.weathercompose.ui.viewmodels.*
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import okhttp3.MediaType.Companion.toMediaType
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module
import retrofit2.Retrofit

//import com.google.android.material.color.DynamicColors




/**
 * An application class that inherits from [Application], allows for the creation of a singleton
 * instance of the [WeatherDatabase]
 */
class BaseApplication : Application() {

    // provide a WeatherDatabase value by lazy here
    val database: WeatherDatabase by lazy { WeatherDatabase.getDatabase(this) }

    override fun onCreate() {
        super.onCreate()

        val appModule = module {
            single { Retrofit.Builder()
                .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
                .baseUrl(BASE_URL)
                .build()
                .create(WeatherApiService::class.java)
            }
            single { // Use factory to create multiple instances for each viewmodel
                WeatherRepository()
            }

            single<WeatherDao> {
                database.getWeatherDao()
            }
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
                AddWeatherLocationViewModel(get(), get())
            }
        }

        startKoin{
            androidLogger()
            androidContext(this@BaseApplication)
            modules(appModule)
        }
    }
}