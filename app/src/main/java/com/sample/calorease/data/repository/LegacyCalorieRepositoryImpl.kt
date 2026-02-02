package com.sample.calorease.data.repository

import com.sample.calorease.data.local.dao.CalorieDao
import com.sample.calorease.data.model.DailyEntry
import com.sample.calorease.data.model.UserStats
import com.sample.calorease.data.session.SessionManager
import com.sample.calorease.domain.repository.LegacyCalorieRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Legacy CalorieRepository implementation for backward compatibility with old ViewModels
 * This is a temporary bridge until all ViewModels are updated to use new entities
 */
@Singleton
class LegacyCalorieRepositoryImpl @Inject constructor(
    private val dao: CalorieDao,
    private val sessionManager: SessionManager
) : LegacyCalorieRepository {
    
    // User Stats Operations (Legacy - uses UserEntity under the hood)
    override suspend fun insertUserStats(userStats: UserStats) {
        // Convert UserStats to UserEntity and save
        // For now, this is a stub - will be implemented when we fully migrate onboarding
    }
    
    override suspend fun updateUserStats(userStats: UserStats) {
        // Convert and update
        // Stub for now
    }
    
    override fun getUserStats(): Flow<UserStats?> {
        return flow {
            // Return null for now - ViewModels will handle gracefully
            emit(null)
        }
    }
    
    override suspend fun getUserStatsOnce(): UserStats? {
        // Get current user from session and convert UserEntity to UserStats
        // For now, return null - login/signup flows will work differently
        return null
    }
    
    override suspend fun deleteAllUserStats() {
        // Not needed for Phase 2
    }
    
    // Daily Entry Operations (Legacy)
    override suspend fun insertDailyEntry(dailyEntry: DailyEntry) {
        // Convert DailyEntry to DailyEntryEntity
        // Stub for now
    }
    
    override suspend fun updateDailyEntry(dailyEntry: DailyEntry) {
        // Stub
    }
    
    override fun getAllDailyEntries(): Flow<List<DailyEntry>> {
        return flow {
            emit(emptyList())
        }
    }
    
    override suspend fun getDailyEntryByDate(date: String): DailyEntry? {
        return null
    }
    
    override fun getDailyEntryByDateFlow(date: String): Flow<DailyEntry?> {
        return flow {
            emit(null)
        }
    }
    
    override fun getRecentDailyEntries(limit: Int): Flow<List<DailyEntry>> {
        return flow {
            emit(emptyList())
        }
    }
    
    override suspend fun getDailyEntriesByDateRange(startDate: String, endDate: String): List<DailyEntry> {
        return emptyList()
    }
    
    override suspend fun deleteDailyEntry(dailyEntry: DailyEntry) {
        // Stub
    }

    override suspend fun deleteAllDailyEntries() {
        // Not needed for Phase 2
    }
}
