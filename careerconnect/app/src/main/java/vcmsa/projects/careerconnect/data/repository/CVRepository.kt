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
 * Repository class for handling CV upload and management operations
 */
class CVRepository {
    
    private val apiService = NetworkModule.apiService
    
    // ===== CV UPLOAD AND MANAGEMENT OPERATIONS =====
    
    /**
     * Upload a new CV
     */
    suspend fun uploadCV(request: CVCreateRequest): Result<CV> {
        // Validate using legacy validator by mapping to CVUploadRequest shape
        val inferredType = when (request.fileName.substringAfterLast('.', "").lowercase()) {
            "pdf" -> CVFileType.PDF
            "doc" -> CVFileType.DOC
            "docx" -> CVFileType.DOCX
            "txt" -> CVFileType.TXT
            else -> CVFileType.PDF
        }
        val validationProxy = CVUploadRequest(
            title = request.fileName,
            description = null,
            fileType = inferredType,
            fileSize = request.fileSize ?: 1L,
            fileUrl = request.fileUrl,
            thumbnailUrl = null,
            isPrimary = request.isPrimary,
            tags = emptyList()
        )
        val validationResult = ProfileValidator.validateCVUploadRequest(validationProxy)
        if (!validationResult.isValid()) {
            val errors = validationResult.getErrors().joinToString(", ") { "${it.field}: ${it.message}" }
            return Result.failure(Exception("Validation failed: $errors"))
        }
        
        return safeApiCall(
            apiCall = { apiService.uploadCV(request) },
            errorHandler = { ApiErrorHandler.logError("CVRepository", it, "Upload CV") }
        )
    }
    
    /**
     * Get user's CVs with pagination
     */
    suspend fun getMyCVs(page: Int = 1, limit: Int = 20, status: CVStatus? = null, fileType: CVFileType? = null): Result<PaginatedResponse<CV>> {
        if (page < 1) {
            return Result.failure(Exception("Page must be greater than 0"))
        }
        
        if (limit < 1 || limit > 100) {
            return Result.failure(Exception("Limit must be between 1 and 100"))
        }
        
        return safeApiCall(
            apiCall = { apiService.getMyCVs(page, limit, status, fileType) },
            errorHandler = { ApiErrorHandler.logError("CVRepository", it, "Get My CVs") }
        )
    }
    
    /**
     * Get CV details by ID
     */
    suspend fun getCVDetails(cvId: String): Result<CV> {
        if (cvId.isBlank()) {
            return Result.failure(Exception("CV ID cannot be empty"))
        }
        
        return safeApiCall(
            apiCall = { apiService.getCVDetails(cvId) },
            errorHandler = { ApiErrorHandler.logError("CVRepository", it, "Get CV Details") }
        )
    }
    
    /**
     * Update CV details
     */
    suspend fun updateCV(cvId: String, request: CVUpdateRequest): Result<CV> {
        if (cvId.isBlank()) {
            return Result.failure(Exception("CV ID cannot be empty"))
        }
        
        // Validate request before making API call
        val validationResult = ProfileValidator.validateCVUpdateRequest(request)
        if (!validationResult.isValid()) {
            val errors = validationResult.getErrors().joinToString(", ") { "${it.field}: ${it.message}" }
            return Result.failure(Exception("Validation failed: $errors"))
        }
        
        return safeApiCall(
            apiCall = { apiService.updateCV(cvId, request) },
            errorHandler = { ApiErrorHandler.logError("CVRepository", it, "Update CV") }
        )
    }
    
    /**
     * Delete CV
     */
    suspend fun deleteCV(cvId: String): Result<Unit> {
        if (cvId.isBlank()) {
            return Result.failure(Exception("CV ID cannot be empty"))
        }
        
        return safeApiCall(
            apiCall = { apiService.deleteCV(cvId) },
            errorHandler = { ApiErrorHandler.logError("CVRepository", it, "Delete CV") }
        )
    }
    
    /**
     * Set primary CV
     */
    suspend fun setPrimaryCV(cvId: String): Result<CV> {
        if (cvId.isBlank()) {
            return Result.failure(Exception("CV ID cannot be empty"))
        }
        
        return safeApiCall(
            apiCall = { apiService.setPrimaryCV(cvId) },
            errorHandler = { ApiErrorHandler.logError("CVRepository", it, "Set Primary CV") }
        )
    }
    
    /**
     * Get primary CV
     */
    suspend fun getPrimaryCV(): Result<CV> {
        return safeApiCall(
            apiCall = { apiService.getPrimaryCV() },
            errorHandler = { ApiErrorHandler.logError("CVRepository", it, "Get Primary CV") }
        )
    }
    
    /**
     * Search CVs
     */
    suspend fun searchCVs(request: CVSearchRequest): Result<PaginatedResponse<CV>> {
        // Validate request before making API call
        val validationResult = ProfileValidator.validateCVSearchRequest(request)
        if (!validationResult.isValid()) {
            val errors = validationResult.getErrors().joinToString(", ") { "${it.field}: ${it.message}" }
            return Result.failure(Exception("Validation failed: $errors"))
        }
        
        return safeApiCall(
            apiCall = { apiService.searchCVs(request) },
            errorHandler = { ApiErrorHandler.logError("CVRepository", it, "Search CVs") }
        )
    }
    
    /**
     * Get CV analytics
     */
    suspend fun getCVAnalytics(): Result<CVAnalytics> {
        return safeApiCall(
            apiCall = { apiService.getCVAnalytics() },
            errorHandler = { ApiErrorHandler.logError("CVRepository", it, "Get CV Analytics") }
        )
    }
    
    /**
     * Get CV storage usage
     */
    suspend fun getCVStorageUsage(): Result<StorageUsage> {
        return safeApiCall(
            apiCall = { apiService.getCVStorageUsage() },
            errorHandler = { ApiErrorHandler.logError("CVRepository", it, "Get CV Storage Usage") }
        )
    }
    
    // ===== CV SHARING OPERATIONS =====
    
    /**
     * Share CV via email
     */
    suspend fun shareCV(cvId: String, request: CreateCVShareRequest): Result<CVShareResponse> {
        if (cvId.isBlank()) {
            return Result.failure(Exception("CV ID cannot be empty"))
        }
        
        // Validate request before making API call
        val validationResult = validateCVShareRequest(request)
        if (!validationResult.isValid()) {
            val errors = validationResult.getErrors().joinToString(", ") { "${it.field}: ${it.message}" }
            return Result.failure(Exception("Validation failed: $errors"))
        }
        
        return safeApiCall(
            apiCall = { apiService.shareCV(cvId, request) },
            errorHandler = { ApiErrorHandler.logError("CVRepository", it, "Share CV") }
        )
    }
    
    /**
     * Get shared CV by token
     */
    suspend fun getSharedCV(shareToken: String): Result<CV> {
        if (shareToken.isBlank()) {
            return Result.failure(Exception("Share token cannot be empty"))
        }
        
        return safeApiCall(
            apiCall = { apiService.getSharedCV(shareToken) },
            errorHandler = { ApiErrorHandler.logError("CVRepository", it, "Get Shared CV") }
        )
    }
    
    /**
     * Revoke CV share
     */
    suspend fun revokeCVShare(cvId: String, shareToken: String): Result<Unit> {
        if (cvId.isBlank()) {
            return Result.failure(Exception("CV ID cannot be empty"))
        }
        
        if (shareToken.isBlank()) {
            return Result.failure(Exception("Share token cannot be empty"))
        }
        
        return safeApiCall(
            apiCall = { apiService.revokeCVShare(cvId, shareToken) },
            errorHandler = { ApiErrorHandler.logError("CVRepository", it, "Revoke CV Share") }
        )
    }
    
    // ===== CV DOWNLOAD OPERATIONS =====
    
    /**
     * Get CV download URL
     */
    suspend fun getCVDownloadUrl(cvId: String, request: CVDownloadRequest): Result<CVDownloadResponse> {
        if (cvId.isBlank()) {
            return Result.failure(Exception("CV ID cannot be empty"))
        }
        
        // Validate request before making API call
        val validationResult = ProfileValidator.validateCVDownloadRequest(request)
        if (!validationResult.isValid()) {
            val errors = validationResult.getErrors().joinToString(", ") { "${it.field}: ${it.message}" }
            return Result.failure(Exception("Validation failed: $errors"))
        }
        
        return safeApiCall(
            apiCall = { apiService.getCVDownloadUrl(cvId, request) },
            errorHandler = { ApiErrorHandler.logError("CVRepository", it, "Get CV Download URL") }
        )
    }
    
    /**
     * Download CV file
     */
    suspend fun downloadCVFile(cvId: String, downloadType: DownloadType = DownloadType.ORIGINAL): Result<Unit> {
        if (cvId.isBlank()) {
            return Result.failure(Exception("CV ID cannot be empty"))
        }
        
        return safeApiCall(
            apiCall = { apiService.downloadCVFile(cvId, downloadType) },
            errorHandler = { ApiErrorHandler.logError("CVRepository", it, "Download CV File") }
        )
    }
    
    // ===== CV UPLOAD HELPERS =====
    
    /**
     * Get CV upload URL for direct upload to Cloudinary
     */
    suspend fun getCVUploadUrl(request: CVUploadUrlRequest): Result<CVUploadUrlResponse> {
        // Validate request before making API call
        val validationResult = validateCVUploadUrlRequest(request)
        if (!validationResult.isValid()) {
            val errors = validationResult.getErrors().joinToString(", ") { "${it.field}: ${it.message}" }
            return Result.failure(Exception("Validation failed: $errors"))
        }
        
        return safeApiCall(
            apiCall = { apiService.getCVUploadUrl(request) },
            errorHandler = { ApiErrorHandler.logError("CVRepository", it, "Get CV Upload URL") }
        )
    }
    
    /**
     * Confirm CV upload after direct upload
     */
    suspend fun confirmCVUpload(cvId: String, request: CVUploadConfirmationRequest): Result<CV> {
        if (cvId.isBlank()) {
            return Result.failure(Exception("CV ID cannot be empty"))
        }
        
        return safeApiCall(
            apiCall = { apiService.confirmCVUpload(cvId, request) },
            errorHandler = { ApiErrorHandler.logError("CVRepository", it, "Confirm CV Upload") }
        )
    }
    
    // ===== CV BULK OPERATIONS =====
    
    /**
     * Bulk delete CVs
     */
    suspend fun bulkDeleteCVs(request: CVBulkDeleteRequest): Result<CVBulkDeleteResponse> {
        // Validate request before making API call
        val validationResult = validateCVBulkDeleteRequest(request)
        if (!validationResult.isValid()) {
            val errors = validationResult.getErrors().joinToString(", ") { "${it.field}: ${it.message}" }
            return Result.failure(Exception("Validation failed: $errors"))
        }
        
        return safeApiCall(
            apiCall = { apiService.bulkDeleteCVs(request) },
            errorHandler = { ApiErrorHandler.logError("CVRepository", it, "Bulk Delete CVs") }
        )
    }
    
    /**
     * Bulk update CV status
     */
    suspend fun bulkUpdateCVStatus(request: CVBulkStatusUpdateRequest): Result<CVBulkStatusUpdateResponse> {
        // Validate request before making API call
        val validationResult = validateCVBulkStatusUpdateRequest(request)
        if (!validationResult.isValid()) {
            val errors = validationResult.getErrors().joinToString(", ") { "${it.field}: ${it.message}" }
            return Result.failure(Exception("Validation failed: $errors"))
        }
        
        return safeApiCall(
            apiCall = { apiService.bulkUpdateCVStatus(request) },
            errorHandler = { ApiErrorHandler.logError("CVRepository", it, "Bulk Update CV Status") }
        )
    }
    
    /**
     * Export CVs data
     */
    suspend fun exportCVsData(format: ExportFormat = ExportFormat.JSON): Result<Unit> {
        return safeApiCall(
            apiCall = { apiService.exportCVsData(format) },
            errorHandler = { ApiErrorHandler.logError("CVRepository", it, "Export CVs Data") }
        )
    }
    
    // ===== CONVENIENCE METHODS =====
    
    /**
     * Get CVs with NetworkResult for UI state management
     */
    suspend fun getMyCVsAsNetworkResult(page: Int = 1, limit: Int = 20, status: CVStatus? = null, fileType: CVFileType? = null): NetworkResult<PaginatedResponse<CV>> {
        return getMyCVs(page, limit, status, fileType).toNetworkResult()
    }
    
    /**
     * Search CVs with NetworkResult for UI state management
     */
    suspend fun searchCVsAsNetworkResult(request: CVSearchRequest): NetworkResult<PaginatedResponse<CV>> {
        return searchCVs(request).toNetworkResult()
    }
    
    /**
     * Upload CV with custom error handling
     */
    suspend fun uploadCVWithErrorHandling(
        request: CVCreateRequest,
        onError: (String) -> Unit
    ): Result<CV> {
        return uploadCV(request).also { result ->
            result.onFailure { throwable ->
                val errorMessage = ApiErrorHandler.getErrorMessage(throwable)
                onError(errorMessage)
            }
        }
    }
    
    /**
     * Update CV with custom error handling
     */
    suspend fun updateCVWithErrorHandling(
        cvId: String,
        request: CVUpdateRequest,
        onError: (String) -> Unit
    ): Result<CV> {
        return updateCV(cvId, request).also { result ->
            result.onFailure { throwable ->
                val errorMessage = ApiErrorHandler.getErrorMessage(throwable)
                onError(errorMessage)
            }
        }
    }
    
    /**
     * Share CV with custom error handling
     */
    suspend fun shareCVWithErrorHandling(
        cvId: String,
        request: CreateCVShareRequest,
        onError: (String) -> Unit
    ): Result<CVShareResponse> {
        return shareCV(cvId, request).also { result ->
            result.onFailure { throwable ->
                val errorMessage = ApiErrorHandler.getErrorMessage(throwable)
                onError(errorMessage)
            }
        }
    }
    
    // ===== VALIDATION METHODS =====
    
    private fun validateCVUploadUrlRequest(request: CVUploadUrlRequest): ValidationResult {
        val errors = mutableListOf<ValidationError>()
        
        // Validate file name
        if (request.fileName.isBlank()) {
            errors.add(ValidationError("file_name", "File name is required"))
        } else if (request.fileName.length > 255) {
            errors.add(ValidationError("file_name", "File name cannot exceed 255 characters"))
        }
        
        // Validate title
        if (request.title.isBlank()) {
            errors.add(ValidationError("title", "Title is required"))
        } else if (request.title.length > 100) {
            errors.add(ValidationError("title", "Title cannot exceed 100 characters"))
        }
        
        // Validate file size
        if (request.fileSize <= 0) {
            errors.add(ValidationError("file_size", "File size must be greater than 0"))
        } else if (request.fileSize > 10 * 1024 * 1024) { // 10MB limit
            errors.add(ValidationError("file_size", "File size cannot exceed 10MB"))
        }
        
        // Validate file type
        val fileExtension = request.fileName.substringAfterLast('.', "").lowercase()
        val expectedExtensions = when (request.fileType) {
            CVFileType.PDF -> listOf("pdf")
            CVFileType.DOC -> listOf("doc")
            CVFileType.DOCX -> listOf("docx")
            CVFileType.TXT -> listOf("txt")
        }
        
        if (fileExtension !in expectedExtensions) {
            errors.add(ValidationError("file_type", "File extension does not match the specified file type"))
        }
        
        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errors)
        }
    }
    
    private fun validateCVBulkDeleteRequest(request: CVBulkDeleteRequest): ValidationResult {
        val errors = mutableListOf<ValidationError>()
        
        // Validate CV IDs
        if (request.cvIds.isEmpty()) {
            errors.add(ValidationError("cv_ids", "At least one CV ID is required"))
        } else if (request.cvIds.size > 50) {
            errors.add(ValidationError("cv_ids", "Maximum 50 CVs can be deleted at once"))
        }
        
        if (request.cvIds.any { it.isBlank() }) {
            errors.add(ValidationError("cv_ids", "CV IDs cannot be empty"))
        }
        
        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errors)
        }
    }
    
    private fun validateCVBulkStatusUpdateRequest(request: CVBulkStatusUpdateRequest): ValidationResult {
        val errors = mutableListOf<ValidationError>()
        
        // Validate CV IDs
        if (request.cvIds.isEmpty()) {
            errors.add(ValidationError("cv_ids", "At least one CV ID is required"))
        } else if (request.cvIds.size > 50) {
            errors.add(ValidationError("cv_ids", "Maximum 50 CVs can be updated at once"))
        }
        
        if (request.cvIds.any { it.isBlank() }) {
            errors.add(ValidationError("cv_ids", "CV IDs cannot be empty"))
        }
        
        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errors)
        }
    }
    
    private fun validateCVShareRequest(request: CreateCVShareRequest): ValidationResult {
        val errors = mutableListOf<ValidationError>()
        
        // Validate CV ID
        if (request.cvId.isBlank()) {
            errors.add(ValidationError("cv_id", "CV ID is required"))
        }
        
        // Validate recipient email
        if (request.recipientEmail.isBlank()) {
            errors.add(ValidationError("recipient_email", "Recipient email is required"))
        } else if (!isValidEmail(request.recipientEmail)) {
            errors.add(ValidationError("recipient_email", "Invalid email format"))
        }
        
        // Validate message length
        request.message?.let { message ->
            if (message.length > 1000) {
                errors.add(ValidationError("message", "Message cannot exceed 1000 characters"))
            }
        }
        
        // Validate expiration date
        request.expiresAt?.let { date ->
            if (!isValidDateTime(date)) {
                errors.add(ValidationError("expires_at", "Invalid date format. Use ISO 8601 format (YYYY-MM-DDTHH:mm:ssZ)"))
            }
        }
        
        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errors)
        }
    }
    
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    
    private fun isValidDateTime(dateTimeString: String): Boolean {
        return try {
            // Simple ISO 8601 datetime validation (YYYY-MM-DDTHH:mm:ssZ)
            val regex = Regex("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z?$")
            regex.matches(dateTimeString)
        } catch (e: Exception) {
            false
        }
    }
}
