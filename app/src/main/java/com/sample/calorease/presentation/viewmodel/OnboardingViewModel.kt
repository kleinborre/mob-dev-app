package com.sample.calorease.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sample.calorease.data.model.UserStats
import com.sample.calorease.domain.model.ActivityLevel
import com.sample.calorease.domain.model.Gender
import com.sample.calorease.domain.model.WeightGoal
import com.sample.calorease.domain.repository.LegacyCalorieRepository
import com.sample.calorease.domain.usecase.CalculatorUseCase
import com.sample.calorease.util.ValidationUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingState(
    // Step 1: Name
    val nickname: String = "",
    val nicknameError: String? = null,
    
    // Step 2: Stats
    val gender: Gender = Gender.MALE,
    val height: String = "",
    val heightError: String? = null,
    val weight: String = "",
    val weightError: String? = null,
    val age: String = "",
    val ageError: String? = null,
    val activityLevel: ActivityLevel = ActivityLevel.SEDENTARY,
    
    // Step 3: Goals
    val targetWeight: String = "",
    val targetWeightError: String? = null,
    val weightGoal: WeightGoal = WeightGoal.MAINTAIN,
    
    // Step 4: Results (calculated)
    val bmiValue: Double = 0.0,
    val bmiStatus: String = "",
    val idealWeight: Double = 0.0,
    val bmr: Double = 0.0,
    val tdee: Double = 0.0,
    val goalCalories: Double = 0.0,
    
    val isLoading: Boolean = false,
    val isSaveSuccess: Boolean = false
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val repository: LegacyCalorieRepository,
    private val calculatorUseCase: CalculatorUseCase
) : ViewModel() {
    
    private val _onboardingState = MutableStateFlow(OnboardingState())
    val onboardingState: StateFlow<OnboardingState> = _onboardingState.asStateFlow()
    
    // Step 1: Name
    fun updateNickname(nickname: String) {
        _onboardingState.value = _onboardingState.value.copy(
            nickname = nickname,
            nicknameError = null
        )
    }
    
    fun validateName(): Boolean {
        val state = _onboardingState.value
        return if (!ValidationUtils.isValidName(state.nickname)) {
            _onboardingState.value = state.copy(nicknameError = "Nickname is required")
            false
        } else {
            true
        }
    }
    
    // Step 2: Stats
    fun updateGender(gender: Gender) {
        _onboardingState.value = _onboardingState.value.copy(gender = gender)
    }
    
    fun updateHeight(height: String) {
        _onboardingState.value = _onboardingState.value.copy(
            height = height,
            heightError = null
        )
    }
    
    fun updateWeight(weight: String) {
        _onboardingState.value = _onboardingState.value.copy(
            weight = weight,
            weightError = null
        )
    }
    
    fun updateAge(age: String) {
        _onboardingState.value = _onboardingState.value.copy(
            age = age,
            ageError = null
        )
    }
    
    fun updateActivityLevel(activityLevel: ActivityLevel) {
        _onboardingState.value = _onboardingState.value.copy(activityLevel = activityLevel)
    }
    
    fun validateStats(): Boolean {
        val state = _onboardingState.value
        var isValid = true
        
        if (!ValidationUtils.isPositiveNumber(state.height)) {
            _onboardingState.value = state.copy(heightError = "Enter valid height in cm")
            isValid = false
        }
        
        if (!ValidationUtils.isPositiveNumber(state.weight)) {
            _onboardingState.value = _onboardingState.value.copy(weightError = "Enter valid weight in kg")
            isValid = false
        }
        
        if (!ValidationUtils.isPositiveNumber(state.age)) {
            _onboardingState.value = _onboardingState.value.copy(ageError = "Enter valid age")
            isValid = false
        }
        
        return isValid
    }
    
    // Step 3: Goals
    fun updateTargetWeight(targetWeight: String) {
        _onboardingState.value = _onboardingState.value.copy(
            targetWeight = targetWeight,
            targetWeightError = null
        )
    }
    
    fun updateWeightGoal(weightGoal: WeightGoal) {
        _onboardingState.value = _onboardingState.value.copy(weightGoal = weightGoal)
    }
    
    fun validateGoals(): Boolean {
        val state = _onboardingState.value
        return if (!ValidationUtils.isPositiveNumber(state.targetWeight)) {
            _onboardingState.value = state.copy(targetWeightError = "Enter valid target weight in kg")
            false
        } else {
            true
        }
    }
    
    // Step 4: Calculate results
    fun calculateResults() {
        val state = _onboardingState.value
        
        if (!validateStats() || !validateGoals()) return
        
        val heightCm = state.height.toDouble()
        val weightKg = state.weight.toDouble()
        val ageYears = state.age.toInt()
        val targetWeightKg = state.targetWeight.toDouble()
        
        // Calculate BMI
        val bmiResult = calculatorUseCase.calculateBmi(weightKg, heightCm)
        
        // Calculate Ideal Weight
        val idealWeightResult = calculatorUseCase.calculateIdealWeight(heightCm, state.gender)
        
        // Calculate BMR
        val bmr = calculatorUseCase.calculateBmr(
            weightKg = weightKg,
            heightCm = heightCm,
            age = ageYears,
            gender = state.gender
        )
        
        // Calculate TDEE
        val tdee = calculatorUseCase.calculateTdee(bmr, state.activityLevel)
        
        // Calculate Goal Calories
        val goalCalories = calculatorUseCase.calculateGoalCalories(tdee, state.weightGoal)
        
        _onboardingState.value = state.copy(
            bmiValue = bmiResult.bmi,
            bmiStatus = bmiResult.status.name,
            idealWeight = idealWeightResult.idealWeight,
            bmr = bmr,
            tdee = tdee,
            goalCalories = goalCalories
        )
    }
    
    // Save to database
    fun saveUserStats() {
        viewModelScope.launch {
            val state = _onboardingState.value
            
            _onboardingState.value = state.copy(isLoading = true)
            
            val userStats = UserStats(
                id = 1,
                nickname = state.nickname,
                gender = state.gender,
                heightCm = state.height.toDouble(),
                weightKg = state.weight.toDouble(),
                age = state.age.toInt(),
                activityLevel = state.activityLevel,
                targetWeightKg = state.targetWeight.toDouble(),
                weightGoal = state.weightGoal,
                goalCalories = state.goalCalories
            )
            
            repository.insertUserStats(userStats)
            
            _onboardingState.value = state.copy(
                isLoading = false,
                isSaveSuccess = true
            )
        }
    }
    
    fun resetSuccessFlag() {
        _onboardingState.value = _onboardingState.value.copy(isSaveSuccess = false)
    }
}
