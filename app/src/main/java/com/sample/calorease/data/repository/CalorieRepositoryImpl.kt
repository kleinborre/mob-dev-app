package com.sample.calorease.data.repository

import com.sample.calorease.data.local.dao.CalorieDao
import com.sample.calorease.data.local.entity.DailyEntryEntity
import com.sample.calorease.domain.repository.CalorieRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * CalorieRepository Implementation
 * Provides daily entry database operations with error handling
 */
@Singleton
class CalorieRepositoryImpl @Inject constructor(
    private val dao: CalorieDao
) : CalorieRepository {
    
    override suspend fun addDailyEntry(entry: DailyEntryEntity): Result<Long> {
        return try {
            val entryId = dao.insertDailyEntry(entry)
            Result.success(entryId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getDailyEntries(userId: Int, date: Long): Result<List<DailyEntryEntity>> {
        return try {
            val entries = dao.getDailyEntries(userId, date)
            Result.success(entries)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getTotalCalories(userId: Int, date: Long): Result<Int> {
        return try {
            val total = dao.getTotalCaloriesForDay(userId, date) ?: 0
            Result.success(total)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteDailyEntry(entryId: Int): Result<Unit> {
        return try {
            dao.deleteDailyEntry(entryId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getEntriesByMealType(userId: Int, date: Long, mealType: String): Result<List<DailyEntryEntity>> {
        return try {
            val entries = dao.getEntriesByMealType(userId, date, mealType)
            Result.success(entries)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
