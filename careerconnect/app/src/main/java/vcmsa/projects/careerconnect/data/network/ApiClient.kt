//CODE ATTRIBUTION
//01
//FirebaseAuth
//Adapted from: Firebase. (2025). FirebaseAuth. [online] Firebase.
//Available at: https://firebase.google.com/docs/reference/android/com/google/firebase/auth/FirebaseAuth
//Date Accessed: 01 October 2025

//02
//FirebaseUser.getIdToken(forceRefresh)
//Adapted from: Firebase. (2025). FirebaseUser#getIdToken. [online] Firebase.
//Available at: https://firebase.google.com/docs/reference/android/com/google/firebase/auth/FirebaseUser#getIdToken(boolean)
//Date Accessed: 01 October 2025

//03
//Google Identity (SignInClient)
//Adapted from: Google Developers. (2025). SignInClient (One Tap). [online]
//Available at: https://developers.google.com/identity/one-tap/android/reference/com/google/android/gms/auth/api/identity/SignInClient
//Date Accessed: 01 October 2025

//04
//GoogleSignInOptions
//Adapted from: Google Developers. (2025). GoogleSignInOptions. [online]
//Available at: https://developers.google.com/android/reference/com/google/android/gms/auth/api/signin/GoogleSignInOptions
//Date Accessed: 01 October 2025

//05
//GoogleSignIn
//Adapted from: Google Developers. (2025). GoogleSignIn. [online]
//Available at: https://developers.google.com/android/reference/com/google/android/gms/auth/api/signin/GoogleSignIn
//Date Accessed: 01 October 2025

package vcmsa.projects.careerconnect.data.network

import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import retrofit2.Response
import vcmsa.projects.careerconnect.domain.model.*
import java.io.IOException

/**
 * Singleton API client that automatically handles Firebase authentication
 * and provides convenient methods for making API calls
 */
object ApiClient {
    
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    val apiService: CareerConnectApiService = NetworkModule.apiService
    
    /**
     * Creates a user profile
     */
    suspend fun createProfile(request: CreateProfileRequest): Result<UserProfile> {
        return safeApiCall(
            apiCall = { apiService.createProfile(request) },
            errorHandler = { ApiErrorHandler.logError("ApiClient", it, "Failed to create profile") }
        )
    }
    
    /**
     * Gets the current user's profile
     */
    suspend fun getProfile(): Result<UserProfile> {
        return safeApiCall(
            apiCall = { apiService.getProfile() },
            errorHandler = { ApiErrorHandler.logError("ApiClient", it, "Failed to get profile") }
        )
    }
    
    /**
     * Updates the current user's profile
     */
    suspend fun updateProfile(request: UpdateProfileRequest): Result<UserProfile> {
        return safeApiCall(
            apiCall = { apiService.updateProfile(request) },
            errorHandler = { ApiErrorHandler.logError("ApiClient", it, "Failed to update profile") }
        )
    }
    
    /**
     * Gets the current Firebase user's ID token
     * This method handles token refresh automatically
     */
    suspend fun getCurrentUserToken(): String? {
        return try {
            val user = firebaseAuth.currentUser
            if (user != null) {
                val tokenResult = user.getIdToken(true).await() // Force refresh
                tokenResult.token
            } else {
                null
            }
        } catch (e: Exception) {
            ApiErrorHandler.logError("ApiClient", e, "Failed to get Firebase token")
            null
        }
    }
    
    /**
     * Checks if the current user is authenticated
     */
    fun isUserAuthenticated(): Boolean {
        return firebaseAuth.currentUser != null
    }
    
    /**
     * Gets the current user's Firebase UID
     */
    fun getCurrentUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }
    
    /**
     * Signs out the current user
     */
    suspend fun signOut(): Result<Unit> {
        return try {
            firebaseAuth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            ApiErrorHandler.logError("ApiClient", e, "Failed to sign out")
            Result.failure(e)
        }
    }

    /**
     * Fully signs out the current user including Google Identity sessions so that
     * the next sign-in shows the account chooser instead of silently reusing state.
     */
    suspend fun signOut(context: android.content.Context): Result<Unit> {
        return try {
            // 1) Firebase sign out
            firebaseAuth.signOut()

            // 2) Google Identity (One Tap / Credential Manager) sign out
            try {
                val oneTap = com.google.android.gms.auth.api.identity.Identity.getSignInClient(context)
                oneTap.signOut()
            } catch (_: Exception) {
                // Ignore if not configured
            }

            // 3) Legacy Google Sign-In client sign out (if used anywhere in app)
            try {
                val gso = com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(
                    com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN
                )
                    .requestEmail()
                    .build()
                val googleClient = com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(context, gso)
                googleClient.signOut()
            } catch (_: Exception) {
                // Ignore if not configured
            }

            Result.success(Unit)
        } catch (e: Exception) {
            ApiErrorHandler.logError("ApiClient", e, "Failed to fully sign out")
            Result.failure(e)
        }
    }
    
    /**
     * Creates a profile with validation
     */
    suspend fun createProfileWithValidation(request: CreateProfileRequest): Result<UserProfile> {
        // Validate request first
        val validationResult = ProfileValidator.validateCreateRequest(request)
        if (!validationResult.isValid()) {
            val errors = validationResult.getErrors().joinToString(", ") { "${it.field}: ${it.message}" }
            return Result.failure(Exception("Validation failed: $errors"))
        }
        
        // Check if user is authenticated
        if (!isUserAuthenticated()) {
            return Result.failure(Exception("User must be authenticated to create profile"))
        }
        
        // Create profile
        return createProfile(request)
    }
    
    /**
     * Updates a profile with validation
     */
    suspend fun updateProfileWithValidation(request: UpdateProfileRequest): Result<UserProfile> {
        // Validate request first
        val validationResult = ProfileValidator.validateUpdateRequest(request)
        if (!validationResult.isValid()) {
            val errors = validationResult.getErrors().joinToString(", ") { "${it.field}: ${it.message}" }
            return Result.failure(Exception("Validation failed: $errors"))
        }
        
        // Check if user is authenticated
        if (!isUserAuthenticated()) {
            return Result.failure(Exception("User must be authenticated to update profile"))
        }
        
        // Update profile
        return updateProfile(request)
    }
    
    /**
     * Gets profile with authentication check
     */
    suspend fun getProfileWithAuthCheck(): Result<UserProfile> {
        if (!isUserAuthenticated()) {
            return Result.failure(Exception("User must be authenticated to get profile"))
        }
        
        return getProfile()
    }
    
    /**
     * Retries an API call with exponential backoff
     */
    private suspend fun <T> retryApiCall(
        maxRetries: Int = 3,
        initialDelayMs: Long = 1000,
        apiCall: suspend () -> Result<T>
    ): Result<T> {
        var lastException: Exception? = null
        
        repeat(maxRetries) { attempt ->
            try {
                val result = apiCall()
                if (result.isSuccess) {
                    return result
                } else {
                    lastException = result.exceptionOrNull() as? Exception
                }
            } catch (e: Exception) {
                lastException = e
            }
            
            // Don't retry on the last attempt
            if (attempt < maxRetries - 1) {
                val delay = initialDelayMs * (attempt + 1)
                kotlinx.coroutines.delay(delay)
            }
        }
        
        return Result.failure(lastException ?: Exception("API call failed after $maxRetries attempts"))
    }
    
    /**
     * Makes an authenticated API call with automatic retry
     */
    suspend fun <T> makeAuthenticatedCall(
        apiCall: suspend () -> Result<T>,
        maxRetries: Int = 3
    ): Result<T> {
        if (!isUserAuthenticated()) {
            return Result.failure(Exception("User must be authenticated"))
        }
        
        return retryApiCall(maxRetries = maxRetries) {
            apiCall()
        }
    }
    
    /**
     * Utility method to check if an error is retryable
     */
    fun isRetryableError(throwable: Throwable): Boolean {
        return ApiErrorHandler.isRetryable(throwable)
    }
    
    /**
     * Utility method to get user-friendly error message
     */
    fun getUserFriendlyErrorMessage(throwable: Throwable): String {
        return ApiErrorHandler.getErrorMessage(throwable)
    }
    
    // ===== JOB SEARCH METHODS =====
    
    /**
     * Search for jobs with filters
     */
    suspend fun searchJobs(request: JobSearchRequest): Result<JobSearchResponse> {
        return safeApiCall(
            apiCall = { apiService.searchJobs(request) },
            errorHandler = { ApiErrorHandler.logError("ApiClient", it, "Failed to search jobs") }
        )
    }
    
    /**
     * Get job details by ID
     */
    suspend fun getJobDetails(jobId: String): Result<Job> {
        return safeApiCall(
            apiCall = { apiService.getJobDetails(jobId) },
            errorHandler = { ApiErrorHandler.logError("ApiClient", it, "Failed to get job details") }
        )
    }
    
    /**
     * Get featured/recommended jobs
     */
    suspend fun getFeaturedJobs(limit: Int = 10): Result<List<Job>> {
        return safeApiCall(
            apiCall = { apiService.getFeaturedJobs(limit) },
            errorHandler = { ApiErrorHandler.logError("ApiClient", it, "Failed to get featured jobs") }
        ).mapCatching { json -> parseJobsJson(json) }
    }
    
    /**
     * Get recent jobs
     */
    suspend fun getRecentJobs(limit: Int = 20): Result<List<Job>> {
        return safeApiCall(
            apiCall = { apiService.getRecentJobs(limit) },
            errorHandler = { ApiErrorHandler.logError("ApiClient", it, "Failed to get recent jobs") }
        ).mapCatching { json -> parseJobsJson(json) }
    }

    private fun parseJobsJson(element: JsonElement): List<Job> {
        return when {
            element.isJsonArray -> {
                val type = TypeToken.getParameterized(List::class.java, Job::class.java).type
                Gson().fromJson(element, type)
            }
            element.isJsonObject -> {
                val obj = element.asJsonObject
                val array = obj.get("jobs") ?: obj.get("data") ?: obj.get("results") ?: obj.get("items")
                if (array != null && array.isJsonArray) {
                    val type = TypeToken.getParameterized(List::class.java, Job::class.java).type
                    Gson().fromJson(array, type)
                } else {
                    emptyList()
                }
            }
            else -> emptyList()
        }
    }
    
    /**
     * Get jobs by company
     */
    suspend fun getJobsByCompany(companyName: String, page: Int = 1, limit: Int = 20): Result<PaginatedResponse<Job>> {
        return safeApiCall(
            apiCall = { apiService.getJobsByCompany(companyName, page, limit) },
            errorHandler = { ApiErrorHandler.logError("ApiClient", it, "Failed to get jobs by company") }
        )
    }
    
    /**
     * Get job types for filter dropdown
     */
    suspend fun getJobTypes(): Result<List<JobType>> {
        return safeApiCall(
            apiCall = { apiService.getJobTypes() },
            errorHandler = { ApiErrorHandler.logError("ApiClient", it, "Failed to get job types") }
        )
    }
    
    /**
     * Get experience levels for filter dropdown
     */
    suspend fun getExperienceLevels(): Result<List<ExperienceLevel>> {
        return safeApiCall(
            apiCall = { apiService.getExperienceLevels() },
            errorHandler = { ApiErrorHandler.logError("ApiClient", it, "Failed to get experience levels") }
        )
    }
    
    
    
    /**
     * Get popular skills for filter suggestions
     */
    suspend fun getPopularSkills(limit: Int = 20): Result<List<String>> {
        return safeApiCall(
            apiCall = { apiService.getPopularSkills(limit) },
            errorHandler = { ApiErrorHandler.logError("ApiClient", it, "Failed to get popular skills") }
        )
    }
    
    /**
     * Search skills with autocomplete
     */
    suspend fun searchSkills(query: String, limit: Int = 10): Result<List<String>> {
        return safeApiCall(
            apiCall = { apiService.searchSkills(query, limit) },
            errorHandler = { ApiErrorHandler.logError("ApiClient", it, "Failed to search skills") }
        )
    }
    
    // ===== JOB APPLICATION METHODS =====
    
    /**
     * Apply for a job
     */
    suspend fun applyForJob(jobId: String, request: JobApplicationRequest): Result<JobApplication> {
        return safeApiCall(
            apiCall = { apiService.createApplication(request.copy(jobId = jobId)) },
            errorHandler = { ApiErrorHandler.logError("ApiClient", it, "Failed to apply for job") }
        )
    }
    
    /**
     * Get user's job applications
     */
    suspend fun getMyApplications(page: Int = 1, limit: Int = 20, status: ApplicationStatus? = null): Result<PaginatedResponse<JobApplication>> {
        return safeApiCall(
            apiCall = { apiService.getMyApplications(page, limit, status) },
            errorHandler = { ApiErrorHandler.logError("ApiClient", it, "Failed to get my applications") }
        )
    }
    
    /**
     * Get application details
     */
    suspend fun getApplicationDetails(applicationId: String): Result<JobApplication> {
        return safeApiCall(
            apiCall = { apiService.getApplicationDetails(applicationId) },
            errorHandler = { ApiErrorHandler.logError("ApiClient", it, "Failed to get application details") }
        )
    }
    
    /**
     * Withdraw job application
     */
    suspend fun withdrawApplication(applicationId: String): Result<Unit> {
        return safeApiCall(
            apiCall = { apiService.withdrawApplication(applicationId) },
            errorHandler = { ApiErrorHandler.logError("ApiClient", it, "Failed to withdraw application") }
        )
    }
    
    // ===== RECRUITER JOB MANAGEMENT METHODS =====
    
    /**
     * Create a new job posting
     */
    suspend fun createJob(request: CreateJobRequest): Result<Unit> {
        return safeApiCall(
            apiCall = { apiService.createJob(request) },
            errorHandler = { ApiErrorHandler.logError("ApiClient", it, "Failed to create job") }
        )
    }
    
    /**
     * Update job posting
     */
    suspend fun updateJob(jobId: String, request: UpdateJobRequest): Result<Job> {
        return safeApiCall(
            apiCall = { apiService.updateJob(jobId, request) },
            errorHandler = { ApiErrorHandler.logError("ApiClient", it, "Failed to update job") }
        )
    }
    
    /**
     * Delete job posting
     */
    suspend fun deleteJob(jobId: String): Result<Unit> {
        return safeApiCall(
            apiCall = { apiService.deleteJob(jobId) },
            errorHandler = { ApiErrorHandler.logError("ApiClient", it, "Failed to delete job") }
        )
    }
    
    /**
     * Get recruiter's job postings
     */
    suspend fun getMyJobPostings(page: Int = 1, limit: Int = 20, status: JobStatus? = null): Result<PaginatedResponse<Job>> {
        return safeApiCall(
            apiCall = { apiService.getMyJobPostings(page, limit, status) },
            errorHandler = { ApiErrorHandler.logError("ApiClient", it, "Failed to get my job postings") }
        )
    }
    
    /**
     * Get applications for a job
     */
    suspend fun getJobApplications(jobId: String, page: Int = 1, limit: Int = 20, status: ApplicationStatus? = null): Result<PaginatedResponse<JobApplication>> {
        return safeApiCall(
            apiCall = { apiService.getJobApplications(jobId, page, limit, status) },
            errorHandler = { ApiErrorHandler.logError("ApiClient", it, "Failed to get job applications") }
        )
    }

    /**
     * Get recruiter applications
     */
    suspend fun getRecruiterApplications(jobId: String? = null): Result<List<JobApplication>> {
        return safeApiCall(
            apiCall = { apiService.getRecruiterApplications(jobId) },
            errorHandler = { ApiErrorHandler.logError("ApiClient", it, "Failed to get recruiter applications") }
        )
    }
    
    /**
     * Update application status
     */
    suspend fun updateApplicationStatus(applicationId: String, request: UpdateApplicationStatusRequest): Result<JobApplication> {
        return safeApiCall(
            apiCall = { apiService.updateApplicationStatus(applicationId, request) },
            errorHandler = { ApiErrorHandler.logError("ApiClient", it, "Failed to update application status") }
        )
    }
    
    // ===== JOB SEARCH WITH VALIDATION =====
    
    /**
     * Search jobs with validation
     */
    suspend fun searchJobsWithValidation(request: JobSearchRequest): Result<JobSearchResponse> {
        // Validate request first
        val validationResult = ProfileValidator.validateJobSearchRequest(request)
        if (!validationResult.isValid()) {
            val errors = validationResult.getErrors().joinToString(", ") { "${it.field}: ${it.message}" }
            return Result.failure(Exception("Validation failed: $errors"))
        }
        
        // Check if user is authenticated
        if (!isUserAuthenticated()) {
            return Result.failure(Exception("User must be authenticated to search jobs"))
        }
        
        // Search jobs
        return searchJobs(request)
    }
    
    /**
     * Apply for job with validation
     */
    suspend fun applyForJobWithValidation(jobId: String, request: JobApplicationRequest): Result<JobApplication> {
        // Validate request first
        val validationResult = ProfileValidator.validateJobApplicationRequest(request)
        if (!validationResult.isValid()) {
            val errors = validationResult.getErrors().joinToString(", ") { "${it.field}: ${it.message}" }
            return Result.failure(Exception("Validation failed: $errors"))
        }
        
        // Check if user is authenticated
        if (!isUserAuthenticated()) {
            return Result.failure(Exception("User must be authenticated to apply for jobs"))
        }
        
        // Apply for job
        return applyForJob(jobId, request)
    }
    
    /**
     * Create job with validation
     */
    suspend fun createJobWithValidation(request: CreateJobRequest): Result<Unit> {
        // Check if user is authenticated
        if (!isUserAuthenticated()) {
            return Result.failure(Exception("User must be authenticated to create jobs"))
        }
        
        // Create job
        return createJob(request)
    }
    
    // ===== CV UPLOAD AND MANAGEMENT METHODS =====
    
    /**
     * Upload a new CV
     */
    suspend fun uploadCV(request: CVCreateRequest): Result<CV> {
        return safeApiCall(
            apiCall = { apiService.uploadCV(request) },
            errorHandler = { ApiErrorHandler.logError("ApiClient", it, "Failed to upload CV") }
        )
    }
    
    /**
     * Get user's CVs
     */
    suspend fun getMyCVs(page: Int = 1, limit: Int = 20, status: CVStatus? = null, fileType: CVFileType? = null): Result<PaginatedResponse<CV>> {
        return safeApiCall(
            apiCall = { apiService.getMyCVs(page, limit, status, fileType) },
            errorHandler = { ApiErrorHandler.logError("ApiClient", it, "Failed to get my CVs") }
        )
    }
    
    /**
     * Get CV details by ID
     */
    suspend fun getCVDetails(cvId: String): Result<CV> {
        return safeApiCall(
            apiCall = { apiService.getCVDetails(cvId) },
            errorHandler = { ApiErrorHandler.logError("ApiClient", it, "Failed to get CV details") }
        )
    }
    
    /**
     * Update CV details
     */
    suspend fun updateCV(cvId: String, request: CVUpdateRequest): Result<CV> {
        return safeApiCall(
            apiCall = { apiService.updateCV(cvId, request) },
            errorHandler = { ApiErrorHandler.logError("ApiClient", it, "Failed to update CV") }
        )
    }
    
    /**
     * Delete CV
     */
    suspend fun deleteCV(cvId: String): Result<Unit> {
        return safeApiCall(
            apiCall = { apiService.deleteCV(cvId) },
            errorHandler = { ApiErrorHandler.logError("ApiClient", it, "Failed to delete CV") }
        )
    }
    
    /**
     * Set primary CV
     */
    suspend fun setPrimaryCV(cvId: String): Result<CV> {
        return safeApiCall(
            apiCall = { apiService.setPrimaryCV(cvId) },
            errorHandler = { ApiErrorHandler.logError("ApiClient", it, "Failed to set primary CV") }
        )
    }
    
    /**
     * Get primary CV
     */
    suspend fun getPrimaryCV(): Result<CV> {
        return safeApiCall(
            apiCall = { apiService.getPrimaryCV() },
            errorHandler = { ApiErrorHandler.logError("ApiClient", it, "Failed to get primary CV") }
        )
    }
    
    /**
     * Search CVs
     */
    suspend fun searchCVs(request: CVSearchRequest): Result<PaginatedResponse<CV>> {
        return safeApiCall(
            apiCall = { apiService.searchCVs(request) },
            errorHandler = { ApiErrorHandler.logError("ApiClient", it, "Failed to search CVs") }
        )
    }
    
    /**
     * Get CV analytics
     */
    suspend fun getCVAnalytics(): Result<CVAnalytics> {
        return safeApiCall(
            apiCall = { apiService.getCVAnalytics() },
            errorHandler = { ApiErrorHandler.logError("ApiClient", it, "Failed to get CV analytics") }
        )
    }
    
    /**
     * Get CV storage usage
     */
    suspend fun getCVStorageUsage(): Result<StorageUsage> {
        return safeApiCall(
            apiCall = { apiService.getCVStorageUsage() },
            errorHandler = { ApiErrorHandler.logError("ApiClient", it, "Failed to get CV storage usage") }
        )
    }
    
    // ===== CV SHARING METHODS =====
    
    /**
     * Share CV via email
     */
    suspend fun shareCV(cvId: String, request: CreateCVShareRequest): Result<CVShareResponse> {
        return safeApiCall(
            apiCall = { apiService.shareCV(cvId, request) },
            errorHandler = { ApiErrorHandler.logError("ApiClient", it, "Failed to share CV") }
        )
    }
    
    /**
     * Get shared CV by token
     */
    suspend fun getSharedCV(shareToken: String): Result<CV> {
        return safeApiCall(
            apiCall = { apiService.getSharedCV(shareToken) },
            errorHandler = { ApiErrorHandler.logError("ApiClient", it, "Failed to get shared CV") }
        )
    }
    
    /**
     * Revoke CV share
     */
    suspend fun revokeCVShare(cvId: String, shareToken: String): Result<Unit> {
        return safeApiCall(
            apiCall = { apiService.revokeCVShare(cvId, shareToken) },
            errorHandler = { ApiErrorHandler.logError("ApiClient", it, "Failed to revoke CV share") }
        )
    }
    
    // ===== CV DOWNLOAD METHODS =====
    
    /**
     * Get CV download URL
     */
    suspend fun getCVDownloadUrl(cvId: String, request: CVDownloadRequest): Result<CVDownloadResponse> {
        return safeApiCall(
            apiCall = { apiService.getCVDownloadUrl(cvId, request) },
            errorHandler = { ApiErrorHandler.logError("ApiClient", it, "Failed to get CV download URL") }
        )
    }
    
    /**
     * Download CV file
     */
    suspend fun downloadCVFile(cvId: String, downloadType: DownloadType = DownloadType.ORIGINAL): Result<Unit> {
        return safeApiCall(
            apiCall = { apiService.downloadCVFile(cvId, downloadType) },
            errorHandler = { ApiErrorHandler.logError("ApiClient", it, "Failed to download CV file") }
        )
    }
    
    // ===== CV UPLOAD HELPERS =====
    
    /**
     * Get CV upload URL for direct upload to Cloudinary
     */
    suspend fun getCVUploadUrl(request: CVUploadUrlRequest): Result<CVUploadUrlResponse> {
        return safeApiCall(
            apiCall = { apiService.getCVUploadUrl(request) },
            errorHandler = { ApiErrorHandler.logError("ApiClient", it, "Failed to get CV upload URL") }
        )
    }
    
    /**
     * Confirm CV upload after direct upload
     */
    suspend fun confirmCVUpload(cvId: String, request: CVUploadConfirmationRequest): Result<CV> {
        return safeApiCall(
            apiCall = { apiService.confirmCVUpload(cvId, request) },
            errorHandler = { ApiErrorHandler.logError("ApiClient", it, "Failed to confirm CV upload") }
        )
    }
    
    // ===== CV BULK OPERATIONS =====
    
    /**
     * Bulk delete CVs
     */
    suspend fun bulkDeleteCVs(request: CVBulkDeleteRequest): Result<CVBulkDeleteResponse> {
        return safeApiCall(
            apiCall = { apiService.bulkDeleteCVs(request) },
            errorHandler = { ApiErrorHandler.logError("ApiClient", it, "Failed to bulk delete CVs") }
        )
    }
    
    /**
     * Bulk update CV status
     */
    suspend fun bulkUpdateCVStatus(request: CVBulkStatusUpdateRequest): Result<CVBulkStatusUpdateResponse> {
        return safeApiCall(
            apiCall = { apiService.bulkUpdateCVStatus(request) },
            errorHandler = { ApiErrorHandler.logError("ApiClient", it, "Failed to bulk update CV status") }
        )
    }
    
    /**
     * Export CVs data
     */
    suspend fun exportCVsData(format: ExportFormat = ExportFormat.JSON): Result<Unit> {
        return safeApiCall(
            apiCall = { apiService.exportCVsData(format) },
            errorHandler = { ApiErrorHandler.logError("ApiClient", it, "Failed to export CVs data") }
        )
    }
    
    // ===== CV METHODS WITH VALIDATION =====
    
    /**
     * Upload CV with validation
     */
    suspend fun uploadCVWithValidation(request: CVCreateRequest): Result<CV> {
        // Validate request first
        val validationResult = ProfileValidator.validateCVUploadRequest(
            CVUploadRequest(
                title = request.fileName,
                description = null,
                fileType = CVFileType.PDF,
                fileSize = request.fileSize ?: 1L,
                fileUrl = request.fileUrl,
                thumbnailUrl = null,
                isPrimary = request.isPrimary,
                tags = emptyList()
            )
        )
        if (!validationResult.isValid()) {
            val errors = validationResult.getErrors().joinToString(", ") { "${it.field}: ${it.message}" }
            return Result.failure(Exception("Validation failed: $errors"))
        }
        
        // Check if user is authenticated
        if (!isUserAuthenticated()) {
            return Result.failure(Exception("User must be authenticated to upload CVs"))
        }
        
        // Upload CV
        return uploadCV(request)
    }
    
    /**
     * Update CV with validation
     */
    suspend fun updateCVWithValidation(cvId: String, request: CVUpdateRequest): Result<CV> {
        // Validate request first
        val validationResult = ProfileValidator.validateCVUpdateRequest(request)
        if (!validationResult.isValid()) {
            val errors = validationResult.getErrors().joinToString(", ") { "${it.field}: ${it.message}" }
            return Result.failure(Exception("Validation failed: $errors"))
        }
        
        // Check if user is authenticated
        if (!isUserAuthenticated()) {
            return Result.failure(Exception("User must be authenticated to update CVs"))
        }
        
        // Update CV
        return updateCV(cvId, request)
    }
    
    /**
     * Share CV with validation
     */
    suspend fun shareCVWithValidation(cvId: String, request: CreateCVShareRequest): Result<CVShareResponse> {
        // Validate request first
        val validationResult = validateCVShareRequest(request)
        when (validationResult) {
            is ValidationResult.Invalid -> {
                val errors = validationResult.errors.joinToString(", ") { "${it.field}: ${it.message}" }
                return Result.failure(Exception("Validation failed: $errors"))
            }
            is ValidationResult.Valid -> {
                // Continue with the request
            }
        }
        
        // Check if user is authenticated
        if (!isUserAuthenticated()) {
            return Result.failure(Exception("User must be authenticated to share CVs"))
        }
        
        // Share CV
        return shareCV(cvId, request)
    }
    
    /**
     * Get CV download URL with validation
     */
    suspend fun getCVDownloadUrlWithValidation(cvId: String, request: CVDownloadRequest): Result<CVDownloadResponse> {
        // Validate request first
        val validationResult = ProfileValidator.validateCVDownloadRequest(request)
        if (!validationResult.isValid()) {
            val errors = validationResult.getErrors().joinToString(", ") { "${it.field}: ${it.message}" }
            return Result.failure(Exception("Validation failed: $errors"))
        }
        
        // Check if user is authenticated
        if (!isUserAuthenticated()) {
            return Result.failure(Exception("User must be authenticated to download CVs"))
        }
        
        // Get download URL
        return getCVDownloadUrl(cvId, request)
    }
    
    /**
     * Validate CV share request
     */
    private fun validateCVShareRequest(request: CreateCVShareRequest): ValidationResult {
        val errors = mutableListOf<ValidationError>()
        
        // Validate CV ID
        if (request.cvId.isBlank()) {
            errors.add(ValidationError("cv_id", "CV ID is required"))
        }
        
        // Validate recipient email
        if (request.recipientEmail.isBlank()) {
            errors.add(ValidationError("recipient_email", "Recipient email is required"))
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(request.recipientEmail).matches()) {
            errors.add(ValidationError("recipient_email", "Invalid email format"))
        }
        
        // Validate message (optional but if provided, should not be too long)
        if (!request.message.isNullOrBlank() && request.message.length > 1000) {
            errors.add(ValidationError("message", "Message cannot exceed 1000 characters"))
        }
        
        // Validate expiration date (optional but if provided, should be in the future)
        if (!request.expiresAt.isNullOrBlank()) {
            try {
                val expirationDate = java.time.Instant.parse(request.expiresAt)
                if (expirationDate.isBefore(java.time.Instant.now())) {
                    errors.add(ValidationError("expires_at", "Expiration date must be in the future"))
                }
            } catch (e: Exception) {
                errors.add(ValidationError("expires_at", "Invalid date format. Use ISO 8601 format"))
            }
        }
        
        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errors)
        }
    }
}