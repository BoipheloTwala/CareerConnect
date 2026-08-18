
# CareerConnect - Android Job Search & Recruitment Platform

##  Overview

**CareerConnect** is a comprehensive Android application built with Kotlin that bridges the gap between job seekers and recruiters. The app provides a seamless experience for browsing jobs, applying for positions, managing applications, and posting job openings.

### Key Highlights

-  **Secure Authentication** with Email/Password & Google Sign-In
-  **Offline-First Architecture** - Full functionality without internet
-  **Customizable UI** - Dark/Light themes & adjustable font sizes
-  **Biometric Login** - Fingerprint authentication support
-  **Multi-language Support** - English, Afrikaans, Zulu
-  **Real-time Notifications** - Firebase Cloud Messaging integration
-  **Local Caching** - Room database for offline persistence
-  **Background Sync** - WorkManager for automatic data synchronization

---

##  Features

### For Job Seekers

- **Job Browsing**
  - Browse all available job postings
  - Advanced search and filtering
  - View detailed job descriptions, requirements, and benefits
  - Filter by industry, location, job type, and experience level

- **Job Applications**
  - Apply for jobs with cover letter and CV upload
  - Track application status (Pending, Reviewing, Accepted, Rejected)
  - Save application drafts for offline completion
  - Automatic sync when connection restored

- **Bookmarks**
  - Save interesting job postings for later
  - Works completely offline
  - Quick access to saved jobs
  - Sync bookmarks across devices

- **Profile Management**
  - Complete professional profile
  - Upload CV and portfolio documents
  - Edit personal and professional information
  - Profile completion tracking

### For Recruiters

- **Job Posting**
  - Create and publish job openings
  - Specify requirements, benefits, and job details
  - Edit or remove posted jobs
  - Track job posting performance

- **Application Management**
  - View all applications for posted jobs
  - Review candidate CVs and profiles
  - Update application status
  - Download and share CVs
  - Filter applications by status

### Authentication & Security

- **Multiple Login Methods**
  - Email/Password authentication
  - Google Sign-In integration
  - Biometric login (fingerprint)
  - Secure credential storage with EncryptedSharedPreferences

- **Password Management**
  - Forgot password recovery
  - Change password from settings
  - Secure password validation

### User Experience

- **Theme Customization**
  - Light Mode
  - Dark Mode
  - System Default (follows device theme)
  - Instant theme switching

- **Font Size Adjustment**
  - Small (0.85x)
  - Medium (1.0x - default)
  - Large (1.15x)
  - App-wide font scaling

- **Localization**
  - English (default)
  - Afrikaans
  - Zulu
  - Easy language switching from settings

### Notifications

- **Push Notifications**
  - New job matches
  - Application status updates
  - Messages from recruiters
  - Android 13+ runtime permission handling

### Offline Capabilities

- **Full Offline Support**
  - View bookmarked jobs without internet
  - Bookmark jobs while offline
  - Create application drafts offline
  - Edit profile information offline
  - Automatic background sync
  - Conflict resolution strategies

---

##  Tech Stack

### Core Technologies

| Technology | Version | Purpose |
|------------|---------|---------|
| **Kotlin** | 2.0.21 | Primary programming language |
| **Android Gradle Plugin** | 8.9.2 | Build system |
| **Min SDK** | 27 (Android 8.1) | Minimum supported version |
| **Target SDK** | 36 | Latest Android features |
| **Compile SDK** | 36 | Compilation target |

### Architecture Components

- **AndroidX Core KTX** (1.17.0) - Kotlin extensions
- **AppCompat** (1.7.1) - Backward compatibility
- **Material Components** (1.13.0) - Material Design UI
- **ConstraintLayout** (2.2.1) - Flexible layouts
- **DrawerLayout** (1.2.0) - Navigation drawer
- **GridLayout** (1.0.0) - Grid-based layouts

### Firebase Services

```kotlin
implementation(platform("com.google.firebase:firebase-bom:33.3.0"))
```

- **Firebase Authentication** - User identity management
- **Firebase Cloud Messaging** - Push notifications
- **Firebase Storage** - File uploads (CVs, documents)
- **Firebase App Check** - Security & abuse prevention
- **Firebase Analytics** - User behavior tracking

### Networking

- **Retrofit** (2.11.0) - REST API client
- **OkHttp** (4.12.0) - HTTP client
- **Gson Converter** - JSON serialization
- **Logging Interceptor** - Network debugging

### Database & Storage

- **Room Database** (2.6.1) - Local SQLite abstraction
- **Room KTX** - Kotlin extensions & coroutine support
- **EncryptedSharedPreferences** (1.1.0-alpha06) - Secure storage

### Authentication

- **Google Play Services Auth** (21.2.0) - Google Sign-In
- **AndroidX Credentials** (1.5.0) - Credential Manager API
- **Google ID** (1.1.1) - Identity services
- **Biometric** (1.2.0-alpha05) - Fingerprint authentication

### Background Processing

- **WorkManager** (2.9.0) - Background job scheduling
- **Kotlin Coroutines** (1.7.3) - Asynchronous programming

### Lifecycle & ViewModel

- **Lifecycle LiveData KTX** (2.7.0) - Reactive data observation
- **Lifecycle ViewModel KTX** (2.7.0) - UI state management

### Testing

- **JUnit** (4.13.2) - Unit testing
- **AndroidX JUnit** (1.3.0) - Android unit tests
- **Espresso** (3.7.0) - UI testing

---

##  Architecture

### Key Architectural Patterns

1. **Repository Pattern**
   - Single source of truth for data
   - Abstraction over data sources
   - Offline-first implementation

2. **MVVM (Model-View-ViewModel)**
   - Clear separation of concerns
   - Reactive UI updates with LiveData/Flow
   - Lifecycle-aware components

3. **Offline-First**
   - Local database as primary data source
   - Background synchronization
   - Conflict resolution strategies

4. **Dependency Injection**
   - Manual DI with factory patterns
   - Centralized dependency management

---

##  Project Structure

```
careerconnect/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/vcmsa/projects/careerconnect/
│   │   │   │   ├── CareerConnectApplication.kt        # Application class
│   │   │   │   ├── data/                              # Data layer
│   │   │   │   │   ├── local/                         # Local database
│   │   │   │   │   │   ├── AppDatabase.kt
│   │   │   │   │   │   ├── dao/                       # Data Access Objects
│   │   │   │   │   │   │   ├── ApplicationDao.kt
│   │   │   │   │   │   │   ├── ProfileDao.kt
│   │   │   │   │   │   │   ├── SavedJobDao.kt
│   │   │   │   │   │   │   └── SyncQueueDao.kt
│   │   │   │   │   │   └── entity/                    # Database entities
│   │   │   │   │   │       ├── ApplicationEntity.kt
│   │   │   │   │   │       ├── ProfileEntity.kt
│   │   │   │   │   │       ├── SavedJobEntity.kt
│   │   │   │   │   │       └── SyncQueueEntity.kt
│   │   │   │   │   ├── network/                       # API layer
│   │   │   │   │   │   ├── ApiClient.kt
│   │   │   │   │   │   ├── ApiErrorHandler.kt
│   │   │   │   │   │   ├── CareerConnectApiService.kt
│   │   │   │   │   │   ├── CloudinaryUploader.kt
│   │   │   │   │   │   └── CVUploader.kt
│   │   │   │   │   └── repository/                    # Repository layer
│   │   │   │   │       ├── AuthRepository.kt
│   │   │   │   │       ├── JobRepository.kt
│   │   │   │   │       ├── ProfileRepository.kt
│   │   │   │   │       ├── OfflineApplicationRepository.kt
│   │   │   │   │       ├── OfflineProfileRepository.kt
│   │   │   │   │       └── OfflineSavedJobRepository.kt
│   │   │   │   ├── domain/                            # Domain layer
│   │   │   │   │   └── model/                         # Domain models
│   │   │   │   │       ├── Job.kt
│   │   │   │   │       ├── UserProfile.kt
│   │   │   │   │       ├── ApplicationTracking.kt
│   │   │   │   │       └── SavedJob.kt
│   │   │   │   ├── ui/                                # Presentation layer
│   │   │   │   │   ├── authentication/
│   │   │   │   │   │   ├── LoginActivity.kt
│   │   │   │   │   │   ├── RegistrationActivity.kt
│   │   │   │   │   │   └── ForgotPasswordActivity.kt
│   │   │   │   │   ├── main/
│   │   │   │   │   │   └── MainActivity.kt
│   │   │   │   │   ├── profile/
│   │   │   │   │   │   ├── ProfileFragment.kt
│   │   │   │   │   │   ├── EditProfileActivity.kt
│   │   │   │   │   │   └── ProfileCompletionActivity.kt
│   │   │   │   │   ├── jobseeker/
│   │   │   │   │   │   ├── AllJobsActivity.kt
│   │   │   │   │   │   ├── JobDetailsActivity.kt
│   │   │   │   │   │   ├── JobApplicationActivity.kt
│   │   │   │   │   │   ├── BookmarkedJobsActivity.kt
│   │   │   │   │   │   ├── JobsAdapter.kt
│   │   │   │   │   │   └── SavedJobsAdapter.kt
│   │   │   │   │   ├── recruiter/
│   │   │   │   │   │   ├── JobPostingActivity.kt
│   │   │   │   │   │   ├── JobApplicationsActivity.kt
│   │   │   │   │   │   ├── RecruiterJobApplicationsActivity.kt
│   │   │   │   │   │   ├── CVViewerActivity.kt
│   │   │   │   │   │   └── adapter/
│   │   │   │   │   ├── settings/
│   │   │   │   │   │   ├── SettingsActivity.kt
│   │   │   │   │   │   └── ChangePasswordActivity.kt
│   │   │   │   │   └── common/
│   │   │   │   │       └── OfflineIndicator.kt
│   │   │   │   ├── utils/                             # Utility classes
│   │   │   │   │   ├── BiometricAuthManager.kt
│   │   │   │   │   ├── BiometricPreferences.kt
│   │   │   │   │   ├── ThemeManager.kt
│   │   │   │   │   ├── FontSizeManager.kt
│   │   │   │   │   ├── LanguageManager.kt
│   │   │   │   │   ├── NetworkConnectivityManager.kt
│   │   │   │   │   ├── NotificationPreferencesManager.kt
│   │   │   │   │   └── FileDownloader.kt
│   │   │   │   ├── sync/                              # Background sync
│   │   │   │   │   ├── SyncManager.kt
│   │   │   │   │   └── SyncWorker.kt
│   │   │   │   ├── session/
│   │   │   │   │   └── SessionManager.kt
│   │   │   │   └── MyFirebaseMessagingService.kt     # FCM service
│   │   │   ├── res/                                   # Resources
│   │   │   │   ├── layout/                            # XML layouts
│   │   │   │   ├── drawable/                          # Vector drawables
│   │   │   │   ├── values/                            # Strings, colors, themes
│   │   │   │   ├── values-af/                         # Afrikaans strings
│   │   │   │   ├── values-zu/                         # Zulu strings
│   │   │   │   ├── values-night/                      # Dark theme
│   │   │   │   └── xml/                               # Preferences, provider paths
│   │   │   └── AndroidManifest.xml                    # App manifest
│   │   ├── androidTest/                               # Instrumentation tests
│   │   └── test/                                      # Unit tests
│   ├── build.gradle.kts                               # Module build config
│   ├── proguard-rules.pro                             # ProGuard rules
│   └── google-services.json                           # Firebase config
├── gradle/                                            # Gradle configuration
│   ├── libs.versions.toml                             # Version catalog
│   └── wrapper/
├── build.gradle.kts                                   # Root build config
├── settings.gradle.kts                                # Project settings
├── README.md                                          # This file
├── BIOMETRIC_AUTHENTICATION.md                        # Biometric setup guide
├── OFFLINE_MODE_IMPLEMENTATION.md                     # Offline features guide
├── FEATURES_VERIFICATION.md                           # Feature checklist
└── SAFETY_VERIFICATION.md                             # Security verification
```

---

##  Prerequisites

### Development Environment

- **Android Studio** - Latest stable version (Hedgehog 2023.1.1 or newer)
- **JDK 11** - Java Development Kit version 11
- **Android SDK** - API levels 27 through 36
- **Git** - Version control

### Device Requirements

- **Minimum Android Version**: Android 8.1 (API 27)
- **Target Android Version**: Android 14 (API 36)
- **Internet Connection**: Required for initial setup and API features
- **Storage**: At least 100MB free space

### Firebase Setup

- Active Firebase project
- `google-services.json` configuration file
- Enabled Firebase services:
  - Authentication (Email/Password, Google)
  - Cloud Messaging
  - Storage
  - App Check

---

##  Setup Instructions

### 1. Clone the Repository

```bash
git clone https://github.com/IIEWFL/prog7314-poe-careerconnect.git
cd prog7314-poe-careerconnect
```

### 2. Firebase Configuration

#### Create Firebase Project

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Click "Add Project"
3. Follow the setup wizard

#### Enable Firebase Services

**Authentication:**
```
1. Firebase Console → Authentication → Sign-in method
2. Enable "Email/Password"
3. Enable "Google" (optional but recommended)
```

**Cloud Messaging:**
```
1. Firebase Console → Cloud Messaging
2. Already enabled by default
3. Note the Server Key for backend integration
```

**Storage:**
```
1. Firebase Console → Storage
2. Click "Get Started"
3. Configure security rules (see below)
```

**App Check:**
```
1. Firebase Console → App Check
2. Register app
3. Enable Debug provider for development
```

#### Download Configuration File

```
1. Firebase Console → Project Settings
2. Your apps → Android app → google-services.json
3. Download and place at: app/google-services.json
```

#### Configure OAuth for Google Sign-In

```bash
# Get your app's SHA-1 and SHA-256 fingerprints
cd android
./gradlew signingReport  # On Windows: gradlew.bat signingReport

# Add fingerprints to Firebase:
# Firebase Console → Project Settings → Your apps → Add fingerprint
```

#### Firebase Storage Rules

```javascript
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /cvs/{userId}/{fileName} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && request.auth.uid == userId;
    }
    match /documents/{userId}/{fileName} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && request.auth.uid == userId;
    }
  }
}
```

### 3. Android Studio Setup

#### Open Project

```
1. Open Android Studio
2. File → Open → Select project folder
3. Wait for Gradle sync to complete
```

#### Configure SDK

```
1. Tools → SDK Manager
2. SDK Platforms tab: Install Android 8.1 (API 27) through Android 14 (API 36)
3. SDK Tools tab: Ensure Android SDK Build-Tools 34+ is installed
```

#### Sync Dependencies

```
File → Sync Project with Gradle Files
```

### 4. Backend API Configuration

Update `NetworkConfig.kt` with your backend API URL:

```kotlin
object NetworkConfig {
    const val BASE_URL = "https://your-api-endpoint.com/api/"
    const val TIMEOUT_SECONDS = 30L
}
```

### 5. Local Properties (Optional)

Create `local.properties` if not exists:

```properties
sdk.dir=C\:\\Users\\YourUsername\\AppData\\Local\\Android\\Sdk
```

---

##  Configuration

### Application ID

Located in `app/build.gradle.kts`:

```kotlin
android {
    namespace = "vcmsa.projects.careerconnect"
    defaultConfig {
        applicationId = "vcmsa.projects.careerconnect"
        minSdk = 27
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }
}
```

### Build Variants

```kotlin
buildTypes {
    release {
        isMinifyEnabled = false
        proguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro"
        )
    }
    debug {
        // Development configuration
    }
}
```

### Signing Configuration (Release)

Add to `app/build.gradle.kts`:

```kotlin
android {
    signingConfigs {
        create("release") {
            storeFile = file("path/to/your/keystore.jks")
            storePassword = "your-store-password"
            keyAlias = "your-key-alias"
            keyPassword = "your-key-password"
        }
    }
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
        }
    }
}
```

---

##  Building & Running

### Using Android Studio

#### Debug Build

```
1. Connect Android device or start emulator
2. Click Run button (green play icon) or press Shift+F10
3. Select target device
4. App will build and launch
```

#### Release Build

```
1. Build → Generate Signed Bundle / APK
2. Select APK
3. Choose key store and signing configuration
4. Select release build variant
5. Click Finish
```

### Using Command Line (Windows)

#### Build Debug APK

```powershell
.\gradlew.bat clean assembleDebug
```

Output: `app/build/outputs/apk/debug/app-debug.apk`

#### Build Release APK

```powershell
.\gradlew.bat clean assembleRelease
```

Output: `app/build/outputs/apk/release/app-release.apk`

#### Install on Device

```powershell
# Install debug build
.\gradlew.bat installDebug

# Install and run
.\gradlew.bat installDebug
adb shell am start -n vcmsa.projects.careerconnect/.ui.authentication.LoginActivity
```

### Using Command Line (Linux/Mac)

```bash
# Build debug
./gradlew clean assembleDebug

# Build release
./gradlew clean assembleRelease

# Install debug
./gradlew installDebug
```

---

##  Offline Mode

CareerConnect includes comprehensive offline support. See [OFFLINE_MODE_IMPLEMENTATION.md](OFFLINE_MODE_IMPLEMENTATION.md) for details.

### Offline Features

 **Works Without Internet:**
- View bookmarked jobs
- Browse previously loaded jobs
- View profile information
- Create application drafts
- Edit profile (syncs later)

 **Offline Operations:**
- Bookmark/unbookmark jobs
- Save job application drafts
- Update profile information
- View application history

 **Automatic Sync:**
- Background synchronization when online
- Conflict resolution
- Retry failed operations
- Sync status indicators

### Testing Offline Mode

1. **Enable Airplane Mode**
```
1. Bookmark several jobs while online
2. Enable Airplane Mode
3. Force close and reopen app
4. Navigate to Bookmarked Jobs
5. Verify jobs are visible
6. Try bookmarking new jobs
```

2. **Verify Sync**
```
1. Perform offline actions
2. Disable Airplane Mode
3. Wait for sync (automatic)
4. Verify changes reflected on server
```

---

### Biometric Authentication

See [BIOMETRIC_AUTHENTICATION.md](BIOMETRIC_AUTHENTICATION.md) for setup guide.

**Supported:**
- Fingerprint authentication
- Android BiometricPrompt API
- Secure credential encryption
- Fallback to password authentication

**Requirements:**
- Android device with biometric hardware
- At least one fingerprint enrolled
- API 27+ (Android 8.1+)

**Testing:**
```
1. Login with password → Enable biometric when prompted
2. Logout
3. Tap "Use Fingerprint" button
4. Authenticate with fingerprint
5. Verify automatic login
```

---

##  Localisation

### Supported Languages

| Language | Code | Status |
|----------|------|--------|
| English | `en` |  Default |
| Afrikaans | `af` |  Complete |
| Zulu | `zu` |  Complete |

### String Resources

```
res/
├── values/           # English (default)
│   └── strings.xml
├── values-af/        # Afrikaans
│   └── strings.xml
└── values-zu/        # Zulu
    └── strings.xml
```

### Adding New Language

1. Create folder: `app/src/main/res/values-{language_code}/`
2. Copy `strings.xml` from `values/`
3. Translate all string resources
4. Add language option to `LanguageManager.kt`
5. Test language switching

### Changing Language

```
1. Open app
2. Login
3. Open Settings
4. Select Language
5. Choose desired language
6. App restarts with new language
```

---

##  Theming & UI Customization

### Theme Options

**Available Themes:**
-  Light Mode
-  Dark Mode
-  System Default (follows device theme)

### Font Size Options

- **Small**: 0.85x scale
- **Medium**: 1.0x scale (default)
- **Large**: 1.15x scale

##  Troubleshooting

### Common Issues

#### 1. "App won't launch"

**Possible causes:**
- Firebase configuration missing
- Invalid `google-services.json`
- SDK versions mismatch

**Solution:**
```powershell
# Clean and rebuild
.\gradlew.bat clean
.\gradlew.bat assembleDebug

# Verify google-services.json exists at app/google-services.json
# Sync Gradle files in Android Studio
```

#### 2. "Google Sign-In fails"

**Possible causes:**
- SHA-1/SHA-256 fingerprints not added to Firebase
- OAuth client not configured

**Solution:**
```bash
# Get fingerprints
.\gradlew.bat signingReport

# Add to Firebase Console → Project Settings → Your apps
# Enable Google Sign-In in Authentication → Sign-in method
```

#### 3. "Biometric not available"

**Possible causes:**
- No fingerprint hardware
- No fingerprints enrolled
- Running on emulator without biometric enabled

**Solution:**
```
For physical device:
1. Settings → Security → Fingerprint
2. Enroll at least one fingerprint

For emulator:
1. Device Manager → Edit → Advanced Settings
2. Enable "Fingerprint" checkbox
3. Enroll fingerprint in emulator settings
```

#### 4. "Network error / Cannot connect to API"

**Possible causes:**
- Incorrect API URL in NetworkConfig
- Backend server not running
- Firewall blocking requests

**Solution:**
```kotlin
// Update NetworkConfig.kt
object NetworkConfig {
    const val BASE_URL = "https://correct-api-url.com/api/"
}

// Check backend server is running
// Verify network permissions in AndroidManifest.xml
```

#### 5. "Room database errors"

**Possible causes:**
- Schema migration issues
- Database corruption
- Main thread database access

**Solution:**
```kotlin
// Clear app data
Settings → Apps → CareerConnect → Clear Data

// Or uninstall and reinstall
adb uninstall vcmsa.projects.careerconnect
.\gradlew.bat installDebug
```

#### 6. "Push notifications not working"

**Possible causes:**
- FCM not configured properly
- POST_NOTIFICATIONS permission not granted (Android 13+)
- Device token not registered

**Solution:**
```kotlin
// Check Logcat for FCM token
D/FCM: Token: <your-token>

// Verify permission granted
Settings → Apps → CareerConnect → Notifications → Allow

// Test from Firebase Console:
// Cloud Messaging → Send test message
```

#### 7. "Offline sync not working"

**Possible causes:**
- WorkManager not scheduled
- Network connectivity not detected
- Sync queue corrupted

**Solution:**
```kotlin
// Force immediate sync
val syncManager = SyncManager(context)
syncManager.triggerImmediateSync()

// Check pending operations
lifecycleScope.launch {
    val pendingCount = syncManager.getPendingOperationsCount()
    Log.d("Sync", "Pending: $pendingCount")
}
```

##  Additional Documentation

- [BIOMETRIC_AUTHENTICATION.md](BIOMETRIC_AUTHENTICATION.md) - Biometric setup and troubleshooting
- [OFFLINE_MODE_IMPLEMENTATION.md](OFFLINE_MODE_IMPLEMENTATION.md) - Offline features guide
- [FEATURES_VERIFICATION.md](FEATURES_VERIFICATION.md) - Feature implementation checklist
- [SAFETY_VERIFICATION.md](SAFETY_VERIFICATION.md) - Security verification report

---

##  Authors
- ST10184833 Tumelo Mabetwa
- ST10382916 Khumoestile Ramerafe
- ST10264959 Boiphelo Twala

##  Acknowledgments

- [Firebase](https://firebase.google.com/) - Backend services
- [Material Design](https://material.io/) - UI components
- [Retrofit](https://square.github.io/retrofit/) - HTTP client
- [Room](https://developer.android.com/training/data-storage/room) - Database
- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) - Async programming
