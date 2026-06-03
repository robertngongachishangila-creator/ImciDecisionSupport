package com.unza.imci.decisionsupport.domain.decision_engine

import com.unza.imci.decisionsupport.data.model.PatientAssessment

/**
 * The core Decision Engine.
 * Encodes WHO IMCI classification rules.
 * Input: PatientAssessment -> Output: (Classification, Treatment)
 */
class ImciDecisionEngine {

    fun assess(patient: PatientAssessment): Pair<String, String> {

        // 1. General Danger Signs (Highest Priority)
        if (patient.cannotDrinkOrBreastfeed ||
            patient.vomitsEverything ||
            patient.convulsions ||
            patient.lethargicOrUnconscious) {
            return "SEVERE GENERAL DISEASE" to "URGENT REFERRAL: Refer immediately to hospital."
        }

        // 2. Cough / Difficult Breathing
        if (patient.hasCough || patient.hasDifficultBreathing) {
            val respiratoryResult = checkRespiratory(patient)
            if (respiratoryResult != null) return respiratoryResult
        }

        // 3. Diarrhoea
        if (patient.hasDiarrhoea) {
            val diarrhoeaResult = checkDiarrhoea(patient)
            if (diarrhoeaResult != null) return diarrhoeaResult
        }

        // 4. Fever (Simplified for now)
        if (patient.hasFever) {
            // Logic for malaria/fever would go here
            // Returning a default for now to keep logic simple
        }

        // Default: No specific classification found
        return "NO SPECIFIC ILLNESS" to "Counsel parent and give supportive care."
    }

    private fun checkRespiratory(patient: PatientAssessment): Pair<String, String>? {
        // Check for fast breathing based on age
        val isFastBreathing = checkFastBreathing(patient)

        return if (patient.chestIndrawing || patient.stridorInCalmChild) {
            "SEVERE PNEUMONIA" to "Refer urgently. Give first dose of antibiotic."
        } else if (isFastBreathing) {
            "PNEUMONIA" to "Give antibiotic for 5 days."
        } else {
            "COUGH OR COLD" to "Soothe throat and treat cough safely."
        }
    }

    private fun checkFastBreathing(patient: PatientAssessment): Boolean {
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

    private fun checkDiarrhoea(patient: PatientAssessment): Pair<String, String>? {
        return if (patient.skinPinchGoesBackSlowly) {
            "SEVERE DEHYDRATION" to "Refer urgently. Give fluids."
        } else if (patient.sunkenEyes || patient.veryThirstyOrDrinksPoorly) {
            "SOME DEHYDRATION" to "Give ORS and Zinc."
        } else {
            "NO DEHYDRATION" to "Give ORS and Zinc."
        }
    }
}