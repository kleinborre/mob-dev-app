package com.sample.calorease.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sample.calorease.data.session.SessionManager
import com.sample.calorease.domain.repository.CalorieRepository
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
    val isLoading: Boolean = true
)

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val repository: CalorieRepository,  // ✅ Use real repository
    private val sessionManager: SessionManager   // ✅ Get userId
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
                // Get userId from session
                val userId = sessionManager.getUserId()
                if (userId == null) {
                    android.util.Log.e("StatisticsViewModel", "❌ No userId in session")
                    _statisticsState.value = _statisticsState.value.copy(
                        chartData = emptyList(),
                        isLoading = false
                    )
                    return@launch
                }
                
                android.util.Log.d("StatisticsViewModel", "📊 Loading weekly data for userId=$userId")
                
                // Get last 7 days as timestamps (oldest to newest)
                val last7Days = getLastNDaysTimestamps(7)
                
                android.util.Log.d("StatisticsViewModel", "📅 Date range: ${formatTimestamp(last7Days.first())} to ${formatTimestamp(last7Days.last())}")
                
                // ✅ CRITICAL FIX: Use date range query to get ALL 7 days of data
                // Previous bug: getDailyEntries(userId, date) only gets ONE specific date
                val allEntriesResult = repository.getDailyEntriesByDateRange(
                    userId = userId,
                    startDate = last7Days.first(),
                    endDate = last7Days.last()
                )
                val allEntries = allEntriesResult.getOrNull() ?: emptyList()
                
                android.util.Log.d("StatisticsViewModel", "📦 Total entries fetched: ${allEntries.size}")
                
                // Group by date, sum calories for each day
                val chartData = last7Days.map { timestamp ->
                    // Filter entries for this specific day
                    val dayEntries = allEntries.filter { it.date == timestamp }
                    val totalCalories = dayEntries.sumOf { it.calories }
                    
                    val dateStr = formatTimestamp(timestamp)
                    android.util.Log.d("StatisticsViewModel", "  📅 $dateStr: ${dayEntries.size} entries, $totalCalories cal")
                    
                    ChartData(
                        date = dateStr,
                        calories = totalCalories.toFloat()
                    )
                }
                
                _statisticsState.value = _statisticsState.value.copy(
                    chartData = chartData,
                    isLoading = false
                )
                
                android.util.Log.d("StatisticsViewModel", "✅ Weekly data loaded: ${chartData.size} days")
            } catch (e: Exception) {
                android.util.Log.e("StatisticsViewModel", "❌ Error loading weekly data", e)
                _statisticsState.value = _statisticsState.value.copy(
                    chartData = emptyList(),
                    isLoading = false
                )
            }
        }
    }
    
    fun refreshData() {
        android.util.Log.d("StatisticsViewModel", "🔄 Refreshing statistics data...")
        loadWeeklyData()
    }
    
    /**
     * Get last N days as timestamps (start of day, midnight)
     * Returns oldest to newest
     */
    private fun getLastNDaysTimestamps(days: Int): List<Long> {
        val calendar = Calendar.getInstance()
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
    
    /**
     * Format timestamp to "yyyy-MM-dd" for display
     */
    private fun formatTimestamp(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
