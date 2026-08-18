//CODE ATTRIBUTION
//01
//Fragment
//Adapted from: Android Developers. (2025). Fragment. [online] Android Developers.
//Available at: https://developer.android.com/reference/androidx/fragment/app/Fragment
//Date Accessed: 17 September 2025

//02
//onCreateView
//Adapted from: Android Developers. (2025). Fragment lifecycle: Create the fragment UI. [online] Android Developers.
//Available at: https://developer.android.com/guide/fragments/lifecycle#creating
//Date Accessed: 17 September 2025

//03
//Layout Inflater
//Adapted from: Android Developers. (2025). LayoutInflater. [online] Android Developers.
//Available at: https://developer.android.com/reference/android/view/LayoutInflater
//Date Accessed: 17 September 2025

//04
//Intent
//Adapted from: Android Developers. (2025). Intent. [online] Android Developers.
//Available at: https://developer.android.com/reference/android/content/Intent
//Date Accessed: 17 September 2025

//05
//Uri
//Adapted from: Android Developers. (2025). Uri. [online] Android Developers.
//Available at: https://developer.android.com/reference/android/net/Uri
//Date Accessed: 17 September 2025

//06
//Image View
//Adapted from: Android Developers. (2025). ImageView. [online] Android Developers.
//Available at: https://developer.android.com/reference/android/widget/ImageView
//Date Accessed: 17 September 2025

//07
//Toast
//Adapted from: Android Developers. (2025). Toast. [online] Android Developers.
//Available at: https://developer.android.com/reference/android/widget/Toast
//Date Accessed: 17 September 2025

//08
//Jetpack Compose Button
//Adapted from: Android Developers. (2025). Button (Jetpack Compose). [online] Android Developers.
//Available at: https://developer.android.com/develop/ui/compose/components/button
//Date Accessed: 17 September 2025

//09
//Jetpack Compose Progress indicators
//Adapted from: Android Developers. (2025). Progress indicators (Jetpack Compose). [online] Android Developers.
//Available at: https://developer.android.com/develop/ui/compose/components/progress
//Date Accessed: 17 September 2025

//10
//Text Input Layout
//Adapted from: Android Developers. (2025). TextInputLayout. [online] Android Developers.
//Available at: https://developer.android.com/reference/com/google/android/material/textfield/TextInputLayout
//Date Accessed: 17 September 2025

//11
//Text Input Edi Text
//Adapted from: Android Developers. (2025). TextInputEditText. [online] Android Developers.
//Available at: https://developer.android.com/reference/com/google/android/material/textfield/TextInputEditText
//Date Accessed: 17 September 2025

package vcmsa.projects.careerconnect.ui.profile

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import vcmsa.projects.careerconnect.R
import vcmsa.projects.careerconnect.data.network.CloudinaryUploader
import vcmsa.projects.careerconnect.data.repository.ProfileRepository
import vcmsa.projects.careerconnect.domain.model.UpdateProfileRequest
import vcmsa.projects.careerconnect.domain.model.UserProfile
import vcmsa.projects.careerconnect.domain.model.UserType

class ProfileFragment : Fragment() {

    private lateinit var ivAvatar: ImageView
    private lateinit var progressImage: CircularProgressIndicator
    private lateinit var btnChangeImage: MaterialButton
    private lateinit var btnSave: MaterialButton
    private lateinit var btnCancel: MaterialButton

    private lateinit var tilFirstName: TextInputLayout
    private lateinit var etFirstName: TextInputEditText
    private lateinit var tilLastName: TextInputLayout
    private lateinit var etLastName: TextInputEditText
    private lateinit var tilEmail: TextInputLayout
    private lateinit var etEmail: TextInputEditText
    private lateinit var tilPhone: TextInputLayout
    private lateinit var etPhone: TextInputEditText
    private lateinit var tilLocation: TextInputLayout
    private lateinit var etLocation: TextInputEditText
    private lateinit var tilBio: TextInputLayout
    private lateinit var etBio: TextInputEditText
    private lateinit var tilCompany: TextInputLayout
    private lateinit var etCompany: TextInputEditText

    private val repository = ProfileRepository()
    private var currentProfile: UserProfile? = null
    private var pendingImageUri: Uri? = null

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            pendingImageUri = it
            ivAvatar.setImageURI(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindViews(view)
        loadProfile()
        setupActions()
    }

    private fun bindViews(view: View) {
        ivAvatar = view.findViewById(R.id.ivAvatar)
        progressImage = view.findViewById(R.id.progressImage)
        btnChangeImage = view.findViewById(R.id.btnChangeImage)
        btnSave = view.findViewById(R.id.btnSave)
        btnCancel = view.findViewById(R.id.btnCancel)

        tilFirstName = view.findViewById(R.id.tilFirstName)
        etFirstName = view.findViewById(R.id.etFirstName)
        tilLastName = view.findViewById(R.id.tilLastName)
        etLastName = view.findViewById(R.id.etLastName)
        tilEmail = view.findViewById(R.id.tilEmail)
        etEmail = view.findViewById(R.id.etEmail)
        tilPhone = view.findViewById(R.id.tilPhone)
        etPhone = view.findViewById(R.id.etPhone)
        tilLocation = view.findViewById(R.id.tilLocation)
        etLocation = view.findViewById(R.id.etLocation)
        tilBio = view.findViewById(R.id.tilBio)
        etBio = view.findViewById(R.id.etBio)
        tilCompany = view.findViewById(R.id.tilCompany)
        etCompany = view.findViewById(R.id.etCompany)
    }

    private fun loadProfile() {
        lifecycleScope.launch {
            val result = repository.getProfile()
            result.onSuccess { profile ->
                currentProfile = profile
                fillForm(profile)
            }.onFailure { e ->
                Toast.makeText(requireContext(), e.message ?: getString(R.string.failed_to_load_profile), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun fillForm(profile: UserProfile) {
        etFirstName.setText(profile.firstName)
        etLastName.setText(profile.lastName)
        etEmail.setText(profile.email)
        etPhone.setText(profile.phone ?: "")
        etLocation.setText(profile.location ?: "")
        etBio.setText(profile.bio ?: "")
        etCompany.setText(profile.companyName ?: "")

        tilCompany.isVisible = profile.userType == UserType.RECRUITER
        // Image loading could use Glide/Picasso; keeping simple
        // ivAvatar.setImageURI if file; for http, a loader lib is preferred
    }

    private fun setupActions() {
        btnChangeImage.setOnClickListener {
            pickImage.launch("image/*")
        }
        btnCancel.setOnClickListener {
            requireActivity().finish()
        }
        btnSave.setOnClickListener {
            if (!validate()) return@setOnClickListener
            saveProfile()
        }
    }

    private fun validate(): Boolean {
        var ok = true
        val first = etFirstName.text?.toString()?.trim().orEmpty()
        val last = etLastName.text?.toString()?.trim().orEmpty()
        val email = etEmail.text?.toString()?.trim().orEmpty()

        if (first.isEmpty()) { tilFirstName.error = getString(R.string.first_name_required); ok = false } else tilFirstName.error = null
        if (last.isEmpty()) { tilLastName.error = getString(R.string.last_name_required); ok = false } else tilLastName.error = null
        if (email.isEmpty()) { tilEmail.error = getString(R.string.email_required); ok = false } else tilEmail.error = null
        return ok
    }

    private fun saveProfile() {
        setSaving(true)
        lifecycleScope.launch {
            // 1) Upload image if selected
            var imageUrl: String? = currentProfile?.profileImageUrl
            val selected = pendingImageUri
            if (selected != null) {
                progressImage.isVisible = true
                val upload = CloudinaryUploader.uploadImage(requireContext(), selected)
                upload.onSuccess { url: String -> imageUrl = url }
                    .onFailure { e: Throwable -> Toast.makeText(requireContext(), e.message ?: getString(R.string.image_upload_failed), Toast.LENGTH_LONG).show() }
                progressImage.isVisible = false
            }

            // 2) Build request
            val request = UpdateProfileRequest(
                firstName = etFirstName.text?.toString()?.trim(),
                lastName = etLastName.text?.toString()?.trim(),
                phone = etPhone.text?.toString()?.trim().takeIf { !it.isNullOrEmpty() },
                location = etLocation.text?.toString()?.trim().takeIf { !it.isNullOrEmpty() },
                companyName = etCompany.text?.toString()?.trim().takeIf { tilCompany.isVisible && !it.isNullOrEmpty() },
                bio = etBio.text?.toString()?.trim().takeIf { !it.isNullOrEmpty() },
                profileImageUrl = imageUrl
            )

            // 3) Call API
            val result = repository.updateProfile(request)
            result.onSuccess { updated ->
                currentProfile = updated
                Toast.makeText(requireContext(), getString(R.string.profile_saved), Toast.LENGTH_SHORT).show()
                requireActivity().finish()
            }.onFailure { e ->
                Toast.makeText(requireContext(), e.message ?: getString(R.string.failed_to_save_profile), Toast.LENGTH_LONG).show()
            }
            setSaving(false)
        }
    }

    private fun setSaving(saving: Boolean) {
        btnSave.isEnabled = !saving
        btnCancel.isEnabled = !saving
        btnChangeImage.isEnabled = !saving
    }
}

