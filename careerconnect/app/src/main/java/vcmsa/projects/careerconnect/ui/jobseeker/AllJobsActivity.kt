//CODE ATTRIBUTION
//01
//Toast
//Adapted from: Android Developers. (2025). Toast. [online] Android Developers.
//Available at: https://developer.android.com/reference/android/widget/Toast
//Date Accessed: 30 September 2025

//02
//Recycler View
//Adapted from: Android Developers. (2025). RecyclerView. [online] Android Developers.
//Available at: https://developer.android.com/reference/androidx/recyclerview/widget/RecyclerView
//Date Accessed: 30 September 2025

//03
//Linear Layout Manager
//Adapted from: Android Developers. (2025). LinearLayoutManager. [online] Android Developers.
//Available at: https://developer.android.com/reference/androidx/recyclerview/widget/LinearLayoutManager
//Date Accessed: 30 September 2025

//04
//Recycler View 
//Adapted from: Android Developers. (2025). RecyclerView.OnScrollListener. [online] Android Developers.
//Available at: https://developer.android.com/reference/androidx/recyclerview/widget/RecyclerView.OnScrollListener
//Date Accessed: 30 September 2025

//05
//Material Button
//Adapted from: Android Developers. (2025). MaterialButton. [online] Android Developers.
//Available at: https://developer.android.com/reference/com/google/android/material/button/MaterialButton
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

//08
//Jetpack Compose Progress indicators
//Adapted from: Android Developers. (2025). Progress indicators (Jetpack Compose). [online] Android Developers.
//Available at: https://developer.android.com/develop/ui/compose/components/progress
//Date Accessed: 30 September 2025

package vcmsa.projects.careerconnect.ui.jobseeker

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
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import vcmsa.projects.careerconnect.R
import vcmsa.projects.careerconnect.data.repository.JobRepository
import vcmsa.projects.careerconnect.domain.model.*

/**
 * Activity for job seekers to browse and apply to all available jobs
 */
class AllJobsActivity : AppCompatActivity() {

    private lateinit var tilSearch: TextInputLayout
    private lateinit var etSearch: TextInputEditText
    private lateinit var rvJobs: RecyclerView
    private lateinit var btnSearch: MaterialButton
    private lateinit var btnClearFilters: MaterialButton
    private lateinit var progressBar: CircularProgressIndicator

    private lateinit var jobsAdapter: JobsAdapter
    private val jobRepository = JobRepository()
    private var currentPage = 1
    private var isLoading = false
    private var hasMoreJobs = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_jobs)

        initializeViews()
        setupRecyclerView()
        setupClickListeners()
        loadJobs()
    }

    private fun initializeViews() {
        tilSearch = findViewById(R.id.tilSearch)
        etSearch = findViewById(R.id.etSearch)
        rvJobs = findViewById(R.id.rvJobs)
        btnSearch = findViewById(R.id.btnSearch)
        btnClearFilters = findViewById(R.id.btnClearFilters)
        progressBar = findViewById(R.id.progressBar)

        // Back button
        findViewById<MaterialButton>(R.id.btnBack)?.setOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        jobsAdapter = JobsAdapter { job ->
            // Navigate to job details
            val intent = Intent(this, JobDetailsActivity::class.java)
            intent.putExtra("job_id", job.id)
            startActivity(intent)
        }
        
        rvJobs.layoutManager = LinearLayoutManager(this)
        rvJobs.adapter = jobsAdapter

        // Add scroll listener for pagination
        rvJobs.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val totalItemCount = layoutManager.itemCount
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()

                if (!isLoading && hasMoreJobs && lastVisibleItem >= totalItemCount - 5) {
                    loadMoreJobs()
                }
            }
        })
    }

    private fun setupClickListeners() {
        btnSearch.setOnClickListener {
            searchJobs()
        }

        btnClearFilters.setOnClickListener {
            clearFilters()
        }
    }

    private fun loadJobs() {
        setLoading(true)
        currentPage = 1
        hasMoreJobs = false // Using recent jobs endpoint (no pagination support)

        lifecycleScope.launch {
            try {
                val result = jobRepository.getRecentJobs(limit = 20)
                result.fold(
                    onSuccess = { jobs ->
                        jobsAdapter.updateJobs(jobs)
                        hasMoreJobs = false
                        setLoading(false)
                    },
                    onFailure = { _ ->
                        // Fallback to featured jobs if recent fails (e.g., server 500)
                        val fallback = jobRepository.getFeaturedJobs(limit = 20)
                        fallback.fold(
                            onSuccess = { featured ->
                                jobsAdapter.updateJobs(featured)
                                hasMoreJobs = false
                                setLoading(false)
                            },
                            onFailure = { _ ->
                                // Final fallback to generic listing
                                val fallback2 = jobRepository.getAllJobs(limit = 20)
                                fallback2.fold(
                                    onSuccess = { all ->
                                        jobsAdapter.updateJobs(all)
                                        hasMoreJobs = false
                                        setLoading(false)
                                    },
                                    onFailure = { error3 ->
                                        setLoading(false)
                                        showError(getString(R.string.failed_to_load_jobs))
                                    }
                                )
                            }
                        )
                    }
                )
            } catch (e: Exception) {
                setLoading(false)
                showError(getString(R.string.error_occurred, e.message))
            }
        }
    }

    private fun searchJobs() {
        val query = etSearch.text?.toString()?.trim()
        if (query.isNullOrEmpty()) {
            loadJobs()
            return
        }

        setLoading(true)
        currentPage = 1
        hasMoreJobs = false

        lifecycleScope.launch {
            try {
                // Fallback: fetch recent jobs and filter client-side
                val result = jobRepository.getRecentJobs(limit = 50)
                result.fold(
                    onSuccess = { jobs ->
                        val filtered = jobs.filter { job ->
                            job.title.contains(query, ignoreCase = true) ||
                            job.companyName.contains(query, ignoreCase = true) ||
                            job.location.contains(query, ignoreCase = true)
                        }
                        jobsAdapter.updateJobs(filtered)
                        hasMoreJobs = false
                        setLoading(false)
                    },
                    onFailure = { _ ->
                        // Fallback to featured then filter
                        val fallback = jobRepository.getFeaturedJobs(limit = 50)
                        fallback.fold(
                            onSuccess = { featured ->
                                val filtered = featured.filter { job ->
                                    job.title.contains(query, ignoreCase = true) ||
                                    job.companyName.contains(query, ignoreCase = true) ||
                                    job.location.contains(query, ignoreCase = true)
                                }
                                jobsAdapter.updateJobs(filtered)
                                hasMoreJobs = false
                                setLoading(false)
                            },
                            onFailure = { _ ->
                                // Final fallback to generic listing then filter
                                val fallback2 = jobRepository.getAllJobs(limit = 50)
                                fallback2.fold(
                                    onSuccess = { all ->
                                        val filtered = all.filter { job ->
                                            job.title.contains(query, ignoreCase = true) ||
                                            job.companyName.contains(query, ignoreCase = true) ||
                                            job.location.contains(query, ignoreCase = true)
                                        }
                                        jobsAdapter.updateJobs(filtered)
                                        hasMoreJobs = false
                                        setLoading(false)
                                    },
                                    onFailure = { error3 ->
                                        setLoading(false)
                                        showError(getString(R.string.search_failed))
                                    }
                                )
                            }
                        )
                    }
                )
            } catch (e: Exception) {
                setLoading(false)
                showError(getString(R.string.error_occurred, e.message))
            }
        }
    }

    private fun loadMoreJobs() {
        // Pagination disabled while using recent jobs endpoint
        return
    }

    private fun clearFilters() {
        etSearch.text?.clear()
        loadJobs()
    }

    private fun setLoading(loading: Boolean) {
        progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        btnSearch.isEnabled = !loading
        btnClearFilters.isEnabled = !loading
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
