//CODE ATTRIBUTION
//01
//OkHttp for HTTP requests
//Adapted from: Square. (2025). OkHttp. [online]
//Available at: https://square.github.io/okhttp/
//Date Accessed: 20 October 2025

//02
//Android File Operations
//Adapted from: Android Developers. (2025). FileProvider. [online] Android Developers.
//Available at: https://developer.android.com/reference/androidx/core/content/FileProvider
//Date Accessed: 20 October 2025

//03
//Firebase Authentication
//Adapted from: Firebase. (2025). FirebaseAuth. [online] Firebase.
//Available at: https://firebase.google.com/docs/reference/android/com/google/firebase/auth/FirebaseAuth
//Date Accessed: 20 October 2025

package vcmsa.projects.careerconnect.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object FileDownloader {
    
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    
    private val client = OkHttpClient.Builder()
        .addInterceptor(createAuthInterceptor())
        .build()
    
    private val plainClient = OkHttpClient.Builder()
        .build()
    
    /**
     * Creates authentication interceptor for Firebase ID tokens
     */
    private fun createAuthInterceptor(): Interceptor {
        return Interceptor { chain ->
            val originalRequest = chain.request()
            
            // Get the current user's ID token
            val user = firebaseAuth.currentUser
            if (user != null) {
                try {
                    // Get the ID token synchronously using runBlocking
                    val token = runBlocking { user.getIdToken(false).await().token }
                    
                    val authenticatedRequest = originalRequest.newBuilder()
                        .header("Authorization", "Bearer $token")
                        .build()
                    
                    chain.proceed(authenticatedRequest)
                } catch (e: Exception) {
                    // If token retrieval fails, proceed without authentication
                    chain.proceed(originalRequest)
                }
            } else {
                // No authenticated user, proceed without auth header
                chain.proceed(originalRequest)
            }
        }
    }
    
    suspend fun downloadAndOpenFile(
        context: Context,
        url: String,
        fileName: String,
        onProgress: (progress: Int) -> Unit = {}
    ): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            android.util.Log.d("FileDownloader", "downloadAndOpenFile called with URL: $url")
            android.util.Log.d("FileDownloader", "File name: $fileName")
            
            android.util.Log.d("FileDownloader", "Making HTTP request to: $url")
            
            // Try Cloudinary URL with plain client first (no auth headers)
            val clientToUse = if (url.contains("res.cloudinary.com")) {
                android.util.Log.d("FileDownloader", "Using plain client for Cloudinary URL")
                plainClient
            } else {
                android.util.Log.d("FileDownloader", "Using authenticated client for other URLs")
                client
            }
            
            val request = Request.Builder()
                .url(url)
                .build()
            
            val response = clientToUse.newCall(request).execute()
            android.util.Log.d("FileDownloader", "Response code: ${response.code}")
            android.util.Log.d("FileDownloader", "Response headers: ${response.headers}")
            
            if (!response.isSuccessful) {
                val errorBody = response.body?.string()
                android.util.Log.e("FileDownloader", "Failed to download file. Code: ${response.code}, Body: $errorBody")
                return@withContext Result.failure(IOException("Failed to download file: ${response.code}"))
            }
            
            val body = response.body ?: return@withContext Result.failure(IOException("Response body is null"))
            val contentLength = body.contentLength()
            
            // Create downloads directory
            val downloadsDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "CareerConnect")
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs()
            }
            
            // Create file
            val file = File(downloadsDir, fileName)
            val outputStream = FileOutputStream(file)
            
            val inputStream = body.byteStream()
            val buffer = ByteArray(8192)
            var bytesRead: Int
            var totalBytesRead = 0L
            
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
                totalBytesRead += bytesRead
                
                if (contentLength > 0) {
                    val progress = ((totalBytesRead * 100) / contentLength).toInt()
                    onProgress(progress)
                }
            }
            
            inputStream.close()
            outputStream.close()
            
            // Get URI for the file
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            
            android.util.Log.d("FileDownloader", "File downloaded successfully. URI: $uri")
            Result.success(uri)
            
        } catch (e: Exception) {
            android.util.Log.e("FileDownloader", "Exception during download: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    /**
     * Download CV file using backend API endpoint
     */
    suspend fun downloadCVFromBackend(
        context: Context,
        cvId: String,
        fileName: String,
        onProgress: (progress: Int) -> Unit = {}
    ): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            // Get CV download info from backend
            val cvInfoUrl = "https://careerconnectapi2.onrender.com/api/cvs/$cvId/file"
            android.util.Log.d("FileDownloader", "Getting CV download info from backend: $cvInfoUrl")
            
            val infoRequest = Request.Builder()
                .url(cvInfoUrl)
                .build()
            
            val infoResponse = client.newCall(infoRequest).execute()
            android.util.Log.d("FileDownloader", "Backend CV info response code: ${infoResponse.code}")
            
            if (!infoResponse.isSuccessful) {
                val errorBody = infoResponse.body?.string()
                android.util.Log.e("FileDownloader", "Failed to get CV info from backend. Code: ${infoResponse.code}, Body: $errorBody")
                return@withContext Result.failure(IOException("Failed to get CV info: ${infoResponse.code}"))
            }
            
            val infoBody = infoResponse.body?.string()
            android.util.Log.d("FileDownloader", "CV info response: $infoBody")
            
            if (infoBody.isNullOrBlank()) {
                return@withContext Result.failure(IOException("Empty response from CV info endpoint"))
            }
            
            // Parse the JSON response to get download_url
            val jsonObject = JSONObject(infoBody)
            val downloadUrl = jsonObject.optString("download_url")
            val actualFileName = jsonObject.optString("file_name") ?: fileName
            
            android.util.Log.d("FileDownloader", "Extracted download URL: $downloadUrl")
            android.util.Log.d("FileDownloader", "Actual file name: $actualFileName")
            
            if (downloadUrl.isBlank()) {
                return@withContext Result.failure(IOException("No download URL found in CV info"))
            }
            
            // Now download the file from the Cloudinary URL using plain client
            android.util.Log.d("FileDownloader", "Downloading file from Cloudinary URL")
            val downloadRequest = Request.Builder()
                .url(downloadUrl)
                .build()
            
            val downloadResponse = plainClient.newCall(downloadRequest).execute()
            android.util.Log.d("FileDownloader", "Download response code: ${downloadResponse.code}")
            
            if (!downloadResponse.isSuccessful) {
                val errorBody = downloadResponse.body?.string()
                android.util.Log.e("FileDownloader", "Failed to download CV file. Code: ${downloadResponse.code}, Body: $errorBody")
                return@withContext Result.failure(IOException("Failed to download CV file: ${downloadResponse.code}"))
            }
            
            val body = downloadResponse.body ?: return@withContext Result.failure(IOException("Response body is null"))
            val contentLength = body.contentLength()
            
            // Create downloads directory
            val downloadsDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "CareerConnect")
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs()
            }
            
            // Create file
            val file = File(downloadsDir, actualFileName)
            val outputStream = FileOutputStream(file)
            
            val inputStream = body.byteStream()
            val buffer = ByteArray(8192)
            var bytesRead: Int
            var totalBytesRead = 0L
            
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
                totalBytesRead += bytesRead
                
                if (contentLength > 0) {
                    val progress = ((totalBytesRead * 100) / contentLength).toInt()
                    onProgress(progress)
                }
            }
            
            inputStream.close()
            outputStream.close()
            
            // Get URI for the file
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            
            android.util.Log.d("FileDownloader", "CV file downloaded successfully. URI: $uri")
            Result.success(uri)
            
        } catch (e: Exception) {
            android.util.Log.e("FileDownloader", "Exception during backend CV download: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    fun openFile(context: Context, uri: Uri, mimeType: String = "application/pdf") {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            } else {
                Toast.makeText(context, "No app available to open this file", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error opening file: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    fun downloadAndOpenPdf(context: Context, url: String, fileName: String) {
        // This would be called from a coroutine scope in the activity
        // Implementation will be in the activity
    }
}
