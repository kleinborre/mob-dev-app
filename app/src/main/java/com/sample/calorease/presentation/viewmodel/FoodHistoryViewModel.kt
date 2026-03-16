package com.sample.calorease.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sample.calorease.data.local.entity.DailyEntryEntity
import com.sample.calorease.data.session.SessionManager
import com.sample.calorease.domain.repository.CalorieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FoodHistoryState(
    val allEntries: List<DailyEntryEntity> = emptyList(),
    val currentPage: Int = 1,
    val isLoading: Boolean = true
) {
    companion object {
        const val PAGE_SIZE = 5
    }

    /** Entries visible on the current page window */
    val pagedEntries: List<DailyEntryEntity>
        get() = allEntries.take(currentPage * PAGE_SIZE)

    val hasMore: Boolean
        get() = allEntries.size > currentPage * PAGE_SIZE

    val totalCount: Int
        get() = allEntries.size
}

/**
 * Terminal Final Phase 1.1: Dedicated ViewModel for the Food History screen.
 * Streams the full, all-time list of food entries from Room in real-time (newest-first)
 * and exposes a paginated window of PAGE_SIZE = 5 cards per page.
 */
@HiltViewModel
class FoodHistoryViewModel @Inject constructor(
    private val calorieRepository: CalorieRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _state = MutableStateFlow(FoodHistoryState())
    val state: StateFlow<FoodHistoryState> = _state.asStateFlow()

    init {
        observeHistory()
    }

    private fun observeHistory() {
        viewModelScope.launch {
            val userId = sessionManager.getUserId() ?: run {
                _state.value = _state.value.copy(isLoading = false)
                return@launch
            }

            calorieRepository.getAllFoodEntriesFlow(userId)
                .collect { entries ->
                    _state.value = _state.value.copy(
                        allEntries = entries,
                        isLoading = false
                    )
                }
        }
    }

    /** Load the next page of 5 entries */
    fun loadNextPage() {
        _state.value = _state.value.copy(currentPage = _state.value.currentPage + 1)
    }
}
