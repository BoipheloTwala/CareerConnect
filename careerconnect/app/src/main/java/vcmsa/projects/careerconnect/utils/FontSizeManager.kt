//CODE ATTRIBUTION
//01
//SharedPreferences
//Adapted from: Android Developers. (2025). SharedPreferences. [online] Android Developers.
//Available at: https://developer.android.com/reference/android/content/SharedPreferences
//Date Accessed: 11 November 2025

//02
//Configuration
//Adapted from: Android Developers. (2025). Configuration. [online] Android Developers.
//Available at: https://developer.android.com/reference/android/content/res/Configuration
//Date Accessed: 15 November 2025

//03
//Resources
//Adapted from: Android Developers. (2025). Resources. [online] Android Developers.
//Available at: https://developer.android.com/reference/android/content/res/Resources
//Date Accessed: 15 November 2025

package vcmsa.projects.careerconnect.utils

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build

/**
 * Manager class for handling app font size preferences
 */
class FontSizeManager(private val context: Context) {

    companion object {
        private const val PREFS_NAME = "font_size_prefs"
        private const val KEY_FONT_SIZE = "font_size"
        const val FONT_SIZE_SMALL = "small"
        const val FONT_SIZE_MEDIUM = "medium"
        const val FONT_SIZE_LARGE = "large"
        
        // Font scale values
        private const val FONT_SCALE_SMALL = 0.85f
        private const val FONT_SCALE_MEDIUM = 1.0f
        private const val FONT_SCALE_LARGE = 1.15f
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Get the currently selected font size
     */
    fun getFontSize(): String {
        return prefs.getString(KEY_FONT_SIZE, FONT_SIZE_MEDIUM) ?: FONT_SIZE_MEDIUM
    }

    /**
     * Set the font size and apply it immediately
     */
    fun setFontSize(size: String): Boolean {
        return try {
            // Save to preferences
            prefs.edit().putString(KEY_FONT_SIZE, size).apply()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Get the font scale value for a given font size
     */
    fun getFontScale(size: String): Float {
        return when (size) {
            FONT_SIZE_SMALL -> FONT_SCALE_SMALL
            FONT_SIZE_LARGE -> FONT_SCALE_LARGE
            else -> FONT_SCALE_MEDIUM
        }
    }

    /**
     * Get the current font scale
     */
    fun getCurrentFontScale(): Float {
        val size = getFontSize()
        return getFontScale(size)
    }

    /**
     * Apply the saved font size to the app configuration
     */
    fun applySavedFontSize(resources: Resources) {
        val savedSize = getFontSize()
        val fontScale = getFontScale(savedSize)
        applyFontScale(resources, fontScale)
    }

    /**
     * Apply font scale to resources configuration
     * Uses modern API for Android 8.0+ and fallback for older versions
     */
    private fun applyFontScale(resources: Resources, scale: Float) {
        val configuration = Configuration(resources.configuration)
        configuration.fontScale = scale
        
        // Update the configuration - works on all Android versions
        // For Android 8.0+, this method signature is still valid and works correctly
        // For older versions, we suppress the deprecation warning
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Modern approach - works reliably on Android 8.0+
            resources.updateConfiguration(configuration, resources.displayMetrics)
        } else {
            // Older versions - use deprecated method
            @Suppress("DEPRECATION")
            resources.updateConfiguration(configuration, resources.displayMetrics)
        }
    }

    /**
     * Get display name for a font size
     */
    fun getFontSizeDisplayName(size: String): String {
        return when (size) {
            FONT_SIZE_SMALL -> context.getString(vcmsa.projects.careerconnect.R.string.font_size_small)
            FONT_SIZE_LARGE -> context.getString(vcmsa.projects.careerconnect.R.string.font_size_large)
            else -> context.getString(vcmsa.projects.careerconnect.R.string.font_size_medium)
        }
    }

    /**
     * Get all available font sizes
     */
    fun getAvailableFontSizes(): List<FontSizeItem> {
        return listOf(
            FontSizeItem(FONT_SIZE_SMALL, context.getString(vcmsa.projects.careerconnect.R.string.font_size_small)),
            FontSizeItem(FONT_SIZE_MEDIUM, context.getString(vcmsa.projects.careerconnect.R.string.font_size_medium)),
            FontSizeItem(FONT_SIZE_LARGE, context.getString(vcmsa.projects.careerconnect.R.string.font_size_large))
        )
    }
}

/**
 * Data class for font size items
 */
data class FontSizeItem(
    val size: String,
    val displayName: String
)

