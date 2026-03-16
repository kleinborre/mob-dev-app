package com.sample.calorease.domain.repository

import com.sample.calorease.data.local.entity.DailyEntryEntity
import kotlinx.coroutines.flow.Flow

/**
 * Calorie Repository Interface
 * Handles daily food entry operations.
 * All suspend methods are one-shot; Flow methods provide real-time reactive updates.
 */
interface CalorieRepository {

    /** Add a new daily entry */
    suspend fun addDailyEntry(entry: DailyEntryEntity): Result<Long>

    /** Get all daily entries for a user on a specific date (one-shot) */
    suspend fun getDailyEntries(userId: Int, date: Long): Result<List<DailyEntryEntity>>

    /** Observe daily entries for a user on a specific date (real-time) */
    fun getDailyEntriesFlow(userId: Int, date: Long): Flow<List<DailyEntryEntity>>

    /** Get total calories for a user on a specific date (one-shot) */
    suspend fun getTotalCalories(userId: Int, date: Long): Result<Int>

    /** Observe total calories for a user on a specific date (real-time) */
    fun getTotalCaloriesFlow(userId: Int, date: Long): Flow<Int?>

    /** Delete a daily entry by ID */
    suspend fun deleteDailyEntry(entryId: Int): Result<Unit>

    /** Permanently wipe a daily entry from the device */
    suspend fun physicallyDeleteDailyEntry(entryId: Int): Result<Unit>

    /** Update an existing daily entry */
    suspend fun updateDailyEntry(entry: DailyEntryEntity): Result<Unit>

    /** Get entries by meal type */
    suspend fun getEntriesByMealType(userId: Int, date: Long, mealType: String): Result<List<DailyEntryEntity>>

    /** Get all entries for a user within date range (for Statistics) */
    suspend fun getDailyEntriesByDateRange(userId: Int, startDate: Long, endDate: Long): Result<List<DailyEntryEntity>>

    /** Get ALL entries for a user (for Sprint 4 Remote Sync) */
    suspend fun getAllFoodEntriesSortedByDate(userId: Int): Result<List<DailyEntryEntity>>

    /** Terminal Final Phase 1.1: Observe ALL non-deleted entries in real-time (newest-first) for History page */
    fun getAllFoodEntriesFlow(userId: Int): Flow<List<DailyEntryEntity>>
}
