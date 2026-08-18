//CODE ATTRIBUTION
//01
//RecyclerView Adapter
//Adapted from: Android Developers. (2025). RecyclerView.Adapter. [online] Android Developers.
//Available at: https://developer.android.com/reference/androidx/recyclerview/widget/RecyclerView.Adapter
//Date Accessed: 01 October 2025

//02
//Material Design Components
//Adapted from: Material Design. (2025). Material Design Components. [online]
//Available at: https://material.io/components
//Date Accessed: 01 October 2025

package vcmsa.projects.careerconnect.ui.recruiter.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textview.MaterialTextView
import vcmsa.projects.careerconnect.R
import vcmsa.projects.careerconnect.domain.model.Job
import java.text.SimpleDateFormat
import java.util.*

/**
 * Adapter for displaying recruiter's job postings in a RecyclerView
 */
class RecruiterJobsAdapter(
    private val jobs: MutableList<Job>,
    private val onJobClick: (Job) -> Unit
) : RecyclerView.Adapter<RecruiterJobsAdapter.JobViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recruiter_job, parent, false)
        return JobViewHolder(view)
    }

    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
        holder.bind(jobs[position])
    }

    override fun getItemCount(): Int = jobs.size

    inner class JobViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: MaterialCardView = itemView.findViewById(R.id.cardView)
        private val tvJobTitle: MaterialTextView = itemView.findViewById(R.id.tvJobTitle)
        private val tvCompanyName: MaterialTextView = itemView.findViewById(R.id.tvCompanyName)
        private val tvLocation: MaterialTextView = itemView.findViewById(R.id.tvLocation)
        private val tvJobType: MaterialTextView = itemView.findViewById(R.id.tvJobType)
        private val tvApplicationCount: MaterialTextView = itemView.findViewById(R.id.tvApplicationCount)
        private val tvPostedDate: MaterialTextView = itemView.findViewById(R.id.tvPostedDate)

        fun bind(job: Job) {
            tvJobTitle.text = job.title
            tvCompanyName.text = job.companyName
            tvLocation.text = job.location
            tvJobType.text = formatJobType(job.jobType)
            tvApplicationCount.text = itemView.context.getString(R.string.applications_count, job.applicationCount)
            tvPostedDate.text = itemView.context.getString(R.string.posted) + ": " + formatDate(job.createdAt)

            // Click listener
            cardView.setOnClickListener {
                onJobClick(job)
            }
        }

        private fun formatJobType(jobType: vcmsa.projects.careerconnect.domain.model.JobType): String {
            return when (jobType) {
                vcmsa.projects.careerconnect.domain.model.JobType.FULL_TIME -> itemView.context.getString(R.string.job_type_full_time)
                vcmsa.projects.careerconnect.domain.model.JobType.PART_TIME -> itemView.context.getString(R.string.job_type_part_time)
                vcmsa.projects.careerconnect.domain.model.JobType.CONTRACT -> itemView.context.getString(R.string.job_type_contract)
                vcmsa.projects.careerconnect.domain.model.JobType.INTERNSHIP -> itemView.context.getString(R.string.job_type_internship)
            }
        }

        private fun formatDate(dateString: String): String {
            return try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                val date = inputFormat.parse(dateString)
                outputFormat.format(date ?: Date())
            } catch (e: Exception) {
                dateString
            }
        }
    }
}

