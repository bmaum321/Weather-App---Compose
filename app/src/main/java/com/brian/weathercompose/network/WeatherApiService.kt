package com.brian.weathercompose.network

import com.brian.weathercompose.model.ForecastContainer
import com.brian.weathercompose.model.Search
import com.brian.weathercompose.util.Constants
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Converter
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * A retrofit service to fetch the weather data from the API
 */

private const val BASE_URL = "https://api.weatherapi.com/v1/"
private const val CURRENT = "current.json?key=${Constants.APIKEY}"
private const val FORECAST = "forecast.json?key=${Constants.APIKEY}"
private const val SEARCH = "search.json?key=${Constants.APIKEY}"


private val json = Json {
    ignoreUnknownKeys = true
}

// Configure retrofit to parse JSON and use coroutines
@OptIn(ExperimentalSerializationApi::class)
private val retrofit: Retrofit = Retrofit.Builder()
    .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
    .baseUrl(BASE_URL)
    .build()


interface WeatherApiService {
    @GET(CURRENT)
    suspend fun getWeather(
        @Query("q") zipcode: String
    ): WeatherContainer

    @GET(CURRENT)
    suspend fun getWeatherWithErrorHandling(
        @Query("q") zipcode: String
    ): Response<WeatherContainer>

    @GET(SEARCH)
    suspend fun locationSearch(
        @Query("Q") location: String
    ): Response<List<Search>>

    @GET(FORECAST)
    suspend fun getForecast(
        @Query("q") zipcode: String,
        @Query("days") days: Int = 7, // Maximum forecast days for free API is 3 days, have paid plan with 7 days
        @Query("alerts") alerts: String = "yes"
    ): Response<ForecastContainer>
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
sealed class ApiResponse<T : Any> {
    class Success<T : Any>(val data: T) : ApiResponse<T>()
    class Failure<T : Any>(val code: Int, val message: String?) : ApiResponse<T>()
    class Exception<T : Any>(val e: Throwable) : ApiResponse<T>()
}

/**
 * The handleApi function receives an executable lambda function, which returns a Retrofit response.
 * After executing the lambda function, the handleApi function returns ApiResponse.Success if the
 * response is successful and the body data is a non-null value.
 */

suspend fun <T : Any> handleApi(
    execute: suspend () -> Response<T>
): ApiResponse<T> {
    return try {
        val response = execute()
        val body = response.body()
        if (response.isSuccessful && body != null) {
            ApiResponse.Success(body)
        } else {
            ApiResponse.Failure(code = response.code(), message = response.message())
        }
    } catch (e: HttpException) {
        ApiResponse.Failure(code = e.code(), message = e.message())
    } catch (e: Throwable) {
        ApiResponse.Exception(e)
    }
}
