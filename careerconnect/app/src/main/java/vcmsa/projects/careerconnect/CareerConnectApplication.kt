//CODE ATTRIBUTION
//01
//Application
//Adapted from: Android Developers. (2025). Application. [online] Android Developers.
//Available at: https://developer.android.com/reference/android/app/Application
//Date Accessed: 11 November 2025

//02
//Context
//Adapted from: Android Developers. (2025). Context. [online] Android Developers.
//Available at: https://developer.android.com/reference/android/content/Context
//Date Accessed: 11 November 2025

package vcmsa.projects.careerconnect

import android.app.Application
import android.content.Context
import vcmsa.projects.careerconnect.utils.LanguageManager
import vcmsa.projects.careerconnect.utils.ThemeManager
import vcmsa.projects.careerconnect.utils.FontSizeManager

/**
 * Custom Application class to handle app-level initialization
 * including locale setup for multi-language support
 */
class CareerConnectApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Apply the saved language, theme, and font size at application startup
        // This ensures the locale, theme, and font size are set before any activities are created
        val languageManager = LanguageManager(this)
        languageManager.applySavedLanguage()
        
        val themeManager = ThemeManager(this)
        themeManager.applySavedTheme()
        
        val fontSizeManager = FontSizeManager(this)
        fontSizeManager.applySavedFontSize(resources)
    }

    override fun attachBaseContext(base: Context) {
        // Apply saved language before attaching base context
        val languageManager = LanguageManager(base)
        languageManager.applySavedLanguage()

        super.attachBaseContext(base)
    }
}
