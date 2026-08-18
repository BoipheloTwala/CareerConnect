//CODE ATTRIBUTION
//01
//FirebaseAuth
//Adapted from: Firebase. (2025). FirebaseAuth. [online] Firebase.
//Available at: https://firebase.google.com/docs/reference/android/com/google/firebase/auth/FirebaseAuth
//Date Accessed: 01 October 2025

//02
//FirebaseUser.getIdToken
//Adapted from: Firebase. (2025). FirebaseUser#getIdToken. [online] Firebase.
//Available at: https://firebase.google.com/docs/reference/android/com/google/firebase/auth/FirebaseUser#getIdToken(boolean)
//Date Accessed: 01 October 2025

//03
//tasks.await (Play Services coroutines)
//Adapted from: Kotlinx Coroutines. (2025). Tasks integration. [online]
//Available at: https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-play-services/kotlinx.coroutines.tasks/await.html
//Date Accessed: 01 October 2025

//04
//OkHttpClient
//Adapted from: Square. (2025). OkHttp Overview. [online]
//Available at: https://square.github.io/okhttp/
//Date Accessed: 01 October 2025

//05
//OkHttp Interceptors
//Adapted from: Square. (2025). OkHttp Interceptors. [online]
//Available at: https://square.github.io/okhttp/features/interceptors/
//Date Accessed: 01 October 2025

//06
//Retrofit
//Adapted from: Square. (2025). Retrofit. [online]
//Available at: https://square.github.io/retrofit/
//Date Accessed: 01 October 2025

//07
//Retrofit.Builder
//Adapted from: Square. (2025). Retrofit.Builder. [online]
//Available at: https://square.github.io/retrofit/2.x/retrofit/retrofit2/Retrofit.Builder.html
//Date Accessed: 01 October 2025

package vcmsa.projects.careerconnect.data.network

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Network configuration module for CareerConnect API
 */
object NetworkModule {
    
    private const val BASE_URL = "https://careerconnectapi2.onrender.com/api/"
    private const val TIMEOUT_SECONDS = 30L
    private const val MAX_RETRY_ATTEMPTS = 3
    
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    
    /**
     * Creates an OkHttpClient with comprehensive interceptors
     */
    private fun createOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        return OkHttpClient.Builder()
            .addInterceptor(createAuthInterceptor())
            .addInterceptor(createHeadersInterceptor())
            .addInterceptor(createRetryInterceptor())
            .addInterceptor(loggingInterceptor)
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
    }
    
    /**
     * Creates authentication interceptor for Firebase ID tokens
     */
    private fun createAuthInterceptor(): Interceptor {
        return Interceptor { chain ->
            val originalRequest = chain.request()
            
            // Get the current user's ID token
            val user = firebaseAuth.currentUser
            if (user != null) {
                try {
                    // Get the ID token synchronously using runBlocking
                    val token = runBlocking { user.getIdToken(false).await().token }
                    
                    val authenticatedRequest = originalRequest.newBuilder()
                        .header("Authorization", "Bearer $token")
                        .build()
                    
                    chain.proceed(authenticatedRequest)
                } catch (e: Exception) {
                    // If token retrieval fails, proceed without authentication
                    // In production, you might want to handle this differently
                    chain.proceed(originalRequest)
                }
            } else {
                // No authenticated user, proceed without auth header
                chain.proceed(originalRequest)
            }
        }
    }
    
    /**
     * Creates headers interceptor for common headers
     */
    private fun createHeadersInterceptor(): Interceptor {
        return Interceptor { chain ->
            val originalRequest = chain.request()
            
            val requestWithHeaders = originalRequest.newBuilder()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("User-Agent", "CareerConnect-Android/1.0")
                .header("X-App-Version", "1.0")
                .header("X-Platform", "android")
                .build()
            
            chain.proceed(requestWithHeaders)
        }
    }
    
    /**
     * Creates retry interceptor for failed requests
     */
    private fun createRetryInterceptor(): Interceptor {
        return Interceptor { chain ->
            val request = chain.request()
            var response: Response? = null
            var lastException: IOException? = null
            
            repeat(MAX_RETRY_ATTEMPTS) { attempt ->
                try {
                    response?.close() // Close previous response if any
                    response = chain.proceed(request)
                    
                    // If successful or client error (4xx), don't retry
                    if (response!!.isSuccessful || response!!.code in 400..499) {
                        return@Interceptor response!!
                    }
                } catch (e: IOException) {
                    lastException = e
                    if (attempt == MAX_RETRY_ATTEMPTS - 1) {
                        throw e
                    }
                    // Wait before retry (exponential backoff)
                    Thread.sleep(1000L * (attempt + 1))
                }
            }
            
            response ?: throw lastException ?: IOException("Request failed after $MAX_RETRY_ATTEMPTS attempts")
        }
    }
    
    /**
     * Creates error handling interceptor for common HTTP errors
     */
    private fun createErrorHandlingInterceptor(): Interceptor {
        return Interceptor { chain ->
            val response = chain.proceed(chain.request())
            
            when (response.code) {
                401 -> {
                    // Unauthorized - token might be expired
                    response.close()
                    throw ApiException.Unauthorized("Authentication failed. Please login again.")
                }
                403 -> {
                    response.close()
                    throw ApiException.Forbidden("Access denied. You don't have permission to perform this action.")
                }
                404 -> {
                    response.close()
                    throw ApiException.NotFound("The requested resource was not found.")
                }
                422 -> {
                    response.close()
                    throw ApiException.ValidationError("Invalid request data. Please check your input.")
                }
                429 -> {
                    response.close()
                    throw ApiException.TooManyRequests("Too many requests. Please try again later.")
                }
                500 -> {
                    response.close()
                    throw ApiException.InternalServerError("Server error. Please try again later.")
                }
                502, 503, 504 -> {
                    response.close()
                    throw ApiException.ServerUnavailable("Service temporarily unavailable. Please try again later.")
                }
            }
            
            response
        }
    }
    
    /**
     * Creates a Retrofit instance configured for CareerConnect API
     */
    private fun createRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(createOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    /**
     * Provides the CareerConnect API service instance
     */
    val apiService: CareerConnectApiService by lazy {
        createRetrofit().create(CareerConnectApiService::class.java)
    }
}