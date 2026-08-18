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
 * Repository class for handling analytics operations
 */
class AnalyticsRepository {
    
    private val apiService = NetworkModule.apiService
    
    // ===== JOB SEARCH ANALYTICS =====
    
    /**
     * Track job search analytics
     */
    suspend fun trackJobSearch(request: JobSearchAnalytics): Result<Unit> {
        // Validate request before making API call
        val validationResult = validateJobSearchAnalytics(request)
        if (!validationResult.isValid()) {
            val errors = validationResult.getErrors().joinToString(", ") { "${it.field}: ${it.message}" }
            return Result.failure(Exception("Validation failed: $errors"))
        }
        
        return safeApiCall(
            apiCall = { apiService.trackJobSearch(request) },
            errorHandler = { ApiErrorHandler.logError("AnalyticsRepository", it, "Track Job Search") }
        )
    }
    
    // ===== CV DOWNLOAD ANALYTICS =====
    
    /**
     * Track CV download analytics
     */
    suspend fun trackCVDownload(request: CVDownloadAnalytics): Result<Unit> {
        // Validate request before making API call
        val validationResult = validateCVDownloadAnalytics(request)
        if (!validationResult.isValid()) {
            val errors = validationResult.getErrors().joinToString(", ") { "${it.field}: ${it.message}" }
            return Result.failure(Exception("Validation failed: $errors"))
        }
        
        return safeApiCall(
            apiCall = { apiService.trackCVDownload(request) },
            errorHandler = { ApiErrorHandler.logError("AnalyticsRepository", it, "Track CV Download") }
        )
    }
    
    // ===== APPLICATION STATUS CHANGE ANALYTICS =====
    
    /**
     * Track application status change
     */
    suspend fun trackApplicationStatusChange(request: ApplicationStatusHistory): Result<Unit> {
        // Validate request before making API call
        val validationResult = validateApplicationStatusHistory(request)
        if (!validationResult.isValid()) {
            val errors = validationResult.getErrors().joinToString(", ") { "${it.field}: ${it.message}" }
            return Result.failure(Exception("Validation failed: $errors"))
        }
        
        return safeApiCall(
            apiCall = { apiService.trackApplicationStatusChange(request) },
            errorHandler = { ApiErrorHandler.logError("AnalyticsRepository", it, "Track Application Status Change") }
        )
    }
    
    // ===== HELPER METHODS =====
    
    /**
     * Track job search with custom error handling
     */
    suspend fun trackJobSearchWithErrorHandling(
        request: JobSearchAnalytics,
        onError: (String) -> Unit
    ): Result<Unit> {
        return trackJobSearch(request).also { result ->
            result.onFailure { throwable ->
                val errorMessage = ApiErrorHandler.getErrorMessage(throwable)
                onError(errorMessage)
            }
        }
    }
    
    /**
     * Track CV download with custom error handling
     */
    suspend fun trackCVDownloadWithErrorHandling(
        request: CVDownloadAnalytics,
        onError: (String) -> Unit
    ): Result<Unit> {
        return trackCVDownload(request).also { result ->
            result.onFailure { throwable ->
                val errorMessage = ApiErrorHandler.getErrorMessage(throwable)
                onError(errorMessage)
            }
        }
    }
    
    /**
     * Track application status change with custom error handling
     */
    suspend fun trackApplicationStatusChangeWithErrorHandling(
        request: ApplicationStatusHistory,
        onError: (String) -> Unit
    ): Result<Unit> {
        return trackApplicationStatusChange(request).also { result ->
            result.onFailure { throwable ->
                val errorMessage = ApiErrorHandler.getErrorMessage(throwable)
                onError(errorMessage)
            }
        }
    }
    
    // ===== VALIDATION METHODS =====
    
    private fun validateJobSearchAnalytics(request: JobSearchAnalytics): ValidationResult {
        val errors = mutableListOf<ValidationError>()
        
        // Validate user UID
        if (request.userUid.isBlank()) {
            errors.add(ValidationError("user_uid", "User UID is required"))
        }
        
        // Validate search query length
        request.searchQuery?.let { query ->
            if (query.length > 200) {
                errors.add(ValidationError("search_query", "Search query cannot exceed 200 characters"))
            }
        }
        
        // Validate results count
        if (request.resultsCount < 0) {
            errors.add(ValidationError("results_count", "Results count cannot be negative"))
        }
        
        // Validate search timestamp
        if (request.searchTimestamp.isBlank()) {
            errors.add(ValidationError("search_timestamp", "Search timestamp is required"))
        }
        
        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errors)
        }
    }
    
    private fun validateCVDownloadAnalytics(request: CVDownloadAnalytics): ValidationResult {
        val errors = mutableListOf<ValidationError>()
        
        // Validate CV ID
        if (request.cvId.isBlank()) {
            errors.add(ValidationError("cv_id", "CV ID is required"))
        }
        
        // Validate user UID
        if (request.userUid.isBlank()) {
            errors.add(ValidationError("user_uid", "User UID is required"))
        }
        
        // Validate download timestamp
        if (request.downloadTimestamp.isBlank()) {
            errors.add(ValidationError("download_timestamp", "Download timestamp is required"))
        }
        
        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errors)
        }
    }
    
    private fun validateApplicationStatusHistory(request: ApplicationStatusHistory): ValidationResult {
        val errors = mutableListOf<ValidationError>()
        
        // Validate application ID
        if (request.applicationId.isBlank()) {
            errors.add(ValidationError("application_id", "Application ID is required"))
        }
        
        // Validate new status
        if (request.newStatus.isBlank()) {
            errors.add(ValidationError("new_status", "New status is required"))
        }
        
        // Validate change timestamp
        if (request.changedAt.isBlank()) {
            errors.add(ValidationError("changed_at", "Changed at timestamp is required"))
        }
        
        // Validate change reason length
        request.changeReason?.let { reason ->
            if (reason.length > 500) {
                errors.add(ValidationError("change_reason", "Change reason cannot exceed 500 characters"))
            }
        }
        
        // Validate notes length
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
