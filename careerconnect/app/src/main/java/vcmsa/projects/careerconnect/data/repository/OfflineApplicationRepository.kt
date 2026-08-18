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
import kotlinx.coroutines.flow.map
import vcmsa.projects.careerconnect.data.local.AppDatabase
import vcmsa.projects.careerconnect.data.local.entity.ApplicationEntity
import vcmsa.projects.careerconnect.data.local.entity.SyncQueueEntity
import vcmsa.projects.careerconnect.domain.model.*
import vcmsa.projects.careerconnect.utils.NetworkConnectivityManager
import java.util.UUID

/**
 * Offline-first repository for job applications
 * Supports draft applications and offline submission queue
 */
class OfflineApplicationRepository(context: Context) {
    
    private val database = AppDatabase.getDatabase(context)
    private val applicationDao = database.applicationDao()
    private val syncQueueDao = database.syncQueueDao()
    private val networkManager = NetworkConnectivityManager(context)
    private val gson = Gson()
    
    /**
     * Get all applications (submitted + drafts)
     */
    fun getAllApplicationsFlow(): Flow<List<ApplicationEntity>> {
        return applicationDao.getAllApplications()
    }
    
    /**
     * Get submitted applications only
     */
    fun getSubmittedApplicationsFlow(): Flow<List<ApplicationEntity>> {
        return applicationDao.getSubmittedApplications()
    }
    
    /**
     * Get draft applications only
     */
    fun getDraftApplicationsFlow(): Flow<List<ApplicationEntity>> {
        return applicationDao.getDraftApplications()
    }
    
    /**
     * Create a draft application (works offline)
     */
    suspend fun createDraft(
        jobId: String,
        jobTitle: String?,
        companyName: String?,
        cvId: String?,
        coverLetter: String?
    ): Result<String> {
        return try {
            val localId = UUID.randomUUID().toString()
            val now = System.currentTimeMillis()
            
            val draft = ApplicationEntity(
                localId = localId,
                serverId = null,
                jobId = jobId,
                jobTitle = jobTitle,
                companyName = companyName,
                cvId = cvId,
                cvUrl = null,
                coverLetter = coverLetter,
                status = "DRAFT",
                appliedDate = null,
                createdDate = now,
                modifiedDate = now,
                isSynced = false,
                isDraft = true
            )
            
            applicationDao.insertApplication(draft)
            Result.success(localId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update a draft application
     */
    suspend fun updateDraft(
        localId: String,
        cvId: String?,
        coverLetter: String?
    ): Result<Unit> {
        return try {
            val existing = applicationDao.getApplicationByLocalId(localId)
            if (existing != null && existing.isDraft) {
                val updated = existing.copy(
                    cvId = cvId,
                    coverLetter = coverLetter,
                    modifiedDate = System.currentTimeMillis()
                )
                applicationDao.updateApplication(updated)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Draft not found or already submitted"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Submit application (works offline - queues for sync)
     */
    suspend fun submitApplication(localId: String): Result<Unit> {
        return try {
            val draft = applicationDao.getApplicationByLocalId(localId)
            if (draft == null || !draft.isDraft) {
                return Result.failure(Exception("Draft not found"))
            }
            
            // Mark as submitted locally
            val submitted = draft.copy(
                status = "PENDING",
                isDraft = false,
                appliedDate = System.currentTimeMillis(),
                modifiedDate = System.currentTimeMillis()
            )
            applicationDao.updateApplication(submitted)
            
            // Queue for sync if offline or try to submit immediately
            if (networkManager.isConnected()) {
                // Try to submit immediately
                // Note: Would need to call actual API here
                queueSubmitOperation(localId)
            } else {
                // Queue for later sync
                queueSubmitOperation(localId)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Delete a draft application
     */
    suspend fun deleteDraft(localId: String): Result<Unit> {
        return try {
            applicationDao.deleteApplicationById(localId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get draft count
     */
    suspend fun getDraftCount(): Int {
        return applicationDao.getDraftCount()
    }
    
    /**
     * Sync pending submissions
     */
    suspend fun syncPendingSubmissions(): Result<Int> {
        return try {
            if (!networkManager.isConnected()) {
                return Result.failure(Exception("No internet connection"))
            }
            
            val pendingOps = syncQueueDao.getPendingOperations()
                .filter { it.entityType == "APPLICATION" }
            
            var successCount = 0
            
            for (op in pendingOps) {
                syncQueueDao.markAsInProgress(op.id, System.currentTimeMillis())
                
                when (op.operationType) {
                    "SUBMIT_APPLICATION" -> {
                        // Here you would call the actual API
                        // For now, mark as completed
                        val application = applicationDao.getApplicationByLocalId(op.entityId)
                        application?.let {
                            val updated = it.copy(
                                isSynced = true,
                                status = "SUBMITTED"
                            )
                            applicationDao.updateApplication(updated)
                            syncQueueDao.markAsCompleted(op.id, System.currentTimeMillis())
                            successCount++
                        }
                    }
                }
            }
            
            Result.success(successCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private suspend fun queueSubmitOperation(localId: String) {
        val operation = SyncQueueEntity(
            operationType = "SUBMIT_APPLICATION",
            entityType = "APPLICATION",
            entityId = localId,
            payload = gson.toJson(mapOf("local_id" to localId)),
            createdAt = System.currentTimeMillis()
        )
        syncQueueDao.insertOperation(operation)
    }
}

