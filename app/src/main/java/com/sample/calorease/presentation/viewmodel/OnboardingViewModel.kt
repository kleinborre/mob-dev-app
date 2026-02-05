package com.sample.calorease.presentation.viewmodel

import android.util.Log
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
    val firstName: String = "",
    val firstNameError: String? = null,
    val lastName: String = "",
    val lastNameError: String? = null,
    val nickname: String = "",  // Optional
    
    // Step 2: Stats
    val gender: Gender = Gender.MALE,
    val height: String = "",
    val heightError: String? = null,
    val weight: String = "",
    val weightError: String? = null,
    val birthday: Long? = null,  // Birth date timestamp
    val birthdayError: String? = null,  // Validation error for birthday
    val age: String = "",        // Calculated from birthday
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
    private val calculatorUseCase: CalculatorUseCase,
    private val sessionManager: com.sample.calorease.data.session.SessionManager  // Add SessionManager
) : ViewModel() {
    
    private val _onboardingState = MutableStateFlow(OnboardingState())
    val onboardingState: StateFlow<OnboardingState> = _onboardingState.asStateFlow()
    
    init {
        // ✅ CRITICAL: Load existing onboarding state if resuming
        loadExistingState()
    }
    
    private fun loadExistingState() {
        viewModelScope.launch {
            try {
                val userId = sessionManager.getUserId() ?: return@launch
                val stats = repository.getUserStats(userId) ?: return@launch
                
                Log.d("OnboardingVM", "✅ Loaded existing state: step=${stats.currentOnboardingStep}")
                
                // Restore state from database
                _onboardingState.value = _onboardingState.value.copy(
                    firstName = stats.firstName,
                    lastName = stats.lastName,
                    nickname = stats.nickname ?: "",
                    gender = stats.gender,
                    height = stats.heightCm.toString(),
                    weight = stats.weightKg.toString(),
                    age = stats.age.toString(),
                    activityLevel = stats.activityLevel,
                    targetWeight = stats.targetWeightKg.toString(),
                    weightGoal = stats.weightGoal,
                    
                    // ✅ STEP 4 FIX: Restore calculated health metrics from database
                    bmiValue = stats.bmiValue,
                    bmiStatus = stats.bmiStatus,
                    idealWeight = stats.idealWeight,
                    bmr = stats.bmr,
                    tdee = stats.tdee,
                    goalCalories = stats.goalCalories
                )
            } catch (e: Exception) {
                Log.e("OnboardingVM", "Error loading existing state", e)
            }
        }
    }
    
    // Step 1: Name
    fun updateFirstName(firstName: String) {
        _onboardingState.value = _onboardingState.value.copy(
            firstName = firstName.trimEnd(),  // Trim trailing whitespace
            firstNameError = null
        )
    }
    
    fun updateLastName(lastName: String) {
        _onboardingState.value = _onboardingState.value.copy(
            lastName = lastName.trimEnd(),  // Trim trailing whitespace
            lastNameError = null
        )
    }
    
    fun updateNickname(nickname: String) {
        _onboardingState.value = _onboardingState.value.copy(nickname = nickname)
    }
    
    fun validateName(): Boolean {
        val state = _onboardingState.value
        var isValid = true
        
        if (state.firstName.isBlank()) {
            _onboardingState.value = state.copy(firstNameError = "First name is required")
            isValid = false
        }
        
        if (state.lastName.isBlank()) {
            _onboardingState.value = _onboardingState.value.copy(lastNameError = "Last name is required")
            isValid = false
        }
        
        // Nickname is optional, no validation needed
        return isValid
    }
    
    // Step 2: Stats
    fun updateGender(gender: Gender) {
        _onboardingState.value = _onboardingState.value.copy(gender = gender)
    }
    
    fun updateHeight(height: String) {
        // Real-time validation - clear error when valid
        val error = if (height.isNotEmpty()) {
            val h = height.toDoubleOrNull()
            when {
                h == null -> "Please enter a valid number"
                h < 100 -> "Height must be at least 100 cm"
                h > 250 -> "Height must be less than 250 cm"
                else -> null  // ✅ Valid - clear error
            }
        } else null
        
        _onboardingState.value = _onboardingState.value.copy(
            height = height,
            heightError = error
        )
    }
    
    fun updateWeight(weight: String) {
        // Real-time validation - clear error when valid
        val error = if (weight.isNotEmpty()) {
            val w = weight.toDoubleOrNull()
            when {
                w == null -> "Please enter a valid number"
                w < 30 -> "Weight must be at least 30 kg"
                w > 300 -> "Weight must be less than 300 kg"
                else -> null  // ✅ Valid - clear error
            }
        } else null
        
        _onboardingState.value = _onboardingState.value.copy(
            weight = weight,
            weightError = error
        )
    }
    
    fun updateBirthday(birthday: Long) {
        // Validate birthday before updating
        val error = validateBirthday(birthday)
        
        _onboardingState.value = _onboardingState.value.copy(
            birthday = birthday,
            birthdayError = error
        )
        
        // Auto-calculate age if valid birthday
        if (error == null) {
            val age = calculateAgeFromBirthday(birthday)
            _onboardingState.value = _onboardingState.value.copy(
                age = age.toString(),  // Convert Int to String
                ageError = null
            )
        }
    }
    
    private fun validateBirthday(birthday: Long): String? {
        val today = java.util.Calendar.getInstance()
        val birthDate = java.util.Calendar.getInstance().apply { timeInMillis = birthday }
        
        // Check if birthday is today (invalid)
        val isSameDay = today.get(java.util.Calendar.YEAR) == birthDate.get(java.util.Calendar.YEAR) &&
                today.get(java.util.Calendar.DAY_OF_YEAR) == birthDate.get(java.util.Calendar.DAY_OF_YEAR)
        
        if (isSameDay) {
            return "Please enter a valid date"
        }
        
        // Calculate age
        var age = today.get(java.util.Calendar.YEAR) - birthDate.get(java.util.Calendar.YEAR)
        if (today.get(java.util.Calendar.DAY_OF_YEAR) < birthDate.get(java.util.Calendar.DAY_OF_YEAR)) {
            age--
        }
        
        // Check minimum age (no minors under 13)
        if (age < 13) {
            return "You must be at least 13 years old to use this app"
        }
        
        return null
    }
    
    private fun calculateAgeFromBirthday(birthday: Long): Int {
        val today = java.util.Calendar.getInstance()
        val birthDate = java.util.Calendar.getInstance().apply { timeInMillis = birthday }
        
        var age = today.get(java.util.Calendar.YEAR) - birthDate.get(java.util.Calendar.YEAR)
        if (today.get(java.util.Calendar.DAY_OF_YEAR) < birthDate.get(java.util.Calendar.DAY_OF_YEAR)) {
            age--
        }
        return age
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
        
        if (state.birthday == null) {
            _onboardingState.value = _onboardingState.value.copy(ageError = "Please select your birth date")
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
    
    // ✅ PHASE 2: Progressive save after Step 1
    fun saveStepOne() {
        viewModelScope.launch {
            try {
                android.util.Log.d("OnboardingVM", "🔄 saveStepOne() STARTED")
                val userId = sessionManager.getUserId()
                if (userId == null) {
                    android.util.Log.e("OnboardingVM", "❌ saveStepOne() FAILED: No userId in session")
                    return@launch
                }
                
                val state = _onboardingState.value
                android.util.Log.d("OnboardingVM", "📝 Step 1 data: firstName=${state.firstName}, lastName=${state.lastName}, nickname=${state.nickname}")
                
                val userStats = UserStats(
                    userId = userId,
                    firstName = state.firstName,
                    lastName = state.lastName,
                    nickname = state.nickname.takeIf { it.isNotBlank() },
                    gender = Gender.MALE,
                    heightCm = 0.0,
                    weightKg = 0.0,
                    age = 0,
                    birthday = null,
                    activityLevel = ActivityLevel.SEDENTARY,
                    weightGoal = WeightGoal.MAINTAIN,
                    targetWeightKg = 0.0,
                    goalCalories = 0.0,
                    onboardingCompleted = false,
                    currentOnboardingStep = 1
                )
                
                repository.saveOnboardingState(userStats)
                android.util.Log.d("OnboardingVM", "✅ Step 1 SAVED successfully for userId=$userId")
            } catch (e: Exception) {
                android.util.Log.e("OnboardingVM", "❌ saveStepOne() EXCEPTION", e)
            }
        }
    }
    
    // ✅ PHASE 2: Progressive save after Step 2
    fun saveStepTwo() {
        viewModelScope.launch {
            val userId = sessionManager.getUserId() ?: return@launch
            val state = _onboardingState.value
            val existing = repository.getUserStats(userId)
            
            val userStats = UserStats(
                userId = userId,
                firstName = existing?.firstName ?: state.firstName,
                lastName = existing?.lastName ?: state.lastName,
                nickname = existing?.nickname ?: state.nickname.takeIf { it.isNotBlank() },
                gender = state.gender,
                heightCm = state.height.toDoubleOrNull() ?: 0.0,
                weightKg = state.weight.toDoubleOrNull() ?: 0.0,
                age = state.age.toIntOrNull() ?: 0,  // ✅ FIX: Int not Double
                birthday = state.birthday,
                activityLevel = state.activityLevel,
                weightGoal = WeightGoal.MAINTAIN,
                targetWeightKg = 0.0,
                goalCalories = 0.0,
                onboardingCompleted = false,
                currentOnboardingStep = 2
            )
            
            repository.saveOnboardingState(userStats)
            android.util.Log.d("OnboardingVM", "✅ Step 2 saved")
        }
    }
    
    // ✅ PHASE 2: Progressive save after Step 3
    // ✅ PHASE J FIX: Made suspend function to ensure completion before navigation
    suspend fun saveStepThree() {
        try {
            android.util.Log.d("OnboardingVM", "🔄 saveStepThree() STARTED")
            val userId = sessionManager.getUserId()
            if (userId == null) {
                android.util.Log.e("OnboardingVM", "❌ saveStepThree() FAILED: No userId in session")
                return
            }
            
            val state = _onboardingState.value
            android.util.Log.d("OnboardingVM", "📝 Step 3 data: targetWeight=${state.targetWeight}, weightGoal=${state.weightGoal}")
            
            val existing = repository.getUserStats(userId)
            if (existing == null) {
                android.util.Log.e("OnboardingVM", "❌ saveStepThree() FAILED: No existing user_stats found")
                return
            }
            
            val userStats = existing.copy(
                targetWeightKg = state.targetWeight.toDoubleOrNull() ?: 0.0,
                weightGoal = state.weightGoal,
                currentOnboardingStep = 3
            )
            
            repository.saveOnboardingState(userStats)
            android.util.Log.d("OnboardingVM", "✅ Step 3 SAVED successfully for userId=$userId")
        } catch (e: Exception) {
            android.util.Log.e("OnboardingVM", "❌ saveStepThree() EXCEPTION", e)
        }
    }
    
    // Step 4: Calculate results
    fun calculateResults() {
        val state = _onboardingState.value
        // ✅ REMOVED: if (!validateStats() || !validateGoals()) return
        // This was preventing calculations from running!
        
        Log.d("OnboardingVM", "📊 ============================")
        Log.d("OnboardingVM", "📊 CALCULATING HEALTH METRICS")
        Log.d("OnboardingVM", "📊 ============================")
        Log.d("OnboardingVM", "📊 Input Data:")
        Log.d("OnboardingVM", "  - Height: ${state.height} cm (raw string)")
        Log.d("OnboardingVM", "  - Weight: ${state.weight} kg (raw string)")
        Log.d("OnboardingVM", "  - Age: ${state.age} years (raw string)")
        Log.d("OnboardingVM", "  - Gender: ${state.gender}")
        Log.d("OnboardingVM", "  - Activity: ${state.activityLevel}")
        Log.d("OnboardingVM", "  - Target Weight: ${state.targetWeight} kg")
        Log.d("OnboardingVM", "  - Weight Goal: ${state.weightGoal}")
        
        try {
            // ✅ DEFENSIVE PARSING: Handle empty strings with defaults
            val heightCm = state.height.toDoubleOrNull() ?: run {
                Log.w("OnboardingVM", "⚠️ Height is empty or invalid, using minimum 0.1")
                0.1  // Avoid division by zero
            }
            val weightKg = state.weight.toDoubleOrNull() ?: run {
                Log.w("OnboardingVM", "⚠️ Weight is empty or invalid, using minimum 0.1")
                0.1
            }
            val ageYears = state.age.toIntOrNull() ?: run {
                Log.w("OnboardingVM", "⚠️ Age is empty or invalid, using minimum 1")
                1
            }
            val targetWeightKg = state.targetWeight.toDoubleOrNull() ?: run {
                Log.w("OnboardingVM", "⚠️ Target weight is empty or invalid, using weight")
                weightKg
            }
            
            // Validate non-zero values
            if (heightCm <= 0 || weightKg <= 0 || ageYears <= 0) {
                Log.e("OnboardingVM", "❌ Invalid values after parsing: height=$heightCm, weight=$weightKg, age=$ageYears")
                return
            }
            
            Log.d("OnboardingVM", "📊 Parsed Values:")
            Log.d("OnboardingVM", "  - Height: $heightCm cm")
            Log.d("OnboardingVM", "  - Weight: $weightKg kg")
            Log.d("OnboardingVM", "  - Age: $ageYears years")
            Log.d("OnboardingVM", "  - Target: $targetWeightKg kg")
            
            // Calculate BMI
            val bmiResult = calculatorUseCase.calculateBmi(weightKg, heightCm)
            Log.d("OnboardingVM", "📊 BMI Calculation:")
            Log.d("OnboardingVM", "  - BMI Value: ${bmiResult.bmi}")
            Log.d("OnboardingVM", "  - BMI Status: ${bmiResult.status}")
            
            // Calculate Ideal Weight (recommendation based on healthy BMI)
            val idealWeight = calculatorUseCase.calculateIdealWeight(heightCm)
            Log.d("OnboardingVM", "📊 Ideal Weight: $idealWeight kg")
            
            // Calculate BMR
            val bmr = calculatorUseCase.calculateBmr(
                weightKg = weightKg,
                heightCm = heightCm,
                age = ageYears,
                gender = state.gender
            )
            Log.d("OnboardingVM", "📊 BMR: $bmr cal/day")
            
            // Calculate TDEE
            val tdee = calculatorUseCase.calculateTdee(bmr, state.activityLevel)
            Log.d("OnboardingVM", "📊 TDEE: $tdee cal/day")
            
            // Calculate Goal Calories
            val goalCalories = calculatorUseCase.calculateGoalCalories(tdee, state.weightGoal)
            Log.d("OnboardingVM", "📊 Goal Calories: $goalCalories cal/day")
            
            // Update state
            _onboardingState.value = state.copy(
                bmiValue = bmiResult.bmi,
                bmiStatus = bmiResult.status.name,
                idealWeight = idealWeight,
                bmr = bmr,
                tdee = tdee,
                goalCalories = goalCalories
            )
            
            // ✅ PHASE C FIX #4: Confirm state was updated
            val updatedState = _onboardingState.value
            Log.d("OnboardingVM", "✅ STATE UPDATED SUCCESSFULLY!")
            Log.d("OnboardingVM", "📊 Final State Values:")
            Log.d("OnboardingVM", "  - bmiValue: ${updatedState.bmiValue}")
            Log.d("OnboardingVM", "  - bmiStatus: ${updatedState.bmiStatus}")
            Log.d("OnboardingVM", "  - idealWeight: ${updatedState.idealWeight}")
            Log.d("OnboardingVM", "  - bmr: ${updatedState.bmr}")
            Log.d("OnboardingVM", "  - tdee: ${updatedState.tdee}")
            Log.d("OnboardingVM", "  - goalCalories: ${updatedState.goalCalories}")
            
            Log.d("OnboardingVM", "✅ State updated successfully!")
            Log.d("OnboardingVM", "📊 Final State Values:")
            Log.d("OnboardingVM", "  - bmiValue: ${_onboardingState.value.bmiValue}")
            Log.d("OnboardingVM", "  - bmr: ${_onboardingState.value.bmr}")
            Log.d("OnboardingVM", "  - tdee: ${_onboardingState.value.tdee}")
            Log.d("OnboardingVM", "  - goalCalories: ${_onboardingState.value.goalCalories}")
            Log.d("OnboardingVM", "📊 ============================")
        } catch (e: NumberFormatException) {
            Log.e("OnboardingVM", "❌ NUMBER FORMAT ERROR in calculateResults", e)
            Log.e("OnboardingVM", "  - Height: '${state.height}' (${state.height.toDoubleOrNull()})")
            Log.e("OnboardingVM", "  - Weight: '${state.weight}' (${state.weight.toDoubleOrNull()})")
            Log.e("OnboardingVM", "  - Age: '${state.age}' (${state.age.toIntOrNull()})")
        } catch (e: Exception) {
            Log.e("OnboardingVM", "❌ CALCULATION ERROR", e)
            Log.e("OnboardingVM", "Error message: ${e.message}")
            e.printStackTrace()
        }
    }
    
    // Save to database
    fun saveUserStats() {
        viewModelScope.launch {
            try {
                val state = _onboardingState.value
                
                _onboardingState.value = state.copy(isLoading = true)
                
                // Get the logged-in user's ID from session
                val userId = sessionManager.getUserId() ?: 0  // Handle nullable Int
                
                if (userId == 0) {
                    _onboardingState.value = state.copy(isLoading = false)
                    Log.e("OnboardingViewModel", "❌ No user ID in session! Cannot save onboarding data.")
                    return@launch
                }
                
                Log.d("OnboardingViewModel", "💾 Saving onboarding data for userId=$userId")
                Log.d("OnboardingViewModel", "Data: ${state.firstName} ${state.lastName}, age=${state.age}, goalCal=${state.goalCalories}")
                
                val userStats = UserStats(
                    userId = userId,  // ✅ Use actual user ID from session, links to UserEntity
                    firstName = state.firstName,
                    lastName = state.lastName,
                    nickname = state.nickname.takeIf { it.isNotBlank() },  // Save only if not blank
                    gender = state.gender,
                    heightCm = state.height.toDouble(),
                    weightKg = state.weight.toDouble(),
                    age = state.age.toInt(),
                    birthday = state.birthday,  // Save birthday timestamp
                    activityLevel = state.activityLevel,
                    targetWeightKg = state.targetWeight.toDouble(),
                    weightGoal = state.weightGoal,
                    goalCalories = state.goalCalories,
                    
                    // ✅ STEP 4 FIX: Save calculated health metrics to database
                    bmiValue = state.bmiValue,
                    bmiStatus = state.bmiStatus,
                    idealWeight = state.idealWeight,
                    bmr = state.bmr,
                    tdee = state.tdee,
                    
                    onboardingCompleted = true,  // Mark as complete when saving from step 4
                    currentOnboardingStep = 4    // Final step
                )
                
                repository.insertUserStats(userStats)
                
                _onboardingState.value = state.copy(
                    isLoading = false,
                    isSaveSuccess = true
                )
                
                Log.d("OnboardingViewModel", "✅ Onboarding data saved successfully for userId=$userId (${state.firstName} ${state.lastName})")
            } catch (e: Exception) {
                _onboardingState.value = _onboardingState.value.copy(
                    isLoading = false,
                    isSaveSuccess = false
                )
                Log.e("OnboardingViewModel", "❌ CRASH: Error saving onboarding data", e)
                Log.e("OnboardingViewModel", "Error message: ${e.message}")
                Log.e("OnboardingViewModel", "Error cause: ${e.cause}")
                e.printStackTrace()
            }
        }
    }
    
    fun resetSuccessFlag() {
        _onboardingState.value = _onboardingState.value.copy(isSaveSuccess = false)
    }
    
    // ✅ NEW: Load saved onboarding progress for incomplete users
    fun loadProgress() {
        viewModelScope.launch {
            val userId = sessionManager.getUserId() ?: return@launch
            
            android.util.Log.d("OnboardingVM", "🔍 Loading saved progress for userId=$userId")
            
            val savedStats = repository.getUserStats(userId)
            
            if (savedStats != null && !savedStats.onboardingCompleted) {
                // Restore incomplete onboarding data
                _onboardingState.value = OnboardingState(
                    firstName = savedStats.firstName,
                    lastName = savedStats.lastName,
                    nickname = savedStats.nickname ?: "",  // ✅ Handle nullable
                    gender = savedStats.gender,
                    height = if (savedStats.heightCm > 0) savedStats.heightCm.toInt().toString() else "",
                    weight = if (savedStats.weightKg > 0) savedStats.weightKg.toInt().toString() else "",
                    age = if (savedStats.age > 0) savedStats.age.toString() else "",
                    birthday = savedStats.birthday,
                    activityLevel = savedStats.activityLevel,
                    weightGoal = savedStats.weightGoal,
                    targetWeight = if (savedStats.targetWeightKg > 0) savedStats.targetWeightKg.toInt().toString() else "",
                    bmiValue = 0.0,  // Will recalculate
                    bmr = 0.0,
                    tdee = 0.0,
                    goalCalories = 0.0,
                    isLoading = false,
                    isSaveSuccess = false
                )
                
                android.util.Log.d("OnboardingVM", "✅ Progress restored: ${savedStats.firstName} ${savedStats.lastName}")
                android.util.Log.d("OnboardingVM", "   Height: ${savedStats.heightCm}, Weight: ${savedStats.weightKg}")
                
                // ✅ CRITICAL FIX: Trigger calculations after loading saved data
                // This ensures Step 4 has values when navigating from saved progress
                if (savedStats.heightCm > 0 && savedStats.weightKg > 0 && savedStats.age > 0) {
                    android.util.Log.d("OnboardingVM", "🔄 Triggering calculations with loaded data...")
                    calculateResults()
                }
            } else {
                android.util.Log.d("OnboardingVM", "No saved progress or onboarding already complete")
            }
        }
    }
}
