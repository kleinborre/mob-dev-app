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
        dao.insertOrUpdateUserStats(userStats)  // ✅ FIXED: Use upsert instead of plain insert
        android.util.Log.d("LegacyRepo", "✅ Inserted/Updated UserStats for userId=${userStats.userId}")
    }
    
    override suspend fun updateUserStats(userStats: UserStats) {
        dao.insertOrUpdateUserStats(userStats)  // ✅ CRITICAL FIX: Was stub, now actually saves!
        android.util.Log.d("LegacyRepo", "✅ Updated UserStats for userId=${userStats.userId}")
    }
    
    override fun getUserStats(): Flow<UserStats?> {
        return flow {
            // Return null for now - ViewModels will handle gracefully
            emit(null)
        }
    }
    
    override suspend fun getUserStats(userId: Int): UserStats? {
        return dao.getUserStats(userId)  // Query by userId
    }
    
    override suspend fun getUserStatsOnce(): UserStats? {
        // Get current user from session and convert UserEntity to UserStats
        // For now, return null - login/signup flows will work differently
        return null
    }
    
    override suspend fun deleteAllUserStats() {
        // Not needed for Phase 2
    }
    
    // ✅ Onboarding State Persistence
    override suspend fun saveOnboardingState(userStats: UserStats) {
        dao.insertOrUpdateUserStats(userStats)
        android.util.Log.d("LegacyRepo", "✅ Saved onboarding state for userId=${userStats.userId}, step=${userStats.currentOnboardingStep}")
    }
    
    override suspend fun updateOnboardingProgress(userId: Int, step: Int) {
        dao.updateOnboardingProgress(userId, step)
        android.util.Log.d("LegacyRepo", "✅ Updated onboarding progress: userId=$userId, step=$step")
    }
    
    override suspend fun markOnboardingComplete(userId: Int) {
        dao.markOnboardingComplete(userId)
        android.util.Log.d("LegacyRepo", "✅ Marked onboarding complete for userId=$userId")
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
    
    override suspend fun deleteAllDailyEntriesForUser(userId: Int) {
        dao.deleteAllDailyEntriesForUser(userId)  // ✅ Delete user progress
    }
}
