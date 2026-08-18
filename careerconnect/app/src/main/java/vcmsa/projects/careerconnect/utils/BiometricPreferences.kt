//CODE ATTRIBUTION
//01
//SharedPreferences
//Adapted from: Android Developers. (2025). SharedPreferences. [online] Android Developers.
//Available at: https://developer.android.com/reference/android/content/SharedPreferences
//Date Accessed: 15 November 2025

//02
//EncryptedSharedPreferences
//Adapted from: Android Developers. (2025). EncryptedSharedPreferences. [online] Android Developers.
//Available at: https://developer.android.com/reference/androidx/security/crypto/EncryptedSharedPreferences
//Date Accessed: 15 November 2025

//03
//MasterKey
//Adapted from: Android Developers. (2025). MasterKey. [online] Android Developers.
//Available at: https://developer.android.com/reference/androidx/security/crypto/MasterKey
//Date Accessed: 15 November 2025

package vcmsa.projects.careerconnect.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Manages biometric authentication preferences and credentials
 * Uses EncryptedSharedPreferences for secure credential storage
 */
class BiometricPreferences(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )
    
    private val encryptedPrefs: SharedPreferences by lazy {
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            
            EncryptedSharedPreferences.create(
                context,
                ENCRYPTED_PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            // Fallback to regular SharedPreferences if encryption fails
            context.getSharedPreferences(ENCRYPTED_PREFS_NAME, Context.MODE_PRIVATE)
        }
    }

    companion object {
        private const val PREFS_NAME = "biometric_prefs"
        private const val ENCRYPTED_PREFS_NAME = "biometric_encrypted_prefs"
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
        private const val KEY_STORED_EMAIL = "stored_email"
        private const val KEY_STORED_PASSWORD = "stored_password"
    }

    /**
     * Check if biometric authentication is enabled for the user
     */
    fun isBiometricEnabled(): Boolean {
        return prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false)
    }

    /**
     * Enable or disable biometric authentication
     */
    fun setBiometricEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
    }

    /**
     * Save user credentials for biometric login
     * Uses encrypted storage for password security
     * @param email User's email
     * @param password User's password (will be encrypted before storage)
     */
    fun saveCredentials(email: String, password: String) {
        prefs.edit()
            .putString(KEY_STORED_EMAIL, email)
            .apply()
        
        encryptedPrefs.edit()
            .putString(KEY_STORED_PASSWORD, password)
            .apply()
    }

    /**
     * Get stored email for biometric login
     */
    fun getStoredEmail(): String? {
        return prefs.getString(KEY_STORED_EMAIL, null)
    }

    /**
     * Get stored password (decrypted automatically by EncryptedSharedPreferences)
     */
    fun getStoredPassword(): String? {
        return encryptedPrefs.getString(KEY_STORED_PASSWORD, null)
    }

    /**
     * Check if credentials are stored
     */
    fun hasStoredCredentials(): Boolean {
        return !getStoredEmail().isNullOrEmpty() && !getStoredPassword().isNullOrEmpty()
    }

    /**
     * Clear all biometric data
     */
    fun clearBiometricData() {
        prefs.edit()
            .remove(KEY_BIOMETRIC_ENABLED)
            .remove(KEY_STORED_EMAIL)
            .apply()
            
        encryptedPrefs.edit()
            .remove(KEY_STORED_PASSWORD)
            .apply()
    }

    /**
     * Get stored credentials (email and password)
     * Returns a Pair of email and password, or null if not available
     */
    fun getStoredCredentials(): Pair<String, String>? {
        val email = getStoredEmail()
        val password = getStoredPassword()
        return if (!email.isNullOrEmpty() && !password.isNullOrEmpty()) {
            Pair(email, password)
        } else {
            null
        }
    }
}

