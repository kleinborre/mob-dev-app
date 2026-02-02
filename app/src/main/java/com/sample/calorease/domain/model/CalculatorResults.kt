package com.sample.calorease.domain.model

data class BmiResult(
    val bmi: Double,
    val status: BmiStatus
)

data class IdealWeightResult(
    val idealWeight: Double
)
