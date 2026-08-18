//CODE ATTRIBUTION
//01
//App Compat Activity
//Adapted from: Android Developers. (2025). AppCompatActivity. [online] Android Developers.
//Available at: https://developer.android.com/reference/androidx/appcompat/app/AppCompatActivity
//Date Accessed: 30 September 2025

//02
//Intent 
//Adapted from: Android Developers. (2025). Intent. [online] Android Developers.
//Available at: https://developer.android.com/reference/android/content/Intent
//Date Accessed: 30 September 2025

//03
//View visibility
//Adapted from: Android Developers. (2025). View. [online] Android Developers.
//Available at: https://developer.android.com/reference/android/view/View#Visibility
//Date Accessed: 30 September 2025

//04
//Toast
//Adapted from: Android Developers. (2025). Toast. [online] Android Developers.
//Available at: https://developer.android.com/reference/android/widget/Toast
//Date Accessed: 30 September 2025

//05
//Circular Progress Indicator
//Adapted from: Material Design. (2025). Progress indicators. [online] Material Design.
//Available at: https://m3.material.io/components/progress-indicators/overview
//Date Accessed: 30 September 2025

//06
//Material Text View
//Adapted from: Android Developers. (2025). MaterialTextView. [online] Android Developers.
//Available at: https://developer.android.com/reference/com/google/android/material/textview/MaterialTextView
//Date Accessed: 30 September 2025

package vcmsa.projects.careerconnect.ui.jobseeker

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.textview.MaterialTextView
import kotlinx.coroutines.launch
import vcmsa.projects.careerconnect.R
import vcmsa.projects.careerconnect.data.repository.JobRepository
import vcmsa.projects.careerconnect.data.repository.OfflineSavedJobRepository
import vcmsa.projects.careerconnect.data.repository.ProfileRepository
import vcmsa.projects.careerconnect.domain.model.Job
import vcmsa.projects.careerconnect.domain.model.SaveJobRequest
import vcmsa.projects.careerconnect.domain.model.SavedJobPriority
import vcmsa.projects.careerconnect.domain.model.UserType
import vcmsa.projects.careerconnect.utils.NetworkConnectivityManager
import java.text.NumberFormat
import java.util.*

/**
 * Activity for displaying job details and applying to jobs
 */
class JobDetailsActivity : AppCompatActivity() {

    private lateinit var tvJobTitle: MaterialTextView
    private lateinit var tvCompanyName: MaterialTextView
    private lateinit var tvLocation: MaterialTextView
    private lateinit var tvJobType: MaterialTextView
    private lateinit var tvExperienceLevel: MaterialTextView
    private lateinit var tvSalary: MaterialTextView
    private lateinit var tvDescription: MaterialTextView
    private lateinit var tvBenefits: MaterialTextView
    private lateinit var tvIndustry: MaterialTextView
    private lateinit var tvRequirements: MaterialTextView
    private lateinit var btnApply: MaterialButton
    private lateinit var btnSaveJob: MaterialButton
    private lateinit var progressBar: CircularProgressIndicator

    private val jobRepository = JobRepository()
    private lateinit var savedJobRepository: OfflineSavedJobRepository
    private lateinit var networkManager: NetworkConnectivityManager
    private val profileRepository = ProfileRepository()
    private var currentJob: Job? = null
    private var currentUserType: UserType = UserType.JOB_SEEKER
    private var isJobSaved: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_job_details)

        // Initialize offline repository
        savedJobRepository = OfflineSavedJobRepository(this)
        networkManager = NetworkConnectivityManager(this)

        initializeViews()
        setupClickListeners()
        loadUserRole()
        loadJobDetails()
    }

    private fun initializeViews() {
        tvJobTitle = findViewById(R.id.tvJobTitle)
        tvCompanyName = findViewById(R.id.tvCompanyName)
        tvLocation = findViewById(R.id.tvLocation)
        tvJobType = findViewById(R.id.tvJobType)
        tvExperienceLevel = findViewById(R.id.tvExperienceLevel)
        tvSalary = findViewById(R.id.tvSalary)
        tvDescription = findViewById(R.id.tvDescription)
        tvIndustry = findViewById(R.id.tvIndustry)
        tvRequirements = findViewById(R.id.tvRequirements)
        btnApply = findViewById(R.id.btnApply)
        btnSaveJob = findViewById(R.id.btnSaveJob)
        progressBar = findViewById(R.id.progressBar)

        // Back button
        findViewById<MaterialButton>(R.id.btnBack)?.setOnClickListener {
            finish()
        }
    }

    private fun setupClickListeners() {
        btnApply.setOnClickListener {
            applyToJob()
        }

        btnSaveJob.setOnClickListener {
            saveJob()
        }
    }

    private fun loadUserRole() {
        lifecycleScope.launch {
            val result = profileRepository.getProfile()
            result.onSuccess { profile ->
                currentUserType = profile.userType
                updateRoleUI()
            }.onFailure {
                currentUserType = UserType.JOB_SEEKER
                updateRoleUI()
            }
        }
    }

    private fun updateRoleUI() {
        // Recruiters should not be able to save jobs
        if (currentUserType == UserType.RECRUITER) {
            btnSaveJob.visibility = View.GONE
        } else {
            btnSaveJob.visibility = View.VISIBLE
        }
    }

    private fun loadJobDetails() {
        val jobId = intent.getStringExtra("job_id")
        if (jobId.isNullOrEmpty()) {
            showError(getString(R.string.job_id_not_provided))
            finish()
            return
        }

        setLoading(true)

        lifecycleScope.launch {
            try {
                val result = jobRepository.getJobDetails(jobId)
                result.fold(
                    onSuccess = { job ->
                        currentJob = job
                        displayJobDetails(job)
                        checkIfJobSaved(jobId)
                        setLoading(false)
                    },
                    onFailure = { error ->
                        setLoading(false)
                        showError(getString(R.string.failed_to_load_job_details))
                    }
                )
            } catch (e: Exception) {
                setLoading(false)
                showError(getString(R.string.error_occurred, e.message))
            }
        }
    }
    
    /**
     * Check if the job is already saved/bookmarked
     */
    private fun checkIfJobSaved(jobId: String) {
        lifecycleScope.launch {
            try {
                isJobSaved = savedJobRepository.isJobSaved(jobId)
                updateSaveButton()
            } catch (e: Exception) {
                // Ignore errors
            }
        }
    }
    
    /**
     * Update the save button text based on saved status
     */
    private fun updateSaveButton() {
        if (isJobSaved) {
            btnSaveJob.text = getString(R.string.bookmarked)
            btnSaveJob.isEnabled = false
        } else {
            btnSaveJob.text = getString(R.string.bookmark_job)
            btnSaveJob.isEnabled = true
        }
    }

    private fun displayJobDetails(job: Job) {
        tvJobTitle.text = job.title
        tvCompanyName.text = job.companyName
        tvLocation.text = job.location
        tvJobType.text = formatJobType(job.jobType)
        // Work arrangement removed from model/backend; leave empty
        findViewById<MaterialTextView>(R.id.tvWorkArrangement)?.text = ""
        tvExperienceLevel.text = formatExperienceLevel(job.experienceLevel)
        tvSalary.text = formatSalary(job.salaryMin, job.salaryMax, job.currency ?: "ZAR")
        tvDescription.text = job.description
        // Remove Skills card usage entirely (not in schema)

        // Industry, Requirements, Benefits
        val cardIndustry = findViewById<com.google.android.material.card.MaterialCardView>(R.id.cardIndustry)
        val cardRequirements = findViewById<com.google.android.material.card.MaterialCardView>(R.id.cardRequirements)
        val cardBenefits = findViewById<com.google.android.material.card.MaterialCardView>(R.id.cardBenefits)

        if (job.industry.isNullOrBlank()) {
            cardIndustry.visibility = View.GONE
        } else {
            tvIndustry.text = job.industry
            cardIndustry.visibility = View.VISIBLE
        }

        if (job.requirements.isNullOrBlank()) {
            cardRequirements.visibility = View.GONE
        } else {
            tvRequirements.text = job.requirements
            cardRequirements.visibility = View.VISIBLE
        }

        if (job.benefits.isNullOrBlank()) {
            cardBenefits.visibility = View.GONE
        } else {
            tvBenefits.text = job.benefits
            cardBenefits.visibility = View.VISIBLE
        }
        // Application deadline removed from schema; no longer displayed
    }

    private fun applyToJob() {
        val job = currentJob ?: return

        // Navigate to job application activity
        val intent = Intent(this, JobApplicationActivity::class.java)
        intent.putExtra("job_id", job.id)
        intent.putExtra("job_title", job.title)
        intent.putExtra("company_name", job.companyName)
        startActivity(intent)
    }

    /**
     * Save/bookmark job (works offline)
     */
    private fun saveJob() {
        val job = currentJob ?: return

        setLoading(true)

        lifecycleScope.launch {
            try {
                // Save using offline repository (works offline)
                val result = savedJobRepository.saveJob(job)
                result.fold(
                    onSuccess = {
                        setLoading(false)
                        isJobSaved = true
                        updateSaveButton()
                        
                        val message = if (networkManager.isConnected()) {
                            getString(R.string.job_saved_successfully)
                        } else {
                            getString(R.string.job_saved_offline)
                        }
                        Toast.makeText(this@JobDetailsActivity, message, Toast.LENGTH_SHORT).show()
                    },
                    onFailure = { error ->
                        setLoading(false)
                        showError(getString(R.string.failed_to_save_job))
                    }
                )
            } catch (e: Exception) {
                setLoading(false)
                showError(getString(R.string.error_occurred, e.message))
            }
        }
    }

    private fun formatJobType(jobType: vcmsa.projects.careerconnect.domain.model.JobType): String {
        return when (jobType) {
            vcmsa.projects.careerconnect.domain.model.JobType.FULL_TIME -> getString(R.string.job_type_full_time)
            vcmsa.projects.careerconnect.domain.model.JobType.PART_TIME -> getString(R.string.job_type_part_time)
            vcmsa.projects.careerconnect.domain.model.JobType.CONTRACT -> getString(R.string.job_type_contract)
            vcmsa.projects.careerconnect.domain.model.JobType.INTERNSHIP -> getString(R.string.job_type_internship)
        }
    }

    // Work arrangement no longer used

    private fun formatExperienceLevel(experienceLevel: vcmsa.projects.careerconnect.domain.model.ExperienceLevel): String {
        return when (experienceLevel) {
            vcmsa.projects.careerconnect.domain.model.ExperienceLevel.ENTRY -> getString(R.string.experience_entry)
            vcmsa.projects.careerconnect.domain.model.ExperienceLevel.MID -> getString(R.string.experience_mid)
            vcmsa.projects.careerconnect.domain.model.ExperienceLevel.SENIOR -> getString(R.string.experience_senior)
            vcmsa.projects.careerconnect.domain.model.ExperienceLevel.EXECUTIVE -> getString(R.string.experience_executive)
        }
    }

    private fun formatSalary(salaryMin: Double?, salaryMax: Double?, currency: String): String {
        return when {
            salaryMin != null && salaryMax != null -> {
                val formatter = NumberFormat.getCurrencyInstance(Locale.getDefault())
                formatter.currency = Currency.getInstance(currency)
                "${formatter.format(salaryMin)} - ${formatter.format(salaryMax)}"
            }
            salaryMin != null -> {
                val formatter = NumberFormat.getCurrencyInstance(Locale.getDefault())
                formatter.currency = Currency.getInstance(currency)
                "${formatter.format(salaryMin)}+"
            }
            salaryMax != null -> {
                val formatter = NumberFormat.getCurrencyInstance(Locale.getDefault())
                formatter.currency = Currency.getInstance(currency)
                "Up to ${formatter.format(salaryMax)}"
            }
            else -> getString(R.string.salary_not_specified)
        }
    }

    private fun setLoading(loading: Boolean) {
        progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        btnApply.isEnabled = !loading
        // Avoid enabling a hidden button for recruiters
        if (btnSaveJob.visibility == View.VISIBLE) {
            btnSaveJob.isEnabled = !loading
        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
