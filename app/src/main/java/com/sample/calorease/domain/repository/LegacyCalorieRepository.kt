package com.sample.calorease.domain.repository

import com.sample.calorease.data.model.UserStats
import kotlinx.coroutines.flow.Flow

/**
 * Legacy CalorieRepository interface for backward compatibility.
 * Handles UserStats and Onboarding persistence operations.
 * Note: DailyEntry legacy methods removed — food entry operations use
 * CalorieRepository with the current DailyEntryEntity model.
 */
interface LegacyCalorieRepository {

    // User Stats Operations
    suspend fun insertUserStats(userStats: UserStats)
    suspend fun updateUserStats(userStats: UserStats)
    fun getUserStats(): Flow<UserStats?>
    suspend fun getUserStats(userId: Int): UserStats?
    suspend fun getUserStatsOnce(): UserStats?
    suspend fun deleteAllUserStats()

    // Onboarding State Persistence
    suspend fun saveOnboardingState(userStats: UserStats)
    suspend fun updateOnboardingProgress(userId: Int, step: Int)
    suspend fun markOnboardingComplete(userId: Int)

    // User Progress Cleanup
    suspend fun deleteAllDailyEntriesForUser(userId: Int)
}
