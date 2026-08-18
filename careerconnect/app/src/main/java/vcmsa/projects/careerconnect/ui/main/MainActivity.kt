//CODE ATTRIBUTION
//01
//AppCompatActivity
//Adapted from: Android Developers. (2025). AppCompatActivity. [online] Android Developers.
//Available at: https://developer.android.com/reference/androidx/appcompat/app/AppCompatActivity
//Date Accessed: 30 September 2025

//02
//Bundle (onCreate)
//Adapted from: Android Developers. (2025). Bundle. [online] Android Developers.
//Available at: https://developer.android.com/reference/android/os/Bundle
//Date Accessed: 30 September 2025

//03
//MaterialCardView
//Adapted from: Android Developers. (2025). MaterialCardView. [online] Android Developers.
//Available at: https://developer.android.com/reference/com/google/android/material/card/MaterialCardView
//Date Accessed: 30 September 2025

//04
//MaterialToolbar
//Adapted from: Android Developers. (2025). MaterialToolbar. [online] Android Developers.
//Available at: https://developer.android.com/reference/com/google/android/material/appbar/MaterialToolbar
//Date Accessed: 30 September 2025

//05
//Toolbar (setSupportActionBar)
//Adapted from: Android Developers. (2025). Toolbar. [online] Android Developers.
//Available at: https://developer.android.com/reference/androidx/appcompat/widget/Toolbar
//Date Accessed: 30 September 2025

//06
//DrawerLayout
//Adapted from: Android Developers. (2025). DrawerLayout. [online] Android Developers.
//Available at: https://developer.android.com/reference/androidx/drawerlayout/widget/DrawerLayout
//Date Accessed: 30 September 2025

//07
//ActionBarDrawerToggle
//Adapted from: Android Developers. (2025). ActionBarDrawerToggle. [online] Android Developers.
//Available at: https://developer.android.com/reference/androidx/appcompat/app/ActionBarDrawerToggle
//Date Accessed: 30 September 2025

//08
//NavigationView
//Adapted from: Android Developers. (2025). NavigationView. [online] Android Developers.
//Available at: https://developer.android.com/reference/com/google/android/material/navigation/NavigationView
//Date Accessed: 30 September 2025

//09
//GravityCompat
//Adapted from: Android Developers. (2025). GravityCompat. [online] Android Developers.
//Available at: https://developer.android.com/reference/androidx/core/view/GravityCompat
//Date Accessed: 30 September 2025

package vcmsa.projects.careerconnect.ui.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.ActionBarDrawerToggle
import android.widget.Button
import com.google.android.material.card.MaterialCardView
import android.content.Intent
import vcmsa.projects.careerconnect.R
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import vcmsa.projects.careerconnect.data.network.ApiClient
import vcmsa.projects.careerconnect.session.SessionManager
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import vcmsa.projects.careerconnect.data.repository.ProfileRepository
import vcmsa.projects.careerconnect.ui.authentication.LoginActivity
import vcmsa.projects.careerconnect.domain.model.UserType
import vcmsa.projects.careerconnect.ui.settings.SettingsActivity
import vcmsa.projects.careerconnect.ui.recruiter.JobPostingActivity
import vcmsa.projects.careerconnect.ui.recruiter.RecruiterJobApplicationsActivity
import vcmsa.projects.careerconnect.ui.jobseeker.AllJobsActivity
import vcmsa.projects.careerconnect.ui.jobseeker.BookmarkedJobsActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import com.google.android.material.navigation.NavigationView
import com.google.android.material.appbar.MaterialToolbar
import androidx.core.view.GravityCompat
import vcmsa.projects.careerconnect.utils.LanguageManager
import vcmsa.projects.careerconnect.utils.ThemeManager
import vcmsa.projects.careerconnect.utils.FontSizeManager

/**
 * Main activity that shows different content based on user type
 */
class MainActivity : AppCompatActivity() {
    
    private val sessionManager = SessionManager()
    private var currentUserType: UserType = UserType.JOB_SEEKER
    private val profileRepository = ProfileRepository()
    private lateinit var drawerToggle: ActionBarDrawerToggle
    private lateinit var drawerLayout: DrawerLayout
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Apply saved language, theme, and font size before setting content view
        val languageManager = LanguageManager(this)
        languageManager.applySavedLanguage()
        
        val themeManager = ThemeManager(this)
        themeManager.applySavedTheme()
        
        val fontSizeManager = FontSizeManager(this)
        fontSizeManager.applySavedFontSize(resources)

        setContentView(R.layout.activity_main)
        setupDrawer()
        
        // Handle notification intent (when app is opened from a notification)
        handleNotificationIntent(intent)
        
        // Get user type from intent or fetch from profile as fallback
        val userTypeString = intent.getStringExtra("user_type")
        if (userTypeString.isNullOrBlank()) {
            lifecycleScope.launch {
                val result = profileRepository.getProfile()
                result.onSuccess { profile ->
                    currentUserType = profile.userType
                    setupUI()
                }.onFailure {
                    currentUserType = UserType.JOB_SEEKER
                    setupUI()
                }
            }
        } else {
            currentUserType = try {
                UserType.valueOf(userTypeString)
            } catch (e: IllegalArgumentException) {
                UserType.JOB_SEEKER
            }
            setupUI()
        }

        // Register FCM token for push notifications (non-blocking)
        registerFCMTokenAsync()

        setupClickListeners()
    }
    
    /**
     * Handle new intents when app is already running
     * This is called when user taps a notification while app is in the foreground
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent) // Update the intent so getIntent() returns the latest one
        handleNotificationIntent(intent)

    }

    private fun registerFCMTokenAsync() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            android.util.Log.w("MainActivity", "No current user logged in, skipping FCM token registration")
            return
        }

        // Launch FCM registration in background without blocking UI
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                android.util.Log.d("MainActivity", "Starting FCM token registration...")

                // Set a timeout for the entire operation
                val timeoutJob = launch {
                    kotlinx.coroutines.delay(10000) // 10 second timeout
                    android.util.Log.w("MainActivity", "FCM token registration timed out")
                }

                val registrationJob = launch {
                    try {
                        // Get FCM token with timeout
                        val fcmToken = withTimeoutOrNull(5000) { // 5 second timeout
                            FirebaseMessaging.getInstance().token.await()
                        }

                        if (fcmToken != null) {
                            android.util.Log.d("MainActivity", "Got FCM token: ${fcmToken.take(20)}...")
                        // Register with backend (with timeout)
                        withTimeoutOrNull(3000) { // 3 second timeout for backend call
                            registerFCMTokenWithBackend(currentUser.uid, fcmToken)
                        } ?: run {
                            android.util.Log.w("MainActivity", "Backend FCM registration timed out")
                        }
                        } else {
                            android.util.Log.w("MainActivity", "FCM token retrieval timed out")
                        }

                        timeoutJob.cancel() // Cancel timeout if successful
                    } catch (e: Exception) {
                        android.util.Log.e("MainActivity", "FCM token registration failed", e)
                        timeoutJob.cancel()
                    }
                }

                // Wait for either completion or timeout
                registrationJob.join()

            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "FCM registration error", e)
            }
        }
    }

    private suspend fun registerFCMTokenWithBackend(userId: String, fcmToken: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            android.util.Log.w("MainActivity", "No current user for FCM token registration")
            return
        }

        // Get ID token using Task API with timeout
        val idTokenResult = withTimeoutOrNull(2000) { // 2 second timeout
            currentUser.getIdToken(false).await()
        }
        val idToken = idTokenResult?.token

        if (idToken != null) {
            try {
                // Make direct HTTP call to register FCM token
                val url = java.net.URL("https://careerconnectapi2.onrender.com/api/notifications/fcm/tokens")
                val connection = url.openConnection() as java.net.HttpURLConnection

                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty("Authorization", "Bearer $idToken")
                connection.doOutput = true

                // Get device info
                val deviceId = android.provider.Settings.Secure.getString(
                    this@MainActivity.contentResolver,
                    android.provider.Settings.Secure.ANDROID_ID
                )

                // Create request body
                val requestBody = org.json.JSONObject(mapOf(
                    "fcm_token" to fcmToken,
                    "device_id" to deviceId,
                    "device_type" to "android"
                )).toString()

                connection.outputStream.use { os ->
                    os.write(requestBody.toByteArray(Charsets.UTF_8))
                }

                val responseCode = connection.responseCode
                connection.disconnect()

                if (responseCode == 200) {
                    android.util.Log.d("MainActivity", "FCM token registered successfully for user: $userId")
                } else {
                    android.util.Log.e("MainActivity", "Failed to register FCM token. Response code: $responseCode")
                    // Don't crash the app if FCM registration fails - just log it
                    runOnUiThread {
                        android.widget.Toast.makeText(
                            this@MainActivity,
                            getString(R.string.push_notifications_unavailable_server),
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Network error registering FCM token", e)
                // Don't crash the app if network fails - just show a message
                runOnUiThread {
                    android.widget.Toast.makeText(
                        this@MainActivity,
                        getString(R.string.push_notifications_unavailable_network),
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                }
            }
        } else {
            android.util.Log.e("MainActivity", "Failed to get ID token for FCM registration")
            runOnUiThread {
                android.widget.Toast.makeText(
                    this@MainActivity,
                    getString(R.string.push_notifications_unavailable_auth),
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    
    /**
     * Handle notification intent when app is opened from a notification
     * This is called when the app is in the background and user taps a notification
     */
    private fun handleNotificationIntent(intent: Intent?) {
        if (intent == null) return
        
        // Check if app was opened from a notification
        val notificationType = intent.getStringExtra("notification_type")
        if (notificationType != null) {
            android.util.Log.d("MainActivity", "App opened from notification: $notificationType")
            
            // Handle different notification types
            when (notificationType) {
                "application_update" -> {
                    val jobId = intent.getStringExtra("job_id")
                    val applicationId = intent.getStringExtra("application_id")
                    val jobTitle = intent.getStringExtra("job_title")
                    val status = intent.getStringExtra("status")
                    
                    android.util.Log.d("MainActivity", "Application update notification - Job: $jobTitle, Status: $status")
                    
                    // Show a toast or navigate to application details
                    android.widget.Toast.makeText(
                        this,
                        getString(R.string.application_status_updated, status ?: "Unknown"),
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                    
                    // TODO: Navigate to application details if needed
                    // For example: startActivity(Intent(this, ApplicationDetailsActivity::class.java).apply {
                    //     putExtra("application_id", applicationId)
                    // })
                }
                "new_application" -> {
                    val jobId = intent.getStringExtra("job_id")
                    val applicationId = intent.getStringExtra("application_id")
                    val jobTitle = intent.getStringExtra("job_title")
                    
                    android.util.Log.d("MainActivity", "New application notification - Job: $jobTitle")
                    
                    // TODO: Navigate to application details for recruiters
                }
                else -> {
                    android.util.Log.d("MainActivity", "General notification: $notificationType")
                }
            }
        }
    }
    
    private fun setupUI() {
        when (currentUserType) {
            UserType.JOB_SEEKER -> {
                setupJobSeekerUI()
                updateDrawerMenuForRole()
            }
            UserType.RECRUITER -> {
                setupRecruiterUI()
                updateDrawerMenuForRole()
            }
        }
    }
    
    private fun setupJobSeekerUI() {
        // Show job seeker explanation, hide recruiter explanation
        findViewById<MaterialCardView>(R.id.sectionJobSeeker).visibility = android.view.View.VISIBLE
        findViewById<MaterialCardView>(R.id.sectionRecruiter).visibility = android.view.View.GONE
    }
    
    private fun setupRecruiterUI() {
        // Show recruiter explanation, hide job seeker explanation
        findViewById<MaterialCardView>(R.id.sectionJobSeeker).visibility = android.view.View.GONE
        findViewById<MaterialCardView>(R.id.sectionRecruiter).visibility = android.view.View.VISIBLE
    }
    
    private fun setupClickListeners() {
        // No dashboard actions; navigation handled via the side drawer
    }

    private fun setupDrawer() {
        drawerLayout = findViewById(R.id.drawer_layout)
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerToggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(drawerToggle)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        drawerToggle.syncState()

        val navView = findViewById<NavigationView>(R.id.nav_view)
        navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_all_jobs -> startActivity(Intent(this, AllJobsActivity::class.java))
                R.id.nav_bookmarked_jobs -> startActivity(Intent(this, BookmarkedJobsActivity::class.java))
                R.id.nav_job_posting -> startActivity(Intent(this, JobPostingActivity::class.java))
                R.id.nav_job_applications -> startActivity(Intent(this, RecruiterJobApplicationsActivity::class.java))
                R.id.nav_settings -> startActivity(Intent(this, SettingsActivity::class.java))
                R.id.nav_logout -> {
                    lifecycleScope.launch {
                        ApiClient.signOut(this@MainActivity)
                        sessionManager.logout()
                        val intent = Intent(this@MainActivity, LoginActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        }
                        startActivity(intent)
                        finish()
                    }
                }
            }
            drawerLayout.closeDrawers()
            true
        }
    }

    private fun updateDrawerMenuForRole() {
        val navView = findViewById<NavigationView>(R.id.nav_view)
        val menu = navView.menu
        val isRecruiter = currentUserType == UserType.RECRUITER

        // Common
        menu.findItem(R.id.nav_all_jobs)?.isVisible = true
        menu.findItem(R.id.nav_settings)?.isVisible = true
        menu.findItem(R.id.nav_logout)?.isVisible = true

        // Role-specific
        menu.findItem(R.id.nav_bookmarked_jobs)?.isVisible = !isRecruiter
        menu.findItem(R.id.nav_job_posting)?.isVisible = isRecruiter
        menu.findItem(R.id.nav_job_applications)?.isVisible = isRecruiter
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        if (::drawerToggle.isInitialized && drawerToggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (::drawerLayout.isInitialized && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}