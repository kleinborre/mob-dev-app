package com.sample.calorease.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.sample.calorease.data.remote.dto.DailyEntryDto
import com.sample.calorease.data.remote.dto.UserDto
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

interface FirestoreService {
    suspend fun saveUser(user: UserDto)
    suspend fun getUser(userEmail: String): UserDto?
    suspend fun saveUserStats(userEmail: String, stats: com.sample.calorease.data.remote.dto.UserStatsDto)
    suspend fun getUserStats(userEmail: String): com.sample.calorease.data.remote.dto.UserStatsDto?
    suspend fun saveDailyEntry(userEmail: String, entry: DailyEntryDto)
    suspend fun getDailyEntries(userEmail: String): List<DailyEntryDto>

    // Sprint 4 Phase 7.9: Physical Database Deletions
    suspend fun deleteDailyEntry(userEmail: String, uniqueId: String)
    suspend fun deleteUser(userEmail: String)
    suspend fun deleteUserStats(userEmail: String)

    // Sprint 4 Phase 7.4.1: GLOBAL Admin Fetch Protocols
    suspend fun getAllUsers(): List<UserDto>
    suspend fun getAllUserStats(): List<com.sample.calorease.data.remote.dto.UserStatsDto>

    // Sprint 4 Phase 7.7: Realtime Firestore snapshot listener as a Flow
    fun observeUsers(): Flow<List<UserDto>>

    /**
     * Terminal Final: Returns the highest userId currently stored in Firestore.
     * Used during sign-up to assign a globally unique, collision-free userId
     * instead of relying on Room's local auto-increment which restarts from 1 on reinstall.
     * Returns 0 if no users exist yet.
     */
    suspend fun getMaxUserId(): Int
}

@Singleton
class FirestoreServiceImpl @Inject constructor() : FirestoreService {

    private val db = FirebaseFirestore.getInstance()

    // Using userEmail as the unique document identifier in Firestore since Room's incremented auto-ID
    // does not sync logically across multiple devices.
    private val usersCollection = db.collection("users")

    override suspend fun saveUser(user: UserDto) {
        if (user.email.isBlank()) return
        usersCollection.document(user.email).set(user).await()
    }

    override suspend fun getUser(userEmail: String): UserDto? {
        if (userEmail.isBlank()) return null
        val snapshot = usersCollection.document(userEmail).get().await()
        return snapshot.toObject(UserDto::class.java)
    }

    override suspend fun saveUserStats(userEmail: String, stats: com.sample.calorease.data.remote.dto.UserStatsDto) {
        if (userEmail.isBlank()) return
        usersCollection.document(userEmail).collection("user_stats").document("stats").set(stats).await()
    }

    override suspend fun getUserStats(userEmail: String): com.sample.calorease.data.remote.dto.UserStatsDto? {
        if (userEmail.isBlank()) return null
        val snapshot = usersCollection.document(userEmail).collection("user_stats").document("stats").get().await()
        return snapshot.toObject(com.sample.calorease.data.remote.dto.UserStatsDto::class.java)
    }

    override suspend fun saveDailyEntry(userEmail: String, entry: DailyEntryDto) {
        if (userEmail.isBlank()) return
        val entriesCollection = usersCollection.document(userEmail).collection("daily_entries")
        // Sprint 4 Phase 7.3: Fallback Primary Key bridging
        val uniqueId = if (entry.syncId.isNotBlank()) entry.syncId else "${entry.entryId}_${entry.date}"
        entriesCollection.document(uniqueId).set(entry).await()
    }

    override suspend fun getDailyEntries(userEmail: String): List<DailyEntryDto> {
        if (userEmail.isBlank()) return emptyList()
        val snapshot = usersCollection.document(userEmail).collection("daily_entries").get().await()
        return snapshot.documents.mapNotNull { it.toObject(DailyEntryDto::class.java) }
    }

    // Sprint 4 Phase 7.4.1: Global Admin Fetch
    override suspend fun getAllUsers(): List<UserDto> {
        val snapshot = usersCollection.get().await()
        return snapshot.documents.mapNotNull { it.toObject(UserDto::class.java) }
    }

    override suspend fun getAllUserStats(): List<com.sample.calorease.data.remote.dto.UserStatsDto> {
        val snapshot = db.collectionGroup("user_stats").get().await()
        return snapshot.documents.mapNotNull { it.toObject(com.sample.calorease.data.remote.dto.UserStatsDto::class.java) }
    }

    // Sprint 4 Phase 7.7: Returns a Flow that emits the full users list on every Firestore change.
    // AdminUsersViewModel and AdminStatsViewModel collect this for realtime updates without logout.
    override fun observeUsers(): Flow<List<UserDto>> = callbackFlow {
        val registration: ListenerRegistration = usersCollection
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val users = snapshot?.documents?.mapNotNull { it.toObject(UserDto::class.java) }
                    ?: emptyList()
                trySend(users)
            }
        awaitClose { registration.remove() }
    }
    
    override suspend fun deleteDailyEntry(userEmail: String, uniqueId: String) {
        if (userEmail.isBlank() || uniqueId.isBlank()) return
        usersCollection.document(userEmail).collection("daily_entries").document(uniqueId).delete().await()
    }

    override suspend fun deleteUser(userEmail: String) {
        if (userEmail.isBlank()) return
        usersCollection.document(userEmail).delete().await()
    }

    override suspend fun deleteUserStats(userEmail: String) {
        if (userEmail.isBlank()) return
        usersCollection.document(userEmail).collection("user_stats").document("stats").delete().await()
    }

    /**
     * Terminal Final: Scan all Firestore user documents and return the highest userId field.
     * Called before every new user registration to guarantee a collision-free auto-increment.
     */
    override suspend fun getMaxUserId(): Int {
        return try {
            val snapshot = usersCollection.get().await()
            snapshot.documents
                .mapNotNull { it.getLong("userId")?.toInt() }
                .maxOrNull() ?: 0
        } catch (e: Exception) {
            android.util.Log.w("FirestoreService", "getMaxUserId failed (offline?): ${e.message}")
            0
        }
    }
}
