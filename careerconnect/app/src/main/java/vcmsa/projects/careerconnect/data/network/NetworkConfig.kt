//CODE ATTRIBUTION
//01
//Kotlin object declarations
//Adapted from: Kotlin Docs. (2025). Object declarations. [online]
//Available at: https://kotlinlang.org/docs/object-declarations.html
//Date Accessed: 01 October 2025

//02
//Kotlin constants (const val)
//Adapted from: Kotlin Docs. (2025). Compile-time constants. [online]
//Available at: https://kotlinlang.org/docs/properties.html#compile-time-constants
//Date Accessed: 01 October 2025

//03
//Kotlin functions
//Adapted from: Kotlin Docs. (2025). Functions. [online]
//Available at: https://kotlinlang.org/docs/functions.html
//Date Accessed: 01 October 2025

//04
//Kotlin basic types (String, Long)
//Adapted from: Kotlin Docs. (2025). Basic types. [online]
//Available at: https://kotlinlang.org/docs/basic-types.html
//Date Accessed: 01 October 2025

package vcmsa.projects.careerconnect.data.network

/**
 * Network configuration constants and utilities
 */
object NetworkConfig {
    
    // API Configuration
    const val BASE_URL = "https://careerconnectapi2.onrender.com/api/"
    const val TIMEOUT_SECONDS = 30L
    const val MAX_RETRY_ATTEMPTS = 3
    
    // Headers
    const val HEADER_CONTENT_TYPE = "Content-Type"
    const val HEADER_ACCEPT = "Accept"
    const val HEADER_AUTHORIZATION = "Authorization"
    const val HEADER_USER_AGENT = "User-Agent"
    const val HEADER_APP_VERSION = "X-App-Version"
    const val HEADER_PLATFORM = "X-Platform"
    
    // Content Types
    const val CONTENT_TYPE_JSON = "application/json"
    
    // Platform
    const val PLATFORM_ANDROID = "android"
    
    /**
     * Gets the user agent string for requests
     */
    fun getUserAgent(): String {
        return "CareerConnect-Android/1.0"
    }
    
    /**
     * Checks if debug logging is enabled
     */
    fun isDebugLoggingEnabled(): Boolean {
        return true // Enable debug logging by default
    }
    
    /**
     * Gets the app version for headers
     */
    fun getAppVersion(): String {
        return "1.0"
    }
    
    /**
     * Gets the API timeout in milliseconds
     */
    fun getTimeoutMillis(): Long {
        return TIMEOUT_SECONDS * 1000
    }
}