package com.sample.calorease.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sample.calorease.data.local.entity.DailyEntryEntity
import com.sample.calorease.data.local.entity.UserEntity
import com.sample.calorease.data.session.SessionManager
import com.sample.calorease.domain.repository.CalorieRepository
import com.sample.calorease.domain.repository.UserRepository
import com.sample.calorease.domain.usecase.CalculatorUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class DashboardState(
    val user: UserEntity? = null,
    val foodEntries: List<DailyEntryEntity> = emptyList(),
    val nickname: String = "",
    val goalCalories: Int = 0,
    val consumedCalories: Int = 0,
    val remainingCalories: Int = 0,
    val progress: Float = 0f,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val calorieRepository: CalorieRepository,
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager,
    private val calculatorUseCase: CalculatorUseCase,
    private val legacyRepository: com.sample.calorease.domain.repository.LegacyCalorieRepository  // ✅ NEW: Load user_stats
) : ViewModel() {
    
    private val _dashboardState = MutableStateFlow(DashboardState())
    val dashboardState: StateFlow<DashboardState> = _dashboardState.asStateFlow()
    
    init {
        loadDashboardData()
    }
    
    private fun loadDashboardData() {
        viewModelScope.launch {
            _dashboardState.value = _dashboardState.value.copy(isLoading = true)
            
            try {
                // Get current user ID from session
                val userId = sessionManager.getUserId() ?: run {
                    _dashboardState.value = _dashboardState.value.copy(
                        isLoading = false,
                        error = "User not logged in"
                    )
                    return@launch
                }
                
                // ✅ CRITICAL FIX: Load from user_stats instead of users  
                // Onboarding saves to user_stats, so we need to load from there
                val userStats = legacyRepository.getUserStats(userId)
                
                // Also load UserEntity for backward compatibility
                val userResult = userRepository.getUserById(userId)
                val user = userResult.getOrNull() ?: run {
                    _dashboardState.value = _dashboardState.value.copy(
                        isLoading = false,
                        error = "Failed to load user data"
                    )
                    return@launch
                }
                
                // Get today's timestamp (start of day)
                val todayTimestamp = getTodayTimestamp()
                
                // Load today's food entries
                val entriesResult = calorieRepository.getDailyEntries(userId, todayTimestamp)
                val entries = entriesResult.getOrNull() ?: emptyList()
                
                // Calculate total consumed calories
                val totalCaloriesResult = calorieRepository.getTotalCalories(userId, todayTimestamp)
                val consumed = totalCaloriesResult.getOrNull() ?: 0
                
                // ✅ CRITICAL FIX: Use goalCalories from user_stats (from onboarding)
                // Don't recalculate - use what was saved during onboarding
                val dailyGoal = userStats?.goalCalories?.toInt() ?: 0
                
                val remaining = dailyGoal - consumed
                val progress = if (dailyGoal > 0) {
                    (consumed.toFloat() / dailyGoal.toFloat()).coerceIn(0f, 1f)
                } else {
                    0f
                }
                
                _dashboardState.value = DashboardState(
                    user = user,
                    foodEntries = entries,
                    nickname = userStats?.nickname ?: userStats?.firstName ?: "User",  // ✅ Use nickname from user_stats
                    goalCalories = dailyGoal,  // ✅ From onboarding
                    consumedCalories = consumed,
                    remainingCalories = remaining,
                    progress = progress,
                    isLoading = false,
                    error = null
                )
            } catch (e: Exception) {
                _dashboardState.value = _dashboardState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error"
                )
            }
        }
    }
    
    fun addFoodEntry(foodName: String, calories: Int, mealType: String) {
        viewModelScope.launch {
            try {
                val userId = sessionManager.getUserId() ?: return@launch
                val todayTimestamp = getTodayTimestamp()
                
                val entry = DailyEntryEntity(
                    userId = userId,
                    date = todayTimestamp,
                    foodName = foodName,
                    calories = calories,
                    mealType = mealType
                )
                
                calorieRepository.addDailyEntry(entry)
                
                // Refresh dashboard
                loadDashboardData()
            } catch (e: Exception) {
                _dashboardState.value = _dashboardState.value.copy(
                    error = "Failed to add entry: ${e.message}"
                )
            }
        }
    }
    
    fun deleteFoodEntry(entryId: Int) {
        viewModelScope.launch {
            try {
                calorieRepository.deleteDailyEntry(entryId)
                
                // Refresh dashboard
                loadDashboardData()
            } catch (e: Exception) {
                _dashboardState.value = _dashboardState.value.copy(
                    error = "Failed to delete entry: ${e.message}"
                )
            }
        }
    }
    
    // Update food entry
    fun updateFoodEntry(entry: DailyEntryEntity) {
        viewModelScope.launch {
            try {
                calorieRepository.updateDailyEntry(entry)
                
                // Refresh dashboard
                loadDashboardData()
            } catch (e: Exception) {
                _dashboardState.value = _dashboardState.value.copy(
                    error = "Failed to update entry: ${e.message}"
                )
            }
        }
    }
    
    fun refreshData() {
        loadDashboardData()
    }
    
    private fun getTodayTimestamp(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
}
