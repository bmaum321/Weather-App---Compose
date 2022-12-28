package com.example.weather.db_tests

import android.content.ClipData
import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.brian.weather.data.local.WeatherDao
import com.brian.weather.data.local.WeatherDatabase
import com.brian.weather.data.local.WeatherEntity
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class WeatherDaoTest {
    private lateinit var weatherDao: WeatherDao
    private lateinit var weatherDatabase: WeatherDatabase
    private val weather1 = WeatherEntity(
        id = 1,
        cityName = "Beverly Hills",
        zipCode = "90210",
        sortOrder = 1
    )
    private val weather2 = WeatherEntity(
        id = 2,
        cityName = "Liverpool",
        zipCode = "13088",
        sortOrder = 2
    )

    @Before
    fun createDb() {
        val context: Context = ApplicationProvider.getApplicationContext()
        weatherDatabase = Room.inMemoryDatabaseBuilder(
            context, WeatherDatabase::class.java
        )
                // Allow main thread queries just for testing
            .allowMainThreadQueries()
            .build()
        weatherDao = weatherDatabase.getWeatherDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        weatherDatabase.close()
    }


    private suspend fun addOneItemToDb() {
        weatherDao.insert(weather1)
    }

    private suspend fun addTwoItemsToDb(){
        weatherDao.insert(weather1)
        weatherDao.insert(weather2)
    }

    @Test
    @Throws(Exception::class)
    fun daoInsert_insertsItemIntoDB() = runBlocking {
        addOneItemToDb()
        val allWeathers = weatherDao.getAllWeatherEntities().first()
        assertEquals(allWeathers[0], weather1)
    }

    @Test
    @Throws(Exception::class)
    fun daoGetAllItems_returnsAllItemsFromDB() = runBlocking {
        addTwoItemsToDb()
        val allWeathers = weatherDao.getAllWeatherEntities().first()
        Assert.assertEquals(allWeathers[0], weather1)
        Assert.assertEquals(allWeathers[1], weather2)
    }

    @Test
    @Throws(Exception::class)
    fun daoDeleteItems_deletesAllItemsFromDB() = runBlocking {
        addTwoItemsToDb()
        weatherDao.delete(weather1)
        weatherDao.delete(weather2)
        val allItems = weatherDao.getAllWeatherEntities().first()
        assertTrue(allItems.isEmpty())
    }

    @Test
    @Throws(Exception::class)
    fun daoUpdateItems_updatesItemsInDB() = runBlocking {
        addTwoItemsToDb()
        weatherDao.update(WeatherEntity(id = 1, cityName = "Miami", zipCode = "00000", sortOrder = 1))
        weatherDao.update(WeatherEntity(id = 2, cityName = "Buffalo", zipCode = "11111", sortOrder = 2))

        val allItems = weatherDao.getAllWeatherEntities().first()
        assertEquals(allItems[0], WeatherEntity(id = 1, cityName = "Miami", zipCode = "00000", sortOrder = 1))
        assertEquals(allItems[1], WeatherEntity(id = 2, cityName = "Buffalo", zipCode = "11111", sortOrder = 2))
    }

    @Test
    @Throws(Exception::class)
    fun daoGetWeatherById_returnsCorrectWeather() = runBlocking {
        addTwoItemsToDb()
        val weather = weatherDao.getWeatherById(weather1.id).first()
        assertEquals(weather1, weather)
    }

    @Test
    @Throws(Exception::class)
    fun daoGetZipcodes_returnsZipcodes() = runBlocking {
        addTwoItemsToDb()
        val zipcodes = weatherDao.getZipcodes()
        assertTrue(zipcodes.containsAll(listOf(weather1.zipCode, weather2.zipCode)))
    }

    @Test
    @Throws(Exception::class)
    fun daoGetWeatherByZipcode_returnsCorrectWeather() = runBlocking {
        addTwoItemsToDb()
        val weatherEntity = weatherDao.getWeatherByZipcode(weather1.zipCode).first()
        assertEquals(weather1, weatherEntity)
    }

    @Test
    @Throws(Exception::class)
    fun daoGetWeatherByLocation_returnsCorrectWeather() = runBlocking {
        addTwoItemsToDb()
        val weatherEntity = weatherDao.getWeatherByLocation(weather1.zipCode).first()
        assertEquals(weather1, weatherEntity)
    }

    @Test
    @Throws(Exception::class)
    fun daoSelectLastEntry_returnsLastEntry() = runBlocking {
        addTwoItemsToDb()
        val weatherEntity = weatherDao.selectLastEntry()
        assertEquals(weather2, weatherEntity)
    }

    @Test
    @Throws(Exception::class)
    fun daoCheckDBEmpty_returnsTrue() = runBlocking {
        addTwoItemsToDb()
        weatherDao.delete(weather1)
        weatherDao.delete(weather2)
        val isEmpty = weatherDao.isEmpty()
        assertTrue(isEmpty)
    }
}