package com.sample.calorease.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sample.calorease.data.local.entity.UserEntity
import com.sample.calorease.data.model.UserStats
import com.sample.calorease.data.remote.FirestoreService
import com.sample.calorease.data.remote.dto.UserDto
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
    val showAdminAccessConfirmDialog: Boolean = false, // Phase C: Confirm admin access toggle
    // Current user info
    val currentUserId: Int = 0,         // Phase C: ID of logged-in admin
    val isSuperAdmin: Boolean = false,  // Phase C: Is current user super admin?
    // Edit fields
    val editFirstName: String = "",
    val editLastName: String = "",
    val editNickname: String = "",
    val editAge: String = "",
    val editHeight: String = "",
    val editWeight: String = "",
    val editAdminAccess: Boolean = false // Phase C: Admin access toggle
)

/**
 * Combined data class with UserEntity and UserStats for display
 */
data class UserWithStats(
    val userEntity: UserEntity,
    val userStats: UserStats?
) {
    val displayName: String
        get() {
            if (userStats != null) {
                val name = "${userStats.firstName} ${userStats.lastName}".trim()
                if (name.isNotBlank()) return name
            }
            // Terminal Final Phase 1.4: Fallback to email for accounts without completed onboarding
            return userEntity.email.ifBlank { "Unknown User (ID: ${userEntity.userId})" }
        }
    
    val nickname: String
        get() = userStats?.nickname?.takeIf { it.isNotBlank() } ?: "-"
    
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
    
    /** Terminal Final Phase 1.4: True when user has no onboarding stats (new or incomplete Google OAuth users) */
    val isIncomplete: Boolean
        get() = userStats == null || (userStats.firstName.isBlank() && userStats.lastName.isBlank())
}

@HiltViewModel
class AdminUsersViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val legacyRepository: LegacyCalorieRepository,
    private val sessionManager: com.sample.calorease.data.session.SessionManager, // Phase C: For current user
    private val firestoreService: FirestoreService // Sprint 4 Phase 7.4.1: Direct remote fetch
) : ViewModel() {
    
    private val _state = MutableStateFlow(AdminUsersState())
    val state: StateFlow<AdminUsersState> = _state.asStateFlow()
    
    init {
        loadCurrentUser() // Phase C: Load logged-in admin info
        // Sprint 4 Phase 7.7: Start realtime Firestore observation so the table auto-refreshes
        startObservingUsers()
    }

    /** Sprint 4 Phase 7.7: Collect the Firestore snapshot Flow to keep the table live **/
    private fun startObservingUsers() {
        viewModelScope.launch {
            try {
                firestoreService.observeUsers().collect {
                    // Each emission means Firestore changed — reload full deduplication pass
                    loadAllUsers()
                }
            } catch (e: Exception) {
                android.util.Log.e("AdminUsersViewModel", "Realtime observation error", e)
                // Fallback to one-shot load if the stream dies
                loadAllUsers()
            }
        }
    }
    
    /**
     * Phase C: Load current logged-in admin's info
     */
    private fun loadCurrentUser() {
        viewModelScope.launch {
            try {
                val userId = sessionManager.getUserId() ?: run {
                    android.util.Log.e("AdminUsersViewModel", "No user ID in session")
                    return@launch
                }
                
                val currentUser = userRepository.getUserById(userId).getOrNull()
                _state.value = _state.value.copy(
                    currentUserId = currentUser?.userId ?: 0,
                    isSuperAdmin = currentUser?.isSuperAdmin ?: false
                )
                android.util.Log.d("AdminUsersViewModel", "Current user ID: ${currentUser?.userId}, isSuperAdmin: ${currentUser?.isSuperAdmin}")
            } catch (e: Exception) {
                android.util.Log.e("AdminUsersViewModel", "Failed to load current user", e)
            }
        }
    }
    
    fun loadAllUsers() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            
            try {
                val remoteUsers = firestoreService.getAllUsers()
                val remoteStats = firestoreService.getAllUserStats()
                
                // Sprint 4 Phase 7.9: True Database Cleanup & Descending Sort
                // Group by case-insensitive email to find duplicates caused by aborted signups
                val groupedUsers = remoteUsers.groupBy { it.email.lowercase() }
                val cleanedUsers = mutableListOf<UserDto>()
                
                for ((emailGroup, docs) in groupedUsers) {
                    if (emailGroup.isBlank()) continue
                    
                    // Pick the legitimate document: prioritize non-zero userId and newest timestamp
                    val winner = docs.maxByOrNull { if (it.userId != 0) it.lastUpdated + Long.MAX_VALUE / 2 else it.lastUpdated } ?: docs.first()
                    cleanedUsers.add(winner)
                    
                    // TRUE DATABASE CLEANUP: Delete the ghost/duplicate documents from Firestore permanently
                    val losers = docs.filter { it != winner }
                    for (loser in losers) {
                        try {
                            // Call the physical delete using the exact case-sensitive email doc ID of the loser
                            firestoreService.deleteUser(loser.email)
                            firestoreService.deleteUserStats(loser.email)
                            android.util.Log.w("AdminUsersVM", "Physically deleted duplicate ghost doc from remote: ${loser.email}")
                        } catch (ignored: Exception) {}
                    }
                }
                
                // Sort descending based on ID per request (5, 4, 3, 2, 1)
                val sortedDeduplicatedUsers = cleanedUsers.sortedByDescending { it.userId }
                
                val usersWithStats = sortedDeduplicatedUsers.map { dto ->
                    val userStatDto = remoteStats.find { it.userId == dto.userId }
                    
                    val userEntity = UserEntity(
                        userId = dto.userId,
                        email = dto.email,
                        password = "", 
                        googleId = dto.googleId,
                        isEmailVerified = dto.isEmailVerified,
                        nickname = dto.nickname,
                        role = dto.role,
                        isActive = dto.isActive,
                        accountStatus = dto.accountStatus,
                        adminAccess = dto.adminAccess,
                        isSuperAdmin = dto.isSuperAdmin,
                        accountCreated = dto.accountCreated,
                        gender = dto.gender,
                        height = dto.height,
                        weight = dto.weight,
                        age = dto.age,
                        activityLevel = dto.activityLevel,
                        targetWeight = dto.targetWeight,
                        goalType = dto.goalType,
                        bmr = dto.bmr,
                        tdee = dto.tdee,
                        lastUpdated = dto.lastUpdated
                    )
                    
                    val userStats = userStatDto?.let { statDto ->
                        UserStats(
                            userId = statDto.userId,
                            firstName = statDto.firstName,
                            lastName = statDto.lastName,
                            nickname = statDto.nickname,
                            gender = try { com.sample.calorease.domain.model.Gender.valueOf(statDto.gender) } catch (e: Exception) { com.sample.calorease.domain.model.Gender.MALE },
                            heightCm = statDto.heightCm,
                            weightKg = statDto.weightKg,
                            age = statDto.age,
                            birthday = statDto.birthday,
                            activityLevel = try { com.sample.calorease.domain.model.ActivityLevel.valueOf(statDto.activityLevel) } catch (e: Exception) { com.sample.calorease.domain.model.ActivityLevel.SEDENTARY },
                            weightGoal = try { com.sample.calorease.domain.model.WeightGoal.valueOf(statDto.weightGoal) } catch (e: Exception) { com.sample.calorease.domain.model.WeightGoal.MAINTAIN },
                            targetWeightKg = statDto.targetWeightKg,
                            goalCalories = statDto.goalCalories,
                            bmiValue = statDto.bmiValue,
                            bmiStatus = statDto.bmiStatus,
                            idealWeight = statDto.idealWeight,
                            bmr = statDto.bmr,
                            tdee = statDto.tdee,
                            onboardingCompleted = statDto.onboardingCompleted,
                            currentOnboardingStep = statDto.currentOnboardingStep
                        )
                    }
                    
                    UserWithStats(userEntity, userStats)
                }
                
                _state.value = _state.value.copy(
                    allUsers = usersWithStats,
                    filteredUsers = usersWithStats,
                    isLoading = false
                )
                android.util.Log.d("AdminUsersViewModel", "Loaded ${usersWithStats.size} deduplicated users from Firestore.")
            } catch (e: Exception) {
                android.util.Log.e("AdminUsersViewModel", "Failed to load global users", e)
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
            editAdminAccess = user.userEntity.adminAccess // Phase C: Load admin access status
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
                
                // Sprint 4 Phase 7.4.1: Global update logic
                // Write locally IF it's the current user, but MASSIVELY write strictly to Firestore regardless so the data guarantees
                if (selectedUser.userEntity.userId == _state.value.currentUserId) {
                    legacyRepository.updateUserStats(updatedStats)
                }
                
                val statDto = com.sample.calorease.data.remote.dto.UserStatsDto(
                    userId = updatedStats.userId,
                    firstName = updatedStats.firstName,
                    lastName = updatedStats.lastName,
                    nickname = updatedStats.nickname,
                    gender = updatedStats.gender.name,
                    heightCm = updatedStats.heightCm,
                    weightKg = updatedStats.weightKg,
                    age = updatedStats.age,
                    birthday = updatedStats.birthday,
                    activityLevel = updatedStats.activityLevel.name,
                    weightGoal = updatedStats.weightGoal.name,
                    targetWeightKg = updatedStats.targetWeightKg,
                    goalCalories = updatedStats.goalCalories,
                    bmiValue = updatedStats.bmiValue,
                    bmiStatus = updatedStats.bmiStatus,
                    idealWeight = updatedStats.idealWeight,
                    bmr = updatedStats.bmr,
                    tdee = updatedStats.tdee,
                    onboardingCompleted = updatedStats.onboardingCompleted,
                    currentOnboardingStep = updatedStats.currentOnboardingStep
                )
                firestoreService.saveUserStats(selectedUser.userEntity.email, statDto)
                
                android.util.Log.d("AdminUsersViewModel", "Updated global user stats ${selectedUser.userEntity.userId}")
                
                // Reload users and close dialog
                hideEditDialog()
                loadAllUsers()
            } catch (e: Exception) {
                android.util.Log.e("AdminUsersViewModel", "Failed to update user", e)
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
                val newStatus = if (selectedUser.accountStatus == "active") "deactivated" else "active"
                val updatedUser = selectedUser.userEntity.copy(accountStatus = newStatus, isActive = newStatus == "active", lastUpdated = System.currentTimeMillis())
                
                // Sprint 4 Phase 7.7: ALWAYS update local Room as well, not just when editing self.
                // This ensures the SyncManager sees the correct status on next background pass.
                val localUser = userRepository.getUserByEmail(updatedUser.email).getOrNull()
                if (localUser != null) {
                    userRepository.updateUser(localUser.copy(accountStatus = newStatus, isActive = newStatus == "active", lastUpdated = updatedUser.lastUpdated))
                }
                
                // Write to Firestore
                val remoteUser = firestoreService.getUser(updatedUser.email)
                if (remoteUser != null) {
                    val finalDto = remoteUser.copy(accountStatus = newStatus, isActive = newStatus == "active", lastUpdated = updatedUser.lastUpdated)
                    firestoreService.saveUser(finalDto)
                }
                
                android.util.Log.d("AdminUsersViewModel", "Toggled user ${selectedUser.userEntity.userId} status to $newStatus")
                hideStatusConfirmDialog()
                loadAllUsers()
            } catch (e: Exception) {
                android.util.Log.e("AdminUsersViewModel", "Failed to toggle user status", e)
            }
        }
    }
    
    fun refreshUsers() {
        _state.value = _state.value.copy(searchQuery = "")
        loadAllUsers()
    }
    
    // Phase C: Admin Access Management
    
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
                // Validation 1: Only super admin can toggle admin access
                if (!isSuperAdmin) {
                    android.util.Log.w("AdminUsersViewModel", " Only super admin can toggle admin access")
                    hideAdminAccessConfirmDialog()
                    return@launch
                }
                
                // Validation 2: Cannot remove own admin access
                if (selectedUser.userEntity.userId == currentUserId && !newAdminAccess) {
                    android.util.Log.w("AdminUsersViewModel", " Cannot remove your own admin access")
                    hideAdminAccessConfirmDialog()
                    return@launch
                }
                
                // Validation 3: Cannot modify super admin's admin access
                if (selectedUser.userEntity.isSuperAdmin) {
                    android.util.Log.w("AdminUsersViewModel", " Cannot modify super admin's access")
                    hideAdminAccessConfirmDialog()
                    return@launch
                }
                
                // Apply admin access toggle
                val updatedUser = selectedUser.userEntity.copy(adminAccess = newAdminAccess, lastUpdated = System.currentTimeMillis())
                
                if (updatedUser.userId == currentUserId) {
                    userRepository.updateUser(updatedUser)
                }
                
                val remoteUser = firestoreService.getUser(updatedUser.email)
                if (remoteUser != null) {
                    val finalDto = remoteUser.copy(adminAccess = newAdminAccess, lastUpdated = updatedUser.lastUpdated)
                    firestoreService.saveUser(finalDto)
                }
                
                android.util.Log.d("AdminUsersViewModel", "Toggled global admin access for user ${selectedUser.userEntity.userId} to $newAdminAccess")
                
                // Close dialog and reload
                hideAdminAccessConfirmDialog()
                loadAllUsers()
            } catch (e: Exception) {
                android.util.Log.e("AdminUsersViewModel", "Failed to toggle admin access", e)
                hideAdminAccessConfirmDialog()
            }
        }
    }
}
