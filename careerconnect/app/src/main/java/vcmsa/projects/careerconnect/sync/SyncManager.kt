//CODE ATTRIBUTION
//01
//WorkManager
//Adapted from: Android Developers. (2025). WorkManager. [online] Android Developers.
//Available at: https://developer.android.com/topic/libraries/architecture/workmanager
//Date Accessed: 15 November 2025

package vcmsa.projects.careerconnect.sync

import android.content.Context
import androidx.work.*
import vcmsa.projects.careerconnect.data.local.AppDatabase
import vcmsa.projects.careerconnect.data.repository.*
import vcmsa.projects.careerconnect.utils.NetworkConnectivityManager
import java.util.concurrent.TimeUnit

/**
 * Central manager for all synchronization operations
 * Coordinates sync between local database and server
 */
class SyncManager(private val context: Context) {
    
    private val workManager = WorkManager.getInstance(context)
    private val networkManager = NetworkConnectivityManager(context)
    
    private val savedJobRepository = OfflineSavedJobRepository(context)
    private val applicationRepository = OfflineApplicationRepository(context)
    private val profileRepository = OfflineProfileRepository(context)
    
    /**
     * Sync all pending operations
     */
    suspend fun syncAll(): Result<SyncResult> {
        if (!networkManager.isConnected()) {
            return Result.failure(Exception("No internet connection"))
        }
        
        var totalSuccess = 0
        var totalFailed = 0
        val errors = mutableListOf<String>()
        
        // Sync saved jobs
        try {
            val jobResult = savedJobRepository.syncPendingOperations()
            if (jobResult.isSuccess) {
                totalSuccess += jobResult.getOrNull() ?: 0
            } else {
                errors.add("Jobs sync failed: ${jobResult.exceptionOrNull()?.message}")
                totalFailed++
            }
        } catch (e: Exception) {
            errors.add("Jobs sync error: ${e.message}")
            totalFailed++
        }
        
        // Sync applications
        try {
            val appResult = applicationRepository.syncPendingSubmissions()
            if (appResult.isSuccess) {
                totalSuccess += appResult.getOrNull() ?: 0
            } else {
                errors.add("Applications sync failed: ${appResult.exceptionOrNull()?.message}")
                totalFailed++
            }
        } catch (e: Exception) {
            errors.add("Applications sync error: ${e.message}")
            totalFailed++
        }
        
        // Sync profile
        try {
            val profileResult = profileRepository.syncProfileChanges()
            if (profileResult.isSuccess) {
                totalSuccess++
            } else {
                errors.add("Profile sync failed: ${profileResult.exceptionOrNull()?.message}")
                totalFailed++
            }
        } catch (e: Exception) {
            errors.add("Profile sync error: ${e.message}")
            totalFailed++
        }
        
        return Result.success(
            SyncResult(
                successCount = totalSuccess,
                failedCount = totalFailed,
                errors = errors
            )
        )
    }
    
    /**
     * Schedule periodic background sync
     */
    fun schedulePeriodicSync(intervalHours: Long = 1) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            intervalHours, TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()
        
        workManager.enqueueUniquePeriodicWork(
            SYNC_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }
    
    /**
     * Trigger immediate sync
     */
    fun triggerImmediateSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .build()
        
        workManager.enqueueUniqueWork(
            IMMEDIATE_SYNC_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            syncRequest
        )
    }
    
    /**
     * Cancel all sync work
     */
    fun cancelAllSync() {
        workManager.cancelUniqueWork(SYNC_WORK_NAME)
        workManager.cancelUniqueWork(IMMEDIATE_SYNC_WORK_NAME)
    }
    
    /**
     * Get sync status
     */
    suspend fun getSyncStatus(): SyncStatus {
        val unsyncedJobs = savedJobRepository.getUnsyncedCount()
        val unsyncedApps = applicationRepository.getDraftCount()
        val unsyncedProfile = profileRepository.hasPendingChanges()
        
        return SyncStatus(
            pendingJobsCount = unsyncedJobs,
            pendingApplicationsCount = unsyncedApps,
            profileHasPendingChanges = unsyncedProfile,
            isConnected = networkManager.isConnected()
        )
    }
    
    companion object {
        private const val SYNC_WORK_NAME = "periodic_sync"
        private const val IMMEDIATE_SYNC_WORK_NAME = "immediate_sync"
    }
}

/**
 * Result of sync operation
 */
data class SyncResult(
    val successCount: Int,
    val failedCount: Int,
    val errors: List<String>
)

/**
 * Current sync status
 */
data class SyncStatus(
    val pendingJobsCount: Int,
    val pendingApplicationsCount: Int,
    val profileHasPendingChanges: Boolean,
    val isConnected: Boolean
) {
    fun hasPendingSync(): Boolean {
        return pendingJobsCount > 0 || pendingApplicationsCount > 0 || profileHasPendingChanges
    }
}

