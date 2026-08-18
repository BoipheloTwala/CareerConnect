plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
    id("kotlin-kapt")
}

android {
    namespace = "vcmsa.projects.careerconnect"
    compileSdk = 36

    defaultConfig {
        applicationId = "vcmsa.projects.careerconnect"
        minSdk = 27
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    // Firebase BoM manages versions for Firebase libraries
    implementation(platform("com.google.firebase:firebase-bom:33.3.0"))
    // Core Firebase product (example): Analytics
    implementation("com.google.firebase:firebase-analytics-ktx")
    // Ensure KTX Firebase core namespace is available
    implementation("com.google.firebase:firebase-common-ktx")
    // Firebase Auth for user identity (needed to scope uploads per user)
    implementation("com.google.firebase:firebase-auth-ktx")
    // Firebase Cloud Messaging for push notifications
    implementation("com.google.firebase:firebase-messaging-ktx")
    // Firebase App Check for security
    implementation("com.google.firebase:firebase-appcheck-debug")
    implementation("com.google.android.gms:play-services-auth:21.2.0")
    
    // Kotlin coroutines for Firebase tasks
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    
    // Using built-in Android HTTP client (no additional dependencies needed)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.drawerlayout)
    implementation(libs.androidx.gridlayout)
    implementation(libs.firebase.auth)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.firebase.storage)
    
    // Retrofit2 for API calls
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    
    // Biometric Authentication
    implementation("androidx.biometric:biometric:1.2.0-alpha05")
    
    // Encrypted SharedPreferences for secure credential storage
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    
    // Room Database for offline storage
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    
    // WorkManager for background sync
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    
    // Lifecycle components for LiveData and ViewModel
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}