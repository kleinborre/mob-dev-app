package com.sample.calorease.domain.sync

import android.util.Log
import com.sample.calorease.data.local.entity.DailyEntryEntity
import com.sample.calorease.data.local.entity.UserEntity
import com.sample.calorease.data.remote.FirestoreService
import com.sample.calorease.data.remote.dto.DailyEntryDto
import com.sample.calorease.data.remote.dto.UserDto
import com.sample.calorease.data.session.SessionManager
import com.sample.calorease.domain.repository.CalorieRepository
import com.sample.calorease.domain.repository.UserRepository
import com.sample.calorease.presentation.util.NetworkUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

import com.sample.calorease.domain.repository.LegacyCalorieRepository

/**
 * SyncManager
 * Brokering Two-Way synchronization between local Room DB and Firebase Firestore.
 * Resolves conflicts strictly enforcing Last-Write-Wins based on `lastUpdated` timestamps.
 */
@Singleton
class SyncManager @Inject constructor(
    private val firestoreService: FirestoreService,
    private val userRepository: UserRepository,
    private val calorieRepository: CalorieRepository,
    private val legacyRepository: LegacyCalorieRepository,
    private val sessionManager: SessionManager
) {
    suspend fun performSync() = withContext(Dispatchers.IO) {
        try {
            val email = sessionManager.getUserEmail() ?: return@withContext
            val localUserId = sessionManager.getUserId() ?: return@withContext

            Log.d("SyncManager", "Starting sync for $email ($localUserId)")
            
            // 1) Sync User Profile Data
            syncUser(localUserId, email)

            // 2) Sync Food Entries
            syncDailyEntries(localUserId, email)

            Log.d("SyncManager", "Sync successfully completed.")
        } catch (e: Exception) {
            Log.e("SyncManager", "Sync failed: ${e.message}", e)
        }
    }

    private suspend fun syncUser(localUserId: Int, email: String) {
        val currentLocalUserResult = userRepository.getUserById(localUserId)
        val currentLocalUser = currentLocalUserResult.getOrNull() ?: return
        
        val remoteUser: UserDto? = firestoreService.getUser(email)

        if (remoteUser == null) {
            // Push our local entity to Firebase if nonexistent 
            Log.d("SyncManager", "Remote User is null. Pushing local to Firebase.")
            firestoreService.saveUser(mapToDto(currentLocalUser))
            
            // Push Onboarding/Stats silently concurrently
            val stats = legacyRepository.getUserStats(localUserId)
            if (stats != null) {
                firestoreService.saveUserStats(email, mapToDto(stats))
            }
            return
        }

        // Sprint 4 Phase 6: Fresh Install Detection
        // Prevent newly generated local UserEntity ghosts (which appear "newer" via timestamp) from destroying remote NoSQL data
        val stats = legacyRepository.getUserStats(localUserId)
        val remoteStats = firestoreService.getUserStats(email)
        
        if (stats == null && remoteStats != null) {
            Log.d("SyncManager", "Fresh Install Detected! Restoring remote profile to local Room DB.")
            userRepository.updateUser(mapToEntity(remoteUser, currentLocalUser.password, localUserId))
            legacyRepository.insertUserStats(mapToEntity(remoteStats, localUserId))
            return
        }

        // Conflict Resolution: Last-Write-Wins
        if (currentLocalUser.lastUpdated > remoteUser.lastUpdated) {
            Log.d("SyncManager", "Local User is newer. Pushing to Firebase.")
            firestoreService.saveUser(mapToDto(currentLocalUser))
            
            if (stats != null) {
                firestoreService.saveUserStats(email, mapToDto(stats))
            }
        } else if (remoteUser.lastUpdated > currentLocalUser.lastUpdated) {
            Log.d("SyncManager", "Remote User is newer. Pulling to Room.")
            userRepository.updateUser(mapToEntity(remoteUser, currentLocalUser.password, localUserId)) // password persisted locally only
            
            val remoteStats = firestoreService.getUserStats(email)
            if (remoteStats != null) {
                legacyRepository.updateUserStats(mapToEntity(remoteStats, localUserId))
            }
        } else {
            Log.d("SyncManager", "User profiles natively identical.")
            val stats = legacyRepository.getUserStats(localUserId)
            val remoteStats = firestoreService.getUserStats(email)
            // Edge Case Fallback: if remote stats vanished or never uploaded due to previous bugs
            if (stats != null && remoteStats == null) {
                firestoreService.saveUserStats(email, mapToDto(stats))
            } else if (stats == null && remoteStats != null) {
                legacyRepository.insertUserStats(mapToEntity(remoteStats, localUserId))
            }
        }
    }

    private suspend fun syncDailyEntries(localUserId: Int, email: String) {
        val result = calorieRepository.getAllFoodEntriesSortedByDate(localUserId)
        val localEntries = result.getOrNull() ?: emptyList()

        val remoteEntries = firestoreService.getDailyEntries(email)
        val remoteEntryMap = remoteEntries.associateBy { "${it.entryId}_${it.date}" }
        
        // Check for Local Push overriding Remote
        for (local in localEntries) {
            val uniqueId = "${local.entryId}_${local.date}"
            val remote = remoteEntryMap[uniqueId]

            if (remote == null || local.lastUpdated > remote.lastUpdated) {
                Log.d("SyncManager", "Pushing local DailyEntry $uniqueId to Remote")
                firestoreService.saveDailyEntry(email, mapToDto(local))
            }
        }

        // Check for Remote Pull overriding Local
        val localEntryMap = localEntries.associateBy { "${it.entryId}_${it.date}" }
        for (remote in remoteEntries) {
            val uniqueId = "${remote.entryId}_${remote.date}"
            val local = localEntryMap[uniqueId]

            if (local == null) {
                // Meaning it was added remotely but never fetched locally
                Log.d("SyncManager", "Pulling NEW remote DailyEntry $uniqueId to Local")
                calorieRepository.addDailyEntry(mapToEntity(remote, localUserId))
            } else if (remote.lastUpdated > local.lastUpdated) {
                // Remote update is newer
                Log.d("SyncManager", "Pulling UPDATED remote DailyEntry $uniqueId to Local")
                calorieRepository.updateDailyEntry(mapToEntity(remote, localUserId).copy(entryId = local.entryId))
            }
        }
    }

    // --- Mappers ---
    private fun mapToDto(user: UserEntity): UserDto {
        return UserDto(
            userId = user.userId,
            email = user.email,
            googleId = user.googleId,
            isEmailVerified = user.isEmailVerified,
            nickname = user.nickname,
            role = user.role,
            isActive = user.isActive,
            accountStatus = user.accountStatus,
            adminAccess = user.adminAccess,
            isSuperAdmin = user.isSuperAdmin,
            accountCreated = user.accountCreated,
            gender = user.gender,
            height = user.height,
            weight = user.weight,
            age = user.age,
            activityLevel = user.activityLevel,
            targetWeight = user.targetWeight,
            goalType = user.goalType,
            bmr = user.bmr,
            tdee = user.tdee,
            lastUpdated = user.lastUpdated
        )
    }

    private fun mapToEntity(dto: UserDto, localPasswordFallback: String, fallbackUserId: Int): UserEntity {
        return UserEntity(
            userId = if (dto.userId != 0) dto.userId else fallbackUserId,
            email = dto.email,
            password = localPasswordFallback, // NEVER pulled from remote NoSQL
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
    }

    private fun mapToDto(entry: DailyEntryEntity): DailyEntryDto {
        return DailyEntryDto(
            entryId = entry.entryId,
            userId = entry.userId,
            date = entry.date,
            foodName = entry.foodName,
            calories = entry.calories,
            mealType = entry.mealType,
            lastUpdated = entry.lastUpdated
        )
    }

    private fun mapToEntity(dto: DailyEntryDto, localUserId: Int): DailyEntryEntity {
        return DailyEntryEntity(
            entryId = dto.entryId,
            userId = localUserId,
            date = dto.date,
            foodName = dto.foodName,
            calories = dto.calories,
            mealType = dto.mealType,
            lastUpdated = dto.lastUpdated
        )
    }

    private fun mapToDto(stats: com.sample.calorease.data.model.UserStats): com.sample.calorease.data.remote.dto.UserStatsDto {
        return com.sample.calorease.data.remote.dto.UserStatsDto(
            userId = stats.userId,
            firstName = stats.firstName,
            lastName = stats.lastName,
            nickname = stats.nickname,
            gender = stats.gender.name,
            heightCm = stats.heightCm,
            weightKg = stats.weightKg,
            age = stats.age,
            birthday = stats.birthday,
            activityLevel = stats.activityLevel.name,
            weightGoal = stats.weightGoal.name,
            targetWeightKg = stats.targetWeightKg,
            goalCalories = stats.goalCalories,
            bmiValue = stats.bmiValue,
            bmiStatus = stats.bmiStatus,
            idealWeight = stats.idealWeight,
            bmr = stats.bmr,
            tdee = stats.tdee,
            onboardingCompleted = stats.onboardingCompleted,
            currentOnboardingStep = stats.currentOnboardingStep
        )
    }

    private fun mapToEntity(dto: com.sample.calorease.data.remote.dto.UserStatsDto, localUserId: Int): com.sample.calorease.data.model.UserStats {
        return com.sample.calorease.data.model.UserStats(
            userId = if (dto.userId != 0) dto.userId else localUserId,
            firstName = dto.firstName,
            lastName = dto.lastName,
            nickname = dto.nickname,
            gender = try { com.sample.calorease.domain.model.Gender.valueOf(dto.gender) } catch (e: Exception) { com.sample.calorease.domain.model.Gender.MALE },
            heightCm = dto.heightCm,
            weightKg = dto.weightKg,
            age = dto.age,
            birthday = dto.birthday,
            activityLevel = try { com.sample.calorease.domain.model.ActivityLevel.valueOf(dto.activityLevel) } catch (e: Exception) { com.sample.calorease.domain.model.ActivityLevel.SEDENTARY },
            weightGoal = try { com.sample.calorease.domain.model.WeightGoal.valueOf(dto.weightGoal) } catch (e: Exception) { com.sample.calorease.domain.model.WeightGoal.MAINTAIN },
            targetWeightKg = dto.targetWeightKg,
            goalCalories = dto.goalCalories,
            bmiValue = dto.bmiValue,
            bmiStatus = dto.bmiStatus,
            idealWeight = dto.idealWeight,
            bmr = dto.bmr,
            tdee = dto.tdee,
            onboardingCompleted = dto.onboardingCompleted,
            currentOnboardingStep = dto.currentOnboardingStep
        )
    }
}
