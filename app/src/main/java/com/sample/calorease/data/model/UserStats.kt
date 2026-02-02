package com.sample.calorease.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sample.calorease.domain.model.ActivityLevel
import com.sample.calorease.domain.model.Gender
import com.sample.calorease.domain.model.WeightGoal

@Entity(tableName = "user_stats")
data class UserStats(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nickname: String,
    val gender: Gender,
    val heightCm: Double,        // in cm
    val weightKg: Double,        // in kg
    val age: Int,                // Added age for BMR calculation
    val activityLevel: ActivityLevel,
    val weightGoal: WeightGoal,
    val targetWeightKg: Double,  // in kg
    val goalCalories: Double     // Daily calorie goal
)
