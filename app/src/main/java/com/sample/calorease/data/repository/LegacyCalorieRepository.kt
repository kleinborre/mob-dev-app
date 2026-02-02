package com.sample.calorease.data.repository

import com.sample.calorease.data.local.dao.CalorieDao
import com.sample.calorease.data.model.DailyEntry
import com.sample.calorease.data.model.UserStats
import com.sample.calorease.data.session.SessionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Legacy CalorieRepository for backward compatibility with old ViewModels
 * This is a temporary bridge until all ViewModels are updated to use new entities
 */
@Singleton
class LegacyCalorieRepository @Inject constructor(
    private val dao: CalorieDao,
    private val sessionManager: SessionManager
) {
    
    // User Stats Operations (Legacy - uses UserEntity under the hood)
    suspend fun insertUserStats(userStats: UserStats) {
        // Convert UserStats to UserEntity and save
        // For now, this is a stub - will be implemented when we fully migrate onboarding
    }
    
    suspend fun updateUserStats(userStats: UserStats) {
        // Convert and update
        // Stub for now
    }
    
    fun getUserStats(): Flow<UserStats?> {
        return flow {
            // Return null for now - ViewModels will handle gracefully
            emit(null)
        }
    }
    
    suspend fun getUserStatsOnce(): UserStats? {
        // Get current user from session and convert UserEntity to UserStats
        // For now, return null - login/signup flows will work differently
        return null
    }
    
    suspend fun deleteAllUserStats() {
        // Not needed for Phase 2
    }
    
    // Daily Entry Operations (Legacy)
    suspend fun insertDailyEntry(dailyEntry: DailyEntry) {
        // Convert DailyEntry to DailyEntryEntity
        // Stub for now
    }
    
    suspend fun updateDailyEntry(dailyEntry: DailyEntry) {
        // Stub
    }
    
    fun getAllDailyEntries(): Flow<List<DailyEntry>> {
        return flow {
            emit(emptyList())
        }
    }
    
    suspend fun getDailyEntryByDate(date: String): DailyEntry? {
        return null
    }
    
    fun getDailyEntryByDateFlow(date: String): Flow<DailyEntry?> {
        return flow {
            emit(null)
        }
    }
    
    fun getRecentDailyEntries(limit: Int = 30): Flow<List<DailyEntry>> {
        return flow {
            emit(emptyList())
        }
    }
    
    suspend fun getDailyEntriesByDateRange(startDate: String, endDate: String): List<DailyEntry> {
        return emptyList()
    }
    
    suspend fun deleteDailyEntry(dailyEntry: DailyEntry) {
        // Stub
    }

    suspend fun deleteAllDailyEntries() {
        // Not needed for Phase 2
    }
}
