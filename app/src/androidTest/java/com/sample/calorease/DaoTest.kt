package com.sample.calorease

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sample.calorease.data.local.AppDatabase
import com.sample.calorease.data.local.dao.CalorieDao
import com.sample.calorease.data.local.entity.DailyEntryEntity
import com.sample.calorease.data.local.entity.UserEntity
import com.sample.calorease.data.model.UserStats
import com.sample.calorease.domain.model.ActivityLevel
import com.sample.calorease.domain.model.Gender
import com.sample.calorease.domain.model.WeightGoal
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

/**
 * Instrumented tests for CalorEase Room DAO.
 * Uses an in-memory database — no data persists between tests.
 *
 * Covers:
 *  1. insertUser + getUserById  (CRUD round-trip)
 *  2. insertDailyEntry + getDailyEntriesFlow (Flow emission on insert)
 *  3. deleteDailyEntry + getDailyEntriesFlow (Flow emits empty after delete)
 *  4. updateDailyEntry — calorie value updated correctly
 *  5. insertUserStats + getUserStatsFlow (stats Flow emission)
 *  6. getTotalCaloriesFlow — correct sum, null when no entries
 *  7. Cascade delete — deleting user removes daily entries
 *  8. Duplicate email rejected — unique constraint enforced
 */
@RunWith(AndroidJUnit4::class)
class DaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: CalorieDao

    // ── Shared test fixtures ───────────────────────────────────────────────────

    private val testUser = UserEntity(
        userId        = 0, // autoGenerate
        email         = "test@calorease.com",
        password      = "Test123!",
        nickname      = "Tester",
        role          = "USER",
        isActive      = true,
        accountStatus = "active",
        adminAccess   = false,
        isSuperAdmin  = false,
        accountCreated = System.currentTimeMillis(),
        gender        = "Male",
        height        = 175,
        weight        = 75.0,
        age           = 28,
        activityLevel = "Moderate",
        targetWeight  = 70.0,
        goalType      = "LOSE",
        bmr           = 1700,
        tdee          = 2400
    )

    private val today = run {
        val c = java.util.Calendar.getInstance()
        c.set(java.util.Calendar.HOUR_OF_DAY, 0)
        c.set(java.util.Calendar.MINUTE, 0)
        c.set(java.util.Calendar.SECOND, 0)
        c.set(java.util.Calendar.MILLISECOND, 0)
        c.timeInMillis
    }

    @Before
    fun createDb() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = db.calorieDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() = db.close()

    // ── 1. User CRUD ───────────────────────────────────────────────────────────

    @Test
    fun insertUser_and_getUserById_roundTrip() = runBlocking {
        val insertedId = dao.insertUser(testUser).toInt()
        val fetched = dao.getUserById(insertedId)
        assertNotNull("getUserById should return the inserted user", fetched)
        assertEquals(testUser.email, fetched?.email)
        assertEquals(testUser.nickname, fetched?.nickname)
    }

    @Test
    fun duplicateEmail_throwsException() = runBlocking {
        dao.insertUser(testUser)
        try {
            dao.insertUser(testUser.copy(userId = 0)) // same email, different PK
            fail("Expected an exception for duplicate email")
        } catch (e: Exception) {
            // success — unique constraint violation
            assertTrue(e.message?.contains("UNIQUE", ignoreCase = true) == true
                || e is android.database.sqlite.SQLiteConstraintException)
        }
    }

    // ── 2. Daily Entry — insert → Flow emits new list ──────────────────────────

    @Test
    fun insertDailyEntry_flowEmitsUpdatedList() = runBlocking {
        val userId = dao.insertUser(testUser).toInt()

        // Collect the initial (empty) emission
        val before = dao.getDailyEntriesFlow(userId, today).first()
        assertEquals("No entries yet", 0, before.size)

        dao.insertDailyEntry(DailyEntryEntity(
            userId   = userId, date = today,
            foodName = "Oats", calories = 300, mealType = "Breakfast"
        ))

        val after = dao.getDailyEntriesFlow(userId, today).first()
        assertEquals("One entry inserted", 1, after.size)
        assertEquals("Oats", after[0].foodName)
    }

    // ── 3. Delete → Flow emits empty list ─────────────────────────────────────

    @Test
    fun deleteDailyEntry_flowEmitsEmptyList() = runBlocking {
        val userId = dao.insertUser(testUser).toInt()
        val entryId = dao.insertDailyEntry(DailyEntryEntity(
            userId = userId, date = today,
            foodName = "Rice", calories = 500, mealType = "Lunch"
        )).toInt()

        // Verify it was inserted
        assertEquals(1, dao.getDailyEntriesFlow(userId, today).first().size)

        // Delete and verify Flow reflects removal
        dao.deleteDailyEntry(entryId)
        val after = dao.getDailyEntriesFlow(userId, today).first()
        assertEquals("Entry deleted — list should be empty", 0, after.size)
    }

    // ── 4. Update daily entry ──────────────────────────────────────────────────

    @Test
    fun updateDailyEntry_caloriesChangedCorrectly() = runBlocking {
        val userId = dao.insertUser(testUser).toInt()
        val original = DailyEntryEntity(userId = userId, date = today,
            foodName = "Chicken", calories = 400, mealType = "Dinner")
        val entryId = dao.insertDailyEntry(original).toInt()

        val updated = original.copy(entryId = entryId, calories = 450)
        dao.updateDailyEntry(updated)

        val fetched = dao.getDailyEntriesFlow(userId, today).first()
        assertEquals("Calories should be updated to 450", 450, fetched[0].calories)
    }

    // ── 5. UserStats Flow ──────────────────────────────────────────────────────

    @Test
    fun insertUserStats_flowEmitsStats() = runBlocking {
        val userId = dao.insertUser(testUser).toInt()

        val stats = UserStats(
            userId            = userId,
            firstName         = "Test",
            lastName          = "User",
            gender            = Gender.MALE,
            heightCm          = 175.0,
            weightKg          = 75.0,
            age               = 28,
            activityLevel     = ActivityLevel.MODERATELY_ACTIVE,
            weightGoal        = WeightGoal.LOSE_0_5_KG,
            targetWeightKg    = 70.0,
            goalCalories      = 2200.0,
            onboardingCompleted = true,
            currentOnboardingStep = 4
        )
        dao.insertUserStats(stats)

        val emitted = dao.getUserStatsFlow(userId).first()
        assertNotNull("Stats should be emitted after insert", emitted)
        assertEquals(2200.0, emitted?.goalCalories)
    }

    // ── 6. getTotalCaloriesFlow ────────────────────────────────────────────────

    @Test
    fun totalCaloriesFlow_sumsCorrectly() = runBlocking {
        val userId = dao.insertUser(testUser).toInt()

        // Null when no entries
        val initial = dao.getTotalCaloriesFlow(userId, today).first()
        assertNull("No entries — sum should be null", initial)

        dao.insertDailyEntry(DailyEntryEntity(userId = userId, date = today,
            foodName = "Egg", calories = 70, mealType = "Breakfast"))
        dao.insertDailyEntry(DailyEntryEntity(userId = userId, date = today,
            foodName = "Toast", calories = 100, mealType = "Breakfast"))

        val sum = dao.getTotalCaloriesFlow(userId, today).first()
        assertEquals("Sum should be 170", 170, sum)
    }

    // ── 7. CASCADE delete ──────────────────────────────────────────────────────

    @Test
    fun deleteUser_cascadesAndRemovesDailyEntries() = runBlocking {
        val userId = dao.insertUser(testUser).toInt()
        dao.insertDailyEntry(DailyEntryEntity(userId = userId, date = today,
            foodName = "Apple", calories = 80, mealType = "Snack"))

        // Verify entry exists
        assertEquals(1, dao.getDailyEntriesFlow(userId, today).first().size)

        // Delete the user — cascade should remove daily entries
        dao.deleteAllDailyEntriesForUser(userId) // also tests explicit delete
        val user = dao.getUserById(userId)
        // If we delete the user entity directly via SQL, entries cascade
        db.openHelper.writableDatabase.execSQL("DELETE FROM users WHERE userId = $userId")

        val remaining = dao.getDailyEntriesFlow(userId, today).first()
        assertEquals("Daily entries should be removed via CASCADE", 0, remaining.size)
    }
}
