package com.unza.imci.decisionsupport.domain.decision_engine.classifiers

import com.unza.imci.decisionsupport.data.model.PatientAssessment
import com.unza.imci.decisionsupport.domain.decision_engine.Classification
import com.unza.imci.decisionsupport.domain.decision_engine.ImciClassifier
import com.unza.imci.decisionsupport.domain.decision_engine.ImciModule
import com.unza.imci.decisionsupport.domain.decision_engine.Severity

/**
 * WHO IMCI Chart Booklet (March 2014) — "Does the child have cough or difficult breathing?"
 *  - Any general danger sign or stridor in a calm child -> SEVERE PNEUMONIA OR VERY SEVERE DISEASE
 *  - Chest indrawing or fast breathing (age-based)      -> PNEUMONIA
 *  - No signs of pneumonia or very severe disease       -> COUGH OR COLD
 */
class RespiratoryClassifier : ImciClassifier {
    override fun classify(patient: PatientAssessment): List<Classification> {
        if (!patient.hasCough && !patient.hasDifficultBreathing) return emptyList()

        val classification = when {
            patient.hasGeneralDangerSign || patient.stridorInCalmChild ->
                Classification(
                    ImciModule.RESPIRATORY,
                    "SEVERE PNEUMONIA OR VERY SEVERE DISEASE",
                    "Give the first dose of an appropriate antibiotic. Refer urgently to hospital.",
                    Severity.SEVERE
                )
            patient.chestIndrawing || isFastBreathing(patient) ->
                Classification(
                    ImciModule.RESPIRATORY,
                    "PNEUMONIA",
                    "Give oral amoxicillin for 5 days. Soothe the throat and relieve the cough with a safe " +
                        "remedy. If coughing for more than 14 days, refer for assessment. Advise mother when " +
                        "to return immediately. Follow up in 3 days.",
                    Severity.MODERATE
                )
            else ->
                Classification(
                    ImciModule.RESPIRATORY,
                    "COUGH OR COLD",
                    "Soothe the throat and relieve the cough with a safe remedy. If coughing for more than " +
                        "14 days, refer for assessment. Follow up in 5 days if not improving.",
                    Severity.MILD
                )
        }
        return listOf(classification)
    }

    private fun isFastBreathing(patient: PatientAssessment): Boolean {
        val age = patient.ageInMonths ?: return false
        val rate = patient.breathingRatePerMin ?: return false

        // WHO IMCI fast breathing thresholds for children 2 months up to 5 years
        return when (age) {
            in 2..11 -> rate >= 50
            in 12..59 -> rate >= 40
            else -> false
        }
    }
}
