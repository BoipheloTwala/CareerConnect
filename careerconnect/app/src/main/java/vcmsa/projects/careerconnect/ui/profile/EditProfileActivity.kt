//CODE ATTRIBUTION
//01
//App Compat Activity
//Adapted from: Android Developers. (2025). AppCompatActivity. [online] Android Developers.
//Available at: https://developer.android.com/reference/androidx/appcompat/app/AppCompatActivity
//Date Accessed: 17 September 2025

//02
//Bundle
//Adapted from: Android Developers. (2025). Bundle. [online] Android Developers.
//Available at: https://developer.android.com/reference/android/os/Bundle
//Date Accessed: 17 September 2025

//03
//Fragment Transactions
//Adapted from: Android Developers. (2025). FragmentTransaction. [online] Android Developers.
//Available at: https://developer.android.com/reference/androidx/fragment/app/FragmentTransaction
//Date Accessed: 17 September 2025

//04
//Fragment Manager
//Adapted from: Android Developers. (2025). FragmentManager. [online] Android Developers.
//Available at: https://developer.android.com/reference/androidx/fragment/app/FragmentManager
//Date Accessed: 17 September 2025

//05
//Material Button
//Adapted from: Android Developers. (2025). MaterialButton. [online] Android Developers.
//Available at: https://developer.android.com/reference/com/google/android/material/button/MaterialButton
//Date Accessed: 17 September 2025

//06
//SetOnClickListener
//Adapted from: Android Developers. (2025). View.OnClickListener. [online] Android Developers.
//Available at: https://developer.android.com/reference/android/view/View#setOnClickListener(android.view.View.OnClickListener)
//Date Accessed: 17 September 2025

package vcmsa.projects.careerconnect.ui.profile

import android.os.Bundle
import com.google.android.material.button.MaterialButton
import androidx.appcompat.app.AppCompatActivity
import vcmsa.projects.careerconnect.R
import vcmsa.projects.careerconnect.utils.LanguageManager
import vcmsa.projects.careerconnect.utils.ThemeManager
import vcmsa.projects.careerconnect.utils.FontSizeManager

class EditProfileActivity : AppCompatActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		// Apply saved language, theme, and font size before setting content view
		val languageManager = LanguageManager(this)
		languageManager.applySavedLanguage()
		
		val themeManager = ThemeManager(this)
		themeManager.applySavedTheme()
		
		val fontSizeManager = FontSizeManager(this)
		fontSizeManager.applySavedFontSize(resources)

		setContentView(R.layout.activity_edit_profile)

		if (savedInstanceState == null) {
			supportFragmentManager.beginTransaction()
				.replace(R.id.fragmentContainer, ProfileFragment())
				.commitAllowingStateLoss()
		}

		findViewById<MaterialButton>(R.id.btnBack).setOnClickListener {
			finish()
		}
	}
}


