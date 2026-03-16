package com.sample.calorease.data.remote.dto

/**
 * Data Transfer Object for storing Food Entries in Firebase Firestore.
 */
data class DailyEntryDto(
    val entryId: Int = 0,
    val userId: Int = 0,
    val date: Long = 0L,
    val foodName: String = "",
    val calories: Int = 0,
    val mealType: String = "",
    val lastUpdated: Long = System.currentTimeMillis(), // Sprint 4 Phase 1: Sync Timestamp
    val isDeleted: Boolean = false,
    val syncId: String = ""
)
