//CODE ATTRIBUTION
//01
//Recycler View
//Adapted from: Android Developers. (2025). RecyclerView.Adapter. [online] Android Developers.
//Available at: https://developer.android.com/reference/androidx/recyclerview/widget/RecyclerView.Adapter
//Date Accessed: 30 September 2025

//02
//RecyclerView 
//Adapted from: Android Developers. (2025). RecyclerView.ViewHolder. [online] Android Developers.
//Available at: https://developer.android.com/reference/androidx/recyclerview/widget/RecyclerView.ViewHolder
//Date Accessed: 30 September 2025

//03
//View
//Adapted from: Android Developers. (2025). View. [online] Android Developers.
//Available at: https://developer.android.com/reference/android/view/View
//Date Accessed: 30 September 2025

//04
//Text View
//Adapted from: Android Developers. (2025). TextView. [online] Android Developers.
//Available at: https://developer.android.com/reference/android/widget/TextView
//Date Accessed: 30 September 2025

//05
//notify Data Set Changed
//Adapted from: Android Developers. (2025). RecyclerView.Adapter.notifyDataSetChanged. [online] Android Developers.
//Available at: https://developer.android.com/reference/androidx/recyclerview/widget/RecyclerView.Adapter#notifyDataSetChanged()
//Date Accessed: 30 September 2025

package vcmsa.projects.careerconnect.ui.jobseeker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import vcmsa.projects.careerconnect.R
import vcmsa.projects.careerconnect.domain.model.Job
import java.text.NumberFormat
import java.util.*

/**
 * Adapter for displaying job listings in RecyclerView
 */
class JobsAdapter(
    private val onJobClick: (Job) -> Unit
) : RecyclerView.Adapter<JobsAdapter.JobViewHolder>() {

    private var jobs = mutableListOf<Job>()

    fun updateJobs(newJobs: List<Job>) {
        jobs.clear()
        jobs.addAll(newJobs)
        notifyDataSetChanged()
    }

    fun addJobs(newJobs: List<Job>) {
        val startPosition = jobs.size
        jobs.addAll(newJobs)
        notifyItemRangeInserted(startPosition, newJobs.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_job, parent, false)
        return JobViewHolder(view)
    }

    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
        holder.bind(jobs[position])
    }

    override fun getItemCount(): Int = jobs.size

    inner class JobViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvJobTitle: TextView = itemView.findViewById(R.id.tvJobTitle)
        private val tvCompanyName: TextView = itemView.findViewById(R.id.tvCompanyName)
        private val tvLocation: TextView = itemView.findViewById(R.id.tvLocation)
        private val tvJobType: TextView = itemView.findViewById(R.id.tvJobType)
        private val tvExperienceLevel: TextView = itemView.findViewById(R.id.tvExperienceLevel)
        private val tvSalary: TextView = itemView.findViewById(R.id.tvSalary)
        private val tvPostedDate: TextView = itemView.findViewById(R.id.tvPostedDate)

        fun bind(job: Job) {
            tvJobTitle.text = job.title
            tvCompanyName.text = job.companyName
            tvLocation.text = job.location
            tvJobType.text = formatJobType(job.jobType)
            // Work arrangement and skills removed from schema; leave blank
            itemView.findViewById<TextView>(R.id.tvWorkArrangement)?.text = ""
            tvExperienceLevel.text = formatExperienceLevel(job.experienceLevel)
            tvSalary.text = formatSalary(job.salaryMin, job.salaryMax, job.currency ?: "ZAR")
            tvPostedDate.text = formatDate(job.createdAt)

            itemView.setOnClickListener {
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

        // Work arrangement no longer used

        private fun formatExperienceLevel(experienceLevel: vcmsa.projects.careerconnect.domain.model.ExperienceLevel): String {
            return when (experienceLevel) {
                vcmsa.projects.careerconnect.domain.model.ExperienceLevel.ENTRY -> itemView.context.getString(R.string.experience_entry)
                vcmsa.projects.careerconnect.domain.model.ExperienceLevel.MID -> itemView.context.getString(R.string.experience_mid)
                vcmsa.projects.careerconnect.domain.model.ExperienceLevel.SENIOR -> itemView.context.getString(R.string.experience_senior)
                vcmsa.projects.careerconnect.domain.model.ExperienceLevel.EXECUTIVE -> itemView.context.getString(R.string.experience_executive)
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
                else -> itemView.context.getString(R.string.salary_not_specified)
            }
        }

        private fun formatDate(dateString: String): String {
            return try {
                // Simple date formatting - in production, use proper date parsing
                itemView.context.getString(R.string.posted) + " " + dateString.substring(0, 10)
            } catch (e: Exception) {
                itemView.context.getString(R.string.posted) + " " + itemView.context.getString(R.string.recently)
            }
        }
    }
}
