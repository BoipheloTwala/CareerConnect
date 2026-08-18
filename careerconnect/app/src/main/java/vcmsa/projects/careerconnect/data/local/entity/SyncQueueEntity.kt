//CODE ATTRIBUTION
//01
//Room Entity
//Adapted from: Android Developers. (2025). Room Entity. [online] Android Developers.
//Available at: https://developer.android.com/training/data-storage/room/defining-data
//Date Accessed: 15 November 2025

package vcmsa.projects.careerconnect.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for tracking pending sync operations
 * Queue system for operations that need to be synced to server
 */
@Entity(tableName = "sync_queue")
data class SyncQueueEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val operationType: String, // "SAVE_JOB", "REMOVE_JOB", "SUBMIT_APPLICATION", "UPDATE_PROFILE"
    val entityType: String, // "JOB", "APPLICATION", "PROFILE"
    val entityId: String, // ID of the entity being synced
    val payload: String, // JSON payload of the operation
    val createdAt: Long, // When operation was queued
    val attempts: Int = 0, // Number of sync attempts
    val lastAttemptAt: Long? = null, // Last sync attempt timestamp
    val errorMessage: String? = null, // Last error if failed
    val status: String = "PENDING" // "PENDING", "IN_PROGRESS", "COMPLETED", "FAILED"
)

