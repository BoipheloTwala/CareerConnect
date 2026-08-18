//CODE ATTRIBUTION
//01
//Firebase for Android
//Adapted from: Firebase. (2025). FirebaseMessagingService  |  Firebase SDKs for Android. [online] Firebase.
//Available at: https://firebase.google.com/docs/reference/android/com/google/firebase/messaging/FirebaseMessagingService
//Date Accessed: 12 September 2025

//02
//Firebase Token Management
//Adapted from: Firebase. (2025). Best practices for FCM registration token management  |  Firebase Cloud Messaging. [online] Firebase.
//Available at: https://firebase.google.com/docs/cloud-messaging/manage-tokens#retrieve-the-current-registration-token
//Date Accessed: 12 September 2025

package vcmsa.projects.careerconnect

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.json.JSONObject

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "FCMService"
        private const val CHANNEL_ID = "career_connect_notifications"
        private const val CHANNEL_NAME = "CareerConnect Notifications"
        private const val CHANNEL_DESCRIPTION = "Notifications for CareerConnect app"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "FirebaseMessagingService created")
        createNotificationChannel()
    }

    /**
     * Called when a new FCM token is generated
     * This happens when the app is restored on a new device, the user uninstalls/reinstalls the app, 
     * the user clears app data, or when the token refreshes
     */
    override fun onNewToken(token: String) {
        Log.d(TAG, "New FCM token generated: $token")
        super.onNewToken(token)
        
        // Send updated token to your backend server
        sendTokenToServer(token)
    }
    
    /**
     * Send FCM token to your backend server
     * This ensures your backend always has the current token for this user
     */
    private fun sendTokenToServer(token: String) {
        Log.d(TAG, "Sending updated FCM token to backend...")

        // Get current user from Firebase Auth
        val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Log.w(TAG, "No current user logged in, skipping FCM token registration")
            return
        }

        val userId = currentUser.uid
        Log.d(TAG, "Registering FCM token for user: $userId")

        // Use coroutine scope to make network call
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            try {
                // Get Firebase ID token asynchronously
                val idTokenResult = currentUser.getIdToken(false).await()
                val idToken = idTokenResult.token

                if (idToken == null) {
                    Log.e(TAG, "Failed to get Firebase ID token")
                    return@launch
                }

                // Get device info
                val deviceId = android.provider.Settings.Secure.getString(
                    contentResolver,
                    android.provider.Settings.Secure.ANDROID_ID
                )

                // Create FCM token registration request
                val fcmTokenRequest = mapOf(
                    "fcm_token" to token,
                    "device_id" to deviceId,
                    "device_type" to "android"
                )

                // Make API call using HttpURLConnection since we don't have retrofit instance here
                val url = java.net.URL("https://careerconnectapi2.onrender.com/api/notifications/fcm/tokens") // Production Render API
                val connection = url.openConnection() as java.net.HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty("Authorization", "Bearer $idToken")
                connection.doOutput = true

                // Write request body
                val jsonBody = JSONObject(fcmTokenRequest).toString()
                connection.outputStream.use { os ->
                    os.write(jsonBody.toByteArray(Charsets.UTF_8))
                }

                val responseCode = connection.responseCode
                if (responseCode == 200) {
                    Log.d(TAG, "FCM token registered successfully for user: $userId")
                } else {
                    Log.e(TAG, "Failed to register FCM token. Response code: $responseCode")
                    // Read error response
                    connection.errorStream?.use { inputStream ->
                        val errorResponse = inputStream.bufferedReader().use { it.readText() }
                        Log.e(TAG, "Error response: $errorResponse")
                    }
                }

                connection.disconnect()
            } catch (e: Exception) {
                Log.e(TAG, "Error registering FCM token", e)
            }
        }
    }

    /**
     * Called when a message is received while the app is in foreground
     * Note: When app is in background, Android automatically displays notifications
     * from the 'notification' payload, and onMessageReceived is NOT called.
     * However, the 'data' payload is available in the Intent when user taps the notification.
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "========== FCM MESSAGE RECEIVED ==========")
        Log.d(TAG, "Message received from: ${remoteMessage.from}")
        Log.d(TAG, "Message ID: ${remoteMessage.messageId}")
        Log.d(TAG, "Has notification payload: ${remoteMessage.notification != null}")
        Log.d(TAG, "Has data payload: ${remoteMessage.data.isNotEmpty()}")
        
        // Check if message contains a data payload
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")
            Log.d(TAG, "Notification type: ${remoteMessage.data["type"]}")
            
            // Handle different notification types based on data
            handleNotificationByType(remoteMessage)
        }

        // Check if message contains a notification payload
        remoteMessage.notification?.let { notification ->
            Log.d(TAG, "Message Notification Title: ${notification.title}")
            Log.d(TAG, "Message Notification Body: ${notification.body}")
            
            // Show notification with type-specific handling
            val notificationType = remoteMessage.data["type"] ?: "general"
            Log.d(TAG, "Displaying notification with type: $notificationType")
            showNotification(
                title = notification.title ?: "CareerConnect",
                body = notification.body ?: "New notification",
                type = notificationType,
                data = remoteMessage.data
            )
        }
        
        // If no notification payload, but has data, show a default notification
        // This ensures notifications are always displayed when app is in foreground
        if (remoteMessage.notification == null && remoteMessage.data.isNotEmpty()) {
            val title = remoteMessage.data["title"] ?: "CareerConnect"
            val body = remoteMessage.data["body"] ?: "You have a new message"
            val type = remoteMessage.data["type"] ?: "general"
            Log.d(TAG, "Displaying data-only notification with type: $type")
            showNotification(title, body, type, remoteMessage.data)
        }
        
        Log.d(TAG, "========== END FCM MESSAGE ==========")
    }
    
    /**
     * Handle different types of notifications with specific logic
     */
    private fun handleNotificationByType(remoteMessage: RemoteMessage) {
        val type = remoteMessage.data["type"]
        
        when (type) {
            "interview_invitation" -> {
                Log.d(TAG, "Handling interview invitation notification")
                val employerName = remoteMessage.data["employerName"]
                val jobTitle = remoteMessage.data["jobTitle"]
                Log.d(TAG, "Interview from $employerName for $jobTitle")
            }
            "application_status" -> {
                Log.d(TAG, "Handling application status notification")
                val status = remoteMessage.data["status"]
                Log.d(TAG, "Application status: $status")
            }
            "application_update" -> {
                Log.d(TAG, "Handling application update notification")
                val status = remoteMessage.data["status"]
                val jobTitle = remoteMessage.data["job_title"]
                val applicationId = remoteMessage.data["application_id"]
                val jobId = remoteMessage.data["job_id"]
                Log.d(TAG, "Application status update for $jobTitle (Application ID: $applicationId, Job ID: $jobId): $status")
                when (status) {
                    "under_review", "reviewed" -> Log.d(TAG, "Application moved to review stage")
                    "shortlisted" -> Log.d(TAG, "Application shortlisted!")
                    "interview_scheduled" -> Log.d(TAG, "Interview scheduled")
                    "accepted" -> Log.d(TAG, "Application accepted!")
                    "rejected" -> Log.d(TAG, "Application rejected")
                    "withdrawn" -> Log.d(TAG, "Application withdrawn")
                    "pending" -> Log.d(TAG, "Application pending")
                    else -> Log.d(TAG, "Application status updated to: $status")
                }
            }
            "new_application" -> {
                Log.d(TAG, "Handling new application notification")
                val jobTitle = remoteMessage.data["job_title"]
                val applicationId = remoteMessage.data["application_id"]
                Log.d(TAG, "New application received for job: $jobTitle (ID: $applicationId)")
            }
            "job_match" -> {
                Log.d(TAG, "Handling job match notification")
                val jobCount = remoteMessage.data["jobCount"]
                Log.d(TAG, "New job matches: $jobCount")
            }
            else -> {
                Log.d(TAG, "Handling general notification")
            }
        }
    }

    /**
     * Creates notification channel for Android 8.0 and above
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableLights(true)
                enableVibration(true)
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            
            Log.d(TAG, "Notification channel created: $CHANNEL_ID")
        }
    }

    /**
     * Shows a notification to the user with type-specific customization
     */
    private fun showNotification(title: String, body: String, type: String = "general", data: Map<String, String> = emptyMap()) {
        Log.d(TAG, "Showing notification - Title: $title, Body: $body, Type: $type")
        
        // Intent to open MainActivity when notification is tapped
        val intent = Intent(this, vcmsa.projects.careerconnect.ui.main.MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            // Add notification data to intent for deep linking
            data.forEach { (key, value) ->
                putExtra(key, value)
            }
            putExtra("notification_type", type)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Customize notification based on type
        val (icon, priority, sound) = getNotificationStyle(type)
        
        // Build the notification
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(icon)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(priority)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setDefaults(sound)
        
        // Add type-specific styling
        when (type) {
            "interview_invitation" -> {
                notificationBuilder
                    .setStyle(NotificationCompat.BigTextStyle().bigText("$body\n\nTap to view details and respond."))
                    .setColor(0xFF4CAF50.toInt()) // Green color for positive news
            }
            "application_status" -> {
                val status = data["status"]
                if (status == "accepted" || status == "interview") {
                    notificationBuilder.setColor(0xFF4CAF50.toInt()) // Green for good news
                } else {
                    notificationBuilder.setColor(0xFF2196F3.toInt()) // Blue for neutral news
                }
            }
            "application_update" -> {
                val status = data["status"]
                when (status) {
                    "accepted", "interview_scheduled" -> {
                        notificationBuilder
                            .setColor(0xFF4CAF50.toInt()) // Green for good news
                            .setStyle(NotificationCompat.BigTextStyle().bigText("$body\n\nCongratulations! Tap to view details."))
                    }
                    "shortlisted" -> {
                        notificationBuilder
                            .setColor(0xFFFF9800.toInt()) // Orange for shortlisting
                            .setStyle(NotificationCompat.BigTextStyle().bigText("$body\n\nGreat news! Next steps coming soon."))
                    }
                    "rejected" -> {
                        notificationBuilder
                            .setColor(0xFFF44336.toInt()) // Red for rejection
                            .setStyle(NotificationCompat.BigTextStyle().bigText("$body\n\nKeep applying - more opportunities await!"))
                    }
                    "under_review", "reviewed" -> {
                        notificationBuilder
                            .setColor(0xFF2196F3.toInt()) // Blue for review
                            .setStyle(NotificationCompat.BigTextStyle().bigText("$body\n\nWe'll be in touch soon with an update."))
                    }
                    "withdrawn" -> {
                        notificationBuilder
                            .setColor(0xFF9E9E9E.toInt()) // Gray for withdrawn
                            .setStyle(NotificationCompat.BigTextStyle().bigText("$body\n\nTap to view your applications."))
                    }
                    "pending" -> {
                        notificationBuilder
                            .setColor(0xFF2196F3.toInt()) // Blue for pending
                            .setStyle(NotificationCompat.BigTextStyle().bigText("$body\n\nYour application is being processed."))
                    }
                    else -> {
                        notificationBuilder
                            .setColor(0xFF2196F3.toInt()) // Blue for neutral updates
                            .setStyle(NotificationCompat.BigTextStyle().bigText("$body\n\nTap to view details."))
                    }
                }
            }
            "new_application" -> {
                notificationBuilder
                    .setColor(0xFF2196F3.toInt()) // Blue for new applications
                    .setStyle(NotificationCompat.BigTextStyle().bigText("$body\n\nTap to review the application."))
            }
            "job_match" -> {
                notificationBuilder
                    .setColor(0xFFFF9800.toInt()) // Orange for opportunities
                    .setStyle(NotificationCompat.BigTextStyle().bigText("$body\n\nTap to explore new opportunities."))
            }
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Generate unique notification ID
        val notificationId = System.currentTimeMillis().toInt()
        
        try {
            notificationManager.notify(notificationId, notificationBuilder.build())
            Log.d(TAG, "Notification displayed successfully with ID: $notificationId, Type: $type")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to show notification", e)
        }
    }
    
    /**
     * Get notification styling based on type
     */
    private fun getNotificationStyle(type: String): Triple<Int, Int, Int> {
        return when (type) {
            "interview_invitation" -> Triple(
                R.mipmap.ic_launcher, // Could be interview-specific icon
                NotificationCompat.PRIORITY_HIGH,
                NotificationCompat.DEFAULT_ALL
            )
            "application_status" -> Triple(
                R.mipmap.ic_launcher,
                NotificationCompat.PRIORITY_DEFAULT,
                NotificationCompat.DEFAULT_SOUND
            )
            "job_match" -> Triple(
                R.mipmap.ic_launcher,
                NotificationCompat.PRIORITY_DEFAULT,
                NotificationCompat.DEFAULT_SOUND
            )
            else -> Triple(
                R.mipmap.ic_launcher,
                NotificationCompat.PRIORITY_DEFAULT,
                NotificationCompat.DEFAULT_ALL
            )
        }
    }
}
