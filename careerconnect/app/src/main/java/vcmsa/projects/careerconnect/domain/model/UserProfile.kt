//CODE ATTRIBUTION
//01
//Kotlin enums
//Adapted from: Kotlin Docs. (2025). Enum classes. [online]
//Available at: https://kotlinlang.org/docs/enum-classes.html
//Date Accessed: 30 September 2025

//02
//Kotlin data classes
//Adapted from: Kotlin Docs. (2025). Data classes. [online]
//Available at: https://kotlinlang.org/docs/data-classes.html
//Date Accessed: 30 September 2025

//03
//Serialized GSON Names
//Adapted from: Gson Javadoc. (2025). SerializedName. [online]
//Available at: https://javadoc.io/doc/com.google.code.gson/gson/latest/com/google/gson/annotations/SerializedName.html
//Date Accessed: 30 September 2025

package vcmsa.projects.careerconnect.domain.model

import com.google.gson.annotations.SerializedName

/**
 * Enum for user types
 */
enum class UserType {
    @SerializedName("job_seeker")
    JOB_SEEKER,
    
    @SerializedName("recruiter")
    RECRUITER
}

/**
 * Data class representing a user profile
 */
data class UserProfile(
    @SerializedName("firebase_uid")
    val firebaseUid: String,
    
    @SerializedName("user_type")
    val userType: UserType,
    
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
    
    @SerializedName("company_name")
    val companyName: String? = null, // For recruiters
    
    @SerializedName("bio")
    val bio: String? = null,
    
    @SerializedName("profile_image_url")
    val profileImageUrl: String? = null,
    
    @SerializedName("created_at")
    val createdAt: String? = null,
    
    @SerializedName("updated_at")
    val updatedAt: String? = null
)

/**
 * Request data class for creating a user profile
 */
data class CreateProfileRequest(
    @SerializedName("user_type")
    val userType: UserType,
    
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
    
    @SerializedName("company_name")
    val companyName: String? = null, // For recruiters
    
    @SerializedName("bio")
    val bio: String? = null,
    
    @SerializedName("profile_image_url")
    val profileImageUrl: String? = null
)

/**
 * Request data class for updating a user profile
 */
data class UpdateProfileRequest(
    @SerializedName("first_name")
    val firstName: String? = null,
    
    @SerializedName("last_name")
    val lastName: String? = null,
    
    @SerializedName("phone")
    val phone: String? = null,
    
    @SerializedName("location")
    val location: String? = null,
    
    @SerializedName("company_name")
    val companyName: String? = null, // For recruiters
    
    @SerializedName("bio")
    val bio: String? = null,
    
    @SerializedName("profile_image_url")
    val profileImageUrl: String? = null
)
