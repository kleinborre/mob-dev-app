package com.sample.calorease.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sample.calorease.data.local.entity.UserEntity
import com.sample.calorease.data.model.UserStats
import com.sample.calorease.domain.repository.LegacyCalorieRepository
import com.sample.calorease.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.text.SimpleDateFormat
import java.util.*

/**
 * ViewModel for Admin Users Page
 * Handles CRUD operations on users
 */
data class AdminUsersState(
    val allUsers: List<UserWithStats> = emptyList(),
    val filteredUsers: List<UserWithStats> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val selectedUser: UserWithStats? = null,
    val showEditDialog: Boolean = false,
    val showStatusConfirmDialog: Boolean = false,
    val showAdminAccessConfirmDialog: Boolean = false, // ✅ Phase C: Confirm admin access toggle
    // Current user info
    val currentUserId: Int = 0,         // ✅ Phase C: ID of logged-in admin
    val isSuperAdmin: Boolean = false,  // ✅ Phase C: Is current user super admin?
    // Edit fields
    val editFirstName: String = "",
    val editLastName: String = "",
    val editNickname: String = "",
    val editAge: String = "",
    val editHeight: String = "",
    val editWeight: String = "",
    val editAdminAccess: Boolean = false // ✅ Phase C: Admin access toggle
)

/**
 * Combined data class with UserEntity and UserStats for display
 */
data class UserWithStats(
    val userEntity: UserEntity,
    val userStats: UserStats?
) {
    val displayName: String
        get() = userStats?.let { "${it.firstName} ${it.lastName}" } ?: userEntity.email
    
    val nickname: String
        get() = userStats?.nickname ?: "-"
    
    val age: Int
        get() = userStats?.age ?: 0
    
    val height: Double
        get() = userStats?.heightCm ?: 0.0
    
    val weight: Double
        get() = userStats?.weightKg ?: 0.0
    
    val accountStatus: String
        get() = userEntity.accountStatus
    
    val accountCreatedDate: String
        get() {
            val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            return sdf.format(Date(userEntity.accountCreated))
        }
}

@HiltViewModel
class AdminUsersViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val legacyRepository: LegacyCalorieRepository,
    private val sessionManager: com.sample.calorease.data.session.SessionManager // ✅ Phase C: For current user
) : ViewModel() {
    
    private val _state = MutableStateFlow(AdminUsersState())
    val state: StateFlow<AdminUsersState> = _state.asStateFlow()
    
    init {
        loadCurrentUser() // ✅ Phase C: Load logged-in admin info
        loadAllUsers()
    }
    
    /**
     * ✅ Phase C: Load current logged-in admin's info
     */
    private fun loadCurrentUser() {
        viewModelScope.launch {
            try {
                val userId = sessionManager.getUserId() ?: run {
                    android.util.Log.e("AdminUsersViewModel", "❌ No user ID in session")
                    return@launch
                }
                
                val currentUser = userRepository.getUserById(userId).getOrNull()
                _state.value = _state.value.copy(
                    currentUserId = currentUser?.userId ?: 0,
                    isSuperAdmin = currentUser?.isSuperAdmin ?: false
                )
                android.util.Log.d("AdminUsersViewModel", "✅ Current user ID: ${currentUser?.userId}, isSuperAdmin: ${currentUser?.isSuperAdmin}")
            } catch (e: Exception) {
                android.util.Log.e("AdminUsersViewModel", "❌ Failed to load current user", e)
            }
        }
    }
    
    fun loadAllUsers() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            
            try {
                // Get all users from repository
                val allUserEntities = userRepository.getAllUsers().getOrNull() ?: emptyList()
                
                // Load UserStats for each user
                val usersWithStats = allUserEntities.map { userEntity ->
                    val userStats = legacyRepository.getUserStats(userEntity.userId)
                    UserWithStats(userEntity, userStats)
                }
                
                _state.value = _state.value.copy(
                    allUsers = usersWithStats,
                    filteredUsers = usersWithStats,
                    isLoading = false
                )
                
                android.util.Log.d("AdminUsersViewModel", "✅ Loaded ${usersWithStats.size} users")
            } catch (e: Exception) {
                android.util.Log.e("AdminUsersViewModel", "❌ Failed to load users", e)
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }
    
    fun updateSearchQuery(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
        filterUsers(query)
    }
    
    private fun filterUsers(query: String) {
        val filtered = if (query.isBlank()) {
            _state.value.allUsers
        } else {
            _state.value.allUsers.filter { user ->
                user.displayName.contains(query, ignoreCase = true) ||
                user.nickname.contains(query, ignoreCase = true) ||
                user.userEntity.email.contains(query, ignoreCase = true)
            }
        }
        
        _state.value = _state.value.copy(filteredUsers = filtered)
    }
    
    fun showEditUserDialog(user: UserWithStats) {
        _state.value = _state.value.copy(
            selectedUser = user,
            showEditDialog = true,
            editFirstName = user.userStats?.firstName ?: "",
            editLastName = user.userStats?.lastName ?: "",
            editNickname = user.userStats?.nickname ?: "",
            editAge = user.age.toString(),
            editHeight = user.height.toString(),
            editWeight = user.weight.toString(),
            editAdminAccess = user.userEntity.adminAccess // ✅ Phase C: Load admin access status
        )
    }
    
    fun hideEditDialog() {
        _state.value = _state.value.copy(
            showEditDialog = false,
            selectedUser = null
        )
    }
    
    fun updateEditField(field: String, value: String) {
        _state.value = when (field) {
            "firstName" -> _state.value.copy(editFirstName = value)
            "lastName" -> _state.value.copy(editLastName = value)
            "nickname" -> _state.value.copy(editNickname = value)
            "age" -> _state.value.copy(editAge = value)
            "height" -> _state.value.copy(editHeight = value)
            "weight" -> _state.value.copy(editWeight = value)
            else -> _state.value
        }
    }
    
    fun saveUserEdits() {
        viewModelScope.launch {
            val selectedUser = _state.value.selectedUser ?: return@launch
            val userStats = selectedUser.userStats ?: return@launch
            
            try {
                // Update UserStats with new values
                val updatedStats = userStats.copy(
                    firstName = _state.value.editFirstName,
                    lastName = _state.value.editLastName,
                    nickname = _state.value.editNickname.ifBlank { null },
                    age = _state.value.editAge.toIntOrNull() ?: userStats.age,
                    heightCm = _state.value.editHeight.toDoubleOrNull() ?: userStats.heightCm,
                    weightKg = _state.value.editWeight.toDoubleOrNull() ?: userStats.weightKg
                )
                
                legacyRepository.updateUserStats(updatedStats)
                
                android.util.Log.d("AdminUsersViewModel", "✅ Updated user ${selectedUser.userEntity.userId}")
                
                // Reload users and close dialog
                hideEditDialog()
                loadAllUsers()
            } catch (e: Exception) {
                android.util.Log.e("AdminUsersViewModel", "❌ Failed to update user", e)
            }
        }
    }
    
    fun showStatusConfirmDialog(user: UserWithStats) {
        _state.value = _state.value.copy(
            selectedUser = user,
            showStatusConfirmDialog = true
        )
    }
    
    fun hideStatusConfirmDialog() {
        _state.value = _state.value.copy(
            showStatusConfirmDialog = false,
            selectedUser = null
        )
    }
    
    fun toggleUserStatus() {
        viewModelScope.launch {
            val selectedUser = _state.value.selectedUser ?: return@launch
            
            try {
                // Toggle status: active ↔ deactivated
                val newStatus = if (selectedUser.accountStatus == "active") "deactivated" else "active"
                
                val updatedUser = selectedUser.userEntity.copy(accountStatus = newStatus)
                userRepository.updateUser(updatedUser)
                
                android.util.Log.d("AdminUsersViewModel", "✅ Toggled user ${selectedUser.userEntity.userId} status to $newStatus")
                
                // Reload users and close dialog
                hideStatusConfirmDialog()
                loadAllUsers()
            } catch (e: Exception) {
                android.util.Log.e("AdminUsersViewModel", "❌ Failed to toggle user status", e)
            }
        }
    }
    
    fun refreshUsers() {
        _state.value = _state.value.copy(searchQuery = "")
        loadAllUsers()
    }
    
    // ✅ Phase C: Admin Access Management
    
    /**
     * Toggle admin access field in edit dialog
     */
    fun toggleEditAdminAccess(newValue: Boolean) {
        _state.value = _state.value.copy(editAdminAccess = newValue)
    }
    
    /**
     * Show confirmation dialog for admin access toggle
     */
    fun showAdminAccessConfirmDialog() {
        _state.value = _state.value.copy(showAdminAccessConfirmDialog = true)
    }
    
    /**
     * Hide admin access confirmation dialog
     */
    fun hideAdminAccessConfirmDialog() {
        _state.value = _state.value.copy(showAdminAccessConfirmDialog = false)
    }
    
    /**
     * Apply admin access toggle with full validation
     */
    fun applyAdminAccessToggle() {
        viewModelScope.launch {
            val selectedUser = _state.value.selectedUser ?: return@launch
            val currentUserId = _state.value.currentUserId
            val isSuperAdmin = _state.value.isSuperAdmin
            val newAdminAccess = _state.value.editAdminAccess
            
            try {
                // ✅ Validation 1: Only super admin can toggle admin access
                if (!isSuperAdmin) {
                    android.util.Log.w("AdminUsersViewModel", "⚠️ Only super admin can toggle admin access")
                    hideAdminAccessConfirmDialog()
                    return@launch
                }
                
                // ✅ Validation 2: Cannot remove own admin access
                if (selectedUser.userEntity.userId == currentUserId && !newAdminAccess) {
                    android.util.Log.w("AdminUsersViewModel", "⚠️ Cannot remove your own admin access")
                    hideAdminAccessConfirmDialog()
                    return@launch
                }
                
                // ✅ Validation 3: Cannot modify super admin's admin access
                if (selectedUser.userEntity.isSuperAdmin) {
                    android.util.Log.w("AdminUsersViewModel", "⚠️ Cannot modify super admin's access")
                    hideAdminAccessConfirmDialog()
                    return@launch
                }
                
                // Apply admin access toggle
                val updatedUser = selectedUser.userEntity.copy(adminAccess = newAdminAccess)
                userRepository.updateUser(updatedUser)
                
                android.util.Log.d("AdminUsersViewModel", "✅ Toggled admin access for user ${selectedUser.userEntity.userId} to $newAdminAccess")
                
                // Close dialog and reload
                hideAdminAccessConfirmDialog()
                loadAllUsers()
            } catch (e: Exception) {
                android.util.Log.e("AdminUsersViewModel", "❌ Failed to toggle admin access", e)
                hideAdminAccessConfirmDialog()
            }
        }
    }
}
