//CODE ATTRIBUTION
//01
//Recycler View
//Adapted from: Android Developers. (2025). RecyclerView. [online] Android Developers.
//Available at: https://developer.android.com/reference/androidx/recyclerview/widget/RecyclerView
//Date Accessed: 30 September 2025

//02
//Linear Layout Manager
//Adapted from: Android Developers. (2025). LinearLayoutManager. [online] Android Developers.
//Available at: https://developer.android.com/reference/androidx/recyclerview/widget/LinearLayoutManager
//Date Accessed: 30 September 2025

//03
//Material Button
//Adapted from: Android Developers. (2025). MaterialButton. [online] Android Developers.
//Available at: https://developer.android.com/reference/com/google/android/material/button/MaterialButton
//Date Accessed: 30 September 2025

//04
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
import kotlinx.coroutines.launch
import vcmsa.projects.careerconnect.R
import vcmsa.projects.careerconnect.data.repository.OfflineSavedJobRepository
import vcmsa.projects.careerconnect.domain.model.SavedJob
import vcmsa.projects.careerconnect.utils.NetworkConnectivityManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collectLatest

/**
 * Activity for job seekers to view and manage their bookmarked/saved jobs
 */
class BookmarkedJobsActivity : AppCompatActivity() {

    private lateinit var rvSavedJobs: RecyclerView
    private lateinit var progressBar: CircularProgressIndicator
    private lateinit var offlineIndicator: View

    private lateinit var savedJobsAdapter: SavedJobsAdapter
    private lateinit var savedJobRepository: OfflineSavedJobRepository
    private lateinit var networkManager: NetworkConnectivityManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bookmarked_jobs)

        // Initialize repositories and managers
        savedJobRepository = OfflineSavedJobRepository(this)
        networkManager = NetworkConnectivityManager(this)

        initializeViews()
        setupRecyclerView()
        observeNetworkStatus()
        observeSavedJobs()
    }

    private fun initializeViews() {
        rvSavedJobs = findViewById(R.id.rvSavedJobs)
        progressBar = findViewById(R.id.progressBar)
        offlineIndicator = findViewById(R.id.offlineIndicator)

        // Back button
        findViewById<MaterialButton>(R.id.btnBack)?.setOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        savedJobsAdapter = SavedJobsAdapter(
            onJobClick = { savedJob ->
                // Navigate to job details with robust jobId fallback
                val jobId = when {
                    !savedJob.jobId.isNullOrBlank() -> savedJob.jobId
                    savedJob.job?.id?.isNotBlank() == true -> savedJob.job!!.id
                    else -> null
                }
                if (jobId.isNullOrBlank()) {
                    showError(getString(R.string.job_id_not_available))
                    return@SavedJobsAdapter
                }
                val intent = Intent(this, JobDetailsActivity::class.java)
                intent.putExtra("job_id", jobId)
                startActivity(intent)
            },
            onRemoveClick = { savedJob ->
                removeSavedJob(savedJob)
            }
        )
        
        rvSavedJobs.layoutManager = LinearLayoutManager(this)
        rvSavedJobs.adapter = savedJobsAdapter
    }

    /**
     * Observe saved jobs from local database (offline-first)
     */
    private fun observeSavedJobs() {
        setLoading(true)
        
        lifecycleScope.launch {
            try {
                // Collect saved jobs from Flow (reactive updates)
                savedJobRepository.getSavedJobsFlow().collectLatest { savedJobs ->
                    savedJobsAdapter.updateJobs(savedJobs)
                    setLoading(false)
                    
                    // Show empty state if no jobs
                    if (savedJobs.isEmpty()) {
                        showEmptyState()
                    }
                }
            } catch (e: Exception) {
                setLoading(false)
                showError(getString(R.string.error_occurred, e.message))
            }
        }
        
        // Refresh from server if online
        lifecycleScope.launch {
            if (networkManager.isConnected()) {
                savedJobRepository.refreshFromServer()
            }
        }
    }
    
    /**
     * Observe network connectivity status
     */
    private fun observeNetworkStatus() {
        lifecycleScope.launch {
            networkManager.observeConnectivity().collectLatest { isConnected ->
                updateNetworkIndicator(isConnected)
                
                // Sync pending changes when back online
                if (isConnected) {
                    syncPendingChanges()
                }
            }
        }
    }
    
    /**
     * Update offline/online indicator
     */
    private fun updateNetworkIndicator(isConnected: Boolean) {
        if (isConnected) {
            offlineIndicator.visibility = View.GONE
        } else {
            offlineIndicator.visibility = View.VISIBLE
        }
    }
    
    /**
     * Sync pending changes when back online
     */
    private fun syncPendingChanges() {
        lifecycleScope.launch {
            try {
                val result = savedJobRepository.syncPendingOperations()
                result.onSuccess { syncCount ->
                    if (syncCount > 0) {
                        Snackbar.make(
                            rvSavedJobs,
                            getString(R.string.synced_changes, syncCount),
                            Snackbar.LENGTH_SHORT
                        ).show()
                        
                        // Refresh from server after sync
                        savedJobRepository.refreshFromServer()
                    }
                }
            } catch (e: Exception) {
                // Silent fail - sync will retry later
            }
        }
    }
    
    /**
     * Show empty state message
     */
    private fun showEmptyState() {
        // You can add an empty state view in the layout if desired
        Toast.makeText(this, getString(R.string.no_bookmarked_jobs), Toast.LENGTH_SHORT).show()
    }

    /**
     * Remove a saved job (works offline)
     */
    private fun removeSavedJob(savedJob: SavedJob) {
        lifecycleScope.launch {
            try {
                val jobId = when {
                    !savedJob.jobId.isNullOrBlank() -> savedJob.jobId
                    savedJob.job?.id?.isNotBlank() == true -> savedJob.job!!.id
                    else -> null
                }
                
                if (jobId.isNullOrBlank()) {
                    showError(getString(R.string.job_id_not_available_for_removal))
                    return@launch
                }
                
                // Remove from local cache (works offline)
                val result = savedJobRepository.removeSavedJob(jobId)
                result.fold(
                    onSuccess = {
                        val message = if (networkManager.isConnected()) {
                            getString(R.string.job_removed_from_saved)
                        } else {
                            getString(R.string.job_removed_offline)
                        }
                        Toast.makeText(this@BookmarkedJobsActivity, message, Toast.LENGTH_SHORT).show()
                    },
                    onFailure = { error ->
                        showError(getString(R.string.failed_to_remove_job))
                    }
                )
            } catch (e: Exception) {
                showError(getString(R.string.error_occurred, e.message))
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        progressBar.visibility = if (loading) View.VISIBLE else View.GONE
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
