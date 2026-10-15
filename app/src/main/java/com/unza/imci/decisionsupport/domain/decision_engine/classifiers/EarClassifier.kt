package com.unza.imci.decisionsupport.domain.decision_engine.classifiers

import com.unza.imci.decisionsupport.data.model.PatientAssessment
import com.unza.imci.decisionsupport.domain.decision_engine.Classification
import com.unza.imci.decisionsupport.domain.decision_engine.ImciClassifier
import com.unza.imci.decisionsupport.domain.decision_engine.ImciModule
import com.unza.imci.decisionsupport.domain.decision_engine.Severity

/**
 * WHO IMCI Chart Booklet — "Does the child have an ear problem?"
 *  - Tender swelling behind the ear                       -> MASTOIDITIS
 *  - Discharge for 14 days or more                        -> CHRONIC EAR INFECTION
 *  - Ear pain, or discharge for less than 14 days         -> ACUTE EAR INFECTION
 */
class EarClassifier : ImciClassifier {
    override fun classify(patient: PatientAssessment): List<Classification> {
        if (!patient.hasEarPain && !patient.earDischarge && !patient.tenderSwellingBehindEar) return emptyList()

        val classification = if (patient.tenderSwellingBehindEar) {
            Classification(
                ImciModule.EAR,
                "MASTOIDITIS",
                "Refer urgently. Give first dose of antibiotic. Give paracetamol for pain.",
                Severity.SEVERE
            )
        } else if (patient.earDischarge && (patient.earDischargeDurationDays ?: 0) >= 14) {
            Classification(
                ImciModule.EAR,
                "CHRONIC EAR INFECTION",
                "Dry the ear by wicking. Apply topical quinolone ear drops. Follow up in 5 days.",
                Severity.MODERATE
            )
        } else {
            Classification(
                ImciModule.EAR,
                "ACUTE EAR INFECTION",
                "Give antibiotic for 5 days. Give paracetamol for pain. Dry the ear by wicking. Follow up in 5 days.",
                Severity.MODERATE
            )
        }
        return listOf(classification)
    }
}
