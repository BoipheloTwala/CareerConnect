//CODE ATTRIBUTION
//01
//App CompatActivity
//Adapted from: Android Developers. (2025). AppCompatActivity. [online] Android Developers.
//Available at: https://developer.android.com/reference/androidx/appcompat/app/AppCompatActivity
//Date Accessed: 15 September 2025

//02
//Alert Dialog
//Adapted from: Android Developers. (2025). AlertDialog.Builder. [online] Android Developers.
//Available at: https://developer.android.com/reference/androidx/appcompat/app/AlertDialog.Builder
//Date Accessed: 15 September 2025

//03
//Material Button
//Adapted from: Material Design. (2025). Buttons. [online] Material Design.
//Available at: https://m3.material.io/components/buttons/overview
//Date Accessed: 15 September 2025

//04
//Circular Progress Indicator
//Adapted from: Material Design. (2025). Progress indicators. [online] Material Design.
//Available at: https://m3.material.io/components/progress-indicators/overview
//Date Accessed: 15 September 2025

//05
//Toast
//Adapted from: Android Developers. (2025). Toast. [online] Android Developers.
//Available at: https://developer.android.com/reference/android/widget/Toast
//Date Accessed: 15 September 2025

//06
//Jetpack Compose Button
//Adapted from: Android Developers. (2025). Button (Jetpack Compose). [online] Android Developers.
//Available at: https://developer.android.com/develop/ui/compose/components/button
//Date Accessed: 15 September 2025

package vcmsa.projects.careerconnect.ui.settings

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textview.MaterialTextView
import kotlinx.coroutines.launch
import vcmsa.projects.careerconnect.R
import vcmsa.projects.careerconnect.data.repository.AuthRepository
import vcmsa.projects.careerconnect.data.repository.ProfileRepository
import vcmsa.projects.careerconnect.domain.model.UserProfile
import vcmsa.projects.careerconnect.session.SessionManager
import vcmsa.projects.careerconnect.ui.authentication.LoginActivity
import vcmsa.projects.careerconnect.ui.main.MainActivity
import vcmsa.projects.careerconnect.data.network.ApiClient
import vcmsa.projects.careerconnect.utils.LanguageItem
import vcmsa.projects.careerconnect.utils.LanguageManager
import vcmsa.projects.careerconnect.utils.ThemeManager
import vcmsa.projects.careerconnect.utils.ThemeItem
import vcmsa.projects.careerconnect.utils.FontSizeManager
import vcmsa.projects.careerconnect.utils.FontSizeItem

/**
 * Settings activity for user preferences and account management
 */
class SettingsActivity : AppCompatActivity() {

    private lateinit var tvUserName: MaterialTextView
    private lateinit var tvUserEmail: MaterialTextView
    private lateinit var tvUserType: MaterialTextView
    private lateinit var switchNotifications: SwitchMaterial
    private lateinit var switchEmailUpdates: SwitchMaterial
    private lateinit var spinnerLanguage: Spinner
    private lateinit var spinnerTheme: Spinner
    private lateinit var spinnerFontSize: Spinner
    private lateinit var btnEditProfile: MaterialButton
    private lateinit var btnChangePassword: MaterialButton
    private lateinit var btnDeleteAccount: MaterialButton
    private lateinit var btnLogout: MaterialButton
    private lateinit var progressBar: CircularProgressIndicator

    private val sessionManager = SessionManager()
    private val authRepository = AuthRepository()
    private val profileRepository = ProfileRepository()
    private lateinit var languageManager: LanguageManager
    private lateinit var themeManager: ThemeManager
    private lateinit var fontSizeManager: FontSizeManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize managers after context is available
        languageManager = LanguageManager(this)
        themeManager = ThemeManager(this)
        fontSizeManager = FontSizeManager(this)

        // Apply saved language, theme, and font size before setting content view
        languageManager.applySavedLanguage()
        themeManager.applySavedTheme()
        fontSizeManager.applySavedFontSize(resources)

        setContentView(R.layout.activity_settings)

        initializeViews()
        setupClickListeners()
        loadUserProfile()
    }

    private fun initializeViews() {
        tvUserName = findViewById(R.id.tvUserName)
        tvUserEmail = findViewById(R.id.tvUserEmail)
        tvUserType = findViewById(R.id.tvUserType)
        switchNotifications = findViewById(R.id.switchNotifications)
        switchEmailUpdates = findViewById(R.id.switchEmailUpdates)
        spinnerLanguage = findViewById(R.id.spinnerLanguage)
        spinnerTheme = findViewById(R.id.spinnerTheme)
        spinnerFontSize = findViewById(R.id.spinnerFontSize)
        btnEditProfile = findViewById(R.id.btnEditProfile)
        btnChangePassword = findViewById(R.id.btnChangePassword)
        btnDeleteAccount = findViewById(R.id.btnDeleteAccount)
        btnLogout = findViewById(R.id.btnLogout)
        progressBar = findViewById(R.id.progressBar)

        // Back button
        findViewById<MaterialButton>(R.id.btnBack)?.setOnClickListener {
            finish()
        }

        // Initialize spinners and switches
        setupLanguageSpinner()
        setupThemeSpinner()
        setupFontSizeSpinner()
    }

    private fun setupClickListeners() {
        btnEditProfile.setOnClickListener {
            startActivity(Intent(this, vcmsa.projects.careerconnect.ui.profile.EditProfileActivity::class.java))
        }

        btnChangePassword.setOnClickListener {
            startActivity(Intent(this, ChangePasswordActivity::class.java))
        }

        btnDeleteAccount.setOnClickListener {
            showDeleteAccountConfirmation()
        }

        btnLogout.setOnClickListener {
            logout()
        }

        switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            // TODO: Save notification preference
            Toast.makeText(this, getString(R.string.notification_preference, isChecked.toString()), Toast.LENGTH_SHORT).show()
        }

        switchEmailUpdates.setOnCheckedChangeListener { _, isChecked ->
            // TODO: Save email updates preference
            Toast.makeText(this, getString(R.string.email_updates_preference, isChecked.toString()), Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupThemeSpinner() {
        val themes = themeManager.getAvailableThemes()
        val themeNames = themes.map { it.displayName }

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, themeNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTheme.adapter = adapter

        // Set current selection
        val currentTheme = themeManager.getThemeMode()
        val currentIndex = themes.indexOfFirst { it.mode == currentTheme }
        if (currentIndex >= 0) {
            spinnerTheme.setSelection(currentIndex)
        }

        // Handle theme selection
        spinnerTheme.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedTheme = themes[position]
                val currentSelected = themeManager.getThemeMode()

                if (selectedTheme.mode != currentSelected) {
                    // Apply theme immediately
                    val success = themeManager.setThemeMode(selectedTheme.mode)
                    if (success) {
                        Toast.makeText(
                            this@SettingsActivity,
                            getString(R.string.theme_changed),
                            Toast.LENGTH_SHORT
                        ).show()
                        // Restart activity to apply theme changes properly
                        recreate()
                    } else {
                        Toast.makeText(
                            this@SettingsActivity,
                            getString(R.string.failed_to_change_theme),
                            Toast.LENGTH_SHORT
                        ).show()
                        // Revert selection
                        val revertIndex = themes.indexOfFirst { it.mode == currentSelected }
                        if (revertIndex >= 0) {
                            spinnerTheme.setSelection(revertIndex)
                        }
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
    }

    private fun setupFontSizeSpinner() {
        val fontSizes = fontSizeManager.getAvailableFontSizes()
        val fontSizeNames = fontSizes.map { it.displayName }

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, fontSizeNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFontSize.adapter = adapter

        // Set current selection
        val currentFontSize = fontSizeManager.getFontSize()
        val currentIndex = fontSizes.indexOfFirst { it.size == currentFontSize }
        if (currentIndex >= 0) {
            spinnerFontSize.setSelection(currentIndex)
        }

        // Handle font size selection
        spinnerFontSize.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedFontSize = fontSizes[position]
                val currentSelected = fontSizeManager.getFontSize()

                if (selectedFontSize.size != currentSelected) {
                    // Save font size preference
                    val success = fontSizeManager.setFontSize(selectedFontSize.size)
                    if (success) {
                        Toast.makeText(
                            this@SettingsActivity,
                            getString(R.string.font_size_changed),
                            Toast.LENGTH_SHORT
                        ).show()
                        // Restart activity to apply font size changes properly
                        recreate()
                    } else {
                        Toast.makeText(
                            this@SettingsActivity,
                            getString(R.string.failed_to_change_font_size),
                            Toast.LENGTH_SHORT
                        ).show()
                        // Revert selection
                        val revertIndex = fontSizes.indexOfFirst { it.size == currentSelected }
                        if (revertIndex >= 0) {
                            spinnerFontSize.setSelection(revertIndex)
                        }
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
    }

    private fun setupLanguageSpinner() {
        val languages = languageManager.getAvailableLanguages()
        val languageNames = languages.map { it.displayName }

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, languageNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerLanguage.adapter = adapter

        // Set current selection
        val currentLanguage = languageManager.getSelectedLanguage()
        val currentIndex = languages.indexOfFirst { it.code == currentLanguage }
        if (currentIndex >= 0) {
            spinnerLanguage.setSelection(currentIndex)
        }

        // Handle language selection
        spinnerLanguage.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedLanguage = languages[position]
                val currentSelected = languageManager.getSelectedLanguage()

                if (selectedLanguage.code != currentSelected) {
                    // Show confirmation dialog
                    androidx.appcompat.app.AlertDialog.Builder(this@SettingsActivity)
                        .setTitle(getString(R.string.language))
                        .setMessage(getString(R.string.language_changed_restart))
                        .setPositiveButton(getString(R.string.cancel)) { _, _ ->
                            // Revert selection
                            val revertIndex = languages.indexOfFirst { it.code == currentSelected }
                            if (revertIndex >= 0) {
                                spinnerLanguage.setSelection(revertIndex)
                            }
                        }
                        .setNegativeButton(getString(R.string.apply)) { _, _ ->
                            // Apply language change
                            val success = languageManager.setSelectedLanguage(selectedLanguage.code)
                            if (success) {
                                Toast.makeText(
                                    this@SettingsActivity,
                                    getString(R.string.language_changed_restart),
                                    Toast.LENGTH_LONG
                                ).show()
                                // Restart the entire app to apply language change globally
                                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                    val intent = packageManager.getLaunchIntentForPackage(packageName)
                                    intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                    startActivity(intent)
                                    finishAffinity() // Close all activities
                                }, 1500)
                            } else {
                                Toast.makeText(
                                    this@SettingsActivity,
                                    getString(R.string.failed_to_change_language),
                                    Toast.LENGTH_SHORT
                                ).show()
                                // Revert selection
                                val revertIndex = languages.indexOfFirst { it.code == currentSelected }
                                if (revertIndex >= 0) {
                                    spinnerLanguage.setSelection(revertIndex)
                                }
                            }
                        }
                        .setCancelable(false)
                        .show()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
    }

    private fun loadUserProfile() {
        setLoading(true)
        
        lifecycleScope.launch {
            try {
                val profileResult = authRepository.fetchMyProfile()
                profileResult.fold(
                    onSuccess = { profile ->
                        displayUserProfile(profile)
                        setLoading(false)
                    },
                    onFailure = { error ->
                        setLoading(false)
                        showError(getString(R.string.failed_to_load_profile) + ": ${error.message}")
                    }
                )
            } catch (e: Exception) {
                setLoading(false)
                showError(getString(R.string.an_error_occurred) + ": ${e.message}")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Reload profile when returning from edit screen
        loadUserProfile()
    }

    private fun displayUserProfile(profile: UserProfile) {
        tvUserName.text = "${profile.firstName} ${profile.lastName}"
        tvUserEmail.text = profile.email
        tvUserType.text = when (profile.userType) {
            vcmsa.projects.careerconnect.domain.model.UserType.JOB_SEEKER -> getString(R.string.job_seeker_label)
            vcmsa.projects.careerconnect.domain.model.UserType.RECRUITER -> getString(R.string.recruiter_label)
        }
    }

    private fun showDeleteAccountConfirmation() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(getString(R.string.delete_account_title))
            .setMessage(getString(R.string.delete_account_confirmation))
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                deleteAccount()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun deleteAccount() {
        setLoading(true)
        
        lifecycleScope.launch {
            try {
                // TODO: Implement account deletion API call
                Toast.makeText(this@SettingsActivity, getString(R.string.account_deletion_coming_soon), Toast.LENGTH_SHORT).show()
                setLoading(false)
            } catch (e: Exception) {
                setLoading(false)
                showError(getString(R.string.failed_to_delete_account) + ": ${e.message}")
            }
        }
    }

    private fun logout() {
        setLoading(true)
        
        lifecycleScope.launch {
            try {
                ApiClient.signOut(this@SettingsActivity)
                sessionManager.logout()
                val intent = Intent(this@SettingsActivity, LoginActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                }
                startActivity(intent)
                finish()
            } catch (e: Exception) {
                setLoading(false)
                showError(getString(R.string.logout_error, e.message))
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        btnEditProfile.isEnabled = !loading
        btnChangePassword.isEnabled = !loading
        btnDeleteAccount.isEnabled = !loading
        btnLogout.isEnabled = !loading
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
