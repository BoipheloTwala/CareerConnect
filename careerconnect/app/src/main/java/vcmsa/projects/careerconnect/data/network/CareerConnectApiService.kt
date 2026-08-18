//CODE ATTRIBUTION
//01
//Retrofit Declarations (request method, URL, body, headers)
//Adapted from: Square. (2025). Retrofit Declarations. [online]
//Available at: https://square.github.io/retrofit/declarations/#request-method
//Date Accessed: 01 October 2025

//02
//@GET
//Adapted from: Square. (2025). retrofit2.http.GET. [online]
//Available at: https://square.github.io/retrofit/2.x/retrofit/retrofit2/http/GET.html
//Date Accessed: 01 October 2025

//03
//@POST
//Adapted from: Square. (2025). retrofit2.http.POST. [online]
//Available at: https://square.github.io/retrofit/2.x/retrofit/retrofit2/http/POST.html
//Date Accessed: 01 October 2025

//04
//@PUT
//Adapted from: Square. (2025). retrofit2.http.PUT. [online]
//Available at: https://square.github.io/retrofit/2.x/retrofit/retrofit2/http/PUT.html
//Date Accessed: 01 October 2025

//05
//@DELETE
//Adapted from: Square. (2025). retrofit2.http.DELETE. [online]
//Available at: https://square.github.io/retrofit/2.x/retrofit/retrofit2/http/DELETE.html
//Date Accessed: 01 October 2025

//06
//@PATCH
//Adapted from: Square. (2025). retrofit2.http.PATCH. [online]
//Available at: https://square.github.io/retrofit/2.x/retrofit/retrofit2/http/PATCH.html
//Date Accessed: 01 October 2025

//07
//@Body
//Adapted from: Square. (2025). retrofit2.http.Body. [online]
//Available at: https://square.github.io/retrofit/2.x/retrofit/retrofit2/http/Body.html
//Date Accessed: 01 October 2025

//08
//@Path
//Adapted from: Square. (2025). retrofit2.http.Path. [online]
//Available at: https://square.github.io/retrofit/2.x/retrofit/retrofit2/http/Path.html
//Date Accessed: 01 October 2025

//09
//@Query
//Adapted from: Square. (2025). retrofit2.http.Query. [online]
//Available at: https://square.github.io/retrofit/2.x/retrofit/retrofit2/http/Query.html
//Date Accessed: 01 October 2025

package vcmsa.projects.careerconnect.data.network

import retrofit2.Response
import retrofit2.http.*
import vcmsa.projects.careerconnect.domain.model.*
import com.google.gson.JsonElement

/**
 * Retrofit API service interface for CareerConnect API
 */
interface CareerConnectApiService {
    
    /**
     * Create a new user profile
     * POST /profiles/me
     */
    @POST("profiles/me")
    suspend fun createProfile(
        @Body request: CreateProfileRequest
    ): Response<UserProfile>
    
    /**
     * Get the current user's profile
     * GET /profiles/me
     */
    @GET("profiles/me")
    suspend fun getProfile(): Response<UserProfile>
    
    /**
     * Update the current user's profile
     * PUT /profiles/me
     */
    @PUT("profiles/me")
    suspend fun updateProfile(
        @Body request: UpdateProfileRequest
    ): Response<UserProfile>
    
    // ===== JOB SEARCH ENDPOINTS =====
    
    /**
     * Search for jobs with filters
     * POST /jobs/search
     */
    @POST("jobs/search")
    suspend fun searchJobs(
        @Body request: JobSearchRequest
    ): Response<JobSearchResponse>
    
    /**
     * Get job details by ID
     * GET /jobs/{jobId}
     */
    @GET("jobs/{jobId}")
    suspend fun getJobDetails(
        @Path("jobId") jobId: String
    ): Response<Job>
    
    /**
     * Get featured/recommended jobs
     * GET /jobs/featured
     */
    @GET("jobs/featured")
    suspend fun getFeaturedJobs(
        @Query("limit") limit: Int = 10
    ): Response<JsonElement>
    
    /**
     * Get recent jobs
     * GET /jobs/recent
     */
    @GET("jobs/recent")
    suspend fun getRecentJobs(
        @Query("limit") limit: Int = 20
    ): Response<JsonElement>
    
    /**
     * Get all jobs (generic listing)
     * GET /jobs
     */
    @GET("jobs")
    suspend fun getAllJobs(
        @Query("limit") limit: Int = 20,
        @Query("page") page: Int? = null
    ): Response<JsonElement>
    
    /**
     * Get jobs by company
     * GET /jobs/company/{companyName}
     */
    @GET("jobs/company/{companyName}")
    suspend fun getJobsByCompany(
        @Path("companyName") companyName: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<PaginatedResponse<Job>>
    
    /**
     * Get job types for filter dropdown
     * GET /jobs/types
     */
    @GET("jobs/types")
    suspend fun getJobTypes(): Response<List<JobType>>
    
    /**
     * Get experience levels for filter dropdown
     * GET /jobs/experience-levels
     */
    @GET("jobs/experience-levels")
    suspend fun getExperienceLevels(): Response<List<ExperienceLevel>>
    
    
    
    /**
     * Get popular skills for filter suggestions
     * GET /jobs/skills/popular
     */
    @GET("jobs/skills/popular")
    suspend fun getPopularSkills(
        @Query("limit") limit: Int = 20
    ): Response<List<String>>
    
    /**
     * Search skills with autocomplete
     * GET /jobs/skills/search
     */
    @GET("jobs/skills/search")
    suspend fun searchSkills(
        @Query("query") query: String,
        @Query("limit") limit: Int = 10
    ): Response<List<String>>
    
    // ===== JOB APPLICATION ENDPOINTS =====
    
    /**
     * Apply for a job
     * POST /applications (server expects body with job_id, cv_id, cover_letter)
     */
    @POST("applications")
    suspend fun createApplication(
        @Body request: JobApplicationRequest
    ): Response<JobApplication>
    
    /**
     * Get user's job applications
     * GET /applications/me
     */
    @GET("applications/me")
    suspend fun getMyApplications(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("status") status: ApplicationStatus? = null
    ): Response<PaginatedResponse<JobApplication>>
    
    /**
     * Get application details
     * GET /jobs/applications/{applicationId}
     */
    @GET("jobs/applications/{applicationId}")
    suspend fun getApplicationDetails(
        @Path("applicationId") applicationId: String
    ): Response<JobApplication>
    
    /**
     * Withdraw job application
     * DELETE /jobs/applications/{applicationId}
     */
    @DELETE("jobs/applications/{applicationId}")
    suspend fun withdrawApplication(
        @Path("applicationId") applicationId: String
    ): Response<Unit>
    
    // ===== RECRUITER JOB MANAGEMENT ENDPOINTS =====
    
    /**
     * Create a new job posting
     * POST /jobs
     */
    @POST("jobs")
    suspend fun createJob(
        @Body request: CreateJobRequest
    ): Response<Unit>
    
    /**
     * Update job posting
     * PUT /jobs/{jobId}
     */
    @PUT("jobs/{jobId}")
    suspend fun updateJob(
        @Path("jobId") jobId: String,
        @Body request: UpdateJobRequest
    ): Response<Job>
    
    /**
     * Delete job posting
     * DELETE /jobs/{jobId}
     */
    @DELETE("jobs/{jobId}")
    suspend fun deleteJob(
        @Path("jobId") jobId: String
    ): Response<Unit>
    
    /**
     * Get recruiter's job postings
     * GET /jobs/my-postings
     */
    @GET("jobs/my-postings")
    suspend fun getMyJobPostings(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("status") status: JobStatus? = null
    ): Response<PaginatedResponse<Job>>
    
    /**
     * Get applications for a job
     * GET /jobs/{jobId}/applications
     */
    @GET("jobs/{jobId}/applications")
    suspend fun getJobApplications(
        @Path("jobId") jobId: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("status") status: ApplicationStatus? = null
    ): Response<PaginatedResponse<JobApplication>>

    /**
     * Get recruiter applications
     * GET /applications/recruiter
     */
    @GET("applications/recruiter")
    suspend fun getRecruiterApplications(
        @Query("job_id") jobId: String? = null
    ): Response<List<JobApplication>>
    
    /**
     * Update application status
     * PATCH /applications/{applicationId}/status
     */
    @PATCH("applications/{applicationId}/status")
    suspend fun updateApplicationStatus(
        @Path("applicationId") applicationId: String,
        @Body request: UpdateApplicationStatusRequest
    ): Response<JobApplication>
    
    // ===== CV UPLOAD AND MANAGEMENT ENDPOINTS =====
    
    /**
     * Upload a new CV
     * POST /cvs
     */
    @POST("cvs")
    suspend fun uploadCV(
        @Body request: CVCreateRequest
    ): Response<CV>
    
    /**
     * Get user's CVs
     * GET /cvs
     */
    @GET("cvs")
    suspend fun getMyCVs(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("status") status: CVStatus? = null,
        @Query("file_type") fileType: CVFileType? = null
    ): Response<PaginatedResponse<CV>>
    
    /**
     * Get CV details by ID
     * GET /cvs/{cvId}
     */
    @GET("cvs/{cvId}")
    suspend fun getCVDetails(
        @Path("cvId") cvId: String
    ): Response<CV>
    
    /**
     * Update CV details
     * PUT /cvs/{cvId}
     */
    @PUT("cvs/{cvId}")
    suspend fun updateCV(
        @Path("cvId") cvId: String,
        @Body request: CVUpdateRequest
    ): Response<CV>
    
    /**
     * Delete CV
     * DELETE /cvs/{cvId}
     */
    @DELETE("cvs/{cvId}")
    suspend fun deleteCV(
        @Path("cvId") cvId: String
    ): Response<Unit>
    
    /**
     * Set primary CV
     * PUT /cvs/{cvId}/primary
     */
    @PUT("cvs/{cvId}/primary")
    suspend fun setPrimaryCV(
        @Path("cvId") cvId: String
    ): Response<CV>
    
    /**
     * Get primary CV
     * GET /cvs/primary
     */
    @GET("cvs/primary")
    suspend fun getPrimaryCV(): Response<CV>
    
    /**
     * Search CVs
     * POST /cvs/search
     */
    @POST("cvs/search")
    suspend fun searchCVs(
        @Body request: CVSearchRequest
    ): Response<PaginatedResponse<CV>>
    
    /**
     * Get CV analytics
     * GET /cvs/analytics
     */
    @GET("cvs/analytics")
    suspend fun getCVAnalytics(): Response<CVAnalytics>
    
    /**
     * Get CV storage usage
     * GET /cvs/storage
     */
    @GET("cvs/storage")
    suspend fun getCVStorageUsage(): Response<StorageUsage>
    
    // ===== CV SHARING ENDPOINTS =====
    
    /**
     * Share CV via email
     * POST /cvs/{cvId}/share
     */
    @POST("cvs/{cvId}/share")
    suspend fun shareCV(
        @Path("cvId") cvId: String,
        @Body request: CreateCVShareRequest
    ): Response<CVShareResponse>
    
    /**
     * Get shared CV by token
     * GET /cvs/shared/{shareToken}
     */
    @GET("cvs/shared/{shareToken}")
    suspend fun getSharedCV(
        @Path("shareToken") shareToken: String
    ): Response<CV>
    
    /**
     * Revoke CV share
     * DELETE /cvs/{cvId}/share/{shareToken}
     */
    @DELETE("cvs/{cvId}/share/{shareToken}")
    suspend fun revokeCVShare(
        @Path("cvId") cvId: String,
        @Path("shareToken") shareToken: String
    ): Response<Unit>
    
    // ===== CV DOWNLOAD ENDPOINTS =====
    
    /**
     * Get CV download URL
     * POST /cvs/{cvId}/download
     */
    @POST("cvs/{cvId}/download")
    suspend fun getCVDownloadUrl(
        @Path("cvId") cvId: String,
        @Body request: CVDownloadRequest
    ): Response<CVDownloadResponse>
    
    /**
     * Download CV file
     * GET /cvs/{cvId}/file
     */
    @GET("cvs/{cvId}/file")
    suspend fun downloadCVFile(
        @Path("cvId") cvId: String,
        @Query("type") downloadType: DownloadType = DownloadType.ORIGINAL
    ): Response<Unit> // This will return the file as response body
    
    // ===== CV UPLOAD HELPERS =====
    
    /**
     * Get CV upload URL for direct upload to Cloudinary
     * POST /cvs/upload-url
     */
    @POST("cvs/upload-url")
    suspend fun getCVUploadUrl(
        @Body request: CVUploadUrlRequest
    ): Response<CVUploadUrlResponse>
    
    /**
     * Confirm CV upload after direct upload
     * POST /cvs/{cvId}/confirm-upload
     */
    @POST("cvs/{cvId}/confirm-upload")
    suspend fun confirmCVUpload(
        @Path("cvId") cvId: String,
        @Body request: CVUploadConfirmationRequest
    ): Response<CV>
    
    // ===== CV BULK OPERATIONS =====
    
    /**
     * Bulk delete CVs
     * DELETE /cvs/bulk
     */
    @DELETE("cvs/bulk")
    suspend fun bulkDeleteCVs(
        @Body request: CVBulkDeleteRequest
    ): Response<CVBulkDeleteResponse>
    
    /**
     * Bulk update CV status
     * PUT /cvs/bulk/status
     */
    @PUT("cvs/bulk/status")
    suspend fun bulkUpdateCVStatus(
        @Body request: CVBulkStatusUpdateRequest
    ): Response<CVBulkStatusUpdateResponse>
    
    /**
     * Export CVs data
     * GET /cvs/export
     */
    @GET("cvs/export")
    suspend fun exportCVsData(
        @Query("format") format: ExportFormat = ExportFormat.JSON
    ): Response<Unit> // Returns file as response body
    
    // ===== SAVED JOBS ENDPOINTS =====
    
    /**
     * Save a job
     * POST /saved
     */
    @POST("saved")
    suspend fun saveJob(
        @Body request: SaveJobRequest
    ): Response<SavedJob>
    
    /**
     * Get user's saved jobs
     * GET /saved
     */
    @GET("saved")
    suspend fun getSavedJobs(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<com.google.gson.JsonElement>
    
    /**
     * Search saved jobs
     * POST /jobs/saved/search
     */
    @POST("jobs/saved/search")
    suspend fun searchSavedJobs(
        @Body request: SavedJobsSearchRequest
    ): Response<SavedJobsSearchResponse>
    
    /**
     * Get saved job details
     * GET /saved/{savedJobId}
     */
    @GET("saved/{savedJobId}")
    suspend fun getSavedJobDetails(
        @Path("savedJobId") savedJobId: String
    ): Response<SavedJob>
    
    /**
     * Update saved job
     * PUT /jobs/saved/{savedJobId}
     */
    @PUT("jobs/saved/{savedJobId}")
    suspend fun updateSavedJob(
        @Path("savedJobId") savedJobId: String,
        @Body request: UpdateSavedJobRequest
    ): Response<SavedJob>
    
    /**
     * Remove saved job
     * DELETE /saved/{job_id}
     */
    @DELETE("saved/{job_id}")
    suspend fun removeSavedJob(
        @Path("job_id") jobId: String
    ): Response<Unit>
    
    /**
     * Check if job is saved
     * GET /saved/check/{job_id}
     */
    @GET("saved/check/{job_id}")
    suspend fun isJobSaved(
        @Path("job_id") jobId: String
    ): Response<SavedJob?>
    
    /**
     * Bulk operations on saved jobs
     * POST /jobs/saved/bulk
     */
    @POST("jobs/saved/bulk")
    suspend fun bulkSavedJobsOperation(
        @Body request: SavedJobsBulkRequest
    ): Response<Unit>
    
    /**
     * Bulk update saved jobs
     * PUT /jobs/saved/bulk
     */
    @PUT("jobs/saved/bulk")
    suspend fun bulkUpdateSavedJobs(
        @Body request: SavedJobsBulkUpdateRequest
    ): Response<Unit>
    
    /**
     * Get saved jobs analytics
     * GET /jobs/saved/analytics
     */
    @GET("jobs/saved/analytics")
    suspend fun getSavedJobsAnalytics(): Response<SavedJobsAnalytics>
    
    /**
     * Get saved jobs reminders
     * GET /jobs/saved/reminders
     */
    @GET("jobs/saved/reminders")
    suspend fun getSavedJobsReminders(): Response<List<SavedJob>>
    
    // ===== APPLICATION TRACKING ENDPOINTS =====
    
    /**
     * Create application tracking
     * POST /applications/tracking
     */
    @POST("applications/tracking")
    suspend fun createApplicationTracking(
        @Body request: CreateApplicationTrackingRequest
    ): Response<ApplicationTracking>
    
    /**
     * Get user's application tracking
     * GET /applications/tracking
     */
    @GET("applications/tracking")
    suspend fun getApplicationTracking(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<PaginatedResponse<ApplicationTracking>>
    
    /**
     * Search application tracking
     * POST /applications/tracking/search
     */
    @POST("applications/tracking/search")
    suspend fun searchApplicationTracking(
        @Body request: ApplicationTrackingSearchRequest
    ): Response<ApplicationTrackingSearchResponse>
    
    /**
     * Get application tracking details
     * GET /applications/tracking/{trackingId}
     */
    @GET("applications/tracking/{trackingId}")
    suspend fun getApplicationTrackingDetails(
        @Path("trackingId") trackingId: String
    ): Response<ApplicationTracking>
    
    /**
     * Update application tracking
     * PUT /applications/tracking/{trackingId}
     */
    @PUT("applications/tracking/{trackingId}")
    suspend fun updateApplicationTracking(
        @Path("trackingId") trackingId: String,
        @Body request: UpdateApplicationTrackingRequest
    ): Response<ApplicationTracking>
    
    /**
     * Delete application tracking
     * DELETE /applications/tracking/{trackingId}
     */
    @DELETE("applications/tracking/{trackingId}")
    suspend fun deleteApplicationTracking(
        @Path("trackingId") trackingId: String
    ): Response<Unit>
    
    /**
     * Get application tracking by application ID
     * GET /applications/{applicationId}/tracking
     */
    @GET("applications/{applicationId}/tracking")
    suspend fun getApplicationTrackingByApplicationId(
        @Path("applicationId") applicationId: String
    ): Response<ApplicationTracking>
    
    /**
     * Bulk operations on application tracking
     * POST /applications/tracking/bulk
     */
    @POST("applications/tracking/bulk")
    suspend fun bulkApplicationTrackingOperation(
        @Body request: ApplicationTrackingBulkRequest
    ): Response<Unit>
    
    /**
     * Bulk update application tracking
     * PUT /applications/tracking/bulk
     */
    @PUT("applications/tracking/bulk")
    suspend fun bulkUpdateApplicationTracking(
        @Body request: ApplicationTrackingBulkUpdateRequest
    ): Response<Unit>
    
    /**
     * Get application tracking analytics
     * GET /applications/tracking/analytics
     */
    @GET("applications/tracking/analytics")
    suspend fun getApplicationTrackingAnalytics(): Response<ApplicationTrackingAnalytics>
    
    /**
     * Get application tracking dashboard
     * GET /applications/tracking/dashboard
     */
    @GET("applications/tracking/dashboard")
    suspend fun getApplicationTrackingDashboard(): Response<ApplicationTrackingSearchResponse>
    
    /**
     * Get follow-up reminders
     * GET /applications/tracking/reminders
     */
    @GET("applications/tracking/reminders")
    suspend fun getFollowUpReminders(): Response<List<ApplicationTracking>>
    
    /**
     * Get upcoming interviews
     * GET /applications/tracking/interviews
     */
    @GET("applications/tracking/interviews")
    suspend fun getUpcomingInterviews(): Response<List<ApplicationTracking>>
    
    // ===== ANALYTICS ENDPOINTS =====
    
    /**
     * Track job search analytics
     * POST /analytics/job-search
     */
    @POST("analytics/job-search")
    suspend fun trackJobSearch(
        @Body request: JobSearchAnalytics
    ): Response<Unit>
    
    /**
     * Track CV download analytics
     * POST /analytics/cv-download
     */
    @POST("analytics/cv-download")
    suspend fun trackCVDownload(
        @Body request: CVDownloadAnalytics
    ): Response<Unit>
    
    /**
     * Track application status change
     * POST /analytics/application-status-change
     */
    @POST("analytics/application-status-change")
    suspend fun trackApplicationStatusChange(
        @Body request: ApplicationStatusHistory
    ): Response<Unit>
}