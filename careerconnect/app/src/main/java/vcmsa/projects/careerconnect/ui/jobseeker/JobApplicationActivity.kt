//CODE ATTRIBUTION
//01
//Uri
//Adapted from: Android Developers. (2025). Uri. [online] Android Developers.
//Available at: https://developer.android.com/reference/android/net/Uri
//Date Accessed: 30 September 2025

//02
//register For Activity Result
//Adapted from: Android Developers. (2025). Get results from an activity. [online] Android Developers.
//Available at: https://developer.android.com/training/basics/intents/result
//Date Accessed: 30 September 2025

//03
//Toast
//Adapted from: Android Developers. (2025). Toast. [online] Android Developers.
//Available at: https://developer.android.com/reference/android/widget/Toast
//Date Accessed: 30 September 2025

//04
//Material Button
//Adapted from: Android Developers. (2025). MaterialButton. [online] Android Developers.
//Available at: https://developer.android.com/reference/com/google/android/material/button/MaterialButton
//Date Accessed: 30 September 2025

//05
//Circular Progress Indicator
//Adapted from: Material Design. (2025). Progress indicators. [online] Material Design.
//Available at: https://m3.material.io/components/progress-indicators/overview
//Date Accessed: 30 September 2025

//06
//Text Input Layout
//Adapted from: Android Developers. (2025). TextInputLayout. [online] Android Developers.
//Available at: https://developer.android.com/reference/com/google/android/material/textfield/TextInputLayout
//Date Accessed: 30 September 2025

//07
//Text Input Edit Text
//Adapted from: Android Developers. (2025). TextInputEditText. [online] Android Developers.
//Available at: https://developer.android.com/reference/com/google/android/material/textfield/TextInputEditText
//Date Accessed: 30 September 2025

package vcmsa.projects.careerconnect.ui.jobseeker

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import vcmsa.projects.careerconnect.R
import vcmsa.projects.careerconnect.data.repository.JobRepository
import vcmsa.projects.careerconnect.data.repository.CVRepository
import vcmsa.projects.careerconnect.domain.model.JobApplicationRequest
import vcmsa.projects.careerconnect.domain.model.CVUploadRequest
import vcmsa.projects.careerconnect.domain.model.CVFileType

/**
 * Activity for job seekers to apply to jobs with CV upload
 */
class JobApplicationActivity : AppCompatActivity() {

    private lateinit var tilCoverLetter: TextInputLayout
    private lateinit var etCoverLetter: TextInputEditText
    private lateinit var btnSelectCV: MaterialButton
    private lateinit var btnApply: MaterialButton
    private lateinit var progressBar: CircularProgressIndicator

    private var selectedCVUri: Uri? = null
    private var jobId: String? = null
    private val jobRepository = JobRepository()
    private val cvRepository = CVRepository()

    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedCVUri = uri
                btnSelectCV.text = getString(R.string.cv_selected)
                btnSelectCV.isEnabled = true
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_job_application)

        jobId = intent.getStringExtra("job_id")
        if (jobId.isNullOrEmpty()) {
            showError(getString(R.string.job_id_not_provided))
            finish()
            return
        }

        initializeViews()
        setupClickListeners()
    }

    private fun initializeViews() {
        tilCoverLetter = findViewById(R.id.tilCoverLetter)
        etCoverLetter = findViewById(R.id.etCoverLetter)
        btnSelectCV = findViewById(R.id.btnSelectCV)
        btnApply = findViewById(R.id.btnApply)
        progressBar = findViewById(R.id.progressBar)

        // Back button
        findViewById<MaterialButton>(R.id.btnBack)?.setOnClickListener {
            finish()
        }
    }

    private fun setupClickListeners() {
        btnSelectCV.setOnClickListener {
            selectCVFile()
        }

        btnApply.setOnClickListener {
            applyToJob()
        }
    }

    private fun selectCVFile() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "application/pdf"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        filePickerLauncher.launch(intent)
    }

    private fun applyToJob() {
        if (selectedCVUri == null) {
            showError(getString(R.string.select_cv_error))
            return
        }

        setLoading(true)

        lifecycleScope.launch {
            try {
                // First upload the CV
                // Upload the file to Cloudinary first to obtain a valid URL and size
                val localFileName = "application_cv_${System.currentTimeMillis()}.pdf"
                val uploadResult = vcmsa.projects.careerconnect.data.network.CVUploader.uploadCVFile(
                    context = this@JobApplicationActivity,
                    uri = selectedCVUri!!,
                    fileName = localFileName,
                    fileType = CVFileType.PDF
                )
                uploadResult.fold(
                    onSuccess = { uploaded ->
                        val cvUploadRequest = vcmsa.projects.careerconnect.domain.model.CVCreateRequest(
                            fileName = localFileName,
                            fileUrl = uploaded.fileUrl,
                            fileSize = uploaded.fileSize,
                            isPrimary = false
                        )
                        lifecycleScope.launch {
                            val cvUploadResult = cvRepository.uploadCV(cvUploadRequest)
                            cvUploadResult.fold(
                                onSuccess = { cvCreated ->
                                    // Then apply to the job with the CV URL
                                    val applicationRequest = JobApplicationRequest(
                                        jobId = jobId!!,
                                        cvId = cvCreated.id,
                                        coverLetter = etCoverLetter.text?.toString()?.trim(),
                                        resumeUrl = cvCreated.fileUrl
                                    )
                                    val applyResult = jobRepository.applyForJob(jobId!!, applicationRequest)
                                    applyResult.fold(
                                        onSuccess = {
                                            setLoading(false)
                                            Toast.makeText(this@JobApplicationActivity, getString(R.string.application_submitted_successfully), Toast.LENGTH_SHORT).show()
                                            finish()
                                        },
                                        onFailure = { error ->
                                            setLoading(false)
                                            showError(getString(R.string.failed_to_submit_application))
                                        }
                                    )
                                },
                                onFailure = { error ->
                                    setLoading(false)
                                    showError(getString(R.string.failed_to_upload_cv_metadata))
                                }
                            )
                        }
                    },
                    onFailure = { error ->
                        setLoading(false)
                        showError(getString(R.string.failed_to_upload_cv_file))
                    }
                )
                // handled above in nested fold
            } catch (e: Exception) {
                setLoading(false)
                showError(getString(R.string.error_occurred, e.message))
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        btnSelectCV.isEnabled = !loading
        btnApply.isEnabled = !loading
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
