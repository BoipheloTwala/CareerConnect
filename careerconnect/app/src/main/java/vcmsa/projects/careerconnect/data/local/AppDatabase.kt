//CODE ATTRIBUTION
//01
//Room Database
//Adapted from: Android Developers. (2025). Room Database. [online] Android Developers.
//Available at: https://developer.android.com/training/data-storage/room
//Date Accessed: 15 November 2025

//02
//Room Migration
//Adapted from: Android Developers. (2025). Room Migration. [online] Android Developers.
//Available at: https://developer.android.com/training/data-storage/room/migrating-db-versions
//Date Accessed: 15 November 2025

package vcmsa.projects.careerconnect.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import vcmsa.projects.careerconnect.data.local.dao.*
import vcmsa.projects.careerconnect.data.local.entity.*

/**
 * Main Room database for offline functionality
 * Provides local storage for jobs, applications, profile, and sync queue
 */
@Database(
    entities = [
        SavedJobEntity::class,
        ApplicationEntity::class,
        ProfileEntity::class,
        SyncQueueEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun savedJobDao(): SavedJobDao
    abstract fun applicationDao(): ApplicationDao
    abstract fun profileDao(): ProfileDao
    abstract fun syncQueueDao(): SyncQueueDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        private const val DATABASE_NAME = "careerconnect_db"
        
        /**
         * Get singleton instance of the database
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration() // For development - remove in production
                    .build()
                INSTANCE = instance
                instance
            }
        }
        
        /**
         * Clear database instance (useful for logout)
         */
        fun clearInstance() {
            INSTANCE = null
        }
    }
}

