//CODE ATTRIBUTION
//01
//CoroutineWorker
//Adapted from: Android Developers. (2025). CoroutineWorker. [online] Android Developers.
//Available at: https://developer.android.com/reference/kotlin/androidx/work/CoroutineWorker
//Date Accessed: 15 November 2025

package vcmsa.projects.careerconnect.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Background worker for periodic data synchronization
 * Runs automatically when device has internet connection
 */
class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    private val syncManager = SyncManager(context)
    
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        return@withContext try {
            Log.d(TAG, "Starting background sync...")
            
            val syncResult = syncManager.syncAll()
            
            if (syncResult.isSuccess) {
                val result = syncResult.getOrNull()!!
                Log.d(TAG, "Sync completed: ${result.successCount} successful, ${result.failedCount} failed")
                
                if (result.errors.isNotEmpty()) {
                    Log.w(TAG, "Sync errors: ${result.errors.joinToString(", ")}")
                }
                
                // Consider partial success as success
                Result.success()
            } else {
                Log.e(TAG, "Sync failed: ${syncResult.exceptionOrNull()?.message}")
                Result.retry()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Sync worker error: ${e.message}", e)
            Result.retry()
        }
    }
    
    companion object {
        private const val TAG = "SyncWorker"
    }
}

