package com.sample.calorease.domain.repository

import com.sample.calorease.data.local.entity.UserEntity
import com.sample.calorease.data.model.UserStats
import kotlinx.coroutines.flow.Flow

/**
 * User Repository Interface
 * Handles all user-related database operations.
 * suspend functions are one-shot; Flow functions provide real-time reactive streams.
 */
interface UserRepository {

    /** Register a new user. Returns userId on success. */
    suspend fun registerUser(user: UserEntity): Result<Long>

    /** Login with email and password. */
    suspend fun login(email: String, password: String): Result<UserEntity>

    /** Get user by ID (one-shot). */
    suspend fun getUserById(userId: Int): Result<UserEntity?>

    /** Observe user changes in real-time. */
    fun getUserByIdFlow(userId: Int): Flow<UserEntity?>

    /** Get user by email (one-shot). */
    suspend fun getUserByEmail(email: String): Result<UserEntity?>

    /** Update user information. */
    suspend fun updateUser(user: UserEntity): Result<Unit>

    /** Get all users (Admin only). */
    suspend fun getAllUsers(): Result<List<UserEntity>>

    /** Observe all users in real-time (Admin only). */
    fun getAllUsersFlow(): Flow<List<UserEntity>>

    /** Deactivate user account (soft delete). */
    suspend fun deactivateAccount(userId: Int): Result<Unit>

    /** Check if email exists. */
    suspend fun emailExists(email: String): Result<Boolean>

    /** Observe user stats in real-time. */
    fun getUserStatsFlow(userId: Int): Flow<UserStats?>

    /** Get user stats (one-shot). */
    suspend fun getUserStats(userId: Int): UserStats?

    /** Find a local account linked to a Google UID. */
    suspend fun getUserByGoogleId(googleId: String): Result<UserEntity?>

    /** Link a Google account UID to an existing local user row. */
    suspend fun linkGoogleId(userId: Int, googleId: String): Result<Unit>
}
