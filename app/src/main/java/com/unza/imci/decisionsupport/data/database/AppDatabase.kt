package com.unza.imci.decisionsupport.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.unza.imci.decisionsupport.data.model.AssessmentEntity

/**
 * The main database class for the IMCI app.
 * Defines the database schema and provides access to the DAOs.
 * Implements the Singleton pattern to ensure only one database instance exists.
 */
@Database(entities = [AssessmentEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    /**
     * Abstract method to get the DAO instance.
     * Room generates the implementation automatically.
     */
    abstract fun assessmentDao(): AssessmentDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Get the database instance.
         * Creates the database if it doesn't exist yet.
         */
        fun getDatabase(context: Context): AppDatabase {
            // If the database instance is null, create it
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "imci_database" // Name of the SQLite database file
                )
                    // Fallback to destructive migration if schema changes (for development)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}