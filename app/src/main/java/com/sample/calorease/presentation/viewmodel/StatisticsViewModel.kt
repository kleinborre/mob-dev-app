package com.sample.calorease.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sample.calorease.data.model.DailyEntry
import com.sample.calorease.domain.repository.LegacyCalorieRepository
import com.sample.calorease.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
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
    private val repository: LegacyCalorieRepository
) : ViewModel() {
    
    private val _statisticsState = MutableStateFlow(StatisticsState())
    val statisticsState: StateFlow<StatisticsState> = _statisticsState.asStateFlow()
    
    init {
        loadWeeklyData()
    }
    
    private fun loadWeeklyData() {
        viewModelScope.launch {
            _statisticsState.value = _statisticsState.value.copy(isLoading = true)
            
            val last7Days = DateUtils.getLastNDays(7)
            val entries = repository.getDailyEntriesByDateRange(last7Days.first(), last7Days.last())
            
            val chartData = last7Days.map { date ->
                val entry = entries.find { it.date == date }
                ChartData(
                    date = date,
                    calories = entry?.caloriesConsumed?.toFloat() ?: 0f
                )
            }
            
            _statisticsState.value = _statisticsState.value.copy(
                chartData = chartData,
                isLoading = false
            )
        }
    }
    
    fun refreshData() {
        loadWeeklyData()
    }
}
