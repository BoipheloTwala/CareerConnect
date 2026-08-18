//CODE ATTRIBUTION
//01
//Kotlin coroutines (suspend functions)
//Adapted from: Kotlin Docs. (2025). Coroutines basics. [online]
//Available at: https://kotlinlang.org/docs/coroutines-basics.html
//Date Accessed: 01 October 2025

//02
//Kotlin Result
//Adapted from: Kotlin Stdlib. (2025). Result. [online]
//Available at: https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-result/
//Date Accessed: 01 October 2025

//03
//Kotlin collections (List)
//Adapted from: Kotlin Docs. (2025). Collections overview. [online]
//Available at: https://kotlinlang.org/docs/collections-overview.html
//Date Accessed: 01 October 2025

//04
//Gson
//Adapted from: Gson Javadoc. (2025). Gson. [online]
//Available at: https://javadoc.io/doc/com.google.code.gson/gson/latest/com/google/gson/Gson.html
//Date Accessed: 01 October 2025

//05
//Gson JsonElement
//Adapted from: Gson Javadoc. (2025). JsonElement. [online]
//Available at: https://javadoc.io/doc/com.google.code.gson/gson/latest/com/google/gson/JsonElement.html
//Date Accessed: 01 October 2025

//06
//Gson TypeToken (generic list parsing)
//Adapted from: Gson Javadoc. (2025). TypeToken. [online]
//Available at: https://javadoc.io/doc/com.google.code.gson/gson/latest/com/google/gson/reflect/TypeToken.html
//Date Accessed: 01 October 2025

//07
//java.time.Instant.parse (ISO 8601 validation)
//Adapted from: Oracle Docs. (2025). Instant#parse. [online]
//Available at: https://docs.oracle.com/javase/8/docs/api/java/time/Instant.html#parse-java.lang.CharSequence-
//Date Accessed: 01 October 2025

package vcmsa.projects.careerconnect.data.repository

import vcmsa.projects.careerconnect.data.network.ApiErrorHandler
import vcmsa.projects.careerconnect.data.network.NetworkModule
import vcmsa.projects.careerconnect.data.network.safeApiCall
import vcmsa.projects.careerconnect.domain.model.*

/**
 * Repository class for handling application tracking operations
 */
class ApplicationTrackingRepository {
    
    private val apiService = NetworkModule.apiService
    
    // ===== APPLICATION TRACKING OPERATIONS =====
    
    /**
     * Create application tracking
     */
    suspend fun createApplicationTracking(request: CreateApplicationTrackingRequest): Result<ApplicationTracking> {
        // Validate request before making API call
        val validationResult = validateCreateApplicationTrackingRequest(request)
        if (!validationResult.isValid()) {
            val errors = validationResult.getErrors().joinToString(", ") { "${it.field}: ${it.message}" }
            return Result.failure(Exception("Validation failed: $errors"))
        }
        
        return safeApiCall(
            apiCall = { apiService.createApplicationTracking(request) },
            errorHandler = { ApiErrorHandler.logError("ApplicationTrackingRepository", it, "Create Application Tracking") }
        )
    }
    
    /**
     * Get user's application tracking
     */
    suspend fun getApplicationTracking(page: Int = 1, limit: Int = 20): Result<PaginatedResponse<ApplicationTracking>> {
        if (page < 1) {
            return Result.failure(Exception("Page must be greater than 0"))
        }
        
        if (limit < 1 || limit > 100) {
            return Result.failure(Exception("Limit must be between 1 and 100"))
        }
        
        return safeApiCall(
            apiCall = { apiService.getApplicationTracking(page, limit) },
            errorHandler = { ApiErrorHandler.logError("ApplicationTrackingRepository", it, "Get Application Tracking") }
        )
    }
    
    /**
     * Search application tracking
     */
    suspend fun searchApplicationTracking(request: ApplicationTrackingSearchRequest): Result<ApplicationTrackingSearchResponse> {
        // Validate request before making API call
        val validationResult = validateApplicationTrackingSearchRequest(request)
        if (!validationResult.isValid()) {
            val errors = validationResult.getErrors().joinToString(", ") { "${it.field}: ${it.message}" }
            return Result.failure(Exception("Validation failed: $errors"))
        }
        
        return safeApiCall(
            apiCall = { apiService.searchApplicationTracking(request) },
            errorHandler = { ApiErrorHandler.logError("ApplicationTrackingRepository", it, "Search Application Tracking") }
        )
    }
    
    /**
     * Get application tracking details
     */
    suspend fun getApplicationTrackingDetails(trackingId: String): Result<ApplicationTracking> {
        if (trackingId.isBlank()) {
            return Result.failure(Exception("Tracking ID cannot be empty"))
        }
        
        return safeApiCall(
            apiCall = { apiService.getApplicationTrackingDetails(trackingId) },
            errorHandler = { ApiErrorHandler.logError("ApplicationTrackingRepository", it, "Get Application Tracking Details") }
        )
    }
    
    /**
     * Update application tracking
     */
    suspend fun updateApplicationTracking(trackingId: String, request: UpdateApplicationTrackingRequest): Result<ApplicationTracking> {
        if (trackingId.isBlank()) {
            return Result.failure(Exception("Tracking ID cannot be empty"))
        }
        
        // Validate request before making API call
        val validationResult = validateUpdateApplicationTrackingRequest(request)
        if (!validationResult.isValid()) {
            val errors = validationResult.getErrors().joinToString(", ") { "${it.field}: ${it.message}" }
            return Result.failure(Exception("Validation failed: $errors"))
        }
        
        return safeApiCall(
            apiCall = { apiService.updateApplicationTracking(trackingId, request) },
            errorHandler = { ApiErrorHandler.logError("ApplicationTrackingRepository", it, "Update Application Tracking") }
        )
    }
    
    /**
     * Delete application tracking
     */
    suspend fun deleteApplicationTracking(trackingId: String): Result<Unit> {
        if (trackingId.isBlank()) {
            return Result.failure(Exception("Tracking ID cannot be empty"))
        }
        
        return safeApiCall(
            apiCall = { apiService.deleteApplicationTracking(trackingId) },
            errorHandler = { ApiErrorHandler.logError("ApplicationTrackingRepository", it, "Delete Application Tracking") }
        )
    }
    
    /**
     * Get application tracking by application ID
     */
    suspend fun getApplicationTrackingByApplicationId(applicationId: String): Result<ApplicationTracking> {
        if (applicationId.isBlank()) {
            return Result.failure(Exception("Application ID cannot be empty"))
        }
        
        return safeApiCall(
            apiCall = { apiService.getApplicationTrackingByApplicationId(applicationId) },
            errorHandler = { ApiErrorHandler.logError("ApplicationTrackingRepository", it, "Get Application Tracking by Application ID") }
        )
    }
    
    /**
     * Bulk operations on application tracking
     */
    suspend fun bulkApplicationTrackingOperation(request: ApplicationTrackingBulkRequest): Result<Unit> {
        // Validate request before making API call
        val validationResult = validateApplicationTrackingBulkRequest(request)
        if (!validationResult.isValid()) {
            val errors = validationResult.getErrors().joinToString(", ") { "${it.field}: ${it.message}" }
            return Result.failure(Exception("Validation failed: $errors"))
        }
        
        return safeApiCall(
            apiCall = { apiService.bulkApplicationTrackingOperation(request) },
            errorHandler = { ApiErrorHandler.logError("ApplicationTrackingRepository", it, "Bulk Application Tracking Operation") }
        )
    }
    
    /**
     * Bulk update application tracking
     */
    suspend fun bulkUpdateApplicationTracking(request: ApplicationTrackingBulkUpdateRequest): Result<Unit> {
        // Validate request before making API call
        val validationResult = validateApplicationTrackingBulkUpdateRequest(request)
        if (!validationResult.isValid()) {
            val errors = validationResult.getErrors().joinToString(", ") { "${it.field}: ${it.message}" }
            return Result.failure(Exception("Validation failed: $errors"))
        }
        
        return safeApiCall(
            apiCall = { apiService.bulkUpdateApplicationTracking(request) },
            errorHandler = { ApiErrorHandler.logError("ApplicationTrackingRepository", it, "Bulk Update Application Tracking") }
        )
    }
    
    /**
     * Get application tracking analytics
     */
    suspend fun getApplicationTrackingAnalytics(): Result<ApplicationTrackingAnalytics> {
        return safeApiCall(
            apiCall = { apiService.getApplicationTrackingAnalytics() },
            errorHandler = { ApiErrorHandler.logError("ApplicationTrackingRepository", it, "Get Application Tracking Analytics") }
        )
    }
    
    /**
     * Get application tracking dashboard
     */
    suspend fun getApplicationTrackingDashboard(): Result<ApplicationTrackingSearchResponse> {
        return safeApiCall(
            apiCall = { apiService.getApplicationTrackingDashboard() },
            errorHandler = { ApiErrorHandler.logError("ApplicationTrackingRepository", it, "Get Application Tracking Dashboard") }
        )
    }
    
    /**
     * Get follow-up reminders
     */
    suspend fun getFollowUpReminders(): Result<List<ApplicationTracking>> {
        return safeApiCall(
            apiCall = { apiService.getFollowUpReminders() },
            errorHandler = { ApiErrorHandler.logError("ApplicationTrackingRepository", it, "Get Follow-up Reminders") }
        )
    }
    
    /**
     * Get upcoming interviews
     */
    suspend fun getUpcomingInterviews(): Result<List<ApplicationTracking>> {
        return safeApiCall(
            apiCall = { apiService.getUpcomingInterviews() },
            errorHandler = { ApiErrorHandler.logError("ApplicationTrackingRepository", it, "Get Upcoming Interviews") }
        )
    }
    
    // ===== HELPER METHODS =====
    
    /**
     * Create application tracking with custom error handling
     */
    suspend fun createApplicationTrackingWithErrorHandling(
        request: CreateApplicationTrackingRequest,
        onError: (String) -> Unit
    ): Result<ApplicationTracking> {
        return createApplicationTracking(request).also { result ->
            result.onFailure { throwable ->
                val errorMessage = ApiErrorHandler.getErrorMessage(throwable)
                onError(errorMessage)
            }
        }
    }
    
    /**
     * Search application tracking with custom error handling
     */
    suspend fun searchApplicationTrackingWithErrorHandling(
        request: ApplicationTrackingSearchRequest,
        onError: (String) -> Unit
    ): Result<ApplicationTrackingSearchResponse> {
        return searchApplicationTracking(request).also { result ->
            result.onFailure { throwable ->
                val errorMessage = ApiErrorHandler.getErrorMessage(throwable)
                onError(errorMessage)
            }
        }
    }
    
    /**
     * Get application tracking as NetworkResult for UI state management
     */
    suspend fun getApplicationTrackingAsNetworkResult(page: Int = 1, limit: Int = 20): NetworkResult<PaginatedResponse<ApplicationTracking>> {
        return getApplicationTracking(page, limit).toNetworkResult()
    }
    
    /**
     * Search application tracking as NetworkResult for UI state management
     */
    suspend fun searchApplicationTrackingAsNetworkResult(request: ApplicationTrackingSearchRequest): NetworkResult<ApplicationTrackingSearchResponse> {
        return searchApplicationTracking(request).toNetworkResult()
    }
    
    // ===== VALIDATION METHODS =====
    
    private fun validateCreateApplicationTrackingRequest(request: CreateApplicationTrackingRequest): ValidationResult {
        val errors = mutableListOf<ValidationError>()
        
        // Required fields validation
        if (request.applicationId.isBlank()) {
            errors.add(ValidationError("application_id", "Application ID is required"))
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
        
        // Follow-up date validation
        request.followUpDate?.let { followUpDate ->
            try {
                // Basic ISO 8601 format validation
                java.time.Instant.parse(followUpDate)
            } catch (e: Exception) {
                errors.add(ValidationError("follow_up_date", "Follow-up date must be in ISO 8601 format"))
            }
        }
        
        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errors)
        }
    }
    
    private fun validateUpdateApplicationTrackingRequest(request: UpdateApplicationTrackingRequest): ValidationResult {
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
        
        // Follow-up date validation
        request.followUpDate?.let { followUpDate ->
            try {
                java.time.Instant.parse(followUpDate)
            } catch (e: Exception) {
                errors.add(ValidationError("follow_up_date", "Follow-up date must be in ISO 8601 format"))
            }
        }
        
        // Interview date validation
        request.interviewScheduledAt?.let { interviewDate ->
            try {
                java.time.Instant.parse(interviewDate)
            } catch (e: Exception) {
                errors.add(ValidationError("interview_scheduled_at", "Interview date must be in ISO 8601 format"))
            }
        }
        
        // Interview location validation
        request.interviewLocation?.let { location ->
            if (location.length > 200) {
                errors.add(ValidationError("interview_location", "Interview location cannot exceed 200 characters"))
            }
        }
        
        // Interview notes validation
        request.interviewNotes?.let { notes ->
            if (notes.length > 2000) {
                errors.add(ValidationError("interview_notes", "Interview notes cannot exceed 2000 characters"))
            }
        }
        
        // Salary validation
        request.salaryDiscussed?.let { salary ->
            if (salary < 0) {
                errors.add(ValidationError("salary_discussed", "Salary cannot be negative"))
            }
        }
        
        // Currency validation
        request.salaryCurrency?.let { currency ->
            if (currency.length != 3) {
                errors.add(ValidationError("salary_currency", "Currency must be a 3-letter code (e.g., USD, EUR)"))
            }
        }
        
        // Feedback validation
        request.feedbackReceived?.let { feedback ->
            if (feedback.length > 2000) {
                errors.add(ValidationError("feedback_received", "Feedback cannot exceed 2000 characters"))
            }
        }
        
        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errors)
        }
    }
    
    private fun validateApplicationTrackingSearchRequest(request: ApplicationTrackingSearchRequest): ValidationResult {
        val errors = mutableListOf<ValidationError>()
        
        // Query validation
        request.query?.let { query ->
            if (query.length > 200) {
                errors.add(ValidationError("query", "Search query cannot exceed 200 characters"))
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
        request.appliedAfter?.let { appliedAfter ->
            try {
                java.time.Instant.parse(appliedAfter)
            } catch (e: Exception) {
                errors.add(ValidationError("applied_after", "Applied after date must be in ISO 8601 format"))
            }
        }
        
        request.appliedBefore?.let { appliedBefore ->
            try {
                java.time.Instant.parse(appliedBefore)
            } catch (e: Exception) {
                errors.add(ValidationError("applied_before", "Applied before date must be in ISO 8601 format"))
            }
        }
        
        request.followUpAfter?.let { followUpAfter ->
            try {
                java.time.Instant.parse(followUpAfter)
            } catch (e: Exception) {
                errors.add(ValidationError("follow_up_after", "Follow-up after date must be in ISO 8601 format"))
            }
        }
        
        request.followUpBefore?.let { followUpBefore ->
            try {
                java.time.Instant.parse(followUpBefore)
            } catch (e: Exception) {
                errors.add(ValidationError("follow_up_before", "Follow-up before date must be in ISO 8601 format"))
            }
        }
        
        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errors)
        }
    }
    
    private fun validateApplicationTrackingBulkRequest(request: ApplicationTrackingBulkRequest): ValidationResult {
        val errors = mutableListOf<ValidationError>()
        
        // Application IDs validation
        if (request.applicationIds.isEmpty()) {
            errors.add(ValidationError("application_ids", "At least one application ID is required"))
        }
        
        if (request.applicationIds.size > 100) {
            errors.add(ValidationError("application_ids", "Maximum 100 application IDs allowed"))
        }
        
        if (request.applicationIds.any { it.isBlank() }) {
            errors.add(ValidationError("application_ids", "Application IDs cannot be empty"))
        }
        
        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errors)
        }
    }
    
    private fun validateApplicationTrackingBulkUpdateRequest(request: ApplicationTrackingBulkUpdateRequest): ValidationResult {
        val errors = mutableListOf<ValidationError>()
        
        // Application IDs validation
        if (request.applicationIds.isEmpty()) {
            errors.add(ValidationError("application_ids", "At least one application ID is required"))
        }
        
        if (request.applicationIds.size > 100) {
            errors.add(ValidationError("application_ids", "Maximum 100 application IDs allowed"))
        }
        
        if (request.applicationIds.any { it.isBlank() }) {
            errors.add(ValidationError("application_ids", "Application IDs cannot be empty"))
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
        
        // Follow-up date validation
        request.followUpDate?.let { followUpDate ->
            try {
                java.time.Instant.parse(followUpDate)
            } catch (e: Exception) {
                errors.add(ValidationError("follow_up_date", "Follow-up date must be in ISO 8601 format"))
            }
        }
        
        // Notes validation
        request.notes?.let { notes ->
            if (notes.length > 1000) {
                errors.add(ValidationError("notes", "Notes cannot exceed 1000 characters"))
            }
        }
        
        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errors)
        }
    }
}
