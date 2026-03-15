package com.sample.calorease.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.sample.calorease.data.session.SessionManager
import com.sample.calorease.domain.repository.UserRepository
import com.sample.calorease.util.ValidationUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class UpdateCredentialsState(
    val email: String = "",
    val currentPasswordInput: String = "",
    val newPasswordInput: String = "",
    val confirmPasswordInput: String = "",
    
    val emailError: String? = null,
    val currentPasswordError: String? = null,
    val newPasswordError: String? = null,
    val confirmPasswordError: String? = null,
    
    val isPasswordFieldsUnlocked: Boolean = false,
    val isUpdating: Boolean = false,
    val updateSuccess: Boolean = false,
    val errorMessage: String? = null,
    
    val originalEmail: String = "" // To detect changes
)

@HiltViewModel
class UpdateCredentialsViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _state = MutableStateFlow(UpdateCredentialsState())
    val state: StateFlow<UpdateCredentialsState> = _state.asStateFlow()

    private val auth = FirebaseAuth.getInstance()
    private var localUserId: Int = -1

    init {
        loadCurrentUserEmail()
    }

    private fun loadCurrentUserEmail() {
        viewModelScope.launch {
            val currentEmail = sessionManager.getUserEmail() ?: ""
            localUserId = sessionManager.getUserId() ?: -1
            
            _state.value = _state.value.copy(
                email = currentEmail,
                originalEmail = currentEmail
            )
        }
    }

    fun updateEmail(email: String) {
        val trimmed = email.trim()
        _state.value = _state.value.copy(
            email = trimmed,
            emailError = if (trimmed.isNotBlank()) ValidationUtils.validateEmail(trimmed) else null
        )
    }

    fun updateCurrentPassword(password: String) {
        _state.value = _state.value.copy(
            currentPasswordInput = password,
            currentPasswordError = null
        )
        // If they empty out current password, relock the new password fields
        if (password.isBlank()) {
            _state.value = _state.value.copy(
                isPasswordFieldsUnlocked = false,
                newPasswordInput = "",
                confirmPasswordInput = "",
                newPasswordError = null,
                confirmPasswordError = null
            )
        }
    }

    fun unlockPasswordFields(isUnlocked: Boolean) {
        _state.value = _state.value.copy(isPasswordFieldsUnlocked = isUnlocked)
    }

    fun updateNewPassword(password: String) {
        _state.value = _state.value.copy(
            newPasswordInput = password,
            newPasswordError = ValidationUtils.validatePassword(password),
            // Re-validate confirm if it exists
            confirmPasswordError = if (_state.value.confirmPasswordInput.isNotEmpty()) {
                ValidationUtils.validateConfirmPassword(password, _state.value.confirmPasswordInput)
            } else null
        )
    }

    fun updateConfirmPassword(password: String) {
        _state.value = _state.value.copy(
            confirmPasswordInput = password,
            confirmPasswordError = ValidationUtils.validateConfirmPassword(_state.value.newPasswordInput, password)
        )
    }

    fun resetState() {
        _state.value = _state.value.copy(
            updateSuccess = false,
            errorMessage = null,
            isUpdating = false
        )
    }

    fun updateCredentials() {
        val currentState = _state.value
        val emailChanged = currentState.email != currentState.originalEmail
        val passwordAttempted = currentState.isPasswordFieldsUnlocked && currentState.newPasswordInput.isNotBlank()

        if (!emailChanged && !passwordAttempted) {
            _state.value = currentState.copy(errorMessage = "No changes requested")
            return
        }
        
        // Strict Validation Checks
        if (emailChanged && currentState.emailError != null) return
        if (passwordAttempted) {
            if (currentState.newPasswordError != null || currentState.confirmPasswordError != null) return
            if (currentState.newPasswordInput == currentState.currentPasswordInput) {
                _state.value = currentState.copy(newPasswordError = "New password cannot be same as current")
                return
            }
        }

        val currentUser = auth.currentUser
        if (currentUser == null) {
            _state.value = currentState.copy(errorMessage = "Not logged into Firebase")
            return
        }

        // Must always have current password to update secure fields in Firebase
        if (currentState.currentPasswordInput.isBlank()) {
            _state.value = currentState.copy(currentPasswordError = "Current password is required to save changes")
            return
        }

        _state.value = currentState.copy(isUpdating = true, errorMessage = null)

        viewModelScope.launch {
            try {
                // Step 1: Re-authenticate
                val credential = EmailAuthProvider.getCredential(currentUser.email!!, currentState.currentPasswordInput)
                currentUser.reauthenticate(credential).await()
                Log.d("UpdateCredentialsVM", "Firebase Re-auth successful")

                // Step 2: Update Email
                if (emailChanged) {
                    currentUser.verifyBeforeUpdateEmail(currentState.email).await()
                    // Update Room locally
                    userRepository.updateUserEmail(localUserId, currentState.email)
                    sessionManager.setLoggedIn(currentState.email)
                    sessionManager.saveLastLoginEmail(currentState.email)
                    Log.d("UpdateCredentialsVM", "Email updated locally and verification sent")
                }

                // Step 3: Update Password
                if (passwordAttempted) {
                    currentUser.updatePassword(currentState.newPasswordInput).await()
                    // Update Room locally
                    userRepository.updateUserPassword(localUserId, currentState.newPasswordInput)
                    Log.d("UpdateCredentialsVM", "Password updated successfully")
                }

                _state.value = _state.value.copy(
                    isUpdating = false,
                    updateSuccess = true,
                    originalEmail = currentState.email,
                    currentPasswordInput = "",
                    newPasswordInput = "",
                    confirmPasswordInput = "",
                    isPasswordFieldsUnlocked = false
                )
            } catch (e: Exception) {
                Log.e("UpdateCredentialsVM", "Update failed", e)
                val msg = e.message ?: "Failed to update credentials"
                _state.value = _state.value.copy(
                    isUpdating = false,
                    errorMessage = msg,
                    currentPasswordError = if (msg.contains("password")) "Incorrect password" else null
                )
            }
        }
    }
}
