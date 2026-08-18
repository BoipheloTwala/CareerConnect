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
 * Repository class for handling job search and application operations
 */
class JobRepository {
    
    private val apiService = NetworkModule.apiService
    
    // ===== JOB SEARCH OPERATIONS =====
    
    /**
     * Search for jobs with filters
     */
    suspend fun searchJobs(request: JobSearchRequest): Result<JobSearchResponse> {
        // Validate request before making API call
        val validationResult = ProfileValidator.validateJobSearchRequest(request)
        if (!validationResult.isValid()) {
            val errors = validationResult.getErrors().joinToString(", ") { "${it.field}: ${it.message}" }
            return Result.failure(Exception("Validation failed: $errors"))
        }
        
        return safeApiCall(
            apiCall = { apiService.searchJobs(request) },
            errorHandler = { ApiErrorHandler.logError("JobRepository", it, "Search Jobs") }
        )
    }
    
    /**
     * Search jobs with NetworkResult for UI state management
     */
    suspend fun searchJobsAsNetworkResult(request: JobSearchRequest): NetworkResult<JobSearchResponse> {
        return searchJobs(request).toNetworkResult()
    }
    
    /**
     * Get job details by ID
     */
    suspend fun getJobDetails(jobId: String): Result<Job> {
        if (jobId.isBlank()) {
            return Result.failure(Exception("Job ID cannot be empty"))
        }
        
        return safeApiCall(
            apiCall = { apiService.getJobDetails(jobId) },
            errorHandler = { ApiErrorHandler.logError("JobRepository", it, "Get Job Details") }
        )
    }
    
    /**
     * Get featured/recommended jobs
     */
    suspend fun getFeaturedJobs(limit: Int = 10): Result<List<Job>> {
        if (limit < 1 || limit > 50) {
            return Result.failure(Exception("Limit must be between 1 and 50"))
        }
        
        return safeApiCall(
            apiCall = { apiService.getFeaturedJobs(limit) },
            errorHandler = { ApiErrorHandler.logError("JobRepository", it, "Get Featured Jobs") }
        ).mapCatching { json ->
            parseJobsJson(json)
        }
    }
    
    /**
     * Get recent jobs
     */
    suspend fun getRecentJobs(limit: Int = 20): Result<List<Job>> {
        if (limit < 1 || limit > 100) {
            return Result.failure(Exception("Limit must be between 1 and 100"))
        }
        
        return safeApiCall(
            apiCall = { apiService.getRecentJobs(limit) },
            errorHandler = { ApiErrorHandler.logError("JobRepository", it, "Get Recent Jobs") }
        ).mapCatching { json ->
            parseJobsJson(json)
        }
    }
    
    /**
     * Get all jobs
     */
    suspend fun getAllJobs(limit: Int = 20, page: Int? = null): Result<List<Job>> {
        if (limit < 1 || limit > 100) {
            return Result.failure(Exception("Limit must be between 1 and 100"))
        }
        
        return safeApiCall(
            apiCall = { apiService.getAllJobs(limit, page) },
            errorHandler = { ApiErrorHandler.logError("JobRepository", it, "Get All Jobs") }
        ).mapCatching { json ->
            parseJobsJson(json)
        }
    }

    private fun parseJobsJson(element: com.google.gson.JsonElement): List<Job> {
        return when {
            element.isJsonArray -> {
                val type = com.google.gson.reflect.TypeToken.getParameterized(List::class.java, Job::class.java).type
                vcmsa.projects.careerconnect.data.network.NetworkModule.apiService // no-op to avoid unused import
                com.google.gson.Gson().fromJson(element, type)
            }
            element.isJsonObject -> {
                val obj = element.asJsonObject
                val array = obj.get("jobs") ?: obj.get("data") ?: obj.get("results") ?: obj.get("items")
                if (array != null && array.isJsonArray) {
                    val type = com.google.gson.reflect.TypeToken.getParameterized(List::class.java, Job::class.java).type
                    com.google.gson.Gson().fromJson(array, type)
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
        if (companyName.isBlank()) {
            return Result.failure(Exception("Company name cannot be empty"))
        }
        
        if (page < 1) {
            return Result.failure(Exception("Page must be greater than 0"))
        }
        
        if (limit < 1 || limit > 100) {
            return Result.failure(Exception("Limit must be between 1 and 100"))
        }
        
        return safeApiCall(
            apiCall = { apiService.getJobsByCompany(companyName, page, limit) },
            errorHandler = { ApiErrorHandler.logError("JobRepository", it, "Get Jobs by Company") }
        )
    }
    
    // ===== FILTER OPTIONS =====
    
    /**
     * Get job types for filter dropdown
     */
    suspend fun getJobTypes(): Result<List<JobType>> {
        return safeApiCall(
            apiCall = { apiService.getJobTypes() },
            errorHandler = { ApiErrorHandler.logError("JobRepository", it, "Get Job Types") }
        )
    }
    
    /**
     * Get experience levels for filter dropdown
     */
    suspend fun getExperienceLevels(): Result<List<ExperienceLevel>> {
        return safeApiCall(
            apiCall = { apiService.getExperienceLevels() },
            errorHandler = { ApiErrorHandler.logError("JobRepository", it, "Get Experience Levels") }
        )
    }
    
    
    
    /**
     * Get popular skills for filter suggestions
     */
    suspend fun getPopularSkills(limit: Int = 20): Result<List<String>> {
        if (limit < 1 || limit > 100) {
            return Result.failure(Exception("Limit must be between 1 and 100"))
        }
        
        return safeApiCall(
            apiCall = { apiService.getPopularSkills(limit) },
            errorHandler = { ApiErrorHandler.logError("JobRepository", it, "Get Popular Skills") }
        )
    }
    
    /**
     * Search skills with autocomplete
     */
    suspend fun searchSkills(query: String, limit: Int = 10): Result<List<String>> {
        if (query.isBlank()) {
            return Result.failure(Exception("Search query cannot be empty"))
        }
        
        if (limit < 1 || limit > 50) {
            return Result.failure(Exception("Limit must be between 1 and 50"))
        }
        
        return safeApiCall(
            apiCall = { apiService.searchSkills(query, limit) },
            errorHandler = { ApiErrorHandler.logError("JobRepository", it, "Search Skills") }
        )
    }
    
    // ===== JOB APPLICATION OPERATIONS =====
    
    /**
     * Apply for a job
     */
    suspend fun applyForJob(jobId: String, request: JobApplicationRequest): Result<JobApplication> {
        // Validate request before making API call
        val validationResult = ProfileValidator.validateJobApplicationRequest(request)
        if (!validationResult.isValid()) {
            val errors = validationResult.getErrors().joinToString(", ") { "${it.field}: ${it.message}" }
            return Result.failure(Exception("Validation failed: $errors"))
        }
        
        if (jobId.isBlank()) {
            return Result.failure(Exception("Job ID cannot be empty"))
        }
        
        return safeApiCall(
            apiCall = { apiService.createApplication(request.copy(jobId = jobId)) },
            errorHandler = { ApiErrorHandler.logError("JobRepository", it, "Apply for Job") }
        )
    }
    
    /**
     * Get user's job applications
     */
    suspend fun getMyApplications(page: Int = 1, limit: Int = 20, status: ApplicationStatus? = null): Result<PaginatedResponse<JobApplication>> {
        if (page < 1) {
            return Result.failure(Exception("Page must be greater than 0"))
        }
        
        if (limit < 1 || limit > 100) {
            return Result.failure(Exception("Limit must be between 1 and 100"))
        }
        
        return safeApiCall(
            apiCall = { apiService.getMyApplications(page, limit, status) },
            errorHandler = { ApiErrorHandler.logError("JobRepository", it, "Get My Applications") }
        )
    }
    
    /**
     * Get application details
     */
    suspend fun getApplicationDetails(applicationId: String): Result<JobApplication> {
        if (applicationId.isBlank()) {
            return Result.failure(Exception("Application ID cannot be empty"))
        }
        
        return safeApiCall(
            apiCall = { apiService.getApplicationDetails(applicationId) },
            errorHandler = { ApiErrorHandler.logError("JobRepository", it, "Get Application Details") }
        )
    }
    
    /**
     * Withdraw job application
     */
    suspend fun withdrawApplication(applicationId: String): Result<Unit> {
        if (applicationId.isBlank()) {
            return Result.failure(Exception("Application ID cannot be empty"))
        }
        
        return safeApiCall(
            apiCall = { apiService.withdrawApplication(applicationId) },
            errorHandler = { ApiErrorHandler.logError("JobRepository", it, "Withdraw Application") }
        )
    }
    
    // ===== RECRUITER JOB MANAGEMENT OPERATIONS =====
    
    /**
     * Create a new job posting
     */
    suspend fun createJob(request: CreateJobRequest): Result<Unit> {
        // Validate request before making API call
        val validationResult = validateCreateJobRequest(request)
        if (!validationResult.isValid()) {
            val errors = validationResult.getErrors().joinToString(", ") { "${it.field}: ${it.message}" }
            return Result.failure(Exception("Validation failed: $errors"))
        }
        
        return safeApiCall(
            apiCall = { apiService.createJob(request) },
            errorHandler = { ApiErrorHandler.logError("JobRepository", it, "Create Job") }
        )
    }
    
    /**
     * Update job posting
     */
    suspend fun updateJob(jobId: String, request: UpdateJobRequest): Result<Job> {
        if (jobId.isBlank()) {
            return Result.failure(Exception("Job ID cannot be empty"))
        }
        
        // Validate request before making API call
        val validationResult = validateUpdateJobRequest(request)
        if (!validationResult.isValid()) {
            val errors = validationResult.getErrors().joinToString(", ") { "${it.field}: ${it.message}" }
            return Result.failure(Exception("Validation failed: $errors"))
        }
        
        return safeApiCall(
            apiCall = { apiService.updateJob(jobId, request) },
            errorHandler = { ApiErrorHandler.logError("JobRepository", it, "Update Job") }
        )
    }
    
    /**
     * Delete job posting
     */
    suspend fun deleteJob(jobId: String): Result<Unit> {
        if (jobId.isBlank()) {
            return Result.failure(Exception("Job ID cannot be empty"))
        }
        
        return safeApiCall(
            apiCall = { apiService.deleteJob(jobId) },
            errorHandler = { ApiErrorHandler.logError("JobRepository", it, "Delete Job") }
        )
    }
    
    /**
     * Get recruiter's job postings
     */
    suspend fun getMyJobPostings(page: Int = 1, limit: Int = 20, status: JobStatus? = null): Result<PaginatedResponse<Job>> {
        if (page < 1) {
            return Result.failure(Exception("Page must be greater than 0"))
        }
        
        if (limit < 1 || limit > 100) {
            return Result.failure(Exception("Limit must be between 1 and 100"))
        }
        
        return safeApiCall(
            apiCall = { apiService.getMyJobPostings(page, limit, status) },
            errorHandler = { ApiErrorHandler.logError("JobRepository", it, "Get My Job Postings") }
        )
    }
    
    /**
     * Get applications for a job
     */
    suspend fun getJobApplications(jobId: String, page: Int = 1, limit: Int = 20, status: ApplicationStatus? = null): Result<PaginatedResponse<JobApplication>> {
        if (jobId.isBlank()) {
            return Result.failure(Exception("Job ID cannot be empty"))
        }
        
        if (page < 1) {
            return Result.failure(Exception("Page must be greater than 0"))
        }
        
        if (limit < 1 || limit > 100) {
            return Result.failure(Exception("Limit must be between 1 and 100"))
        }
        
        return safeApiCall(
            apiCall = { apiService.getJobApplications(jobId, page, limit, status) },
            errorHandler = { ApiErrorHandler.logError("JobRepository", it, "Get Job Applications") }
        )
    }

    /**
     * Get recruiter applications
     */
    suspend fun getRecruiterApplications(jobId: String? = null): Result<List<JobApplication>> {
        return safeApiCall(
            apiCall = { apiService.getRecruiterApplications(jobId) },
            errorHandler = { ApiErrorHandler.logError("JobRepository", it, "Get Recruiter Applications") }
        )
    }
    
    /**
     * Update application status
     */
    suspend fun updateApplicationStatus(applicationId: String, request: UpdateApplicationStatusRequest): Result<JobApplication> {
        if (applicationId.isBlank()) {
            return Result.failure(Exception("Application ID cannot be empty"))
        }
        
        return safeApiCall(
            apiCall = { apiService.updateApplicationStatus(applicationId, request) },
            errorHandler = { ApiErrorHandler.logError("JobRepository", it, "Update Application Status") }
        )
    }
    
    // ===== HELPER METHODS =====
    
    /**
     * Create job with custom error handling
     */
    suspend fun createJobWithErrorHandling(
        request: CreateJobRequest,
        onError: (String) -> Unit
    ): Result<Unit> {
        return createJob(request).also { result ->
            result.onFailure { throwable ->
                val errorMessage = ApiErrorHandler.getErrorMessage(throwable)
                onError(errorMessage)
            }
        }
    }
    
    /**
     * Search jobs with custom error handling
     */
    suspend fun searchJobsWithErrorHandling(
        request: JobSearchRequest,
        onError: (String) -> Unit
    ): Result<JobSearchResponse> {
        return searchJobs(request).also { result ->
            result.onFailure { throwable ->
                val errorMessage = ApiErrorHandler.getErrorMessage(throwable)
                onError(errorMessage)
            }
        }
    }
    
    /**
     * Apply for job with custom error handling
     */
    suspend fun applyForJobWithErrorHandling(
        jobId: String,
        request: JobApplicationRequest,
        onError: (String) -> Unit
    ): Result<JobApplication> {
        return applyForJob(jobId, request).also { result ->
            result.onFailure { throwable ->
                val errorMessage = ApiErrorHandler.getErrorMessage(throwable)
                onError(errorMessage)
            }
        }
    }
    
    // ===== VALIDATION METHODS =====
    
    private fun validateCreateJobRequest(request: CreateJobRequest): ValidationResult {
        val errors = mutableListOf<ValidationError>()
        
        // Required fields validation
        if (request.title.isBlank()) {
            errors.add(ValidationError("title", "Job title is required"))
        } else if (request.title.length > 200) {
            errors.add(ValidationError("title", "Job title cannot exceed 200 characters"))
        }
        
        if (request.description.isBlank()) {
            errors.add(ValidationError("description", "Job description is required"))
        } else if (request.description.length > 5000) {
            errors.add(ValidationError("description", "Job description cannot exceed 5000 characters"))
        }
        
        if (request.location.isBlank()) {
            errors.add(ValidationError("location", "Job location is required"))
        } else if (request.location.length > 100) {
            errors.add(ValidationError("location", "Job location cannot exceed 100 characters"))
        }
        
        // Salary validation
        request.salaryMin?.let { min ->
            if (min < 0) {
                errors.add(ValidationError("salary_min", "Minimum salary cannot be negative"))
            }
        }
        
        request.salaryMax?.let { max ->
            if (max < 0) {
                errors.add(ValidationError("salary_max", "Maximum salary cannot be negative"))
            }
        }
        
        if (request.salaryMin != null && request.salaryMax != null) {
            if (request.salaryMin > request.salaryMax) {
                errors.add(ValidationError("salary_range", "Minimum salary cannot be greater than maximum salary"))
            }
        }
        
        // Currency validation (optional)
        request.currency?.let { currency ->
            if (currency.length != 3) {
            errors.add(ValidationError("currency", "Currency must be a 3-letter code (e.g., USD, EUR)"))
            }
        }
        
        // Skills removed from schema
        
        // Industry validation (optional string)
        request.industry?.let { industry ->
            if (industry.isBlank()) {
            errors.add(ValidationError("industry", "Industry cannot be empty"))
            }
        }
        // Requirements validation (optional string)
        request.requirements?.let { reqs ->
            if (reqs.isBlank()) {
            errors.add(ValidationError("requirements", "Requirements cannot be empty"))
            }
        }
        
        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errors)
        }
    }
    
    private fun validateUpdateJobRequest(request: UpdateJobRequest): ValidationResult {
        val errors = mutableListOf<ValidationError>()
        
        // Validate optional fields if provided
        request.title?.let { title ->
            if (title.isBlank()) {
                errors.add(ValidationError("title", "Job title cannot be empty"))
            } else if (title.length > 200) {
                errors.add(ValidationError("title", "Job title cannot exceed 200 characters"))
            } else {
                // no-op: valid title
            }
        }
        
        request.description?.let { description ->
            if (description.isBlank()) {
                errors.add(ValidationError("description", "Job description cannot be empty"))
            } else if (description.length > 5000) {
                errors.add(ValidationError("description", "Job description cannot exceed 5000 characters"))
            } else {
                // no-op: valid description
            }
        }
        
        request.location?.let { location ->
            if (location.isBlank()) {
                errors.add(ValidationError("location", "Job location cannot be empty"))
            } else if (location.length > 100) {
                errors.add(ValidationError("location", "Job location cannot exceed 100 characters"))
            } else {
                // no-op: valid location
            }
        }
        
        // Salary validation
        request.salaryMin?.let { min ->
            if (min < 0) {
                errors.add(ValidationError("salary_min", "Minimum salary cannot be negative"))
            }
        }
        
        request.salaryMax?.let { max ->
            if (max < 0) {
                errors.add(ValidationError("salary_max", "Maximum salary cannot be negative"))
            }
        }
        
        if (request.salaryMin != null && request.salaryMax != null) {
            if (request.salaryMin > request.salaryMax) {
                errors.add(ValidationError("salary_range", "Minimum salary cannot be greater than maximum salary"))
            }
        }
        
        // Currency validation
        request.currency?.let { currency ->
            if (currency.length != 3) {
                errors.add(ValidationError("currency", "Currency must be a 3-letter code (e.g., USD, EUR)"))
            }
        }
        
        // Skills removed from schema
        
        // Industry validation (optional string)
        request.industry?.let { industry ->
            if (industry.isBlank()) {
                errors.add(ValidationError("industry", "Industry cannot be empty"))
            }
        }
        // Requirements validation (optional string)
        request.requirements?.let { reqs ->
            if (reqs.isBlank()) {
                errors.add(ValidationError("requirements", "Requirements cannot be empty"))
            }
        }
        
        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errors)
        }
    }
}


