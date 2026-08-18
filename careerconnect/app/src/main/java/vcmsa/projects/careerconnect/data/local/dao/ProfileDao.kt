//CODE ATTRIBUTION
//01
//Room DAO
//Adapted from: Android Developers. (2025). Room DAO. [online] Android Developers.
//Available at: https://developer.android.com/training/data-storage/room/accessing-data
//Date Accessed: 15 November 2025

package vcmsa.projects.careerconnect.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import vcmsa.projects.careerconnect.data.local.entity.ProfileEntity

/**
 * DAO for user profile
 * Provides CRUD operations for offline profile management
 */
@Dao
interface ProfileDao {
    
    /**
     * Get current user profile as Flow
     */
    @Query("SELECT * FROM user_profile LIMIT 1")
    fun getProfile(): Flow<ProfileEntity?>
    
    /**
     * Get current user profile (one-time fetch)
     */
    @Query("SELECT * FROM user_profile LIMIT 1")
    suspend fun getProfileOnce(): ProfileEntity?
    
    /**
     * Get profile by user ID
     */
    @Query("SELECT * FROM user_profile WHERE userId = :userId LIMIT 1")
    suspend fun getProfileByUserId(userId: String): ProfileEntity?
    
    /**
     * Check if profile has pending changes
     */
    @Query("SELECT EXISTS(SELECT 1 FROM user_profile WHERE isDirty = 1)")
    suspend fun hasPendingChanges(): Boolean
    
    /**
     * Insert or replace profile
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: ProfileEntity)
    
    /**
     * Update profile
     */
    @Update
    suspend fun updateProfile(profile: ProfileEntity)
    
    /**
     * Mark profile as dirty (has pending changes)
     */
    @Query("UPDATE user_profile SET isDirty = 1, pendingChanges = :pendingChanges WHERE userId = :userId")
    suspend fun markAsDirty(userId: String, pendingChanges: String)
    
    /**
     * Mark profile as synced
     */
    @Query("UPDATE user_profile SET isDirty = 0, pendingChanges = NULL, lastSyncedAt = :syncTime WHERE userId = :userId")
    suspend fun markAsSynced(userId: String, syncTime: Long)
    
    /**
     * Update last synced time
     */
    @Query("UPDATE user_profile SET lastSyncedAt = :syncTime WHERE userId = :userId")
    suspend fun updateLastSyncedTime(userId: String, syncTime: Long)
    
    /**
     * Delete profile
     */
    @Query("DELETE FROM user_profile")
    suspend fun deleteProfile()
    
    /**
     * Delete profile by user ID
     */
    @Query("DELETE FROM user_profile WHERE userId = :userId")
    suspend fun deleteProfileByUserId(userId: String)
}

