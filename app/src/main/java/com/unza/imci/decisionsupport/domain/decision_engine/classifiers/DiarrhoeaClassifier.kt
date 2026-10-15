package com.unza.imci.decisionsupport.domain.decision_engine.classifiers

import com.unza.imci.decisionsupport.data.model.PatientAssessment
import com.unza.imci.decisionsupport.domain.decision_engine.Classification
import com.unza.imci.decisionsupport.domain.decision_engine.ImciClassifier
import com.unza.imci.decisionsupport.domain.decision_engine.ImciModule
import com.unza.imci.decisionsupport.domain.decision_engine.Severity

/**
 * WHO IMCI Chart Booklet — "Does the child have diarrhoea?"
 * Up to three classifications can apply at once: dehydration status (always),
 * persistent diarrhoea (14 days or more) and dysentery (blood in stool).
 *
 * Dehydration uses the chart's "two of the following signs" rule:
 *  SEVERE: lethargic or unconscious / sunken eyes / not able to drink or drinking poorly /
 *          skin pinch goes back very slowly
 *  SOME:   restless, irritable / sunken eyes / drinks eagerly, thirsty / skin pinch goes back slowly
 */
class DiarrhoeaClassifier : ImciClassifier {

    private enum class Dehydration { SEVERE, SOME, NONE }

    override fun classify(patient: PatientAssessment): List<Classification> {
        if (!patient.hasDiarrhoea) return emptyList()

        val dehydration = dehydrationStatus(patient)
        val results = mutableListOf(dehydrationClassification(dehydration))

        if ((patient.diarrhoeaDurationDays ?: 0) >= 14) {
            results += if (dehydration != Dehydration.NONE) {
                Classification(
                    ImciModule.DIARRHOEA,
                    "SEVERE PERSISTENT DIARRHOEA",
                    "Treat dehydration before referral unless the child has another severe classification. " +
                        "Refer to hospital.",
                    Severity.SEVERE
                )
            } else {
                Classification(
                    ImciModule.DIARRHOEA,
                    "PERSISTENT DIARRHOEA",
                    "Advise the mother on feeding a child who has persistent diarrhoea. Give multivitamins " +
                        "and minerals (including zinc) for 14 days. Follow up in 5 days.",
                    Severity.MODERATE
                )
            }
        }

        if (patient.bloodInStool) {
            results += Classification(
                ImciModule.DIARRHOEA,
                "DYSENTERY",
                "Give ciprofloxacin for 3 days. Follow up in 2 days.",
                Severity.MODERATE
            )
        }

        return results
    }

    private fun dehydrationStatus(patient: PatientAssessment): Dehydration {
        val severeSigns = listOf(
            patient.lethargicOrUnconscious,
            patient.sunkenEyes,
            patient.cannotDrinkOrBreastfeed || patient.drinkingPoorly,
            patient.skinPinchGoesBackVerySlowly
        ).count { it }

        val someSigns = listOf(
            patient.restlessOrIrritable,
            patient.sunkenEyes,
            patient.drinksEagerlyThirsty,
            // A skin pinch that goes back very slowly has, by definition, also gone back slowly
            patient.skinPinchGoesBackSlowly || patient.skinPinchGoesBackVerySlowly
        ).count { it }

        return when {
            severeSigns >= 2 -> Dehydration.SEVERE
            someSigns >= 2 -> Dehydration.SOME
            else -> Dehydration.NONE
        }
    }

    private fun dehydrationClassification(dehydration: Dehydration) = when (dehydration) {
        Dehydration.SEVERE -> Classification(
            ImciModule.DIARRHOEA,
            "SEVERE DEHYDRATION",
            "Refer URGENTLY with the mother giving frequent sips of ORS on the way. Advise the mother " +
                "to continue breastfeeding. (If trained and no other severe classification: give fluid " +
                "for severe dehydration, Plan C.)",
            Severity.SEVERE
        )
        Dehydration.SOME -> Classification(
            ImciModule.DIARRHOEA,
            "SOME DEHYDRATION",
            "Give fluid, zinc and food for some dehydration (Plan B). Advise mother when to return " +
                "immediately. Follow up in 5 days if not improving.",
            Severity.MODERATE
        )
        Dehydration.NONE -> Classification(
            ImciModule.DIARRHOEA,
            "NO DEHYDRATION",
            "Give fluid, zinc and food to treat diarrhoea at home (Plan A). Advise mother when to " +
                "return immediately. Follow up in 5 days if not improving.",
            Severity.MILD
        )
    }
}
