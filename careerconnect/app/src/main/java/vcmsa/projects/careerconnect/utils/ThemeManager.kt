//CODE ATTRIBUTION
//01
//SharedPreferences
//Adapted from: Android Developers. (2025). SharedPreferences. [online] Android Developers.
//Available at: https://developer.android.com/reference/android/content/SharedPreferences
//Date Accessed: 11 November 2025

//02
//AppCompatDelegate
//Adapted from: Android Developers. (2025). AppCompatDelegate. [online] Android Developers.
//Available at: https://developer.android.com/reference/androidx/appcompat/app/AppCompatDelegate
//Date Accessed: 15 November 2025

package vcmsa.projects.careerconnect.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate

/**
 * Manager class for handling app theme (dark/light mode) switching
 */
class ThemeManager(private val context: Context) {

    companion object {
        private const val PREFS_NAME = "theme_prefs"
        private const val KEY_THEME_MODE = "theme_mode"
        const val THEME_LIGHT = "light"
        const val THEME_DARK = "dark"
        const val THEME_SYSTEM = "system"
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Get the currently selected theme mode
     */
    fun getThemeMode(): String {
        return prefs.getString(KEY_THEME_MODE, THEME_SYSTEM) ?: THEME_SYSTEM
    }

    /**
     * Set the theme mode and apply it immediately
     */
    fun setThemeMode(mode: String): Boolean {
        return try {
            // Save to preferences
            prefs.edit().putString(KEY_THEME_MODE, mode).apply()

            // Apply theme immediately
            applyThemeMode(mode)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Apply the saved theme mode
     */
    fun applySavedTheme() {
        val savedMode = getThemeMode()
        applyThemeMode(savedMode)
    }

    /**
     * Apply theme mode to AppCompatDelegate
     */
    private fun applyThemeMode(mode: String) {
        when (mode) {
            THEME_LIGHT -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            THEME_DARK -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
            THEME_SYSTEM -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
        }
    }

    /**
     * Get display name for a theme mode
     */
    fun getThemeDisplayName(mode: String): String {
        return when (mode) {
            THEME_LIGHT -> context.getString(vcmsa.projects.careerconnect.R.string.theme_light)
            THEME_DARK -> context.getString(vcmsa.projects.careerconnect.R.string.theme_dark)
            THEME_SYSTEM -> context.getString(vcmsa.projects.careerconnect.R.string.theme_system)
            else -> context.getString(vcmsa.projects.careerconnect.R.string.theme_system)
        }
    }

    /**
     * Get all available theme modes
     */
    fun getAvailableThemes(): List<ThemeItem> {
        return listOf(
            ThemeItem(THEME_LIGHT, context.getString(vcmsa.projects.careerconnect.R.string.theme_light)),
            ThemeItem(THEME_DARK, context.getString(vcmsa.projects.careerconnect.R.string.theme_dark)),
            ThemeItem(THEME_SYSTEM, context.getString(vcmsa.projects.careerconnect.R.string.theme_system))
        )
    }
}

/**
 * Data class for theme items
 */
data class ThemeItem(
    val mode: String,
    val displayName: String
)

