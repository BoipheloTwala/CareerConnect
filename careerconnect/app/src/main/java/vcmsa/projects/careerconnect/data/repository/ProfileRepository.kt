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
 * Repository class for handling user profile operations
 */
class ProfileRepository {
    
    private val apiService = NetworkModule.apiService
    
    /**
     * Create a new user profile
     */
    suspend fun createProfile(request: CreateProfileRequest): Result<UserProfile> {
        // Validate request before making API call
        val validationResult = ProfileValidator.validateCreateRequest(request)
        if (!validationResult.isValid()) {
            val errors = validationResult.getErrors().joinToString(", ") { "${it.field}: ${it.message}" }
            return Result.failure(Exception("Validation failed: $errors"))
        }
        
        return safeApiCall(
            apiCall = { apiService.createProfile(request) },
            errorHandler = { ApiErrorHandler.logError("ProfileRepository", it, "Create Profile") }
        )
    }
    
    /**
     * Get the current user's profile
     */
    suspend fun getProfile(): Result<UserProfile> {
        return safeApiCall(
            apiCall = { apiService.getProfile() },
            errorHandler = { ApiErrorHandler.logError("ProfileRepository", it, "Get Profile") }
        )
    }
    
    /**
     * Update the current user's profile
     */
    suspend fun updateProfile(request: UpdateProfileRequest): Result<UserProfile> {
        // Validate request before making API call
        val validationResult = ProfileValidator.validateUpdateRequest(request)
        if (!validationResult.isValid()) {
            val errors = validationResult.getErrors().joinToString(", ") { "${it.field}: ${it.message}" }
            return Result.failure(Exception("Validation failed: $errors"))
        }
        
        return safeApiCall(
            apiCall = { apiService.updateProfile(request) },
            errorHandler = { ApiErrorHandler.logError("ProfileRepository", it, "Update Profile") }
        )
    }
    
    /**
     * Get profile with NetworkResult for UI state management
     */
    suspend fun getProfileAsNetworkResult(): NetworkResult<UserProfile> {
        return getProfile().toNetworkResult()
    }
    
    /**
     * Create profile with NetworkResult for UI state management
     */
    suspend fun createProfileAsNetworkResult(request: CreateProfileRequest): NetworkResult<UserProfile> {
        return createProfile(request).toNetworkResult()
    }
    
    /**
     * Update profile with NetworkResult for UI state management
     */
    suspend fun updateProfileAsNetworkResult(request: UpdateProfileRequest): NetworkResult<UserProfile> {
        return updateProfile(request).toNetworkResult()
    }
    
    /**
     * Create profile with custom error handling
     */
    suspend fun createProfileWithErrorHandling(
        request: CreateProfileRequest,
        onError: (String) -> Unit
    ): Result<UserProfile> {
        return createProfile(request).also { result ->
            result.onFailure { throwable ->
                val errorMessage = ApiErrorHandler.getErrorMessage(throwable)
                onError(errorMessage)
            }
        }
    }
    
    /**
     * Update profile with custom error handling
     */
    suspend fun updateProfileWithErrorHandling(
        request: UpdateProfileRequest,
        onError: (String) -> Unit
    ): Result<UserProfile> {
        return updateProfile(request).also { result ->
            result.onFailure { throwable ->
                val errorMessage = ApiErrorHandler.getErrorMessage(throwable)
                onError(errorMessage)
            }
        }
    }
}