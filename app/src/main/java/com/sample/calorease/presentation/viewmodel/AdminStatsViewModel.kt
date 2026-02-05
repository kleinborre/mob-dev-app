package com.sample.calorease.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sample.calorease.domain.repository.LegacyCalorieRepository
import com.sample.calorease.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * ViewModel for Admin Statistics Page
 * Aggregates user data for admin dashboard
 */
data class AdminStatsState(
    val totalUsers: Int = 0,
    val activeUsers: Int = 0,
    val deactivatedUsers: Int = 0,
    val signupsByDate: List<Pair<String, Int>> = emptyList(), // Date to count
    val isLoading: Boolean = true
)

@HiltViewModel
class AdminStatsViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val legacyRepository: LegacyCalorieRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(AdminStatsState())
    val state: StateFlow<AdminStatsState> = _state.asStateFlow()
    
    init {
        // PHASE 2: Use Flow for real-time updates
        viewModelScope.launch {
            userRepository.getAllUsersFlow()
                .collect { users ->
                    updateStatsFromUsers(users)
                }
        }
    }
    
    private fun updateStatsFromUsers(allUsers: List<com.sample.calorease.data.local.entity.UserEntity>) {
        // Count active vs deactivated
        val activeCount = allUsers.count { it.accountStatus == "active" }
        val deactivatedCount = allUsers.count { it.accountStatus == "deactivated" }
        
        // Group users by signup date (last 7 days)
        val signupData = groupUsersByDate(allUsers)
        
        _state.value = _state.value.copy(
            totalUsers = allUsers.size,
            activeUsers = activeCount,
            deactivatedUsers = deactivatedCount,
            signupsByDate = signupData,
            isLoading = false
        )
        
        android.util.Log.d("AdminStatsViewModel", "✅ Stats updated: Total=${allUsers.size}, Active=$activeCount, Deactivated=$deactivatedCount")
    }
    
    private fun groupUsersByDate(users: List<com.sample.calorease.data.local.entity.UserEntity>): List<Pair<String, Int>> {
        // Get last 7 days
        val calendar = Calendar.getInstance()
        val today = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_YEAR, -6) // Go back 6 days (total 7 including today)
        
        val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
        val dayFormat = SimpleDateFormat("EEE", Locale.getDefault()) // Day of week (Mon, Tue, etc)
        
        val last7Days = mutableListOf<Pair<String, Int>>()
        
        repeat(7) { index ->
            val dayTimestamp = calendar.timeInMillis
            val dayLabel = dayFormat.format(Date(dayTimestamp))
            
            // Count users created on this day
            val usersOnDay = users.count { user ->
                val userDate = Calendar.getInstance().apply { timeInMillis = user.accountCreated }
                userDate.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
                userDate.get(Calendar.DAY_OF_YEAR) == calendar.get(Calendar.DAY_OF_YEAR)
            }
            
            last7Days.add(dayLabel to usersOnDay)
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        
        return last7Days
    }
}
