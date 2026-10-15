package com.unza.imci.decisionsupport.domain.decision_engine

import com.unza.imci.decisionsupport.data.model.PatientAssessment

/**
 * IMCI colour-coded severity rows from the WHO Chart Booklet:
 * pink = urgent referral, yellow = specific treatment, green = home care.
 */
enum class Severity { SEVERE, MODERATE, MILD }

/** The IMCI assessment module that produced a classification. */
enum class ImciModule(val label: String) {
    DANGER_SIGNS("General danger signs"),
    RESPIRATORY("Cough or difficult breathing"),
    DIARRHOEA("Diarrhoea"),
    FEVER("Fever"),
    MEASLES("Measles"),
    EAR("Ear problem"),
    MALNUTRITION("Malnutrition"),
    ANAEMIA("Anaemia"),
    GENERAL("General")
}

data class Classification(
    val module: ImciModule,
    val title: String,
    val treatment: String,
    val severity: Severity
)

/**
 * One classifier per IMCI illness category (proposal: "a separate classifier per illness
 * category"). Each returns every classification that applies for its module, or an empty
 * list when the module's main symptom is absent.
 */
interface ImciClassifier {
    fun classify(patient: PatientAssessment): List<Classification>
}

/** Every classification for one child, most severe first. */
data class AssessmentResult(val classifications: List<Classification>) {
    val needsUrgentReferral: Boolean
        get() = classifications.any { it.severity == Severity.SEVERE }

    val titles: List<String>
        get() = classifications.map { it.title }
}
