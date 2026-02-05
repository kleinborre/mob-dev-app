package com.sample.calorease.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.sample.calorease.data.local.AppDatabase
import com.sample.calorease.data.local.dao.CalorieDao
import com.sample.calorease.data.local.entity.UserEntity
import com.sample.calorease.data.model.UserStats
import com.sample.calorease.domain.model.Gender
import com.sample.calorease.domain.model.ActivityLevel
import com.sample.calorease.domain.model.WeightGoal
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
        // ✅ FIX: Removed database deletion - it was wiping ALL data on every app start!
        // Database should persist across app restarts for production use
        
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "calorease_db" // Phase 2 requirement: database name must be "calorease_db"
        )
            .fallbackToDestructiveMigration() // For development; remove in production
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    // ONE-TIME creation of test user when database is first created
                    CoroutineScope(Dispatchers.IO).launch {
                        val database = Room.databaseBuilder(
                            context,
                            AppDatabase::class.java,
                            "calorease_db"
                        ).build()
                        
                        val dao = database.calorieDao()
                        createTestUser(dao)
                        android.util.Log.d("AppModule", "✅ Database created with test user")
                    }
                }
            })
            .build()
    }
    
    private fun createTestUser(dao: CalorieDao) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Create test user account
                val testUser = UserEntity(
                    email = "test@calorease.com",
                    password = "Test123!",
                    nickname = "",  // Will use firstName from UserStats
                    role = "USER",
                    isActive = true,
                    accountStatus = "active",  // ✅ Phase 1: Active account
                    adminAccess = false,       // ✅ Phase 1: Regular user
                    isSuperAdmin = false,      // ✅ Phase B: Not a super admin
                    accountCreated = System.currentTimeMillis(),  // ✅ Phase 1: Creation timestamp
                    gender = "Male",
                    height = 175,
                    weight = 75.0,
                    age = 28,
                    activityLevel = "Moderate",
                    targetWeight = 70.0,
                    goalType = "LOSE",
                    bmr = 1700,
                    tdee = 2400
                )
                
                dao.insertUser(testUser)
                
                // ✅ Phase 3: Create admin user account
                val adminUser = UserEntity(
                    email = "admin@calorease.com",
                    password = "Admin123!",
                    nickname = "",
                    role = "ADMIN",
                    isActive = true,
                    accountStatus = "active",
                    adminAccess = true,         // ✅ Admin privileges
                    isSuperAdmin = true,        // ✅ Phase B: Super admin (cannot be demoted)
                    accountCreated = System.currentTimeMillis(),
                    gender = "Male",
                    height = 180,
                    weight = 80.0,
                    age = 35,
                    activityLevel = "Moderate",
                    targetWeight = 80.0,
                    goalType = "MAINTAIN",
                    bmr = 1800,
                    tdee = 2500
                )
                
                dao.insertUser(adminUser)
                
                // Create complete UserStats with all onboarding data for test user
                val birthCalendar = java.util.Calendar.getInstance().apply {
                    set(1996, 0, 15)  // Jan 15, 1996 (28 years old)
                }
                
                val testUserStats = UserStats(
                    userId = 1,  // Links to UserEntity.userId (test user has userId=1)
                    firstName = "Test",
                    lastName = "User",
                    nickname = null,  // Optional - will use firstName on dashboard
                    gender = Gender.MALE,
                    birthday = birthCalendar.timeInMillis,
                    age = 28,
                    heightCm = 175.0,
                    weightKg = 75.0,
                    activityLevel = ActivityLevel.MODERATELY_ACTIVE,
                    weightGoal = WeightGoal.LOSE_0_5_KG,
                    targetWeightKg = 70.0,
                    goalCalories = 2200.0,
                    onboardingCompleted = true,
                    currentOnboardingStep = 4
                )
                
                dao.insertUserStats(testUserStats)
                
                // ✅ Phase 2: Create admin user stats
                val adminBirthCalendar = java.util.Calendar.getInstance().apply {
                    set(1989, 5, 20)  // June 20, 1989 (35 years old)
                }
                
                val adminUserStats = UserStats(
                    userId = 2,  // Links to admin UserEntity (userId=2)
                    firstName = "Admin",
                    lastName = "User",
                    nickname = null,
                    gender = Gender.MALE,
                    birthday = adminBirthCalendar.timeInMillis,
                    age = 35,
                    heightCm = 180.0,
                    weightKg = 80.0,
                    activityLevel = ActivityLevel.MODERATELY_ACTIVE,
                    weightGoal = WeightGoal.LOSE_0_5_KG,
                    targetWeightKg = 75.0,
                    goalCalories = 2300.0,
                    onboardingCompleted = true,
                    currentOnboardingStep = 4
                )
                
                dao.insertUserStats(adminUserStats)
                
                android.util.Log.d("AppModule", "✅ Test user and Admin user created successfully!")
            } catch (e: Exception) {
                android.util.Log.e("AppModule", "❌ Failed to create test user", e)
            }
        }
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
