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
 * Room entity for job applications (submitted + drafts)
 * Supports offline application management and draft creation
 */
@Entity(tableName = "job_applications")
data class ApplicationEntity(
    @PrimaryKey
    val localId: String, // Local UUID
    val serverId: String?, // Server application ID (null for drafts)
    val jobId: String,
    val jobTitle: String?,
    val companyName: String?,
    val cvId: String?,
    val cvUrl: String?,
    val coverLetter: String?,
    val status: String, // "DRAFT", "PENDING", "SUBMITTED", "UNDER_REVIEW", etc.
    val appliedDate: Long?, // Timestamp when submitted
    val createdDate: Long, // When created locally
    val modifiedDate: Long, // Last modification
    val isSynced: Boolean = false, // Whether synced with server
    val isDraft: Boolean = true, // Whether it's a draft or submitted
    val syncAttempts: Int = 0 // Number of sync attempts for failed syncs
)

