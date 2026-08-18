//CODE ATTRIBUTION
//01
//Room DAO
//Adapted from: Android Developers. (2025). Room DAO. [online] Android Developers.
//Available at: https://developer.android.com/training/data-storage/room/accessing-data
//Date Accessed: 15 November 2025

package vcmsa.projects.careerconnect.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import vcmsa.projects.careerconnect.data.local.entity.SyncQueueEntity

/**
 * DAO for sync queue operations
 * Manages queue of pending sync operations
 */
@Dao
interface SyncQueueDao {
    
    /**
     * Get all pending sync operations
     */
    @Query("SELECT * FROM sync_queue WHERE status = 'PENDING' OR status = 'FAILED' ORDER BY createdAt ASC")
    suspend fun getPendingOperations(): List<SyncQueueEntity>
    
    /**
     * Get all sync operations as Flow
     */
    @Query("SELECT * FROM sync_queue ORDER BY createdAt DESC")
    fun getAllOperations(): Flow<List<SyncQueueEntity>>
    
    /**
     * Get operation by ID
     */
    @Query("SELECT * FROM sync_queue WHERE id = :id LIMIT 1")
    suspend fun getOperationById(id: Long): SyncQueueEntity?
    
    /**
     * Get operations by entity ID
     */
    @Query("SELECT * FROM sync_queue WHERE entityId = :entityId ORDER BY createdAt DESC")
    suspend fun getOperationsByEntityId(entityId: String): List<SyncQueueEntity>
    
    /**
     * Insert sync operation
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOperation(operation: SyncQueueEntity): Long
    
    /**
     * Update operation
     */
    @Update
    suspend fun updateOperation(operation: SyncQueueEntity)
    
    /**
     * Update operation status
     */
    @Query("UPDATE sync_queue SET status = :status, lastAttemptAt = :timestamp WHERE id = :id")
    suspend fun updateOperationStatus(id: Long, status: String, timestamp: Long)
    
    /**
     * Mark operation as in progress
     */
    @Query("UPDATE sync_queue SET status = 'IN_PROGRESS', lastAttemptAt = :timestamp, attempts = attempts + 1 WHERE id = :id")
    suspend fun markAsInProgress(id: Long, timestamp: Long)
    
    /**
     * Mark operation as completed
     */
    @Query("UPDATE sync_queue SET status = 'COMPLETED', lastAttemptAt = :timestamp WHERE id = :id")
    suspend fun markAsCompleted(id: Long, timestamp: Long)
    
    /**
     * Mark operation as failed with error message
     */
    @Query("UPDATE sync_queue SET status = 'FAILED', lastAttemptAt = :timestamp, attempts = attempts + 1, errorMessage = :error WHERE id = :id")
    suspend fun markAsFailed(id: Long, timestamp: Long, error: String)
    
    /**
     * Delete operation
     */
    @Delete
    suspend fun deleteOperation(operation: SyncQueueEntity)
    
    /**
     * Delete operation by ID
     */
    @Query("DELETE FROM sync_queue WHERE id = :id")
    suspend fun deleteOperationById(id: Long)
    
    /**
     * Delete completed operations
     */
    @Query("DELETE FROM sync_queue WHERE status = 'COMPLETED'")
    suspend fun deleteCompletedOperations()
    
    /**
     * Delete operations older than timestamp
     */
    @Query("DELETE FROM sync_queue WHERE createdAt < :timestamp AND status = 'COMPLETED'")
    suspend fun deleteOldCompletedOperations(timestamp: Long)
    
    /**
     * Get count of pending operations
     */
    @Query("SELECT COUNT(*) FROM sync_queue WHERE status = 'PENDING' OR status = 'FAILED'")
    suspend fun getPendingCount(): Int
    
    /**
     * Clear all operations
     */
    @Query("DELETE FROM sync_queue")
    suspend fun clearAll()
}

