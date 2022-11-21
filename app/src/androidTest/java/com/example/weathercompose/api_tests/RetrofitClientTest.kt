package com.example.weathercompose.api_tests


import com.brian.weathercompose.data.remote.WeatherApi
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.runBlocking
import org.junit.Test


class RetrofitClientTest {
    @Test
    fun weatherApi_testGetWeather_successfulHttpResponse() = runBlocking{
        val service = WeatherApi.retrofitService
        val response = service.getWeatherWithErrorHandling("13088")
        assertTrue(response.errorBody() == null)
        assertTrue(response.code() == 200)
        assertTrue(response.body() != null)

    }

    @Test
    fun weatherApi_testGetWeather_unSuccessfulHttpResponse() = runBlocking{
        val service = WeatherApi.retrofitService
        val response = service.getWeatherWithErrorHandling("xxxx")
        assertTrue(response.errorBody() != null)
        assertTrue(response.code() == 400)
        assertTrue(response.body() == null)

    }

    @Test
    fun weatherApi_testGetForecast_SuccessfulHttpResponse() = runBlocking{
        val service = WeatherApi.retrofitService
        val response = service.getForecast("13088")
        assertTrue(response.errorBody() == null)
        assertTrue(response.code() == 200)
        assertTrue(response.body() != null)

    }

    @Test
    fun weatherApi_testGetSearch_SuccessfulHttpResponse() = runBlocking{
        val service = WeatherApi.retrofitService
        val response = service.locationSearch("13088")
        assertTrue(response.errorBody() == null)
        assertTrue(response.code() == 200)
        assertTrue(response.body() != null)

    }
}
