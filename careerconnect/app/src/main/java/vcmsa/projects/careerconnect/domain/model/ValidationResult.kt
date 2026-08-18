//CODE ATTRIBUTION
//01
//Kotlin sealed classes
//Adapted from: Kotlin Docs. (2025). Sealed classes. [online]
//Available at: https://kotlinlang.org/docs/sealed-classes.html
//Date Accessed: 30 September 2025

//02
//Kotlin data classes
//Adapted from: Kotlin Docs. (2025). Data classes. [online]
//Available at: https://kotlinlang.org/docs/data-classes.html
//Date Accessed: 30 September 2025

//03
//Kotlin extension functions
//Adapted from: Kotlin Docs. (2025). Extensions. [online]
//Available at: https://kotlinlang.org/docs/extensions.html
//Date Accessed: 30 September 2025

//04
//Kotlin when expression
//Adapted from: Kotlin Docs. (2025). Control flow: when expression. [online]
//Available at: https://kotlinlang.org/docs/control-flow.html#when-expression
//Date Accessed: 30 September 2025

//05
//Kotlin Regex API
//Adapted from: Kotlin Stdlib. (2025). Regex. [online]
//Available at: https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.text/-regex/
//Date Accessed: 30 September 2025

//06
//Android Patterns
//Adapted from: Android Developers. (2025). android.util.Patterns. [online]
//Available at: https://developer.android.com/reference/android/util/Patterns
//Date Accessed: 30 September 2025

package vcmsa.projects.careerconnect.domain.model

/**
 * Sealed class representing validation result
 */
sealed class ValidationResult {
    object Valid : ValidationResult()
    data class Invalid(val errors: List<ValidationError>) : ValidationResult()
}

/**
 * Data class representing a validation error
 */
data class ValidationError(
    val field: String,
    val message: String,
    val code: String? = null
)

/**
 * Extension function to check if validation is successful
 */
fun ValidationResult.isValid(): Boolean {
    return this is ValidationResult.Valid
}

/**
 * Extension function to get validation errors
 */
fun ValidationResult.getErrors(): List<ValidationError> {
    return when (this) {
        is ValidationResult.Valid -> emptyList()
        is ValidationResult.Invalid -> errors
    }
}

/**
 * Validation utility functions for UserProfile fields
 */
object ProfileValidator {
    
    fun validateCreateRequest(request: CreateProfileRequest): ValidationResult {
        val errors = mutableListOf<ValidationError>()
        
        // Required fields validation
        if (request.firstName.isBlank()) {
            errors.add(ValidationError("first_name", "First name is required"))
        }
        
        if (request.lastName.isBlank()) {
            errors.add(ValidationError("last_name", "Last name is required"))
        }
        
        if (request.email.isBlank()) {
            errors.add(ValidationError("email", "Email is required"))
        } else if (!isValidEmail(request.email)) {
            errors.add(ValidationError("email", "Invalid email format"))
        }
        
        // Validate company_name for recruiters
        if (request.userType == UserType.RECRUITER && request.companyName.isNullOrBlank()) {
            errors.add(ValidationError("company_name", "Company name is required for recruiters"))
        }
        
        // Optional fields validation
        request.phone?.let { phone ->
            if (phone.isNotBlank() && !isValidPhone(phone)) {
                errors.add(ValidationError("phone", "Invalid phone number format"))
            }
        }
        
        request.profileImageUrl?.let { url ->
            if (url.isNotBlank() && !isValidUrl(url)) {
                errors.add(ValidationError("profile_image_url", "Invalid profile image URL format"))
            }
        }
        
        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errors)
        }
    }
    
    fun validateUpdateRequest(request: UpdateProfileRequest): ValidationResult {
        val errors = mutableListOf<ValidationError>()
        
        // Validate optional fields if provided
        request.firstName?.let { firstName ->
            if (firstName.isBlank()) {
                errors.add(ValidationError("firstName", "First name cannot be empty"))
            }
        }
        
        request.lastName?.let { lastName ->
            if (lastName.isBlank()) {
                errors.add(ValidationError("lastName", "Last name cannot be empty"))
            }
        }
        
        request.phone?.let { phone ->
            if (phone.isNotBlank() && !isValidPhone(phone)) {
                errors.add(ValidationError("phone", "Invalid phone number format"))
            }
        }
        
        request.profileImageUrl?.let { url ->
            if (url.isNotBlank() && !isValidUrl(url)) {
                errors.add(ValidationError("profile_image_url", "Invalid profile image URL format"))
            }
        }
        
        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errors)
        }
    }
    
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    
    private fun isValidPhone(phone: String): Boolean {
        return android.util.Patterns.PHONE.matcher(phone).matches()
    }
    
    private fun isValidUrl(url: String): Boolean {
        return android.util.Patterns.WEB_URL.matcher(url).matches()
    }
    
    /**
     * Validates job search filter request
     */
    fun validateJobSearchFilter(filter: JobSearchFilter): ValidationResult {
        val errors = mutableListOf<ValidationError>()
        
        // Validate salary range
        filter.salaryMin?.let { min ->
            if (min < 0) {
                errors.add(ValidationError("salary_min", "Minimum salary cannot be negative"))
            }
        }
        
        filter.salaryMax?.let { max ->
            if (max < 0) {
                errors.add(ValidationError("salary_max", "Maximum salary cannot be negative"))
            }
        }
        
        // Validate salary range consistency
        if (filter.salaryMin != null && filter.salaryMax != null) {
            if (filter.salaryMin > filter.salaryMax) {
                errors.add(ValidationError("salary_range", "Minimum salary cannot be greater than maximum salary"))
            }
        }
        
        // Validate currency
        if (filter.currency.isNotBlank() && filter.currency.length != 3) {
            errors.add(ValidationError("currency", "Currency must be a 3-letter code (e.g., USD, EUR)"))
        }
        
        // Validate date formats
        filter.postedAfter?.let { date ->
            if (!isValidDate(date)) {
                errors.add(ValidationError("posted_after", "Invalid date format. Use ISO 8601 format (YYYY-MM-DD)"))
            }
        }
        
        filter.postedBefore?.let { date ->
            if (!isValidDate(date)) {
                errors.add(ValidationError("posted_before", "Invalid date format. Use ISO 8601 format (YYYY-MM-DD)"))
            }
        }
        
        // Validate date range consistency
        if (filter.postedAfter != null && filter.postedBefore != null) {
            if (filter.postedAfter > filter.postedBefore) {
                errors.add(ValidationError("date_range", "Posted after date cannot be later than posted before date"))
            }
        }
        
        // Validate skills list
        filter.skills?.let { skills ->
            if (skills.any { it.isBlank() }) {
                errors.add(ValidationError("skills", "Skills cannot be empty"))
            }
            if (skills.size > 20) {
                errors.add(ValidationError("skills", "Maximum 20 skills allowed"))
            }
        }
        
        // Validate query length
        filter.query?.let { query ->
            if (query.length > 200) {
                errors.add(ValidationError("query", "Search query cannot exceed 200 characters"))
            }
        }
        
        // Validate location length
        filter.location?.let { location ->
            if (location.length > 100) {
                errors.add(ValidationError("location", "Location cannot exceed 100 characters"))
            }
        }
        
        // Validate company name length
        filter.companyName?.let { company ->
            if (company.length > 100) {
                errors.add(ValidationError("company_name", "Company name cannot exceed 100 characters"))
            }
        }
        
        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errors)
        }
    }
    
    /**
     * Validates job search request
     */
    fun validateJobSearchRequest(request: JobSearchRequest): ValidationResult {
        val errors = mutableListOf<ValidationError>()
        
        // Validate pagination
        if (request.page < 1) {
            errors.add(ValidationError("page", "Page number must be greater than 0"))
        }
        
        if (request.limit < 1 || request.limit > 100) {
            errors.add(ValidationError("limit", "Limit must be between 1 and 100"))
        }
        
        // Validate filters
        val filterValidation = validateJobSearchFilter(request.filters)
        if (!filterValidation.isValid()) {
            errors.addAll(filterValidation.getErrors())
        }
        
        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errors)
        }
    }
    
    /**
     * Validates job application request
     */
    fun validateJobApplicationRequest(request: JobApplicationRequest): ValidationResult {
        val errors = mutableListOf<ValidationError>()
        
        // Validate job ID
        if (request.jobId.isBlank()) {
            errors.add(ValidationError("job_id", "Job ID is required"))
        }
        
        // Validate cover letter length
        request.coverLetter?.let { letter ->
            if (letter.length > 2000) {
                errors.add(ValidationError("cover_letter", "Cover letter cannot exceed 2000 characters"))
            }
        }
        
        // Validate resume URL
        request.resumeUrl?.let { url ->
            if (!isValidUrl(url)) {
                errors.add(ValidationError("resume_url", "Invalid resume URL format"))
            }
        }
        
        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errors)
        }
    }
    
    private fun isValidDate(dateString: String): Boolean {
        return try {
            // Simple ISO 8601 date validation (YYYY-MM-DD)
            val regex = Regex("^\\d{4}-\\d{2}-\\d{2}$")
            regex.matches(dateString)
        } catch (e: Exception) {
            false
        }
    }
    
    // ===== CV VALIDATION METHODS =====
    
    /**
     * Validates CV upload request
     */
    fun validateCVUploadRequest(request: CVUploadRequest): ValidationResult {
        val errors = mutableListOf<ValidationError>()
        
        // Validate title
        if (request.title.isBlank()) {
            errors.add(ValidationError("title", "CV title is required"))
        } else if (request.title.length > 100) {
            errors.add(ValidationError("title", "CV title cannot exceed 100 characters"))
        }
        
        // Validate description
        request.description?.let { description ->
            if (description.length > 500) {
                errors.add(ValidationError("description", "CV description cannot exceed 500 characters"))
            }
        }
        
        // Validate file size
        if (request.fileSize <= 0) {
            errors.add(ValidationError("file_size", "File size must be greater than 0"))
        } else if (request.fileSize > 10 * 1024 * 1024) { // 10MB limit
            errors.add(ValidationError("file_size", "File size cannot exceed 10MB"))
        }
        
        // Validate file URL
        if (request.fileUrl.isBlank()) {
            errors.add(ValidationError("file_url", "File URL is required"))
        } else if (!isValidUrl(request.fileUrl)) {
            errors.add(ValidationError("file_url", "Invalid file URL format"))
        }
        
        // Validate thumbnail URL if provided
        request.thumbnailUrl?.let { thumbnailUrl ->
            if (!isValidUrl(thumbnailUrl)) {
                errors.add(ValidationError("thumbnail_url", "Invalid thumbnail URL format"))
            }
        }
        
        // Validate tags
        if (request.tags.size > 10) {
            errors.add(ValidationError("tags", "Maximum 10 tags allowed"))
        }
        
        if (request.tags.any { it.isBlank() }) {
            errors.add(ValidationError("tags", "Tags cannot be empty"))
        }
        
        if (request.tags.any { it.length > 50 }) {
            errors.add(ValidationError("tags", "Each tag cannot exceed 50 characters"))
        }
        
        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errors)
        }
    }
    
    /**
     * Validates CV update request
     */
    fun validateCVUpdateRequest(request: CVUpdateRequest): ValidationResult {
        val errors = mutableListOf<ValidationError>()
        
        // Validate title if provided
        request.title?.let { title ->
            if (title.isBlank()) {
                errors.add(ValidationError("title", "CV title cannot be empty"))
            } else if (title.length > 100) {
                errors.add(ValidationError("title", "CV title cannot exceed 100 characters"))
            } else {
                // no-op: valid title
            }
        }
        
        // Validate description if provided
        request.description?.let { description ->
            if (description.length > 500) {
                errors.add(ValidationError("description", "CV description cannot exceed 500 characters"))
            }
        }
        
        // Validate tags if provided
        request.tags?.let { tags ->
            if (tags.size > 10) {
                errors.add(ValidationError("tags", "Maximum 10 tags allowed"))
            }
            
            if (tags.any { it.isBlank() }) {
                errors.add(ValidationError("tags", "Tags cannot be empty"))
            }
            
            if (tags.any { it.length > 50 }) {
                errors.add(ValidationError("tags", "Each tag cannot exceed 50 characters"))
            }
        }
        
        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errors)
        }
    }
    
    /**
     * Validates CV search request
     */
    fun validateCVSearchRequest(request: CVSearchRequest): ValidationResult {
        val errors = mutableListOf<ValidationError>()
        
        // Validate query length
        request.query?.let { query ->
            if (query.length > 200) {
                errors.add(ValidationError("query", "Search query cannot exceed 200 characters"))
            }
        }
        
        // Validate date formats
        request.createdAfter?.let { date ->
            if (!isValidDate(date)) {
                errors.add(ValidationError("created_after", "Invalid date format. Use ISO 8601 format (YYYY-MM-DD)"))
            }
        }
        
        request.createdBefore?.let { date ->
            if (!isValidDate(date)) {
                errors.add(ValidationError("created_before", "Invalid date format. Use ISO 8601 format (YYYY-MM-DD)"))
            }
        }
        
        // Validate date range consistency
        if (request.createdAfter != null && request.createdBefore != null) {
            if (request.createdAfter > request.createdBefore) {
                errors.add(ValidationError("date_range", "Created after date cannot be later than created before date"))
            }
        }
        
        // Validate tags
        request.tags?.let { tags ->
            if (tags.size > 10) {
                errors.add(ValidationError("tags", "Maximum 10 tags allowed for search"))
            }
            
            if (tags.any { it.isBlank() }) {
                errors.add(ValidationError("tags", "Search tags cannot be empty"))
            }
        }
        
        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errors)
        }
    }
    
    /**
     * Validates CV share request
     */
    fun validateCVShareRequest(request: CVShareRequest): ValidationResult {
        val errors = mutableListOf<ValidationError>()
        
        // Validate CV ID
        if (request.cvId.isBlank()) {
            errors.add(ValidationError("cv_id", "CV ID is required"))
        }
        
        // Validate recipient email
        if (request.recipientEmail.isBlank()) {
            errors.add(ValidationError("recipient_email", "Recipient email is required"))
        } else if (!isValidEmail(request.recipientEmail)) {
            errors.add(ValidationError("recipient_email", "Invalid email format"))
        }
        
        // Validate message length
        request.message?.let { message ->
            if (message.length > 1000) {
                errors.add(ValidationError("message", "Message cannot exceed 1000 characters"))
            }
        }
        
        // Validate expiration date
        request.expiresAt?.let { date ->
            if (!isValidDateTime(date)) {
                errors.add(ValidationError("expires_at", "Invalid date format. Use ISO 8601 format (YYYY-MM-DDTHH:mm:ssZ)"))
            }
        }
        
        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errors)
        }
    }
    
    /**
     * Validates CV download request
     */
    fun validateCVDownloadRequest(request: CVDownloadRequest): ValidationResult {
        val errors = mutableListOf<ValidationError>()
        
        // Validate CV ID
        if (request.cvId.isBlank()) {
            errors.add(ValidationError("cv_id", "CV ID is required"))
        }
        
        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(errors)
        }
    }
    
    /**
     * Validates file type for CV upload
     */
    fun validateCVFileType(fileName: String, fileType: CVFileType): ValidationResult {
        val errors = mutableListOf<ValidationError>()
        
        // Get file extension
        val extension = fileName.substringAfterLast('.', "").lowercase()
        
        // Validate file extension matches file type
        val expectedExtensions = when (fileType) {
            CVFileType.PDF -> listOf("pdf")
            CVFileType.DOC -> listOf("doc")
            CVFileType.DOCX -> listOf("docx")
            CVFileType.TXT -> listOf("txt")
        }
        
        if (extension !in expectedExtensions) {
            errors.add(ValidationError("file_type", "File extension does not match the specified file type"))
        }
        
        // Validate file name length
        if (fileName.length > 255) {
            errors.add(ValidationError("file_name", "File name cannot exceed 255 characters"))
        }
        
        // Validate file name characters
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
    
    private fun isValidDateTime(dateTimeString: String): Boolean {
        return try {
            // Simple ISO 8601 datetime validation (YYYY-MM-DDTHH:mm:ssZ)
            val regex = Regex("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z?$")
            regex.matches(dateTimeString)
        } catch (e: Exception) {
            false
        }
    }
}