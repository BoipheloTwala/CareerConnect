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

import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import vcmsa.projects.careerconnect.data.network.ApiClient
import vcmsa.projects.careerconnect.data.network.ApiErrorHandler
import vcmsa.projects.careerconnect.domain.model.CreateProfileRequest
import vcmsa.projects.careerconnect.domain.model.UserProfile

/**
 * Repository responsible for registration-related actions:
 * - Firebase authentication (email/password and Google)
 * - Creating profile via backend API
 */
class RegistrationRepository(
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    suspend fun registerWithEmail(email: String, password: String): Result<Unit> {
        return try {
            firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            Result.success(Unit)
        } catch (t: Throwable) {
            val ex = ApiErrorHandler.handleException(t)
            Result.failure(ex)
        }
    }

    suspend fun signInWithEmail(email: String, password: String): Result<Unit> {
        return try {
            firebaseAuth.signInWithEmailAndPassword(email, password).await()
            Result.success(Unit)
        } catch (t: Throwable) {
            val ex = ApiErrorHandler.handleException(t)
            Result.failure(ex)
        }
    }

    suspend fun linkEmailCredential(email: String, password: String): Result<Unit> {
        return try {
            val user = firebaseAuth.currentUser ?: return Result.failure(IllegalStateException("Not authenticated"))
            val credential = EmailAuthProvider.getCredential(email, password)
            user.linkWithCredential(credential).await()
            Result.success(Unit)
        } catch (t: Throwable) {
            val ex = ApiErrorHandler.handleException(t)
            Result.failure(ex)
        }
    }

    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> {
        return try {
            val user = firebaseAuth.currentUser ?: return Result.failure(IllegalStateException("Not authenticated"))
            val email = user.email ?: return Result.failure(IllegalStateException("No email associated with account"))

            // Reauthenticate with current password
            val credential = EmailAuthProvider.getCredential(email, currentPassword)
            user.reauthenticate(credential).await()

            // Update password
            user.updatePassword(newPassword).await()
            Result.success(Unit)
        } catch (t: Throwable) {
            val ex = ApiErrorHandler.handleException(t)
            Result.failure(ex)
        }
    }

    /**
     * Sends a password reset email to the provided address. This works when the user is not signed in.
     */
    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (t: Throwable) {
            val ex = ApiErrorHandler.handleException(t)
            Result.failure(ex)
        }
    }

    /**
     * Checks whether the given email has any sign-in methods (e.g., password provider).
     */
    suspend fun emailHasAnySignInMethod(email: String): Result<Boolean> {
        return try {
            val normalized = email.trim().lowercase()
            val methods = firebaseAuth.fetchSignInMethodsForEmail(normalized).await()?.signInMethods ?: emptyList()
            Result.success(methods.isNotEmpty())
        } catch (t: Throwable) {
            val ex = ApiErrorHandler.handleException(t)
            Result.failure(ex)
        }
    }

    /** Returns the raw list of sign-in methods for diagnostics or specialized flows. */
    suspend fun getSignInMethods(email: String): Result<List<String>> {
        return try {
            val normalized = email.trim().lowercase()
            val methods = firebaseAuth.fetchSignInMethodsForEmail(normalized).await()?.signInMethods ?: emptyList()
            Result.success(methods)
        } catch (t: Throwable) {
            val ex = ApiErrorHandler.handleException(t)
            Result.failure(ex)
        }
    }

    suspend fun signInWithGoogle(idToken: String): Result<Unit> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            firebaseAuth.signInWithCredential(credential).await()
            Result.success(Unit)
        } catch (t: Throwable) {
            val ex = ApiErrorHandler.handleException(t)
            Result.failure(ex)
        }
    }

    suspend fun createProfile(request: CreateProfileRequest): Result<UserProfile> {
        // Ensure basic app-side validation is already done upstream.
        return vcmsa.projects.careerconnect.data.network.safeApiCall(
            apiCall = { ApiClient.apiService.createProfile(request) },
            errorHandler = { ApiErrorHandler.logError("RegistrationRepository", it, "Create profile failed") }
        )
    }
}

