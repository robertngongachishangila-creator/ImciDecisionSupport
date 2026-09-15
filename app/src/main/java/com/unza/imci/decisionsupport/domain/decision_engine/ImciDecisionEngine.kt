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

        // 4. Fever
        if (patient.hasFever) {
            val feverResult = checkFever(patient)
            if (feverResult != null) return feverResult
        }

        // 5. Ear Problems
        if (patient.hasEarPain || patient.earDischarge || patient.tenderSwellingBehindEar) {
            val earResult = checkEarProblem(patient)
            if (earResult != null) return earResult
        }

        // 6. Malnutrition / Anaemia (assessed for every child; only surfaced here when
        // it is the most significant finding — a severe/moderate result takes priority
        // over the "no specific illness" default below)
        val malnutritionResult = checkMalnutrition(patient)
        if (malnutritionResult != null) return malnutritionResult

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

    private fun checkFever(patient: PatientAssessment): Pair<String, String>? {
        // Very severe febrile disease takes priority over malaria/measles classification
        if (patient.stiffNeck) {
            return "VERY SEVERE FEBRILE DISEASE" to
                "Refer urgently. Give first dose of antibiotic and antimalarial. Treat to prevent low blood sugar."
        }

        if (patient.measles) {
            return checkMeaslesComplications(patient)
        }

        return if (patient.runnyNose) {
            "FEVER: MALARIA UNLIKELY" to
                "Give paracetamol for high fever. Advise mother to return if fever persists beyond 2 days."
        } else {
            "MALARIA" to "Give recommended first-line antimalarial. Give paracetamol for high fever."
        }
    }

    private fun checkMeaslesComplications(patient: PatientAssessment): Pair<String, String> {
        return if (patient.cloudingOfCornea || patient.deepOrExtensiveMouthUlcers) {
            "SEVERE COMPLICATED MEASLES" to
                "Refer urgently. Give vitamin A. Give first dose of antibiotic if clouding of cornea or deep mouth ulcers."
        } else if (patient.pusDrainingFromEye || patient.mouthUlcers) {
            "MEASLES WITH EYE OR MOUTH COMPLICATIONS" to
                "Give vitamin A. Treat eye infection with tetracycline ointment. Treat mouth ulcers with gentian violet."
        } else {
            "MEASLES" to "Give vitamin A."
        }
    }

    private fun checkEarProblem(patient: PatientAssessment): Pair<String, String> {
        return if (patient.tenderSwellingBehindEar) {
            "MASTOIDITIS" to "Refer urgently. Give first dose of antibiotic. Give paracetamol for pain."
        } else if (patient.earDischarge && (patient.earDischargeDurationDays ?: 0) >= 14) {
            "CHRONIC EAR INFECTION" to "Dry the ear by wicking. Apply topical quinolone ear drops."
        } else {
            "ACUTE EAR INFECTION" to "Give antibiotic for 5 days. Give paracetamol for pain. Dry the ear by wicking."
        }
    }

    private fun checkMalnutrition(patient: PatientAssessment): Pair<String, String>? {
        val muac = patient.muacCm

        return if (patient.visibleSevereWasting || patient.bilateralPittingOedema || (muac != null && muac < 11.5f)) {
            "SEVERE ACUTE MALNUTRITION" to
                "Refer urgently to a stabilisation centre. Treat to prevent low blood sugar."
        } else if (patient.palePalms || (muac != null && muac < 12.5f)) {
            "MODERATE ACUTE MALNUTRITION / ANAEMIA" to
                "Counsel on feeding. Give iron. Follow up in 14 days."
        } else {
            null // No acute malnutrition — falls through to the default classification
        }
    }
}