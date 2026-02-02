package com.sample.calorease.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sample.calorease.data.model.DailyEntry
import com.sample.calorease.data.model.UserStats
import com.sample.calorease.domain.repository.LegacyCalorieRepository
import com.sample.calorease.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardState(
    val userStats: UserStats? = null,
    val todayEntry: DailyEntry? = null,
    val nickname: String = "",
    val goalCalories: Double = 0.0,
    val consumedCalories: Double = 0.0,
    val remainingCalories: Double = 0.0,
    val progress: Float = 0f,
    val isLoading: Boolean = true
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: LegacyCalorieRepository
) : ViewModel() {
    
    private val _dashboardState = MutableStateFlow(DashboardState())
    val dashboardState: StateFlow<DashboardState> = _dashboardState.asStateFlow()
    
    init {
        loadDashboardData()
    }
    
    private fun loadDashboardData() {
        viewModelScope.launch {
            _dashboardState.value = _dashboardState.value.copy(isLoading = true)
            
            // Load user stats
            val userStats = repository.getUserStatsOnce()
            
            // Load or create today's entry
            val today = DateUtils.getTodayString()
            var todayEntry = repository.getDailyEntryByDate(today)
            
            if (todayEntry == null && userStats != null) {
                // Create today's entry
                todayEntry = DailyEntry(
                    date = today,
                    caloriesConsumed = 0.0,
                    exerciseCalories = 0.0,
                    weightRecorded = userStats.weightKg
                )
                repository.insertDailyEntry(todayEntry)
            }
            
            val goalCalories = userStats?.goalCalories ?: 2000.0
            val consumed = todayEntry?.caloriesConsumed ?: 0.0
            val remaining = goalCalories - consumed
            val progress = (consumed / goalCalories).toFloat().coerceIn(0f, 1f)
            
            _dashboardState.value = DashboardState(
                userStats = userStats,
                todayEntry = todayEntry,
                nickname = userStats?.nickname ?: "User",
                goalCalories = goalCalories,
                consumedCalories = consumed,
                remainingCalories = remaining,
                progress = progress,
                isLoading = false
            )
        }
    }
    
    fun updateCalories(calories: Double) {
        viewModelScope.launch {
            val state = _dashboardState.value
            val entry = state.todayEntry ?: return@launch
            
            val newConsumed = calories
            val updatedEntry = entry.copy(caloriesConsumed = newConsumed)
            
            repository.updateDailyEntry(updatedEntry)
            
            val remaining = state.goalCalories - newConsumed
            val progress = (newConsumed / state.goalCalories).toFloat().coerceIn(0f, 1f)
            
            _dashboardState.value = state.copy(
                todayEntry = updatedEntry,
                consumedCalories = newConsumed,
                remainingCalories = remaining,
                progress = progress
            )
        }
    }
    
    fun refreshData() {
        loadDashboardData()
    }
}
