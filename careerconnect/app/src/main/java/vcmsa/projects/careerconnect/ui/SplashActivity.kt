//CODE ATTRIBUTION
//01
//AppCompatActivity
//Adapted from: Android Developers. (2025). AppCompatActivity. [online] Android Developers.
//Available at:  https://developer.android.com/reference/androidx/appcompat/app/AppCompatActivity
//Date Accessed: 15 September 2025

//02
//View
//Adapted from: Android Developers. (2025). View  |  API reference  |  Android Developers. [online] Android Developers.
//Available at: https://developer.android.com/reference/android/view/View#Visibility
//Date Accessed: 15 September 2025

//03
//Material Buttons
//Adapted from: Material Design. (2025). Buttons. [online] Material Design.
//Available at: https://m3.material.io/components/buttons/overview
//Date Accessed: 15 September 2025

//04
//Circular Progress Indicator
//Adapted from: Material Design. (2025). Progress indicators. [online] Material Design.
//Available at: https://m3.material.io/components/progress-indicators/overview
//Date Accessed: 15 September 2025

package vcmsa.projects.careerconnect.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import vcmsa.projects.careerconnect.R
import vcmsa.projects.careerconnect.data.network.ApiException
import vcmsa.projects.careerconnect.session.SessionManager
import vcmsa.projects.careerconnect.ui.authentication.LoginActivity
import vcmsa.projects.careerconnect.ui.main.MainActivity
import vcmsa.projects.careerconnect.ui.profile.ProfileCompletionActivity

class SplashActivity : AppCompatActivity() {

    private val sessionManager = SessionManager()

    private lateinit var progress: CircularProgressIndicator
    private lateinit var tvStatus: TextView
    private lateinit var btnRetry: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        progress = findViewById(R.id.progressBar)
        tvStatus = findViewById(R.id.tvStatus)
        btnRetry = findViewById(R.id.btnRetry)

        btnRetry.setOnClickListener { startChecks() }

        startChecks()
    }

    private fun startChecks() {
        progress.visibility = View.VISIBLE
        btnRetry.visibility = View.GONE
        tvStatus.text = "Loading…"

        lifecycleScope.launch {
            // Small delay for splash polish
            delay(300)

            // Auth check
            val authed = sessionManager.isAuthenticated()
            if (!authed) {
                navigateToLogin(); return@launch
            }

            // Refresh token to ensure valid auth header
            sessionManager.getFreshIdToken()

            // Profile check
            val profileResult = sessionManager.fetchProfile()
            if (profileResult.isSuccess) {
                navigateToMain()
            } else {
                when (val ex = profileResult.exceptionOrNull()) {
                    is ApiException.NotFound -> navigateToProfileCompletion()
                    is ApiException.NetworkError -> showRetry("You're offline. Check your connection and try again.")
                    is ApiException.ServerUnavailable, is ApiException.InternalServerError -> showRetry("Server error. Please try again shortly.")
                    else -> showRetry(ex?.message ?: "An unexpected error occurred. Please try again.")
                }
            }
        }
    }

    private fun showRetry(message: String) {
        progress.visibility = View.GONE
        btnRetry.visibility = View.VISIBLE
        tvStatus.text = message
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun navigateToProfileCompletion() {
        startActivity(Intent(this, ProfileCompletionActivity::class.java))
        finish()
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}

