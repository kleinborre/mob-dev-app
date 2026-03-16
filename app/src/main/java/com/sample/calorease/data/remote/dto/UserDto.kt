package com.sample.calorease.data.remote.dto

/**
 * Data Transfer Object for storing User entities in Firebase Firestore.
 * Matches local UserEntity but adds nullability/defaults suitable for NoSQL parsing.
 */
data class UserDto(
    val userId: Int = 0,
    val email: String = "",
    // Skip password — handled safely via FirebaseAuth internally, no need to replicate locally in NoSQL
    val googleId: String? = null,
    val isEmailVerified: Boolean = false,
    val nickname: String = "",
    val role: String = "USER",
    val isActive: Boolean = true,
    val accountStatus: String = "active",
    val adminAccess: Boolean = false,
    val isSuperAdmin: Boolean = false,
    val accountCreated: Long = 0L,
    val gender: String = "",
    val height: Int = 0,
    val weight: Double = 0.0,
    val age: Int = 0,
    val activityLevel: String = "",
    val targetWeight: Double = 0.0,
    val goalType: String = "",
    val bmr: Int = 0,
    val tdee: Int = 0,
    val lastUpdated: Long = System.currentTimeMillis() // Sprint 4 Phase 1: Sync Timestamp
)
