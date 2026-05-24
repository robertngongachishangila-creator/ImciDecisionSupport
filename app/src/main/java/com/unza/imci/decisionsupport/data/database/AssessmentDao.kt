package com.unza.imci.decisionsupport.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.unza.imci.decisionsupport.data.model.AssessmentEntity

/**
 * Data Access Object for IMCI assessments.
 * Defines database operations for storing and retrieving patient assessments.
 * Supports proposal Objective 4: offline-first local storage.
 */
@Dao
interface AssessmentDao {

    /**
     * Insert a new assessment into the database.
     * Used when CHW completes an assessment.
     */
    @Insert
    suspend fun insertAssessment(assessment: AssessmentEntity)

    /**
     * Retrieve all assessments stored locally.
     * Returns assessments ordered by most recent first.
     */
    @Query("SELECT * FROM assessments ORDER BY timestamp DESC")
    suspend fun getAllAssessments(): List<AssessmentEntity>

    /**
     * Retrieve a specific assessment by ID.
     * Used for viewing or reviewing a past assessment.
     */
    @Query("SELECT * FROM assessments WHERE id = :assessmentId")
    suspend fun getAssessmentById(assessmentId: Int): AssessmentEntity?

    /**
     * Retrieve assessments for a specific patient (by anonymized ID).
     * Enables continuity of care for returning patients.
     */
    @Query("SELECT * FROM assessments WHERE patientId = :patientId ORDER BY timestamp DESC")
    suspend fun getAssessmentsByPatientId(patientId: String): List<AssessmentEntity>

    /**
     * Delete a specific assessment.
     * Used for data management or correcting entry errors.
     */
    @Query("DELETE FROM assessments WHERE id = :assessmentId")
    suspend fun deleteAssessment(assessmentId: Int)

    /**
     * Delete all assessments (for testing or data cleanup).
     */
    @Query("DELETE FROM assessments")
    suspend fun deleteAllAssessments()
}

