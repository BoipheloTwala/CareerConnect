//CODE ATTRIBUTION
//01
//Firebase Authentication
//Adapted from: Firebase. (2025). FirebaseAuth. [online] Firebase.
//Available at: https://firebase.google.com/docs/reference/android/com/google/firebase/auth/FirebaseAuth
//Date Accessed: 30 September 2025

//02
//Firebase User 
//Adapted from: Firebase. (2025). FirebaseUser.getIdToken. [online] Firebase.
//Available at: https://firebase.google.com/docs/reference/android/com/google/firebase/auth/FirebaseUser#getIdToken(boolean)
//Date Accessed: 30 September 2025

//03
//tasks.await
//Adapted from: Kotlinx Coroutines. (2025). Tasks integration. [online]
//Available at: https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-play-services/kotlinx.coroutines.tasks/await.html
//Date Accessed: 30 September 2025

//04
//Kotlin Result
//Adapted from: Kotlin Standard Library. (2025). Result. [online]
//Available at: https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-result/
//Date Accessed: 30 September 2025

package vcmsa.projects.careerconnect.session

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import vcmsa.projects.careerconnect.data.network.ApiClient
import vcmsa.projects.careerconnect.data.network.ApiErrorHandler
import vcmsa.projects.careerconnect.data.network.ApiException
import vcmsa.projects.careerconnect.data.repository.AuthRepository
import vcmsa.projects.careerconnect.domain.model.UserProfile

/**
 * Handles session concerns: auth state, token refresh, profile existence, logout
 */
class SessionManager(
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val authRepository: AuthRepository = AuthRepository()
) {

    suspend fun isAuthenticated(): Boolean {
        return firebaseAuth.currentUser != null
    }

    suspend fun getFreshIdToken(): String? {
        val user = firebaseAuth.currentUser ?: return null
        return try {
            user.getIdToken(true).await().token
        } catch (t: Throwable) {
            null
        }
    }

    suspend fun fetchProfile(): Result<UserProfile> {
        return authRepository.fetchMyProfile()
    }


    fun logout(): Result<Unit> {
        return try {
            firebaseAuth.signOut()
            Result.success(Unit)
        } catch (t: Throwable) {
            Result.failure(t)
        }
    }
}

