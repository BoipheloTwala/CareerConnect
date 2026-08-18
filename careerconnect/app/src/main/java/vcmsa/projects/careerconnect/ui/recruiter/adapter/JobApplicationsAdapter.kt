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
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textview.MaterialTextView
import vcmsa.projects.careerconnect.R
import vcmsa.projects.careerconnect.domain.model.ApplicationStatus
import vcmsa.projects.careerconnect.domain.model.JobApplication
import java.text.SimpleDateFormat
import java.util.*

/**
 * Adapter for displaying job applications in a RecyclerView
 */
class JobApplicationsAdapter(
    private val applications: MutableList<JobApplication>,
    private val onApplicationClick: (JobApplication) -> Unit,
    private val onStatusUpdate: (JobApplication, ApplicationStatus) -> Unit
) : RecyclerView.Adapter<JobApplicationsAdapter.ApplicationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApplicationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_job_application, parent, false)
        return ApplicationViewHolder(view)
    }

    override fun onBindViewHolder(holder: ApplicationViewHolder, position: Int) {
        holder.bind(applications[position])
    }

    override fun getItemCount(): Int = applications.size

    inner class ApplicationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: MaterialCardView = itemView.findViewById(R.id.cardView)
        private val tvApplicantName: MaterialTextView = itemView.findViewById(R.id.tvApplicantName)
        private val tvAppliedDate: MaterialTextView = itemView.findViewById(R.id.tvAppliedDate)
        private val tvStatus: MaterialTextView = itemView.findViewById(R.id.tvStatus)
        private val tvCoverLetter: MaterialTextView = itemView.findViewById(R.id.tvCoverLetter)
        private val btnUpdateStatus: MaterialButton = itemView.findViewById(R.id.btnUpdateStatus)

        fun bind(application: JobApplication) {
            // Set applicant name using profile data if available
            val applicantName = if (application.applicantProfile != null) {
                "${application.applicantProfile.firstName} ${application.applicantProfile.lastName}"
            } else {
                "Applicant: ${application.applicantUid.take(8)}..."
            }
            tvApplicantName.text = applicantName

            // Format applied date
            val appliedDate = formatDate(application.appliedAt)
            tvAppliedDate.text = "Applied: $appliedDate"

            // Set status with color coding
            tvStatus.text = application.status.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
            setStatusColor(application.status)

            // Set cover letter preview
            if (!application.coverLetter.isNullOrBlank()) {
                tvCoverLetter.text = application.coverLetter.take(100) + if (application.coverLetter.length > 100) "..." else ""
                tvCoverLetter.visibility = View.VISIBLE
            } else {
                tvCoverLetter.visibility = View.GONE
            }

            // Click listeners
            cardView.setOnClickListener {
                onApplicationClick(application)
            }

            btnUpdateStatus.setOnClickListener {
                showStatusUpdateDialog(application)
            }
        }

        private fun setStatusColor(status: ApplicationStatus) {
            val colorRes = when (status) {
                ApplicationStatus.PENDING -> R.color.status_pending
                ApplicationStatus.UNDER_REVIEW -> R.color.status_under_review
                ApplicationStatus.SHORTLISTED -> R.color.status_shortlisted
                ApplicationStatus.INTERVIEW_SCHEDULED -> R.color.status_interview
                ApplicationStatus.ACCEPTED -> R.color.status_accepted
                ApplicationStatus.REJECTED -> R.color.status_rejected
                ApplicationStatus.WITHDRAWN -> R.color.status_withdrawn
            }
            tvStatus.setTextColor(itemView.context.getColor(colorRes))
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

        private fun showStatusUpdateDialog(application: JobApplication) {
            val context = itemView.context
            val statuses = arrayOf(
                context.getString(R.string.pending) to ApplicationStatus.PENDING,
                context.getString(R.string.accepted) to ApplicationStatus.ACCEPTED,
                context.getString(R.string.rejected) to ApplicationStatus.REJECTED
            )

            val statusNames = statuses.map { it.first }.toTypedArray()

            androidx.appcompat.app.AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.update_application_status))
                .setItems(statusNames) { _, which ->
                    val selectedStatus = statuses[which].second
                    onStatusUpdate(application, selectedStatus)
                }
                .setNegativeButton(context.getString(R.string.cancel), null)
                .show()
        }
    }
}
