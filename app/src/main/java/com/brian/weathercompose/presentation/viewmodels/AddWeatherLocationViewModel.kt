package com.brian.weathercompose.presentation.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.brian.weathercompose.data.local.WeatherDao
import com.brian.weathercompose.data.local.WeatherEntity
import com.brian.weathercompose.data.mapper.toDomainModel
import com.brian.weathercompose.data.remote.dto.asDatabaseModel
import com.brian.weathercompose.data.remote.ApiResponse
import com.brian.weathercompose.repository.WeatherRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
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

    private val refreshFlow = MutableSharedFlow<Unit>(1, 1, BufferOverflow.DROP_OLDEST)
        .apply {
            tryEmit(Unit)
        }

    private val queryFlow = MutableSharedFlow<String>(1, 1, BufferOverflow.DROP_OLDEST)


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
            .flatMapLatest { location ->
                flow {
                    when (val response = weatherRepository.getSearchResults(location)) {
                        is ApiResponse.Success -> {
                            val newSearchResults =
                                response.data.map { it.toDomainModel() }
                                    .map { searchDomainObject ->
                                        searchDomainObject.name + "," + " " + searchDomainObject.region
                                    }
                            emit(SearchViewData.Done(newSearchResults))
                        }
                        is ApiResponse.Failure -> {
                            emit(
                                SearchViewData.Error(
                                    code = response.code,
                                    message = response.message
                                )
                            )
                        }
                        is ApiResponse.Exception -> {
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


    fun setQuery(string: String) {
        queryFlow.tryEmit(string)
    }

    fun clearQueryResults() {
        queryFlow.tryEmit("")
    }

    fun refreshFlow() {
        refreshFlow.tryEmit(Unit)
    }

    suspend fun storeNetworkDataInDatabase(zipcode: String): Boolean {
        /**
         * This runs on a background thread by default so any value modified within this scoupe cannot
         * be returned outside of the scope
         */

        val networkError: Boolean =
            when (val response = weatherRepository.getWeatherWithErrorHandling(zipcode)) {
                is ApiResponse.Success -> {
                    weatherDao.insert(
                        response.data.asDatabaseModel(
                            zipcode,
                            getLastEntrySortValue()
                        )
                    )
                    true
                }
                is ApiResponse.Failure -> false
                is ApiResponse.Exception -> false
            }
        return networkError

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

    fun deleteWeather(weatherEntity: WeatherEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            // call the DAO method to delete a weather object to the database here
            weatherDao.delete(weatherEntity)
        }
    }

    fun isValidEntry(zipcode: String): Boolean {
        return zipcode.isNotBlank()
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