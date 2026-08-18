//CODE ATTRIBUTION
//01
//App Compat Activity
//Adapted from: Android Developers. (2025). AppCompatActivity. [online] Android Developers.
//Available at: https://developer.android.com/reference/androidx/appcompat/app/AppCompatActivity
//Date Accessed: 15 September 2025

//02
//Toast
//Adapted from: Android Developers. (2025). Toast. [online] Android Developers.
//Available at: https://developer.android.com/reference/android/widget/Toast
//Date Accessed: 15 September 2025

//03
//Jetpack Compose Button
//Adapted from: Android Developers. (2025). Button (Jetpack Compose). [online] Android Developers.
//Available at: https://developer.android.com/develop/ui/compose/components/button
//Date Accessed: 15 September 2025

//04
//Jetpack Compose Progress indicators
//Adapted from: Android Developers. (2025). Progress indicators (Jetpack Compose). [online] Android Developers.
//Available at: https://developer.android.com/develop/ui/compose/components/progress
//Date Accessed: 15 September 2025

//05
//Text Input Layout
//Adapted from: Android Developers. (2025). TextInputLayout. [online] Android Developers.
//Available at: https://developer.android.com/reference/com/google/android/material/textfield/TextInputLayout
//Date Accessed: 15 September 2025

//06
//Text Input Edit Text
//Adapted from: Android Developers. (2025). TextInputEditText. [online] Android Developers.
//Available at: https://developer.android.com/reference/com/google/android/material/textfield/TextInputEditText
//Date Accessed: 15 September 2025

//07
//Array Adapter
//Adapted from: Android Developers. (2025). ArrayAdapter. [online] Android Developers.
//Available at: https://developer.android.com/reference/android/widget/ArrayAdapter
//Date Accessed: 16 September 2025

//08
//Material Auto Complete TextView
//Adapted from: Android Developers. (2025). MaterialAutoCompleteTextView. [online] Android Developers.
//Available at: https://developer.android.com/reference/com/google/android/material/textfield/MaterialAutoCompleteTextView
//Date Accessed: 16 September 2025

package vcmsa.projects.careerconnect.ui.recruiter

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import kotlinx.coroutines.launch
import vcmsa.projects.careerconnect.R
import vcmsa.projects.careerconnect.data.repository.JobRepository
import vcmsa.projects.careerconnect.domain.model.*
import vcmsa.projects.careerconnect.ui.jobseeker.AllJobsActivity

/**
 * Activity for recruiters to create and manage job postings
 */
class JobPostingActivity : AppCompatActivity() {

    private lateinit var tilJobTitle: TextInputLayout
    private lateinit var etJobTitle: TextInputEditText
    private lateinit var tilJobDescription: TextInputLayout
    private lateinit var etJobDescription: TextInputEditText
    private lateinit var tilCompanyName: TextInputLayout
    private lateinit var etCompanyName: TextInputEditText
    private lateinit var tilLocation: TextInputLayout
    private lateinit var etLocation: TextInputEditText
    private lateinit var tilSalaryMin: TextInputLayout
    private lateinit var etSalaryMin: TextInputEditText
    private lateinit var tilSalaryMax: TextInputLayout
    private lateinit var etSalaryMax: TextInputEditText
    
    private lateinit var tilIndustry: TextInputLayout
    private lateinit var etIndustry: TextInputEditText
    private lateinit var tilRequirements: TextInputLayout
    private lateinit var etRequirements: TextInputEditText

    private lateinit var actvJobType: MaterialAutoCompleteTextView
    private lateinit var actvWorkArrangement: MaterialAutoCompleteTextView
    private lateinit var actvExperienceLevel: MaterialAutoCompleteTextView

    private lateinit var btnCreateJob: MaterialButton
    private lateinit var btnSaveDraft: MaterialButton
    private lateinit var progressBar: CircularProgressIndicator

    private val jobRepository = JobRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_job_posting)

        initializeViews()
        setupDropdowns()
        setupClickListeners()
    }

    private fun initializeViews() {
        tilJobTitle = findViewById(R.id.tilJobTitle)
        etJobTitle = findViewById(R.id.etJobTitle)
        tilJobDescription = findViewById(R.id.tilJobDescription)
        etJobDescription = findViewById(R.id.etJobDescription)
        tilCompanyName = findViewById(R.id.tilCompanyName)
        etCompanyName = findViewById(R.id.etCompanyName)
        tilLocation = findViewById(R.id.tilLocation)
        etLocation = findViewById(R.id.etLocation)
        tilSalaryMin = findViewById(R.id.tilSalaryMin)
        etSalaryMin = findViewById(R.id.etSalaryMin)
        tilSalaryMax = findViewById(R.id.tilSalaryMax)
        etSalaryMax = findViewById(R.id.etSalaryMax)
        
        tilIndustry = findViewById(R.id.tilIndustry)
        etIndustry = findViewById(R.id.etIndustry)
        tilRequirements = findViewById(R.id.tilRequirements)
        etRequirements = findViewById(R.id.etRequirements)

        actvJobType = findViewById(R.id.actvJobType)
        actvWorkArrangement = findViewById(R.id.actvWorkArrangement)
        actvExperienceLevel = findViewById(R.id.actvExperienceLevel)

        btnCreateJob = findViewById(R.id.btnCreateJob)
        btnSaveDraft = findViewById(R.id.btnSaveDraft)
        progressBar = findViewById(R.id.progressBar)

        // Back button
        findViewById<MaterialButton>(R.id.btnBack)?.setOnClickListener {
            finish()
        }
    }

    private fun setupDropdowns() {
        // Job Type dropdown
        val jobTypes = listOf(
            "Full-time",
            "Part-time",
            "Contract",
            "Internship"
        )
        val jobTypeAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, jobTypes)
        actvJobType.setAdapter(jobTypeAdapter)

        // Work Arrangement dropdown
        val workArrangements = listOf(
            "Remote",
            "On site",
            "Hybrid"
        )
        val workArrangementAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, workArrangements)
        actvWorkArrangement.setAdapter(workArrangementAdapter)

        // Experience Level dropdown
        val experienceLevels = listOf(
            "Entry",
            "Mid",
            "Senior",
            "Executive"
        )
        val experienceLevelAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, experienceLevels)
        actvExperienceLevel.setAdapter(experienceLevelAdapter)
    }

    private fun setupClickListeners() {
        btnCreateJob.setOnClickListener {
            if (validateInput()) {
                createJob(false)
            }
        }

        btnSaveDraft.setOnClickListener {
            if (validateInput()) {
                createJob(true)
            }
        }
    }

    private fun validateInput(): Boolean {
        var isValid = true

        // Validate required fields
        if (etJobTitle.text?.toString()?.trim().isNullOrEmpty()) {
            tilJobTitle.error = "Job title is required"
            isValid = false
        } else {
            tilJobTitle.error = null
        }

        if (etJobDescription.text?.toString()?.trim().isNullOrEmpty()) {
            tilJobDescription.error = "Job description is required"
            isValid = false
        } else {
            tilJobDescription.error = null
        }

        if (etLocation.text?.toString()?.trim().isNullOrEmpty()) {
            tilLocation.error = "Location is required"
            isValid = false
        } else {
            tilLocation.error = null
        }

        if (actvJobType.text?.toString()?.trim().isNullOrEmpty()) {
            actvJobType.error = "Job type is required"
            isValid = false
        } else {
            actvJobType.error = null
        }

        // Work arrangement not used by backend; no validation required

        if (actvExperienceLevel.text?.toString()?.trim().isNullOrEmpty()) {
            actvExperienceLevel.error = "Experience level is required"
            isValid = false
        } else {
            actvExperienceLevel.error = null
        }

        // Validate salary range
        val salaryMin = etSalaryMin.text?.toString()?.trim()?.toDoubleOrNull()
        val salaryMax = etSalaryMax.text?.toString()?.trim()?.toDoubleOrNull()

        if (salaryMin != null && salaryMax != null && salaryMin > salaryMax) {
            tilSalaryMax.error = "Maximum salary must be greater than minimum salary"
            isValid = false
        } else {
            tilSalaryMax.error = null
        }

        return isValid
    }

    private fun createJob(isDraft: Boolean) {
        setLoading(true)

        lifecycleScope.launch {
            try {
                val jobType = getJobTypeFromString(actvJobType.text.toString())
                val experienceLevel = getExperienceLevelFromString(actvExperienceLevel.text.toString())

                val industryString = etIndustry.text?.toString()?.trim()?.takeIf { it.isNotEmpty() }
                val requirementsString = etRequirements.text?.toString()?.trim()?.takeIf { it.isNotEmpty() }

                val request = CreateJobRequest(
                    title = etJobTitle.text.toString().trim(),
                    description = etJobDescription.text.toString().trim(),
                    companyName = etCompanyName.text.toString().trim(),
                    location = etLocation.text.toString().trim(),
                    jobType = jobType,
                    experienceLevel = experienceLevel,
                    salaryMin = etSalaryMin.text?.toString()?.trim()?.toIntOrNull(),
                    salaryMax = etSalaryMax.text?.toString()?.trim()?.toIntOrNull(),
                    industry = industryString,
                    requirements = requirementsString
                )

                val result = jobRepository.createJob(request)
                result.fold(
                    onSuccess = {
                        setLoading(false)
                        val message = if (isDraft) "Job draft saved successfully!" else "Job posted successfully!"
                        Toast.makeText(this@JobPostingActivity, message, Toast.LENGTH_SHORT).show()
                        // Navigate to the job listings so the recruiter can verify visibility
                        if (!isDraft) {
                            startActivity(Intent(this@JobPostingActivity, AllJobsActivity::class.java))
                        }
                        finish()
                    },
                    onFailure = { error ->
                        setLoading(false)
                        Toast.makeText(this@JobPostingActivity, "Failed to create job: ${error.message}", Toast.LENGTH_LONG).show()
                    }
                )
            } catch (e: Exception) {
                setLoading(false)
                Toast.makeText(this@JobPostingActivity, "An error occurred: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun getJobTypeFromString(jobTypeString: String): JobType {
        return when (jobTypeString.trim().lowercase()) {
            "full-time", "full time", "fulltime" -> JobType.FULL_TIME
            "part-time", "part time", "parttime" -> JobType.PART_TIME
            "contract" -> JobType.CONTRACT
            "internship" -> JobType.INTERNSHIP
            else -> JobType.FULL_TIME
        }
    }

    // Work arrangement mapping removed (not used by backend)

    private fun getExperienceLevelFromString(experienceLevelString: String): ExperienceLevel {
        return when (experienceLevelString.lowercase()) {
            "entry" -> ExperienceLevel.ENTRY
            "mid" -> ExperienceLevel.MID
            "senior" -> ExperienceLevel.SENIOR
            "executive" -> ExperienceLevel.EXECUTIVE
            else -> ExperienceLevel.ENTRY
        }
    }

    private fun setLoading(loading: Boolean) {
        progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        btnCreateJob.isEnabled = !loading
        btnSaveDraft.isEnabled = !loading
    }
}
