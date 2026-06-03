package com.unza.imci.decisionsupport.data.repository

import com.unza.imci.decisionsupport.data.database.AssessmentDao
import com.unza.imci.decisionsupport.data.model.AssessmentEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * Repository layer for IMCI assessments.
 * Acts as the single source of truth for assessment data.
 * It abstracts the database implementation (Room) from the rest of the app.
 *
 * Aligns with Proposal Phase 3: "Repository pattern will abstract database operations..."
 */
class AssessmentRepository(private val assessmentDao: AssessmentDao) {

    /**
     * Saves a new assessment to the local database.
     * Generates an anonymized ID for the patient to ensure privacy.
     *
     * @param assessment The AssessmentEntity to save.
     */
    suspend fun saveAssessment(assessment: AssessmentEntity) {
        // If no patient ID is provided, generate an anonymous one
        val finalAssessment = if (assessment.patientId.isBlank()) {
            assessment.copy(patientId = "ANON_${UUID.randomUUID().toString().take(8)}")
        } else {
            assessment
        }

        assessmentDao.insertAssessment(finalAssessment)
    }

    /**
     * Retrieves all stored assessments from the local database.
     * Ordered by timestamp (newest first).
     */
    fun getAllAssessments(): Flow<List<AssessmentEntity>> {
        return assessmentDao.getAllAssessments()
    }

    /**
     * Retrieves a single assessment by its unique ID.
     */
    suspend fun getAssessmentById(id: Int): AssessmentEntity? {
        return assessmentDao.getAssessmentById(id)
    }

    /**
     * Deletes a specific assessment.
     */
    suspend fun deleteAssessment(id: Int) {
        assessmentDao.deleteAssessment(id)
    }
}