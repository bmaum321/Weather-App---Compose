package com.brian.weather.worker_tests

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.workDataOf
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

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun dailyPrecipitationWorker_doWork_resultSuccess() {
        val worker = TestListenableWorkerBuilder<DailyPrecipitationWorker>(context)
            .setInputData(workDataOf())
            .build()
        runBlocking {
            val result = worker.doWork()
            assertTrue(result is ListenableWorker.Result.Success)
        }
    }

    @Test
    fun dailyLocalForecastWorker_doWork_resultSuccess() {
        val worker = TestListenableWorkerBuilder<DailyLocalWeatherWorker>(context)
            .setInputData(workDataOf())
            .build()
        runBlocking {
            val result = worker.doWork()
            assertTrue(result is ListenableWorker.Result.Success)
        }
    }
}