package com.brian.weathercompose.ui.viewmodels


import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainViewModel: ViewModel() {

    private val _title = MutableStateFlow<String>("Weather")
    val title = _title.asStateFlow()

    fun updateActionBarTitle(newTitle: String) {
        _title.value = newTitle
    }

}