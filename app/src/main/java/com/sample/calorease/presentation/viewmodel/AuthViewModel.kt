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
    private val emailValidationRepository: com.sample.calorease.data.repository.EmailValidationRepository,
    // Sprint 4 Phase 7.7: Injected for cross-device deactivation enforcement
    private val firestoreService: com.sample.calorease.data.remote.FirestoreService
) : ViewModel() {
    
    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()

    // Terminal Final: Known admin emails whose privileges are ALWAYS enforced on login.
    // If Firestore or Room ever loses adminAccess for these accounts, login repairs them automatically.
    private val KNOWN_ADMINS = setOf("blitzalexandra19@gmail.com")

    /**
     * After sync, enforce admin privileges locally and remotely for any known admin email.
     * This prevents cloud sync or reinstall from stripping the admin role.
     */
    private suspend fun enforceAdminPrivileges(email: String) {
        if (email.lowercase() !in KNOWN_ADMINS) return
        try {
            Log.d("AuthViewModel", "enforceAdminPrivileges: restoring admin for $email")

            // 1. Ensure local Room record has correct admin flags
            val localUser = userRepository.getUserByEmail(email).getOrNull() ?: return
            if (!localUser.adminAccess || !localUser.isSuperAdmin || localUser.role != "ADMIN") {
                val repaired = localUser.copy(
                    role = "ADMIN",
                    adminAccess = true,
                    isSuperAdmin = true,
                    lastUpdated = System.currentTimeMillis()
                )
                userRepository.updateUser(repaired)
                Log.d("AuthViewModel", "Admin Room record repaired for $email")
            }

            // 2. Ensure Firestore document has correct admin flags
            try {
                val remoteDoc = firestoreService.getUser(email)
                if (remoteDoc != null && (!remoteDoc.adminAccess || !remoteDoc.isSuperAdmin || remoteDoc.role != "ADMIN")) {
                    val repairedDto = remoteDoc.copy(
                        role = "ADMIN",
                        adminAccess = true,
                        isSuperAdmin = true,
                        lastUpdated = System.currentTimeMillis()
                    )
                    firestoreService.saveUser(repairedDto)
                    Log.d("AuthViewModel", "Admin Firestore record repaired for $email")
                } else if (remoteDoc == null) {
                    // No cloud doc at all — create it with admin flags
                    val freshAdminDto = com.sample.calorease.data.remote.dto.UserDto(
                        userId = userRepository.getUserByEmail(email).getOrNull()?.userId ?: 0,
                        email = email,
                        role = "ADMIN",
                        adminAccess = true,
                        isSuperAdmin = true,
                        isActive = true,
                        accountStatus = "active",
                        isEmailVerified = true,
                        lastUpdated = System.currentTimeMillis()
                    )
                    firestoreService.saveUser(freshAdminDto)
                    Log.d("AuthViewModel", "Admin Firestore doc created from scratch for $email")
                }
            } catch (fsEx: Exception) {
                Log.w("AuthViewModel", "Firestore admin repair offline — will retry on next sync", fsEx)
            }
        } catch (e: Exception) {
            Log.e("AuthViewModel", "enforceAdminPrivileges error", e)
        }
    }
    
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
                
                // Sprint 4 Phase 7.3: Silent Firebase Authentication Intercept for Offline Test Accounts
                val isOfflineTestUser = trimmedEmail == "palenciafrancisadrian@gmail.com" || trimmedEmail == "blitzalexandra19@gmail.com"
                
                val result = try {
                    auth.signInWithEmailAndPassword(trimmedEmail, trimmedPassword).await()
                } catch (e: Exception) {
                    // If the account does not exist in Firebase yet (but exists offline as our default seed)
                    if (isOfflineTestUser && (e is com.google.firebase.auth.FirebaseAuthInvalidUserException || e is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException)) {
                        android.util.Log.d("AuthViewModel", "Silent Auth Intercept: Registering offline Test User natively into Firebase.")
                        auth.createUserWithEmailAndPassword(trimmedEmail, trimmedPassword).await()
                    } else {
                        throw e
                    }
                }
                
                val firebaseUser = result.user ?: throw Exception("Invalid credentials")

                // 2. Are they verified? (Bypass validation strictly for the internal Demo/Admin accounts)
                if (!firebaseUser.isEmailVerified && !isOfflineTestUser) {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        emailError = "Please verify your email address to continue.",
                        showResendVerification = true
                    )
                    return@launch
                }

                // 3. Sprint 4 Phase 7.7: Check Firestore (authoritative source) for deactivation FIRST.
                // This ensures admin deactivations are enforced cross-device even before Room syncs.
                val remoteUserDoc = firestoreService.getUser(trimmedEmail)
                if (remoteUserDoc != null && remoteUserDoc.accountStatus == "deactivated") {
                    Log.w("AuthViewModel", "BLOCKED: Account $trimmedEmail is deactivated in Firestore.")
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        emailError = "Your account has been deactivated by an administrator. Please contact support."
                    )
                    return@launch
                }

                // 4. Do they exist locally yet? (Reinstall detection)
                val existingLocal = userRepository.getUserByEmail(trimmedEmail).getOrNull()
                val user: UserEntity

                if (existingLocal == null) {
                    Log.d("AuthViewModel", "Fresh Install Detected during Manual Login. Constructing local shell.")
                    val newUser = UserEntity(
                        email = trimmedEmail,
                        password = trimmedPassword,
                        nickname = "Loading...",
                        role = "USER",
                        isActive = true,
                        isEmailVerified = true,
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
                    if (!existingLocal.isEmailVerified) {
                        userRepository.updateEmailVerified(existingLocal.userId, true)
                    }
                    // Also keep local DB in sync with remote deactivation state
                    if (existingLocal.accountStatus == "deactivated") {
                        _authState.value = _authState.value.copy(
                            isLoading = false,
                            emailError = "Your account has been deactivated by an administrator. Please contact support."
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

                // Terminal Final: Repair admin privileges if this is a known admin account.
                // Runs after sync so we never accidentally get overwritten back to USER role.
                enforceAdminPrivileges(trimmedEmail)

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
                // If the user signed up natively through Google, they don't have an offline password
                val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
                val methods = try { auth.fetchSignInMethodsForEmail(trimmedEmail).await().signInMethods } catch (ignored: Exception) { null }
                
                if (methods?.contains("google.com") == true && !methods.contains("password")) {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        passwordError = "This account uses Google Sign-In. Please use the Google button, or reset your password to enable manual login."
                    )
                } else {
                    // Standard Firebase credential rejection
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        passwordError = "Incorrect email or password, or account doesn't exist"
                    )
                }
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
                // Sprint 4 Phase 7.7: NOTE — do NOT push to Firestore here.
                // Ghost Firestore documents were causing duplicate rows in Admin table when
                // users abandon onboarding. Firestore sync happens on first successful login
                // via syncManager.performSync() after email verification.

                if (firebaseUser != null) {
                    // Send verification email
                    firebaseUser.sendEmailVerification().await()
                    Log.d("AuthViewModel", "Verification email sent to $email")

                    // Terminal Final: Determine a globally unique userId BEFORE inserting into Room.
                    // Room auto-increment restarts from 1 after reinstall, causing collisions with
                    // existing Firestore user IDs. Query Firestore max first, use max+1.
                    val firestoreMaxId = try { firestoreService.getMaxUserId() } catch (e: Exception) { 0 }
                    val localMaxId    = try { userRepository.getAllUsers().getOrNull()?.maxOfOrNull { it.userId } ?: 0 } catch (e: Exception) { 0 }
                    val nextUserId    = maxOf(firestoreMaxId, localMaxId) + 1
                    Log.d("AuthViewModel", "Assigning new userId=$nextUserId (firestoreMax=$firestoreMaxId, localMax=$localMaxId)")

                    // Create offline user in Room — userId placeholder; corrected immediately after
                    val newUser = UserEntity(
                        userId        = nextUserId,   // Force the globally-unique ID
                        email         = email,
                        password      = password.trim(),
                        nickname      = "",
                        role          = "USER",
                        isActive      = true,
                        isEmailVerified = false,
                        gender        = "Male",
                        height        = 170,
                        weight        = 70.0,
                        age           = 25,
                        activityLevel = "Moderate",
                        targetWeight  = 65.0,
                        goalType      = "MAINTAIN",
                        bmr           = 1500,
                        tdee          = 2000,
                        accountCreated = System.currentTimeMillis()
                    )
                    userRepository.registerUser(newUser).getOrThrow()
                    Log.d("AuthViewModel", "New user created with userId=$nextUserId in Room")

                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        isSignUpSuccess = true
                    )
                    Log.d("AuthViewModel", "Sign up successful, verification required")
                    syncScheduler.schedulePeriodicSync()
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

                // Sprint 4 Phase 7.7: Check REMOTE Firestore for deactivation before ANY local access.
                val remoteGoogleDoc = firestoreService.getUser(email)
                if (remoteGoogleDoc != null && remoteGoogleDoc.accountStatus == "deactivated") {
                    Log.w("AuthViewModel", "BLOCKED Google OAuth: $email is deactivated in Firestore.")
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        googleSignInError = "Your account has been deactivated by an administrator. Please contact support."
                    )
                    return@launch
                }

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
                        
                        // Sprint 4 Phase 7.4: Prevent Firebase from nuking the unverified manual password credential!
                        // If they have a valid local password established, use updatePassword to securely inject the provider back into the session
                        if (user.password.isNotBlank() && user.password != googleId) {
                            try {
                                firebaseUser.updatePassword(user.password).await()
                                Log.d("AuthViewModel", "Successfully restored underlying Password provider saving Manual Login capabilities.")
                            } catch (e: Exception) {
                                Log.w("AuthViewModel", "Silent password restoration skipped", e)
                            }
                        }
                    } else {
                        // ── Step 4: Create new local user (Google OAuth, first time) ──────────
                        // Terminal Final: Query Firestore for max userId to avoid collision on reinstall
                        val fsMax    = try { firestoreService.getMaxUserId() } catch (e: Exception) { 0 }
                        val localMax = try { userRepository.getAllUsers().getOrNull()?.maxOfOrNull { it.userId } ?: 0 } catch (e: Exception) { 0 }
                        val nextId   = maxOf(fsMax, localMax) + 1
                        Log.d("AuthViewModel", "Google new user: assigning userId=$nextId (fsMax=$fsMax, localMax=$localMax)")

                        val newUser = com.sample.calorease.data.local.entity.UserEntity(
                            userId       = nextId,
                            email        = email,
                            password     = googleId,      // placeholder
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
                            tdee         = 2000,
                            accountCreated = System.currentTimeMillis()
                        )
                        userRepository.registerUser(newUser).getOrThrow()
                        user = userRepository.getUserById(nextId).getOrThrow()
                            ?: throw Exception("Could not load newly created Google user (userId=$nextId)")
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

                // Terminal Final: Repair admin privileges for known admin accounts.
                enforceAdminPrivileges(email)

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
