//CODE ATTRIBUTION
//01
//Google Sign In Options
//Adapted from: Google Developers. (2025). GoogleSignInOptions. [online] Google Developers.
//Available at: https://developers.google.com/android/reference/com/google/android/gms/auth/api/signin/GoogleSignInOptions
//Date Accessed: 30 September 2025

//02
//Google Sign In
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
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import vcmsa.projects.careerconnect.R
import vcmsa.projects.careerconnect.ui.authentication.LoginActivity
import vcmsa.projects.careerconnect.ui.profile.ProfileCompletionActivity
import vcmsa.projects.careerconnect.utils.LanguageManager

/**
 * Registration activity that handles both email/password and Google Sign-In
 */
class RegistrationActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    
    private lateinit var btnEmailSignIn: MaterialButton
    private lateinit var btnGoogleSignIn: MaterialButton
    private lateinit var progressBar: CircularProgressIndicator

    // Google Sign-In launcher
    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        handleGoogleSignInResult(result.resultCode, result.data)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Apply saved language before setting content view
        val languageManager = LanguageManager(this)
        languageManager.applySavedLanguage()

        setContentView(R.layout.activity_registration)

        initializeViews()
        setupFirebase()
        setupGoogleSignIn()
        setupClickListeners()
    }

    private fun initializeViews() {
        btnEmailSignIn = findViewById(R.id.btnEmailSignIn)
        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setupFirebase() {
        auth = FirebaseAuth.getInstance()
    }

    private fun setupGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun setupClickListeners() {
        btnEmailSignIn.setOnClickListener {
            // Navigate to profile completion to allow entering email & password (editable)
            val intent = Intent(this, ProfileCompletionActivity::class.java)
            intent.putExtra("email_editable", true)
            startActivity(intent)
        }

        btnGoogleSignIn.setOnClickListener {
            signInWithGoogle()
        }

        // No login link on registration page per latest requirements
    }

    // Email dialog removed; email flow now goes directly to ProfileCompletionActivity

    private fun signInWithGoogle() {
        setLoading(true)
        // Clear any default account to force the chooser
        googleSignInClient.signOut().addOnCompleteListener {
            googleSignInClient.revokeAccess().addOnCompleteListener {
                val signInIntent = googleSignInClient.signInIntent
                googleSignInLauncher.launch(signInIntent)
            }
        }
    }

    private fun handleGoogleSignInResult(resultCode: Int, data: Intent?) {
        if (resultCode != RESULT_OK) {
            setLoading(false)
            return
        }
        if (data == null) {
            setLoading(false)
            return
        }
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)
            account?.let { googleAccount ->
                // Defer Firebase sign-in. Send token and email to ProfileCompletionActivity
                setLoading(false)
                val intent = Intent(this, ProfileCompletionActivity::class.java).apply {
                    putExtra("google_id_token", googleAccount.idToken)
                    putExtra("prefill_email", googleAccount.email)
                    putExtra("email_editable", false)
                }
                startActivity(intent)
            } ?: run {
                // No account returned, likely user cancelled
                setLoading(false)
            }
        } catch (e: ApiException) {
            // If user cancels, status code is CommonStatusCodes.CANCELED
            setLoading(false)
            if (e.statusCode != com.google.android.gms.common.api.CommonStatusCodes.CANCELED) {
                showError(getString(R.string.google_signin_failed, e.message ?: "Unknown error"))
            }
        }
    }

    private fun handleEmailSignIn(email: String, password: String) {
        setLoading(true)
        
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                if (result.user != null) {
                    // Successfully signed up, proceed to profile completion
                    proceedToProfileCompletion()
                } else {
                    setLoading(false)
                    showError(getString(R.string.registration_failed_retry))
                }
            } catch (e: Exception) {
                setLoading(false)
                showError(getString(R.string.registration_failed_retry) + ": ${e.message}")
            }
        }
    }

    private fun proceedToProfileCompletion() {
        setLoading(false)
        val intent = Intent(this, ProfileCompletionActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onResume() {
        super.onResume()
        // Wire up Back button to return to LoginActivity
        findViewById<com.google.android.material.button.MaterialButton>(R.id.btnBack)?.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            startActivity(intent)
            finish()
        }
    }

    private fun setLoading(loading: Boolean) {
        progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        btnEmailSignIn.isEnabled = !loading
        btnGoogleSignIn.isEnabled = !loading
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    // Old dialog callback removed
}