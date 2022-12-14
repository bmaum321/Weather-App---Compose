package com.brian.weather.util

object Constants {
    const val APIKEY = "759618142cff4efb89d192409221909"
    const val ERRORTEXT = "Cannot add location, check network connection or location"
    const val TRIGGER_AUTO_COMPLETE = 100
    const val AUTO_COMPLETE_DELAY: Long = 300
    const val TAG_OUTPUT = "Daily worker ran"
    const val rainIconUrl = "//cdn.weatherapi.com/weather/64x64/night/308.png"
    const val snowIconUrl = "//cdn.weatherapi.com/weather/64x64/night/338.png"
    const val BASE_URL = "https://api.weatherapi.com/v1/"
    const val CURRENT = "current.json?key=$APIKEY"
    const val FORECAST = "forecast.json?key=$APIKEY"
    const val SEARCH = "search.json?key=$APIKEY"

}
