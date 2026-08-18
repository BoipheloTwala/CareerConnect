//CODE ATTRIBUTION
//01
//Google Sign In Options
//Adapted from: Google Developers. (2025). GoogleSignInOptions. [online] Google Developers.
//Available at: https://developers.google.com/android/reference/com/google/android/gms/auth/api/signin/GoogleSignInOptions
//Date Accessed: 30 September 2025

//02
//Google Sign In Client
//Adapted from: Google Developers. (2025). GoogleSignInClient. [online] Google Developers.
//Available at: https://developers.google.com/android/reference/com/google/android/gms/auth/api/signin/GoogleSignInClient
//Date Accessed: 30 September 2025

//03
//Google Sign In
//Adapted from: Google Developers. (2025). GoogleSignIn. [online] Google Developers.
//Available at: https://developers.google.com/android/reference/com/google/android/gms/auth/api/signin/GoogleSignIn
//Date Accessed: 30 September 2025

//04
//API Exception
//Adapted from: Google Developers. (2025). ApiException. [online] Google Developers.
//Available at: https://developers.google.com/android/reference/com/google/android/gms/common/api/ApiException
//Date Accessed: 30 September 2025

//05
//Status Codes
//Adapted from: Google Developers. (2025). CommonStatusCodes. [online] Google Developers.
//Available at: https://developers.google.com/android/reference/com/google/android/gms/common/api/CommonStatusCodes
//Date Accessed: 30 September 2025

//06
//Firebase Authentication
//Adapted from: Firebase. (2025). FirebaseAuth. [online] Firebase.
//Available at: https://firebase.google.com/docs/reference/android/com/google/firebase/auth/FirebaseAuth
//Date Accessed: 30 September 2025

//07
//Google Authentication Provider
//Adapted from: Firebase. (2025). GoogleAuthProvider. [online] Firebase.
//Available at: https://firebase.google.com/docs/reference/android/com/google/firebase/auth/GoogleAuthProvider
//Date Accessed: 30 September 2025

package vcmsa.projects.careerconnect.ui.authentication

import android.content.Intent
import android.os.Bundle
import android.app.AlertDialog
import android.widget.EditText
import android.widget.LinearLayout
import android.text.InputType
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException as GmsApiException
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import vcmsa.projects.careerconnect.R
import vcmsa.projects.careerconnect.data.network.ApiErrorHandler
import vcmsa.projects.careerconnect.data.network.ApiException as AppApiException
import vcmsa.projects.careerconnect.data.repository.AuthRepository
import vcmsa.projects.careerconnect.data.repository.RegistrationRepository
import vcmsa.projects.careerconnect.ui.main.MainActivity
import vcmsa.projects.careerconnect.ui.profile.ProfileCompletionActivity
import vcmsa.projects.careerconnect.utils.LanguageManager
import vcmsa.projects.careerconnect.utils.BiometricAuthManager
import vcmsa.projects.careerconnect.utils.BiometricPreferences
import vcmsa.projects.careerconnect.utils.ThemeManager
import vcmsa.projects.careerconnect.utils.FontSizeManager

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleClient: GoogleSignInClient

    private lateinit var tilEmail: TextInputLayout
    private lateinit var etEmail: TextInputEditText
    private lateinit var tilPassword: TextInputLayout
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnLogin: MaterialButton
    private lateinit var btnGoogle: MaterialButton
    private lateinit var btnBiometric: MaterialButton
    private lateinit var progress: CircularProgressIndicator
    private lateinit var tvGoToRegister: android.widget.TextView
    private lateinit var tvForgotPassword: android.widget.TextView

    private val authRepository = AuthRepository()
    private val registrationRepository = RegistrationRepository()
    
    private lateinit var biometricAuthManager: BiometricAuthManager
    private lateinit var biometricPreferences: BiometricPreferences

    private val googleLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { res ->
        // If user backed out or operation didn't complete, reset UI and exit
        if (res.resultCode != RESULT_OK) {
            setLoading(false)
            return@registerForActivityResult
        }
        val data = res.data ?: run {
            setLoading(false)
            return@registerForActivityResult
        }
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(GmsApiException::class.java)
            val idToken = account.idToken
            val email = account.email?.trim()
            if (idToken != null && !email.isNullOrBlank()) {
                setLoading(true)
                lifecycleScope.launch {
                    try {
                        // If this email has password provider but no Google provider, prompt to link
                        val methods = auth.fetchSignInMethodsForEmail(email).await()?.signInMethods ?: emptyList()
                        val hasPassword = methods.any { it.equals("password", ignoreCase = true) }
                        val hasGoogle = methods.any { it.equals("google.com", ignoreCase = true) }
                        if (hasPassword && !hasGoogle) {
                            setLoading(false)
                            promptPasswordAndLink(email, idToken)
                            return@launch
                        }
                        // Sign in with Google credential
                        val googleCredential = GoogleAuthProvider.getCredential(idToken, null)

                        try {
                        val authResult = auth.signInWithCredential(googleCredential).await()
                        val isNewUser = authResult.additionalUserInfo?.isNewUser == true

                        if (isNewUser) {
                                // Check if there's an existing account with this email that has password provider
                                val existingMethods = auth.fetchSignInMethodsForEmail(email).await()?.signInMethods ?: emptyList()
                                val hasPasswordProvider = existingMethods.any { it.equals("password", ignoreCase = true) }

                                if (hasPasswordProvider) {
                                    // There's an existing email/password account - we need to link Google to it
                                    // First, sign out the temporary Google account
                                    try { auth.currentUser?.delete()?.await() } catch (_: Exception) {}
                                    auth.signOut()
                                    googleClient.signOut()

                                    // Prompt user to sign in with password first, then we'll link Google
                                    setLoading(false)
                                    promptPasswordAndLink(email, idToken)
                                    return@launch
                                } else {
                                    // No existing account at all - require registration
                            try { auth.currentUser?.delete()?.await() } catch (_: Exception) {}
                            auth.signOut()
                            googleClient.signOut()
                            setLoading(false)
                            showError(getString(R.string.no_account_found))
                            return@launch
                                }
                            }
                        } catch (e: Exception) {
                            // Check if this is an account linking error
                            if (e.message?.contains("already exists", ignoreCase = true) == true ||
                                e.message?.contains("credential already associated", ignoreCase = true) == true) {

                                // Account exists with different provider - try to link
                                setLoading(false)
                                promptPasswordAndLink(email, idToken)
                                return@launch
                            } else {
                                throw e
                            }
                        }

                        // Existing Firebase user: ensure password provider is also linked
                        val currentUser = auth.currentUser
                        val currentEmail = currentUser?.email
                        val hasPasswordProviderNow = currentUser?.providerData?.any { it.providerId.equals("password", ignoreCase = true) } == true
                        if (!hasPasswordProviderNow && !currentEmail.isNullOrBlank()) {
                            // Double-check via network methods in case providerData is stale
                            val linkedMethods = auth.fetchSignInMethodsForEmail(currentEmail).await()?.signInMethods ?: emptyList()
                            val hasPasswordNow = linkedMethods.any { it.equals("password", ignoreCase = true) }
                            if (!hasPasswordNow) {
                                setLoading(false)
                                promptSetPasswordAndLink(currentEmail) {
                                    setLoading(true)
                                    handleAuthResult(true, null)
                                }
                                return@launch
                            }
                        }
                        // Already has password provider
                        handleAuthResult(true, null)
                    } catch (t: Throwable) {
                        setLoading(false)
                        showError(getString(R.string.unable_google_signin))
                        googleClient.signOut()
                    }
                }
            } else {
                // No token indicates cancellation or failure path
                setLoading(false)
            }
        } catch (e: GmsApiException) {
            // When user cancels the chooser, task.getResult throws an ApiException with status == CANCELED
            setLoading(false)
            // Only show an error if it wasn't a user cancellation
            if (e.statusCode != com.google.android.gms.common.api.CommonStatusCodes.CANCELED) {
                showError(getString(R.string.google_signin_failed, e.message ?: "Unknown error"))
            }
        }
    }

    private fun promptPasswordAndLink(email: String, googleIdToken: String) {
        val context = this
        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 24, 48, 0)
        }
        val passwordInput = EditText(context).apply {
            hint = getString(R.string.enter_password)
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        container.addView(passwordInput)

        AlertDialog.Builder(context)
            .setTitle(getString(R.string.link_google_title))
            .setMessage(getString(R.string.link_google_message))
            .setView(container)
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
                googleClient.signOut()
            }
            .setPositiveButton(getString(R.string.link)) { dialog, _ ->
                val password = passwordInput.text?.toString().orEmpty()
                if (password.isBlank()) {
                    showError(getString(R.string.password_required_link))
                    googleClient.signOut()
                    return@setPositiveButton
                }
                setLoading(true)
                lifecycleScope.launch {
                    try {
                        // Sign in with email/password first
                        val emailResult = auth.signInWithEmailAndPassword(email, password).await()
                        if (emailResult.user == null) {
                            setLoading(false)
                            showError(getString(R.string.email_signin_failed))
                            googleClient.signOut()
                            return@launch
                        }
                        // Link Google credential
                        val googleCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                        auth.currentUser?.linkWithCredential(googleCredential)?.await()
                        // Proceed with normal post-auth flow
                        handleAuthResult(true, null)
                    } catch (t: Throwable) {
                        setLoading(false)
                        showError(getString(R.string.linking_failed, t.message ?: "Unknown error"))
                        googleClient.signOut()
                    }
                }
                dialog.dismiss()
            }
            .show()
    }

    private fun promptSetPasswordAndLink(email: String, onLinked: () -> Unit) {
        val context = this
        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 24, 48, 0)
        }
        val passwordInput = EditText(context).apply {
            hint = getString(R.string.enter_password)
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        container.addView(passwordInput)

        AlertDialog.Builder(context)
            .setTitle(getString(R.string.add_password_title))
            .setMessage(getString(R.string.add_password_message))
            .setView(container)
            .setNegativeButton(getString(R.string.skip)) { dialog, _ -> dialog.dismiss() }
            .setPositiveButton(getString(R.string.save)) { dialog, _ ->
                val password = passwordInput.text?.toString().orEmpty()
                if (password.isBlank()) {
                    showError(getString(R.string.password_required_save))
                    return@setPositiveButton
                }
                setLoading(true)
                lifecycleScope.launch {
                    try {
                        val emailCredential = com.google.firebase.auth.EmailAuthProvider.getCredential(email, password)
                        auth.currentUser?.linkWithCredential(emailCredential)?.await()
                        onLinked()
                    } catch (t: Throwable) {
                        setLoading(false)
                        showError(getString(R.string.failed_link_password, t.message ?: "Unknown error"))
                    }
                }
                dialog.dismiss()
            }
            .show()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Apply saved language, theme, and font size before setting content view
        val languageManager = LanguageManager(this)
        languageManager.applySavedLanguage()
        
        val themeManager = ThemeManager(this)
        themeManager.applySavedTheme()
        
        val fontSizeManager = FontSizeManager(this)
        fontSizeManager.applySavedFontSize(resources)

        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        biometricAuthManager = BiometricAuthManager(this)
        biometricPreferences = BiometricPreferences(this)
        
        initViews()
        setupGoogle()
        setupClicks()
        setupBiometric()
    }

    private fun initViews() {
        tilEmail = findViewById(R.id.tilEmail)
        etEmail = findViewById(R.id.etEmail)
        tilPassword = findViewById(R.id.tilPassword)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnGoogle = findViewById(R.id.btnGoogle)
        btnBiometric = findViewById(R.id.btnBiometric)
        progress = findViewById(R.id.progressBar)
        tvGoToRegister = findViewById(R.id.tvGoToRegister)
        tvForgotPassword = findViewById(R.id.tvForgotPassword)
    }

    private fun setupGoogle() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleClient = GoogleSignIn.getClient(this, gso)
    }

    private fun setupClicks() {
        btnLogin.setOnClickListener {
            val email = etEmail.text?.toString()?.trim().orEmpty()
            val password = etPassword.text?.toString().orEmpty()
            if (email.isEmpty()) {
                tilEmail.error = getString(R.string.email_required); return@setOnClickListener
            } else tilEmail.error = null
            if (password.isEmpty()) {
                tilPassword.error = getString(R.string.password_required); return@setOnClickListener
            } else tilPassword.error = null
            setLoading(true)
            lifecycleScope.launch {
                val result = registrationRepository.signInWithEmail(email, password)
                if (result.isSuccess) {
                    // Check if biometric should be prompted (before navigating away)
                    val shouldPromptBiometric = shouldPromptEnableBiometric()
                    if (shouldPromptBiometric) {
                        setLoading(false)
                        promptEnableBiometric(email, password)
                        return@launch
                    }
                }
                handleAuthResult(result.isSuccess, result.exceptionOrNull()?.message)
            }
        }

        btnGoogle.setOnClickListener {
            setLoading(true)
            // Ensure chooser shows and previous default account is cleared
            googleClient.signOut().addOnCompleteListener {
                googleClient.revokeAccess().addOnCompleteListener {
                    googleLauncher.launch(googleClient.signInIntent)
                }
            }
        }
        
        btnBiometric.setOnClickListener {
            performBiometricLogin()
        }

        tvGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegistrationActivity::class.java))
            finish()
        }

        tvForgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
    }

    private fun handleAuthResult(success: Boolean, errorMessage: String?) {
        if (!success) {
            setLoading(false)
            showError(errorMessage ?: getString(R.string.authentication_failed))
            return
        }

        // After auth success, check profile existence
        lifecycleScope.launch {
            val profileResult = authRepository.fetchMyProfile()
            if (profileResult.isSuccess) {
                val profile = profileResult.getOrNull()
                navigateToMain(profile?.userType)
            } else {
                val ex = profileResult.exceptionOrNull()
                when (ex) {
                    is AppApiException.NotFound -> {
                        // Deny entry for users without a profile and inform them
                        showError(getString(R.string.no_account_found))
                        auth.signOut()
                    }
                    is AppApiException.NetworkError -> showRetry(getString(R.string.offline_try_again))
                    is AppApiException.InternalServerError, is AppApiException.ServerUnavailable -> showRetry(getString(R.string.server_error_try_again))
                    else -> showError(ApiErrorHandler.getErrorMessage(ex ?: Exception("Unknown error")))
                }
            }
            setLoading(false)
        }
    }

    private fun navigateToMain(userType: vcmsa.projects.careerconnect.domain.model.UserType? = null) {
        val intent = Intent(this, MainActivity::class.java)
        userType?.let { intent.putExtra("user_type", it.name) }
        startActivity(intent)
        finish()
    }

    private fun navigateToProfileCompletion() {
        startActivity(Intent(this, ProfileCompletionActivity::class.java))
        finish()
    }

    private fun showRetry(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        // You can also show a Snackbar with Retry action.
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun setLoading(loading: Boolean) {
        progress.visibility = if (loading) View.VISIBLE else View.GONE
        btnLogin.isEnabled = !loading
        btnGoogle.isEnabled = !loading
        btnBiometric.isEnabled = !loading
    }
    
    // ===== BIOMETRIC AUTHENTICATION METHODS =====
    
    /**
     * Setup biometric authentication on activity start
     */
    private fun setupBiometric() {
        val availability = biometricAuthManager.checkBiometricAvailability()
        
        when (availability) {
            BiometricAuthManager.BiometricAvailability.AVAILABLE -> {
                // Check if user has enabled biometric and has stored credentials
                if (biometricPreferences.isBiometricEnabled() && 
                    biometricPreferences.hasStoredCredentials()) {
                    btnBiometric.visibility = View.VISIBLE
                    
                    // Pre-fill email if available
                    biometricPreferences.getStoredEmail()?.let { email ->
                        etEmail.setText(email)
                    }
                }
            }
            BiometricAuthManager.BiometricAvailability.NONE_ENROLLED -> {
                // Don't show button, but could show a hint to enable fingerprint
            }
            else -> {
                // Biometric not available
                btnBiometric.visibility = View.GONE
            }
        }
    }
    
    /**
     * Perform biometric authentication
     */
    private fun performBiometricLogin() {
        val availability = biometricAuthManager.checkBiometricAvailability()
        
        if (availability != BiometricAuthManager.BiometricAvailability.AVAILABLE) {
            showError(biometricAuthManager.getAvailabilityMessage(availability))
            return
        }
        
        if (!biometricPreferences.hasStoredCredentials()) {
            showError(getString(R.string.biometric_not_available))
            return
        }
        
        biometricAuthManager.showBiometricPrompt(
            title = getString(R.string.biometric_prompt_title),
            subtitle = getString(R.string.biometric_prompt_subtitle),
            negativeButtonText = getString(R.string.biometric_prompt_cancel),
            onSuccess = {
                // Biometric authentication successful, retrieve stored credentials
                val credentials = biometricPreferences.getStoredCredentials()
                if (credentials != null) {
                    val (email, password) = credentials
                    
                    // Authenticate with Firebase using stored credentials
                    setLoading(true)
                    lifecycleScope.launch {
                        try {
                            val result = registrationRepository.signInWithEmail(email, password)
                            if (result.isSuccess) {
                                Toast.makeText(
                                    this@LoginActivity,
                                    getString(R.string.biometric_login_success),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            handleAuthResult(result.isSuccess, result.exceptionOrNull()?.message)
                        } catch (e: Exception) {
                            setLoading(false)
                            showError(getString(R.string.biometric_login_failed))
                        }
                    }
                } else {
                    showError(getString(R.string.biometric_login_failed))
                }
            },
            onError = { errorMessage ->
                showError(errorMessage)
            },
            onFailed = {
                Toast.makeText(
                    this,
                    getString(R.string.biometric_authentication_failed),
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
    }
    
    /**
     * Check if we should prompt to enable biometric
     */
    private fun shouldPromptEnableBiometric(): Boolean {
        val availability = biometricAuthManager.checkBiometricAvailability()
        return availability == BiometricAuthManager.BiometricAvailability.AVAILABLE &&
                !biometricPreferences.isBiometricEnabled()
    }
    
    /**
     * Prompt user to enable biometric authentication after successful login
     */
    private fun promptEnableBiometric(email: String, password: String) {
        // Check if biometric is available and not already enabled
        val availability = biometricAuthManager.checkBiometricAvailability()
        
        // Debug log to see what's happening
        android.util.Log.d("BiometricAuth", "Biometric availability: $availability")
        android.util.Log.d("BiometricAuth", "Biometric enabled: ${biometricPreferences.isBiometricEnabled()}")
        
        if (availability != BiometricAuthManager.BiometricAvailability.AVAILABLE) {
            // Biometric not available, show info message and continue login
            val message = when (availability) {
                BiometricAuthManager.BiometricAvailability.NO_HARDWARE -> 
                    "Biometric hardware not available on this device"
                BiometricAuthManager.BiometricAvailability.NONE_ENROLLED -> 
                    "No fingerprints enrolled. Set up fingerprint in device settings to use biometric login."
                BiometricAuthManager.BiometricAvailability.HARDWARE_UNAVAILABLE -> 
                    "Biometric hardware temporarily unavailable"
                else -> "Biometric authentication not available: $availability"
            }
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            
            // Continue with normal login
            setLoading(true)
            lifecycleScope.launch {
                handleAuthResult(true, null)
            }
            return
        }
        
        if (!biometricPreferences.isBiometricEnabled()) {
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.enable_biometric_title))
                .setMessage(getString(R.string.enable_biometric_message))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.enable_biometric_yes)) { dialog, _ ->
                    // Enable biometric and save credentials
                    biometricPreferences.setBiometricEnabled(true)
                    biometricPreferences.saveCredentials(email, password)
                    Toast.makeText(
                        this,
                        getString(R.string.biometric_enabled),
                        Toast.LENGTH_SHORT
                    ).show()
                    dialog.dismiss()
                    
                    // Now continue with login
                    setLoading(true)
                    lifecycleScope.launch {
                        handleAuthResult(true, null)
                    }
                }
                .setNegativeButton(getString(R.string.enable_biometric_no)) { dialog, _ ->
                    dialog.dismiss()
                    
                    // Continue with login without enabling biometric
                    setLoading(true)
                    lifecycleScope.launch {
                        handleAuthResult(true, null)
                    }
                }
                .show()
        } else {
            // Already enabled, just continue
            setLoading(true)
            lifecycleScope.launch {
                handleAuthResult(true, null)
            }
        }
    }
}