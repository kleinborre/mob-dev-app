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
    val adminAccess: Boolean = false,
    val newWeight: String = "",
    val newWeightGoal: WeightGoal? = null,
    val showEditWeightDialog: Boolean = false,
    val showWeightConfirmDialog: Boolean = false,
    val showChangeGoalDialog: Boolean = false,
    val showGoalConfirmDialog: Boolean = false,
    val showLogoutConfirmDialog: Boolean = false,
    val showDeleteConfirmDialog: Boolean = false,
    val showDeleteFinalWarningDialog: Boolean = false,

    val shouldNavigateToStart: Boolean = false,
    val shouldNavigateToLogin: Boolean = false,
    val isLoading: Boolean = false,
    // Fix: removed isWeightSaving / isGoalSaving — they caused a frozen LOADING dialog.
    // Room writes are instant; success is shown via successMessage -> STATUS.SUCCESS (auto-dismisses).
    val isLoggingOut: Boolean = false,
    val isDeletingAccount: Boolean = false,
    val successMessage: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: LegacyCalorieRepository,
    private val calculatorUseCase: CalculatorUseCase,
    val sessionManager: com.sample.calorease.data.session.SessionManager,  // PHASE 3: Public for Switch to Admin
    private val userRepository: com.sample.calorease.domain.repository.UserRepository,  // Phase 4: For adminAccess
    private val syncScheduler: com.sample.calorease.domain.sync.SyncScheduler,
    private val firestoreService: com.sample.calorease.data.remote.FirestoreService
) : ViewModel() {
    
    private val _settingsState = MutableStateFlow(SettingsState())
    val settingsState: StateFlow<SettingsState> = _settingsState.asStateFlow()
    
    init {
        loadUserStats()
    }
    
    private fun loadUserStats() {
        viewModelScope.launch {
            val userId = sessionManager.getUserId() ?: run {
                android.util.Log.e("SettingsViewModel", "No user ID in session! Cannot load stats.")
                _settingsState.value = _settingsState.value.copy(userStats = null)
                return@launch
            }

            val userStats  = repository.getUserStats(userId)
            val adminAccess = userRepository.getUserById(userId).getOrNull()?.adminAccess ?: false

            _settingsState.value = _settingsState.value.copy(
                userStats   = userStats,
                adminAccess = adminAccess
            )
            android.util.Log.d("SettingsViewModel", "Loaded UserStats for userId=$userId: ${userStats != null}")
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
    
    // STEP 1: Show weight change confirmation
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
    
    fun saveNewWeight() {
        viewModelScope.launch {
            val state     = _settingsState.value
            val userStats  = state.userStats ?: return@launch
            val newWeight  = state.newWeight.toDoubleOrNull() ?: return@launch

            // Dismiss confirm dialog immediately — no loading state to avoid frozen dialog bug
            _settingsState.value = _settingsState.value.copy(showWeightConfirmDialog = false)

            val bmr         = calculatorUseCase.calculateBmr(newWeight, userStats.heightCm, userStats.age, userStats.gender)
            val tdee        = calculatorUseCase.calculateTdee(bmr, userStats.activityLevel)
            val goalCalories = calculatorUseCase.calculateGoalCalories(tdee, userStats.weightGoal)
            val updatedStats = userStats.copy(weightKg = newWeight, goalCalories = goalCalories)

            // 1. Always write to Room first (offline-safe, instant)
            repository.updateUserStats(updatedStats)

            // 2. Reflect changes in UI immediately
            _settingsState.value = _settingsState.value.copy(
                userStats      = updatedStats,
                successMessage = "Weight updated to ${String.format("%.1f", newWeight)} kg"
            )

            // 3. Best-effort Firestore push (fire-and-forget; WorkManager retries if offline)
            val email = sessionManager.getUserEmail() ?: ""
            if (email.isNotBlank()) {
                try { firestoreService.saveUserStats(email, mapToDto(updatedStats)) }
                catch (syncEx: Exception) {
                    android.util.Log.w("SettingsViewModel", "Offline: weight sync queued. ${syncEx.message}")
                }
            }
            try { syncScheduler.triggerImmediateSync() } catch (ignored: Exception) {}
            android.util.Log.d("SettingsViewModel", "Weight updated to $newWeight kg")
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
    
    // STEP 1: Show confirmation (first dialog -> second dialog)
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
    
    fun saveNewGoal() {
        viewModelScope.launch {
            val state     = _settingsState.value
            val userStats  = state.userStats ?: return@launch
            val newGoal    = state.newWeightGoal ?: return@launch

            // Dismiss confirm dialog immediately — no loading state to avoid frozen dialog bug
            _settingsState.value = _settingsState.value.copy(showGoalConfirmDialog = false)

            val bmr         = calculatorUseCase.calculateBmr(userStats.weightKg, userStats.heightCm, userStats.age, userStats.gender)
            val tdee        = calculatorUseCase.calculateTdee(bmr, userStats.activityLevel)
            val goalCalories = calculatorUseCase.calculateGoalCalories(tdee, newGoal)
            val updatedStats = userStats.copy(weightGoal = newGoal, goalCalories = goalCalories)

            // 1. Always write to Room first (offline-safe, instant)
            repository.updateUserStats(updatedStats)

            // 2. Reflect changes in UI immediately
            _settingsState.value = _settingsState.value.copy(
                userStats      = updatedStats,
                successMessage = "Goal updated successfully"
            )

            // 3. Best-effort Firestore push (fire-and-forget; WorkManager retries if offline)
            val email = sessionManager.getUserEmail() ?: ""
            if (email.isNotBlank()) {
                try { firestoreService.saveUserStats(email, mapToDto(updatedStats)) }
                catch (syncEx: Exception) {
                    android.util.Log.w("SettingsViewModel", "Offline: goal sync queued. ${syncEx.message}")
                }
            }
            try { syncScheduler.triggerImmediateSync() } catch (ignored: Exception) {}
            android.util.Log.d("SettingsViewModel", "Goal changed to $newGoal")
        }
    }

    fun clearSuccessMessage() {
        _settingsState.value = _settingsState.value.copy(successMessage = null)
    }
    
    // Delete all DailyEntryEntity records for user
    private suspend fun deleteUserProgress(userId: Int) {
        try {
            repository.deleteAllDailyEntriesForUser(userId)
            android.util.Log.d("SettingsViewModel", "Deleted all daily entries for userId=$userId")
        } catch (e: Exception) {
            android.util.Log.e("SettingsViewModel", "Error deleting progress", e)
        }
    }
    


    private fun mapToDto(stats: UserStats): com.sample.calorease.data.remote.dto.UserStatsDto {
        return com.sample.calorease.data.remote.dto.UserStatsDto(
            userId = stats.userId,
            firstName = stats.firstName,
            lastName = stats.lastName,
            nickname = stats.nickname,
            gender = stats.gender.name,
            heightCm = stats.heightCm,
            weightKg = stats.weightKg,
            age = stats.age,
            birthday = stats.birthday,
            activityLevel = stats.activityLevel.name,
            weightGoal = stats.weightGoal.name,
            targetWeightKg = stats.targetWeightKg,
            goalCalories = stats.goalCalories,
            bmiValue = stats.bmiValue,
            bmiStatus = stats.bmiStatus,
            idealWeight = stats.idealWeight,
            bmr = stats.bmr,
            tdee = stats.tdee,
            onboardingCompleted = stats.onboardingCompleted,
            currentOnboardingStep = stats.currentOnboardingStep
        )
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
            _settingsState.value = _settingsState.value.copy(
                isLoggingOut = true,
                showLogoutConfirmDialog = false
            )
            // Show loading animation briefly
            kotlinx.coroutines.delay(800)
            
            sessionManager.clearSession()

            // BUGFIX Sprint 4 Phase 6: Natively terminate sticky Android & Firebase Sessions so they don't leak "lr.ojkborre" data
            try {
                com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
            } catch (e: Exception) {
                // Silent catch
            }
            
            _settingsState.value = _settingsState.value.copy(
                isLoggingOut = false,
                successMessage = "Logged out successfully",
                shouldNavigateToLogin = true 
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
    
    // PHASE 3: First confirmation -> Second confirmation
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
            _settingsState.value = _settingsState.value.copy(
                isDeletingAccount = true,
                showDeleteFinalWarningDialog = false
            )
            
            // Show loading animation briefly
            kotlinx.coroutines.delay(800)
            
            // Deactivate account and mark success for login screen toast
            val userId = sessionManager.getUserId()
            userId?.let {
                userRepository.deactivateAccount(it)
                android.util.Log.d("SettingsViewModel", "Account deactivated locally (userId=$it)")
            }
            // Sprint 4 Phase 7.4.1: Massively sync to Firestore immediately before clearing the session so the cloud recognizes the deactivation.
            syncScheduler.triggerImmediateSync()
            
            sessionManager.saveAccountDeletionSuccess(true)
            sessionManager.clearSession()
            
            _settingsState.value = _settingsState.value.copy(
                isDeletingAccount = false,
                successMessage = "Account deleted successfully",
                shouldNavigateToStart = true
            )
        }
    }
    
    fun resetNavigationFlag() {
        _settingsState.value = _settingsState.value.copy(
            shouldNavigateToStart = false,
            shouldNavigateToLogin = false
        )
    }
    
    
    /**
     * PHASE 2 FIX: Switch to admin mode using injected SessionManager
     */
    fun switchToAdminMode(onNavigate: () -> Unit) {
        viewModelScope.launch {
            if (!com.sample.calorease.presentation.util.NetworkUtils.isNetworkAvailable(sessionManager.context)) {
                _settingsState.value = _settingsState.value.copy(
                    successMessage = "No network connection. Admin Mode requires internet access."
                )
                return@launch
            }
            sessionManager.saveLastDashboardMode("admin")
            android.util.Log.d("SettingsViewModel", "Saved lastDashboardMode = admin")
            onNavigate()
        }
    }
}
