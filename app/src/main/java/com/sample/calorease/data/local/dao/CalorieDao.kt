package com.sample.calorease.data.local.dao

import androidx.room.*
import com.sample.calorease.data.local.entity.DailyEntryEntity
import com.sample.calorease.data.local.entity.UserEntity

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
    
    // ==================== ADMIN OPERATIONS ====================
    
    /**
     * Get all users (Admin feature)
     */
    @Query("SELECT * FROM users ORDER BY userId DESC")
    suspend fun getAllUsers(): List<UserEntity>
    
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
     * Delete a specific daily entry
     */
    @Query("DELETE FROM daily_entries WHERE entryId = :entryId")
    suspend fun deleteDailyEntry(entryId: Int)
    
    /**
     * Get entries by meal type for a specific date
     */
    @Query("SELECT * FROM daily_entries WHERE userId = :userId AND date = :date AND mealType = :mealType ORDER BY entryId DESC")
    suspend fun getEntriesByMealType(userId: Int, date: Long, mealType: String): List<DailyEntryEntity>
}
