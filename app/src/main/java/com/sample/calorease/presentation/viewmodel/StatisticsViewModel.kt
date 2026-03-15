package com.sample.calorease.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sample.calorease.data.session.SessionManager
import com.sample.calorease.domain.repository.CalorieRepository
import com.sample.calorease.domain.repository.LegacyCalorieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class ChartData(
    val date: String,
    val calories: Float
)

data class StatisticsState(
    val chartData: List<ChartData> = emptyList(),
    val goalCalories: Float        = 2000f,  // Daily calorie goal from user profile
    val currentWeight: Float       = 0f,     // Current weight (kg) from user profile
    val targetWeight: Float        = 0f,     // Goal weight (kg) from user profile
    val todayCalories: Float       = 0f,     // Calories logged today
    val isLoading: Boolean         = true
)

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val repository: CalorieRepository,
    private val legacyRepository: LegacyCalorieRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _statisticsState = MutableStateFlow(StatisticsState())
    val statisticsState: StateFlow<StatisticsState> = _statisticsState.asStateFlow()

    init {
        loadWeeklyData()
    }

    private fun loadWeeklyData() {
        viewModelScope.launch {
            _statisticsState.value = _statisticsState.value.copy(isLoading = true)

            try {
                val userId = sessionManager.getUserId()
                if (userId == null) {
                    _statisticsState.value = _statisticsState.value.copy(
                        chartData = emptyList(),
                        isLoading = false
                    )
                    return@launch
                }

                // ── Weekly calorie chart ─────────────────────────────────────
                val last7Days = getLastNDaysTimestamps(7)

                val allEntriesResult = repository.getDailyEntriesByDateRange(
                    userId    = userId,
                    startDate = last7Days.first(),
                    endDate   = last7Days.last()
                )
                val allEntries = allEntriesResult.getOrNull() ?: emptyList()

                val chartData = last7Days.map { timestamp ->
                    val dayEntries    = allEntries.filter { it.date == timestamp }
                    val totalCalories = dayEntries.sumOf { it.calories }
                    ChartData(
                        date     = formatTimestamp(timestamp),
                        calories = totalCalories.toFloat()
                    )
                }

                // Calories logged today
                val todayStart = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0);      set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                val todayCalories = allEntries
                    .filter { it.date == todayStart }
                    .sumOf { it.calories }
                    .toFloat()

                // ── User profile — goal calories, current & target weight ────
                val userStats = try {
                    legacyRepository.getUserStats(userId)
                } catch (e: Exception) { null }

                _statisticsState.value = _statisticsState.value.copy(
                    chartData      = chartData,
                    goalCalories   = userStats?.goalCalories?.toFloat() ?: 2000f,
                    currentWeight  = userStats?.weightKg?.toFloat()     ?: 0f,
                    targetWeight   = userStats?.targetWeightKg?.toFloat() ?: 0f,
                    todayCalories  = todayCalories,
                    isLoading      = false
                )

            } catch (e: Exception) {
                android.util.Log.e("StatisticsViewModel", "Error loading data", e)
                _statisticsState.value = _statisticsState.value.copy(
                    chartData = emptyList(),
                    isLoading = false
                )
            }
        }
    }

    fun refreshData() {
        loadWeeklyData()
    }

    private fun getLastNDaysTimestamps(days: Int): List<Long> {
        val calendar   = Calendar.getInstance()
        val timestamps = mutableListOf<Long>()
        for (daysAgo in (days - 1) downTo 0) {
            calendar.timeInMillis = System.currentTimeMillis()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            calendar.add(Calendar.DAY_OF_YEAR, -daysAgo)
            timestamps.add(calendar.timeInMillis)
        }
        return timestamps
    }

    private fun formatTimestamp(timestamp: Long): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(timestamp))
}
