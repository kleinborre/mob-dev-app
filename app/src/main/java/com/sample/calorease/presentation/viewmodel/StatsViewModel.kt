package com.sample.calorease.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sample.calorease.data.session.SessionManager
import com.sample.calorease.domain.repository.CalorieRepository
import com.sample.calorease.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class DayCalorieData(
    val dayLabel: String,
    val calories: Int
)

data class StatsState(
    val weekData: List<DayCalorieData> = emptyList(),
    val dailyGoal: Int = 0,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val calorieRepository: CalorieRepository,
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager,
    private val legacyRepository: com.sample.calorease.domain.repository.LegacyCalorieRepository  // ✅ PHASE 7
) : ViewModel() {
    
    private val _statsState = MutableStateFlow(StatsState())
    val statsState: StateFlow<StatsState> = _statsState.asStateFlow()
    
    init {
        loadWeekData()
    }
    
    private fun loadWeekData() {
        viewModelScope.launch {
            _statsState.value = _statsState.value.copy(isLoading = true)
            
            try {
                val userId = sessionManager.getUserId() ?: return@launch
                
                // ✅ PHASE 7 FIX: Get user's daily goal from user_stats (not users table)
                // This matches DashboardViewModel logic
                val userStatsResult = legacyRepository.getUserStats(userId)
                val dailyGoal = userStatsResult?.goalCalories?.toInt() ?: 2000
                
                android.util.Log.d("StatsViewModel", "📊 Loading stats for userId=$userId, dailyGoal=$dailyGoal")
                
                // Get last 7 days of data
                val weekData = mutableListOf<DayCalorieData>()
                val calendar = Calendar.getInstance()
                val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
                
                for (i in 6 downTo 0) {
                    calendar.time = Date()
                    calendar.add(Calendar.DAY_OF_YEAR, -i)
                    
                    // Reset to start of day
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    
                    val dayLabel = dayFormat.format(calendar.time)
                    val dateMillis = calendar.timeInMillis
                    
                    val entriesResult = calorieRepository.getDailyEntries(userId, dateMillis)
                    val totalCalories = entriesResult.getOrNull()
                        ?.sumOf { it.calories }
                        ?: 0
                    
                    weekData.add(DayCalorieData(dayLabel, totalCalories))
                }
                
                _statsState.value = StatsState(
                    weekData = weekData,
                    dailyGoal = dailyGoal,
                    isLoading = false,
                    error = null
                )
                
            } catch (e: Exception) {
                _statsState.value = _statsState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load stats"
                )
            }
        }
    }
    
    fun refreshData() {
        loadWeekData()
    }
}
