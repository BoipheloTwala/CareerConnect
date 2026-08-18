//CODE ATTRIBUTION
//01
//Kotlin sealed classes
//Adapted from: Kotlin Docs. (2025). Sealed classes. [online]
//Available at: https://kotlinlang.org/docs/sealed-classes.html
//Date Accessed: 01 October 2025

//02
//Kotlin exceptions
//Adapted from: Kotlin Docs. (2025). Exceptions. [online]
//Available at: https://kotlinlang.org/docs/exceptions.html
//Date Accessed: 01 October 2025

//03
//Kotlin when expression
//Adapted from: Kotlin Docs. (2025). Control flow: when expression. [online]
//Available at: https://kotlinlang.org/docs/control-flow.html#when-expression
//Date Accessed: 01 October 2025

//04
//Kotlin extension functions
//Adapted from: Kotlin Docs. (2025). Extensions. [online]
//Available at: https://kotlinlang.org/docs/extensions.html
//Date Accessed: 01 October 2025

package vcmsa.projects.careerconnect.data.network

import vcmsa.projects.careerconnect.domain.model.ErrorResponse

/**
 * Base class for API exceptions
 */
sealed class ApiException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    
    /**
     * Unauthorized (401) - Authentication failed
     */
    class Unauthorized(message: String = "Authentication failed") : ApiException(message)
    
    /**
     * Forbidden (403) - Access denied
     */
    class Forbidden(message: String = "Access denied") : ApiException(message)
    
    /**
     * Not Found (404) - Resource not found
     */
    class NotFound(message: String = "Resource not found") : ApiException(message)
    
    /**
     * Conflict (409) - Resource already exists or state conflict
     */
    class Conflict(message: String = "Resource conflict") : ApiException(message)
    
    /**
     * Validation Error (422) - Invalid request data
     */
    class ValidationError(
        message: String = "Validation failed",
        val errors: List<ErrorResponse>? = null
    ) : ApiException(message)
    
    /**
     * Too Many Requests (429) - Rate limit exceeded
     */
    class TooManyRequests(message: String = "Too many requests") : ApiException(message)
    
    /**
     * Internal Server Error (500) - Server error
     */
    class InternalServerError(message: String = "Internal server error") : ApiException(message)
    
    /**
     * Server Unavailable (502, 503, 504) - Service unavailable
     */
    class ServerUnavailable(message: String = "Service unavailable") : ApiException(message)
    
    /**
     * Network Error - Connection issues
     */
    class NetworkError(message: String = "Network error", cause: Throwable? = null) : ApiException(message, cause)
    
    /**
     * Timeout Error - Request timeout
     */
    class TimeoutError(message: String = "Request timeout") : ApiException(message)
    
    /**
     * Unknown Error - Unexpected error
     */
    class UnknownError(message: String = "Unknown error occurred", cause: Throwable? = null) : ApiException(message, cause)
    
    /**
     * Parse Error - JSON parsing failed
     */
    class ParseError(message: String = "Failed to parse response", cause: Throwable? = null) : ApiException(message, cause)
}

/**
 * Extension function to convert HTTP status code to ApiException
 */
fun Int.toApiException(message: String? = null): ApiException {
    return when (this) {
        401 -> ApiException.Unauthorized(message ?: "Authentication failed")
        403 -> ApiException.Forbidden(message ?: "Access denied")
        404 -> ApiException.NotFound(message ?: "Resource not found")
        409 -> ApiException.Conflict(message ?: "Resource conflict")
        422 -> ApiException.ValidationError(message ?: "Validation failed")
        429 -> ApiException.TooManyRequests(message ?: "Too many requests")
        500 -> ApiException.InternalServerError(message ?: "Internal server error")
        502, 503, 504 -> ApiException.ServerUnavailable(message ?: "Service unavailable")
        else -> ApiException.UnknownError(message ?: "HTTP Error: $this")
    }
}

/**
 * Extension function to get user-friendly error message from ApiException
 */
fun ApiException.getUserFriendlyMessage(): String {
    return when (this) {
        is ApiException.Unauthorized -> "Please login again to continue"
        is ApiException.Forbidden -> "You don't have permission to perform this action"
        is ApiException.NotFound -> "The requested information was not found"
        is ApiException.Conflict -> message ?: "This resource already exists"
        is ApiException.ValidationError -> message ?: "Validation failed"
        is ApiException.TooManyRequests -> "Please wait a moment and try again"
        is ApiException.InternalServerError -> "Something went wrong on our end. Please try again"
        is ApiException.ServerUnavailable -> "Service is temporarily unavailable. Please try again later"
        is ApiException.NetworkError -> "Please check your internet connection"
        is ApiException.TimeoutError -> "Request timed out. Please try again"
        is ApiException.ParseError -> "Failed to process server response"
        is ApiException.UnknownError -> "An unexpected error occurred. Please try again"
    }
}

/**
 * Extension function to check if error is retryable
 */
fun ApiException.isRetryable(): Boolean {
    return when (this) {
        is ApiException.Unauthorized,
        is ApiException.Forbidden,
        is ApiException.NotFound,
        is ApiException.Conflict,
        is ApiException.ValidationError,
        is ApiException.TooManyRequests -> false
        is ApiException.InternalServerError,
        is ApiException.ServerUnavailable,
        is ApiException.NetworkError,
        is ApiException.TimeoutError,
        is ApiException.UnknownError,
        is ApiException.ParseError -> true
    }
}