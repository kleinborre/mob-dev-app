package com.sample.calorease.data.repository

import com.sample.calorease.data.local.dao.CalorieDao
import com.sample.calorease.data.local.entity.UserEntity
import com.sample.calorease.domain.repository.UserRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * UserRepository Implementation
 * Provides user database operations with error handling
 */
@Singleton
class UserRepositoryImpl @Inject constructor(
    private val dao: CalorieDao
) : UserRepository {
    
    override suspend fun registerUser(user: UserEntity): Result<Long> {
        return try {
            // Check if email already exists
            val existingUser = dao.getUserByEmail(user.email)
            if (existingUser != null) {
                Result.failure(Exception("Email already exists"))
            } else {
                val userId = dao.insertUser(user)
                Result.success(userId)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun login(email: String, password: String): Result<UserEntity> {
        return try {
            val user = dao.login(email, password)
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("Invalid email or password"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getUserById(userId: Int): Result<UserEntity?> {
        return try {
            val user = dao.getUserById(userId)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getUserByEmail(email: String): Result<UserEntity?> {
        return try {
            val user = dao.getUserByEmail(email)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateUser(user: UserEntity): Result<Unit> {
        return try {
            dao.updateUser(user)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getAllUsers(): Result<List<UserEntity>> {
        return try {
            val users = dao.getAllUsers()
            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    
    override fun getAllUsersFlow(): kotlinx.coroutines.flow.Flow<List<UserEntity>> {
        return dao.getAllUsersFlow()
    }
    
    override suspend fun deactivateAccount(userId: Int): Result<Unit> {
        return try {
            dao.deactivateAccount(userId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun emailExists(email: String): Result<Boolean> {
        return try {
            val count = dao.emailExists(email)
            Result.success(count > 0)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
