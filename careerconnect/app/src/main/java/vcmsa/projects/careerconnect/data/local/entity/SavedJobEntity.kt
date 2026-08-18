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
 * Room entity for cached saved/bookmarked jobs
 * Supports offline viewing of bookmarked jobs
 */
@Entity(tableName = "saved_jobs")
data class SavedJobEntity(
    @PrimaryKey
    val jobId: String,
    val savedJobId: String?, // ID from server's saved_jobs table
    val title: String,
    val company: String,
    val location: String?,
    val jobType: String?,
    val workArrangement: String?,
    val experienceLevel: String?,
    val salary: String?,
    val description: String?,
    val requirements: String?,
    val benefits: String?,
    val industry: String?,
    val postedDate: Long?, // Timestamp
    val savedDate: Long, // When user saved it
    val isSynced: Boolean = true, // Whether synced with server
    val pendingAction: String? = null // "ADD" or "REMOVE" for pending sync
)

