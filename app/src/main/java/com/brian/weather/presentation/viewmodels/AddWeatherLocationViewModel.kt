package com.brian.weather.presentation.viewmodels

import androidx.lifecycle.*
import com.brian.weather.data.local.WeatherDao
import com.brian.weather.data.remote.dto.asDatabaseModel
import com.brian.weather.data.remote.NetworkResult
import com.brian.weather.domain.usecase.CreateSearchStateUseCase
import com.brian.weather.repository.WeatherRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*


sealed class SearchState {
    object Loading : SearchState()
    data class Error(
        val code: Int,
        val message: String?
    ) : SearchState()

    data class Success(
        val searchResults: List<String>,
        val query: String = ""
    ) : SearchState()
}



sealed class AddWeatherScreenEvent {
    object ClearQuery: AddWeatherScreenEvent()
    data class SetQuery(val query: String): AddWeatherScreenEvent()
    data class SaveQueryInDatabase(val query: String): AddWeatherScreenEvent()
}

/**
 * [ViewModel] to provide data to the  [AddWeatherLocationFragment] and allow for interaction the the [WeatherDao]
 */

class AddWeatherLocationViewModel(
    private val weatherRepository: WeatherRepository,
    private val createSearchStateUseCase: CreateSearchStateUseCase,
) : ViewModel() {

    private val queryFlow = MutableSharedFlow<String>(1, 1, BufferOverflow.DROP_OLDEST)

    private var searchJob: Job? = null

    // sort counter for database entries

    /**
     * If database is empty, initial sort value is 1
     * If not empty, find last entry in database, increment sort value by 1
     */
    private fun getLastEntrySortValue(): Int {
        var dbSortOrderValue = 1
        if (!weatherRepository.isDbEmpty()) {
            dbSortOrderValue = (weatherRepository.selectLastEntryInDb()?.sortOrder ?: 0) + 1
        }
        return dbSortOrderValue
    }

    private suspend fun checkIfLocationAlreadyInDb(location: String): Boolean {
        val weather = weatherRepository.getWeatherByZipcode(location).firstOrNull()
        return weather != null
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    val getSearchResults: StateFlow<SearchState> =
        queryFlow
            .flatMapLatest { currentQuery ->
                flow {
                    emit(createSearchStateUseCase(currentQuery))
                }
            }.stateIn(viewModelScope, SharingStarted.Lazily, SearchState.Loading)


    private fun setSearchQuery(currentQuery: String) {
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

    fun clearSearchQuery() {
        queryFlow.tryEmit("")
    }

    private suspend fun storeNetworkDataInDatabase(zipcode: String): Boolean {
        /**
         * This runs on a background thread by default so any value modified within this scope cannot
         * be returned outside of the scope
         */

        val networkError: Boolean =
            when (val response = weatherRepository.getWeather(zipcode)) {
                is NetworkResult.Success -> {
                    if (!checkIfLocationAlreadyInDb(zipcode)) {
                        weatherRepository.insert(
                            response.data.asDatabaseModel(
                                zipcode,
                                getLastEntrySortValue()
                            )
                        )

                        false
                    } else true

                }
                is NetworkResult.Failure -> true
                is NetworkResult.Exception -> true
            }
        return networkError

    }

    fun onEvent(event: AddWeatherScreenEvent): Boolean {
        var insertError = false
        when(event) {
            is AddWeatherScreenEvent.ClearQuery -> {
                clearSearchQuery()
            }
            is AddWeatherScreenEvent.SaveQueryInDatabase -> {
                viewModelScope.launch(Dispatchers.IO) {
                    if (storeNetworkDataInDatabase(event.query)) insertError = true
                }
            }
            is AddWeatherScreenEvent.SetQuery -> {
                setSearchQuery(event.query)
            }
        }
        return insertError
    }


/*

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

 */

}

