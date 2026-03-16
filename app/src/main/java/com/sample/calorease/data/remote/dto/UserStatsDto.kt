package com.sample.calorease.data.remote.dto

/**
 * Data Transfer Object for storing UserStats entities in Firebase Firestore.
 * Natively mirroring the offline Room UserStats entity structure.
 */
data class UserStatsDto(
    val userId: Int = 0,
    val firstName: String = "",
    val lastName: String = "",
    val nickname: String? = null,
    val gender: String = "Male", // Converted Gender enum to String natively
    val heightCm: Double = 0.0,
    val weightKg: Double = 0.0,
    val age: Int = 0,
    val birthday: Long? = null,
    val activityLevel: String = "Sedentary", // Converted ActivityLevel enum
    val weightGoal: String = "Maintain",     // Converted WeightGoal enum
    val targetWeightKg: Double = 0.0,
    val goalCalories: Double = 0.0,
    
    val bmiValue: Double = 0.0,
    val bmiStatus: String = "",
    val idealWeight: Double = 0.0,
    val bmr: Double = 0.0,
    val tdee: Double = 0.0,
    
    val onboardingCompleted: Boolean = false,
    val currentOnboardingStep: Int = 1
)
