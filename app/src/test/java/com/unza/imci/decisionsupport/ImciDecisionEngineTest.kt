package com.unza.imci.decisionsupport.domain.decision_engine

import com.unza.imci.decisionsupport.data.model.PatientAssessment
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * Local Unit Tests for the Decision Engine.
 * Validates WHO IMCI classification rules against the published Chart Booklet decision
 * tables (WHO, 2020), per the proposal's Evaluation Criteria: 50+ cases across all six
 * modules, 8+ per module, exercising severity boundaries (severe / moderate-non-severe / none).
 * Runs on the local JVM (no emulator needed).
 */
class ImciDecisionEngineTest {

    private lateinit var engine: ImciDecisionEngine

    @Before
    fun setup() {
        engine = ImciDecisionEngine()
    }

    // ---------------------------------------------------------------------
    // 1. General Danger Signs
    // ---------------------------------------------------------------------

    @Test
    fun dangerSigns_cannotDrinkOrBreastfeed_isSevereGeneralDisease() {
        val patient = PatientAssessment(ageInMonths = 10, cannotDrinkOrBreastfeed = true)
        assertEquals("SEVERE GENERAL DISEASE", engine.assess(patient).first)
    }

    @Test
    fun dangerSigns_vomitsEverything_isSevereGeneralDisease() {
        val patient = PatientAssessment(ageInMonths = 8, vomitsEverything = true)
        assertEquals("SEVERE GENERAL DISEASE", engine.assess(patient).first)
    }

    @Test
    fun dangerSigns_convulsions_isSevereGeneralDisease() {
        val patient = PatientAssessment(ageInMonths = 36, convulsions = true)
        assertEquals("SEVERE GENERAL DISEASE", engine.assess(patient).first)
    }

    @Test
    fun dangerSigns_lethargicOrUnconscious_isSevereGeneralDisease() {
        val patient = PatientAssessment(ageInMonths = 5, lethargicOrUnconscious = true)
        assertEquals("SEVERE GENERAL DISEASE", engine.assess(patient).first)
    }

    @Test
    fun dangerSigns_takePriorityOverRespiratoryFindings() {
        val patient = PatientAssessment(
            ageInMonths = 24,
            convulsions = true,
            hasCough = true,
            chestIndrawing = true
        )
        assertEquals("SEVERE GENERAL DISEASE", engine.assess(patient).first)
    }

    @Test
    fun dangerSigns_multipleSignsPresent_stillSevereGeneralDisease() {
        val patient = PatientAssessment(
            ageInMonths = 14,
            cannotDrinkOrBreastfeed = true,
            vomitsEverything = true,
            convulsions = true
        )
        assertEquals("SEVERE GENERAL DISEASE", engine.assess(patient).first)
    }

    @Test
    fun dangerSigns_none_fallsThroughToOtherModules() {
        val patient = PatientAssessment(ageInMonths = 24, hasCough = true, breathingRatePerMin = 45)
        assertEquals("PNEUMONIA", engine.assess(patient).first)
    }

    @Test
    fun dangerSigns_absent_healthyChild_isNoSpecificIllness() {
        val patient = PatientAssessment(ageInMonths = 30)
        assertEquals("NO SPECIFIC ILLNESS", engine.assess(patient).first)
    }

    // ---------------------------------------------------------------------
    // 2. Cough / Difficult Breathing
    // ---------------------------------------------------------------------

    @Test
    fun respiratory_chestIndrawing_isSeverePneumonia() {
        val patient = PatientAssessment(ageInMonths = 20, hasCough = true, chestIndrawing = true)
        assertEquals("SEVERE PNEUMONIA", engine.assess(patient).first)
    }

    @Test
    fun respiratory_stridorInCalmChild_isSeverePneumonia() {
        val patient = PatientAssessment(ageInMonths = 18, hasDifficultBreathing = true, stridorInCalmChild = true)
        assertEquals("SEVERE PNEUMONIA", engine.assess(patient).first)
    }

    @Test
    fun respiratory_fastBreathing_under2Months_thresholdIs60() {
        val patient = PatientAssessment(ageInMonths = 1, hasCough = true, breathingRatePerMin = 60)
        assertEquals("PNEUMONIA", engine.assess(patient).first)
    }

    @Test
    fun respiratory_belowThreshold_under2Months_isNotFastBreathing() {
        val patient = PatientAssessment(ageInMonths = 1, hasCough = true, breathingRatePerMin = 59)
        assertEquals("COUGH OR COLD", engine.assess(patient).first)
    }

    @Test
    fun respiratory_fastBreathing_2to11Months_thresholdIs50() {
        val patient = PatientAssessment(ageInMonths = 6, hasCough = true, breathingRatePerMin = 50)
        assertEquals("PNEUMONIA", engine.assess(patient).first)
    }

    @Test
    fun respiratory_belowThreshold_2to11Months_isNotFastBreathing() {
        val patient = PatientAssessment(ageInMonths = 6, hasCough = true, breathingRatePerMin = 49)
        assertEquals("COUGH OR COLD", engine.assess(patient).first)
    }

    @Test
    fun respiratory_fastBreathing_12to59Months_thresholdIs40() {
        val patient = PatientAssessment(ageInMonths = 24, hasCough = true, breathingRatePerMin = 40)
        assertEquals("PNEUMONIA", engine.assess(patient).first)
    }

    @Test
    fun respiratory_belowThreshold_12to59Months_isNotFastBreathing() {
        val patient = PatientAssessment(ageInMonths = 24, hasCough = true, breathingRatePerMin = 39)
        assertEquals("COUGH OR COLD", engine.assess(patient).first)
    }

    @Test
    fun respiratory_noSevereSignsAndNormalRate_isCoughOrCold() {
        val patient = PatientAssessment(ageInMonths = 30, hasCough = true, breathingRatePerMin = 30)
        assertEquals("COUGH OR COLD", engine.assess(patient).first)
    }

    // ---------------------------------------------------------------------
    // 3. Diarrhoea
    // ---------------------------------------------------------------------

    @Test
    fun diarrhoea_skinPinchGoesBackSlowly_isSevereDehydration() {
        val patient = PatientAssessment(ageInMonths = 18, hasDiarrhoea = true, skinPinchGoesBackSlowly = true)
        assertEquals("SEVERE DEHYDRATION", engine.assess(patient).first)
    }

    @Test
    fun diarrhoea_sunkenEyes_isSomeDehydration() {
        val patient = PatientAssessment(ageInMonths = 18, hasDiarrhoea = true, sunkenEyes = true)
        assertEquals("SOME DEHYDRATION", engine.assess(patient).first)
    }

    @Test
    fun diarrhoea_veryThirstyOrDrinksPoorly_isSomeDehydration() {
        val patient = PatientAssessment(ageInMonths = 18, hasDiarrhoea = true, veryThirstyOrDrinksPoorly = true)
        assertEquals("SOME DEHYDRATION", engine.assess(patient).first)
    }

    @Test
    fun diarrhoea_severeSignTakesPriorityOverSomeDehydrationSigns() {
        val patient = PatientAssessment(
            ageInMonths = 18,
            hasDiarrhoea = true,
            skinPinchGoesBackSlowly = true,
            sunkenEyes = true
        )
        assertEquals("SEVERE DEHYDRATION", engine.assess(patient).first)
    }

    @Test
    fun diarrhoea_noDehydrationSigns_isNoDehydration() {
        val patient = PatientAssessment(ageInMonths = 18, hasDiarrhoea = true)
        assertEquals("NO DEHYDRATION", engine.assess(patient).first)
    }

    @Test
    fun diarrhoea_withBloodInStool_stillClassifiesDehydrationStatus() {
        val patient = PatientAssessment(ageInMonths = 18, hasDiarrhoea = true, bloodInStool = true, sunkenEyes = true)
        assertEquals("SOME DEHYDRATION", engine.assess(patient).first)
    }

    @Test
    fun diarrhoea_longDuration_noDehydrationSigns_isNoDehydration() {
        val patient = PatientAssessment(ageInMonths = 18, hasDiarrhoea = true, diarrhoeaDurationDays = 20)
        assertEquals("NO DEHYDRATION", engine.assess(patient).first)
    }

    @Test
    fun diarrhoea_absent_fallsThroughToOtherModules() {
        val patient = PatientAssessment(ageInMonths = 18, hasDiarrhoea = false, hasFever = true)
        assertEquals("MALARIA", engine.assess(patient).first)
    }

    // ---------------------------------------------------------------------
    // 4. Fever
    // ---------------------------------------------------------------------

    @Test
    fun fever_stiffNeck_isVerySevereFebrileDisease() {
        val patient = PatientAssessment(ageInMonths = 24, hasFever = true, stiffNeck = true)
        assertEquals("VERY SEVERE FEBRILE DISEASE", engine.assess(patient).first)
    }

    @Test
    fun fever_noRunnyNoseNoMeasles_isMalaria() {
        val patient = PatientAssessment(ageInMonths = 24, hasFever = true)
        assertEquals("MALARIA", engine.assess(patient).first)
    }

    @Test
    fun fever_withRunnyNose_isMalariaUnlikely() {
        val patient = PatientAssessment(ageInMonths = 24, hasFever = true, runnyNose = true)
        assertEquals("FEVER: MALARIA UNLIKELY", engine.assess(patient).first)
    }

    @Test
    fun fever_measlesNoComplications_isMeasles() {
        val patient = PatientAssessment(ageInMonths = 24, hasFever = true, measles = true)
        assertEquals("MEASLES", engine.assess(patient).first)
    }

    @Test
    fun fever_measlesWithCloudingOfCornea_isSevereComplicatedMeasles() {
        val patient = PatientAssessment(ageInMonths = 24, hasFever = true, measles = true, cloudingOfCornea = true)
        assertEquals("SEVERE COMPLICATED MEASLES", engine.assess(patient).first)
    }

    @Test
    fun fever_measlesWithDeepMouthUlcers_isSevereComplicatedMeasles() {
        val patient = PatientAssessment(ageInMonths = 24, hasFever = true, measles = true, deepOrExtensiveMouthUlcers = true)
        assertEquals("SEVERE COMPLICATED MEASLES", engine.assess(patient).first)
    }

    @Test
    fun fever_measlesWithPusDrainingFromEye_isMeaslesWithEyeOrMouthComplications() {
        val patient = PatientAssessment(ageInMonths = 24, hasFever = true, measles = true, pusDrainingFromEye = true)
        assertEquals("MEASLES WITH EYE OR MOUTH COMPLICATIONS", engine.assess(patient).first)
    }

    @Test
    fun fever_measlesWithMouthUlcers_isMeaslesWithEyeOrMouthComplications() {
        val patient = PatientAssessment(ageInMonths = 24, hasFever = true, measles = true, mouthUlcers = true)
        assertEquals("MEASLES WITH EYE OR MOUTH COMPLICATIONS", engine.assess(patient).first)
    }

    @Test
    fun fever_stiffNeckTakesPriorityOverMeasles() {
        val patient = PatientAssessment(ageInMonths = 24, hasFever = true, stiffNeck = true, measles = true, cloudingOfCornea = true)
        assertEquals("VERY SEVERE FEBRILE DISEASE", engine.assess(patient).first)
    }

    @Test
    fun fever_absent_isNoSpecificIllness() {
        val patient = PatientAssessment(ageInMonths = 24, hasFever = false)
        assertEquals("NO SPECIFIC ILLNESS", engine.assess(patient).first)
    }

    // ---------------------------------------------------------------------
    // 5. Ear Problems
    // ---------------------------------------------------------------------

    @Test
    fun ear_tenderSwellingBehindEar_isMastoiditis() {
        val patient = PatientAssessment(ageInMonths = 24, tenderSwellingBehindEar = true)
        assertEquals("MASTOIDITIS", engine.assess(patient).first)
    }

    @Test
    fun ear_dischargeUnder14Days_isAcuteEarInfection() {
        val patient = PatientAssessment(ageInMonths = 24, earDischarge = true, earDischargeDurationDays = 5)
        assertEquals("ACUTE EAR INFECTION", engine.assess(patient).first)
    }

    @Test
    fun ear_dischargeExactly14Days_isChronicEarInfection() {
        val patient = PatientAssessment(ageInMonths = 24, earDischarge = true, earDischargeDurationDays = 14)
        assertEquals("CHRONIC EAR INFECTION", engine.assess(patient).first)
    }

    @Test
    fun ear_dischargeOver14Days_isChronicEarInfection() {
        val patient = PatientAssessment(ageInMonths = 24, earDischarge = true, earDischargeDurationDays = 21)
        assertEquals("CHRONIC EAR INFECTION", engine.assess(patient).first)
    }

    @Test
    fun ear_earPainOnly_isAcuteEarInfection() {
        val patient = PatientAssessment(ageInMonths = 24, hasEarPain = true)
        assertEquals("ACUTE EAR INFECTION", engine.assess(patient).first)
    }

    @Test
    fun ear_mastoiditisTakesPriorityOverChronicDischarge() {
        val patient = PatientAssessment(
            ageInMonths = 24,
            tenderSwellingBehindEar = true,
            earDischarge = true,
            earDischargeDurationDays = 30
        )
        assertEquals("MASTOIDITIS", engine.assess(patient).first)
    }

    @Test
    fun ear_noEarSymptoms_fallsThroughToMalnutritionOrDefault() {
        val patient = PatientAssessment(ageInMonths = 24)
        assertEquals("NO SPECIFIC ILLNESS", engine.assess(patient).first)
    }

    @Test
    fun ear_dischargeWithNoDurationRecorded_defaultsToAcute() {
        val patient = PatientAssessment(ageInMonths = 24, earDischarge = true)
        assertEquals("ACUTE EAR INFECTION", engine.assess(patient).first)
    }

    // ---------------------------------------------------------------------
    // 6. Malnutrition / Anaemia
    // ---------------------------------------------------------------------

    @Test
    fun malnutrition_visibleSevereWasting_isSevereAcuteMalnutrition() {
        val patient = PatientAssessment(ageInMonths = 24, visibleSevereWasting = true)
        assertEquals("SEVERE ACUTE MALNUTRITION", engine.assess(patient).first)
    }

    @Test
    fun malnutrition_bilateralPittingOedema_isSevereAcuteMalnutrition() {
        val patient = PatientAssessment(ageInMonths = 24, bilateralPittingOedema = true)
        assertEquals("SEVERE ACUTE MALNUTRITION", engine.assess(patient).first)
    }

    @Test
    fun malnutrition_muacBelow11point5_isSevereAcuteMalnutrition() {
        val patient = PatientAssessment(ageInMonths = 24, muacCm = 11.0f)
        assertEquals("SEVERE ACUTE MALNUTRITION", engine.assess(patient).first)
    }

    @Test
    fun malnutrition_muacBetween11point5And12point5_isModerateAcuteMalnutrition() {
        val patient = PatientAssessment(ageInMonths = 24, muacCm = 12.0f)
        assertEquals("MODERATE ACUTE MALNUTRITION / ANAEMIA", engine.assess(patient).first)
    }

    @Test
    fun malnutrition_palePalms_isModerateAcuteMalnutrition() {
        val patient = PatientAssessment(ageInMonths = 24, palePalms = true)
        assertEquals("MODERATE ACUTE MALNUTRITION / ANAEMIA", engine.assess(patient).first)
    }

    @Test
    fun malnutrition_muacAt12point5OrAbove_noOtherSigns_isNoSpecificIllness() {
        val patient = PatientAssessment(ageInMonths = 24, muacCm = 13.0f)
        assertEquals("NO SPECIFIC ILLNESS", engine.assess(patient).first)
    }

    @Test
    fun malnutrition_severeSignsTakePriorityOverModerateSigns() {
        val patient = PatientAssessment(ageInMonths = 24, visibleSevereWasting = true, palePalms = true)
        assertEquals("SEVERE ACUTE MALNUTRITION", engine.assess(patient).first)
    }

    @Test
    fun malnutrition_noSignsAtAll_isNoSpecificIllness() {
        val patient = PatientAssessment(ageInMonths = 24)
        assertEquals("NO SPECIFIC ILLNESS", engine.assess(patient).first)
    }

    // ---------------------------------------------------------------------
    // Cross-module priority ordering
    // ---------------------------------------------------------------------

    @Test
    fun priority_respiratoryTakesPrecedenceOverFever() {
        val patient = PatientAssessment(
            ageInMonths = 24,
            hasCough = true,
            chestIndrawing = true,
            hasFever = true
        )
        assertEquals("SEVERE PNEUMONIA", engine.assess(patient).first)
    }

    @Test
    fun priority_diarrhoeaTakesPrecedenceOverEarProblem() {
        val patient = PatientAssessment(
            ageInMonths = 24,
            hasDiarrhoea = true,
            skinPinchGoesBackSlowly = true,
            hasEarPain = true
        )
        assertEquals("SEVERE DEHYDRATION", engine.assess(patient).first)
    }

    @Test
    fun priority_earProblemTakesPrecedenceOverMalnutritionWhenNoHigherPriorityFindings() {
        val patient = PatientAssessment(
            ageInMonths = 24,
            tenderSwellingBehindEar = true,
            palePalms = true
        )
        assertEquals("MASTOIDITIS", engine.assess(patient).first)
    }

    @Test
    fun priority_malnutritionSurfacesWhenNoOtherFindingsPresent() {
        val patient = PatientAssessment(ageInMonths = 24, muacCm = 10.0f)
        assertEquals("SEVERE ACUTE MALNUTRITION", engine.assess(patient).first)
    }

    @Test
    fun priority_completelyHealthyChild_isNoSpecificIllness() {
        val patient = PatientAssessment(ageInMonths = 12)
        assertEquals("NO SPECIFIC ILLNESS", engine.assess(patient).first)
    }
}
