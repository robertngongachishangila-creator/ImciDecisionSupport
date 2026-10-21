package com.unza.imci.decisionsupport.domain.decision_engine.classifiers

import com.unza.imci.decisionsupport.data.model.AppetiteTestResult
import com.unza.imci.decisionsupport.data.model.PatientAssessment
import com.unza.imci.decisionsupport.domain.decision_engine.Classification
import com.unza.imci.decisionsupport.domain.decision_engine.ImciClassifier
import com.unza.imci.decisionsupport.domain.decision_engine.ImciModule
import com.unza.imci.decisionsupport.domain.decision_engine.Severity

/**
 * WHO IMCI Chart Booklet (March 2014) — "Check for acute malnutrition" and "Check for anaemia".
 * Assessed for every child. Malnutrition and anaemia are classified separately.
 *
 * Malnutrition (MUAC applies only to children 6 months or older):
 *  - SAM signs (oedema of both feet or MUAC < 11.5 cm) with a medical complication, or an
 *    appetite test that was not passed                             -> COMPLICATED SEVERE ACUTE MALNUTRITION
 *  - SAM signs, no medical complication, appetite test passed      -> UNCOMPLICATED SEVERE ACUTE MALNUTRITION
 *  - MUAC 11.5 cm to < 12.5 cm                                     -> MODERATE ACUTE MALNUTRITION
 *  - MUAC 12.5 cm or more, no oedema                               -> NO ACUTE MALNUTRITION
 *  - No MUAC and no oedema                                         -> not classified
 * Medical complication: any general danger sign, chest indrawing, stridor in a calm child, or
 * any other severe (pink) classification for the same child, including SEVERE ANAEMIA.
 * Anaemia:
 *  - Severe palmar pallor                                          -> SEVERE ANAEMIA
 *  - Some palmar pallor                                            -> ANAEMIA (no iron if the child has SAM)
 */
class NutritionClassifier : ImciClassifier {

    /** Classifies without knowing the other modules' results; [ImciDecisionEngine] uses the overload below. */
    override fun classify(patient: PatientAssessment): List<Classification> = classify(patient, emptyList())

    /** [otherClassifications] are the results of the other modules for the same child. */
    fun classify(patient: PatientAssessment, otherClassifications: List<Classification>): List<Classification> {
        val muacApplies = (patient.ageInMonths ?: MUAC_MIN_AGE_MONTHS) >= MUAC_MIN_AGE_MONTHS
        val muac = if (muacApplies) patient.muacCm else null
        val hasSamSigns = patient.bilateralPittingOedema || (muac != null && muac < 11.5f)

        val anaemia = anaemia(patient, hasSamSigns)
        val anySevereElsewhere = (otherClassifications + listOfNotNull(anaemia)).any { it.severity == Severity.SEVERE }
        return listOfNotNull(malnutrition(patient, muac, hasSamSigns, anySevereElsewhere), anaemia)
    }

    private fun malnutrition(
        patient: PatientAssessment,
        muac: Float?,
        hasSamSigns: Boolean,
        anySevereElsewhere: Boolean
    ): Classification? {
        val medicalComplication = patient.hasGeneralDangerSign || patient.chestIndrawing ||
            patient.stridorInCalmChild || anySevereElsewhere

        // Oedema of both feet appears only in the chart's pink row, so it is always complicated
        return if (hasSamSigns &&
            (patient.bilateralPittingOedema || medicalComplication ||
                patient.appetiteTest != AppetiteTestResult.PASSED)
        ) {
            Classification(
                ImciModule.MALNUTRITION,
                "COMPLICATED SEVERE ACUTE MALNUTRITION",
                "Refer urgently to hospital. Treat the child to prevent low blood sugar. Keep the child warm.",
                Severity.SEVERE
            )
        } else if (hasSamSigns) {
            Classification(
                ImciModule.MALNUTRITION,
                "UNCOMPLICATED SEVERE ACUTE MALNUTRITION",
                "Give ready-to-use therapeutic food (RUTF). Counsel the mother on feeding. Advise mother " +
                    "when to return immediately. Follow up in 14 days.",
                Severity.MODERATE
            )
        } else if (muac != null && muac < 12.5f) {
            Classification(
                ImciModule.MALNUTRITION,
                "MODERATE ACUTE MALNUTRITION",
                "Assess the child's feeding and counsel the mother on feeding. Follow up in 30 days.",
                Severity.MODERATE
            )
        } else if (muac != null) {
            Classification(
                ImciModule.MALNUTRITION,
                "NO ACUTE MALNUTRITION",
                "Assess the child's feeding and counsel the mother on feeding if the child is under 2 years.",
                Severity.MILD,
                isNoFinding = true
            )
        } else {
            null
        }
    }

    private fun anaemia(patient: PatientAssessment, hasSam: Boolean): Classification? {
        return if (patient.severePalmarPallor) {
            Classification(ImciModule.ANAEMIA, "SEVERE ANAEMIA", "Refer urgently to hospital.", Severity.SEVERE)
        } else if (patient.somePalmarPallor) {
            // Therapeutic food (RUTF) already contains iron
            val iron = if (hasSam) "Do not give iron to a child on therapeutic feeding." else "Give iron."
            Classification(
                ImciModule.ANAEMIA,
                "ANAEMIA",
                "$iron Give albendazole/mebendazole if 1 year or older and no dose in the last 6 months. " +
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
