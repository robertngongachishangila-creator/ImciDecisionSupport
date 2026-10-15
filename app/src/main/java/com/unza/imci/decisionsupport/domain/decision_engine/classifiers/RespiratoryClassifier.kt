package com.unza.imci.decisionsupport.domain.decision_engine.classifiers

import com.unza.imci.decisionsupport.data.model.PatientAssessment
import com.unza.imci.decisionsupport.domain.decision_engine.Classification
import com.unza.imci.decisionsupport.domain.decision_engine.ImciClassifier
import com.unza.imci.decisionsupport.domain.decision_engine.ImciModule
import com.unza.imci.decisionsupport.domain.decision_engine.Severity

/**
 * WHO IMCI Chart Booklet — "Does the child have cough or difficult breathing?"
 *  - Any general danger sign, chest indrawing or stridor in a calm child -> SEVERE PNEUMONIA
 *  - Fast breathing (age-based threshold)                                 -> PNEUMONIA
 *  - No signs of pneumonia                                                -> COUGH OR COLD
 */
class RespiratoryClassifier : ImciClassifier {
    override fun classify(patient: PatientAssessment): List<Classification> {
        if (!patient.hasCough && !patient.hasDifficultBreathing) return emptyList()

        val classification = when {
            patient.hasGeneralDangerSign || patient.chestIndrawing || patient.stridorInCalmChild ->
                Classification(
                    ImciModule.RESPIRATORY,
                    "SEVERE PNEUMONIA",
                    "Refer urgently. Give first dose of antibiotic.",
                    Severity.SEVERE
                )
            isFastBreathing(patient) ->
                Classification(
                    ImciModule.RESPIRATORY,
                    "PNEUMONIA",
                    "Give antibiotic for 5 days. Soothe the throat and relieve the cough with a safe remedy. " +
                        "Advise mother when to return immediately. Follow up in 2 days.",
                    Severity.MODERATE
                )
            else ->
                Classification(
                    ImciModule.RESPIRATORY,
                    "COUGH OR COLD",
                    "Soothe throat and treat cough safely. If coughing for more than 3 weeks, refer for " +
                        "assessment. Follow up in 5 days if not improving.",
                    Severity.MILD
                )
        }
        return listOf(classification)
    }

    private fun isFastBreathing(patient: PatientAssessment): Boolean {
        val age = patient.ageInMonths ?: return false
        val rate = patient.breathingRatePerMin ?: return false

        // WHO IMCI Fast Breathing Thresholds
        return when {
            age < 2 -> rate >= 60
            age < 12 -> rate >= 50
            age < 60 -> rate >= 40 // 12 months to 5 years
            else -> false
        }
    }
}
