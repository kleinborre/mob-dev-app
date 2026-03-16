package com.sample.calorease.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sample.calorease.data.local.entity.DailyEntryEntity
import com.sample.calorease.data.local.entity.UserEntity
import com.sample.calorease.data.session.SessionManager
import com.sample.calorease.domain.repository.CalorieRepository
import com.sample.calorease.domain.repository.LegacyCalorieRepository
import com.sample.calorease.domain.repository.UserRepository
import com.sample.calorease.domain.usecase.CalculatorUseCase
import com.sample.calorease.domain.sync.SyncScheduler
import com.sample.calorease.presentation.ui.UiEvent
import com.sample.calorease.data.remote.FirestoreService
import com.sample.calorease.data.remote.dto.DailyEntryDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
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
    val isRefreshing: Boolean = false,
    val error: String? = null,

    // Add entry dialog persistence
    val showAddDialog: Boolean = false,
    val tempFoodName: String = "",
    val tempCalories: String = "",
    val tempMealType: String = "Breakfast"
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val calorieRepository: CalorieRepository,
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager,
    private val calculatorUseCase: CalculatorUseCase,
    private val legacyRepository: LegacyCalorieRepository,
    private val syncScheduler: SyncScheduler,
    private val firestoreService: FirestoreService
) : ViewModel() {

    private val _dashboardState = MutableStateFlow(DashboardState())
    val dashboardState: StateFlow<DashboardState> = _dashboardState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()

    // ── Today's date key (start-of-day timestamp) ──────────────────────────────
    private val todayTimestamp: Long get() = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0);      set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    init {
        observeDashboardRealTime()
    }

    /**
     * Kick off real-time observers for food entries and calorie totals.
     * The Flow chain automatically updates the UI whenever Room data changes —
     * no manual reload calls needed after add/delete/update.
     */
    private fun observeDashboardRealTime() {
        viewModelScope.launch {
            val userId = sessionManager.getUserId() ?: run {
                _dashboardState.value = _dashboardState.value.copy(
                    isLoading = false,
                    error = "User not logged in"
                )
                return@launch
            }

            // Load static data once (user profile + goal)
            val userResult = userRepository.getUserById(userId)
            val user = userResult.getOrNull() ?: run {
                _dashboardState.value = _dashboardState.value.copy(
                    isLoading = false,
                    error = "Failed to load user data"
                )
                return@launch
            }
            val userStats  = legacyRepository.getUserStats(userId)
            val dailyGoal  = userStats?.goalCalories?.toInt() ?: 0
            val nickname   = userStats?.nickname ?: userStats?.firstName ?: user.nickname.ifBlank { "User" }

            _dashboardState.value = _dashboardState.value.copy(
                user         = user,
                nickname     = nickname,
                goalCalories = dailyGoal,
                isLoading    = false
            )

            // ── Real-time: observe food entries for today ──────────────────
            calorieRepository.getDailyEntriesFlow(userId, todayTimestamp)
                .collect { entries ->
                    val consumed  = entries.sumOf { it.calories }
                    val remaining = dailyGoal - consumed
                    val progress  = if (dailyGoal > 0)
                        (consumed.toFloat() / dailyGoal.toFloat()).coerceIn(0f, 1f)
                    else 0f

                    _dashboardState.value = _dashboardState.value.copy(
                        foodEntries       = entries,
                        consumedCalories  = consumed,
                        remainingCalories = remaining,
                        progress          = progress,
                        isRefreshing      = false
                    )
                }
        }
    }

    fun addFoodEntry(foodName: String, calories: Int, mealType: String) {
        viewModelScope.launch {
            try {
                val userId = sessionManager.getUserId() ?: return@launch
                val entry = DailyEntryEntity(
                    userId   = userId,
                    date     = todayTimestamp,
                    foodName = foodName,
                    calories = calories,
                    mealType = mealType
                )
                
                // Sprint 4 Phase 7.6: DB-ID Explicit Linking
                val generatedId = calorieRepository.addDailyEntry(entry).getOrThrow()
                val syncedEntry = entry.copy(entryId = generatedId.toInt())
                
                // Sprint 4 Phase 7.8: Offline-safe Firestore push. Local Room write always succeeds.
                // If online, push immediately. If offline, WorkManager queues it on network restore.
                val email = sessionManager.getUserEmail() ?: ""
                if (email.isNotBlank()) {
                    try {
                        firestoreService.saveDailyEntry(email, mapToDto(syncedEntry))
                    } catch (syncEx: Exception) {
                        android.util.Log.w("DashboardViewModel", "Offline: food add queued for sync. ${syncEx.message}")
                    }
                }
                
                _uiEvent.emit(UiEvent.ShowSuccess("$foodName added successfully!"))
                try { syncScheduler.triggerImmediateSync() } catch (ignored: Exception) {}
            } catch (e: Exception) {
                _dashboardState.value = _dashboardState.value.copy(error = "Failed to add entry: ${e.message}")
                _uiEvent.emit(UiEvent.ShowError("Failed to add entry"))
            }
        }
    }

    fun deleteFoodEntry(entryId: Int) {
        viewModelScope.launch {
            try {
                val userId = sessionManager.getUserId() ?: return@launch
                val email = sessionManager.getUserEmail() ?: ""
                val now = System.currentTimeMillis()

                // Sprint 4 Phase 7.9: True Physical Deletes
                val allEntries = calorieRepository.getAllFoodEntriesSortedByDate(userId).getOrNull() ?: emptyList()
                val entryToDelete = allEntries.find { it.entryId == entryId }
                
                // 1. Permanently wipe from local Room Db (no more soft-delete)
                calorieRepository.physicallyDeleteDailyEntry(entryId)

                // 2. Permanently wipe from Firestore (natively offline-safe via Android SDK queuing)
                if (email.isNotBlank() && entryToDelete != null) {
                    val uniqueId = if (entryToDelete.syncId.isNotBlank()) entryToDelete.syncId else "${entryToDelete.entryId}_${entryToDelete.date}"
                    try {
                        firestoreService.deleteDailyEntry(email, uniqueId)
                    } catch (syncEx: Exception) {
                        android.util.Log.w("DashboardViewModel", "Offline: food delete queued by native SDK. ${syncEx.message}")
                    }
                }

                _uiEvent.emit(UiEvent.ShowSuccess("Entry removed"))
                try { syncScheduler.triggerImmediateSync() } catch (ignored: Exception) {}
            } catch (e: Exception) {
                _uiEvent.emit(UiEvent.ShowError("Failed to delete entry"))
            }
        }
    }

    fun updateFoodEntry(entry: DailyEntryEntity) {
        viewModelScope.launch {
            try {
                val updatedEntry = entry.copy(lastUpdated = System.currentTimeMillis())
                calorieRepository.updateDailyEntry(updatedEntry)
                
                // Sprint 4 Phase 7.8: Offline-safe Firestore push
                val email = sessionManager.getUserEmail() ?: ""
                if (email.isNotBlank()) {
                    try {
                        firestoreService.saveDailyEntry(email, mapToDto(updatedEntry))
                    } catch (syncEx: Exception) {
                        android.util.Log.w("DashboardViewModel", "Offline: food update queued for sync. ${syncEx.message}")
                    }
                }
                
                _uiEvent.emit(UiEvent.ShowSuccess("${entry.foodName} updated"))
                try { syncScheduler.triggerImmediateSync() } catch (ignored: Exception) {}
            } catch (e: Exception) {
                _uiEvent.emit(UiEvent.ShowError("Failed to update entry"))
            }
        }
    }

    /** Pull-to-refresh — sets isRefreshing flag; Flow data arrives automatically. */
    fun refreshDashboard() {
        _dashboardState.value = _dashboardState.value.copy(isRefreshing = true)
        // The Flow collector will reset isRefreshing on next emission
        observeDashboardRealTime()
    }

    /** Kept for backward compatibility with any remaining callers. */
    fun refreshData() {
        refreshDashboard()
    }

    // ── Dialog state helpers ───────────────────────────────────────────────────

    fun showAddDialog()  { _dashboardState.value = _dashboardState.value.copy(showAddDialog = true) }
    fun hideAddDialog()  { _dashboardState.value = _dashboardState.value.copy(showAddDialog = false) }
    fun updateTempFoodName(v: String) { _dashboardState.value = _dashboardState.value.copy(tempFoodName = v) }
    fun updateTempCalories(v: String) { _dashboardState.value = _dashboardState.value.copy(tempCalories = v) }
    fun updateTempMealType(v: String) { _dashboardState.value = _dashboardState.value.copy(tempMealType = v) }
    fun dismissError() {
        _dashboardState.value = _dashboardState.value.copy(error = null)
    }
    
    private fun mapToDto(entry: DailyEntryEntity): DailyEntryDto {
        return DailyEntryDto(
            entryId = entry.entryId,
            userId = entry.userId,
            date = entry.date,
            foodName = entry.foodName,
            calories = entry.calories,
            mealType = entry.mealType,
            lastUpdated = entry.lastUpdated,
            isDeleted = entry.isDeleted,
            syncId = entry.syncId
        )
    }
    fun clearTempInput() {
        _dashboardState.value = _dashboardState.value.copy(
            tempFoodName = "", tempCalories = "", tempMealType = "Breakfast", showAddDialog = false
        )
    }
}
