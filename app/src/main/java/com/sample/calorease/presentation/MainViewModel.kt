package com.sample.calorease.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sample.calorease.data.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _startDestination = MutableStateFlow<String?>(null)
    val startDestination: StateFlow<String?> = _startDestination.asStateFlow()

    init {
        determineStartDestination()
    }

    private fun determineStartDestination() {
        viewModelScope.launch {
            val isLoggedIn = sessionManager.isLoggedIn()
            _startDestination.value = if (isLoggedIn) {
                "dashboard"  // User is logged in
            } else {
                "getting_started"  // New user or logged out
            }
        }
    }
}
