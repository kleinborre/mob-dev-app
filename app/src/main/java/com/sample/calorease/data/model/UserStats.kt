package com.sample.calorease.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.sample.calorease.data.local.entity.UserEntity
import com.sample.calorease.domain.model.ActivityLevel
import com.sample.calorease.domain.model.Gender
import com.sample.calorease.domain.model.WeightGoal

@Entity(
    tableName = "user_stats",
    foreignKeys = [ForeignKey(
        entity = UserEntity::class,
        parentColumns = ["userId"],
        childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["userId"], unique = true)]
)
data class UserStats(
    @PrimaryKey
    val userId: Int,  // Links to UserEntity.userId (NOT auto-generated!)
    val firstName: String,       // User's first name (required)
    val lastName: String,        // User's last name (required)
    val nickname: String? = null, // Optional nickname for personalization
    val gender: Gender,
    val heightCm: Double,        // in cm
    val weightKg: Double,        // in kg
    val age: Int,                // Calculated from birthday or entered directly
    val birthday: Long? = null,  // Birth date timestamp (nullable for backward compatibility)
    val activityLevel: ActivityLevel,
    val weightGoal: WeightGoal,
    val targetWeightKg: Double,  // in kg
    val goalCalories: Double,    // Daily calorie goal
    
    // ✅ STEP 4 FIX: Add calculated health metrics to database
    val bmiValue: Double = 0.0,      // Body Mass Index
    val bmiStatus: String = "",       // BMI category (Underweight, Normal, etc.)
    val idealWeight: Double = 0.0,    // Ideal weight based on height
    val bmr: Double = 0.0,            // Basal Metabolic Rate (cal/day)
    val tdee: Double = 0.0,           // Total Daily Energy Expenditure (cal/day)
    
    val onboardingCompleted: Boolean = false,  // Has user finished all 4 onboarding steps?
    val currentOnboardingStep: Int = 1        // Which step are they on? (1-4)
)
