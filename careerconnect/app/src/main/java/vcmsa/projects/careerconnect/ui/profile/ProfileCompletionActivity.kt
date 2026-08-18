//CODE ATTRIBUTION
//01
//App Compat Activity
//Adapted from: Android Developers. (2025). AppCompatActivity. [online] Android Developers.
//Available at: https://developer.android.com/reference/androidx/appcompat/app/AppCompatActivity
//Date Accessed: 17 September 2025

//02
//Intent 
//Adapted from: Android Developers. (2025). Intent. [online] Android Developers.
//Available at: https://developer.android.com/reference/android/content/Intent
//Date Accessed: 17 September 2025

//03
//Patterns.PHONE
//Adapted from: Android Developers. (2025). Patterns.PHONE. [online] Android Developers.
//Available at: https://developer.android.com/reference/android/util/Patterns#PHONE
//Date Accessed: 17 September 2025

//04
//Firebase Authentication
//Adapted from: Firebase. (2025). FirebaseAuth. [online] Firebase.
//Available at: https://firebase.google.com/docs/reference/android/com/google/firebase/auth/FirebaseAuth
//Date Accessed: 17 September 2025

//05
//Google Authentication Provider
//Adapted from: Firebase. (2025). GoogleAuthProvider. [online] Firebase.
//Available at: https://firebase.google.com/docs/reference/android/com/google/firebase/auth/GoogleAuthProvider
//Date Accessed: 17 September 2025

//06
//Email Authentication Provider
//Adapted from: Firebase. (2025). EmailAuthProvider. [online] Firebase.
//Available at: https://firebase.google.com/docs/reference/android/com/google/firebase/auth/EmailAuthProvider
//Date Accessed: 17 September 2025

//07
//Sign In With Credentials
//Adapted from: Firebase. (2025). FirebaseAuth.signInWithCredential. [online] Firebase.
//Available at: https://firebase.google.com/docs/reference/android/com/google/firebase/auth/FirebaseAuth#signinwithcredential
//Date Accessed: 17 September 2025

//08
//Create User With Email & Password
//Adapted from: Firebase. (2025). FirebaseAuth.createUserWithEmailAndPassword. [online] Firebase.
//Available at: https://firebase.google.com/docs/reference/android/com/google/firebase/auth/FirebaseAuth#createuserwithemailandpassword
//Date Accessed: 17 September 2025

package vcmsa.projects.careerconnect.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.radiobutton.MaterialRadioButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import vcmsa.projects.careerconnect.R
import vcmsa.projects.careerconnect.data.repository.RegistrationRepository
import vcmsa.projects.careerconnect.domain.model.CreateProfileRequest
import vcmsa.projects.careerconnect.domain.model.UserType
import vcmsa.projects.careerconnect.ui.main.MainActivity
import vcmsa.projects.careerconnect.utils.LanguageManager
import vcmsa.projects.careerconnect.utils.ThemeManager
import vcmsa.projects.careerconnect.utils.FontSizeManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import vcmsa.projects.careerconnect.ui.authentication.LoginActivity

/**
 * Activity for completing user profile after authentication
 */
class ProfileCompletionActivity : AppCompatActivity() {

    private lateinit var tilFirstName: TextInputLayout
    private lateinit var etFirstName: TextInputEditText
    private lateinit var tilLastName: TextInputLayout
    private lateinit var etLastName: TextInputEditText
    private lateinit var tilEmail: TextInputLayout
    private lateinit var etEmail: TextInputEditText
    private lateinit var tilPassword: TextInputLayout
    private lateinit var etPassword: TextInputEditText
    private lateinit var tilConfirmPassword: TextInputLayout
    private lateinit var etConfirmPassword: TextInputEditText
    private lateinit var tilPhone: TextInputLayout
    private lateinit var etPhone: TextInputEditText
    private lateinit var tilLocation: TextInputLayout
    private lateinit var etLocation: TextInputEditText
    private lateinit var tilBio: TextInputLayout
    private lateinit var etBio: TextInputEditText
    private lateinit var tilCompanyName: TextInputLayout
    private lateinit var etCompanyName: TextInputEditText
    private lateinit var rbJobSeeker: MaterialRadioButton
    private lateinit var rbRecruiter: MaterialRadioButton
    private lateinit var btnCompleteProfile: MaterialButton
    private lateinit var progressBar: CircularProgressIndicator

    private var selectedUserType: UserType = UserType.JOB_SEEKER
    private val registrationRepository by lazy { RegistrationRepository() }
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Apply saved language, theme, and font size before setting content view
        val languageManager = LanguageManager(this)
        languageManager.applySavedLanguage()
        
        val themeManager = ThemeManager(this)
        themeManager.applySavedTheme()
        
        val fontSizeManager = FontSizeManager(this)
        fontSizeManager.applySavedFontSize(resources)

        setContentView(R.layout.activity_profile_completion)

        initializeViews()
        setupFirebase()
        setupClickListeners()
        setupUserTypeSelection()
    }

    private fun initializeViews() {
        tilFirstName = findViewById(R.id.tilFirstName)
        etFirstName = findViewById(R.id.etFirstName)
        tilLastName = findViewById(R.id.tilLastName)
        etLastName = findViewById(R.id.etLastName)
        tilEmail = findViewById(R.id.tilEmail)
        etEmail = findViewById(R.id.etEmail)
        tilPassword = findViewById(R.id.tilPassword)
        etPassword = findViewById(R.id.etPassword)
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        tilPhone = findViewById(R.id.tilPhone)
        etPhone = findViewById(R.id.etPhone)
        tilLocation = findViewById(R.id.tilLocation)
        etLocation = findViewById(R.id.etLocation)
        tilBio = findViewById(R.id.tilBio)
        etBio = findViewById(R.id.etBio)
        tilCompanyName = findViewById(R.id.tilCompanyName)
        etCompanyName = findViewById(R.id.etCompanyName)
        rbJobSeeker = findViewById(R.id.rbJobSeeker)
        rbRecruiter = findViewById(R.id.rbRecruiter)
        btnCompleteProfile = findViewById(R.id.btnCompleteProfile)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setupFirebase() {
        firebaseAuth = FirebaseAuth.getInstance()
        // Decide email field behavior based on entry flow
        val allowEmailEdit = intent?.getBooleanExtra("email_editable", false) == true
        val prefillEmail = intent?.getStringExtra("prefill_email")
        val email = firebaseAuth.currentUser?.email ?: prefillEmail
        if (!email.isNullOrBlank()) {
            etEmail.setText(email)
            etEmail.isEnabled = allowEmailEdit
        } else {
            // No Google email; allow manual entry
            etEmail.isEnabled = true
        }
    }

    private fun setupClickListeners() {
        btnCompleteProfile.setOnClickListener {
            if (validateInput()) {
                createProfile()
            }
        }

        // Back to Login
        findViewById<com.google.android.material.button.MaterialButton>(R.id.btnBack)?.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            startActivity(intent)
            finish()
        }
    }

    private fun setupUserTypeSelection() {
        rbJobSeeker.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedUserType = UserType.JOB_SEEKER
                tilCompanyName.visibility = View.GONE
            }
        }

        rbRecruiter.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedUserType = UserType.RECRUITER
                tilCompanyName.visibility = View.VISIBLE
            }
        }

        // Set default selection
        rbJobSeeker.isChecked = true
    }

    private fun validateInput(): Boolean {
        var isValid = true

        // Validate email/password fields for users who came via Google (no password yet)
        val emailText = etEmail.text?.toString()?.trim().orEmpty()
        val passwordText = etPassword.text?.toString().orEmpty()
        val confirmText = etConfirmPassword.text?.toString().orEmpty()

        if (emailText.isEmpty()) {
            tilEmail.error = getString(R.string.email_required)
            isValid = false
        } else {
            tilEmail.error = null
        }

        if (passwordText.length < 6) {
            tilPassword.error = getString(R.string.password_min_length)
            isValid = false
        } else {
            tilPassword.error = null
        }

        if (confirmText != passwordText) {
            tilConfirmPassword.error = getString(R.string.passwords_do_not_match)
            isValid = false
        } else {
            tilConfirmPassword.error = null
        }

        // Validate first name
        val firstName = etFirstName.text.toString().trim()
        if (firstName.isEmpty()) {
            tilFirstName.error = getString(R.string.first_name_required)
            isValid = false
        } else {
            tilFirstName.error = null
        }

        // Validate last name
        val lastName = etLastName.text.toString().trim()
        if (lastName.isEmpty()) {
            tilLastName.error = getString(R.string.last_name_required)
            isValid = false
        } else {
            tilLastName.error = null
        }

        // Validate company name for recruiters
        if (selectedUserType == UserType.RECRUITER) {
            val companyName = etCompanyName.text.toString().trim()
            if (companyName.isEmpty()) {
                tilCompanyName.error = getString(R.string.company_name_required)
                isValid = false
            } else {
                tilCompanyName.error = null
            }
        }

        // Validate phone (optional but if provided, should be valid)
        val phone = etPhone.text.toString().trim()
        if (phone.isNotEmpty() && !android.util.Patterns.PHONE.matcher(phone).matches()) {
            tilPhone.error = getString(R.string.invalid_phone)
            isValid = false
        } else {
            tilPhone.error = null
        }

        return isValid
    }

    private fun createProfile() {
        setLoading(true)

        lifecycleScope.launch {
            try {
                // Determine entry mode
                val googleIdToken = intent?.getStringExtra("google_id_token")

                // Gather email/password from UI
                val emailText = etEmail.text?.toString()?.trim() ?: ""
                val passwordText = etPassword.text?.toString() ?: ""

                // Create/sign-in Firebase user now, based on flow
                if (firebaseAuth.currentUser == null) {
                    if (!googleIdToken.isNullOrBlank()) {
                        // Google flow: sign in with Google credential
                        try {
                            val credential = GoogleAuthProvider.getCredential(googleIdToken, null)
                            val result = firebaseAuth.signInWithCredential(credential).await()
                            if (result.user == null) {
                                setLoading(false)
                                showError(getString(R.string.auth_failed_retry))
                                return@launch
                            }
                        } catch (e: Exception) {
                            setLoading(false)
                            showError(getString(R.string.auth_failed_retry) + ": ${e.message}")
                            return@launch
                        }
                    } else {
                        // Email flow: create user with email/password
                        if (emailText.isBlank() || passwordText.isBlank()) {
                            setLoading(false)
                            showError(getString(R.string.email_password_required))
                            return@launch
                        }
                        try {
                            val result = firebaseAuth.createUserWithEmailAndPassword(emailText, passwordText).await()
                            if (result.user == null) {
                                setLoading(false)
                                showError(getString(R.string.registration_failed_retry))
                                return@launch
                            }
                        } catch (e: Exception) {
                            setLoading(false)
                            showError(getString(R.string.registration_failed_retry) + ": ${e.message}")
                            return@launch
                        }
                    }
                }

                val currentUser = firebaseAuth.currentUser
                if (currentUser?.email == null && emailText.isBlank()) {
                    setLoading(false)
                    showError(getString(R.string.user_email_not_found))
                    return@launch
                }

                // Link email/password to current Google-authenticated user if needed (but only if not already linked)
                if (!googleIdToken.isNullOrBlank() && emailText.isNotEmpty() && passwordText.isNotEmpty()) {
                    try {
                        val methods = firebaseAuth.fetchSignInMethodsForEmail(emailText).await()?.signInMethods ?: emptyList()
                        val hasPassword = methods.any { it.equals("password", ignoreCase = true) }
                        if (!hasPassword) {
                            val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(emailText, passwordText)
                            val user = firebaseAuth.currentUser ?: run {
                            setLoading(false)
                            showError(getString(R.string.user_not_authenticated))
                            return@launch
                            }
                            user.linkWithCredential(credential).await()
                        }
                    } catch (e: Exception) {
                        // If already linked, ignore; otherwise, show friendly message
                        val message = e.message ?: getString(R.string.failed_set_password)
                        // If error is 'credential-already-in-use', we can ignore linking
                        if (!message.contains("already in use", ignoreCase = true)) {
                            setLoading(false)
                            showError(message)
                            return@launch
                        }
                    }
                }

                val resolvedEmail = if (emailText.isNotBlank()) emailText else (firebaseAuth.currentUser?.email ?: run {
                    setLoading(false)
                    showError(getString(R.string.user_email_not_found))
                    return@launch
                })

                val request = CreateProfileRequest(
                    userType = selectedUserType,
                    firstName = etFirstName.text.toString().trim(),
                    lastName = etLastName.text.toString().trim(),
                    email = resolvedEmail,
                    phone = etPhone.text.toString().trim().takeIf { it.isNotEmpty() },
                    location = etLocation.text.toString().trim().takeIf { it.isNotEmpty() },
                    companyName = if (selectedUserType == UserType.RECRUITER) {
                        etCompanyName.text.toString().trim().takeIf { it.isNotEmpty() }
                    } else null,
                    bio = etBio.text.toString().trim().takeIf { it.isNotEmpty() }
                )

                val result = registrationRepository.createProfile(request)
                
                result.fold(
                    onSuccess = { profile ->
                        // Profile created successfully
                        showSuccess(getString(R.string.profile_created_successfully))
                        navigateToMainScreen(profile.userType)
                    },
                    onFailure = { error ->
                        setLoading(false)
                        val friendly = vcmsa.projects.careerconnect.data.network.ApiErrorHandler.getErrorMessage(error)
                        showError(getString(R.string.failed_create_profile, friendly))
                    }
                )
            } catch (e: Exception) {
                setLoading(false)
                showError(getString(R.string.error_occurred, e.message ?: "Unknown error"))
            }
        }
    }

    private fun navigateToMainScreen(userType: UserType) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("user_type", userType.name)
        }
        startActivity(intent)
        finish()
    }

    private fun setLoading(loading: Boolean) {
        progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        btnCompleteProfile.isEnabled = !loading
    }

    private fun showSuccess(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}