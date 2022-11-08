package com.brian.weathercompose.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


class MainViewModel: ViewModel() {
/*
    private val _title = MutableStateFlow<String>("Weather")
    val title: StateFlow<String>
        get() = _title
    fun updateActionBarTitle(title: String) {
        _title.value = title
    }

 */

    private val _title = MutableLiveData<String>("Weather")
    val title: LiveData<String>
        get() = _title
    fun updateActionBarTitle(title: String) {
        _title.value = title
    }
}