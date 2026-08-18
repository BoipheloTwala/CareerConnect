//CODE ATTRIBUTION
//01
//BiometricPrompt
//Adapted from: Android Developers. (2025). BiometricPrompt. [online] Android Developers.
//Available at: https://developer.android.com/reference/androidx/biometric/BiometricPrompt
//Date Accessed: 15 November 2025

//02
//BiometricManager
//Adapted from: Android Developers. (2025). BiometricManager. [online] Android Developers.
//Available at: https://developer.android.com/reference/androidx/biometric/BiometricManager
//Date Accessed: 15 November 2025

package vcmsa.projects.careerconnect.utils

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/**
 * Manager class for handling biometric authentication operations
 */
class BiometricAuthManager(private val activity: FragmentActivity) {

    private val biometricManager = BiometricManager.from(activity)

    /**
     * Check if biometric authentication is available on the device
     * @return BiometricAvailability status
     */
    fun checkBiometricAvailability(): BiometricAvailability {
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricAvailability.AVAILABLE
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricAvailability.NO_HARDWARE
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> BiometricAvailability.HARDWARE_UNAVAILABLE
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricAvailability.NONE_ENROLLED
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> BiometricAvailability.SECURITY_UPDATE_REQUIRED
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> BiometricAvailability.UNSUPPORTED
            BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> BiometricAvailability.UNKNOWN
            else -> BiometricAvailability.UNKNOWN
        }
    }

    /**
     * Show biometric prompt for authentication
     * @param title Title for the biometric prompt
     * @param subtitle Subtitle for the biometric prompt
     * @param negativeButtonText Text for the negative button
     * @param onSuccess Callback when authentication is successful
     * @param onError Callback when authentication fails with error message
     * @param onFailed Callback when authentication fails (e.g., wrong fingerprint)
     */
    fun showBiometricPrompt(
        title: String,
        subtitle: String,
        negativeButtonText: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        onFailed: () -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)

        val biometricPrompt = BiometricPrompt(activity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    // Don't show error if user cancelled
                    if (errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON &&
                        errorCode != BiometricPrompt.ERROR_USER_CANCELED) {
                        onError(errString.toString())
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    onFailed()
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText(negativeButtonText)
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    /**
     * Get user-friendly message for biometric availability status
     */
    fun getAvailabilityMessage(availability: BiometricAvailability): String {
        return when (availability) {
            BiometricAvailability.AVAILABLE -> "Biometric authentication is available"
            BiometricAvailability.NO_HARDWARE -> "No biometric hardware available on this device"
            BiometricAvailability.HARDWARE_UNAVAILABLE -> "Biometric hardware is currently unavailable"
            BiometricAvailability.NONE_ENROLLED -> "No biometric credentials enrolled. Please set up fingerprint in device settings"
            BiometricAvailability.SECURITY_UPDATE_REQUIRED -> "Security update required for biometric authentication"
            BiometricAvailability.UNSUPPORTED -> "Biometric authentication is not supported"
            BiometricAvailability.UNKNOWN -> "Biometric authentication status unknown"
        }
    }

    /**
     * Enum representing biometric availability states
     */
    enum class BiometricAvailability {
        AVAILABLE,
        NO_HARDWARE,
        HARDWARE_UNAVAILABLE,
        NONE_ENROLLED,
        SECURITY_UPDATE_REQUIRED,
        UNSUPPORTED,
        UNKNOWN
    }
}

