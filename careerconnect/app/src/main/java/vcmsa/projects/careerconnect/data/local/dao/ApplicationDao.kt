//CODE ATTRIBUTION
//01
//Room DAO
//Adapted from: Android Developers. (2025). Room DAO. [online] Android Developers.
//Available at: https://developer.android.com/training/data-storage/room/accessing-data
//Date Accessed: 15 November 2025

package vcmsa.projects.careerconnect.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import vcmsa.projects.careerconnect.data.local.entity.ApplicationEntity

/**
 * DAO for job applications (submitted + drafts)
 * Provides CRUD operations for offline application management
 */
@Dao
interface ApplicationDao {
    
    /**
     * Get all applications (including drafts) as Flow
     */
    @Query("SELECT * FROM job_applications ORDER BY modifiedDate DESC")
    fun getAllApplications(): Flow<List<ApplicationEntity>>
    
    /**
     * Get submitted applications only
     */
    @Query("SELECT * FROM job_applications WHERE isDraft = 0 ORDER BY appliedDate DESC")
    fun getSubmittedApplications(): Flow<List<ApplicationEntity>>
    
    /**
     * Get draft applications only
     */
    @Query("SELECT * FROM job_applications WHERE isDraft = 1 ORDER BY createdDate DESC")
    fun getDraftApplications(): Flow<List<ApplicationEntity>>
    
    /**
     * Get applications that need to be synced
     */
    @Query("SELECT * FROM job_applications WHERE isSynced = 0 AND isDraft = 0")
    suspend fun getUnsyncedApplications(): List<ApplicationEntity>
    
    /**
     * Get application by local ID
     */
    @Query("SELECT * FROM job_applications WHERE localId = :localId LIMIT 1")
    suspend fun getApplicationByLocalId(localId: String): ApplicationEntity?
    
    /**
     * Get application by server ID
     */
    @Query("SELECT * FROM job_applications WHERE serverId = :serverId LIMIT 1")
    suspend fun getApplicationByServerId(serverId: String): ApplicationEntity?
    
    /**
     * Insert or replace an application
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApplication(application: ApplicationEntity)
    
    /**
     * Insert multiple applications
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApplications(applications: List<ApplicationEntity>)
    
    /**
     * Update an application
     */
    @Update
    suspend fun updateApplication(application: ApplicationEntity)
    
    /**
     * Delete an application
     */
    @Delete
    suspend fun deleteApplication(application: ApplicationEntity)
    
    /**
     * Delete application by local ID
     */
    @Query("DELETE FROM job_applications WHERE localId = :localId")
    suspend fun deleteApplicationById(localId: String)
    
    /**
     * Mark application as synced
     */
    @Query("UPDATE job_applications SET isSynced = 1, serverId = :serverId WHERE localId = :localId")
    suspend fun markAsSynced(localId: String, serverId: String)
    
    /**
     * Increment sync attempts
     */
    @Query("UPDATE job_applications SET syncAttempts = syncAttempts + 1 WHERE localId = :localId")
    suspend fun incrementSyncAttempts(localId: String)
    
    /**
     * Get count of draft applications
     */
    @Query("SELECT COUNT(*) FROM job_applications WHERE isDraft = 1")
    suspend fun getDraftCount(): Int
    
    /**
     * Clear all applications
     */
    @Query("DELETE FROM job_applications")
    suspend fun clearAll()
}

