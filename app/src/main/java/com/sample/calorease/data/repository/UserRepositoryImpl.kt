package com.sample.calorease.data.repository

import com.sample.calorease.data.local.dao.CalorieDao
import com.sample.calorease.data.local.entity.UserEntity
import com.sample.calorease.data.model.UserStats
import com.sample.calorease.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * UserRepository Implementation
 * Provides user database operations with error handling.
 * Flow methods delegate directly to DAO for real-time reactivity.
 */
@Singleton
class UserRepositoryImpl @Inject constructor(
    private val dao: CalorieDao
) : UserRepository {

    override suspend fun registerUser(user: UserEntity): Result<Long> = try {
        if (dao.getUserByEmail(user.email) != null) {
            Result.failure(Exception("Email already exists"))
        } else {
            Result.success(dao.insertUser(user))
        }
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun login(email: String, password: String): Result<UserEntity> = try {
        val user = dao.login(email, password)
        if (user != null) Result.success(user)
        else Result.failure(Exception("Invalid email or password"))
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun getUserById(userId: Int): Result<UserEntity?> = try {
        Result.success(dao.getUserById(userId))
    } catch (e: Exception) { Result.failure(e) }

    /** Real-time user observer — emits whenever the users row changes. */
    override fun getUserByIdFlow(userId: Int): Flow<UserEntity?> =
        dao.getUserByIdFlow(userId)

    override suspend fun getUserByEmail(email: String): Result<UserEntity?> = try {
        Result.success(dao.getUserByEmail(email))
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun updateUser(user: UserEntity): Result<Unit> = try {
        dao.updateUser(user)
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun getAllUsers(): Result<List<UserEntity>> = try {
        Result.success(dao.getAllUsers())
    } catch (e: Exception) { Result.failure(e) }

    override fun getAllUsersFlow(): Flow<List<UserEntity>> =
        dao.getAllUsersFlow()

    override suspend fun deactivateAccount(userId: Int): Result<Unit> = try {
        dao.deactivateAccount(userId)
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun emailExists(email: String): Result<Boolean> = try {
        Result.success(dao.emailExists(email) > 0)
    } catch (e: Exception) { Result.failure(e) }

    /** Real-time user stats observer. */
    override fun getUserStatsFlow(userId: Int): Flow<UserStats?> =
        dao.getUserStatsFlow(userId)

    override suspend fun getUserStats(userId: Int): UserStats? =
        dao.getUserStats(userId)
}
