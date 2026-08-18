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
 * Enum for CV file types
 */
enum class CVFileType {
    @SerializedName("pdf")
    PDF,
    
    @SerializedName("doc")
    DOC,
    
    @SerializedName("docx")
    DOCX,
    
    @SerializedName("txt")
    TXT
}

/**
 * Enum for CV status
 */
enum class CVStatus {
    @SerializedName("active")
    ACTIVE,
    
    @SerializedName("archived")
    ARCHIVED,
    
    @SerializedName("deleted")
    DELETED
}

/**
 * Data class representing a CV
 */
data class CV(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("user_uid")
    val userUid: String, // Firebase UID
    
    @SerializedName("title")
    val title: String,
    
    @SerializedName("description")
    val description: String? = null,
    
    @SerializedName("file_name")
    val fileName: String,
    
    @SerializedName("file_type")
    val fileType: CVFileType,
    
    @SerializedName("file_size")
    val fileSize: Long, // Size in bytes
    
    @SerializedName("file_url")
    val fileUrl: String, // Cloudinary URL
    
    @SerializedName("thumbnail_url")
    val thumbnailUrl: String? = null, // For PDF preview
    
    @SerializedName("is_primary")
    val isPrimary: Boolean = false,
    
    @SerializedName("status")
    val status: CVStatus = CVStatus.ACTIVE,
    
    @SerializedName("created_at")
    val createdAt: String,
    
    @SerializedName("updated_at")
    val updatedAt: String,
    
    @SerializedName("last_used")
    val lastUsed: String? = null, // Last time used in application
    
    @SerializedName("download_count")
    val downloadCount: Int = 0,
    
    @SerializedName("version")
    val version: Int = 1,
    
    @SerializedName("tags")
    val tags: List<String> = emptyList()
)

/**
 * Data class for CV upload request
 */
data class CVUploadRequest(
    @SerializedName("title")
    val title: String,
    
    @SerializedName("description")
    val description: String? = null,
    
    @SerializedName("file_type")
    val fileType: CVFileType,
    
    @SerializedName("file_size")
    val fileSize: Long,
    
    @SerializedName("file_url")
    val fileUrl: String,
    
    @SerializedName("thumbnail_url")
    val thumbnailUrl: String? = null,
    
    @SerializedName("is_primary")
    val isPrimary: Boolean = false,
    
    @SerializedName("tags")
    val tags: List<String> = emptyList()
)

/**
 * Request matching backend /cvs create schema
 */
data class CVCreateRequest(
    @SerializedName("file_name")
    val fileName: String,
    
    @SerializedName("file_url")
    val fileUrl: String,
    
    @SerializedName("file_size")
    val fileSize: Long? = null,
    
    @SerializedName("is_primary")
    val isPrimary: Boolean = false
)

/**
 * Data class for CV update request
 */
data class CVUpdateRequest(
    @SerializedName("title")
    val title: String? = null,
    
    @SerializedName("description")
    val description: String? = null,
    
    @SerializedName("is_primary")
    val isPrimary: Boolean? = null,
    
    @SerializedName("status")
    val status: CVStatus? = null,
    
    @SerializedName("tags")
    val tags: List<String>? = null
)

/**
 * Data class for CV upload response
 */
data class CVUploadResponse(
    @SerializedName("cv")
    val cv: CV,
    
    @SerializedName("upload_url")
    val uploadUrl: String? = null, // For direct upload to Cloudinary
    
    @SerializedName("upload_token")
    val uploadToken: String? = null // For secure upload
)

/**
 * Data class for CV list response
 */
data class CVListResponse(
    @SerializedName("cvs")
    val cvs: List<CV>,
    
    @SerializedName("total_count")
    val totalCount: Int,
    
    @SerializedName("primary_cv")
    val primaryCV: CV? = null
)

/**
 * Data class for CV search request
 */
data class CVSearchRequest(
    @SerializedName("query")
    val query: String? = null,
    
    @SerializedName("file_types")
    val fileTypes: List<CVFileType>? = null,
    
    @SerializedName("status")
    val status: CVStatus? = null,
    
    @SerializedName("is_primary")
    val isPrimary: Boolean? = null,
    
    @SerializedName("tags")
    val tags: List<String>? = null,
    
    @SerializedName("created_after")
    val createdAfter: String? = null,
    
    @SerializedName("created_before")
    val createdBefore: String? = null,
    
    @SerializedName("sort_by")
    val sortBy: CVSortBy = CVSortBy.CREATED_AT,
    
    @SerializedName("sort_order")
    val sortOrder: SortOrder = SortOrder.DESC
)

/**
 * Enum for CV sorting options
 */
enum class CVSortBy {
    @SerializedName("created_at")
    CREATED_AT,
    
    @SerializedName("updated_at")
    UPDATED_AT,
    
    @SerializedName("title")
    TITLE,
    
    @SerializedName("file_size")
    FILE_SIZE,
    
    @SerializedName("last_used")
    LAST_USED,
    
    @SerializedName("download_count")
    DOWNLOAD_COUNT
}

/**
 * Data class for CV analytics
 */
data class CVAnalytics(
    @SerializedName("total_cvs")
    val totalCVs: Int,
    
    @SerializedName("total_size")
    val totalSize: Long, // Total size in bytes
    
    @SerializedName("primary_cv")
    val primaryCV: CV? = null,
    
    @SerializedName("most_used_cv")
    val mostUsedCV: CV? = null,
    
    @SerializedName("recent_uploads")
    val recentUploads: List<CV>,
    
    @SerializedName("storage_usage")
    val storageUsage: StorageUsage
)

/**
 * Data class for storage usage information
 */
data class StorageUsage(
    @SerializedName("used_bytes")
    val usedBytes: Long,
    
    @SerializedName("max_bytes")
    val maxBytes: Long,
    
    @SerializedName("usage_percentage")
    val usagePercentage: Double,
    
    @SerializedName("remaining_bytes")
    val remainingBytes: Long
)

/**
 * Data class for CV sharing
 */
data class CVShareRequest(
    @SerializedName("cv_id")
    val cvId: String,
    
    @SerializedName("recipient_email")
    val recipientEmail: String,
    
    @SerializedName("message")
    val message: String? = null,
    
    @SerializedName("expires_at")
    val expiresAt: String? = null // ISO 8601 date
)


/**
 * Data class for CV download request
 */
data class CVDownloadRequest(
    @SerializedName("cv_id")
    val cvId: String,
    
    @SerializedName("download_type")
    val downloadType: DownloadType = DownloadType.ORIGINAL
)

/**
 * Enum for download types
 */
enum class DownloadType {
    @SerializedName("original")
    ORIGINAL,
    
    @SerializedName("thumbnail")
    THUMBNAIL,
    
    @SerializedName("preview")
    PREVIEW
}

/**
 * Data class for CV download response
 */
data class CVDownloadResponse(
    @SerializedName("download_url")
    val downloadUrl: String,
    
    @SerializedName("expires_at")
    val expiresAt: String,
    
    @SerializedName("file_name")
    val fileName: String,
    
    @SerializedName("file_size")
    val fileSize: Long
)

/**
 * Data class for CV upload URL request
 */
data class CVUploadUrlRequest(
    @SerializedName("file_name")
    val fileName: String,
    
    @SerializedName("file_type")
    val fileType: CVFileType,
    
    @SerializedName("file_size")
    val fileSize: Long,
    
    @SerializedName("title")
    val title: String
)

/**
 * Data class for CV upload URL response
 */
data class CVUploadUrlResponse(
    @SerializedName("upload_url")
    val uploadUrl: String,
    
    @SerializedName("upload_token")
    val uploadToken: String,
    
    @SerializedName("cv_id")
    val cvId: String,
    
    @SerializedName("expires_at")
    val expiresAt: String
)

/**
 * Data class for CV upload confirmation request
 */
data class CVUploadConfirmationRequest(
    @SerializedName("file_url")
    val fileUrl: String,
    
    @SerializedName("thumbnail_url")
    val thumbnailUrl: String? = null,
    
    @SerializedName("file_size")
    val fileSize: Long
)

/**
 * Data class for CV bulk delete request
 */
data class CVBulkDeleteRequest(
    @SerializedName("cv_ids")
    val cvIds: List<String>
)

/**
 * Data class for CV bulk delete response
 */
data class CVBulkDeleteResponse(
    @SerializedName("deleted_count")
    val deletedCount: Int,
    
    @SerializedName("failed_count")
    val failedCount: Int,
    
    @SerializedName("failed_cv_ids")
    val failedCvIds: List<String>
)

/**
 * Data class for CV bulk status update request
 */
data class CVBulkStatusUpdateRequest(
    @SerializedName("cv_ids")
    val cvIds: List<String>,
    
    @SerializedName("status")
    val status: CVStatus
)

/**
 * Data class for CV bulk status update response
 */
data class CVBulkStatusUpdateResponse(
    @SerializedName("updated_count")
    val updatedCount: Int,
    
    @SerializedName("failed_count")
    val failedCount: Int,
    
    @SerializedName("failed_cv_ids")
    val failedCvIds: List<String>
)

/**
 * Enum for export formats
 */
enum class ExportFormat {
    @SerializedName("json")
    JSON,
    
    @SerializedName("csv")
    CSV,
    
    @SerializedName("xlsx")
    XLSX
}
