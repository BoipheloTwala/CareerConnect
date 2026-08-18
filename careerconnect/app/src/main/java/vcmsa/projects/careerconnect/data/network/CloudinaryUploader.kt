//CODE ATTRIBUTION
//01
//Context
//Adapted from: Android Developers. (2025). Context. [online] Android Developers.
//Available at: https://developer.android.com/reference/android/content/Context
//Date Accessed: 01 October 2025

//02
//Uri
//Adapted from: Android Developers. (2025). Uri. [online] Android Developers.
//Available at: https://developer.android.com/reference/android/net/Uri
//Date Accessed: 01 October 2025

//03
//HttpURLConnection
//Adapted from: Oracle Docs. (2025). HttpURLConnection. [online]
//Available at: https://docs.oracle.com/javase/8/docs/api/java/net/HttpURLConnection.html
//Date Accessed: 01 October 2025

//04
//Cloudinary upload API (Raw upload)
//Adapted from: Cloudinary Docs. (2025). Image upload API reference. [online]
//Available at: https://cloudinary.com/documentation/image_upload_api_reference#upload_required_parameters
//Date Accessed: 01 October 2025

package vcmsa.projects.careerconnect.data.network

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

object CloudinaryUploader {

    private const val CLOUD_NAME = "dk5xkbwot"
    private const val UPLOAD_PRESET = "cv-upload-preset"
    private const val FOLDER = "CareerConnect/profile_images"
    private const val BOUNDARY_PREFIX = "----WebKitFormBoundary"

    suspend fun uploadImage(context: Context, uri: Uri): Result<String> = withContext(Dispatchers.IO) {
        try {
            val temp = createTempFile(context, uri)
            val boundary = "$BOUNDARY_PREFIX${System.currentTimeMillis()}"
            val uploadUrl = URL("https://api.cloudinary.com/v1_1/$CLOUD_NAME/image/upload")
            val conn = (uploadUrl.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
                doOutput = true
                doInput = true
            }

            conn.outputStream.use { os ->
                val writer = os.bufferedWriter()

                fun writePart(header: String, content: String) {
                    writer.apply {
                        append("--$boundary\r\n")
                        append(header)
                        append("\r\n\r\n")
                        append(content)
                        append("\r\n")
                        flush()
                    }
                }

                // file
                writer.append("--$boundary\r\n")
                writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"image.jpg\"\r\n")
                writer.append("Content-Type: image/jpeg\r\n\r\n")
                writer.flush()
                FileInputStream(temp).use { it.copyTo(os) }
                writer.append("\r\n")

                writePart("Content-Disposition: form-data; name=\"upload_preset\"", UPLOAD_PRESET)
                writePart("Content-Disposition: form-data; name=\"folder\"", FOLDER)

                writer.append("--$boundary--\r\n").flush()
            }

            val code = conn.responseCode
            val body = if (code == HttpURLConnection.HTTP_OK) {
                BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }
            } else {
                BufferedReader(InputStreamReader(conn.errorStream)).use { it.readText() }
            }
            conn.disconnect()
            temp.delete()

            val imageUrl = Regex("\\\"secure_url\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"").find(body)?.groupValues?.get(1)
                ?: Regex("\\\"url\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"").find(body)?.groupValues?.get(1)
            if (code == HttpURLConnection.HTTP_OK && !imageUrl.isNullOrBlank()) {
                Result.success(imageUrl)
            } else {
                Result.failure(Exception("Cloudinary error: $code $body"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun createTempFile(context: Context, uri: Uri): File {
        val input = context.contentResolver.openInputStream(uri)
        val file = File(context.cacheDir, "profile_image_${System.currentTimeMillis()}.jpg")
        file.outputStream().use { output ->
            input?.copyTo(output)
        }
        return file
    }
}

