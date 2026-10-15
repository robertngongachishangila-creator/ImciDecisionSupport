package com.unza.imci.decisionsupport.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.unza.imci.decisionsupport.data.model.AssessmentEntity
import com.unza.imci.decisionsupport.data.model.ClassificationEntity

/**
 * The main database class for the IMCI app.
 * Defines the database schema and provides access to the DAOs.
 * Implements the Singleton pattern to ensure only one database instance exists.
 */
@Database(entities = [AssessmentEntity::class, ClassificationEntity::class], version = 2, exportSchema = false)
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
         * Version 1 -> 2: adds the classifications table (one row per IMCI classification) and
         * indexes on assessments. Existing assessments keep their single stored classification,
         * copied across as a MODERATE "LEGACY" row so no saved patient record is lost.
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_assessments_patientId` ON `assessments` (`patientId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_assessments_timestamp` ON `assessments` (`timestamp`)")
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `classifications` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`assessmentId` INTEGER NOT NULL, " +
                        "`module` TEXT NOT NULL, " +
                        "`title` TEXT NOT NULL, " +
                        "`treatment` TEXT NOT NULL, " +
                        "`severity` TEXT NOT NULL, " +
                        "FOREIGN KEY(`assessmentId`) REFERENCES `assessments`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)"
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_classifications_assessmentId` ON `classifications` (`assessmentId`)")
                db.execSQL(
                    "INSERT INTO `classifications` (`assessmentId`, `module`, `title`, `treatment`, `severity`) " +
                        "SELECT `id`, 'LEGACY', `classification`, `treatmentRecommendation`, 'MODERATE' FROM `assessments`"
                )
            }
        }

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
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
