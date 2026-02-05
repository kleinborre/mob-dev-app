package com.sample.calorease.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * User Entity for Room Database
 * Stores complete user information including credentials, profile, and calculated metrics
 */
@Entity(
    tableName = "users",
    indices = [Index(value = ["email"], unique = true)]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val userId: Int = 0,
    
    // Authentication
    val email: String,
    val password: String, // Plain text for local prototype
    
    // Profile
    val nickname: String,
    val role: String = "USER", // "USER" or "ADMIN"
    val isActive: Boolean = true,
    
    // Admin Features (Phase 1)
    val accountStatus: String = "active",  // "active" or "deactivated"
    val adminAccess: Boolean = false,       // true = admin, false = regular user
    val isSuperAdmin: Boolean = false, // ✅ Phase B: Super admin (cannot be demoted)
    val accountCreated: Long = System.currentTimeMillis(), // Timestamp when account created
    
    // Physical Stats
    val gender: String, // "Male" or "Female"
    val height: Int, // in cm
    val weight: Double, // in kg
    val age: Int, // in years
    val activityLevel: String, // Activity multiplier level
    
    // Goals
    val targetWeight: Double, // in kg
    val goalType: String, // "LOSE", "GAIN", "MAINTAIN"
    
    // Calculated Metrics
    val bmr: Int, // Basal Metabolic Rate (kcal)
    val tdee: Int // Total Daily Energy Expenditure (kcal)
)
