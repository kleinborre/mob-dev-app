package com.sample.calorease.domain.usecase

import com.sample.calorease.domain.model.BmiResult
import com.sample.calorease.domain.model.IdealWeightResult
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

/**
 * Calculator UseCase for BMR and TDEE calculations
 * Uses Mifflin-St Jeor Formula for accurate calorie calculations
 */
@Singleton
class CalculatorUseCase @Inject constructor() {
    
    /**
     * Calculate Basal Metabolic Rate (BMR) using Mifflin-St Jeor Formula
     * 
     * Men: BMR = (10 × weight) + (6.25 × height) - (5 × age) + 5
     * Women: BMR = (10 × weight) + (6.25 × height) - (5 × age) - 161
     * 
     * @param weight Weight in kg
     * @param height Height in cm
     * @param age Age in years
     * @param gender "Male" or "Female"
     * @return BMR in kcal/day
     */
    fun calculateBMR(
        weight: Double,
        height: Int,
        age: Int,
        gender: String
    ): Int {
        val bmr = (10 * weight) + (6.25 * height) - (5 * age)
        return when (gender.lowercase()) {
            "male" -> (bmr + 5).roundToInt()
            "female" -> (bmr - 161).roundToInt()
            else -> (bmr - 161).roundToInt() // Default to female if unspecified
        }
    }
    
    /**
     * Calculate Total Daily Energy Expenditure (TDEE)
     * 
     * Activity Multipliers:
     * - Sedentary: 1.2 (little/no exercise)
     * - Light: 1.375 (light exercise 1-3 days/week)
     * - Moderate: 1.55 (moderate exercise 3-5 days/week)
     * - Active: 1.725 (hard exercise 6-7 days/week)
     * - Very Active: 1.9 (very hard exercise, physical job, or training twice per day)
     * 
     * @param bmr Basal Metabolic Rate
     * @param activityLevel Activity level string
     * @return TDEE in kcal/day
     */
    fun calculateTDEE(bmr: Int, activityLevel: String): Int {
        val multiplier = when (activityLevel.lowercase()) {
            "sedentary" -> 1.2
            "light" -> 1.375
            "moderate" -> 1.55
            "active" -> 1.725
            "very active", "veryactive" -> 1.9
            else -> 1.55 // Default to moderate
        }
        return (bmr * multiplier).roundToInt()
    }
    
    /**
     * Calculate Daily Target Calories based on goal
     * 
     * Goal adjustments:
     * - LOSE: -500 kcal (approx 0.5kg/week loss)
     * - GAIN: +500 kcal (approx 0.5kg/week gain)
     * - MAINTAIN: +0 kcal (maintain current weight)
     * 
     * **SAFETY CLAMP**: Minimum 1200 kcal/day to prevent dangerous calorie restriction
     * 
     * @param tdee Total Daily Energy Expenditure
     * @param goalType Goal type: "LOSE", "GAIN", or "MAINTAIN"
     * @return Daily calorie target with safety clamp applied
     */
    fun calculateDailyTarget(tdee: Int, goalType: String): Int {
        val adjustment = when (goalType.uppercase()) {
            "LOSE" -> -500
            "GAIN" -> +500
            "MAINTAIN" -> 0
            else -> 0 // Default to maintain
        }
        
        val target = tdee + adjustment
        
        // CRITICAL SAFETY CLAMP: Never go below 1200 kcal
        return if (target < 1200) {
            1200
        } else {
            target
        }
    }
    
    /**
     * Calculate all metrics at once
     * Convenience method to get BMR, TDEE, and Daily Target in one call
     * 
     * @return Triple of (BMR, TDEE, DailyTarget)
     */
    fun calculateAllMetrics(
        weight: Double,
        height: Int,
        age: Int,
        gender: String,
        activityLevel: String,
        goalType: String
    ): Triple<Int, Int, Int> {
        val bmr = calculateBMR(weight, height, age, gender)
        val tdee = calculateTDEE(bmr, activityLevel)
        val dailyTarget = calculateDailyTarget(tdee, goalType)
        return Triple(bmr, tdee, dailyTarget)
    }
    
    // ============ Additional Helper Methods for ViewModels ============
    
    /**
     * Calculate BMI (Body Mass Index)
     * Formula: weight (kg) / (height (m))²
     */
    fun calculateBmi(weight: Double, height: Double): BmiResult {
        val heightInMeters = height / 100.0
        val bmi = weight / (heightInMeters * heightInMeters)
        val status = when {
            bmi < 18.5 -> com.sample.calorease.domain.model.BmiStatus.UNDERWEIGHT
            bmi < 25.0 -> com.sample.calorease.domain.model.BmiStatus.NORMAL
            bmi < 30.0 -> com.sample.calorease.domain.model.BmiStatus.OVERWEIGHT
            else -> com.sample.calorease.domain.model.BmiStatus.OBESE
        }
        return BmiResult(bmi, status)
    }
    
    /**
     * Calculate ideal weight range based on BMI
     * Uses BMI range of 18.5-24.9 for normal weight
     * Returns the middle of the range
     */
    fun calculateIdealWeight(height: Double): Double {
        val heightInMeters = height / 100.0
        val idealBmi = 22.0 // Middle of normal BMI range
        return idealBmi * (heightInMeters * heightInMeters)
    }
    
    /**
     * Calculate goal calories (alias for calculateDailyTarget)
     * This method exists for backward compatibility with older ViewModels
     */
    fun calculateGoalCalories(tdee: Double, goalType: String): Double {
        return calculateDailyTarget(tdee.toInt(), goalType).toDouble()
    }
    
    /**
     * Legacy method: Calculate BMR with Double return type
     */
    fun calculateBmr(weight: Double, height: Double, age: Int, gender: String): Double {
        return calculateBMR(weight, height.toInt(), age, gender).toDouble()
    }
    
    /**
     * Legacy method: Calculate TDEE with Double return type
     */
    fun calculateTdee(bmr: Double, activityLevel: String): Double {
        return calculateTDEE(bmr.toInt(), activityLevel).toDouble()
    }
    
    // ============ Enum-based overloads for ViewModels ============
    
    /**
     * Calculate BMR with enum Gender
     */
    fun calculateBmr(
        weightKg: Double,
        heightCm: Double,
        age: Int,
        gender: com.sample.calorease.domain.model.Gender
    ): Double {
        return calculateBMR(weightKg, heightCm.toInt(), age, gender.name).toDouble()
    }
    
    /**
     * Calculate TDEE with enum ActivityLevel
     */
    fun calculateTdee(
        bmr: Double,
        activityLevel: com.sample.calorease.domain.model.ActivityLevel
    ): Double {
        return calculateTDEE(bmr.toInt(), activityLevel.name.replace("_", " ")).toDouble()
    }
    
    /**
     * Calculate goal calories with enum WeightGoal
     */
    fun calculateGoalCalories(
        tdee: Double,
        weightGoal: com.sample.calorease.domain.model.WeightGoal
    ): Double {
        val goalType = when (weightGoal) {
            com.sample.calorease.domain.model.WeightGoal.LOSE_0_25_KG,
            com.sample.calorease.domain.model.WeightGoal.LOSE_0_5_KG,
            com.sample.calorease.domain.model.WeightGoal.LOSE_1_KG -> "LOSE"
            com.sample.calorease.domain.model.WeightGoal.GAIN_0_25_KG,
            com.sample.calorease.domain.model.WeightGoal.GAIN_0_5_KG,
            com.sample.calorease.domain.model.WeightGoal.GAIN_1_KG -> "GAIN"
            com.sample.calorease.domain.model.WeightGoal.MAINTAIN -> "MAINTAIN"
        }
        return calculateDailyTarget(tdee.toInt(), goalType).toDouble()
    }
    
    /**
     * Calculate ideal weight with gender param (for ViewModels)
     */
    fun calculateIdealWeight(
        height: Double,
        gender: com.sample.calorease.domain.model.Gender
    ): IdealWeightResult {
        val idealWeight = calculateIdealWeight(height)
        return IdealWeightResult(idealWeight = idealWeight)
    }
}
