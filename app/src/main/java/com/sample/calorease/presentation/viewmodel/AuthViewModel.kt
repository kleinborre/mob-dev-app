package com.sample.calorease.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sample.calorease.data.local.entity.UserEntity
import com.sample.calorease.data.session.SessionManager
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
    val isSignUpSuccess: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager,
    private val calculatorUseCase: CalculatorUseCase
) : ViewModel() {
    
    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    fun updateEmail(email: String) {
        _authState.value = _authState.value.copy(
            email = email,
            emailError = null
        )
    }
    
    fun updatePassword(password: String) {
        _authState.value = _authState.value.copy(
            password = password,
            passwordError = null
        )
    }
    
    fun updateConfirmPassword(confirmPassword: String) {
        _authState.value = _authState.value.copy(
            confirmPassword = confirmPassword,
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
        val email = _authState.value.email
        val password = _authState.value.password
        
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
            try {
                // Use new UserRepository login
                val result = userRepository.login(email, password)
                
                result.onSuccess { user ->
                    // Save session with userId and role
                    sessionManager.setLoggedIn(user.email)
                    sessionManager.saveUserId(user.userId)
                    sessionManager.saveRole(user.role)
                    
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        isLoginSuccess = true
                    )
                    Log.d("AuthViewModel", "Login successful for user: ${user.nickname}")
                }.onFailure { error ->
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        emailError = error.message ?: "Invalid credentials"
                    )
                    Log.e("AuthViewModel", "Login error", error)
                }
            } catch (e: Exception) {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    emailError = "Login failed"
                )
                Log.e("AuthViewModel", "Login error", e)
            }
        }
    }
    
    fun signUp() {
        val email = _authState.value.email
        val password = _authState.value.password
        val confirmPassword = _authState.value.confirmPassword
        val name = _authState.value.name
        
        // Validate all fields
        val emailError = ValidationUtils.validateEmail(email)
        val passwordError = ValidationUtils.validatePassword(password)
        val confirmPasswordError = ValidationUtils.validateConfirmPassword(password, confirmPassword)
        val nameError = ValidationUtils.validateName(name)
        
        if (emailError != null || passwordError != null || 
            confirmPasswordError != null || nameError != null) {
            _authState.value = _authState.value.copy(
                emailError = emailError,
                passwordError = passwordError,
                confirmPasswordError = confirmPasswordError,
                nameError = nameError
            )
            return
        }
        
        _authState.value = _authState.value.copy(isLoading = true)
        
        viewModelScope.launch {
            try {
                // Create new user with default values
                // Note: User will complete onboarding to set these properly
                val newUser = UserEntity(
                    email = email,
                    password = password,
                    nickname = name,
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
        // TODO: Implement Google Sign In
        Log.d("AuthViewModel", "Google Sign In clicked")
    }
    
    fun resetSuccessFlags() {
        _authState.value = _authState.value.copy(
            isLoginSuccess = false,
            isSignUpSuccess = false
        )
    }
    
    fun resetPassword(email: String) {
        // TODO: Implement password reset
        Log.d("AuthViewModel", "Reset password for: $email")
    }
}
