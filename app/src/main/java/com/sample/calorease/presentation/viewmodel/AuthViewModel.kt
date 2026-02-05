package com.sample.calorease.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sample.calorease.data.local.entity.UserEntity
import com.sample.calorease.data.model.UserStats
import com.sample.calorease.data.repository.LegacyCalorieRepository
import com.sample.calorease.data.session.SessionManager
import com.sample.calorease.domain.model.ActivityLevel
import com.sample.calorease.domain.model.Gender
import com.sample.calorease.domain.model.WeightGoal
import com.sample.calorease.domain.repository.UserRepository
import com.sample.calorease.domain.usecase.CalculatorUseCase
import com.sample.calorease.util.ValidationUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val name: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val nameError: String? = null,
    val isLoading: Boolean = false,
    val isLoginSuccess: Boolean = false,
    val isSignUpSuccess: Boolean = false,
    // ✅ NEW: Navigation destination flags
    val navigateToDashboard: Boolean = false,
    val navigateToOnboarding: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager,
    private val calculatorUseCase: CalculatorUseCase,
    private val legacyRepository: com.sample.calorease.domain.repository.LegacyCalorieRepository
) : ViewModel() {
    
    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    fun updateEmail(email: String) {
        _authState.value = _authState.value.copy(
            email = email.trimEnd(),  // Trim trailing whitespace
            emailError = null
        )
    }
    
    fun updatePassword(password: String) {
        _authState.value = _authState.value.copy(
            password = password.trimEnd(),  // Trim trailing whitespace
            passwordError = null
        )
    }
    
    fun updateConfirmPassword(confirmPassword: String) {
        _authState.value = _authState.value.copy(
            confirmPassword = confirmPassword.trimEnd(),  // Trim trailing whitespace
            confirmPasswordError = null
        )
    }
    
    fun updateName(name: String) {
        _authState.value = _authState.value.copy(
            name = name,
            nameError = null
        )
    }
    
    fun login() {
        val email = _authState.value.email.trim()  // Trim all whitespace for email
        val password = _authState.value.password.trimEnd()  // Trim trailing whitespace only
        
        // Validate
        val emailError = ValidationUtils.validateEmail(email)
        val passwordError = ValidationUtils.validatePassword(password)
        
        if (emailError != null || passwordError != null) {
            _authState.value = _authState.value.copy(
                emailError = emailError,
                passwordError = passwordError
            )
            return
        }
        
        
        _authState.value = _authState.value.copy(isLoading = true)
        
        viewModelScope.launch {
            // Check if email exists first for better error messages
            userRepository.getUserByEmail(email).onSuccess { existingUser ->
                if (existingUser == null) {
                    // Email doesn't exist
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        emailError = "No account found with this email"
                    )
                    Log.d("AuthViewModel", "Login failed: Email not found")
                    return@launch
                }
                
                // ✅ Phase 3: Check if account is deactivated
                if (existingUser.accountStatus == "deactivated") {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        emailError = "This account is no longer accessible as it was deleted. Please contact support."
                    )
                    Log.d("AuthViewModel", "Login failed: Account deactivated")
                    return@launch
                }
                
                // Email exists and account active, try login
                userRepository.login(email, password).onSuccess { user ->
                    // ✅ FIX: Always allow login - MainViewModel handles onboarding routing
                    // Removed block that prevented login for incomplete onboarding (catch-22 bug)
                    sessionManager.setLoggedIn(user.email)
                    sessionManager.saveUserId(user.userId)
                    sessionManager.saveRole(user.role)
                    
                    // PHASE 1: Save initial dashboard mode based on role
                    val initialMode = if (user.role == "admin") "admin" else "user"
                    sessionManager.saveLastDashboardMode(initialMode)
                    Log.d("AuthViewModel", "🔒 Saved initial lastDashboardMode=$initialMode (role=${user.role})")
                    
                    // ✅ CRITICAL: Check onboarding to determine destination
                    val userStats = legacyRepository.getUserStats(user.userId)
                    val onboardingCompleted = userStats?.onboardingCompleted ?: false
                    
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        isLoginSuccess = true,
                        navigateToDashboard = onboardingCompleted,
                        navigateToOnboarding = !onboardingCompleted
                    )
                    Log.d("AuthViewModel", "✅ Login successful for user: ${user.nickname} (userId=${user.userId}, onboarding=$onboardingCompleted)")
                }.onFailure { error ->
                    // Login failed - likely wrong password
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        passwordError = "Incorrect password"
                    )
                    Log.e("AuthViewModel", "Login error: Wrong password", error)
                }
            }.onFailure { error ->
                // Database error during email check
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    emailError = "Login failed. Please try again."
                )
                Log.e("AuthViewModel", "Database error during login", error)
            }
        }
    }
    
    fun signUp() {
        val email = _authState.value.email
        val password = _authState.value.password
        val confirmPassword = _authState.value.confirmPassword
        
        // Validate fields (name was removed from signup)
        val emailError = ValidationUtils.validateEmail(email)
        val passwordError = ValidationUtils.validatePassword(password)
        val confirmPasswordError = ValidationUtils.validateConfirmPassword(password, confirmPassword)
        
        if (emailError != null || passwordError != null || confirmPasswordError != null) {
            _authState.value = _authState.value.copy(
                emailError = emailError,
                passwordError = passwordError,
                confirmPasswordError = confirmPasswordError
            )
            return
        }
        
        _authState.value = _authState.value.copy(isLoading = true)
        
        viewModelScope.launch {
            try {
                // Check for duplicate email - properly unwrap Result
                val emailCheckResult = userRepository.getUserByEmail(email)
                
                var shouldProceed = false
                var emailExists = false
                
                emailCheckResult.onSuccess { existingUser ->
                    if (existingUser != null) {
                        emailExists = true
                    } else {
                        shouldProceed = true
                    }
                }.onFailure { error ->
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        emailError = "Database error: ${error.message}"
                    )
                    Log.e("AuthViewModel", "Email check error", error)
                }
                
                if (emailExists) {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        emailError = "This email is already registered"
                    )
                    Log.d("AuthViewModel", "Email already exists: $email")
                    return@launch
                }
                
                if (!shouldProceed) return@launch
                
                // Create new user (name collection moved to onboarding)
                val newUser = UserEntity(
                    email = email,
                    password = password.trim(),  // Trim to match login trimming
                    nickname = "",  // Will be set in onboarding
                    role = "USER",
                    isActive = true,
                    gender = "Male", // Default, will be updated in onboarding
                    height = 170, // Default
                    weight = 70.0, // Default
                    age = 25, // Default
                    activityLevel = "Moderate", // Default
                    targetWeight = 65.0, // Default
                    goalType = "MAINTAIN", // Default
                    bmr = 1500, // Will be calculated in onboarding
                    tdee = 2000 // Will be calculated in onboarding
                )
                
                val result = userRepository.registerUser(newUser)
                
                result.onSuccess { userId ->
                    // ✅ CRITICAL FIX: Don't create default user_stats here!
                    // user_stats will ONLY be created when onboarding completes
                    // This prevents wrong gender (MALE) showing instead of user's selection
                    
                    // Save session
                    sessionManager.setLoggedIn(email)
                    sessionManager.saveUserId(userId.toInt())
                    sessionManager.saveRole("USER")
                    
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        isSignUpSuccess = true
                    )
                    Log.d("AuthViewModel", "Sign up successful, userId: $userId")
                }.onFailure { error ->
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        emailError = error.message ?: "Sign up failed"
                    )
                    Log.e("AuthViewModel", "Sign up error", error)
                }
            } catch (e: Exception) {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    emailError = "Sign up failed"
                )
                Log.e("AuthViewModel", "Sign up error", e)
            }
        }
    }
    
    fun googleSignIn() {
        Log.d("AuthViewModel", "Google Sign In clicked")
    }
    
    fun resetSuccessFlags() {
        _authState.value = _authState.value.copy(
            isLoginSuccess = false,
            isSignUpSuccess = false,
            navigateToDashboard = false,  // ✅ Clear nav flags
            navigateToOnboarding = false
        )
    }
    
    fun resetPassword(email: String) {
        Log.d("AuthViewModel", "Reset password for: $email")
    }
}
