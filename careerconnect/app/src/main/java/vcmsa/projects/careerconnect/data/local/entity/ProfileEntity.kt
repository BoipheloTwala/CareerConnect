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
 * Room entity for cached user profile
 * Supports offline profile viewing and editing with pending sync
 */
@Entity(tableName = "user_profile")
data class ProfileEntity(
    @PrimaryKey
    val userId: String,
    val email: String,
    val firstName: String?,
    val lastName: String?,
    val phoneNumber: String?,
    val location: String?,
    val bio: String?,
    val companyName: String?,
    val profileImageUrl: String?,
    val userType: String, // "JOB_SEEKER" or "RECRUITER"
    val createdAt: Long?,
    val updatedAt: Long?,
    val lastSyncedAt: Long, // Last successful sync timestamp
    val isDirty: Boolean = false, // Whether there are pending local changes
    val pendingChanges: String? = null // JSON string of pending field changes
)

