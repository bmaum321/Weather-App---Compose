package com.brian.weather.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow


/**
 * Data Access Object for database interaction.
 *
 *
By specifying a Flow return type, Room executes the query with the following characteristics:

Main-safety – Queries with a Flow return type always run on the Room executors, so they are always main-safe.
You don't need to do anything in your code to make them run off the main thread.
Observes changes – Room automatically observes changes and emits new values to the flow.

Async sequence – Flow emits the entire query result on each change, and it won't introduce any buffers.
If you return a Flow<List<T>>, the flow emits a List<T> that contains all rows from the query result.
It will execute just like a sequence – emitting one query result at a time and suspending until it
is asked for the next one.

Cancellable – When the scope that's collecting these flows is cancelled, Room cancels observing this query.

Put together, this makes Flow a great return type for observing the database from the UI layer.
 */
@Dao
interface WeatherDao {

    // method to retrieve all zipcodes from database and order them by sort order ascending
    @Query("SELECT zipCode FROM weather_database ORDER BY sortOrder ASC")
    fun getZipcodesFlow(): List<String>

    // method to retrieve all weather entities from database
    @Query("SELECT * FROM weather_database ORDER BY sortOrder ASC")
    fun getAllWeatherEntities(): Flow<List<WeatherEntity>>

    // method to retrieve a Weather from the database by id
    @Query("SELECT * FROM weather_database WHERE id = :id")
    fun getWeatherById(id: Long): Flow<WeatherEntity>

    // method to retrieve a Weather from the database by zipcode
    @Query("SELECT * FROM weather_database WHERE zipCode = :zipcode")
    fun getWeatherByZipcode(zipcode: String): Flow<WeatherEntity>

    // method to retrieve a Weather from the database by location and return as object
    @Query("SELECT * FROM weather_database WHERE zipCode = :location")
    fun getWeatherByLocation(location: String): Flow<WeatherEntity> //TODO should be nullable

    // method to insert a Weather into the database
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(weatherEntity: WeatherEntity)

    // method to update a Weather that is already in the database
    @Update
    suspend fun update(weatherEntity: WeatherEntity)

    // method to delete a Weather from the database.
    @Delete
    suspend fun delete(weatherEntity: WeatherEntity)

    // method to retrieve last entry in the table
    @Query("SELECT * FROM weather_database ORDER BY ID DESC LIMIT 1")
    fun selectLastEntry(): WeatherEntity

    // Check if database is empty
    @Query("SELECT (SELECT COUNT(*) FROM weather_database) == 0")
    fun isEmpty(): Boolean
}