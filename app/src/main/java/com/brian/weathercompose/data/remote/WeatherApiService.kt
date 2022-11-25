package com.brian.weathercompose.data.remote

import com.brian.weathercompose.data.remote.dto.WeatherContainer
import com.brian.weathercompose.data.remote.dto.ForecastContainer
import com.brian.weathercompose.data.remote.dto.Search
import com.brian.weathercompose.util.Constants.BASE_URL
import com.brian.weathercompose.util.Constants.CURRENT
import com.brian.weathercompose.util.Constants.FORECAST
import com.brian.weathercompose.util.Constants.SEARCH
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * A retrofit service to fetch the weather data from the API
 */

val json = Json {
    ignoreUnknownKeys = true
}

// Configure retrofit to parse JSON and use coroutines
@OptIn(ExperimentalSerializationApi::class)
val retrofit: Retrofit = Retrofit.Builder()
    .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
    .baseUrl(BASE_URL)
    .addCallAdapterFactory(NetworkResultCallAdapterFactory.create())
    .build()


interface WeatherApiService {
    @GET(CURRENT)
    suspend fun getWeather(
        @Query("q") zipcode: String
    ): NetworkResult<WeatherContainer>

    @GET(SEARCH)
    suspend fun locationSearch(
        @Query("Q") location: String
    ): NetworkResult<List<Search>>

    @GET(FORECAST)
    suspend fun getForecast(
        @Query("q") zipcode: String,
        @Query("days") days: Int = 7, // Maximum forecast days for free API is 3 days, have paid plan with 7 days
        @Query("alerts") alerts: String = "yes"
    ): NetworkResult<ForecastContainer>
}


/**
 * Main entry point for network access
 */

object WeatherApi {
    val retrofitService: WeatherApiService by lazy {
        retrofit.create(WeatherApiService::class.java)
    }
}


/**
 * Sealed class to handle API responses
 */
sealed interface NetworkResult<T : Any> {
    class Success<T : Any>(val data: T) : NetworkResult<T>
    class Failure<T : Any>(val code: Int, val message: String?) : NetworkResult<T>
    class Exception<T : Any>(val e: Throwable) : NetworkResult<T>
}


