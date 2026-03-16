package com.sample.calorease.presentation.viewmodel

import android.content.Context
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
import com.sample.calorease.presentation.ui.UiEvent
import com.sample.calorease.presentation.util.NetworkUtils
import com.sample.calorease.util.ValidationUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
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
    val googleSignInError: String? = null,
    // Phase 3: Email Verification State
    val showResendVerification: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager,
    private val calculatorUseCase: CalculatorUseCase,
    private val legacyRepository: com.sample.calorease.domain.repository.LegacyCalorieRepository,
    private val syncScheduler: com.sample.calorease.domain.sync.SyncScheduler,
    private val syncManager: com.sample.calorease.domain.sync.SyncManager,
    private val emailValidationRepository: com.sample.calorease.data.repository.EmailValidationRepository
) : ViewModel() {
    
    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()
    
    fun updateEmail(email: String) {
        val trimmedEmail = email.trim()
        Log.d("AuthViewModel", "updateEmail called: input='$email', trimmed='$trimmedEmail'")
        _authState.value = _authState.value.copy(
            email = trimmedEmail,
            emailError = null
        )
        Log.d("AuthViewModel", "State updated: email='${_authState.value.email}'")
    }
    
    // Sprint 4 Phase 4: Debounced API Email Deliverability Check
    private var emailValidationJob: kotlinx.coroutines.Job? = null
    
    fun updateSignUpEmail(email: String) {
        val trimmedEmail = email.trim()
        _authState.value = _authState.value.copy(
            email = trimmedEmail,
            emailError = null
        )
        
        emailValidationJob?.cancel()
        emailValidationJob = viewModelScope.launch {
            kotlinx.coroutines.delay(800L) // Debounce typing
            
            // Only fire expensive remote API if basic offline regex passes first
            if (trimmedEmail.isNotEmpty() && ValidationUtils.validateEmail(trimmedEmail) == null) {
                // Failsafe check — Abstract API live validation
                val result = emailValidationRepository.validateEmailLive(trimmedEmail)
                val errorString = result.getOrNull()
                
                if (errorString != null) {
                    _authState.value = _authState.value.copy(emailError = errorString)
                }
            }
        }
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
        
        // Sprint 4 Phase 3: Offline Block
        if (!NetworkUtils.isNetworkAvailable(context)) {
            _authState.value = _authState.value.copy(isLoading = false)
            viewModelScope.launch { _uiEvent.emit(UiEvent.ShowError("No network connection")) }
            return
        }
        
        viewModelScope.launch {
            try {
                // 1. Authenticate with Firebase first natively! No more local checking blockade.
                val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
                val result = try {
                    auth.signInWithEmailAndPassword(trimmedEmail, trimmedPassword).await()
                } catch (e: Exception) {
                    // Sprint 4 Phase 7.2: Intercept offline test accounts and silently migrate them to Firebase
                    val isTestAccount = (trimmedEmail == "lirioroineil@gmail.com" || trimmedEmail == "christinegaemaruquin@gmail.com") && trimmedPassword == "CaloreaseA3105!"
                    if (isTestAccount) {
                        try {
                            auth.createUserWithEmailAndPassword(trimmedEmail, trimmedPassword).await()
                        } catch (createEx: Exception) {
                            throw e 
                        }
                    } else {
                        throw e
                    }
                }
                
                val firebaseUser = result.user ?: throw Exception("Invalid credentials")

                // 2. Are they verified? (Bypass verification block for test accounts)
                val isTestAccount = (trimmedEmail == "lirioroineil@gmail.com" || trimmedEmail == "christinegaemaruquin@gmail.com")
                if (!firebaseUser.isEmailVerified && !isTestAccount) {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        emailError = "Please verify your email address to continue.",
                        showResendVerification = true
                    )
                    return@launch
                }

                // 3. Since Firebase passed, we guarantee they own the account.
                // Do they exist locally yet? (Reinstall detection)
                val existingLocal = userRepository.getUserByEmail(trimmedEmail).getOrNull()
                val user: UserEntity

                if (existingLocal == null) {
                    Log.d("AuthViewModel", "Fresh Install Detected during Manual Login. Constructing local shell.")
                    // Create phantom shell user to hold session
                    val newUser = UserEntity(
                        email = trimmedEmail,
                        password = trimmedPassword, 
                        nickname = "Loading...", // Will be overwritten by SyncManager
                        role = "USER",
                        isActive = true,
                        isEmailVerified = true, // We literally just checked this in Firebase
                        gender = "Male",
                        height = 170,
                        weight = 70.0,
                        age = 25,
                        activityLevel = "Moderate",
                        targetWeight = 65.0,
                        goalType = "MAINTAIN",
                        bmr = 1500,
                        tdee = 2000
                    )
                    val newId = userRepository.registerUser(newUser).getOrThrow()
                    user = userRepository.getUserById(newId.toInt()).getOrThrow()!!
                } else {
                    // Update email verified flag just in case
                    if (!existingLocal.isEmailVerified) {
                        userRepository.updateEmailVerified(existingLocal.userId, true)
                    }
                    if (existingLocal.accountStatus == "deactivated") {
                        _authState.value = _authState.value.copy(
                            isLoading = false,
                            emailError = "This account is no longer accessible as it was deleted. Please contact support."
                        )
                        return@launch
                    }
                    user = userRepository.getUserByEmail(trimmedEmail).getOrThrow()!!
                }

                // 4. Session Manager
                sessionManager.setLoggedIn(user.email)
                sessionManager.saveUserId(user.userId)
                sessionManager.saveRole(user.role)
                sessionManager.saveLastLoginEmail(user.email)
                
                val existingMode = sessionManager.getLastDashboardMode()
                val modeWasSaved = existingMode != "user" || user.role == "user"
                if (!modeWasSaved) {
                    val initialMode = if (user.role == "admin") "admin" else "user"
                    sessionManager.saveLastDashboardMode(initialMode)
                }

                // 5. SYNCHRONOUS PULL FROM FIREBASE
                // Sprint 4 Phase 6: Sync execution MUST block here to pull data BEFORE making Navigation decisions
                syncManager.performSync()

                // 6. Navigate
                val userStats = userRepository.getUserStats(user.userId)
                val onboardingCompleted = userStats?.onboardingCompleted ?: false

                _authState.value = _authState.value.copy(
                    isLoading = false,
                    isLoginSuccess = true,
                    navigateToDashboard = onboardingCompleted,
                    navigateToOnboarding = !onboardingCompleted,
                    showResendVerification = false
                )
                
                syncScheduler.schedulePeriodicSync()
                syncScheduler.triggerImmediateSync()
                Log.d("AuthViewModel", "Login fully completed via Firebase Auth.")
                
            } catch (e: Exception) {
                // Firebase rejected the credentials or network failed
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    passwordError = "Incorrect email or password, or account doesn't exist"
                )
                Log.e("AuthViewModel", "Login failed", e)
            }
        }
    }
    
    fun signUp() {
        val email = _authState.value.email
        val password = _authState.value.password
        val confirmPassword = _authState.value.confirmPassword
        
        // Validate fields (Inherit async API errors if present, fallback to regex)
        val emailError = _authState.value.emailError ?: ValidationUtils.validateEmail(email)
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
        
        // Sprint 4 Phase 3: Offline Block
        if (!NetworkUtils.isNetworkAvailable(context)) {
            _authState.value = _authState.value.copy(isLoading = false)
            viewModelScope.launch { _uiEvent.emit(UiEvent.ShowError("No network connection")) }
            return
        }
        
        viewModelScope.launch {
            try {
                // Check if email already exists locally
                val alreadyExistsLocal = userRepository.getUserByEmail(email).getOrNull() != null
                if (alreadyExistsLocal) {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        emailError = "This email is already registered locally"
                    )
                    return@launch
                }

                // Create user in Firebase Auth
                val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                val firebaseUser = result.user

                if (firebaseUser != null) {
                    // Send verification email
                    firebaseUser.sendEmailVerification().await()
                    Log.d("AuthViewModel", "Verification email sent to $email")

                    // Create offline user in Room with isEmailVerified = false
                    val newUser = UserEntity(
                        email = email,
                        password = password.trim(),
                        nickname = "",
                        role = "USER",
                        isActive = true,
                        isEmailVerified = false,
                        gender = "Male",
                        height = 170, 
                        weight = 70.0,
                        age = 25,
                        activityLevel = "Moderate",
                        targetWeight = 65.0,
                        goalType = "MAINTAIN",
                        bmr = 1500,
                        tdee = 2000
                    )
                    userRepository.registerUser(newUser).getOrThrow()

                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        isSignUpSuccess = true
                    )
                    Log.d("AuthViewModel", "Sign up successful, verification required")
                    
                    // Sprint 4 Phase 2: Broker Sync to Firestore upon sign-up natively
                    syncScheduler.schedulePeriodicSync()
                    syncScheduler.triggerImmediateSync()
                }
            } catch (e: Exception) {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    emailError = e.message ?: "Firebase sign up failed"
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
        
        // Sprint 4 Phase 3: Offline Block
        if (!NetworkUtils.isNetworkAvailable(context)) {
            _authState.value = _authState.value.copy(isLoading = false, googleSignInError = "No network connection. Online features are restricted.")
            viewModelScope.launch { _uiEvent.emit(com.sample.calorease.presentation.ui.UiEvent.ShowError("No network connection")) }
            return
        }

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

                // ── SYNCHRONOUS PULL FROM FIREBASE ──────────────────────────────
                // Sprint 4 Phase 6: Sync execution MUST block here to pull data BEFORE making Navigation decisions
                syncManager.performSync()

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

                // Sprint 4 Phase 2: Broker Sync to Firestore securely via Google identity
                syncScheduler.schedulePeriodicSync()
                syncScheduler.triggerImmediateSync()

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
    
    fun resendVerificationEmail() {
        val email = _authState.value.email
        val password = _authState.value.password // We need this to auth with Firebase to resend
        
        if (email.isBlank() || password.isBlank()) return
        
        _authState.value = _authState.value.copy(isLoading = true)
        
        viewModelScope.launch {
            try {
                val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
                // Must sign in to resend the verification email to prevent spam/abuse
                val result = auth.signInWithEmailAndPassword(email, password).await()
                result.user?.sendEmailVerification()?.await()
                
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    emailError = "Verification email sent. Please check your inbox.",
                    showResendVerification = false // Hide button after sending
                )
            } catch (e: Exception) {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    emailError = "Failed to resend verification: ${e.message}"
                )
            }
        }
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
        val trimmedEmail = email.trim()
        val emailError = ValidationUtils.validateEmail(trimmedEmail)
        
        if (emailError != null) {
            _authState.value = _authState.value.copy(emailError = emailError)
            return
        }

        Log.d("AuthViewModel", "Reset password for: $trimmedEmail")
        _authState.value = _authState.value.copy(isLoading = true)

        viewModelScope.launch {
            try {
                com.google.firebase.auth.FirebaseAuth.getInstance().sendPasswordResetEmail(trimmedEmail).await()
                // Always show success for security purposes
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    isLoginSuccess = true // We overload this flag to trigger the success dialog
                )
            } catch (e: Exception) {
                // Still show success to prevent email sweeping, but log the real error
                Log.e("AuthViewModel", "Failed to send reset email", e)
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    isLoginSuccess = true
                )
            }
        }
    }
}
