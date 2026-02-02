package com.sample.calorease.domain.repository

import com.sample.calorease.data.local.entity.UserEntity

/**
 * User Repository Interface
 * Handles all user-related database operations
 */
interface UserRepository {
    
    /**
     * Register a new user
     * @return Result with userId on success, or error
     */
    suspend fun registerUser(user: UserEntity): Result<Long>
    
    /**
     * Login with email and password
     * @return Result with UserEntity on success, or error
     */
    suspend fun login(email: String, password: String): Result<UserEntity>
    
    /**
     * Get user by ID
     */
    suspend fun getUserById(userId: Int): Result<UserEntity?>
    
    /**
     * Get user by email
     */
    suspend fun getUserByEmail(email: String): Result<UserEntity?>
    
    /**
     * Update user information
     */
    suspend fun updateUser(user: UserEntity): Result<Unit>
    
    /**
     * Check if email exists
     */
    suspend fun emailExists(email: String): Result<Boolean>
}
