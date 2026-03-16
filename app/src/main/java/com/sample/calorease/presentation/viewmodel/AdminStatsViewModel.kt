package com.sample.calorease.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sample.calorease.data.remote.FirestoreService
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
    private val firestoreService: FirestoreService
) : ViewModel() {
    
    private val _state = MutableStateFlow(AdminStatsState())
    val state: StateFlow<AdminStatsState> = _state.asStateFlow()
    
    init {
        // Sprint 4 Phase 7.7: Replace one-shot fetch with live Firestore snapshot observation.
        // Stats now auto-update whenever any user document changes (deactivation, new signup, etc.)
        startObservingStats()
    }

    private fun startObservingStats() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                firestoreService.observeUsers().collect { remoteUsers ->
                    val mappedUsers = remoteUsers.map { dto ->
                        com.sample.calorease.data.local.entity.UserEntity(
                            userId = dto.userId,
                            email = dto.email,
                            password = "",
                            googleId = dto.googleId,
                            isEmailVerified = dto.isEmailVerified,
                            nickname = dto.nickname,
                            role = dto.role,
                            isActive = dto.isActive,
                            accountStatus = dto.accountStatus,
                            adminAccess = dto.adminAccess,
                            isSuperAdmin = dto.isSuperAdmin,
                            accountCreated = dto.accountCreated,
                            gender = dto.gender,
                            height = dto.height,
                            weight = dto.weight,
                            age = dto.age,
                            activityLevel = dto.activityLevel,
                            targetWeight = dto.targetWeight,
                            goalType = dto.goalType,
                            bmr = dto.bmr,
                            tdee = dto.tdee,
                            lastUpdated = dto.lastUpdated
                        )
                    }
                    updateStatsFromUsers(mappedUsers)
                }
            } catch (e: Exception) {
                android.util.Log.e("AdminStatsViewModel", "Realtime stats observation error: ${e.message}")
                // Fallback to one-shot
                loadGlobalStats()
            }
        }
    }

    fun loadGlobalStats() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val remoteUsers = firestoreService.getAllUsers()
                val mappedUsers = remoteUsers.map { dto ->
                    com.sample.calorease.data.local.entity.UserEntity(
                        userId = dto.userId,
                        email = dto.email,
                        password = "",
                        googleId = dto.googleId,
                        isEmailVerified = dto.isEmailVerified,
                        nickname = dto.nickname,
                        role = dto.role,
                        isActive = dto.isActive,
                        accountStatus = dto.accountStatus,
                        adminAccess = dto.adminAccess,
                        isSuperAdmin = dto.isSuperAdmin,
                        accountCreated = dto.accountCreated,
                        gender = dto.gender,
                        height = dto.height,
                        weight = dto.weight,
                        age = dto.age,
                        activityLevel = dto.activityLevel,
                        targetWeight = dto.targetWeight,
                        goalType = dto.goalType,
                        bmr = dto.bmr,
                        tdee = dto.tdee,
                        lastUpdated = dto.lastUpdated
                    )
                }
                updateStatsFromUsers(mappedUsers)
            } catch (e: Exception) {
                android.util.Log.e("AdminStatsViewModel", "Failed to fetch global stats: ${e.message}")
                _state.value = _state.value.copy(isLoading = false)
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
        
        android.util.Log.d("AdminStatsViewModel", "Stats updated: Total=${allUsers.size}, Active=$activeCount, Deactivated=$deactivatedCount")
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
