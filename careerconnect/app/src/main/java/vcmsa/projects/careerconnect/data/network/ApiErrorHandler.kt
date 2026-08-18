//CODE ATTRIBUTION
//01
//Retrofit Declarations (request method, URL, body, headers)
//Adapted from: Square. (2025). Retrofit Declarations. [online]
//Available at: https://square.github.io/retrofit/declarations/#request-method
//Date Accessed: 01 October 2025


//CODE ATTRIBUTION
//01
//Gson (Gson)
//Adapted from: Gson Javadoc. (2025). Gson. [online]
//Available at: https://javadoc.io/doc/com.google.code.gson/gson/latest/com/google/gson/Gson.html
//Date Accessed: 01 October 2025

//02
//Gson (JsonSyntaxException)
//Adapted from: Gson Javadoc. (2025). JsonSyntaxException. [online]
//Available at: https://javadoc.io/doc/com.google.code.gson/gson/latest/com/google/gson/JsonSyntaxException.html
//Date Accessed: 01 October 2025

//03
//Retrofit HttpException
//Adapted from: Square. (2025). retrofit2.HttpException. [online]
//Available at: https://square.github.io/retrofit/2.x/retrofit/retrofit2/HttpException.html
//Date Accessed: 01 October 2025

package vcmsa.projects.careerconnect.data.network

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import okhttp3.ResponseBody
import retrofit2.HttpException
import vcmsa.projects.careerconnect.domain.model.ErrorResponse
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Utility class for handling API errors
 */
object ApiErrorHandler {
    
    private val gson = Gson()
    
    /**
     * Converts various exceptions to ApiException
     */
    fun handleException(throwable: Throwable): ApiException {
        return when (throwable) {
            is ApiException -> throwable
            is HttpException -> handleHttpException(throwable)
            is SocketTimeoutException -> ApiException.TimeoutError("Request timed out")
            is UnknownHostException -> ApiException.NetworkError("No internet connection")
            is IOException -> ApiException.NetworkError("Network error: ${throwable.message}", throwable)
            is JsonSyntaxException -> ApiException.ParseError("Failed to parse server response", throwable)
            else -> ApiException.UnknownError("Unexpected error: ${throwable.message}", throwable)
        }
    }
    
    /**
     * Handles HTTP exceptions from Retrofit
     */
    private fun handleHttpException(httpException: HttpException): ApiException {
        val code = httpException.code()
        val message = try {
            val errorBody = httpException.response()?.errorBody()
            val errorResponse = parseErrorResponse(errorBody)
            errorResponse?.message ?: httpException.message()
        } catch (e: Exception) {
            httpException.message()
        }
        
        return code.toApiException(message)
    }
    
    /**
     * Parses error response from server
     */
    private fun parseErrorResponse(errorBody: ResponseBody?): ErrorResponse? {
        return try {
            errorBody?.let { body ->
                val jsonString = body.string()
                gson.fromJson(jsonString, ErrorResponse::class.java)
            }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Parses API response error
     */
    fun parseApiResponseError(response: retrofit2.Response<*>): ApiException {
        return if (response.isSuccessful) {
            // When successful but body missing or not as expected
            if (response.body() == null) {
                ApiException.UnknownError("Empty response body")
            } else {
                ApiException.UnknownError("Unexpected successful response")
            }
        } else {
            // Not successful: try to parse server error body for a better message
            try {
                val errorBodyString = response.errorBody()?.string()
                if (!errorBodyString.isNullOrBlank()) {
                    val parsed = try { gson.fromJson(errorBodyString, ErrorResponse::class.java) } catch (_: Exception) { null }
                    val message = parsed?.message ?: errorBodyString
                    return when (response.code()) {
                        401 -> ApiException.Unauthorized(message)
                        403 -> ApiException.Forbidden(message)
                        404 -> ApiException.NotFound(message)
                        409 -> ApiException.Conflict(message)
                        422 -> ApiException.ValidationError(message, parsed?.let { listOf(it) })
                        429 -> ApiException.TooManyRequests(message)
                        500 -> ApiException.InternalServerError(message)
                        502, 503, 504 -> ApiException.ServerUnavailable(message)
                        else -> ApiException.UnknownError(message)
                    }
                }
            } catch (_: Exception) {
                // ignore and fall back below
            }
            response.code().toApiException(response.message())
        }
    }
    
    /**
     * Creates a user-friendly error message
     */
    fun getErrorMessage(throwable: Throwable): String {
        val apiException = handleException(throwable)
        return apiException.getUserFriendlyMessage()
    }
    
    /**
     * Checks if the error is retryable
     */
    fun isRetryable(throwable: Throwable): Boolean {
        val apiException = handleException(throwable)
        return apiException.isRetryable()
    }
    
    /**
     * Logs error details for debugging
     */
    fun logError(tag: String, throwable: Throwable, additionalInfo: String? = null) {
        val apiException = handleException(throwable)
        val logMessage = buildString {
            append("API Error: ${apiException.javaClass.simpleName}")
            append("\nMessage: ${apiException.message}")
            apiException.cause?.let { 
                append("\nCause: ${it.javaClass.simpleName} - ${it.message}")
            }
            additionalInfo?.let { 
                append("\nAdditional Info: $it")
            }
        }
        
        // In a real app, you might want to use a proper logging library
        println("$tag: $logMessage")
    }
}

/**
 * Extension function to safely execute API calls with error handling
 */
suspend fun <T> safeApiCall(
    apiCall: suspend () -> retrofit2.Response<T>,
    errorHandler: (Throwable) -> Unit = {}
): Result<T> {
    return try {
        val response = apiCall()
        if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!)
        } else {
            val exception = ApiErrorHandler.parseApiResponseError(response as retrofit2.Response<*>)
            errorHandler(exception)
            Result.failure(exception)
        }
    } catch (throwable: Throwable) {
        val exception = ApiErrorHandler.handleException(throwable)
        errorHandler(exception)
        Result.failure(exception)
    }
}