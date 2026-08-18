//CODE ATTRIBUTION
//01
//View
//Adapted from: Android Developers. (2025). View. [online] Android Developers.
//Available at: https://developer.android.com/reference/android/view/View
//Date Accessed: 15 November 2025

package vcmsa.projects.careerconnect.ui.common

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import vcmsa.projects.careerconnect.R

/**
 * Custom view for showing offline/sync status indicator
 * Shows at top of screen with colored background
 */
class OfflineIndicator @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    
    private val textView: TextView
    
    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER
        setPadding(16, 8, 16, 8)
        
        textView = TextView(context).apply {
            textSize = 14f
            setTextColor(ContextCompat.getColor(context, android.R.color.white))
        }
        
        addView(textView)
        visibility = View.GONE
    }
    
    /**
     * Show offline indicator
     */
    fun showOffline(pendingCount: Int = 0) {
        visibility = View.VISIBLE
        setBackgroundColor(ContextCompat.getColor(context, android.R.color.holo_orange_dark))
        
        textView.text = if (pendingCount > 0) {
            context.getString(R.string.pending_sync, pendingCount)
        } else {
            context.getString(R.string.offline_mode)
        }
    }
    
    /**
     * Show syncing indicator
     */
    fun showSyncing() {
        visibility = View.VISIBLE
        setBackgroundColor(ContextCompat.getColor(context, android.R.color.holo_blue_dark))
        textView.text = context.getString(R.string.syncing)
    }
    
    /**
     * Show sync complete (briefly)
     */
    fun showSyncComplete() {
        visibility = View.VISIBLE
        setBackgroundColor(ContextCompat.getColor(context, android.R.color.holo_green_dark))
        textView.text = context.getString(R.string.sync_complete)
        
        // Hide after 2 seconds
        postDelayed({
            hide()
        }, 2000)
    }
    
    /**
     * Show online indicator (briefly)
     */
    fun showOnline() {
        visibility = View.VISIBLE
        setBackgroundColor(ContextCompat.getColor(context, android.R.color.holo_green_dark))
        textView.text = context.getString(R.string.online_mode)
        
        // Hide after 1 second
        postDelayed({
            hide()
        }, 1000)
    }
    
    /**
     * Hide indicator
     */
    fun hide() {
        visibility = View.GONE
    }
}

