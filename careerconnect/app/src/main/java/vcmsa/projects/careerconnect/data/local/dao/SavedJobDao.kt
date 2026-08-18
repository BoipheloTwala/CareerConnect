//CODE ATTRIBUTION
//01
//Room DAO
//Adapted from: Android Developers. (2025). Room DAO. [online] Android Developers.
//Available at: https://developer.android.com/training/data-storage/room/accessing-data
//Date Accessed: 15 November 2025

package vcmsa.projects.careerconnect.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import vcmsa.projects.careerconnect.data.local.entity.SavedJobEntity

/**
 * DAO for saved/bookmarked jobs
 * Provides CRUD operations for offline job bookmarks
 */
@Dao
interface SavedJobDao {
    
    /**
     * Get all saved jobs as Flow for reactive UI updates
     */
    @Query("SELECT * FROM saved_jobs ORDER BY savedDate DESC")
    fun getAllSavedJobs(): Flow<List<SavedJobEntity>>
    
    /**
     * Get saved jobs that need to be synced
     */
    @Query("SELECT * FROM saved_jobs WHERE isSynced = 0")
    suspend fun getUnsyncedJobs(): List<SavedJobEntity>
    
    /**
     * Get a specific saved job by job ID
     */
    @Query("SELECT * FROM saved_jobs WHERE jobId = :jobId LIMIT 1")
    suspend fun getSavedJobById(jobId: String): SavedJobEntity?
    
    /**
     * Check if a job is saved
     */
    @Query("SELECT EXISTS(SELECT 1 FROM saved_jobs WHERE jobId = :jobId)")
    suspend fun isJobSaved(jobId: String): Boolean
    
    /**
     * Insert or replace a saved job
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedJob(job: SavedJobEntity)
    
    /**
     * Insert multiple saved jobs
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedJobs(jobs: List<SavedJobEntity>)
    
    /**
     * Delete a saved job
     */
    @Delete
    suspend fun deleteSavedJob(job: SavedJobEntity)
    
    /**
     * Delete saved job by job ID
     */
    @Query("DELETE FROM saved_jobs WHERE jobId = :jobId")
    suspend fun deleteSavedJobById(jobId: String)
    
    /**
     * Update sync status
     */
    @Query("UPDATE saved_jobs SET isSynced = :isSynced WHERE jobId = :jobId")
    suspend fun updateSyncStatus(jobId: String, isSynced: Boolean)
    
    /**
     * Clear all saved jobs
     */
    @Query("DELETE FROM saved_jobs")
    suspend fun clearAll()
    
    /**
     * Get count of saved jobs
     */
    @Query("SELECT COUNT(*) FROM saved_jobs")
    suspend fun getSavedJobsCount(): Int
}

