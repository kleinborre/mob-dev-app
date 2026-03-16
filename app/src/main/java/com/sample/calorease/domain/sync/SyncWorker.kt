package com.sample.calorease.domain.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Sprint 4 Phase 2: Periodic WorkManager Hook
 * Executes SyncManager seamlessly in the background to ensure Two-Way Firestore reconciliation 
 * across Airplane mode transitions and application minimizations.
 */
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val syncManager: SyncManager
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // Broker the two-way logic map mapping Local <-> Remote entities
            syncManager.performSync()
            
            // Mark Successful execution for OS cleanup
            Result.success()
        } catch (e: com.google.firebase.firestore.FirebaseFirestoreException) {
            // BUGFIX Sprint 4 Phase 6: Expose silent Firestore absences
            val errorMsg = "CRITICAL FIRESTORE ERROR: ${e.code.name}. Is your Cloud Firestore Database initialized in the Firebase Console?"
            android.util.Log.e("SyncWorker", errorMsg, e)
            Result.failure()
        } catch (e: Exception) {
            // If failed (e.g., severe IO or missing user states beyond offline catches), retry
            android.util.Log.e("SyncWorker", "Standard Sync error", e)
            Result.retry()
        }
    }
}
