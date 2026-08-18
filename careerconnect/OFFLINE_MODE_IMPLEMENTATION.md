# Offline Mode Implementation - CareerConnect

## Summary

Fixed and implemented complete offline functionality in CareerConnect. The app now fully supports offline mode for bookmarking jobs, allowing users to:
- View bookmarked jobs even when offline (airplane mode)
- Bookmark jobs while offline
- Remove bookmarks while offline
- Automatically sync changes when connection is restored

## Issues Fixed

### 1. App Not Running in Airplane Mode
**Problem:** When airplane mode was enabled, the app would open but not function properly.

**Solution:** Implemented offline-first architecture using:
- `OfflineSavedJobRepository` for local data storage
- Room database for persistent offline storage
- Network connectivity detection with graceful degradation

### 2. Bookmarks Not Available Offline
**Problem:** Bookmarked jobs required internet connection to view.

**Solution:** 
- Switched from network-only `SavedJobRepository` to offline-first `OfflineSavedJobRepository`
- All bookmarks are now cached locally using Room database
- Jobs are displayed from local cache immediately
- Background sync happens when connection is restored

### 3. Cannot Bookmark While Offline
**Problem:** Users couldn't bookmark jobs when offline.

**Solution:**
- Bookmarking now works offline using local database
- Operations are queued for sync when connection is restored
- User receives clear feedback: "Job bookmarked offline (will sync when online)"

---

## Changes Made

### 1. BookmarkedJobsActivity.kt
**File:** `app/src/main/java/vcmsa/projects/careerconnect/ui/jobseeker/BookmarkedJobsActivity.kt`

**Changes:**
- Replaced `SavedJobRepository` with `OfflineSavedJobRepository`
- Added `NetworkConnectivityManager` for connectivity monitoring
- Implemented offline indicator in UI
- Changed from one-time data fetch to reactive Flow observation
- Added automatic sync when connection is restored
- Added network status monitoring

**Key Features:**
```kotlin
// Observe saved jobs reactively (updates automatically)
savedJobRepository.getSavedJobsFlow().collectLatest { savedJobs ->
    savedJobsAdapter.updateJobs(savedJobs)
}

// Monitor network status
networkManager.observeConnectivity().collectLatest { isConnected ->
    updateNetworkIndicator(isConnected)
    if (isConnected) syncPendingChanges()
}
```

### 2. JobDetailsActivity.kt
**File:** `app/src/main/java/vcmsa/projects/careerconnect/ui/jobseeker/JobDetailsActivity.kt`

**Changes:**
- Replaced `SavedJobRepository` with `OfflineSavedJobRepository`
- Added `NetworkConnectivityManager` for connectivity detection
- Bookmark button now checks if job is already saved
- Bookmarking works offline with appropriate feedback
- Button text updates based on saved status ("Bookmark Job" → "Bookmarked")

**Key Features:**
```kotlin
// Check if job is already bookmarked
isJobSaved = savedJobRepository.isJobSaved(jobId)

// Save job offline
val result = savedJobRepository.saveJob(job)
val message = if (networkManager.isConnected()) {
    getString(R.string.job_saved_successfully)
} else {
    getString(R.string.job_saved_offline)
}
```

### 3. activity_bookmarked_jobs.xml
**File:** `app/src/main/res/layout/activity_bookmarked_jobs.xml`

**Changes:**
- Added offline indicator banner
- Shows "Offline Mode - Bookmarks available offline" when offline
- Automatically hides when connection is restored

### 4. strings.xml
**File:** `app/src/main/res/values/strings.xml`

**Added Strings:**
- `bookmarks_available_offline` - "Bookmarks available offline"
- `synced_changes` - "%1$d items synced"
- `no_bookmarked_jobs` - "No bookmarked jobs yet"
- `job_removed_offline` - "Job removed (will sync when online)"
- `job_saved_offline` - "Job bookmarked offline (will sync when online)"
- `bookmarked` - "Bookmarked"
- `bookmark_job` - "Bookmark Job"

---

## Testing Guide

### **Test 1: Bookmark Jobs While Online**
1. Open the app with WiFi/data enabled
2. Browse jobs and bookmark 2-3 jobs
3. Go to "Bookmarked Jobs"
4. Jobs should appear immediately
5. No offline indicator should be visible

### **Test 2: View Bookmarks in Airplane Mode**
1. With bookmarked jobs from Test 1
2. Enable Airplane Mode on your device
3. **Force close and reopen the app** (important!)
4. Navigate to "Bookmarked Jobs"
5. App should open successfully
6. Orange "Offline Mode" banner should appear
7. All bookmarked jobs should still be visible
8. You can tap jobs to view details

### **Test 3: Bookmark Jobs While Offline**
1. Keep Airplane Mode enabled
2. Browse available jobs
3. Tap on a job to view details
4. Tap "Bookmark Job" button
5. Should see "Job bookmarked offline (will sync when online)"
6. Job should appear in "Bookmarked Jobs" immediately
7. Offline indicator should still be visible

### **Test 4: Remove Bookmarks While Offline**
1. Keep Airplane Mode enabled
2. Go to "Bookmarked Jobs"
3. Tap "Remove" on a bookmarked job
4. Should see "Job removed (will sync when online)"
5. Job should disappear from list immediately

### **Test 5: Sync When Coming Back Online**
1. With offline changes from Test 3 & 4
2. Disable Airplane Mode
3. Wait a few seconds
4. Offline indicator should disappear
5. Should see "X items synced" notification
6. Changes should be synced to server

### **Test 6: Duplicate Bookmark Prevention**
1. With internet connection
2. Bookmark a job
3. Navigate away and back to same job
4. Button should show "Bookmarked" and be disabled
5. Should not be able to bookmark again

### **Test 7: Draft Applications**
1. Go offline
2. Create application draft
3. Edit draft (add cover letter)
4. Submit draft → Queued for sync
5. Go online → Application submitted automatically

### **Test 8: Profile Conflict Resolution**
1. Edit profile offline → Changes saved locally
2. Edit same profile on another device
3. Go online → Server version wins
4. Local changes discarded (conflict resolved)

### **Test 9: Background Sync**
1. Enable Airplane Mode
2. Bookmark multiple jobs
3. Disable Airplane Mode
4. Wait for periodic sync (or trigger manually)
5. Verify all bookmarks appear on server

### **Test 10: Sync Status Indicator**
1. Go offline
2. Make changes (bookmark jobs, edit profile)
3. Check indicator → Shows "X pending changes"
4. Go online
5. Indicator shows "Syncing..."
6. Changes complete → Shows "Sync complete" briefly

---

## Troubleshooting

### **"Database not found" error**

**Solution:**
```kotlin
// Initialize database in Application class
class CareerConnectApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppDatabase.getDatabase(this) // Initialize
    }
}
```

### **Sync not working**

**Solution:**
```kotlin
// Check WorkManager status
val workInfo = WorkManager.getInstance(context)
    .getWorkInfosForUniqueWork("periodic_sync")
    .get()

// Cancel and reschedule
syncManager.cancelAllSync()
syncManager.schedulePeriodicSync()
```

### **"Room database accessed on main thread" error**

**Solution:**  
All database operations are already wrapped in `suspend` functions. Ensure you call them from:
- Coroutines: `lifecycleScope.launch { }`
- Repositories: Already handles threading

### **Jobs not appearing after sync**

**Possible Causes:**
1. Sync queue has failed operations
2. Network connection unstable
3. Server API not responding

**Solution:**
```kotlin
// Check sync status
val status = syncManager.getSyncStatus()
Log.d("Sync", "Pending jobs: ${status.pendingJobsCount}")

// Force immediate sync
syncManager.triggerImmediateSync()

// Clear failed operations and retry
val database = AppDatabase.getDatabase(context)
database.syncQueueDao().clearAll()
```

### **Offline indicator not showing**

**Solution:**
```kotlin
// Make sure you're observing network status
lifecycleScope.launch {
    networkManager.observeConnectivity().collect { isConnected ->
        Log.d("Network", "Connected: $isConnected")
        if (isConnected) {
            offlineIndicator.hide()
        } else {
            offlineIndicator.showOffline()
        }
    }
}
```

### **Drafts not submitting when online**

**Solution:**
```kotlin
// Check if sync worker is scheduled
val workManager = WorkManager.getInstance(context)
val workInfo = workManager.getWorkInfosForUniqueWork("periodic_sync").get()

if (workInfo.isEmpty()) {
    // Reschedule
    syncManager.schedulePeriodicSync()
}

// Or manually trigger sync
syncManager.triggerImmediateSync()
```

### **Profile changes not syncing**

**Check if profile is marked as dirty:**
```kotlin
val repository = OfflineProfileRepository(context)
val hasPending = repository.hasPendingChanges()

if (hasPending) {
    // Force sync
    repository.syncProfileChanges()
}
```

### **App crashes on sync**

**Common Causes:**
1. Invalid data in sync queue
2. Network timeout
3. Server returning unexpected format

**Solution:**
```kotlin
// Clear sync queue
lifecycleScope.launch {
    val database = AppDatabase.getDatabase(context)
    database.syncQueueDao().clearAll()
    
    // Refresh from server
    val repository = OfflineSavedJobRepository(context)
    repository.refreshFromServer()
}
```

### **Conflict resolution not working**

**Verify conflict resolution settings:**
```kotlin
// Profile uses "Server Wins" strategy
// If local changes are lost, this is expected behavior

// To keep local changes, modify OfflineProfileRepository:
// Change resolveConflict() method to merge instead of replace
```

---

## How It Works

### Data Flow

1. **Viewing Bookmarks:**
   - App always reads from local Room database (works offline)
   - When online, background sync refreshes data from server
   - UI updates automatically via Flow

2. **Adding Bookmark:**
   - Saves to local database immediately
   - If online: Syncs to server in background
   - If offline: Queues for sync, shows "will sync when online"

3. **Removing Bookmark:**
   - Removes from local database immediately
   - If online: Syncs removal to server
   - If offline: Queues for sync

4. **Reconnecting to Internet:**
   - Network status change detected automatically
   - Pending operations synced in background
   - User sees "X items synced" notification

---

## Technical Details

### Database Schema
```sql
CREATE TABLE saved_jobs (
    jobId TEXT PRIMARY KEY,
    savedJobId TEXT,
    title TEXT NOT NULL,
    company TEXT NOT NULL,
    location TEXT,
    jobType TEXT,
    experienceLevel TEXT,
    salary TEXT,
    description TEXT,
    requirements TEXT,
    benefits TEXT,
    industry TEXT,
    postedDate INTEGER,
    savedDate INTEGER NOT NULL,
    isSynced INTEGER DEFAULT 1,
    pendingAction TEXT
);
```

### Sync Queue
Operations are queued when offline:
```json
{
  "operationType": "SAVE_JOB",
  "entityType": "JOB",
  "entityId": "job-123",
  "payload": "{\"jobId\":\"job-123\"}",
  "createdAt": 1699999999999
}
```

### Network Monitoring
```kotlin
networkManager.observeConnectivity().collectLatest { isConnected ->
    if (isConnected) {
        // Sync pending changes
        syncManager.syncAll()
    } else {
        // Show offline indicator
        showOfflineBanner()
    }
}
```

---

## Benefits

1. **Better User Experience:**
   - App works seamlessly offline
   - No confusing error messages
   - Clear feedback about offline status

2. **Data Reliability:**
   - No data loss when offline
   - Automatic sync when connection restored
   - Duplicate prevention

3. **Performance:**
   - Instant UI updates (no waiting for network)
   - Cached data loads immediately
   - Background sync doesn't block UI

4. **Offline-First:**
   - Users in areas with poor connectivity can still use bookmarks
   - Reduces dependency on constant internet connection
   - Better experience during commutes, travel, etc.

---

**Implementation Date:** November 15, 2025  
**Status:** Production-Ready
