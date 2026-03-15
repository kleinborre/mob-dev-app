package com.sample.calorease.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.sample.calorease.data.remote.dto.DailyEntryDto
import com.sample.calorease.data.remote.dto.UserDto
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

interface FirestoreService {
    suspend fun saveUser(user: UserDto)
    suspend fun getUser(userEmail: String): UserDto?
    suspend fun saveDailyEntry(userEmail: String, entry: DailyEntryDto)
    suspend fun getDailyEntries(userEmail: String): List<DailyEntryDto>
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

    override suspend fun saveDailyEntry(userEmail: String, entry: DailyEntryDto) {
        if (userEmail.isBlank()) return
        // We nest daily entries inside a sub-collection for the specific user
        val entriesCollection = usersCollection.document(userEmail).collection("daily_entries")
        // We use the entry's unique local timestamp or entryId plus date to guarantee a unique remote ID
        val uniqueId = "${entry.entryId}_${entry.date}"
        entriesCollection.document(uniqueId).set(entry).await()
    }

    override suspend fun getDailyEntries(userEmail: String): List<DailyEntryDto> {
        if (userEmail.isBlank()) return emptyList()
        val snapshot = usersCollection.document(userEmail).collection("daily_entries").get().await()
        return snapshot.documents.mapNotNull { it.toObject(DailyEntryDto::class.java) }
    }
}
