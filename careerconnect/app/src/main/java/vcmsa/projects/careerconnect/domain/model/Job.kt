//CODE ATTRIBUTION
//01
//Kotlin data classes
//Adapted from: Kotlin Docs. (2025). Data classes. [online]
//Available at: https://kotlinlang.org/docs/data-classes.html
//Date Accessed: 30 September 2025

//02
//Kotlin enums
//Adapted from: Kotlin Docs. (2025). Enum classes. [online]
//Available at: https://kotlinlang.org/docs/enum-classes.html
//Date Accessed: 30 September 2025

//03
//Serialized GSON Names
//Adapted from: Gson Javadoc. (2025). SerializedName. [online]
//Available at: https://javadoc.io/doc/com.google.code.gson/gson/latest/com/google/gson/annotations/SerializedName.html
//Date Accessed: 30 September 2025

//04
//Kotlin collections List/Map
//Adapted from: Kotlin Docs. (2025). Collections overview. [online]
//Available at: https://kotlinlang.org/docs/collections-overview.html
//Date Accessed: 30 September 2025

//05
//Kotlin Pair
//Adapted from: Kotlin Stdlib. (2025). Pair. [online]
//Available at: https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-pair/
//Date Accessed: 30 September 2025

package vcmsa.projects.careerconnect.domain.model

import com.google.gson.annotations.SerializedName

/**
 * Enum for job types
 */
enum class JobType {
    @SerializedName("full-time")
    FULL_TIME,
    
    @SerializedName("part-time")
    PART_TIME,
    
    @SerializedName("contract")
    CONTRACT,
    
    @SerializedName("internship")
    INTERNSHIP
}

/**
 * Enum for experience levels
 */
enum class ExperienceLevel {
    @SerializedName("entry")
    ENTRY,
    
    @SerializedName("mid")
    MID,
    
    @SerializedName("senior")
    SENIOR,
    
    @SerializedName("executive")
    EXECUTIVE
}


/**
 * Data class representing a job posting
 */
data class Job(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("recruiter_uid")
    val recruiterUid: String, // Recruiter's Firebase UID
    
    @SerializedName("title")
    val title: String,
    
    @SerializedName("description")
    val description: String,
    
    @SerializedName("company_name")
    val companyName: String,
    
    
    @SerializedName("location")
    val location: String,
    
    @SerializedName("job_type")
    val jobType: JobType,
    
    // work_arrangement omitted (not used by backend)
    
    @SerializedName("experience_level")
    val experienceLevel: ExperienceLevel,
    
    @SerializedName("salary_min")
    val salaryMin: Double? = null,
    
    @SerializedName("salary_max")
    val salaryMax: Double? = null,
    
    @SerializedName("currency")
    val currency: String? = null,
    
    @SerializedName("industry")
    val industry: String? = null,
    
    @SerializedName("requirements")
    val requirements: String? = null,
    
    // benefits present in DB but not used in UI; omit or keep null
    @SerializedName("benefits")
    val benefits: String? = null,
    
    // application_deadline removed to match DB schema
    
    @SerializedName("application_count")
    val applicationCount: Int = 0,
    
    @SerializedName("is_active")
    val isActive: Boolean = true,
    
    @SerializedName("created_at")
    val createdAt: String,
    
    @SerializedName("updated_at")
    val updatedAt: String
)

/**
 * Data class for job search filters
 */
data class JobSearchFilter(
    @SerializedName("query")
    val query: String? = null,
    
    @SerializedName("location")
    val location: String? = null,
    
    @SerializedName("job_types")
    val jobTypes: List<JobType>? = null,
    
    @SerializedName("experience_levels")
    val experienceLevels: List<ExperienceLevel>? = null,
    
    @SerializedName("salary_min")
    val salaryMin: Double? = null,
    
    @SerializedName("salary_max")
    val salaryMax: Double? = null,
    
    @SerializedName("currency")
    val currency: String = "USD",
    
    @SerializedName("skills")
    val skills: List<String>? = null,
    
    @SerializedName("company_name")
    val companyName: String? = null,
    
    @SerializedName("is_remote")
    val isRemote: Boolean? = null,
    
    @SerializedName("posted_after")
    val postedAfter: String? = null, // ISO 8601 date string
    
    @SerializedName("posted_before")
    val postedBefore: String? = null, // ISO 8601 date string
    
    @SerializedName("sort_by")
    val sortBy: JobSortBy = JobSortBy.RELEVANCE,
    
    @SerializedName("sort_order")
    val sortOrder: SortOrder = SortOrder.DESC
)

/**
 * Enum for job sorting options
 */
enum class JobSortBy {
    @SerializedName("relevance")
    RELEVANCE,
    
    @SerializedName("date_posted")
    DATE_POSTED,
    
    @SerializedName("salary")
    SALARY,
    
    @SerializedName("company_name")
    COMPANY_NAME,
    
    @SerializedName("title")
    TITLE
}

/**
 * Enum for sort order
 */
enum class SortOrder {
    @SerializedName("asc")
    ASC,
    
    @SerializedName("desc")
    DESC
}

/**
 * Data class for job search request
 */
data class JobSearchRequest(
    @SerializedName("filters")
    val filters: JobSearchFilter,
    
    @SerializedName("page")
    val page: Int = 1,
    
    @SerializedName("limit")
    val limit: Int = 20
)

/**
 * Data class for job search response
 */
data class JobSearchResponse(
    @SerializedName("jobs")
    val jobs: List<Job>,
    
    @SerializedName("pagination")
    val pagination: PaginationMeta,
    
    @SerializedName("filters_applied")
    val filtersApplied: JobSearchFilter,
    
    @SerializedName("total_matches")
    val totalMatches: Int
)

/**
 * Data class for job details request
 */
data class JobDetailsRequest(
    @SerializedName("job_id")
    val jobId: String
)

/**
 * Data class for job application
 */
data class JobApplication(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("job_id")
    val jobId: String,
    
    @SerializedName("applicant_uid")
    val applicantUid: String, // Job seeker's Firebase UID
    
    @SerializedName("cv_id")
    val cvId: String? = null,
    
    @SerializedName("cover_letter")
    val coverLetter: String? = null,
    
    @SerializedName("resume_url")
    val resumeUrl: String? = null,
    
    @SerializedName("status")
    val status: ApplicationStatus = ApplicationStatus.PENDING,
    
    @SerializedName("applied_at")
    val appliedAt: String,
    
    @SerializedName("reviewed_at")
    val reviewedAt: String? = null,
    
    @SerializedName("updated_at")
    val updatedAt: String,
    
    // Extended fields for recruiter applications
    @SerializedName("job_postings")
    val jobPosting: JobPosting? = null,
    
    @SerializedName("user_profiles")
    val applicantProfile: ApplicantProfile? = null,
    
    @SerializedName("cvs")
    val cv: ApplicationCV? = null
)

/**
 * Data class for job posting details in application
 */
data class JobPosting(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("title")
    val title: String,
    
    @SerializedName("company_name")
    val companyName: String,
    
    @SerializedName("location")
    val location: String,
    
    @SerializedName("job_type")
    val jobType: JobType
)

/**
 * Data class for applicant profile in application
 */
data class ApplicantProfile(
    @SerializedName("first_name")
    val firstName: String,
    
    @SerializedName("last_name")
    val lastName: String,
    
    @SerializedName("email")
    val email: String,
    
    @SerializedName("phone")
    val phone: String? = null,
    
    @SerializedName("location")
    val location: String? = null,
    
    @SerializedName("profile_image_url")
    val profileImageUrl: String? = null
)

/**
 * Data class for CV details in application
 */
data class ApplicationCV(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("file_name")
    val fileName: String,
    
    @SerializedName("file_url")
    val fileUrl: String,
    
    @SerializedName("file_size")
    val fileSize: Long? = null
)

/**
 * Enum for application status
 */
enum class ApplicationStatus {
    @SerializedName("pending")
    PENDING,
    
    @SerializedName("under_review")
    UNDER_REVIEW,
    
    @SerializedName("shortlisted")
    SHORTLISTED,
    
    @SerializedName("interview_scheduled")
    INTERVIEW_SCHEDULED,
    
    @SerializedName("rejected")
    REJECTED,
    
    @SerializedName("accepted")
    ACCEPTED,
    
    @SerializedName("withdrawn")
    WITHDRAWN
}

/**
 * Data class for job application request
 */
data class JobApplicationRequest(
    @SerializedName("job_id")
    val jobId: String,
    
    @SerializedName("cv_id")
    val cvId: String? = null,
    
    @SerializedName("cover_letter")
    val coverLetter: String? = null,
    
    @SerializedName("resume_url")
    val resumeUrl: String? = null
)

/**
 * Enum for job status (for recruiter management)
 */
enum class JobStatus {
    @SerializedName("draft")
    DRAFT,
    
    @SerializedName("active")
    ACTIVE,
    
    @SerializedName("paused")
    PAUSED,
    
    @SerializedName("closed")
    CLOSED,
    
    @SerializedName("expired")
    EXPIRED
}

/**
 * Data class for creating a job posting
 */
data class CreateJobRequest(
    @SerializedName("title")
    val title: String,
    
    @SerializedName("description")
    val description: String,
    
    @SerializedName("company_name")
    val companyName: String,
    
    
    @SerializedName("location")
    val location: String,
    
    @SerializedName("job_type")
    val jobType: JobType,
    
    @SerializedName("experience_level")
    val experienceLevel: ExperienceLevel,
    
    @SerializedName("salary_min")
    val salaryMin: Int? = null,
    
    @SerializedName("salary_max")
    val salaryMax: Int? = null,
    
    // Not supported by current backend schema; omit when null
    @SerializedName("currency")
    val currency: String? = null,
    
    
    
    @SerializedName("industry")
    val industry: String? = null,
    
    @SerializedName("requirements")
    val requirements: String? = null,
    
    // Removed fields to match DB schema: company_logo_url, application_deadline
)

/**
 * Data class for updating a job posting
 */
data class UpdateJobRequest(
    @SerializedName("title")
    val title: String? = null,
    
    @SerializedName("description")
    val description: String? = null,
    
    @SerializedName("company_name")
    val companyName: String? = null,
    
    
    @SerializedName("location")
    val location: String? = null,
    
    @SerializedName("job_type")
    val jobType: JobType? = null,
    
    @SerializedName("experience_level")
    val experienceLevel: ExperienceLevel? = null,
    
    @SerializedName("salary_min")
    val salaryMin: Double? = null,
    
    @SerializedName("salary_max")
    val salaryMax: Double? = null,
    
    @SerializedName("currency")
    val currency: String? = null,
    
    
    
    @SerializedName("industry")
    val industry: String? = null,
    
    @SerializedName("requirements")
    val requirements: String? = null,
    
    @SerializedName("is_active")
    val isActive: Boolean? = null
)

/**
 * Data class for updating application status
 */
data class UpdateApplicationStatusRequest(
    @SerializedName("status")
    val status: ApplicationStatus,
    
    @SerializedName("notes")
    val notes: String? = null
)
