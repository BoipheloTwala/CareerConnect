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
 * Data class representing a saved/bookmarked job
 */
data class SavedJob(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("job_id")
    val jobId: String,
    
    @SerializedName("user_uid")
    val userUid: String, // Job seeker's Firebase UID
    
    @SerializedName("notes")
    val notes: String? = null, // Optional user notes about the job
    
    @SerializedName("tags")
    val tags: List<String> = emptyList(), // User-defined tags for organization
    
    @SerializedName("priority")
    val priority: SavedJobPriority = SavedJobPriority.MEDIUM,
    
    @SerializedName("reminder_date")
    val reminderDate: String? = null, // Optional reminder date
    
    @SerializedName("is_reminder_set")
    val isReminderSet: Boolean = false,
    
    @SerializedName("created_at")
    val createdAt: String, // ISO 8601 timestamp
    
    @SerializedName("updated_at")
    val updatedAt: String,
    
    // Job details (populated when fetching saved jobs)
    @SerializedName(value = "job", alternate = ["job_postings"])
    val job: Job? = null
)

/**
 * Enum for saved job priority levels
 */
enum class SavedJobPriority {
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
 * Data class for saving a job request
 */
data class SaveJobRequest(
    @SerializedName("job_id")
    val jobId: String,
    
    @SerializedName("notes")
    val notes: String? = null,
    
    @SerializedName("tags")
    val tags: List<String> = emptyList(),
    
    @SerializedName("priority")
    val priority: SavedJobPriority = SavedJobPriority.MEDIUM,
    
    @SerializedName("reminder_date")
    val reminderDate: String? = null
)

/**
 * Data class for updating a saved job
 */
data class UpdateSavedJobRequest(
    @SerializedName("notes")
    val notes: String? = null,
    
    @SerializedName("tags")
    val tags: List<String>? = null,
    
    @SerializedName("priority")
    val priority: SavedJobPriority? = null,
    
    @SerializedName("reminder_date")
    val reminderDate: String? = null,
    
    @SerializedName("is_reminder_set")
    val isReminderSet: Boolean? = null
)

/**
 * Data class for saved jobs search request
 */
data class SavedJobsSearchRequest(
    @SerializedName("query")
    val query: String? = null,
    
    @SerializedName("tags")
    val tags: List<String>? = null,
    
    @SerializedName("priority")
    val priority: SavedJobPriority? = null,
    
    @SerializedName("company_name")
    val companyName: String? = null,
    
    @SerializedName("location")
    val location: String? = null,
    
    @SerializedName("job_type")
    val jobType: JobType? = null,
    
    @SerializedName("experience_level")
    val experienceLevel: ExperienceLevel? = null,
    
    @SerializedName("has_reminder")
    val hasReminder: Boolean? = null,
    
    @SerializedName("saved_after")
    val savedAfter: String? = null, // ISO 8601 date string
    
    @SerializedName("saved_before")
    val savedBefore: String? = null, // ISO 8601 date string
    
    @SerializedName("sort_by")
    val sortBy: SavedJobSortBy = SavedJobSortBy.SAVED_DATE,
    
    @SerializedName("sort_order")
    val sortOrder: SortOrder = SortOrder.DESC
)

/**
 * Enum for saved jobs sorting options
 */
enum class SavedJobSortBy {
    @SerializedName("saved_date")
    SAVED_DATE,
    
    @SerializedName("priority")
    PRIORITY,
    
    @SerializedName("job_title")
    JOB_TITLE,
    
    @SerializedName("company_name")
    COMPANY_NAME,
    
    @SerializedName("reminder_date")
    REMINDER_DATE
}

/**
 * Data class for saved jobs search response
 */
data class SavedJobsSearchResponse(
    @SerializedName("saved_jobs")
    val savedJobs: List<SavedJob>,
    
    @SerializedName("pagination")
    val pagination: PaginationMeta,
    
    @SerializedName("total_count")
    val totalCount: Int,
    
    @SerializedName("filters_applied")
    val filtersApplied: SavedJobsSearchRequest
)

/**
 * Data class for bulk operations on saved jobs
 */
data class SavedJobsBulkRequest(
    @SerializedName("saved_job_ids")
    val savedJobIds: List<String>,
    
    @SerializedName("action")
    val action: SavedJobsBulkAction
)

/**
 * Enum for bulk actions on saved jobs
 */
enum class SavedJobsBulkAction {
    @SerializedName("delete")
    DELETE,
    
    @SerializedName("update_priority")
    UPDATE_PRIORITY,
    
    @SerializedName("update_tags")
    UPDATE_TAGS,
    
    @SerializedName("set_reminder")
    SET_REMINDER,
    
    @SerializedName("remove_reminder")
    REMOVE_REMINDER
}

/**
 * Data class for bulk update request
 */
data class SavedJobsBulkUpdateRequest(
    @SerializedName("saved_job_ids")
    val savedJobIds: List<String>,
    
    @SerializedName("priority")
    val priority: SavedJobPriority? = null,
    
    @SerializedName("tags")
    val tags: List<String>? = null,
    
    @SerializedName("reminder_date")
    val reminderDate: String? = null
)

/**
 * Data class for saved jobs analytics
 */
data class SavedJobsAnalytics(
    @SerializedName("total_saved_jobs")
    val totalSavedJobs: Int,
    
    @SerializedName("jobs_by_priority")
    val jobsByPriority: Map<SavedJobPriority, Int>,
    
    @SerializedName("jobs_by_company")
    val jobsByCompany: Map<String, Int>,
    
    @SerializedName("jobs_by_location")
    val jobsByLocation: Map<String, Int>,
    
    @SerializedName("jobs_by_job_type")
    val jobsByJobType: Map<JobType, Int>,
    
    @SerializedName("most_used_tags")
    val mostUsedTags: List<Pair<String, Int>>,
    
    @SerializedName("jobs_with_reminders")
    val jobsWithReminders: Int,
    
    @SerializedName("recent_saves")
    val recentSaves: List<SavedJob>
)
