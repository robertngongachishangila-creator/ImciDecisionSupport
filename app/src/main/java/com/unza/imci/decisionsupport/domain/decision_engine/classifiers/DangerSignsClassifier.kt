package com.unza.imci.decisionsupport.domain.decision_engine.classifiers

import com.unza.imci.decisionsupport.data.model.PatientAssessment
import com.unza.imci.decisionsupport.domain.decision_engine.Classification
import com.unza.imci.decisionsupport.domain.decision_engine.ImciClassifier
import com.unza.imci.decisionsupport.domain.decision_engine.ImciModule
import com.unza.imci.decisionsupport.domain.decision_engine.Severity

/** True if the child has any IMCI general danger sign. Several other modules escalate on this. */
val PatientAssessment.hasGeneralDangerSign: Boolean
    get() = cannotDrinkOrBreastfeed || vomitsEverything || convulsions || lethargicOrUnconscious

/**
 * WHO IMCI Chart Booklet — "Check for general danger signs".
 * A child with any general danger sign needs URGENT attention and referral.
 */
class DangerSignsClassifier : ImciClassifier {
    override fun classify(patient: PatientAssessment): List<Classification> {
        if (!patient.hasGeneralDangerSign) return emptyList()
        return listOf(
            Classification(
                ImciModule.DANGER_SIGNS,
                "SEVERE GENERAL DISEASE",
                "URGENT REFERRAL: Refer immediately to hospital. Complete the rest of the assessment " +
                    "quickly and give pre-referral treatment for any other severe classification.",
                Severity.SEVERE
            )
        )
    }
}
