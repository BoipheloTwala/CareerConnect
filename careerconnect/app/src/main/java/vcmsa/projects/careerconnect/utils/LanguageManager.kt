//CODE ATTRIBUTION
//01
//SharedPreferences
//Adapted from: Android Developers. (2025). SharedPreferences. [online] Android Developers.
//Available at: https://developer.android.com/reference/android/content/SharedPreferences
//Date Accessed: 11 November 2025

//02
//Locale
//Adapted from: Android Developers. (2025). Locale. [online] Android Developers.
//Available at: https://developer.android.com/reference/java/util/Locale
//Date Accessed: 11 November 2025

//03
//Configuration
//Adapted from: Android Developers. (2025). Configuration. [online] Android Developers.
//Available at: https://developer.android.com/reference/android/content/res/Configuration
//Date Accessed: 11 November 2025

//04
//Resources
//Adapted from: Android Developers. (2025). Resources. [online] Android Developers.
//Available at: https://developer.android.com/reference/android/content/res/Resources
//Date Accessed: 11 November 2025

package vcmsa.projects.careerconnect.utils

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import java.util.*

/**
 * Language manager for handling app language switching
 */
class LanguageManager(private val context: Context) {

    companion object {
        private const val PREFS_NAME = "language_prefs"
        private const val KEY_LANGUAGE = "selected_language"
        const val ENGLISH = "en"
        const val AFRIKAANS = "af"
        const val ZULU = "zu"
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Get the currently selected language
     */
    fun getSelectedLanguage(): String {
        return prefs.getString(KEY_LANGUAGE, ENGLISH) ?: ENGLISH
    }

    /**
     * Set the selected language and update app configuration
     */
    fun setSelectedLanguage(languageCode: String): Boolean {
        try {
            // Save to preferences
            prefs.edit().putString(KEY_LANGUAGE, languageCode).apply()

            // Don't update locale here - it will be applied on app restart
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    /**
     * Update the app locale configuration
     */
    private fun updateAppLocale(languageCode: String) {
        val locale = when (languageCode) {
            AFRIKAANS -> Locale("af", "ZA") // Afrikaans - South Africa
            ZULU -> Locale("zu", "ZA") // Zulu - South Africa
            else -> Locale("en", "ZA") // English - South Africa (default)
        }

        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale)
            config.setLayoutDirection(locale)
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
        }

        // Update the configuration
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }

    /**
     * Apply the saved language when the app starts
     */
    fun applySavedLanguage() {
        val savedLanguage = getSelectedLanguage()
        updateAppLocale(savedLanguage) // Always apply the saved language
    }

    /**
     * Get display name for a language code
     */
    fun getLanguageDisplayName(languageCode: String): String {
        return when (languageCode) {
            AFRIKAANS -> context.getString(vcmsa.projects.careerconnect.R.string.afrikaans)
            ZULU -> context.getString(vcmsa.projects.careerconnect.R.string.zulu)
            else -> context.getString(vcmsa.projects.careerconnect.R.string.english)
        }
    }

    /**
     * Get all available languages
     */
    fun getAvailableLanguages(): List<LanguageItem> {
        return listOf(
            LanguageItem(ENGLISH, context.getString(vcmsa.projects.careerconnect.R.string.english)),
            LanguageItem(AFRIKAANS, context.getString(vcmsa.projects.careerconnect.R.string.afrikaans)),
            LanguageItem(ZULU, context.getString(vcmsa.projects.careerconnect.R.string.zulu))
        )
    }

    /**
     * Check if the app needs to be restarted for language change
     */
    fun requiresRestart(): Boolean {
        return true // Android requires restart for locale changes to take full effect
    }
}

/**
 * Data class for language items
 */
data class LanguageItem(
    val code: String,
    val displayName: String
)
