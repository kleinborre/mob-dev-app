package com.sample.calorease.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sample.calorease.data.model.UserStats
import com.sample.calorease.domain.model.WeightGoal
import com.sample.calorease.domain.repository.LegacyCalorieRepository
import com.sample.calorease.domain.usecase.CalculatorUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsState(
    val userStats: UserStats? = null,
    val newWeight: String = "",
    val newWeightGoal: WeightGoal? = null,
    val showEditWeightDialog: Boolean = false,
    val showChangeGoalDialog: Boolean = false,
    val showLogoutConfirmDialog: Boolean = false,
    val showDeleteConfirmDialog: Boolean = false,
    val isLoading: Boolean = true,
    val shouldNavigateToStart: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: LegacyCalorieRepository,
    private val calculatorUseCase: CalculatorUseCase,
    private val sessionManager: com.sample.calorease.data.session.SessionManager
) : ViewModel() {
    
    private val _settingsState = MutableStateFlow(SettingsState())
    val settingsState: StateFlow<SettingsState> = _settingsState.asStateFlow()
    
    init {
        loadUserStats()
    }
    
    private fun loadUserStats() {
        viewModelScope.launch {
            val userStats = repository.getUserStatsOnce()
            _settingsState.value = _settingsState.value.copy(
                userStats = userStats,
                isLoading = false
            )
        }
    }
    
    // Edit Weight Dialog
    fun showEditWeightDialog() {
        val currentWeight = _settingsState.value.userStats?.weightKg?.toString() ?: ""
        _settingsState.value = _settingsState.value.copy(
            showEditWeightDialog = true,
            newWeight = currentWeight
        )
    }
    
    fun hideEditWeightDialog() {
        _settingsState.value = _settingsState.value.copy(showEditWeightDialog = false)
    }
    
    fun updateNewWeight(weight: String) {
        _settingsState.value = _settingsState.value.copy(newWeight = weight)
    }
    
    fun saveNewWeight() {
        viewModelScope.launch {
            val state = _settingsState.value
            val userStats = state.userStats ?: return@launch
            val newWeight = state.newWeight.toDoubleOrNull() ?: return@launch
            
            // Recalculate BMR, TDEE, and Goal Calories
            val bmr = calculatorUseCase.calculateBmr(
                weightKg = newWeight,
                heightCm = userStats.heightCm,
                age = userStats.age,
                gender = userStats.gender
            )
            
            val tdee = calculatorUseCase.calculateTdee(bmr, userStats.activityLevel)
            val goalCalories = calculatorUseCase.calculateGoalCalories(tdee, userStats.weightGoal)
            
            val updatedStats = userStats.copy(
                weightKg = newWeight,
                goalCalories = goalCalories
            )
            
            repository.updateUserStats(updatedStats)
            
            _settingsState.value = state.copy(
                userStats = updatedStats,
                showEditWeightDialog = false
            )
        }
    }
    
    // Change Goal Dialog
    fun showChangeGoalDialog() {
        _settingsState.value = _settingsState.value.copy(
            showChangeGoalDialog = true,
            newWeightGoal = _settingsState.value.userStats?.weightGoal
        )
    }
    
    fun hideChangeGoalDialog() {
        _settingsState.value = _settingsState.value.copy(showChangeGoalDialog = false)
    }
    
    fun updateNewWeightGoal(goal: WeightGoal) {
        _settingsState.value = _settingsState.value.copy(newWeightGoal = goal)
    }
    
    fun saveNewGoal() {
        viewModelScope.launch {
            val state = _settingsState.value
            val userStats = state.userStats ?: return@launch
            val newGoal = state.newWeightGoal ?: return@launch
            
            // Recalculate BMR, TDEE
            val bmr = calculatorUseCase.calculateBmr(
                weightKg = userStats.weightKg,
                heightCm = userStats.heightCm,
                age = userStats.age,
                gender = userStats.gender
            )
            
            val tdee = calculatorUseCase.calculateTdee(bmr, userStats.activityLevel)
            val goalCalories = calculatorUseCase.calculateGoalCalories(tdee, newGoal)
            
            val updatedStats = userStats.copy(
                weightGoal = newGoal,
                goalCalories = goalCalories
            )
            
            repository.updateUserStats(updatedStats)
            
            _settingsState.value = state.copy(
                userStats = updatedStats,
                showChangeGoalDialog = false
            )
        }
    }
    
    // Logout
    fun showLogoutConfirmDialog() {
        _settingsState.value = _settingsState.value.copy(showLogoutConfirmDialog = true)
    }

    fun hideLogoutConfirmDialog() {
        _settingsState.value = _settingsState.value.copy(showLogoutConfirmDialog = false)
    }

    fun logout() {
        viewModelScope.launch {
            sessionManager.clearSession()
            _settingsState.value = _settingsState.value.copy(
                showLogoutConfirmDialog = false,
                shouldNavigateToStart = true
            )
        }
    }

    // Delete Account
    fun showDeleteConfirmDialog() {
        _settingsState.value = _settingsState.value.copy(showDeleteConfirmDialog = true)
    }
    
    fun hideDeleteConfirmDialog() {
        _settingsState.value = _settingsState.value.copy(showDeleteConfirmDialog = false)
    }
    
    fun deleteAccount() {
        viewModelScope.launch {
            // Delete all user data
            repository.deleteAllUserStats()
            repository.deleteAllDailyEntries()
            
            _settingsState.value = _settingsState.value.copy(
                showDeleteConfirmDialog = false,
                shouldNavigateToStart = true
            )
        }
    }
    
    fun resetNavigationFlag() {
        _settingsState.value = _settingsState.value.copy(shouldNavigateToStart = false)
    }
}
