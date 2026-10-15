package com.unza.imci.decisionsupport.domain.decision_engine.classifiers

import com.unza.imci.decisionsupport.data.model.MalariaTestResult
import com.unza.imci.decisionsupport.data.model.PatientAssessment
import com.unza.imci.decisionsupport.domain.decision_engine.Classification
import com.unza.imci.decisionsupport.domain.decision_engine.ImciClassifier
import com.unza.imci.decisionsupport.domain.decision_engine.ImciModule
import com.unza.imci.decisionsupport.domain.decision_engine.Severity

/**
 * WHO IMCI Chart Booklet — "Does the child have fever?"
 * Produces a fever/malaria classification and, separately, a measles classification when
 * the child has measles now or within the last 3 months.
 *
 * Fever:
 *  - Any general danger sign or stiff neck                  -> VERY SEVERE FEBRILE DISEASE
 *  - Malaria risk area: RDT positive or no test available    -> MALARIA
 *                       RDT negative                         -> FEVER: NO MALARIA
 *  - No malaria risk:   RDT positive                         -> MALARIA
 *                       runny nose or measles (other cause)  -> FEVER: MALARIA UNLIKELY
 *                       no other cause of fever              -> MALARIA
 *  - Fever present every day for more than 7 days            -> add "refer for assessment"
 */
class FeverClassifier : ImciClassifier {

    override fun classify(patient: PatientAssessment): List<Classification> {
        if (!patient.hasFever) return emptyList()

        val results = mutableListOf(feverClassification(patient))
        if (patient.measles) results += measlesClassification(patient)
        return results
    }

    private fun feverClassification(patient: PatientAssessment): Classification {
        if (patient.hasGeneralDangerSign || patient.stiffNeck) {
            return Classification(
                ImciModule.FEVER,
                "VERY SEVERE FEBRILE DISEASE",
                "Refer urgently. Give first dose of antibiotic and antimalarial. Treat to prevent low blood sugar.",
                Severity.SEVERE
            )
        }

        val prolongedFeverAdvice =
            if ((patient.feverDurationDays ?: 0) > 7) " Fever every day for more than 7 days: refer for assessment." else ""

        val test = patient.malariaTestResult
        val malaria = when {
            test == MalariaTestResult.POSITIVE -> true
            patient.malariaRiskArea -> test == MalariaTestResult.NOT_DONE
            else -> !(patient.runnyNose || patient.measles)
        }

        return when {
            malaria -> Classification(
                ImciModule.FEVER,
                "MALARIA",
                (if (test == MalariaTestResult.NOT_DONE) "Do a malaria rapid diagnostic test (RDT) if available. " else "") +
                    "Give recommended first-line antimalarial. Give paracetamol for high fever. " +
                    "Follow up in 3 days if fever persists." + prolongedFeverAdvice,
                Severity.MODERATE
            )
            patient.malariaRiskArea -> Classification(
                ImciModule.FEVER,
                "FEVER: NO MALARIA",
                "Give paracetamol for high fever. Treat other obvious causes of fever. " +
                    "Follow up in 3 days if fever persists." + prolongedFeverAdvice,
                Severity.MILD
            )
            else -> Classification(
                ImciModule.FEVER,
                "FEVER: MALARIA UNLIKELY",
                "Give paracetamol for high fever. Advise mother to return if fever persists beyond 2 days." +
                    prolongedFeverAdvice,
                Severity.MILD
            )
        }
    }

    private fun measlesClassification(patient: PatientAssessment): Classification {
        return if (patient.hasGeneralDangerSign || patient.cloudingOfCornea || patient.deepOrExtensiveMouthUlcers) {
            Classification(
                ImciModule.MEASLES,
                "SEVERE COMPLICATED MEASLES",
                "Refer urgently. Give vitamin A. Give first dose of antibiotic if clouding of cornea or deep mouth ulcers.",
                Severity.SEVERE
            )
        } else if (patient.pusDrainingFromEye || patient.mouthUlcers) {
            Classification(
                ImciModule.MEASLES,
                "MEASLES WITH EYE OR MOUTH COMPLICATIONS",
                "Give vitamin A. Treat eye infection with tetracycline ointment. Treat mouth ulcers with gentian violet.",
                Severity.MODERATE
            )
        } else {
            Classification(ImciModule.MEASLES, "MEASLES", "Give vitamin A.", Severity.MILD)
        }
    }
}
