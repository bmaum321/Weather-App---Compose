package com.brian.weathercompose.presentation.viewmodels


import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainViewModel: ViewModel() {

    var tempUnit = "Fahrenheit"
    private val _title = MutableStateFlow<String>("Weather")
    val title = _title.asStateFlow()

    fun updateActionBarTitle(newTitle: String) {
        _title.value = newTitle
    }

}