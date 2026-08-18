//CODE ATTRIBUTION
//01
//App Compat Activity
//Adapted from: Android Developers. (2025). AppCompatActivity. [online] Android Developers.
//Available at: https://developer.android.com/reference/androidx/appcompat/app/AppCompatActivity
//Date Accessed: 15 September 2025

//02
//Toast
//Adapted from: Android Developers. (2025). Toast. [online] Android Developers.
//Available at: https://developer.android.com/reference/android/widget/Toast
//Date Accessed: 15 September 2025

//03
//Jetpack Compose Button
//Adapted from: Android Developers. (2025). Button (Jetpack Compose). [online] Android Developers.
//Available at: https://developer.android.com/develop/ui/compose/components/button
//Date Accessed: 15 September 2025

//04
//Jetpack Compose Progress indicators
//Adapted from: Android Developers. (2025). Progress indicators (Jetpack Compose). [online] Android Developers.
//Available at: https://developer.android.com/develop/ui/compose/components/progress
//Date Accessed: 15 September 2025

//05
//Text Input Layout
//Adapted from: Android Developers. (2025). TextInputLayout. [online] Android Developers.
//Available at: https://developer.android.com/reference/com/google/android/material/textfield/TextInputLayout
//Date Accessed: 15 September 2025

//06
//Text Input Edi Text
//Adapted from: Android Developers. (2025). TextInputEditText. [online] Android Developers.
//Available at: https://developer.android.com/reference/com/google/android/material/textfield/TextInputEditText
//Date Accessed: 15 September 2025

package vcmsa.projects.careerconnect.ui.settings

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import vcmsa.projects.careerconnect.R
import vcmsa.projects.careerconnect.data.repository.RegistrationRepository

class ChangePasswordActivity : AppCompatActivity() {

    private lateinit var tilCurrentPassword: TextInputLayout
    private lateinit var etCurrentPassword: TextInputEditText
    private lateinit var tilNewPassword: TextInputLayout
    private lateinit var etNewPassword: TextInputEditText
    private lateinit var tilConfirmPassword: TextInputLayout
    private lateinit var etConfirmPassword: TextInputEditText
    private lateinit var btnSave: MaterialButton
    private lateinit var btnBack: MaterialButton
    private lateinit var progressBar: CircularProgressIndicator

    private val registrationRepository = RegistrationRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)

        initViews()
        setupClicks()
    }

    private fun initViews() {
        tilCurrentPassword = findViewById(R.id.tilCurrentPassword)
        etCurrentPassword = findViewById(R.id.etCurrentPassword)
        tilNewPassword = findViewById(R.id.tilNewPassword)
        etNewPassword = findViewById(R.id.etNewPassword)
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnSave = findViewById(R.id.btnSave)
        btnBack = findViewById(R.id.btnBack)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setupClicks() {
        btnBack.setOnClickListener { finish() }

        btnSave.setOnClickListener {
            val current = etCurrentPassword.text?.toString().orEmpty()
            val new = etNewPassword.text?.toString().orEmpty()
            val confirm = etConfirmPassword.text?.toString().orEmpty()

            var hasError = false
            if (current.isEmpty()) { tilCurrentPassword.error = "Current password is required"; hasError = true } else tilCurrentPassword.error = null
            if (new.length < 6) { tilNewPassword.error = "New password must be at least 6 characters"; hasError = true } else tilNewPassword.error = null
            if (confirm != new) { tilConfirmPassword.error = "Passwords do not match"; hasError = true } else tilConfirmPassword.error = null
            if (hasError) return@setOnClickListener

            setLoading(true)
            lifecycleScope.launch {
                val result = registrationRepository.changePassword(current, new)
                setLoading(false)
                if (result.isSuccess) {
                    Toast.makeText(this@ChangePasswordActivity, "Password updated", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    val message = result.exceptionOrNull()?.message ?: "Failed to change password"
                    Toast.makeText(this@ChangePasswordActivity, message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        btnSave.isEnabled = !loading
        btnBack.isEnabled = !loading
    }
}


