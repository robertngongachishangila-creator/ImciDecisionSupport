package com.unza.imci.decisionsupport.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents a single IMCI assessment stored locally in SQLite.
 * Aligns with proposal Objective 4: "offline-first local storage for patient assessments"
 * and Ethical Considerations: "anonymized identifiers, no cloud transmission".
 * The individual classifications are stored in [ClassificationEntity]; [classification] and
 * [treatmentRecommendation] hold a combined summary for quick display in lists.
 * Indexed on patientId and timestamp (proposal Risk Analysis: efficient database indexing).
 */
@Entity(
    tableName = "assessments",
    indices = [Index("patientId"), Index("timestamp")]
)
data class AssessmentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val patientId: String,                    // Anonymized ID (e.g., "CHW01_20260522_001")
    val assessmentJson: String,               // Full PatientAssessment serialized as JSON
    val classification: String,               // e.g., "Severe Pneumonia", "Diarrhoea with some dehydration"
    val treatmentRecommendation: String,      // e.g., "Refer urgently + give first dose amoxicillin"
    val timestamp: Long = System.currentTimeMillis(), // When assessment was completed
    val isSynced: Boolean = false             // Always false per proposal scope (offline-only)
)

