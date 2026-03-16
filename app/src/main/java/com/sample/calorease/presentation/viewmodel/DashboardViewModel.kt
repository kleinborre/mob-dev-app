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
    private val syncScheduler: SyncScheduler
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
                calorieRepository.addDailyEntry(entry)
                // Flow observer auto-refreshes UI — no manual loadDashboardData() call needed
                _uiEvent.emit(UiEvent.ShowSuccess("$foodName added successfully!"))
                
                // Sprint 4 Phase 2: Broker Sync to Firestore
                syncScheduler.triggerImmediateSync()
            } catch (e: Exception) {
                _dashboardState.value = _dashboardState.value.copy(
                    error = "Failed to add entry: ${e.message}"
                )
                _uiEvent.emit(UiEvent.ShowError("Failed to add entry"))
            }
        }
    }

    fun deleteFoodEntry(entryId: Int) {
        viewModelScope.launch {
            try {
                calorieRepository.deleteDailyEntry(entryId)
                // Flow observer auto-refreshes UI
                _uiEvent.emit(UiEvent.ShowSuccess("Entry removed"))
                
                // Sprint 4 Phase 2: Broker Sync to Firestore
                syncScheduler.triggerImmediateSync()
            } catch (e: Exception) {
                _uiEvent.emit(UiEvent.ShowError("Failed to delete entry"))
            }
        }
    }

    fun updateFoodEntry(entry: DailyEntryEntity) {
        viewModelScope.launch {
            try {
                calorieRepository.updateDailyEntry(entry)
                // Flow observer auto-refreshes UI
                _uiEvent.emit(UiEvent.ShowSuccess("success:${entry.foodName} updated"))
                
                // Sprint 4 Phase 2: Broker Sync to Firestore
                syncScheduler.triggerImmediateSync()
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
    fun clearTempInput() {
        _dashboardState.value = _dashboardState.value.copy(
            tempFoodName = "", tempCalories = "", tempMealType = "Breakfast", showAddDialog = false
        )
    }
}
