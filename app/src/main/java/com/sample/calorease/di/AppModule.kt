package com.sample.calorease.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.sample.calorease.data.local.AppDatabase
import com.sample.calorease.data.local.dao.CalorieDao
import com.sample.calorease.data.local.entity.UserEntity
import com.sample.calorease.data.model.UserStats
import com.sample.calorease.domain.model.ActivityLevel
import com.sample.calorease.domain.model.Gender
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

import com.sample.calorease.data.remote.FirestoreService
import com.sample.calorease.data.remote.FirestoreServiceImpl
import com.sample.calorease.data.remote.api.AbstractEmailApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAbstractEmailApi(): AbstractEmailApi {
        return Retrofit.Builder()
            .baseUrl(AbstractEmailApi.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AbstractEmailApi::class.java)
    }

    @Provides
    @Singleton
    fun provideFirestoreService(): FirestoreService {
        return FirestoreServiceImpl()
    }

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        // ─────────────────────────────────────────────────────────────────────
        // PERSISTENCE FIX (Sprint 3.1)
        //
        // ❌ REMOVED: fallbackToDestructiveMigration()
        //    This was the #1 cause of data loss — Room silently wiped and
        //    recreated the entire database whenever it encountered an unknown
        //    schema version (e.g. after a clean install → forced upgrade test).
        //
        // ✅ ADDED:  addMigrations(MIGRATION_12_13) — all schema changes are
        //    handled with explicit, non-destructive SQL migrations.
        //    Future engineers must ADD a Migration object for every version bump.
        //
        // ❌ REMOVED: redundant second Room.databaseBuilder() inside the
        //    onCreate callback — that created a race-condition second DB
        //    connection that could leave the main DB in an inconsistent state.
        //    Seed data now written through the already-open SupportSQLiteDatabase.
        // ─────────────────────────────────────────────────────────────────────
        return Room.databaseBuilder(
            context.applicationContext,     // always use applicationContext — not activity context
            AppDatabase::class.java,
            "calorease_db"
        )
            .addMigrations(
                AppDatabase.MIGRATION_12_13, 
                AppDatabase.MIGRATION_13_14,
                AppDatabase.MIGRATION_14_15,
                AppDatabase.MIGRATION_15_16
            )
            // NOTE: fallbackToDestructiveMigration() has been intentionally REMOVED
            // to prevent silent data wipes. If you add a new DB version, add a
            // Migration object in AppDatabase.kt first.
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    // ONE-TIME seed: only executed on a brand-new database install.
                    // Uses a separate coroutine on IO dispatcher; Room queues the
                    // writes after the schema is fully initialised.
                    CoroutineScope(Dispatchers.IO).launch {
                        seedDefaultUsers(db)
                        android.util.Log.d("AppModule", "Database seeded with default users")
                    }
                }
            })
            .build()
    }

    /**
     * Seeds the database on first install using raw SQL on the already-open
     * SupportSQLiteDatabase — avoids the old bug of opening a second Room
     * instance inside the callback.
     *
     * Only called ONCE when the database file is first created.
     */
    private fun seedDefaultUsers(db: SupportSQLiteDatabase) {
        try {
            val now = System.currentTimeMillis()

            // Test user: male, 28y, 175cm, 75kg, goal LOSE_0_5_KG
            // BMR(Mifflin) = 10*75 + 6.25*175 - 5*28 + 5 = 1759
            // TDEE = 1759 * 1.55 (MODERATELY_ACTIVE) = 2726; goalCals = 2726 - 500 = 2226
            db.execSQL("""
                INSERT OR IGNORE INTO users
                (email, password, nickname, role, isActive, accountStatus,
                 adminAccess, isSuperAdmin, accountCreated,
                 gender, height, weight, age, activityLevel,
                 targetWeight, goalType, bmr, tdee, googleId, isEmailVerified, lastUpdated)
                VALUES
                ('palenciafrancisadrian@gmail.com', 'TestUser123!', 'Test User', 'USER', 1, 'active',
                 0, 0, $now,
                 'Male', 175, 75.0, 28, 'Moderate',
                 70.0, 'LOSE', 1759, 2726, NULL, 1, $now)
            """.trimIndent())

            // Admin user: male, 35y, 180cm, 80kg, goal MAINTAIN
            // BMR = 10*80 + 6.25*180 - 5*35 + 5 = 1880
            // TDEE = 1880 * 1.55 = 2914; goalCals = 2914 (maintain)
            db.execSQL("""
                INSERT OR IGNORE INTO users
                (email, password, nickname, role, isActive, accountStatus,
                 adminAccess, isSuperAdmin, accountCreated,
                 gender, height, weight, age, activityLevel,
                 targetWeight, goalType, bmr, tdee, googleId, isEmailVerified, lastUpdated)
                VALUES
                ('blitzalexandra19@gmail.com', 'AdminUser123!', 'Admin User', 'ADMIN', 1, 'active',
                 1, 1, $now,
                 'Male', 180, 80.0, 35, 'Moderate',
                 80.0, 'MAINTAIN', 1880, 2914, NULL, 1, $now)
            """.trimIndent())

            // user_stats for test user (userId = 1) - onboardingCompleted = 1 to skip onboarding
            // Enum fields MUST match Kotlin enum name() exactly: MALE, MODERATELY_ACTIVE, LOSE_0_5_KG
            val testBirthday = java.util.Calendar.getInstance().apply { set(1996, 0, 15) }.timeInMillis
            db.execSQL("""
                INSERT OR IGNORE INTO user_stats
                (userId, firstName, lastName, gender, birthday, age,
                 heightCm, weightKg, activityLevel, weightGoal,
                 targetWeightKg, goalCalories,
                 bmiValue, bmiStatus, idealWeight, bmr, tdee,
                 onboardingCompleted, currentOnboardingStep)
                VALUES
                (1, 'Test', 'User', 'MALE', $testBirthday, 28,
                 175.0, 75.0, 'MODERATELY_ACTIVE', 'LOSE_0_5_KG',
                 70.0, 2226.0,
                 24.49, 'Normal', 68.75, 1759.0, 2726.0,
                 1, 4)
            """.trimIndent())

            // user_stats for admin user (userId = 2) - MAINTAIN goal, onboardingCompleted = 1
            val adminBirthday = java.util.Calendar.getInstance().apply { set(1989, 5, 20) }.timeInMillis
            db.execSQL("""
                INSERT OR IGNORE INTO user_stats
                (userId, firstName, lastName, gender, birthday, age,
                 heightCm, weightKg, activityLevel, weightGoal,
                 targetWeightKg, goalCalories,
                 bmiValue, bmiStatus, idealWeight, bmr, tdee,
                 onboardingCompleted, currentOnboardingStep)
                VALUES
                (2, 'Admin', 'User', 'MALE', $adminBirthday, 35,
                 180.0, 80.0, 'MODERATELY_ACTIVE', 'MAINTAIN',
                 80.0, 2914.0,
                 24.69, 'Normal', 72.0, 1880.0, 2914.0,
                 1, 4)
            """.trimIndent())

            android.util.Log.d("AppModule", "Default users seeded with full onboarding data")
        } catch (e: Exception) {
            android.util.Log.e("AppModule", "Failed to seed default users: ${e.message}", e)
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
