package com.sample.calorease.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.sample.calorease.data.local.AppDatabase
import com.sample.calorease.data.local.dao.CalorieDao
import com.sample.calorease.data.local.entity.UserEntity
import com.sample.calorease.data.repository.CalorieRepositoryImpl
import com.sample.calorease.data.repository.LegacyCalorieRepositoryImpl
import com.sample.calorease.data.repository.UserRepositoryImpl
import com.sample.calorease.data.session.SessionManager
import com.sample.calorease.domain.repository.CalorieRepository
import com.sample.calorease.domain.repository.LegacyCalorieRepository
import com.sample.calorease.domain.repository.UserRepository
import com.sample.calorease.domain.usecase.CalculatorUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "calorease_db" // Phase 2 requirement: database name must be "calorease_db"
        )
            .fallbackToDestructiveMigration() // For development; remove in production
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    // Prepopulate database with test user
                    CoroutineScope(Dispatchers.IO).launch {
                        val database = Room.databaseBuilder(
                            context,
                            AppDatabase::class.java,
                            "calorease_db"
                        ).build()
                        
                        val dao = database.calorieDao()
                        
                        // Create test user
                        val testUser = UserEntity(
                            email = "test@calorease.com",
                            password = "Test123!",
                            nickname = "TestUser",
                            role = "USER",
                            isActive = true,
                            gender = "Male",
                            height = 175,
                            weight = 75.0,
                            age = 28,
                            activityLevel = "Moderate",
                            targetWeight = 70.0,
                            goalType = "LOSE",
                            bmr = 1663,
                            tdee = 2578
                        )
                        
                        dao.insertUser(testUser)
                    }
                }
            })
            .build()
    }
    
    @Provides
    @Singleton
    fun provideCalorieDao(database: AppDatabase): CalorieDao {
        return database.calorieDao()
    }
    
    @Provides
    @Singleton
    fun provideUserRepository(dao: CalorieDao): UserRepository {
        return UserRepositoryImpl(dao)
    }
    
    @Provides
    @Singleton
    fun provideCalorieRepository(dao: CalorieDao): CalorieRepository {
        return CalorieRepositoryImpl(dao)
    }
    
    @Provides
    @Singleton
    fun provideLegacyCalorieRepository(
        dao: CalorieDao,
        sessionManager: SessionManager
    ): LegacyCalorieRepository {
        return LegacyCalorieRepositoryImpl(dao, sessionManager)
    }

    @Provides
    @Singleton
    fun provideSessionManager(
        @ApplicationContext context: Context
    ): SessionManager {
        return SessionManager(context)
    }
    
    @Provides
    @Singleton
    fun provideCalculatorUseCase(): CalculatorUseCase {
        return CalculatorUseCase()
    }
}
