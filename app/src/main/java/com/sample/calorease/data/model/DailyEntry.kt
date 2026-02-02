package com.sample.calorease.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_entries")
data class DailyEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val date: String,               // Format: "yyyy-MM-dd"
    val caloriesConsumed: Double = 0.0,
    val exerciseCalories: Double = 0.0,
    val weightRecorded: Double? = null     // Optional weight recording for the day (in kg)
)
