package com.sample.calorease.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.tasks.Tasks
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
import kotlinx.coroutines.tasks.await
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
    // Navigation destination flags
    val navigateToDashboard: Boolean = false,
    val navigateToOnboarding: Boolean = false,
    // Google Sign-In error (null = no error)
    val googleSignInError: String? = null
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
        val trimmedEmail = email.trim()
        Log.d("AuthViewModel", "updateEmail called: input='$email', trimmed='$trimmedEmail'")
        _authState.value = _authState.value.copy(
            email = trimmedEmail,
            emailError = null
        )
        Log.d("AuthViewModel", "State updated: email='${_authState.value.email}'")
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
    
    fun login(email: String = _authState.value.email, password: String = _authState.value.password) {
        Log.d("AuthViewModel", "login() called")
        Log.d("AuthViewModel", "Email parameter: '$email'")
        Log.d("AuthViewModel", "Password length: ${password.length}")
        
        // Trim the passed parameters
        val trimmedEmail = email.trim()
        val trimmedPassword = password.trimEnd()
        Log.d("AuthViewModel", "Email after trim: '$trimmedEmail'")
        
        // Validate
        val emailError = ValidationUtils.validateEmail(trimmedEmail)
        val passwordError = ValidationUtils.validatePassword(trimmedPassword)
        
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
            userRepository.getUserByEmail(trimmedEmail).onSuccess { existingUser ->
                if (existingUser == null) {
                    // Email doesn't exist
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        emailError = "No account found with this email"
                    )
                    Log.d("AuthViewModel", "Login failed: Email not found")
                    return@launch
                }
                
                // Phase 3: Check if account is deactivated
                if (existingUser.accountStatus == "deactivated") {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        emailError = "This account is no longer accessible as it was deleted. Please contact support."
                    )
                    Log.d("AuthViewModel", "Login failed: Account deactivated")
                    return@launch
                }
                
                // Email exists and account active, try login
                userRepository.login(trimmedEmail, trimmedPassword).onSuccess { user ->
                    // FIX: Always allow login - MainViewModel handles onboarding routing
                    // Removed block that prevented login for incomplete onboarding (catch-22 bug)
                    sessionManager.setLoggedIn(user.email)
                    sessionManager.saveUserId(user.userId)
                    sessionManager.saveRole(user.role)
                    
                    // BUGFIX Issue 7: Save last login email for persistence after logout
                    sessionManager.saveLastLoginEmail(user.email)
                    
                    // BUG3 FIX: Preserve last mode across logout/login.
                    // Only set a default mode if there is no previously saved mode.
                    // If admin toggled to user before logout, that must be preserved.
                    val existingMode = sessionManager.getLastDashboardMode()
                    val modeWasSaved = existingMode != "user" || user.role == "user"
                    if (!modeWasSaved) {
                        // No prior preference — set role-based default
                        val initialMode = if (user.role == "admin") "admin" else "user"
                        sessionManager.saveLastDashboardMode(initialMode)
                        Log.d("AuthViewModel", "No prior mode — saved default: $initialMode (role=${user.role})")
                    } else {
                        Log.d("AuthViewModel", "Preserved existing mode: $existingMode (role=${user.role})")
                    }
                    
                    // CRITICAL: Check onboarding status using direct DAO path (not legacy stub)
                    // userRepository.getUserStats() was added in Sprint 3.1 and calls dao.getUserStats()
                    // directly — this correctly reads the `onboardingCompleted` flag saved during onboarding.
                    val userStats = userRepository.getUserStats(user.userId)
                    val onboardingCompleted = userStats?.onboardingCompleted ?: false

                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        isLoginSuccess = true,
                        navigateToDashboard = onboardingCompleted,
                        navigateToOnboarding = !onboardingCompleted
                    )
                    Log.d("AuthViewModel", "Login successful for user: ${user.nickname} (userId=${user.userId}, onboarding=$onboardingCompleted)")
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
                    // CRITICAL FIX: Don't create default user_stats here!
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
    
    /**
     * Google Sign-In — receives the idToken from the Credential Manager launcher in the screen.
     *
     * Flow:
     *  1. Verify token with Firebase Auth
     *  2. Extract uid (googleId), email, displayName from Firebase result
     *  3. Check Room for existing row with matching googleId → log in directly
     *  4. Else check Room for matching email → link googleId to existing account
     *  5. Else create a brand-new Room user (role=USER, password=googleId as placeholder)
     *  6. Set navigateToDashboard or navigateToOnboarding based on onboarding completion
     */
    fun googleSignIn(idToken: String) {
        _authState.value = _authState.value.copy(isLoading = true, googleSignInError = null)
        viewModelScope.launch {
            try {
                // ── Step 1: Verify with Firebase ──────────────────────────────
                val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
                val firebaseResult = com.google.firebase.auth.FirebaseAuth.getInstance()
                    .signInWithCredential(credential).await()

                val firebaseUser = firebaseResult.user
                    ?: throw Exception("Google Sign-In failed: no Firebase user returned")

                val googleId  = firebaseUser.uid
                val email     = firebaseUser.email ?: throw Exception("Google account has no email")
                val firstName = firebaseUser.displayName?.substringBefore(" ") ?: ""
                val lastName  = firebaseUser.displayName?.substringAfter(" ", "") ?: ""

                Log.d("AuthViewModel", "Google Sign-In Firebase OK: uid=$googleId, email=$email")

                // ── Step 2: Lookup by googleId ─────────────────────────────────
                val byGoogleId = userRepository.getUserByGoogleId(googleId).getOrNull()
                val user: com.sample.calorease.data.local.entity.UserEntity

                if (byGoogleId != null) {
                    // Already linked — just log in
                    user = byGoogleId
                    Log.d("AuthViewModel", "Google user already linked, logging in userId=${user.userId}")

                } else {
                    // ── Step 3: Lookup by email ────────────────────────────────
                    val byEmail = userRepository.getUserByEmail(email).getOrNull()

                    if (byEmail != null) {
                        // Existing manual account — link google id
                        userRepository.linkGoogleId(byEmail.userId, googleId)
                        user = byEmail
                        Log.d("AuthViewModel", "Linked Google to existing account userId=${user.userId}")
                    } else {
                        // ── Step 4: Create new local user ──────────────────────
                        val newUser = com.sample.calorease.data.local.entity.UserEntity(
                            email        = email,
                            password     = googleId,      // placeholder — user never enters a password
                            nickname     = firstName,
                            role         = "USER",
                            isActive     = true,
                            googleId     = googleId,
                            gender       = "Male",
                            height       = 170,
                            weight       = 70.0,
                            age          = 25,
                            activityLevel= "Moderate",
                            targetWeight = 65.0,
                            goalType     = "MAINTAIN",
                            bmr          = 1500,
                            tdee         = 2000
                        )
                        val newId = userRepository.registerUser(newUser)
                            .getOrThrow()
                        user = userRepository.getUserById(newId.toInt()).getOrThrow()
                            ?: throw Exception("Could not load newly created user")
                        Log.d("AuthViewModel", "Created new Google user userId=${user.userId}")
                    }
                }

                // ── Step 5: Save session ────────────────────────────────────────
                sessionManager.setLoggedIn(user.email)
                sessionManager.saveUserId(user.userId)
                sessionManager.saveRole(user.role)
                sessionManager.saveLastLoginEmail(user.email)

                // ── Step 6: Decide navigation ───────────────────────────────────
                val userStats         = userRepository.getUserStats(user.userId)
                val onboardingDone    = userStats?.onboardingCompleted ?: false

                _authState.value = _authState.value.copy(
                    isLoading       = false,
                    isLoginSuccess  = true,
                    navigateToDashboard  = onboardingDone,
                    navigateToOnboarding = !onboardingDone
                )
                Log.d("AuthViewModel", "Google login done: onboarding=$onboardingDone")

            } catch (e: androidx.credentials.exceptions.NoCredentialException) {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    googleSignInError = "No Google accounts found on this device. Add a Google account in Settings and try again."
                )
            } catch (e: Exception) {
                val msg = when {
                    e.message?.contains("network", ignoreCase = true) == true ->
                        "Google Sign-In unavailable. Check your connection."
                    e.message?.contains("cancel", ignoreCase = true) == true -> null  // user cancelled — silent
                    else -> "Google Sign-In failed. Please try again."
                }
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    googleSignInError = msg
                )
                Log.e("AuthViewModel", "Google Sign-In error", e)
            }
        }
    }

    fun clearGoogleSignInError() {
        _authState.value = _authState.value.copy(googleSignInError = null)
    }
    
    fun resetSuccessFlags() {
        _authState.value = _authState.value.copy(
            isLoginSuccess = false,
            isSignUpSuccess = false,
            navigateToDashboard = false,  // Clear nav flags
            navigateToOnboarding = false
        )
    }
    
    fun resetPassword(email: String) {
        Log.d("AuthViewModel", "Reset password for: $email")
    }
}
