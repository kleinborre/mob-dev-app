package com.sample.calorease.data.local

import androidx.room.*
import com.sample.calorease.data.model.DailyEntry
import com.sample.calorease.data.model.UserStats
import kotlinx.coroutines.flow.Flow

@Dao
interface CalorieDao {
    
    // User Stats Operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserStats(userStats: UserStats)
    
    @Update
    suspend fun updateUserStats(userStats: UserStats)
    
    @Query("SELECT * FROM user_stats LIMIT 1")
    fun getUserStats(): Flow<UserStats?>
    
    @Query("SELECT * FROM user_stats LIMIT 1")
    suspend fun getUserStatsOnce(): UserStats?
    
    @Query("DELETE FROM user_stats")
    suspend fun deleteAllUserStats()
    
    // Daily Entry Operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyEntry(dailyEntry: DailyEntry)
    
    @Update
    suspend fun updateDailyEntry(dailyEntry: DailyEntry)
    
    @Query("SELECT * FROM daily_entries ORDER BY date DESC")
    fun getAllDailyEntries(): Flow<List<DailyEntry>>
    
    @Query("SELECT * FROM daily_entries WHERE date = :date LIMIT 1")
    suspend fun getDailyEntryByDate(date: String): DailyEntry?
    
    @Query("SELECT * FROM daily_entries WHERE date = :date LIMIT 1")
    fun getDailyEntryByDateFlow(date: String): Flow<DailyEntry?>
    
    @Query("SELECT * FROM daily_entries ORDER BY date DESC LIMIT :limit")
    fun getRecentDailyEntries(limit: Int = 30): Flow<List<DailyEntry>>
    
    @Query("SELECT * FROM daily_entries WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    suspend fun getDailyEntriesByDateRange(startDate: String, endDate: String): List<DailyEntry>
    
    @Delete
    suspend fun deleteDailyEntry(dailyEntry: DailyEntry)

    @Query("DELETE FROM daily_entries")
    suspend fun deleteAllDailyEntries()
}
