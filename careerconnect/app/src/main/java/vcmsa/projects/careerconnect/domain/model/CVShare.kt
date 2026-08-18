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
 * Data class representing a CV share
 */
data class CVShare(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("cv_id")
    val cvId: String,
    
    @SerializedName("user_uid")
    val userUid: String, // Owner's Firebase UID
    
    @SerializedName("share_token")
    val shareToken: String,
    
    @SerializedName("recipient_email")
    val recipientEmail: String,
    
    @SerializedName("message")
    val message: String? = null,
    
    @SerializedName("expires_at")
    val expiresAt: String? = null, // ISO 8601 timestamp
    
    @SerializedName("is_active")
    val isActive: Boolean = true,
    
    @SerializedName("created_at")
    val createdAt: String
)

/**
 * Data class for creating a CV share request
 */
data class CreateCVShareRequest(
    @SerializedName("cv_id")
    val cvId: String,
    
    @SerializedName("recipient_email")
    val recipientEmail: String,
    
    @SerializedName("message")
    val message: String? = null,
    
    @SerializedName("expires_at")
    val expiresAt: String? = null // ISO 8601 timestamp
)

/**
 * Data class for CV share response
 */
data class CVShareResponse(
    @SerializedName("share_url")
    val shareUrl: String,
    
    @SerializedName("share_token")
    val shareToken: String,
    
    @SerializedName("expires_at")
    val expiresAt: String
)

/**
 * Data class for CV download analytics
 */
data class CVDownloadAnalytics(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("cv_id")
    val cvId: String,
    
    @SerializedName("user_uid")
    val userUid: String,
    
    @SerializedName("download_type")
    val downloadType: DownloadType,
    
    @SerializedName("downloaded_by")
    val downloadedBy: String? = null,
    
    @SerializedName("download_timestamp")
    val downloadTimestamp: String,
    
    @SerializedName("ip_address")
    val ipAddress: String? = null,
    
    @SerializedName("user_agent")
    val userAgent: String? = null
)

/**
 * Data class for application status history
 */
data class ApplicationStatusHistory(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("application_id")
    val applicationId: String,
    
    @SerializedName("old_status")
    val oldStatus: String? = null,
    
    @SerializedName("new_status")
    val newStatus: String,
    
    @SerializedName("changed_by")
    val changedBy: String? = null,
    
    @SerializedName("change_reason")
    val changeReason: String? = null,
    
    @SerializedName("notes")
    val notes: String? = null,
    
    @SerializedName("changed_at")
    val changedAt: String
)

/**
 * Data class for job search analytics
 */
data class JobSearchAnalytics(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("user_uid")
    val userUid: String,
    
    @SerializedName("search_query")
    val searchQuery: String? = null,
    
    @SerializedName("filters_applied")
    val filtersApplied: Map<String, Any>? = null,
    
    @SerializedName("results_count")
    val resultsCount: Int,
    
    @SerializedName("search_timestamp")
    val searchTimestamp: String,
    
    @SerializedName("session_id")
    val sessionId: String? = null
)
