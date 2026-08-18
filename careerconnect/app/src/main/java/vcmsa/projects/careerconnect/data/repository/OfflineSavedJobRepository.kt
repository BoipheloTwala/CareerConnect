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
import vcmsa.projects.careerconnect.data.local.entity.SavedJobEntity
import vcmsa.projects.careerconnect.data.local.entity.SyncQueueEntity
import vcmsa.projects.careerconnect.domain.model.*
import vcmsa.projects.careerconnect.utils.NetworkConnectivityManager

/**
 * Offline-first repository for saved jobs
 * Implements offline caching and sync queue functionality
 */
class OfflineSavedJobRepository(context: Context) {
    
    private val database = AppDatabase.getDatabase(context)
    private val savedJobDao = database.savedJobDao()
    private val syncQueueDao = database.syncQueueDao()
    private val networkManager = NetworkConnectivityManager(context)
    private val onlineRepository = SavedJobRepository()
    private val gson = Gson()
    
    /**
     * Get all saved jobs (offline-first)
     * Returns cached data immediately, refreshes from API if online
     */
    fun getSavedJobsFlow(): Flow<List<SavedJob>> {
        return savedJobDao.getAllSavedJobs().map { entities ->
            entities.map { it.toSavedJob() }
        }
    }
    
    /**
     * Save a job (works offline)
     * Queues operation if offline, executes immediately if online
     */
    suspend fun saveJob(job: Job): Result<Unit> {
        return try {
            val entity = job.toSavedJobEntity()
            savedJobDao.insertSavedJob(entity)
            
            if (networkManager.isConnected()) {
                // Online: Try to sync immediately
                val request = SaveJobRequest(
                    jobId = job.id,
                    tags = emptyList(),
                    notes = null,
                    reminderDate = null
                )
                
                val result = onlineRepository.saveJob(request)
                if (result.isSuccess) {
                    // Update with server data
                    result.getOrNull()?.let { savedJob ->
                        savedJobDao.insertSavedJob(
                            entity.copy(
                                savedJobId = savedJob.id,
                                isSynced = true,
                                pendingAction = null
                            )
                        )
                    }
                } else {
                    // Failed to sync - queue for later
                    queueSaveOperation(job.id)
                }
            } else {
                // Offline: Queue for sync
                savedJobDao.updateSyncStatus(job.id, false)
                queueSaveOperation(job.id)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Remove a saved job (works offline)
     */
    suspend fun removeSavedJob(jobId: String): Result<Unit> {
        return try {
            if (networkManager.isConnected()) {
                // Online: Try to remove from server
                val savedJob = savedJobDao.getSavedJobById(jobId)
                savedJob?.savedJobId?.let { savedJobId ->
                    val result = onlineRepository.removeSavedJob(savedJobId)
                    if (result.isSuccess) {
                        savedJobDao.deleteSavedJobById(jobId)
                    } else {
                        // Failed - queue for later and mark as pending delete
                        queueRemoveOperation(jobId)
                        savedJobDao.insertSavedJob(
                            savedJob.copy(
                                isSynced = false,
                                pendingAction = "REMOVE"
                            )
                        )
                    }
                }
            } else {
                // Offline: Queue for deletion
                val savedJob = savedJobDao.getSavedJobById(jobId)
                savedJob?.let {
                    queueRemoveOperation(jobId)
                    savedJobDao.insertSavedJob(
                        it.copy(
                            isSynced = false,
                            pendingAction = "REMOVE"
                        )
                    )
                }
            }
            
            // Remove from local cache immediately for better UX
            savedJobDao.deleteSavedJobById(jobId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Check if job is saved
     */
    suspend fun isJobSaved(jobId: String): Boolean {
        return savedJobDao.isJobSaved(jobId)
    }
    
    /**
     * Refresh saved jobs from server (when online)
     */
    suspend fun refreshFromServer(): Result<Unit> {
        return try {
            if (!networkManager.isConnected()) {
                return Result.failure(Exception("No internet connection"))
            }
            
            val result = onlineRepository.getSavedJobs(page = 1, limit = 100)
            if (result.isSuccess) {
                val savedJobs = result.getOrNull()?.data ?: emptyList()
                val entities = savedJobs.map { it.toSavedJobEntity() }
                savedJobDao.insertSavedJobs(entities)
                Result.success(Unit)
            } else {
                Result.failure(result.exceptionOrNull() ?: Exception("Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Sync pending operations
     */
    suspend fun syncPendingOperations(): Result<Int> {
        return try {
            if (!networkManager.isConnected()) {
                return Result.failure(Exception("No internet connection"))
            }
            
            val pendingOps = syncQueueDao.getPendingOperations()
                .filter { it.entityType == "JOB" }
            
            var successCount = 0
            
            for (op in pendingOps) {
                syncQueueDao.markAsInProgress(op.id, System.currentTimeMillis())
                
                val success = when (op.operationType) {
                    "SAVE_JOB" -> {
                        val request = gson.fromJson(op.payload, SaveJobRequest::class.java)
                        val result = onlineRepository.saveJob(request)
                        if (result.isSuccess) {
                            savedJobDao.updateSyncStatus(op.entityId, true)
                            true
                        } else {
                            false
                        }
                    }
                    "REMOVE_JOB" -> {
                        val savedJob = savedJobDao.getSavedJobById(op.entityId)
                        savedJob?.savedJobId?.let { savedJobId ->
                            val result = onlineRepository.removeSavedJob(savedJobId)
                            if (result.isSuccess) {
                                savedJobDao.deleteSavedJobById(op.entityId)
                                true
                            } else {
                                false
                            }
                        } ?: false
                    }
                    else -> false
                }
                
                if (success) {
                    syncQueueDao.markAsCompleted(op.id, System.currentTimeMillis())
                    successCount++
                } else {
                    syncQueueDao.markAsFailed(op.id, System.currentTimeMillis(), "Sync failed")
                }
            }
            
            Result.success(successCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get count of unsynced items
     */
    suspend fun getUnsyncedCount(): Int {
        return savedJobDao.getUnsyncedJobs().size
    }
    
    // ===== PRIVATE HELPER METHODS =====
    
    private suspend fun queueSaveOperation(jobId: String) {
        val operation = SyncQueueEntity(
            operationType = "SAVE_JOB",
            entityType = "JOB",
            entityId = jobId,
            payload = gson.toJson(SaveJobRequest(jobId = jobId, tags = emptyList())),
            createdAt = System.currentTimeMillis()
        )
        syncQueueDao.insertOperation(operation)
    }
    
    private suspend fun queueRemoveOperation(jobId: String) {
        val operation = SyncQueueEntity(
            operationType = "REMOVE_JOB",
            entityType = "JOB",
            entityId = jobId,
            payload = gson.toJson(mapOf("job_id" to jobId)),
            createdAt = System.currentTimeMillis()
        )
        syncQueueDao.insertOperation(operation)
    }
}

// ===== EXTENSION FUNCTIONS FOR MAPPING =====

/**
 * Convert Job to SavedJobEntity
 */
fun Job.toSavedJobEntity(): SavedJobEntity {
    val salaryStr = when {
        salaryMin != null && salaryMax != null -> "${currency ?: "USD"} $salaryMin - $salaryMax"
        salaryMin != null -> "${currency ?: "USD"} $salaryMin+"
        salaryMax != null -> "Up to ${currency ?: "USD"} $salaryMax"
        else -> null
    }
    
    return SavedJobEntity(
        jobId = this.id,
        savedJobId = null,
        title = this.title,
        company = this.companyName,
        location = this.location,
        jobType = this.jobType.name,
        workArrangement = null, // Not available in Job model
        experienceLevel = this.experienceLevel.name,
        salary = salaryStr,
        description = this.description,
        requirements = this.requirements,
        benefits = this.benefits,
        industry = this.industry,
        postedDate = parseToTimestamp(this.createdAt),
        savedDate = System.currentTimeMillis(),
        isSynced = false,
        pendingAction = "ADD"
    )
}

/**
 * Convert SavedJob to SavedJobEntity
 */
fun SavedJob.toSavedJobEntity(): SavedJobEntity {
    val job = this.job ?: throw IllegalStateException("SavedJob must have job data")
    
    val salaryStr = when {
        job.salaryMin != null && job.salaryMax != null -> "${job.currency ?: "USD"} ${job.salaryMin} - ${job.salaryMax}"
        job.salaryMin != null -> "${job.currency ?: "USD"} ${job.salaryMin}+"
        job.salaryMax != null -> "Up to ${job.currency ?: "USD"} ${job.salaryMax}"
        else -> null
    }
    
    return SavedJobEntity(
        jobId = job.id,
        savedJobId = this.id,
        title = job.title,
        company = job.companyName,
        location = job.location,
        jobType = job.jobType.name,
        workArrangement = null, // Not available in Job model
        experienceLevel = job.experienceLevel.name,
        salary = salaryStr,
        description = job.description,
        requirements = job.requirements,
        benefits = job.benefits,
        industry = job.industry,
        postedDate = parseToTimestamp(job.createdAt),
        savedDate = parseToTimestamp(this.createdAt) ?: System.currentTimeMillis(),
        isSynced = true,
        pendingAction = null
    )
}

/**
 * Convert SavedJobEntity to SavedJob
 */
fun SavedJobEntity.toSavedJob(): SavedJob {
    // Parse salary string back to min/max
    val (salaryMin, salaryMax, currency) = parseSalaryString(this.salary)
    
    // Parse job type and experience level back to enums
    val jobTypeEnum = try {
        JobType.valueOf(this.jobType ?: "FULL_TIME")
    } catch (e: Exception) {
        JobType.FULL_TIME
    }
    
    val experienceLevelEnum = try {
        ExperienceLevel.valueOf(this.experienceLevel ?: "ENTRY")
    } catch (e: Exception) {
        ExperienceLevel.ENTRY
    }
    
    return SavedJob(
        id = this.savedJobId ?: this.jobId,
        jobId = this.jobId,
        userUid = "", // Will be filled from auth
        job = Job(
            id = this.jobId,
            recruiterUid = "", // Not stored locally
            title = this.title,
            description = this.description ?: "",
            companyName = this.company,
            location = this.location ?: "",
            jobType = jobTypeEnum,
            experienceLevel = experienceLevelEnum,
            salaryMin = salaryMin,
            salaryMax = salaryMax,
            currency = currency,
            industry = this.industry,
            requirements = this.requirements,
            benefits = this.benefits,
            applicationCount = 0,
            isActive = true,
            createdAt = this.postedDate?.let { timestampToString(it) } ?: timestampToString(System.currentTimeMillis()),
            updatedAt = timestampToString(System.currentTimeMillis())
        ),
        tags = emptyList(),
        notes = null,
        priority = SavedJobPriority.MEDIUM,
        reminderDate = null,
        isReminderSet = false,
        createdAt = timestampToString(this.savedDate),
        updatedAt = timestampToString(System.currentTimeMillis())
    )
}

/**
 * Parse salary string into min, max, and currency
 */
private fun parseSalaryString(salary: String?): Triple<Double?, Double?, String?> {
    if (salary.isNullOrBlank()) return Triple(null, null, null)
    
    return try {
        val parts = salary.split(" ")
        val currency = parts.firstOrNull()
        val numbers = salary.replace(Regex("[^0-9.-]"), " ").trim().split(Regex("\\s+"))
        
        when {
            numbers.size >= 2 -> Triple(numbers[0].toDoubleOrNull(), numbers[1].toDoubleOrNull(), currency)
            numbers.size == 1 -> Triple(numbers[0].toDoubleOrNull(), null, currency)
            else -> Triple(null, null, currency)
        }
    } catch (e: Exception) {
        Triple(null, null, null)
    }
}

private fun parseToTimestamp(dateString: String?): Long? {
    return try {
        dateString?.let { java.time.Instant.parse(it).toEpochMilli() }
    } catch (e: Exception) {
        null
    }
}

private fun timestampToString(timestamp: Long): String {
    return java.time.Instant.ofEpochMilli(timestamp).toString()
}

