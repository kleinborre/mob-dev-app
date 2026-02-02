package com.sample.calorease.domain.repository

import com.sample.calorease.data.model.DailyEntry
import com.sample.calorease.data.model.UserStats
import kotlinx.coroutines.flow.Flow

/**
 * Legacy CalorieRepository interface for backward compatibility
 * Used by old ViewModels that haven't been migrated to new entities yet
 */
interface LegacyCalorieRepository {
    
    // User Stats Operations
    suspend fun insertUserStats(userStats: UserStats)
    suspend fun updateUserStats(userStats: UserStats)
    fun getUserStats(): Flow<UserStats?>
    suspend fun getUserStatsOnce(): UserStats?
    suspend fun deleteAllUserStats()
    
    // Daily Entry Operations
    suspend fun insertDailyEntry(dailyEntry: DailyEntry)
    suspend fun updateDailyEntry(dailyEntry: DailyEntry)
    fun getAllDailyEntries(): Flow<List<DailyEntry>>
    suspend fun getDailyEntryByDate(date: String): DailyEntry?
    fun getDailyEntryByDateFlow(date: String): Flow<DailyEntry?>
    fun getRecentDailyEntries(limit: Int = 30): Flow<List<DailyEntry>>
    suspend fun getDailyEntriesByDateRange(startDate: String, endDate: String): List<DailyEntry>
    suspend fun deleteDailyEntry(dailyEntry: DailyEntry)
    suspend fun deleteAllDailyEntries()
}
