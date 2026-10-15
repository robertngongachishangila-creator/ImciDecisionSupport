package com.unza.imci.decisionsupport.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * One IMCI classification (and its treatment recommendation) belonging to a saved assessment.
 * A single assessment can have several, e.g. PNEUMONIA + SOME DEHYDRATION + MALARIA.
 * Aligns with proposal Phase 3: separate storage of classifications and treatment recommendations.
 */
@Entity(
    tableName = "classifications",
    foreignKeys = [
        ForeignKey(
            entity = AssessmentEntity::class,
            parentColumns = ["id"],
            childColumns = ["assessmentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("assessmentId")]
)
data class ClassificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val assessmentId: Int,
    val module: String,      // ImciModule name, e.g. "RESPIRATORY"
    val title: String,       // e.g. "PNEUMONIA"
    val treatment: String,
    val severity: String     // Severity name: SEVERE / MODERATE / MILD
)
