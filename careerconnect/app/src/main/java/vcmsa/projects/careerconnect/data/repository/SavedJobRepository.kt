package vcmsa.projects.careerconnect.data.repository

import vcmsa.projects.careerconnect.data.network.ApiErrorHandler
import vcmsa.projects.careerconnect.data.network.NetworkModule
import vcmsa.projects.careerconnect.data.network.safeApiCall
import vcmsa.projects.careerconnect.domain.model.*

/**
 * Repository class for handling saved jobs operations
 */
class SavedJobRepository {
    
    private val apiService = NetworkModule.apiService
    
    // ===== SAVED JOBS OPERATIONS =====
    
    /**
     * Save a job
     */
    suspend fun saveJob(request: SaveJobRequest): Result<SavedJob> {
        // Validate request before making API call
        val validationResult = validateSaveJobRequest(request)
        if (!validationResult.isValid()) {
            val errors = validationResult.getErrors().joinToString(", ") { "${it.field}: ${it.message}" }
            return Result.failure(Exception("Validation failed: $errors"))
        }
        
        return safeApiCall(
            apiCall = { apiService.saveJob(request) },
            errorHandler = { ApiErrorHandler.logError("SavedJobRepository", it, "Save Job") }
        )
    }
    
    /**
     * Get user's saved jobs
     */
    suspend fun getSavedJobs(page: Int = 1, limit: Int = 20): Result<PaginatedResponse<SavedJob>> {
        if (page < 1) {
            return Result.failure(Exception("Page must be greater than 0"))
        }
        
        if (limit < 1 || limit > 100) {
            return Result.failure(Exception("Limit must be between 1 and 100"))
        }
        
        return safeApiCall(
            apiCall = { apiService.getSavedJobs(page, limit) },
            errorHandler = { ApiErrorHandler.logError("SavedJobRepository", it, "Get Saved Jobs") }
        ).mapCatching { json ->
            parseSavedJobsJson(json)
        }
    }

    private fun parseSavedJobsJson(element: com.google.gson.JsonElement): PaginatedResponse<SavedJob> {
        val gson = com.google.gson.Gson()
        return if (element.isJsonObject) {
            val obj = element.asJsonObject
            val dataArray = obj.get("data") ?: obj.get("saved_jobs") ?: obj.get("items")
            val paginationObj = obj.getAsJsonObject("pagination")
            val typeList = com.google.gson.reflect.TypeToken.getParameterized(List::class.java, SavedJob::class.java).type
            val list: List<SavedJob> = if (dataArray != null && dataArray.isJsonArray) gson.fromJson(dataArray, typeList) else emptyList()
            val pagination = if (paginationObj != null) gson.fromJson(paginationObj, PaginationMeta::class.java) else PaginationMeta(
                page = 1,
                limit = list.size,
                total = list.size,
                totalPages = 1,
                hasNext = false,
                hasPrev = false
            )
            PaginatedResponse(
                success = true,
                data = list,
                pagination = pagination
            )
        } else if (element.isJsonArray) {
            val typeList = com.google.gson.reflect.TypeToken.getParameterized(List::class.java, SavedJob::class.java).type
            val list: List<SavedJob> = gson.fromJson(element, typeList)
            PaginatedResponse(
                success = true,
                data = list,
                pagination = PaginationMeta(
                    page = 1,
                    limit = list.size,
                    total = list.size,
                    totalPages = 1,
                    hasNext = false,
                    hasPrev = false
                )
            )
        } else {
            PaginatedResponse(
                success = true,
                data = emptyList(),
                pagination = PaginationMeta(
                    page = 1,
                    limit = 0,
                    total = 0,
                    totalPages = 1,
                    hasNext = false,
                    hasPrev = false
                )
            )
        }
    }
    
    /**
     * Search saved jobs
     */
    suspend fun searchSavedJobs(request: SavedJobsSearchRequest): Result<SavedJobsSearchResponse> {
        // Validate request before making API call
        val validationResult = validateSavedJobsSearchRequest(request)
        if (!validationResult.isValid()) {
            val errors = validationResult.getErrors().joinToString(", ") { "${it.field}: ${it.message}" }
            return Result.failure(Exception("Validation failed: $errors"))
        }
        
        return safeApiCall(
            apiCall = { apiService.searchSavedJobs(request) },
            errorHandler = { ApiErrorHandler.logError("SavedJobRepository", it, "Search Saved Jobs") }
        )
    }
    
    /**
     * Get saved job details
     */
    suspend fun getSavedJobDetails(savedJobId: String): Result<SavedJob> {
        if (savedJobId.isBlank()) {
            return Result.failure(Exception("Saved job ID cannot be empty"))
        }
        
        return safeApiCall(
            apiCall = { apiService.getSavedJobDetails(savedJobId) },
            errorHandler = { ApiErrorHandler.logError("SavedJobRepository", it, "Get Saved Job Details") }
        )
    }
    
    /**
     * Update saved job
     */
    suspend fun updateSavedJob(savedJobId: String, request: UpdateSavedJobRequest): Result<SavedJob> {
        if (savedJobId.isBlank()) {
            return Result.failure(Exception("Saved job ID cannot be empty"))
        }
        
        // Validate request before making API call
        val validationResult = validateUpdateSavedJobRequest(request)
        if (!validationResult.isValid()) {
            val errors = validationResult.getErrors().joinToString(", ") { "${it.field}: ${it.message}" }
            return Result.failure(Exception("Validation failed: $errors"))
        }
        
        return safeApiCall(
            apiCall = { apiService.updateSavedJob(savedJobId, request) },
            errorHandler = { ApiErrorHandler.logError("SavedJobRepository", it, "Update Saved Job") }
        )
    }
    
    /**
     * Remove saved job
     */
    suspend fun removeSavedJob(savedJobId: String): Result<Unit> {
        if (savedJobId.isBlank()) {
            return Result.failure(Exception("Saved job ID cannot be empty"))
        }
        
        return safeApiCall(
            apiCall = { apiService.removeSavedJob(savedJobId) },
            errorHandler = { ApiErrorHandler.logError("SavedJobRepository", it, "Remove Saved Job") }
        )
    }
    
    /**
     * Check if job is saved
     */
    suspend fun isJobSaved(jobId: String): Result<SavedJob?> {
        if (jobId.isBlank()) {
            return Result.failure(Exception("Job ID cannot be empty"))
        }
        
        return safeApiCall(
            apiCall = { apiService.isJobSaved(jobId) },
            errorHandler = { ApiErrorHandler.logError("SavedJobRepository", it, "Check if Job is Saved") }
        )
    }
    
    /**
     * Bulk operations on saved jobs
     */
    suspend fun bulkSavedJobsOperation(request: SavedJobsBulkRequest): Result<Unit> {
        // Validate request before making API call
        val validationResult = validateSavedJobsBulkRequest(request)
        if (!validationResult.isValid()) {
            val errors = validationResult.getErrors().joinToString(", ") { "${it.field}: ${it.message}" }
            return Result.failure(Exception("Validation failed: $errors"))
        }
        
        return safeApiCall(
            apiCall = { apiService.bulkSavedJobsOperation(request) },
            errorHandler = { ApiErrorHandler.logError("SavedJobRepository", it, "Bulk Saved Jobs Operation") }
        )
    }
    
    /**
     * Bulk update saved jobs
     */
    suspend fun bulkUpdateSavedJobs(request: SavedJobsBulkUpdateRequest): Result<Unit> {
        // Validate request before making API call
        val validationResult = validateSavedJobsBulkUpdateRequest(request)
        if (!validationResult.isValid()) {
            val errors = validationResult.getErrors().joinToString(", ") { "${it.field}: ${it.message}" }
            return Result.failure(Exception("Validation failed: $errors"))
        }
        
        return safeApiCall(
            apiCall = { apiService.bulkUpdateSavedJobs(request) },
            errorHandler = { ApiErrorHandler.logError("SavedJobRepository", it, "Bulk Update Saved Jobs") }
        )
    }
    
    /**
     * Get saved jobs analytics
     */
    suspend fun getSavedJobsAnalytics(): Result<SavedJobsAnalytics> {
        return safeApiCall(
            apiCall = { apiService.getSavedJobsAnalytics() },
            errorHandler = { ApiErrorHandler.logError("SavedJobRepository", it, "Get Saved Jobs Analytics") }
        )
    }
    
    /**
     * Get saved jobs reminders
     */
    suspend fun getSavedJobsReminders(): Result<List<SavedJob>> {
        return safeApiCall(
            apiCall = { apiService.getSavedJobsReminders() },
            errorHandler = { ApiErrorHandler.logError("SavedJobRepository", it, "Get Saved Jobs Reminders") }
        )
    }
    
    // ===== HELPER METHODS =====
    
    /**
     * Save job with custom error handling
     */
    suspend fun saveJobWithErrorHandling(
        request: SaveJobRequest,
        onError: (String) -> Unit
    ): Result<SavedJob> {
        return saveJob(request).also { result ->
            result.onFailure { throwable ->
                val errorMessage = ApiErrorHandler.getErrorMessage(throwable)
                onError(errorMessage)
            }
        }
    }
    
    /**
     * Search saved jobs with custom error handling
     */
    suspend fun searchSavedJobsWithErrorHandling(
        request: SavedJobsSearchRequest,
        onError: (String) -> Unit
    ): Result<SavedJobsSearchResponse> {
        return searchSavedJobs(request).also { result ->
            result.onFailure { throwable ->
                val errorMessage = ApiErrorHandler.getErrorMessage(throwable)
                onError(errorMessage)
            }
        }
    }
    
    /**
     * Get saved jobs as NetworkResult for UI state management
     */
    suspend fun getSavedJobsAsNetworkResult(page: Int = 1, limit: Int = 20): NetworkResult<PaginatedResponse<SavedJob>> {
        return getSavedJobs(page, limit).toNetworkResult()
    }
    
    /**
     * Search saved jobs as NetworkResult for UI state management
     */
    suspend fun searchSavedJobsAsNetworkResult(request: SavedJobsSearchRequest): NetworkResult<SavedJobsSearchResponse> {
        return searchSavedJobs(request).toNetworkResult()
    }
    
    // ===== VALIDATION METHODS =====
    
    private fun validateSaveJobRequest(request: SaveJobRequest): ValidationResult {
        val errors = mutableListOf<ValidationError>()
        
        // Required fields validation
        if (request.jobId.isBlank()) {
            errors.add(ValidationError("job_id", "Job ID is required"))
        }
        
        // Notes validation
        request.notes?.let { notes ->
            if (notes.length > 1000) {
                errors.add(ValidationError("notes", "Notes cannot exceed 1000 characters"))
            }
        }
        
        // Tags validation
        if (request.tags.size > 20) {
            errors.add(ValidationError("tags", "Maximum 20 tags allowed"))
        }
        
        if (request.tags.any { it.isBlank() }) {
            errors.add(ValidationError("tags", "Tags cannot be empty"))
        }
        
        if (request.tags.any { it.length > 50 }) {
            errors.add(ValidationError("tags", "Each tag cannot exceed 50 characters"))
        }
        
        // Reminder date validation
        request.reminderDate?.let { reminderDate ->
            try {
                // Basic ISO 8601 format validation
                java.time.Instant.parse(reminderDate)
            } catch (e: Exception) {
                errors.add(ValidationError("reminder_date", "Reminder date must be in ISO 8601 format"))
            }
        }
        
        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errors)
        }
    }
    
    private fun validateUpdateSavedJobRequest(request: UpdateSavedJobRequest): ValidationResult {
        val errors = mutableListOf<ValidationError>()
        
        // Notes validation
        request.notes?.let { notes ->
            if (notes.length > 1000) {
                errors.add(ValidationError("notes", "Notes cannot exceed 1000 characters"))
            }
        }
        
        // Tags validation
        request.tags?.let { tags ->
            if (tags.size > 20) {
                errors.add(ValidationError("tags", "Maximum 20 tags allowed"))
            }
            
            if (tags.any { it.isBlank() }) {
                errors.add(ValidationError("tags", "Tags cannot be empty"))
            }
            
            if (tags.any { it.length > 50 }) {
                errors.add(ValidationError("tags", "Each tag cannot exceed 50 characters"))
            }
        }
        
        // Reminder date validation
        request.reminderDate?.let { reminderDate ->
            try {
                // Basic ISO 8601 format validation
                java.time.Instant.parse(reminderDate)
            } catch (e: Exception) {
                errors.add(ValidationError("reminder_date", "Reminder date must be in ISO 8601 format"))
            }
        }
        
        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errors)
        }
    }
    
    private fun validateSavedJobsSearchRequest(request: SavedJobsSearchRequest): ValidationResult {
        val errors = mutableListOf<ValidationError>()
        
        // Query validation
        request.query?.let { query ->
            if (query.length > 200) {
                errors.add(ValidationError("query", "Search query cannot exceed 200 characters"))
            }
        }
        
        // Tags validation
        request.tags?.let { tags ->
            if (tags.size > 20) {
                errors.add(ValidationError("tags", "Maximum 20 tags allowed"))
            }
            
            if (tags.any { it.isBlank() }) {
                errors.add(ValidationError("tags", "Tags cannot be empty"))
            }
        }
        
        // Company name validation
        request.companyName?.let { companyName ->
            if (companyName.length > 100) {
                errors.add(ValidationError("company_name", "Company name cannot exceed 100 characters"))
            }
        }
        
        // Location validation
        request.location?.let { location ->
            if (location.length > 100) {
                errors.add(ValidationError("location", "Location cannot exceed 100 characters"))
            }
        }
        
        // Date validation
        request.savedAfter?.let { savedAfter ->
            try {
                java.time.Instant.parse(savedAfter)
            } catch (e: Exception) {
                errors.add(ValidationError("saved_after", "Saved after date must be in ISO 8601 format"))
            }
        }
        
        request.savedBefore?.let { savedBefore ->
            try {
                java.time.Instant.parse(savedBefore)
            } catch (e: Exception) {
                errors.add(ValidationError("saved_before", "Saved before date must be in ISO 8601 format"))
            }
        }
        
        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errors)
        }
    }
    
    private fun validateSavedJobsBulkRequest(request: SavedJobsBulkRequest): ValidationResult {
        val errors = mutableListOf<ValidationError>()
        
        // Saved job IDs validation
        if (request.savedJobIds.isEmpty()) {
            errors.add(ValidationError("saved_job_ids", "At least one saved job ID is required"))
        }
        
        if (request.savedJobIds.size > 100) {
            errors.add(ValidationError("saved_job_ids", "Maximum 100 saved job IDs allowed"))
        }
        
        if (request.savedJobIds.any { it.isBlank() }) {
            errors.add(ValidationError("saved_job_ids", "Saved job IDs cannot be empty"))
        }
        
        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errors)
        }
    }
    
    private fun validateSavedJobsBulkUpdateRequest(request: SavedJobsBulkUpdateRequest): ValidationResult {
        val errors = mutableListOf<ValidationError>()
        
        // Saved job IDs validation
        if (request.savedJobIds.isEmpty()) {
            errors.add(ValidationError("saved_job_ids", "At least one saved job ID is required"))
        }
        
        if (request.savedJobIds.size > 100) {
            errors.add(ValidationError("saved_job_ids", "Maximum 100 saved job IDs allowed"))
        }
        
        if (request.savedJobIds.any { it.isBlank() }) {
            errors.add(ValidationError("saved_job_ids", "Saved job IDs cannot be empty"))
        }
        
        // Tags validation
        request.tags?.let { tags ->
            if (tags.size > 20) {
                errors.add(ValidationError("tags", "Maximum 20 tags allowed"))
            }
            
            if (tags.any { it.isBlank() }) {
                errors.add(ValidationError("tags", "Tags cannot be empty"))
            }
            
            if (tags.any { it.length > 50 }) {
                errors.add(ValidationError("tags", "Each tag cannot exceed 50 characters"))
            }
        }
        
        // Reminder date validation
        request.reminderDate?.let { reminderDate ->
            try {
                java.time.Instant.parse(reminderDate)
            } catch (e: Exception) {
                errors.add(ValidationError("reminder_date", "Reminder date must be in ISO 8601 format"))
            }
        }
        
        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errors)
        }
    }
}
