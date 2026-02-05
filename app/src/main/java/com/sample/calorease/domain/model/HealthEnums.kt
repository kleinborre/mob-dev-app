package com.sample.calorease.domain.model

enum class Gender {
    MALE,
    FEMALE
}

enum class ActivityLevel(val multiplier: Double) {
    SEDENTARY(1.2),
    LIGHTLY_ACTIVE(1.375),
    MODERATELY_ACTIVE(1.55),
    VERY_ACTIVE(1.725),
    EXTRA_ACTIVE(1.9)
}

enum class WeightGoal(val calorieAdjustment: Int) {
    LOSE_1_KG(-1000),     // Lose 1kg per week (most aggressive)
    LOSE_0_5_KG(-500),    // Lose 0.5kg per week
    LOSE_0_25_KG(-250),   // Lose 0.25kg per week (least aggressive)
    MAINTAIN(0),          // Maintain current weight
    GAIN_0_25_KG(250),    // Gain 0.25kg per week (least aggressive)
    GAIN_0_5_KG(500),     // Gain 0.5kg per week
    GAIN_1_KG(1000)       // Gain 1kg per week (most aggressive)
}

enum class BmiStatus {
    UNDERWEIGHT,    // BMI < 18.5
    NORMAL,         // BMI 18.5 - 24.99
    OVERWEIGHT,     // BMI 25 - 29.99
    OBESE           // BMI >= 30
}
