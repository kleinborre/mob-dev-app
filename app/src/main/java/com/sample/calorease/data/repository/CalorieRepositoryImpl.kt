package com.sample.calorease.data.repository

import com.sample.calorease.data.local.dao.CalorieDao
import com.sample.calorease.data.local.entity.DailyEntryEntity
import com.sample.calorease.domain.repository.CalorieRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * CalorieRepository Implementation
 * Provides daily entry database operations with error handling.
 * Flow methods delegate directly to DAO (Room handles background threading).
 */
@Singleton
class CalorieRepositoryImpl @Inject constructor(
    private val dao: CalorieDao
) : CalorieRepository {

    override suspend fun addDailyEntry(entry: DailyEntryEntity): Result<Long> = try {
        Result.success(dao.insertDailyEntry(entry))
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun getDailyEntries(userId: Int, date: Long): Result<List<DailyEntryEntity>> = try {
        Result.success(dao.getDailyEntries(userId, date))
    } catch (e: Exception) { Result.failure(e) }

    /** Real-time observable — Room emits on every table change automatically. */
    override fun getDailyEntriesFlow(userId: Int, date: Long): Flow<List<DailyEntryEntity>> =
        dao.getDailyEntriesFlow(userId, date)

    override suspend fun getTotalCalories(userId: Int, date: Long): Result<Int> = try {
        Result.success(dao.getTotalCaloriesForDay(userId, date) ?: 0)
    } catch (e: Exception) { Result.failure(e) }

    /** Real-time observable calorie sum — emits null when no entries exist. */
    override fun getTotalCaloriesFlow(userId: Int, date: Long): Flow<Int?> =
        dao.getTotalCaloriesFlow(userId, date)

    override suspend fun deleteDailyEntry(entryId: Int): Result<Unit> = try {
        dao.deleteDailyEntry(entryId, System.currentTimeMillis())
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun physicallyDeleteDailyEntry(entryId: Int): Result<Unit> = try {
        dao.physicallyDeleteDailyEntry(entryId)
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun updateDailyEntry(entry: DailyEntryEntity): Result<Unit> = try {
        dao.updateDailyEntry(entry)
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun getEntriesByMealType(userId: Int, date: Long, mealType: String): Result<List<DailyEntryEntity>> = try {
        Result.success(dao.getEntriesByMealType(userId, date, mealType))
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun getDailyEntriesByDateRange(userId: Int, startDate: Long, endDate: Long): Result<List<DailyEntryEntity>> = try {
        Result.success(dao.getDailyEntriesByDateRangeForUser(userId, startDate, endDate))
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun getAllFoodEntriesSortedByDate(userId: Int): Result<List<DailyEntryEntity>> = try {
        Result.success(dao.getAllFoodEntriesSortedByDate(userId))
    } catch (e: Exception) { Result.failure(e) }

    override fun getAllFoodEntriesFlow(userId: Int): kotlinx.coroutines.flow.Flow<List<DailyEntryEntity>> =
        dao.getAllFoodEntriesFlow(userId)
}
