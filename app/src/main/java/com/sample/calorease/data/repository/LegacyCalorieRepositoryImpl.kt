package com.sample.calorease.data.repository

import com.sample.calorease.data.local.dao.CalorieDao
import com.sample.calorease.data.model.UserStats
import com.sample.calorease.data.session.SessionManager
import com.sample.calorease.domain.repository.LegacyCalorieRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of LegacyCalorieRepository.
 * Bridges ViewModels that use UserStats/Onboarding with the Room DAO.
 * DailyEntry legacy stubs removed — food entry operations use CalorieRepository.
 */
@Singleton
class LegacyCalorieRepositoryImpl @Inject constructor(
    private val dao: CalorieDao,
    private val sessionManager: SessionManager
) : LegacyCalorieRepository {

    // ── User Stats ────────────────────────────────────────────────────────────

    override suspend fun insertUserStats(userStats: UserStats) {
        dao.insertOrUpdateUserStats(userStats)
        android.util.Log.d("LegacyRepo", "Inserted/Updated UserStats for userId=${userStats.userId}")
    }

    override suspend fun updateUserStats(userStats: UserStats) {
        dao.insertOrUpdateUserStats(userStats)
        android.util.Log.d("LegacyRepo", "Updated UserStats for userId=${userStats.userId}")
    }

    override fun getUserStats(): Flow<UserStats?> = flow { emit(null) }

    override suspend fun getUserStats(userId: Int): UserStats? = dao.getUserStats(userId)

    override suspend fun getUserStatsOnce(): UserStats? = null

    override suspend fun deleteAllUserStats() { /* Not needed */ }

    // ── Onboarding ────────────────────────────────────────────────────────────

    override suspend fun saveOnboardingState(userStats: UserStats) {
        dao.insertOrUpdateUserStats(userStats)
        android.util.Log.d("LegacyRepo", "Saved onboarding state userId=${userStats.userId}, step=${userStats.currentOnboardingStep}")
    }

    override suspend fun updateOnboardingProgress(userId: Int, step: Int) {
        dao.updateOnboardingProgress(userId, step)
        android.util.Log.d("LegacyRepo", "Updated onboarding progress: userId=$userId, step=$step")
    }

    override suspend fun markOnboardingComplete(userId: Int) {
        dao.markOnboardingComplete(userId)
        android.util.Log.d("LegacyRepo", "Marked onboarding complete for userId=$userId")
    }

    // ── User Progress Cleanup ─────────────────────────────────────────────────

    override suspend fun deleteAllDailyEntriesForUser(userId: Int) {
        dao.deleteAllDailyEntriesForUser(userId)
    }
}
