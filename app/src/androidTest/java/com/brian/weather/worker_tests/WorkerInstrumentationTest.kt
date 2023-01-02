package com.brian.weather.worker_tests

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.Data
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.testing.TestWorkerBuilder
import androidx.work.workDataOf
import com.brian.weather.data.settings.AppPreferences
import com.brian.weather.util.workers.DailyLocalWeatherWorker
import com.brian.weather.util.workers.DailyPrecipitationWorker
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

/**
 * Work manager must be tested as an Android test because they require context
 */
class WorkerInstrumentationTest {
    private lateinit var context: Context
    private val preferences = AppPreferences(
        tempUnit = "Fahrenheit",
        clockFormat = "hh:mm",
        dateFormat = "dd:mm",
        windUnit = "mph",
        dynamicColors = true,
        showAlerts = true,
        measurementUnit = "in",
        showNotifications = true,
        showLocalForecast = true,
        showPrecipitationNotifications = true,
        precipitationLocations = setOf("Miami, Florida")
    )

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    /**
     * TestListenableWorkerBuilder is used for coroutine workers
     * TestWorkerBuilder is used for other cases
     */

    /**
     * Verify that worker returns a success result.
     * Verify that all input data passed to worker successfully
     */
    @Test
    fun dailyPrecipitationWorker_doWork_resultSuccess() {
        val data = Data.Builder()
        data.putStringArray("locations", preferences.precipitationLocations.toTypedArray())
        data.putString("clockFormat", preferences.clockFormat)
        data.putString("dateFormat", preferences.dateFormat)
        val worker = TestListenableWorkerBuilder<DailyPrecipitationWorker>(context)
            .setInputData(data.build())
            .build()
        runBlocking {
            val result = worker.doWork()
            val resultLocations = result.outputData.getStringArray("locations")
            val resultClockFormat = result.outputData.getString("clockFormat")
            val resultDateFormat = result.outputData.getString("dateFormat")
            assertTrue(result is ListenableWorker.Result.Success)
            resultLocations?.let { assertTrue(it.contains("Miami, Florida")) }
            resultClockFormat?.let { assertTrue(it == "hh:mm") }
            resultDateFormat?.let { assertTrue(it == "dd:mm") }
        }
    }


    /**
     * Verify that worker returns a success result.
     * Verify that all input data passed to worker successfully
     */
    @Test
    fun dailyLocalForecastWorker_doWork_resultSuccess() {
        val coordinates = doubleArrayOf(24.44, 25.55)
        val data = Data.Builder()
        data.putStringArray("locations", preferences.precipitationLocations.toTypedArray())
        data.putString("clockFormat", preferences.clockFormat)
        data.putString("dateFormat", preferences.dateFormat)
        data.putString("tempUnit", preferences.tempUnit)
        data.putDoubleArray("coordinates", coordinates)
        val worker = TestListenableWorkerBuilder<DailyLocalWeatherWorker>(context)
            .setInputData(data.build())
            .build()
        runBlocking {
            val result = worker.doWork()
            val resultClockFormat = result.outputData.getString("clockFormat")
            val resultDateFormat = result.outputData.getString("dateFormat")
            val resultTempUnit = result.outputData.getString("tempUnit")
            val resultCoordinates = result.outputData.getDoubleArray("coordinates")
            assertTrue(result is ListenableWorker.Result.Success)
            resultClockFormat?.let { assertTrue(it == "hh:mm") }
            resultDateFormat?.let { assertTrue(it == "dd:mm") }
            resultTempUnit?.let { assertTrue(it == "Fahrenheit") }
            resultCoordinates?.let { assertTrue(it.isNotEmpty()) }
        }
    }
}