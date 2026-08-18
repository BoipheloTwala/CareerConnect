# Biometric Authentication Implementation - CareerConnect

## Testing Guide

### **Prerequisites**
- Android device with fingerprint sensor OR Android Studio emulator with fingerprint enabled
- At least one fingerprint enrolled
- Android API Level 27+

### **Important: BlueStacks Does NOT Support Biometric**

BlueStacks App Player lacks biometric hardware. Use:
- **Android Studio AVD Emulator** (with fingerprint enabled)
- **Physical Android device** with fingerprint sensor
- **NOT BlueStacks**

---

### **Test 1: Enable Biometric Authentication**

**Steps:**
1. Open the app
2. Login with email and password
3. Dialog appears: "Enable Biometric Login?"
4. Tap "Enable"
5. Verify toast: "Biometric login enabled"

**Expected Result:** Credentials encrypted and stored

---

### **Test 2: Biometric Login (Android Studio Emulator)**

**Setup Emulator First:**
1. Open Android Studio → Tools → Device Manager
2. Create/Edit emulator (Pixel 5, API 30+)
3. Enable "Fingerprint" in emulator settings
4. Start emulator
5. Settings → Security → Add fingerprint

**Test Steps:**
1. Logout from the app
2. Return to login screen
3. Verify "Use Fingerprint" button is visible
4. Tap "Use Fingerprint"
5. Biometric prompt appears
6. **Simulate fingerprint:**
   - Click fingerprint icon in emulator controls (bottom right)
   - OR: Extended Controls (⋮) → Fingerprint tab → "Touch Sensor"
7. Verify successful login

**Expected Result:** Logged in automatically

---

### **Test 3: Biometric Login (Physical Device)**

**Setup Device First:**
1. Settings → Security → Fingerprint
2. Enroll at least one fingerprint

**Test Steps:**
1. Enable USB Debugging
2. Connect device to computer
3. Run app from Android Studio
4. Login with password → Enable biometric
5. Logout
6. Tap "Use Fingerprint"
7. Place enrolled finger on sensor
8. Verify successful login

**Expected Result:** Logged in automatically

---

### **Test 4: Failed Biometric Attempt**

**Steps:**
1. On login screen, tap "Use Fingerprint"
2. Provide wrong fingerprint (non-enrolled finger)
3. Verify error message appears
4. System allows retry

**Expected Result:** Shows "Authentication failed"

---

### **Test 5: Cancel Biometric Prompt**

**Steps:**
1. Tap "Use Fingerprint"
2. Tap "Use Password" button on biometric prompt
3. Verify prompt dismisses without error
4. Can still login with password

**Expected Result:** Returns to login screen

---

### **Test 6: No Fingerprints Enrolled**

**Steps:**
1. Remove all fingerprints from device settings
2. Open app
3. Verify "Use Fingerprint" button is NOT visible
4. Can still login with password

**Expected Result:** Biometric button hidden

---

### **Test 7: BlueStacks (Expected Behavior)**

**Steps:**
1. Run app on BlueStacks
2. Login with password
3. See toast: "Biometric hardware not available on this device"
4. No biometric button appears

**Expected Result:** App works normally without biometric

---

## Troubleshooting

### **"Biometric authentication not available"**

**Possible Causes:**
- Device has no fingerprint hardware
- No fingerprints enrolled
- Running on emulator without fingerprint enabled

**Solution:**
```kotlin
// Check availability
val availability = biometricAuthManager.checkBiometricAvailability()
Log.d("Biometric", "Status: $availability")

when (availability) {
    BiometricAuthManager.BiometricAvailability.NO_HARDWARE -> 
        Log.e("Biometric", "No hardware available")
    BiometricAuthManager.BiometricAvailability.NONE_ENROLLED -> 
        Log.e("Biometric", "No fingerprints enrolled")
    else -> 
        Log.d("Biometric", "Available: ${availability}")
}
```

---

### **Biometric button not showing**

**Check these conditions:**
```kotlin
// All must be true for button to show:
// 1. Biometric available
val available = biometricAuthManager.checkBiometricAvailability() == 
    BiometricAuthManager.BiometricAvailability.AVAILABLE

// 2. Biometric enabled by user
val enabled = biometricPreferences.isBiometricEnabled()

// 3. Credentials stored
val hasCredentials = biometricPreferences.hasStoredCredentials()

Log.d("Biometric", "Available: $available, Enabled: $enabled, HasCreds: $hasCredentials")
```

**Solution:**
- Login with password once to store credentials
- Enable biometric when prompted
- Verify fingerprint is enrolled in device settings

---

### **"Please sign in with password first"**

**Cause:** No credentials stored for biometric login

**Solution:**
1. Login with email and password
2. When prompted, tap "Enable" biometric
3. Credentials will be encrypted and stored
4. Biometric button will appear on next login

---

### **EncryptedSharedPreferences errors**

**Error:** `GeneralSecurityException` or encryption fails

**Solution:**
```kotlin
// Clear encrypted preferences
val sharedPrefs = context.getSharedPreferences("biometric_encrypted_prefs", Context.MODE_PRIVATE)
sharedPrefs.edit().clear().apply()

// Re-login and enable biometric again
```

---

### **Biometric works but login fails**

**Cause:** Stored credentials may be outdated or password changed

**Solution:**
```kotlin
// Clear biometric data
biometricPreferences.clearBiometricData()

// Login with new password
// Re-enable biometric
```

---

### **Android Studio Emulator fingerprint not working**

**Solution:**

1. **Check emulator has fingerprint enabled:**
   - Device Manager → Edit → Show Advanced Settings
   - Verify "Fingerprint" is checked

2. **Enroll fingerprint in emulator:**
   - Settings → Security & Location → Fingerprint
   - Add fingerprint (any pattern works)

3. **Simulate fingerprint correctly:**
   - Use Extended Controls (⋮) → Fingerprint
   - Click "Touch Sensor" button
   - OR use fingerprint icon in emulator toolbar

---

### **"Biometric hardware not available" on physical device**

**Possible Causes:**
1. Device actually has no fingerprint sensor
2. Fingerprint sensor is damaged
3. Device doesn't support strong biometric

**Solution:**
```kotlin
// Check device capabilities
val biometricManager = BiometricManager.from(context)
val canAuthenticate = biometricManager.canAuthenticate(
    BiometricManager.Authenticators.BIOMETRIC_STRONG
)

when (canAuthenticate) {
    BiometricManager.BIOMETRIC_SUCCESS -> Log.d("Biometric", "Available")
    BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> Log.e("Biometric", "No hardware")
    BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> Log.e("Biometric", "Hardware unavailable")
    BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> Log.e("Biometric", "No fingerprints")
}
```

---

### **App crashes on biometric prompt**

**Check Logcat for errors:**
```
D/BiometricAuth: Biometric availability: AVAILABLE
D/BiometricAuth: Biometric enabled: true
```

**Common causes:**
1. FragmentActivity not used (use AppCompatActivity)
2. Calling from background thread
3. Activity destroyed before prompt shown

**Solution:**
```kotlin
// Ensure calling from UI thread
runOnUiThread {
    biometricAuthManager.showBiometricPrompt(...)
}
```

---

### **Biometric prompt appears but freezes**

**Cause:** Emulator or device fingerprint sensor stuck

**Solution:**
- **Emulator:** Restart emulator
- **Physical Device:** Restart device
- **Clear cache:** Settings → Apps → CareerConnect → Clear Cache

---

**Implementation Date:** November 15, 2025  
**Android Version Support:** API 27+  
**Status:** Production-Ready
