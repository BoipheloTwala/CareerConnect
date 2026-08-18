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
 * Data class representing application tracking information
 */
data class ApplicationTracking(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("application_id")
    val applicationId: String,
    
    @SerializedName("user_uid")
    val userUid: String, // Job seeker's Firebase UID
    
    @SerializedName("job_id")
    val jobId: String,
    
    @SerializedName("status")
    val status: ApplicationStatus,
    
    @SerializedName("applied_at")
    val appliedAt: String, // ISO 8601 timestamp
    
    @SerializedName("last_updated")
    val lastUpdated: String, // ISO 8601 timestamp
    
    @SerializedName("notes")
    val notes: String? = null, // User's personal notes about the application
    
    @SerializedName("follow_up_date")
    val followUpDate: String? = null, // When to follow up
    
    @SerializedName("is_follow_up_set")
    val isFollowUpSet: Boolean = false,
    
    @SerializedName("interview_scheduled_at")
    val interviewScheduledAt: String? = null,
    
    @SerializedName("interview_location")
    val interviewLocation: String? = null,
    
    @SerializedName("interview_notes")
    val interviewNotes: String? = null,
    
    @SerializedName("salary_discussed")
    val salaryDiscussed: Double? = null,
    
    @SerializedName("salary_currency")
    val salaryCurrency: String = "USD",
    
    @SerializedName("feedback_received")
    val feedbackReceived: String? = null,
    
    @SerializedName("tags")
    val tags: List<String> = emptyList(),
    
    @SerializedName("priority")
    val priority: ApplicationPriority = ApplicationPriority.MEDIUM,
    
    @SerializedName("created_at")
    val createdAt: String,
    
    @SerializedName("updated_at")
    val updatedAt: String,
    
    // Job and application details (populated when fetching)
    @SerializedName("job")
    val job: Job? = null,
    
    @SerializedName("application")
    val application: JobApplication? = null
)

/**
 * Enum for application tracking priority levels
 */
enum class ApplicationPriority {
    @SerializedName("low")
    LOW,
    
    @SerializedName("medium")
    MEDIUM,
    
    @SerializedName("high")
    HIGH,
    
    @SerializedName("urgent")
    URGENT
}

/**
 * Data class for creating application tracking
 */
data class CreateApplicationTrackingRequest(
    @SerializedName("application_id")
    val applicationId: String,
    
    @SerializedName("notes")
    val notes: String? = null,
    
    @SerializedName("follow_up_date")
    val followUpDate: String? = null,
    
    @SerializedName("tags")
    val tags: List<String> = emptyList(),
    
    @SerializedName("priority")
    val priority: ApplicationPriority = ApplicationPriority.MEDIUM
)

/**
 * Data class for updating application tracking
 */
data class UpdateApplicationTrackingRequest(
    @SerializedName("notes")
    val notes: String? = null,
    
    @SerializedName("follow_up_date")
    val followUpDate: String? = null,
    
    @SerializedName("is_follow_up_set")
    val isFollowUpSet: Boolean? = null,
    
    @SerializedName("interview_scheduled_at")
    val interviewScheduledAt: String? = null,
    
    @SerializedName("interview_location")
    val interviewLocation: String? = null,
    
    @SerializedName("interview_notes")
    val interviewNotes: String? = null,
    
    @SerializedName("salary_discussed")
    val salaryDiscussed: Double? = null,
    
    @SerializedName("salary_currency")
    val salaryCurrency: String? = null,
    
    @SerializedName("feedback_received")
    val feedbackReceived: String? = null,
    
    @SerializedName("tags")
    val tags: List<String>? = null,
    
    @SerializedName("priority")
    val priority: ApplicationPriority? = null
)

/**
 * Data class for application tracking search request
 */
data class ApplicationTrackingSearchRequest(
    @SerializedName("query")
    val query: String? = null,
    
    @SerializedName("status")
    val status: ApplicationStatus? = null,
    
    @SerializedName("priority")
    val priority: ApplicationPriority? = null,
    
    @SerializedName("company_name")
    val companyName: String? = null,
    
    @SerializedName("location")
    val location: String? = null,
    
    @SerializedName("job_type")
    val jobType: JobType? = null,
    
    @SerializedName("experience_level")
    val experienceLevel: ExperienceLevel? = null,
    
    @SerializedName("has_follow_up")
    val hasFollowUp: Boolean? = null,
    
    @SerializedName("has_interview")
    val hasInterview: Boolean? = null,
    
    @SerializedName("applied_after")
    val appliedAfter: String? = null, // ISO 8601 date string
    
    @SerializedName("applied_before")
    val appliedBefore: String? = null, // ISO 8601 date string
    
    @SerializedName("follow_up_after")
    val followUpAfter: String? = null, // ISO 8601 date string
    
    @SerializedName("follow_up_before")
    val followUpBefore: String? = null, // ISO 8601 date string
    
    @SerializedName("sort_by")
    val sortBy: ApplicationTrackingSortBy = ApplicationTrackingSortBy.APPLIED_DATE,
    
    @SerializedName("sort_order")
    val sortOrder: SortOrder = SortOrder.DESC
)

/**
 * Enum for application tracking sorting options
 */
enum class ApplicationTrackingSortBy {
    @SerializedName("applied_date")
    APPLIED_DATE,
    
    @SerializedName("last_updated")
    LAST_UPDATED,
    
    @SerializedName("status")
    STATUS,
    
    @SerializedName("priority")
    PRIORITY,
    
    @SerializedName("job_title")
    JOB_TITLE,
    
    @SerializedName("company_name")
    COMPANY_NAME,
    
    @SerializedName("follow_up_date")
    FOLLOW_UP_DATE,
    
    @SerializedName("interview_date")
    INTERVIEW_DATE
}

/**
 * Data class for application tracking search response
 */
data class ApplicationTrackingSearchResponse(
    @SerializedName("applications")
    val applications: List<ApplicationTracking>,
    
    @SerializedName("pagination")
    val pagination: PaginationMeta,
    
    @SerializedName("total_count")
    val totalCount: Int,
    
    @SerializedName("filters_applied")
    val filtersApplied: ApplicationTrackingSearchRequest,
    
    @SerializedName("status_summary")
    val statusSummary: ApplicationStatusSummary
)

/**
 * Data class for application status summary
 */
data class ApplicationStatusSummary(
    @SerializedName("total_applications")
    val totalApplications: Int,
    
    @SerializedName("applications_by_status")
    val applicationsByStatus: Map<ApplicationStatus, Int>,
    
    @SerializedName("applications_by_priority")
    val applicationsByPriority: Map<ApplicationPriority, Int>,
    
    @SerializedName("pending_follow_ups")
    val pendingFollowUps: Int,
    
    @SerializedName("upcoming_interviews")
    val upcomingInterviews: Int,
    
    @SerializedName("recent_applications")
    val recentApplications: List<ApplicationTracking>
)

/**
 * Data class for application tracking analytics
 */
data class ApplicationTrackingAnalytics(
    @SerializedName("total_applications")
    val totalApplications: Int,
    
    @SerializedName("applications_by_month")
    val applicationsByMonth: Map<String, Int>,
    
    @SerializedName("applications_by_status")
    val applicationsByStatus: Map<ApplicationStatus, Int>,
    
    @SerializedName("applications_by_company")
    val applicationsByCompany: Map<String, Int>,
    
    @SerializedName("applications_by_location")
    val applicationsByLocation: Map<String, Int>,
    
    @SerializedName("applications_by_job_type")
    val applicationsByJobType: Map<JobType, Int>,
    
    @SerializedName("success_rate")
    val successRate: Double, // Percentage of applications that led to interviews or offers
    
    @SerializedName("average_response_time")
    val averageResponseTime: Double, // Average days between application and first response
    
    @SerializedName("most_applied_companies")
    val mostAppliedCompanies: List<Pair<String, Int>>,
    
    @SerializedName("interview_conversion_rate")
    val interviewConversionRate: Double,
    
    @SerializedName("offer_conversion_rate")
    val offerConversionRate: Double
)

/**
 * Data class for bulk operations on application tracking
 */
data class ApplicationTrackingBulkRequest(
    @SerializedName("application_ids")
    val applicationIds: List<String>,
    
    @SerializedName("action")
    val action: ApplicationTrackingBulkAction
)

/**
 * Enum for bulk actions on application tracking
 */
enum class ApplicationTrackingBulkAction {
    @SerializedName("update_priority")
    UPDATE_PRIORITY,
    
    @SerializedName("update_tags")
    UPDATE_TAGS,
    
    @SerializedName("set_follow_up")
    SET_FOLLOW_UP,
    
    @SerializedName("remove_follow_up")
    REMOVE_FOLLOW_UP,
    
    @SerializedName("add_notes")
    ADD_NOTES,
    
    @SerializedName("mark_as_rejected")
    MARK_AS_REJECTED,
    
    @SerializedName("mark_as_withdrawn")
    MARK_AS_WITHDRAWN
}

/**
 * Data class for bulk update request
 */
data class ApplicationTrackingBulkUpdateRequest(
    @SerializedName("application_ids")
    val applicationIds: List<String>,
    
    @SerializedName("priority")
    val priority: ApplicationPriority? = null,
    
    @SerializedName("tags")
    val tags: List<String>? = null,
    
    @SerializedName("follow_up_date")
    val followUpDate: String? = null,
    
    @SerializedName("notes")
    val notes: String? = null
)

/**
 * Data class for application tracking reminders
 */
data class ApplicationTrackingReminder(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("application_tracking_id")
    val applicationTrackingId: String,
    
    @SerializedName("user_id")
    val userId: String,
    
    @SerializedName("reminder_type")
    val reminderType: ReminderType,
    
    @SerializedName("reminder_date")
    val reminderDate: String,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("is_sent")
    val isSent: Boolean = false,
    
    @SerializedName("created_at")
    val createdAt: String
)

/**
 * Enum for reminder types
 */
enum class ReminderType {
    @SerializedName("follow_up")
    FOLLOW_UP,
    
    @SerializedName("interview")
    INTERVIEW,
    
    @SerializedName("response")
    RESPONSE,
    
    @SerializedName("deadline")
    DEADLINE
}
