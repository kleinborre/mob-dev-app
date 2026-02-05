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
    val adminAccess: Boolean = false,  // ✅ Phase 4: Admin access flag
    val newWeight: String = "",
    val newWeightGoal: WeightGoal? = null,
    val showEditWeightDialog: Boolean = false,
    val showWeightConfirmDialog: Boolean = false,  // ✅ NEW: Edit Weight confirmation
    val showChangeGoalDialog: Boolean = false,
    val showGoalConfirmDialog: Boolean = false,  // ✅ Double confirmation for goal
    val showLogoutConfirmDialog: Boolean = false,
    val showDeleteConfirmDialog: Boolean = false,
    val showDeleteFinalWarningDialog: Boolean = false,  // PHASE 3: Second delete confirmation
    val isLoading: Boolean = true,
    val shouldNavigateToStart: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: LegacyCalorieRepository,
    private val calculatorUseCase: CalculatorUseCase,
    val sessionManager: com.sample.calorease.data.session.SessionManager,  // PHASE 3: Public for Switch to Admin
    private val userRepository: com.sample.calorease.domain.repository.UserRepository  // ✅ Phase 4: For adminAccess
) : ViewModel() {
    
    private val _settingsState = MutableStateFlow(SettingsState())
    val settingsState: StateFlow<SettingsState> = _settingsState.asStateFlow()
    
    init {
        loadUserStats()
    }
    
    private fun loadUserStats() {
        viewModelScope.launch {
            // Get the logged-in user's ID from session
            val userId = sessionManager.getUserId() ?: run {
                android.util.Log.e("SettingsViewModel", "No user ID in session! Cannot load stats.")
                _settingsState.value = _settingsState.value.copy(
                    userStats = null,
                    isLoading = false
                )
                return@launch
            }
            
            // Load UserStats by userId
            val userStats = repository.getUserStats(userId)
            
            // ✅ Phase 4: Load admin access from UserEntity
            val adminAccess = userRepository.getUserById(userId).getOrNull()?.adminAccess ?: false
            
            _settingsState.value = _settingsState.value.copy(
                userStats = userStats,
                adminAccess = adminAccess,
                isLoading = false
            )
            
            android.util.Log.d("SettingsViewModel", "Loaded UserStats for userId=$userId: ${userStats != null}, adminAccess=$adminAccess")
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
    
    // ✅ STEP 1: Show weight change confirmation
    fun requestWeightChange() {
        _settingsState.value = _settingsState.value.copy(
            showEditWeightDialog = false,
            showWeightConfirmDialog = true
        )
    }
    
    fun hideWeightConfirmDialog() {
        _settingsState.value = _settingsState.value.copy(
            showWeightConfirmDialog = false,
            showEditWeightDialog = true  // Back to input
        )
    }
    
    // ✅ STEP 2: Actually save weight
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
            
            // ✅ FIX #4: AWAIT delete completion before updating UI
            // This ensures food logs are deleted BEFORE Dashboard/Stats refresh
            android.util.Log.d("SettingsViewModel", "🗑️ Deleting progress for userId=${userStats.userId}...")
            deleteUserProgress(userStats.userId)  // Suspends until complete
            android.util.Log.d("SettingsViewModel", "✅ Progress deleted, now updating UI state")
            
            _settingsState.value = state.copy(
                userStats = updatedStats,
                showEditWeightDialog = false,
                showWeightConfirmDialog = false
            )
            
            android.util.Log.d("SettingsViewModel", "✅ Weight updated to $newWeight kg, UI refreshed")
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
    
    // ✅ STEP 1: Show confirmation (first dialog → second dialog)
    fun requestGoalChange() {
        _settingsState.value = _settingsState.value.copy(
            showChangeGoalDialog = false,
            showGoalConfirmDialog = true  // Show second confirmation
        )
    }
    
    fun hideGoalConfirmDialog() {
        _settingsState.value = _settingsState.value.copy(
            showGoalConfirmDialog = false,
            showChangeGoalDialog = true  // Back to goal selection
        )
    }
    
    // ✅ STEP 2: Actually save and DELETE PROGRESS
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
            
            // ✅ FIX #4: AWAIT delete completion before updating UI
            android.util.Log.d("SettingsViewModel", "🗑️ Deleting progress for userId=${userStats.userId}...")
            deleteUserProgress(userStats.userId)  // Suspends until complete
            android.util.Log.d("SettingsViewModel", "✅ Progress deleted, now updating UI state")
            
            _settingsState.value = state.copy(
                userStats = updatedStats,
                showChangeGoalDialog = false,
                showGoalConfirmDialog = false
            )
            
            android.util.Log.d("SettingsViewModel", "✅ Goal changed to $newGoal, UI refreshed!")
        }
    }
    
    // ✅ Delete all DailyEntryEntity records for user
    private suspend fun deleteUserProgress(userId: Int) {
        try {
            repository.deleteAllDailyEntriesForUser(userId)
            android.util.Log.d("SettingsViewModel", "🗑️ Deleted all daily entries for userId=$userId")
        } catch (e: Exception) {
            android.util.Log.e("SettingsViewModel", "Error deleting progress", e)
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
    
    // PHASE 3: First confirmation → Second confirmation
    fun confirmFirstDelete() {
        _settingsState.value = _settingsState.value.copy(
            showDeleteConfirmDialog = false,
            showDeleteFinalWarningDialog = true  // Show second warning
        )
    }
    
    fun hideFinalWarningDialog() {
        _settingsState.value = _settingsState.value.copy(showDeleteFinalWarningDialog = false)
    }
    
    fun deleteAccount() {
        viewModelScope.launch {
            // PHASE 2: Deactivate account instead of deleting data
            // This marks account_status as "deactivated" and triggers Flow update for admin stats
            val userId = sessionManager.getUserId()
            userId?.let {
                userRepository.deactivateAccount(it)
                android.util.Log.d("SettingsViewModel", "✅ Account deactivated (userId=$it)")
            }
            
            // PHASE 3: Save deletion success flag for login screen notification
            sessionManager.saveAccountDeletionSuccess(true)
            
            // Clear session and navigate to start
            sessionManager.clearSession()
            
            _settingsState.value = _settingsState.value.copy(
                showDeleteFinalWarningDialog = false,  // PHASE 3: Close second dialog
                shouldNavigateToStart = true
            )
        }
    }
    
    fun resetNavigationFlag() {
        _settingsState.value = _settingsState.value.copy(shouldNavigateToStart = false)
    }
    
    /**
     * PHASE 3: Save last dashboard mode to admin
     */
    fun saveAndSwitchToAdmin() {
        viewModelScope.launch {
            sessionManager.saveLastDashboardMode("admin")
        }
    }
}
