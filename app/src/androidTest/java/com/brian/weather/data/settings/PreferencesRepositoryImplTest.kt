package com.brian.weather.data.settings

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
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PreferencesRepositoryImplTest {


    //datastore needs to be singleton for testing, this can probably be made via koin
    companion object {

        val context: Context = ApplicationProvider.getApplicationContext<Context>()
        private val testDatastore = PreferenceDataStoreFactory.create(
            corruptionHandler = ReplaceFileCorruptionHandler(
                produceNewData = { emptyPreferences() }
            ),
            migrations = listOf(SharedPreferencesMigration(context, "TestPreferences")),
            scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
            produceFile = { context.preferencesDataStoreFile("TestPreferences") })
        private val preferencesRepository = PreferencesRepositoryImpl(testDatastore)
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

    @Test
    fun preferencesRepository_readAndWriteClockSettingToDatastore() {
        runBlocking {
            // Read initial setting from repository
            assertEquals("hh:mm a", preferencesRepository.getClockFormat.first())
            preferencesRepository.saveClockFormatSetting("kk:mm")
            assertEquals("kk:mm", preferencesRepository.getClockFormat.first())
            // Set back to initial setting
            preferencesRepository.saveClockFormatSetting("hh:mm a")
        }
    }

    @Test
    fun preferencesRepository_readAndWriteDynamicColorSetting() {
        runBlocking {
            // Read initial setting from repository
            assertEquals(true, preferencesRepository.getDynamicColorsSetting.first())
            preferencesRepository.saveDynamicColorSetting(false)
            assertEquals(false, preferencesRepository.getDynamicColorsSetting.first())
            // Set back to initial setting
            preferencesRepository.saveDynamicColorSetting(true)
        }
    }

    @Test
    fun preferencesRepository_readAndWriteAlertSetting() {
        runBlocking {
            // Read initial setting from repository
            assertEquals(true, preferencesRepository.getWeatherAlertsSetting.first())
            preferencesRepository.saveWeatherAlertSetting(false)
            assertEquals(false, preferencesRepository.getWeatherAlertsSetting.first())
            // Set back to initial setting
            preferencesRepository.saveWeatherAlertSetting(true)
        }
    }


    @Test
    fun preferencesRepository_readAndWriteDateSetting() {
        runBlocking {
            // Read initial setting from repository
            assertEquals("MM/DD", preferencesRepository.getDateFormat.first())
            preferencesRepository.saveDateFormatSetting("DD/MM")
            assertEquals("DD/MM", preferencesRepository.getDateFormat.first())
            // Set back to initial setting
            preferencesRepository.saveDateFormatSetting("MM/DD")
        }
    }

    @Test
    fun preferencesRepository_readAndWriteNotificationSetting() {
        runBlocking {
            // Read initial setting from repository
            assertEquals(true, preferencesRepository.getNotificationSetting.first())
            preferencesRepository.saveNotificationSetting(false)
            assertEquals(false, preferencesRepository.getNotificationSetting.first())
            // Set back to initial setting
            preferencesRepository.saveNotificationSetting(true)
        }
    }

    @Test
    fun preferencesRepository_readAndWriteLocalNotificationSetting() {
        runBlocking {
            // Read initial setting from repository
            assertEquals(false, preferencesRepository.getLocalForecastSetting.first())
            preferencesRepository.saveLocalForecastSetting(true)
            assertEquals(true, preferencesRepository.getLocalForecastSetting.first())
            // Set back to initial setting
            preferencesRepository.saveLocalForecastSetting(false)
        }
    }

    @Test
    fun preferencesRepository_readAndWritePrecipitationNotificationSetting() {
        runBlocking {
            // Read initial setting from repository
            assertEquals(true, preferencesRepository.getPrecipitationSetting.first())
            preferencesRepository.savePrecipitationSetting(false)
            assertEquals(false, preferencesRepository.getPrecipitationSetting.first())
            // Set back to initial setting
            preferencesRepository.savePrecipitationSetting(true)
        }
    }

    @Test
    fun preferencesRepository_readAndWritePrecipitationLocations() {
        runBlocking {
            // Read initial setting from repository
            assertEquals(emptySet<String>(), preferencesRepository.getPrecipitationLocations.first())
            preferencesRepository.savePrecipitationLocations(setOf("Miami"))
            assertEquals(setOf("Miami"), preferencesRepository.getPrecipitationLocations.first())
            // Set back to initial setting
            preferencesRepository.savePrecipitationLocations(emptySet())
        }
    }

    @Test
    fun preferencesRepository_readAllAppPreferences() {
        runBlocking {
            // Read initial setting from repository
            val preferences = preferencesRepository.getAllPreferences.first()
            assertEquals(preferences, preferencesRepository.getAllPreferences.first())
        }
    }

}