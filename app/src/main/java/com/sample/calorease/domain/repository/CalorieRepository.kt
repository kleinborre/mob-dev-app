package com.sample.calorease.domain.repository

import com.sample.calorease.data.local.entity.DailyEntryEntity

/**
 * Calorie Repository Interface
 * Handles daily food entry operations
 */
interface CalorieRepository {
    
    /**
     * Add a new daily entry
     */
    suspend fun addDailyEntry(entry: DailyEntryEntity): Result<Long>
    
    /**
     * Get all daily entries for a user on a specific date
     */
    suspend fun getDailyEntries(userId: Int, date: Long): Result<List<DailyEntryEntity>>
    
    /**
     * Get total calories for a user on a specific date
     */
    suspend fun getTotalCalories(userId: Int, date: Long): Result<Int>
    
    /**
     * Delete a daily entry
     */
    suspend fun deleteDailyEntry(entryId: Int): Result<Unit>
    
    /**
     * Update an existing daily entry
     */
    suspend fun updateDailyEntry(entry: DailyEntryEntity): Result<Unit>
    
    /**
     * Get entries by meal type
     */
    suspend fun getEntriesByMealType(userId: Int, date: Long, mealType: String): Result<List<DailyEntryEntity>>
    
    /**
     * Get all entries for a user within date range (for Statistics)
     */
    suspend fun getDailyEntriesByDateRange(userId: Int, startDate: Long, endDate: Long): Result<List<DailyEntryEntity>>
}
