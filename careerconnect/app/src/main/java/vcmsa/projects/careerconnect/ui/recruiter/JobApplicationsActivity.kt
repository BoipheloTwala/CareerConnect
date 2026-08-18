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

//03
//Material Design Components
//Adapted from: Material Design. (2025). Material Design Components. [online]
//Available at: https://material.io/components
//Date Accessed: 01 October 2025

package vcmsa.projects.careerconnect.ui.recruiter

import android.content.Intent
import android.net.Uri
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
import vcmsa.projects.careerconnect.domain.model.ApplicationStatus
import vcmsa.projects.careerconnect.domain.model.Job
import vcmsa.projects.careerconnect.domain.model.JobApplication
import vcmsa.projects.careerconnect.ui.recruiter.adapter.JobApplicationsAdapter
import vcmsa.projects.careerconnect.utils.FileDownloader

/**
 * Activity for recruiters to view job applications for their posted jobs
 */
class JobApplicationsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: CircularProgressIndicator
    private lateinit var emptyStateView: View
    private lateinit var emptyStateText: MaterialTextView
    private lateinit var errorStateView: View
    private lateinit var errorStateText: MaterialTextView
    private lateinit var retryButton: MaterialButton

    private val jobRepository = JobRepository()
    private var jobId: String? = null
    private var jobTitle: String? = null
    private var companyName: String? = null
    private var applications = mutableListOf<JobApplication>()
    private lateinit var adapter: JobApplicationsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_job_applications)

        // Get job information from intent
        jobId = intent.getStringExtra("job_id")
        jobTitle = intent.getStringExtra("job_title")
        companyName = intent.getStringExtra("company_name")
        
        if (jobId.isNullOrEmpty()) {
            showError(getString(R.string.job_info_not_provided))
            finish()
            return
        }

        initializeViews()
        setupRecyclerView()
        setupClickListeners()
        loadJobApplications()
    }

    private fun initializeViews() {
        recyclerView = findViewById(R.id.recyclerViewApplications)
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

        // Set job title in toolbar
        findViewById<MaterialTextView>(R.id.tvJobTitle)?.text = jobTitle ?: getString(R.string.default_job_applications_title)
        findViewById<MaterialTextView>(R.id.tvCompanyName)?.text = companyName ?: ""
    }

    private fun setupRecyclerView() {
        adapter = JobApplicationsAdapter(
            applications = applications,
            onApplicationClick = { application ->
                // Handle application click - could show details or update status
                showApplicationDetails(application)
            },
            onStatusUpdate = { application, newStatus ->
                updateApplicationStatus(application, newStatus)
            }
        )
        
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun setupClickListeners() {
        retryButton.setOnClickListener {
            loadJobApplications()
        }
    }

    private fun loadJobApplications() {
        val currentJobId = jobId ?: return
        
        setLoading(true)
        hideEmptyState()
        hideErrorState()

        lifecycleScope.launch {
            try {
                // Use the recruiter applications endpoint with job filter
                val result = jobRepository.getRecruiterApplications(currentJobId)
                result.fold(
                    onSuccess = { applicationsList ->
                        setLoading(false)
                        applications.clear()
                        applications.addAll(applicationsList)
                        adapter.notifyDataSetChanged()
                        
                        if (applications.isEmpty()) {
                            showEmptyState()
                        } else {
                            showApplications()
                        }
                    },
                    onFailure = { error ->
                        setLoading(false)
                        showError(getString(R.string.unable_to_load_applications))
                    }
                )
            } catch (e: Exception) {
                setLoading(false)
                showError(getString(R.string.error_occurred, e.message))
            }
        }
    }

    private fun showApplicationDetails(application: JobApplication) {
        // TODO: Implement application details dialog or activity
        Toast.makeText(this, getString(R.string.application_details_for, application.id), Toast.LENGTH_SHORT).show()
    }

    private fun updateApplicationStatus(application: JobApplication, newStatus: ApplicationStatus) {
        lifecycleScope.launch {
            try {
                // Show loading state
                Toast.makeText(this@JobApplicationsActivity, getString(R.string.updating_status), Toast.LENGTH_SHORT).show()

                // Call API to update status
                val result = jobRepository.updateApplicationStatus(
                    applicationId = application.id,
                    request = vcmsa.projects.careerconnect.domain.model.UpdateApplicationStatusRequest(
                        status = newStatus
                    )
                )

                result.fold(
                    onSuccess = { updatedApplication ->
                        // Update the local application list
                        val index = applications.indexOfFirst { it.id == application.id }
                        if (index != -1) {
                            applications[index] = updatedApplication
                            adapter.notifyItemChanged(index)
                        }

                        Toast.makeText(
                            this@JobApplicationsActivity,
                            getString(R.string.status_updated_to, newStatus.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }),
                            Toast.LENGTH_LONG
                        ).show()
                    },
                    onFailure = { error ->
                        Toast.makeText(
                            this@JobApplicationsActivity,
                            getString(R.string.failed_to_update_status),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                )
            } catch (e: Exception) {
                Toast.makeText(
                    this@JobApplicationsActivity,
                    getString(R.string.error_updating_status),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        recyclerView.visibility = if (loading) View.GONE else View.VISIBLE
    }

    private fun showEmptyState() {
        emptyStateView.visibility = View.VISIBLE
        emptyStateText.text = getString(R.string.no_applications_found)
        recyclerView.visibility = View.GONE
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

    private fun showApplications() {
        recyclerView.visibility = View.VISIBLE
        hideEmptyState()
        hideErrorState()
    }
}