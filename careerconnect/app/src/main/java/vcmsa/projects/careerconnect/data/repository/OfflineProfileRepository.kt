//CODE ATTRIBUTION
//01
//Repository Pattern
//Adapted from: Android Developers. (2025). Repository Pattern. [online] Android Developers.
//Available at: https://developer.android.com/topic/architecture/data-layer
//Date Accessed: 15 November 2025

package vcmsa.projects.careerconnect.data.repository

import android.content.Context
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import vcmsa.projects.careerconnect.data.local.AppDatabase
import vcmsa.projects.careerconnect.data.local.entity.ProfileEntity
import vcmsa.projects.careerconnect.domain.model.*
import vcmsa.projects.careerconnect.utils.NetworkConnectivityManager

/**
 * Offline-first repository for user profile
 * Supports offline editing with conflict resolution
 */
class OfflineProfileRepository(context: Context) {
    
    private val database = AppDatabase.getDatabase(context)
    private val profileDao = database.profileDao()
    private val networkManager = NetworkConnectivityManager(context)
    private val onlineRepository = ProfileRepository()
    private val gson = Gson()
    
    /**
     * Get profile as Flow (reactive updates)
     */
    fun getProfileFlow(): Flow<ProfileEntity?> {
        return profileDao.getProfile()
    }
    
    /**
     * Get profile (one-time fetch)
     */
    suspend fun getProfile(): ProfileEntity? {
        return profileDao.getProfileOnce()
    }
    
    /**
     * Cache profile from server
     */
    suspend fun cacheProfile(userProfile: UserProfile): Result<Unit> {
        return try {
            val entity = userProfile.toProfileEntity()
            profileDao.insertProfile(entity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update profile (works offline with pending sync)
     */
    suspend fun updateProfile(
        userId: String,
        updates: Map<String, Any?>
    ): Result<Unit> {
        return try {
            val existing = profileDao.getProfileByUserId(userId)
            if (existing == null) {
                return Result.failure(Exception("Profile not found"))
            }
            
            // Apply updates to local profile
            val updated = applyUpdates(existing, updates)
            
            if (networkManager.isConnected()) {
                // Try to sync immediately
                val request = createUpdateRequest(updates)
                val result = onlineRepository.updateProfile(request)
                
                if (result.isSuccess) {
                    // Update successful - cache new data
                    result.getOrNull()?.let { serverProfile ->
                        profileDao.insertProfile(
                            serverProfile.toProfileEntity()
                        )
                    }
                } else {
                    // Failed to sync - mark as dirty
                    profileDao.insertProfile(
                        updated.copy(
                            isDirty = true,
                            pendingChanges = gson.toJson(updates)
                        )
                    )
                }
            } else {
                // Offline - mark as dirty
                profileDao.insertProfile(
                    updated.copy(
                        isDirty = true,
                        pendingChanges = gson.toJson(updates)
                    )
                )
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Sync profile changes (with conflict resolution)
     */
    suspend fun syncProfileChanges(): Result<Unit> {
        return try {
            if (!networkManager.isConnected()) {
                return Result.failure(Exception("No internet connection"))
            }
            
            val profile = profileDao.getProfileOnce()
            if (profile == null || !profile.isDirty) {
                return Result.success(Unit)
            }
            
            // Get server version
            val serverResult = onlineRepository.getProfile()
            if (serverResult.isFailure) {
                return Result.failure(serverResult.exceptionOrNull() ?: Exception("Sync failed"))
            }
            
            val serverProfile = serverResult.getOrNull()!!
            
            // Check for conflicts
            val lastSyncTime = profile.lastSyncedAt
            val serverUpdateTime = parseToTimestamp(serverProfile.updatedAt)
            
            if (serverUpdateTime != null && serverUpdateTime > lastSyncTime) {
                // Conflict detected - server has newer data
                // Strategy: Last Write Wins (server wins)
                resolveConflict(profile, serverProfile)
            } else {
                // No conflict - apply local changes to server
                val updates = if (profile.pendingChanges != null) {
                    gson.fromJson(profile.pendingChanges, Map::class.java) as Map<String, Any?>
                } else {
                    emptyMap()
                }
                
                val request = createUpdateRequest(updates)
                val updateResult = onlineRepository.updateProfile(request)
                
                if (updateResult.isSuccess) {
                    // Sync successful
                    profileDao.markAsSynced(profile.userId, System.currentTimeMillis())
                } else {
                    return Result.failure(updateResult.exceptionOrNull() ?: Exception("Update failed"))
                }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Refresh profile from server
     */
    suspend fun refreshFromServer(): Result<Unit> {
        return try {
            if (!networkManager.isConnected()) {
                return Result.failure(Exception("No internet connection"))
            }
            
            val result = onlineRepository.getProfile()
            if (result.isSuccess) {
                result.getOrNull()?.let { serverProfile ->
                    profileDao.insertProfile(serverProfile.toProfileEntity())
                }
                Result.success(Unit)
            } else {
                Result.failure(result.exceptionOrNull() ?: Exception("Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Check if profile has pending changes
     */
    suspend fun hasPendingChanges(): Boolean {
        return profileDao.hasPendingChanges()
    }
    
    /**
     * Clear profile cache (for logout)
     */
    suspend fun clearProfile() {
        profileDao.deleteProfile()
    }
    
    // ===== PRIVATE HELPER METHODS =====
    
    private fun applyUpdates(profile: ProfileEntity, updates: Map<String, Any?>): ProfileEntity {
        return profile.copy(
            firstName = (updates["firstName"] as? String) ?: profile.firstName,
            lastName = (updates["lastName"] as? String) ?: profile.lastName,
            phoneNumber = (updates["phoneNumber"] as? String) ?: profile.phoneNumber,
            location = (updates["location"] as? String) ?: profile.location,
            bio = (updates["bio"] as? String) ?: profile.bio,
            companyName = (updates["companyName"] as? String) ?: profile.companyName,
            profileImageUrl = (updates["profileImageUrl"] as? String) ?: profile.profileImageUrl,
            updatedAt = System.currentTimeMillis()
        )
    }
    
    private fun createUpdateRequest(updates: Map<String, Any?>): UpdateProfileRequest {
        return UpdateProfileRequest(
            firstName = updates["firstName"] as? String,
            lastName = updates["lastName"] as? String,
            phone = updates["phoneNumber"] as? String,
            location = updates["location"] as? String,
            bio = updates["bio"] as? String,
            companyName = updates["companyName"] as? String,
            profileImageUrl = updates["profileImageUrl"] as? String
        )
    }
    
    /**
     * Resolve conflict between local and server profile
     * Strategy: Server wins (Last Write Wins from server)
     */
    private suspend fun resolveConflict(localProfile: ProfileEntity, serverProfile: UserProfile) {
        // Server version is newer - accept server changes
        profileDao.insertProfile(
            serverProfile.toProfileEntity().copy(
                isDirty = false,
                pendingChanges = null
            )
        )
    }
}

// ===== EXTENSION FUNCTIONS =====

fun UserProfile.toProfileEntity(): ProfileEntity {
    return ProfileEntity(
        userId = this.firebaseUid,
        email = this.email,
        firstName = this.firstName,
        lastName = this.lastName,
        phoneNumber = this.phone,
        location = this.location,
        bio = this.bio,
        companyName = this.companyName,
        profileImageUrl = this.profileImageUrl,
        userType = this.userType.name,
        createdAt = parseToTimestamp(this.createdAt),
        updatedAt = parseToTimestamp(this.updatedAt),
        lastSyncedAt = System.currentTimeMillis(),
        isDirty = false,
        pendingChanges = null
    )
}

private fun parseToTimestamp(dateString: String?): Long? {
    return try {
        dateString?.let { java.time.Instant.parse(it).toEpochMilli() }
    } catch (e: Exception) {
        null
    }
}

