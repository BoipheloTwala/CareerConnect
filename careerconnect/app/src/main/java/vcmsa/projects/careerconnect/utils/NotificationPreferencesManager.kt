//CODE ATTRIBUTION
//01
//SharedPreferences
//Adapted from: Android Developers. (2025). SharedPreferences. [online] Android Developers.
//Available at: https://developer.android.com/reference/android/content/SharedPreferences
//Date Accessed: 11 November 2025

package vcmsa.projects.careerconnect.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * Manager class for handling notification preferences (sound, vibration, etc.)
 */
class NotificationPreferencesManager(private val context: Context) {

    companion object {
        private const val PREFS_NAME = "notification_prefs"
        private const val KEY_NOTIFICATION_SOUND = "notification_sound_enabled"
        private const val KEY_NOTIFICATION_VIBRATION = "notification_vibration_enabled"
        
        // Default values
        private const val DEFAULT_SOUND_ENABLED = true
        private const val DEFAULT_VIBRATION_ENABLED = true
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Check if notification sound is enabled
     */
    fun isNotificationSoundEnabled(): Boolean {
        return prefs.getBoolean(KEY_NOTIFICATION_SOUND, DEFAULT_SOUND_ENABLED)
    }

    /**
     * Set notification sound enabled/disabled
     */
    fun setNotificationSoundEnabled(enabled: Boolean): Boolean {
        return try {
            prefs.edit().putBoolean(KEY_NOTIFICATION_SOUND, enabled).apply()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Check if notification vibration is enabled
     */
    fun isNotificationVibrationEnabled(): Boolean {
        return prefs.getBoolean(KEY_NOTIFICATION_VIBRATION, DEFAULT_VIBRATION_ENABLED)
    }

    /**
     * Set notification vibration enabled/disabled
     */
    fun setNotificationVibrationEnabled(enabled: Boolean): Boolean {
        return try {
            prefs.edit().putBoolean(KEY_NOTIFICATION_VIBRATION, enabled).apply()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}

