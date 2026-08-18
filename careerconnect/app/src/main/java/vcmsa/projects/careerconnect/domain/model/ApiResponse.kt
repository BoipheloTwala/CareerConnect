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
 * Generic API response wrapper
 */
data class ApiResponse<T>(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("data")
    val data: T? = null,
    
    @SerializedName("message")
    val message: String? = null,
    
    @SerializedName("error")
    val error: ErrorResponse? = null
)

/**
 * Error response from the API
 */
data class ErrorResponse(
    @SerializedName("code")
    val code: String? = null,
    
    @SerializedName("message")
    val message: String? = null,
    
    @SerializedName("details")
    val details: String? = null,
    
    @SerializedName("field")
    val field: String? = null
)

/**
 * Pagination metadata for list responses
 */
data class PaginationMeta(
    @SerializedName("page")
    val page: Int,
    
    @SerializedName("limit")
    val limit: Int,
    
    @SerializedName("total")
    val total: Int,
    
    @SerializedName("totalPages")
    val totalPages: Int,
    
    @SerializedName("hasNext")
    val hasNext: Boolean,
    
    @SerializedName("hasPrev")
    val hasPrev: Boolean
)

/**
 * Paginated API response wrapper
 */
data class PaginatedResponse<T>(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("data")
    val data: List<T>? = null,
    
    @SerializedName("pagination")
    val pagination: PaginationMeta? = null,
    
    @SerializedName("message")
    val message: String? = null,
    
    @SerializedName("error")
    val error: ErrorResponse? = null
)