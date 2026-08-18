package vcmsa.projects.careerconnect.ui.authentication

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import android.util.Log
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import vcmsa.projects.careerconnect.R
import vcmsa.projects.careerconnect.data.repository.RegistrationRepository
import vcmsa.projects.careerconnect.utils.LanguageManager
import com.google.firebase.FirebaseApp

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var tilEmail: TextInputLayout
    private lateinit var etEmail: TextInputEditText
    private lateinit var btnSend: MaterialButton
    private lateinit var btnBack: MaterialButton
    private lateinit var progressBar: CircularProgressIndicator

    private val registrationRepository = RegistrationRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Apply saved language before setting content view
        val languageManager = LanguageManager(this)
        languageManager.applySavedLanguage()

        setContentView(R.layout.activity_forgot_password)

        initViews()
        setupClicks()
    }

    private fun initViews() {
        tilEmail = findViewById(R.id.tilEmail)
        etEmail = findViewById(R.id.etEmail)
        btnSend = findViewById(R.id.btnSend)
        btnBack = findViewById(R.id.btnBack)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setupClicks() {
        btnBack.setOnClickListener { finish() }
        btnSend.setOnClickListener {
            val email = etEmail.text?.toString()?.trim().orEmpty()
            if (email.isEmpty()) {
                tilEmail.error = getString(R.string.email_required)
                return@setOnClickListener
            } else {
                tilEmail.error = null
            }
            setLoading(true)
            lifecycleScope.launch {
                // Log project and providers for diagnostics (non-blocking)
                try {
                    val opts = FirebaseApp.getInstance().options
                    Log.d("ForgotPassword", "Firebase projectId=${opts.projectId}, applicationId=${opts.applicationId}")
                    val debugMethods = registrationRepository.getSignInMethods(email).getOrNull() ?: emptyList()
                    Log.d("ForgotPassword", "signInMethods for $email => $debugMethods")
                } catch (_: Exception) { }

                // Always attempt to send reset email to avoid account enumeration leaks
                val result = registrationRepository.sendPasswordResetEmail(email)
                setLoading(false)
                if (result.isSuccess) {
                    Toast.makeText(this@ForgotPasswordActivity, getString(R.string.reset_email_sent), Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    val message = result.exceptionOrNull()?.message ?: getString(R.string.failed_send_reset)
                    Toast.makeText(this@ForgotPasswordActivity, message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        btnSend.isEnabled = !loading
        btnBack.isEnabled = !loading
    }
}


