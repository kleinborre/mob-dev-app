package com.sample.calorease.data.session

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "session_prefs")

@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object PreferencesKeys {
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val USER_EMAIL = stringPreferencesKey("user_email")
        val USER_ID = intPreferencesKey("user_id")
        val USER_ROLE = stringPreferencesKey("user_role")
        val HAS_EVER_LOGGED_IN = booleanPreferencesKey("has_ever_logged_in")
        val LAST_DASHBOARD_MODE = stringPreferencesKey("last_dashboard_mode")  // PHASE 3: "admin" or "user"
        val ACCOUNT_DELETION_SUCCESS = booleanPreferencesKey("account_deletion_success")  // PHASE 3
    }

    /**
     * Set user as logged in with their email
     * Also marks that user has logged in at least once (for returning user detection)
     */
    suspend fun setLoggedIn(email: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_LOGGED_IN] = true
            preferences[PreferencesKeys.USER_EMAIL] = email
            preferences[PreferencesKeys.HAS_EVER_LOGGED_IN] = true  // ✅ Mark as returning user
        }
    }

    /**
     * Check if user is logged in
     */
    suspend fun isLoggedIn(): Boolean {
        val preferences = context.dataStore.data.first()
        return preferences[PreferencesKeys.IS_LOGGED_IN] ?: false
    }

    /**
     * Get logged in user email
     */
    suspend fun getUserEmail(): String? {
        val preferences = context.dataStore.data.first()
        return preferences[PreferencesKeys.USER_EMAIL]
    }

    /**
     * Save user ID (Phase 2)
     */
    suspend fun saveUserId(userId: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_ID] = userId
        }
    }

    /**
     * Get user ID (Phase 2)
     */
    suspend fun getUserId(): Int? {
        val preferences = context.dataStore.data.first()
        return preferences[PreferencesKeys.USER_ID]
    }

    /**
     * Save user role (Phase 2)
     */
    suspend fun saveRole(role: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_ROLE] = role
        }
    }

    /**
     * Get user role (Phase 2)
     */
    suspend fun getRole(): String? {
        val preferences = context.dataStore.data.first()
        return preferences[PreferencesKeys.USER_ROLE]
    }

    /**
     * Clear session (logout)
     * Clears all session data including userId and role
     */
    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    /**
     * Flow to observe login state
     */
    fun isLoggedInFlow(): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.IS_LOGGED_IN] ?: false
        }
    }
    
    /**
     * Check if user has ever logged in before (for returning user detection)
     * This flag persists even after logout, unlike IS_LOGGED_IN
     */
    suspend fun hasEverLoggedIn(): Boolean {
        val preferences = context.dataStore.data.first()
        return preferences[PreferencesKeys.HAS_EVER_LOGGED_IN] ?: false
    }
    
    /**
     * Checks if this is a fresh install by looking for a non-backed-up marker file.
     * If the marker is missing (meaning fresh install), we FORCE clear the session.
     * This handles the edge case where 'allowBackup=true' restores the session_prefs
     * but we want the user to log in again.
     */
    suspend fun checkInstallState() {
        // This directory is NEVER backed up by Android Auto Backup
        val noBackupDir = context.noBackupFilesDir
        val installMarker = java.io.File(noBackupDir, "install_marker")
        
        if (!installMarker.exists()) {
            // Marker missing = Fresh Install (or Clear Data)
            // Even if dataStore has a restored session, it is INVALID for this new install instance.
            clearSession()
            
            // Create marker so subsequent runs know this install is valid
            try {
                installMarker.createNewFile()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    /**
     * PHASE 3: Save last dashboard mode (admin or user)
     */
    suspend fun saveLastDashboardMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_DASHBOARD_MODE] = mode
        }
    }
    
    /**
     * PHASE 3: Get last dashboard mode
     * Returns "admin" or "user", defaults to "user"
     */
    suspend fun getLastDashboardMode(): String {
        val preferences = context.dataStore.data.first()
        return preferences[PreferencesKeys.LAST_DASHBOARD_MODE] ?: "user"
    }
    
    /**
     * PHASE 3: Save account deletion success flag
     */
    suspend fun saveAccountDeletionSuccess(value: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ACCOUNT_DELETION_SUCCESS] = value
        }
    }
    
    /**
     * PHASE 3: Check if account was just deleted (for login success message)
     */
    suspend fun wasAccountDeleted(): Boolean {
        val preferences = context.dataStore.data.first()
        return preferences[PreferencesKeys.ACCOUNT_DELETION_SUCCESS] ?: false
    }
    
    /**
     * PHASE 3: Clear account deletion flag (after showing success message)
     */
    suspend fun clearAccountDeletionFlag() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ACCOUNT_DELETION_SUCCESS] = false
        }
    }
}
