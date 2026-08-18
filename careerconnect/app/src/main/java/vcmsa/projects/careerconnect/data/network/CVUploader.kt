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
import vcmsa.projects.careerconnect.domain.model.CVFileType
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * Utility class for uploading CV files to Cloudinary
 * Extends the existing CloudinaryUploader with CV-specific functionality
 */
object CVUploader {
    
    private const val CLOUD_NAME = "dk5xkbwot"
    private const val UPLOAD_PRESET = "cv-upload-preset"
    private const val FOLDER = "CareerConnect/cvs"
    private const val BOUNDARY_PREFIX = "----WebKitFormBoundary"
    
    /**
     * Upload CV file to Cloudinary
     */
    suspend fun uploadCVFile(
        context: Context, 
        uri: Uri, 
        fileName: String,
        fileType: CVFileType
    ): Result<CVUploadResult> = withContext(Dispatchers.IO) {
        try {
            val tempFile = createTempFile(context, uri, fileName)
            val boundary = "$BOUNDARY_PREFIX${System.currentTimeMillis()}"
            // Use raw upload endpoint for non-image files like PDFs
            val uploadUrl = URL("https://api.cloudinary.com/v1_1/$CLOUD_NAME/raw/upload")
            
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

                // Upload file
                writer.append("--$boundary\r\n")
                writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"$fileName\"\r\n")
                writer.append("Content-Type: ${getMimeType(fileType)}\r\n\r\n")
                writer.flush()
                
                FileInputStream(tempFile).use { it.copyTo(os) }
                writer.append("\r\n")

                // Upload parameters
                writePart("Content-Disposition: form-data; name=\"upload_preset\"", UPLOAD_PRESET)
                writePart("Content-Disposition: form-data; name=\"folder\"", FOLDER)

                writer.append("--$boundary--\r\n").flush()
            }

            val code = conn.responseCode
            val body = if (code == HttpURLConnection.HTTP_OK) {
                conn.inputStream.bufferedReader().use { it.readText() }
            } else {
                conn.errorStream.bufferedReader().use { it.readText() }
            }
            conn.disconnect()
            tempFile.delete()

            if (code == HttpURLConnection.HTTP_OK) {
                val fileUrl = extractFileUrl(body)
                val thumbnailUrl = extractThumbnailUrl(body)
                val fileSize = extractFileSize(body)
                
                if (fileUrl != null) {
                    Result.success(CVUploadResult(
                        fileUrl = fileUrl,
                        thumbnailUrl = thumbnailUrl,
                        fileSize = fileSize ?: tempFile.length(),
                        publicId = extractPublicId(body)
                    ))
                } else {
                    Result.failure(Exception("Failed to extract file URL from response"))
                }
            } else {
                Result.failure(Exception("Cloudinary upload failed: $code $body"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Generate PDF thumbnail for CV preview
     */
    suspend fun generatePDFThumbnail(
        context: Context,
        pdfUri: Uri
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            // This would typically use a PDF rendering library
            // For now, we'll return a placeholder
            // In a real implementation, you might use libraries like:
            // - AndroidPdfViewer
            // - PdfRenderer (Android API 21+)
            // - Or a server-side thumbnail generation service
            
            Result.success("https://via.placeholder.com/300x400/cccccc/666666?text=PDF+Preview")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Validate CV file before upload
     */
    fun validateCVFile(uri: Uri, fileName: String, fileType: CVFileType, maxSizeBytes: Long = 10 * 1024 * 1024): ValidationResult {
        val errors = mutableListOf<ValidationError>()
        
        // Check file extension
        val extension = fileName.substringAfterLast('.', "").lowercase()
        val expectedExtensions = when (fileType) {
            CVFileType.PDF -> listOf("pdf")
            CVFileType.DOC -> listOf("doc")
            CVFileType.DOCX -> listOf("docx")
            CVFileType.TXT -> listOf("txt")
        }
        
        if (extension !in expectedExtensions) {
            errors.add(ValidationError("file_type", "File extension does not match the specified file type"))
        }
        
        // Check file name length
        if (fileName.length > 255) {
            errors.add(ValidationError("file_name", "File name cannot exceed 255 characters"))
        }
        
        // Check file name characters
        val invalidChars = Regex("[<>:\"/\\\\|?*]")
        if (invalidChars.containsMatchIn(fileName)) {
            errors.add(ValidationError("file_name", "File name contains invalid characters"))
        }
        
        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errors)
        }
    }
    
    /**
     * Get file size from URI
     */
    suspend fun getFileSize(context: Context, uri: Uri): Long = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.available().toLong()
            } ?: 0L
        } catch (e: Exception) {
            0L
        }
    }
    
    private fun createTempFile(context: Context, uri: Uri, fileName: String): File {
        val input = context.contentResolver.openInputStream(uri)
        val file = File(context.cacheDir, "cv_${System.currentTimeMillis()}_$fileName")
        file.outputStream().use { output ->
            input?.copyTo(output)
        }
        return file
    }
    
    private fun getMimeType(fileType: CVFileType): String {
        return when (fileType) {
            CVFileType.PDF -> "application/pdf"
            CVFileType.DOC -> "application/msword"
            CVFileType.DOCX -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            CVFileType.TXT -> "text/plain"
        }
    }
    
    private fun extractFileUrl(responseBody: String): String? {
        val regex = Regex("\\\"secure_url\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"")
        return regex.find(responseBody)?.groupValues?.get(1)
            ?: Regex("\\\"url\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"").find(responseBody)?.groupValues?.get(1)
    }
    
    private fun extractThumbnailUrl(responseBody: String): String? {
        // For PDFs, we might generate a thumbnail
        // For now, return null as we don't have thumbnail generation implemented
        return null
    }
    
    private fun extractFileSize(responseBody: String): Long? {
        val regex = Regex("\\\"bytes\\\"\\s*:\\s*(\\d+)")
        return regex.find(responseBody)?.groupValues?.get(1)?.toLongOrNull()
    }
    
    private fun extractPublicId(responseBody: String): String? {
        val regex = Regex("\\\"public_id\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"")
        return regex.find(responseBody)?.groupValues?.get(1)
    }
}

/**
 * Data class for CV upload result
 */
data class CVUploadResult(
    val fileUrl: String,
    val thumbnailUrl: String? = null,
    val fileSize: Long,
    val publicId: String? = null
)

/**
 * Validation result for CV file validation
 */
sealed class ValidationResult {
    object Valid : ValidationResult()
    data class Invalid(val errors: List<ValidationError>) : ValidationResult()
}

/**
 * Data class for validation error
 */
data class ValidationError(
    val field: String,
    val message: String
)
