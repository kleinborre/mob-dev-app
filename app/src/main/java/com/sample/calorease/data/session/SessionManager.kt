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
    }

    /**
     * Set user as logged in with their email
     */
    suspend fun setLoggedIn(email: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_LOGGED_IN] = true
            preferences[PreferencesKeys.USER_EMAIL] = email
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
}
