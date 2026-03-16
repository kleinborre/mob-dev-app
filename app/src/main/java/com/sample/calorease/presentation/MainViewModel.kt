package com.sample.calorease.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sample.calorease.data.session.SessionManager
import com.sample.calorease.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val repository: com.sample.calorease.domain.repository.LegacyCalorieRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _startDestination = MutableStateFlow<String?>(null)
    val startDestination: StateFlow<String?> = _startDestination.asStateFlow()

    init {
        determineStartDestination()
    }

    private fun determineStartDestination() {
        viewModelScope.launch {
            // BUGFIX Sprint 4 Phase 6: Wipe corrupt DataStore auto-backup caches on fresh installs before anything else
            sessionManager.checkInstallState()
            
            // Phase 2: Check if user has ever logged in before
            val hasLoggedInBefore = sessionManager.hasEverLoggedIn()
            val isCurrentlyLoggedIn = sessionManager.isLoggedIn()
            
            android.util.Log.d("MainViewModel", "hasLoggedInBefore=$hasLoggedInBefore, isCurrentlyLoggedIn=$isCurrentlyLoggedIn")
            
            _startDestination.value = when {
                // User is currently logged in - check onboarding status
                isCurrentlyLoggedIn -> {
                    val userId = sessionManager.getUserId() ?: 0
                    
                    android.util.Log.d("MainViewModel", " userId from session = $userId")
                    
                    if (userId > 0) {
                        // Use userRepository (direct DAO path) — never returns null due to stubs
                        val userStats = userRepository.getUserStats(userId)
                        val onboardingCompleted = userStats?.onboardingCompleted ?: false
                        
                        android.util.Log.d("MainViewModel", " onboardingCompleted=$onboardingCompleted")
                        
                        if (onboardingCompleted) {
                            // PHASE 3: Check user role and last dashboard mode
                            val userRole = sessionManager.getRole()
                            val lastMode = sessionManager.getLastDashboardMode()
                            
                            android.util.Log.d("MainViewModel", "Onboarding complete -> Role=$userRole, LastMode=$lastMode")
                            
                            // If user is admin and last mode was admin, go to admin dashboard
                            if (userRole == "admin" && lastMode == "admin") {
                                android.util.Log.d("MainViewModel", "Admin returning to admin dashboard")
                                "admin_stats"
                            } else {
                                android.util.Log.d("MainViewModel", "Going to user dashboard")
                                "dashboard"
                            }
                        } else {
                            // User with incomplete onboarding should NOT have session
                            android.util.Log.d("MainViewModel", "Onboarding incomplete but session exists - clearing and forcing re-login")
                            sessionManager.clearSession()
                            // Return to login since they've logged in before
                            if (hasLoggedInBefore) "login" else "getting_started"
                        }
                    } else {
                        android.util.Log.d("MainViewModel", "No valid userId - clearing session")
                        sessionManager.clearSession()
                        // Return to login since they've logged in before
                        if (hasLoggedInBefore) "login" else "getting_started"
                    }
                }
                
                // User not logged in, but has logged in before -> Login screen
                hasLoggedInBefore -> {
                    android.util.Log.d("MainViewModel", "Returning user -> login")
                    "login"
                }
                
                // First-time user -> Getting Started (intro)
                else -> {
                    android.util.Log.d("MainViewModel", " First-time user -> getting_started")
                    "getting_started"
                }
            }
            
            android.util.Log.d("MainViewModel", " Final destination: ${_startDestination.value}")
        }
    }
}
