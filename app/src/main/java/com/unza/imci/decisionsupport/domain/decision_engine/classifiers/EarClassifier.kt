package com.unza.imci.decisionsupport.domain.decision_engine.classifiers

import com.unza.imci.decisionsupport.data.model.PatientAssessment
import com.unza.imci.decisionsupport.domain.decision_engine.Classification
import com.unza.imci.decisionsupport.domain.decision_engine.ImciClassifier
import com.unza.imci.decisionsupport.domain.decision_engine.ImciModule
import com.unza.imci.decisionsupport.domain.decision_engine.Severity

/**
 * WHO IMCI Chart Booklet (March 2014) — "Does the child have an ear problem?"
 * Assessed for every child. [PatientAssessment.earDischarge] means pus seen draining from the ear.
 *  - Tender swelling behind the ear                       -> MASTOIDITIS
 *  - Pus draining for 14 days or more                     -> CHRONIC EAR INFECTION
 *  - Ear pain, or pus draining for less than 14 days      -> ACUTE EAR INFECTION
 *  - None of the above                                    -> NO EAR INFECTION
 */
class EarClassifier : ImciClassifier {
    override fun classify(patient: PatientAssessment): List<Classification> {
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
                "Dry the ear by wicking. Apply topical quinolone ear drops for 14 days. Follow up in 5 days.",
                Severity.MODERATE
            )
        } else if (patient.hasEarPain || patient.earDischarge) {
            Classification(
                ImciModule.EAR,
                "ACUTE EAR INFECTION",
                "Give antibiotic for 5 days. Give paracetamol for pain. Dry the ear by wicking. Follow up in 5 days.",
                Severity.MODERATE
            )
        } else {
            Classification(
                ImciModule.EAR,
                "NO EAR INFECTION",
                "No treatment needed.",
                Severity.MILD,
                isNoFinding = true
            )
        }
        return listOf(classification)
    }
}
