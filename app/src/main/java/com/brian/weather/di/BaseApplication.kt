package com.brian.weather.di

import android.app.Application
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.brian.weather.data.local.WeatherDao
import com.brian.weather.data.local.WeatherDatabase
import com.brian.weather.data.remote.WeatherApi
import com.brian.weather.data.settings.PreferencesRepository
import com.brian.weather.data.settings.PreferencesRepositoryImpl
import com.brian.weather.domain.usecase.CreateDailyForecastStateUseCase
import com.brian.weather.domain.usecase.CreateHourlyForecastStateUseCase
import com.brian.weather.domain.usecase.CreateSearchStateUseCase
import com.brian.weather.domain.usecase.CreateWeatherListStateUsecase
import com.brian.weather.repository.WeatherRepository
import com.brian.weather.repository.WeatherRepositoryImpl
import com.brian.weather.presentation.viewmodels.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
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

            // Singleton for the Weather Repository implementation passed to all viewmodels
            single<WeatherRepository> {
                WeatherRepositoryImpl(get(), get())
                //FakeWeatherRepository()
            }

            // Singleton for WeatherApi Object passed to the weather repository
            single {
                WeatherApi
            }

            // Singleton for the dao passed to all viewmodels
            single<WeatherDao> {
                database.getWeatherDao()
            }

            /** Singleton for the Settings Repository implementation passed to the WeatherListViewModel
             * And to the UnitsScreen
             */

            single<PreferencesRepository> {
                PreferencesRepositoryImpl(get())
            }

            // Singleton for the preferences data store that is passed to the Settings Repository
            single {
                PreferenceDataStoreFactory.create(
                    corruptionHandler = ReplaceFileCorruptionHandler(
                        produceNewData = { emptyPreferences() }
                    ),
                migrations = listOf(SharedPreferencesMigration(this@BaseApplication,"Preferences")),
                scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
                produceFile = {this@BaseApplication.preferencesDataStoreFile("Preferences")})
            }

            single {
                CreateWeatherListStateUsecase(get(), get())
            }

            single {
                CreateDailyForecastStateUseCase(get(), get())
            }

            single {
                CreateHourlyForecastStateUseCase(get(), get())
            }

            single {
                CreateSearchStateUseCase(get(), get())
            }

            // Use factory to create multiple instances for each viewmodel
            viewModel {
                MainViewModel()
            }
            viewModel {
                WeatherListViewModel(get(), get(), get())
            }
            viewModel {
                DailyForecastViewModel(get(), get())
            }
            viewModel {
                HourlyForecastViewModel(get(), get())
            }
            viewModel {
                AddWeatherLocationViewModel(get(), get())
            }
        }

        startKoin {
            androidLogger()
            androidContext(this@BaseApplication)
            modules(appModule)
        }
    }
}