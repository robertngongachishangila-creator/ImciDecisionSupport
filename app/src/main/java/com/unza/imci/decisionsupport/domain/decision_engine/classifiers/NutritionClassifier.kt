package com.unza.imci.decisionsupport.domain.decision_engine.classifiers

import com.unza.imci.decisionsupport.data.model.PatientAssessment
import com.unza.imci.decisionsupport.domain.decision_engine.Classification
import com.unza.imci.decisionsupport.domain.decision_engine.ImciClassifier
import com.unza.imci.decisionsupport.domain.decision_engine.ImciModule
import com.unza.imci.decisionsupport.domain.decision_engine.Severity

/**
 * WHO IMCI Chart Booklet — "Check for acute malnutrition" and "Check for anaemia".
 * Assessed for every child. Malnutrition and anaemia are classified separately.
 *
 * Malnutrition (MUAC applies only to children 6 months or older):
 *  - Oedema of both feet, visible severe wasting or MUAC < 11.5 cm -> SEVERE ACUTE MALNUTRITION
 *  - MUAC 11.5 cm to < 12.5 cm                                     -> MODERATE ACUTE MALNUTRITION
 * Anaemia:
 *  - Severe palmar pallor                                          -> SEVERE ANAEMIA
 *  - Some palmar pallor                                            -> ANAEMIA
 */
class NutritionClassifier : ImciClassifier {

    override fun classify(patient: PatientAssessment): List<Classification> {
        return listOfNotNull(malnutrition(patient), anaemia(patient))
    }

    private fun malnutrition(patient: PatientAssessment): Classification? {
        val muacApplies = (patient.ageInMonths ?: MUAC_MIN_AGE_MONTHS) >= MUAC_MIN_AGE_MONTHS
        val muac = if (muacApplies) patient.muacCm else null

        return if (patient.visibleSevereWasting || patient.bilateralPittingOedema || (muac != null && muac < 11.5f)) {
            Classification(
                ImciModule.MALNUTRITION,
                "SEVERE ACUTE MALNUTRITION",
                "Refer urgently to a stabilisation centre. Treat to prevent low blood sugar. Keep the child warm.",
                Severity.SEVERE
            )
        } else if (muac != null && muac < 12.5f) {
            Classification(
                ImciModule.MALNUTRITION,
                "MODERATE ACUTE MALNUTRITION",
                "Assess the child's feeding and counsel the mother on feeding. Follow up in 30 days.",
                Severity.MODERATE
            )
        } else {
            null
        }
    }

    private fun anaemia(patient: PatientAssessment): Classification? {
        return if (patient.severePalmarPallor) {
            Classification(ImciModule.ANAEMIA, "SEVERE ANAEMIA", "Refer urgently to hospital.", Severity.SEVERE)
        } else if (patient.somePalmarPallor) {
            Classification(
                ImciModule.ANAEMIA,
                "ANAEMIA",
                "Give iron. Give albendazole/mebendazole if 1 year or older and no dose in the last 6 months. " +
                    "Test for malaria in a malaria risk area. Follow up in 14 days.",
                Severity.MODERATE
            )
        } else {
            null
        }
    }

    private companion object {
        const val MUAC_MIN_AGE_MONTHS = 6
    }
}
