package com.brian.weather.presentation.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.brian.weather.data.local.WeatherDao
import com.brian.weather.data.local.WeatherEntity
import com.brian.weather.data.mapper.toDomainModel
import com.brian.weather.data.remote.dto.asDatabaseModel
import com.brian.weather.data.remote.NetworkResult
import com.brian.weather.repository.WeatherRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch


/**
 * [ViewModel] to provide data to the  [AddWeatherLocationFragment] and allow for interaction the the [WeatherDao]
 */

// Pass an application as a parameter to the viewmodel constructor which is the context passed to the singleton database object
class AddWeatherLocationViewModel(
    private val weatherRepository: WeatherRepository,
    private val weatherDao: WeatherDao,
    application: Application) :
    AndroidViewModel(application) {

    private val queryFlow = MutableSharedFlow<String>(1, 1, BufferOverflow.DROP_OLDEST)

    private var searchJob: Job? = null


    // sort counter for database entries

    /**
     * If database is empty, initial sort value is 1
     * If not empty, find last entry in database, increment sort value by 1
     */
    private fun getLastEntrySortValue(): Int {
        var dbSortOrderValue = 1
        if (!weatherDao.isEmpty()) {
            dbSortOrderValue = weatherDao.selectLastEntry().sortOrder + 1
        }
        return dbSortOrderValue
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val getSearchResults: StateFlow<SearchViewData> =
        queryFlow
            .flatMapLatest { currentQuery ->
                flow {
                    when (val response = weatherRepository.getSearchResults(currentQuery)) {
                        is NetworkResult.Success -> {
                            val newSearchResults =
                                response.data.map { it.toDomainModel() }
                                    .map { searchDomainObject ->
                                        searchDomainObject.name + "," + " " + searchDomainObject.region
                                    }
                            emit(SearchViewData.Done(newSearchResults))
                        }
                        is NetworkResult.Failure -> {
                            emit(
                                SearchViewData.Error(
                                    code = response.code,
                                    message = response.message
                                )
                            )
                        }
                        is NetworkResult.Exception -> {
                            emit(
                                SearchViewData.Error(
                                    code = response.e.hashCode(),
                                    message = response.e.message
                                )
                            )
                        }
                    }
                }
            }.stateIn(viewModelScope, SharingStarted.Lazily, SearchViewData.Loading)


    fun setQuery(currentQuery: String) {
        /**
         * This will cancel the job every time the query is changed in the search field, if a character
         * is not typed in 500ms, the queryflow will emit its value. This prevents the API from being
         * called on every character being typed in the search field
         */
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(500)
            queryFlow.tryEmit(currentQuery)
        }

    }

    fun clearQueryResults() {
        queryFlow.tryEmit("")
    }

    suspend fun storeNetworkDataInDatabase(zipcode: String): Boolean {
        /**
         * This runs on a background thread by default so any value modified within this scoupe cannot
         * be returned outside of the scope
         */

        val networkError: Boolean =
            when (val response = weatherRepository.getWeather(zipcode)) {
                is NetworkResult.Success -> {
                    weatherDao.insert(
                        response.data.asDatabaseModel(
                            zipcode,
                            getLastEntrySortValue()
                        )
                    )
                    true
                }
                is NetworkResult.Failure -> false
                is NetworkResult.Exception -> false
            }
        return networkError

    }

    fun getWeatherByZipcode(location: String): WeatherEntity {
        return weatherDao.getWeatherByLocation(location)
    }

    fun updateWeather(
        id: Long,
        name: String,
        zipcode: String,
        sortOrder: Int
    ) {
        val weatherEntity = WeatherEntity(
            id = id,
            cityName = name,
            zipCode = zipcode,
            sortOrder = sortOrder
        )
        viewModelScope.launch(Dispatchers.IO) {
            // call the DAO method to update a weather object to the database here
            weatherDao.insert(weatherEntity)
        }
    }


// create a view model factory that takes a WeatherDao as a property and
//  creates a WeatherViewModel

    class AddWeatherLocationViewModelFactory(
        private val weatherRepository: WeatherRepository,
        private val weatherDao: WeatherDao,
        val app: Application
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AddWeatherLocationViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return AddWeatherLocationViewModel(weatherRepository, weatherDao, app) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

}

sealed class SearchViewData {
    object Loading : SearchViewData()
    data class Error(val code: Int, val message: String?) : SearchViewData()
    data class Done(val searchResults: List<String>) : SearchViewData()
}