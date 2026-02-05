package com.sample.calorease.data.local.dao

import androidx.room.*
import com.sample.calorease.data.local.entity.DailyEntryEntity
import com.sample.calorease.data.local.entity.UserEntity
import com.sample.calorease.data.model.UserStats

/**
 * Data Access Object for CalorEase Database
 * Provides all database operations for users and daily entries
 */
@Dao
interface CalorieDao {
    
    // ==================== USER OPERATIONS ====================
    
    /**
     * Insert a new user
     * @throws SQLException if email already exists (unique constraint)
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: UserEntity): Long
    
    /**
     * Update existing user
     */
    @Update
    suspend fun updateUser(user: UserEntity)
    
    /**
     * Login query - authenticate user with email and password
     */
    @Query("SELECT * FROM users WHERE email = :email AND password = :password LIMIT 1")
    suspend fun login(email: String, password: String): UserEntity?
    
    /**
     * Get user by ID
     */
    @Query("SELECT * FROM users WHERE userId = :userId")
    suspend fun getUserById(userId: Int): UserEntity?
    
    /**
     * Get user by email
     */
    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmail(email: String): UserEntity?
    
    /**
     * Check if email exists
     */
    @Query("SELECT COUNT(*) FROM users WHERE email = :email")
    suspend fun emailExists(email: String): Int
    
    // ==================== USER STATS OPERATIONS ====================
    
    /**
     * Insert user stats (onboarding data)
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserStats(userStats: UserStats): Long
    
    /**
     * Get user stats by ID
     */
    @Query("SELECT * FROM user_stats WHERE userId = :userId")
    suspend fun getUserStats(userId: Int): UserStats?
    
    /**
     * Update or insert user stats (upsert)
     * ✅ CRITICAL: For onboarding state persistence
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateUserStats(userStats: UserStats)
    
    /**
     * Update onboarding progress
     * ✅ Saves currentOnboardingStep after each step completion
     */
    @Query("UPDATE user_stats SET currentOnboardingStep = :step WHERE userId = :userId")
    suspend fun updateOnboardingProgress(userId: Int, step: Int)
    
    /**
     * Mark onboarding as complete
     */
    @Query("UPDATE user_stats SET onboardingCompleted = 1 WHERE userId = :userId")
    suspend fun markOnboardingComplete(userId: Int)
    
    // ==================== ADMIN OPERATIONS ====================
    
    /**
     * Get all users (Admin feature)
     */
    @Query("SELECT * FROM users ORDER BY userId DESC")
    suspend fun getAllUsers(): List<UserEntity>
    
    /**
     * PHASE 2: Get all users as Flow for real-time updates
     */
    @Query("SELECT * FROM users ORDER BY userId DESC")
    fun getAllUsersFlow(): kotlinx.coroutines.flow.Flow<List<UserEntity>>
    
    /**
     * PHASE 2: Deactivate account (set status to deactivated)
     */
    @Query("UPDATE users SET accountStatus = 'deactivated' WHERE userId = :userId")
    suspend fun deactivateAccount(userId: Int)
    
    /**
     * Search users by name or email (Admin feature)
     */
    @Query("SELECT * FROM users WHERE email LIKE '%' || :query || '%' OR nickname LIKE '%' || :query || '%'")
    suspend fun searchUsers(query: String): List<UserEntity>
    
    // ==================== DAILY ENTRY OPERATIONS ====================
    
    /**
     * Insert a new daily entry
     */
    @Insert
    suspend fun insertDailyEntry(entry: DailyEntryEntity): Long
    
    /**
     * Get all daily entries for a user on a specific date
     */
    @Query("SELECT * FROM daily_entries WHERE userId = :userId AND date = :date ORDER BY entryId DESC")
    suspend fun getDailyEntries(userId: Int, date: Long): List<DailyEntryEntity>
    
    /**
     * Get total calories for a user on a specific date
     */
    @Query("SELECT SUM(calories) FROM daily_entries WHERE userId = :userId AND date = :date")
    suspend fun getTotalCaloriesForDay(userId: Int, date: Long): Int?
    
    /**
     * Get entries by date range (legacy - no userId filter)
     */
    @Query("SELECT * FROM daily_entries WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    suspend fun getDailyEntriesByDateRange(startDate: String, endDate: String): List<DailyEntryEntity>
    
    /**
     * Get all entries for a user within a date range (for Statistics)
     * ✅ CRITICAL: Used by StatisticsViewModel to load weekly chart data
     */
    @Query("SELECT * FROM daily_entries WHERE userId = :userId AND date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    suspend fun getDailyEntriesByDateRangeForUser(userId: Int, startDate: Long, endDate: Long): List<DailyEntryEntity>
    
    /**
     * Delete a daily entry
     */
    @Delete
    suspend fun deleteDailyEntry(entry: DailyEntryEntity)
    
    /**
     * Delete all daily entries for a specific user
     * ✅ CRITICAL: Used when user changes goal (progress reset)
     */
    @Query("DELETE FROM daily_entries WHERE userId = :userId")
    suspend fun deleteAllDailyEntriesForUser(userId: Int)
    
    /**
     * Delete a specific daily entry
     */
    @Query("DELETE FROM daily_entries WHERE entryId = :entryId")
    suspend fun deleteDailyEntry(entryId: Int)
    
    /**
     * Update an existing daily entry
     */
    @Update
    suspend fun updateDailyEntry(entry: DailyEntryEntity)
    
    
    /**
     * Get entries by meal type for a specific date
     */
    @Query("SELECT * FROM daily_entries WHERE userId = :userId AND date = :date AND mealType = :mealType ORDER BY entryId DESC")
    suspend fun getEntriesByMealType(userId: Int, date: Long, mealType: String): List<DailyEntryEntity>
    
    /**
     * Get ALL food entries for a user sorted by date (latest first) for history page
     */
    @Query("SELECT * FROM daily_entries WHERE userId = :userId ORDER BY date DESC, entryId DESC")
    suspend fun getAllFoodEntriesSortedByDate(userId: Int): List<DailyEntryEntity>
}
