//CODE ATTRIBUTION
//01
//Android Activity
//Adapted from: Android Developers. (2025). Activity. [online] Android Developers.
//Available at: https://developer.android.com/reference/android/app/Activity
//Date Accessed: 01 October 2025

//02
//RecyclerView
//Adapted from: Android Developers. (2025). RecyclerView. [online] Android Developers.
//Available at: https://developer.android.com/reference/androidx/recyclerview/widget/RecyclerView
//Date Accessed: 01 October 2025

package vcmsa.projects.careerconnect.ui.recruiter

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.textview.MaterialTextView
import kotlinx.coroutines.launch
import vcmsa.projects.careerconnect.R
import vcmsa.projects.careerconnect.data.repository.JobRepository
import vcmsa.projects.careerconnect.domain.model.Job
import vcmsa.projects.careerconnect.ui.recruiter.adapter.RecruiterJobsAdapter

/**
 * Activity for recruiters to view all their job postings and navigate to applications
 */
class RecruiterJobApplicationsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: CircularProgressIndicator
    private lateinit var emptyStateView: View
    private lateinit var emptyStateText: MaterialTextView
    private lateinit var errorStateView: View
    private lateinit var errorStateText: MaterialTextView
    private lateinit var retryButton: MaterialButton

    private val jobRepository = JobRepository()
    private var jobs = mutableListOf<Job>()
    private lateinit var adapter: RecruiterJobsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recruiter_job_applications)

        initializeViews()
        setupRecyclerView()
        setupClickListeners()
        loadRecruiterJobs()
    }

    private fun initializeViews() {
        recyclerView = findViewById(R.id.recyclerViewJobs)
        progressBar = findViewById(R.id.progressBar)
        emptyStateView = findViewById(R.id.emptyStateView)
        emptyStateText = findViewById(R.id.emptyStateText)
        errorStateView = findViewById(R.id.errorStateView)
        errorStateText = findViewById(R.id.errorStateText)
        retryButton = findViewById(R.id.retryButton)

        // Back button
        findViewById<MaterialButton>(R.id.btnBack)?.setOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        adapter = RecruiterJobsAdapter(
            jobs = jobs,
            onJobClick = { job ->
                // Navigate to job applications for this specific job
                val intent = Intent(this, JobApplicationsActivity::class.java)
                intent.putExtra("job_id", job.id)
                intent.putExtra("job_title", job.title)
                intent.putExtra("company_name", job.companyName)
                startActivity(intent)
            }
        )
        
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun setupClickListeners() {
        retryButton.setOnClickListener {
            loadRecruiterJobs()
        }
    }

    private fun loadRecruiterJobs() {
        setLoading(true)
        hideEmptyState()
        hideErrorState()

        lifecycleScope.launch {
            try {
                // Get recruiter applications to extract unique jobs
                val result = jobRepository.getRecruiterApplications()
                result.fold(
                    onSuccess = { applications ->
                        setLoading(false)
                        jobs.clear()
                        
                        // Extract unique jobs from applications
                        val uniqueJobs = applications.mapNotNull { application ->
                            application.jobPosting?.let { jobPosting ->
                                // Convert JobPosting to Job object
                                Job(
                                    id = jobPosting.id,
                                    recruiterUid = "temp_recruiter_uid", // Temporary value
                                    title = jobPosting.title,
                                    description = "Job description not available", // Default description
                                    companyName = jobPosting.companyName,
                                    location = jobPosting.location,
                                    jobType = jobPosting.jobType,
                                    experienceLevel = vcmsa.projects.careerconnect.domain.model.ExperienceLevel.ENTRY, // Default
                                    salaryMin = null,
                                    salaryMax = null,
                                    currency = null,
                                    industry = null,
                                    requirements = null,
                                    benefits = null,
                                    applicationCount = applications.count { it.jobId == jobPosting.id },
                                    isActive = true,
                                    createdAt = "2024-01-01T00:00:00", // Default date
                                    updatedAt = "2024-01-01T00:00:00" // Default date
                                )
                            }
                        }.distinctBy { it.id }
                        
                        jobs.addAll(uniqueJobs)
                        adapter.notifyDataSetChanged()
                        
                        if (jobs.isEmpty()) {
                            showEmptyState()
                        } else {
                            showJobs()
                        }
                    },
                    onFailure = { error ->
                        setLoading(false)
                        showError(getString(R.string.unable_to_load_job_applications))
                    }
                )
            } catch (e: Exception) {
                setLoading(false)
                showError(getString(R.string.error_loading_applications))
            }
        }
    }


    private fun setLoading(loading: Boolean) {
        progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        recyclerView.visibility = if (loading) View.GONE else View.VISIBLE
    }

    private fun showEmptyState() {
        emptyStateView.visibility = View.VISIBLE
        emptyStateText.text = getString(R.string.no_job_postings_found_create_first)
        recyclerView.visibility = View.GONE
        
        // Add a button to navigate to job posting
        val createJobButton = findViewById<MaterialButton>(R.id.btnCreateJob)
        createJobButton?.setOnClickListener {
            val intent = Intent(this, vcmsa.projects.careerconnect.ui.recruiter.JobPostingActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun hideEmptyState() {
        emptyStateView.visibility = View.GONE
    }

    private fun showError(message: String) {
        errorStateView.visibility = View.VISIBLE
        errorStateText.text = message
        recyclerView.visibility = View.GONE
    }

    private fun hideErrorState() {
        errorStateView.visibility = View.GONE
    }

    private fun showJobs() {
        recyclerView.visibility = View.VISIBLE
        hideEmptyState()
        hideErrorState()
    }
}
