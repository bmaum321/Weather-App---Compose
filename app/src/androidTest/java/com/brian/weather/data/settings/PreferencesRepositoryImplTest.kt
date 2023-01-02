package com.brian.weather.data.settings

import android.app.Application
import android.content.Context
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.brian.weather.repository.FakePreferencesRepositoryImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PreferencesRepositoryImplTest {


    //datastore needs to be singleton for testing, this can probably be made via koin
    companion object {

        val context: Context = ApplicationProvider.getApplicationContext<Context>()
        val dataStore = PreferenceDataStoreFactory.create(
            corruptionHandler = ReplaceFileCorruptionHandler(
                produceNewData = { emptyPreferences() }
            ),
            migrations = listOf(SharedPreferencesMigration(context, "TestPreferences")),
            scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
            produceFile = { context.preferencesDataStoreFile("TestPreferences") })
        private val preferencesRepository = FakePreferencesRepositoryImpl(dataStore)
    }


    @After
    fun clearPreferences() {
       // preferencesRepository.
    }

    @Test
    fun preferencesRepository_readAndWriteTempSettingToDatastore() {
        runBlocking {
            // Read initial setting from repository
            assertEquals("Fahrenheit", preferencesRepository.getTemperatureUnit.first())
            preferencesRepository.saveTemperatureSetting("Celsius")
            assertEquals("Celsius", preferencesRepository.getTemperatureUnit.first())
            // Set back to initial setting
            preferencesRepository.saveTemperatureSetting("Fahrenheit")
        }
    }

    @Test
    fun preferencesRepository_readAndWriteWindSpeedSettingToDatastore() {
        runBlocking {
            // Read initial setting from repository
            assertEquals("MPH", preferencesRepository.getWindspeedUnit.first())
            preferencesRepository.saveWindspeedSetting("KPH")
            assertEquals("KPH", preferencesRepository.getWindspeedUnit.first())
            // Set back to initial setting
            preferencesRepository.saveWindspeedSetting("MPH")
        }
    }

    @Test
    fun preferencesRepository_readAndWriteMeasurementSettingToDatastore() {
        runBlocking {
            // Read initial setting from repository
            assertEquals("IN", preferencesRepository.getMeasurementUnit.first())
            preferencesRepository.saveMeasurementSetting("MM")
            assertEquals("MM", preferencesRepository.getMeasurementUnit.first())
            // Set back to initial setting
            preferencesRepository.saveMeasurementSetting("IN")
        }
    }

}