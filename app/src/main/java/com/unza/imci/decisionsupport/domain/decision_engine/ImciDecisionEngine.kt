package com.unza.imci.decisionsupport.domain.decision_engine

import com.unza.imci.decisionsupport.data.model.PatientAssessment
import com.unza.imci.decisionsupport.domain.decision_engine.classifiers.DangerSignsClassifier
import com.unza.imci.decisionsupport.domain.decision_engine.classifiers.DiarrhoeaClassifier
import com.unza.imci.decisionsupport.domain.decision_engine.classifiers.EarClassifier
import com.unza.imci.decisionsupport.domain.decision_engine.classifiers.FeverClassifier
import com.unza.imci.decisionsupport.domain.decision_engine.classifiers.NutritionClassifier
import com.unza.imci.decisionsupport.domain.decision_engine.classifiers.RespiratoryClassifier

/**
 * The core Decision Engine.
 * Encodes WHO IMCI classification rules.
 *
 * IMCI is an *integrated* assessment: every module is assessed for every child and the child
 * receives one classification per illness found (e.g. PNEUMONIA + SOME DEHYDRATION + MALARIA).
 * Input: PatientAssessment -> Output: all classifications, most severe (pink) first.
 */
class ImciDecisionEngine {

    private val classifiers: List<ImciClassifier> = listOf(
        DangerSignsClassifier(),
        RespiratoryClassifier(),
        DiarrhoeaClassifier(),
        FeverClassifier(),
        EarClassifier()
    )

    // Runs last: any severe classification from the other modules makes SAM complicated
    private val nutritionClassifier = NutritionClassifier()

    fun assess(patient: PatientAssessment): AssessmentResult {
        val others = classifiers.flatMap { it.classify(patient) }
        // sortedBy is stable, so within a severity level the IMCI assessment order is kept
        val classifications = (others + nutritionClassifier.classify(patient, others))
            .sortedBy { it.severity.ordinal }

        // Modules that are assessed for every child return a "no problem found" row, so an
        // empty list is not the only way a child can have nothing to treat.
        if (classifications.all { it.isNoFinding }) {
            return AssessmentResult(
                classifications + Classification(
                    ImciModule.GENERAL,
                    "NO SPECIFIC ILLNESS",
                    "Counsel parent and give supportive care. Advise mother when to return immediately.",
                    Severity.MILD
                )
            )
        }
        return AssessmentResult(classifications)
    }
}
