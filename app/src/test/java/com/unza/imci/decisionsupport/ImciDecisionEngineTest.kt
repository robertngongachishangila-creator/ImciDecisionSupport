package com.unza.imci.decisionsupport.domain.decision_engine

import com.unza.imci.decisionsupport.data.model.MalariaTestResult
import com.unza.imci.decisionsupport.data.model.PatientAssessment
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Local Unit Tests for the Decision Engine.
 * Validates WHO IMCI classification rules against the published Chart Booklet decision
 * tables (WHO, 2020), per the proposal's Evaluation Criteria: 50+ cases across all six
 * modules, 8+ per module, exercising severity boundaries (severe / moderate-non-severe / none).
 * IMCI is integrated, so each case checks the classification(s) for its module and the
 * final section checks children with several illnesses at once.
 * Runs on the local JVM (no emulator needed).
 */
class ImciDecisionEngineTest {

    private lateinit var engine: ImciDecisionEngine

    @Before
    fun setup() {
        engine = ImciDecisionEngine()
    }

    private fun titles(patient: PatientAssessment) = engine.assess(patient).titles

    private fun assertClassifiedAs(patient: PatientAssessment, vararg expected: String) {
        val actual = titles(patient)
        expected.forEach { assertTrue("Expected $it in $actual", it in actual) }
    }

    private fun assertNotClassifiedAs(patient: PatientAssessment, vararg unexpected: String) {
        val actual = titles(patient)
        unexpected.forEach { assertFalse("Did not expect $it in $actual", it in actual) }
    }

    // ---------------------------------------------------------------------
    // 1. General Danger Signs
    // ---------------------------------------------------------------------

    @Test
    fun dangerSigns_cannotDrinkOrBreastfeed_isSevereGeneralDisease() {
        assertClassifiedAs(PatientAssessment(ageInMonths = 10, cannotDrinkOrBreastfeed = true), "SEVERE GENERAL DISEASE")
    }

    @Test
    fun dangerSigns_vomitsEverything_isSevereGeneralDisease() {
        assertClassifiedAs(PatientAssessment(ageInMonths = 8, vomitsEverything = true), "SEVERE GENERAL DISEASE")
    }

    @Test
    fun dangerSigns_convulsions_isSevereGeneralDisease() {
        assertClassifiedAs(PatientAssessment(ageInMonths = 36, convulsions = true), "SEVERE GENERAL DISEASE")
    }

    @Test
    fun dangerSigns_lethargicOrUnconscious_isSevereGeneralDisease() {
        assertClassifiedAs(PatientAssessment(ageInMonths = 5, lethargicOrUnconscious = true), "SEVERE GENERAL DISEASE")
    }

    @Test
    fun dangerSigns_withCough_alsoClassifiesSeverePneumonia() {
        // A danger sign no longer hides the other modules: the child still gets a respiratory classification
        val patient = PatientAssessment(ageInMonths = 24, convulsions = true, hasCough = true)
        assertClassifiedAs(patient, "SEVERE GENERAL DISEASE", "SEVERE PNEUMONIA")
    }

    @Test
    fun dangerSigns_multipleSignsPresent_classifiedOnce() {
        val patient = PatientAssessment(ageInMonths = 18, vomitsEverything = true, convulsions = true, lethargicOrUnconscious = true)
        assertEquals(1, titles(patient).count { it == "SEVERE GENERAL DISEASE" })
    }

    @Test
    fun dangerSigns_none_notClassified() {
        assertNotClassifiedAs(PatientAssessment(ageInMonths = 24, hasCough = true), "SEVERE GENERAL DISEASE")
    }

    @Test
    fun dangerSigns_present_needsUrgentReferral() {
        assertTrue(engine.assess(PatientAssessment(ageInMonths = 12, convulsions = true)).needsUrgentReferral)
    }

    // ---------------------------------------------------------------------
    // 2. Cough / Difficult Breathing
    // ---------------------------------------------------------------------

    @Test
    fun respiratory_chestIndrawing_isSeverePneumonia() {
        assertClassifiedAs(PatientAssessment(ageInMonths = 18, hasCough = true, chestIndrawing = true), "SEVERE PNEUMONIA")
    }

    @Test
    fun respiratory_stridorInCalmChild_isSeverePneumonia() {
        assertClassifiedAs(PatientAssessment(ageInMonths = 18, hasDifficultBreathing = true, stridorInCalmChild = true), "SEVERE PNEUMONIA")
    }

    @Test
    fun respiratory_fastBreathing_under2Months_thresholdIs60() {
        assertClassifiedAs(PatientAssessment(ageInMonths = 1, hasCough = true, breathingRatePerMin = 60), "PNEUMONIA")
    }

    @Test
    fun respiratory_belowThreshold_under2Months_isNotFastBreathing() {
        assertClassifiedAs(PatientAssessment(ageInMonths = 1, hasCough = true, breathingRatePerMin = 59), "COUGH OR COLD")
    }

    @Test
    fun respiratory_fastBreathing_2to11Months_thresholdIs50() {
        assertClassifiedAs(PatientAssessment(ageInMonths = 6, hasCough = true, breathingRatePerMin = 50), "PNEUMONIA")
    }

    @Test
    fun respiratory_belowThreshold_2to11Months_isNotFastBreathing() {
        assertClassifiedAs(PatientAssessment(ageInMonths = 6, hasCough = true, breathingRatePerMin = 49), "COUGH OR COLD")
    }

    @Test
    fun respiratory_fastBreathing_12to59Months_thresholdIs40() {
        assertClassifiedAs(PatientAssessment(ageInMonths = 30, hasCough = true, breathingRatePerMin = 40), "PNEUMONIA")
    }

    @Test
    fun respiratory_belowThreshold_12to59Months_isNotFastBreathing() {
        assertClassifiedAs(PatientAssessment(ageInMonths = 30, hasCough = true, breathingRatePerMin = 39), "COUGH OR COLD")
    }

    @Test
    fun respiratory_noSevereSignsAndNormalRate_isCoughOrCold() {
        assertClassifiedAs(PatientAssessment(ageInMonths = 24, hasCough = true), "COUGH OR COLD")
    }

    @Test
    fun respiratory_noCoughOrDifficultBreathing_notAssessed() {
        assertNotClassifiedAs(
            PatientAssessment(ageInMonths = 24, breathingRatePerMin = 55),
            "SEVERE PNEUMONIA", "PNEUMONIA", "COUGH OR COLD"
        )
    }

    // ---------------------------------------------------------------------
    // 3. Diarrhoea (dehydration uses the chart's "two signs" rule)
    // ---------------------------------------------------------------------

    @Test
    fun diarrhoea_twoSevereSigns_isSevereDehydration() {
        val patient = PatientAssessment(ageInMonths = 20, hasDiarrhoea = true, sunkenEyes = true, skinPinchGoesBackVerySlowly = true)
        assertClassifiedAs(patient, "SEVERE DEHYDRATION")
    }

    @Test
    fun diarrhoea_lethargicFromDangerSigns_countsTowardsSevereDehydration() {
        val patient = PatientAssessment(ageInMonths = 20, hasDiarrhoea = true, lethargicOrUnconscious = true, drinkingPoorly = true)
        assertClassifiedAs(patient, "SEVERE DEHYDRATION")
    }

    @Test
    fun diarrhoea_twoSomeSigns_isSomeDehydration() {
        val patient = PatientAssessment(ageInMonths = 20, hasDiarrhoea = true, restlessOrIrritable = true, drinksEagerlyThirsty = true)
        assertClassifiedAs(patient, "SOME DEHYDRATION")
    }

    @Test
    fun diarrhoea_sunkenEyesAndSlowSkinPinch_isSomeDehydration() {
        val patient = PatientAssessment(ageInMonths = 20, hasDiarrhoea = true, sunkenEyes = true, skinPinchGoesBackSlowly = true)
        assertClassifiedAs(patient, "SOME DEHYDRATION")
    }

    @Test
    fun diarrhoea_singleSign_isNoDehydration() {
        // Boundary: one sign is not enough for either dehydration classification
        assertClassifiedAs(PatientAssessment(ageInMonths = 20, hasDiarrhoea = true, sunkenEyes = true), "NO DEHYDRATION")
    }

    @Test
    fun diarrhoea_noDehydrationSigns_isNoDehydration() {
        assertClassifiedAs(PatientAssessment(ageInMonths = 20, hasDiarrhoea = true), "NO DEHYDRATION")
    }

    @Test
    fun diarrhoea_bloodInStool_isDysenteryAlongsideDehydrationStatus() {
        assertClassifiedAs(PatientAssessment(ageInMonths = 20, hasDiarrhoea = true, bloodInStool = true), "DYSENTERY", "NO DEHYDRATION")
    }

    @Test
    fun diarrhoea_14DaysNoDehydration_isPersistentDiarrhoea() {
        assertClassifiedAs(PatientAssessment(ageInMonths = 20, hasDiarrhoea = true, diarrhoeaDurationDays = 14), "PERSISTENT DIARRHOEA")
    }

    @Test
    fun diarrhoea_13Days_isNotPersistent() {
        assertNotClassifiedAs(
            PatientAssessment(ageInMonths = 20, hasDiarrhoea = true, diarrhoeaDurationDays = 13),
            "PERSISTENT DIARRHOEA", "SEVERE PERSISTENT DIARRHOEA"
        )
    }

    @Test
    fun diarrhoea_14DaysWithDehydration_isSeverePersistentDiarrhoea() {
        val patient = PatientAssessment(
            ageInMonths = 20, hasDiarrhoea = true, diarrhoeaDurationDays = 15,
            restlessOrIrritable = true, sunkenEyes = true
        )
        assertClassifiedAs(patient, "SEVERE PERSISTENT DIARRHOEA", "SOME DEHYDRATION")
    }

    @Test
    fun diarrhoea_absent_notAssessed() {
        assertNotClassifiedAs(
            PatientAssessment(ageInMonths = 20, sunkenEyes = true, skinPinchGoesBackVerySlowly = true),
            "SEVERE DEHYDRATION", "SOME DEHYDRATION", "NO DEHYDRATION"
        )
    }

    // ---------------------------------------------------------------------
    // 4. Fever and Measles
    // ---------------------------------------------------------------------

    @Test
    fun fever_stiffNeck_isVerySevereFebrileDisease() {
        assertClassifiedAs(PatientAssessment(ageInMonths = 24, hasFever = true, stiffNeck = true), "VERY SEVERE FEBRILE DISEASE")
    }

    @Test
    fun fever_withDangerSign_isVerySevereFebrileDisease() {
        assertClassifiedAs(PatientAssessment(ageInMonths = 24, hasFever = true, convulsions = true), "VERY SEVERE FEBRILE DISEASE")
    }

    @Test
    fun fever_riskArea_rdtPositive_isMalaria() {
        val patient = PatientAssessment(ageInMonths = 24, hasFever = true, malariaTestResult = MalariaTestResult.POSITIVE)
        assertClassifiedAs(patient, "MALARIA")
    }

    @Test
    fun fever_riskArea_rdtNegative_isFeverNoMalaria() {
        val patient = PatientAssessment(ageInMonths = 24, hasFever = true, malariaTestResult = MalariaTestResult.NEGATIVE)
        assertClassifiedAs(patient, "FEVER: NO MALARIA")
    }

    @Test
    fun fever_riskArea_noTest_isMalaria() {
        assertClassifiedAs(PatientAssessment(ageInMonths = 24, hasFever = true), "MALARIA")
    }

    @Test
    fun fever_noRiskArea_runnyNose_isMalariaUnlikely() {
        val patient = PatientAssessment(ageInMonths = 24, hasFever = true, malariaRiskArea = false, runnyNose = true)
        assertClassifiedAs(patient, "FEVER: MALARIA UNLIKELY")
    }

    @Test
    fun fever_noRiskArea_noOtherCause_isMalaria() {
        assertClassifiedAs(PatientAssessment(ageInMonths = 24, hasFever = true, malariaRiskArea = false), "MALARIA")
    }

    @Test
    fun fever_moreThan7Days_advisesReferralForAssessment() {
        val result = engine.assess(PatientAssessment(ageInMonths = 24, hasFever = true, feverDurationDays = 8))
        assertTrue(result.classifications.first { it.module == ImciModule.FEVER }.treatment.contains("refer for assessment"))
    }

    @Test
    fun fever_measlesNoComplications_isMeasles() {
        assertClassifiedAs(PatientAssessment(ageInMonths = 24, hasFever = true, measles = true), "MEASLES")
    }

    @Test
    fun fever_measlesWithCloudingOfCornea_isSevereComplicatedMeasles() {
        val patient = PatientAssessment(ageInMonths = 24, hasFever = true, measles = true, cloudingOfCornea = true)
        assertClassifiedAs(patient, "SEVERE COMPLICATED MEASLES")
    }

    @Test
    fun fever_measlesWithDeepMouthUlcers_isSevereComplicatedMeasles() {
        val patient = PatientAssessment(ageInMonths = 24, hasFever = true, measles = true, deepOrExtensiveMouthUlcers = true)
        assertClassifiedAs(patient, "SEVERE COMPLICATED MEASLES")
    }

    @Test
    fun fever_measlesWithPusDrainingFromEye_isMeaslesWithEyeOrMouthComplications() {
        val patient = PatientAssessment(ageInMonths = 24, hasFever = true, measles = true, pusDrainingFromEye = true)
        assertClassifiedAs(patient, "MEASLES WITH EYE OR MOUTH COMPLICATIONS")
    }

    @Test
    fun fever_measlesWithMouthUlcers_isMeaslesWithEyeOrMouthComplications() {
        val patient = PatientAssessment(ageInMonths = 24, hasFever = true, measles = true, mouthUlcers = true)
        assertClassifiedAs(patient, "MEASLES WITH EYE OR MOUTH COMPLICATIONS")
    }

    @Test
    fun fever_stiffNeckAndMeasles_bothClassified() {
        val patient = PatientAssessment(ageInMonths = 24, hasFever = true, stiffNeck = true, measles = true)
        assertClassifiedAs(patient, "VERY SEVERE FEBRILE DISEASE", "MEASLES")
    }

    @Test
    fun fever_absent_notAssessed() {
        assertNotClassifiedAs(PatientAssessment(ageInMonths = 24, stiffNeck = true), "VERY SEVERE FEBRILE DISEASE", "MALARIA")
    }

    // ---------------------------------------------------------------------
    // 5. Ear Problems
    // ---------------------------------------------------------------------

    @Test
    fun ear_tenderSwellingBehindEar_isMastoiditis() {
        assertClassifiedAs(PatientAssessment(ageInMonths = 30, tenderSwellingBehindEar = true), "MASTOIDITIS")
    }

    @Test
    fun ear_dischargeUnder14Days_isAcuteEarInfection() {
        assertClassifiedAs(PatientAssessment(ageInMonths = 30, earDischarge = true, earDischargeDurationDays = 13), "ACUTE EAR INFECTION")
    }

    @Test
    fun ear_dischargeExactly14Days_isChronicEarInfection() {
        assertClassifiedAs(PatientAssessment(ageInMonths = 30, earDischarge = true, earDischargeDurationDays = 14), "CHRONIC EAR INFECTION")
    }

    @Test
    fun ear_dischargeOver14Days_isChronicEarInfection() {
        assertClassifiedAs(PatientAssessment(ageInMonths = 30, earDischarge = true, earDischargeDurationDays = 21), "CHRONIC EAR INFECTION")
    }

    @Test
    fun ear_earPainOnly_isAcuteEarInfection() {
        assertClassifiedAs(PatientAssessment(ageInMonths = 30, hasEarPain = true), "ACUTE EAR INFECTION")
    }

    @Test
    fun ear_mastoiditisTakesPriorityOverChronicDischarge() {
        val patient = PatientAssessment(
            ageInMonths = 30, tenderSwellingBehindEar = true, earDischarge = true, earDischargeDurationDays = 20
        )
        assertClassifiedAs(patient, "MASTOIDITIS")
        assertNotClassifiedAs(patient, "CHRONIC EAR INFECTION")
    }

    @Test
    fun ear_noEarSymptoms_notAssessed() {
        assertNotClassifiedAs(
            PatientAssessment(ageInMonths = 30),
            "MASTOIDITIS", "CHRONIC EAR INFECTION", "ACUTE EAR INFECTION"
        )
    }

    @Test
    fun ear_dischargeWithNoDurationRecorded_defaultsToAcute() {
        assertClassifiedAs(PatientAssessment(ageInMonths = 30, earDischarge = true), "ACUTE EAR INFECTION")
    }

    // ---------------------------------------------------------------------
    // 6. Malnutrition and Anaemia
    // ---------------------------------------------------------------------

    @Test
    fun malnutrition_visibleSevereWasting_isSevereAcuteMalnutrition() {
        assertClassifiedAs(PatientAssessment(ageInMonths = 24, visibleSevereWasting = true), "SEVERE ACUTE MALNUTRITION")
    }

    @Test
    fun malnutrition_bilateralPittingOedema_isSevereAcuteMalnutrition() {
        assertClassifiedAs(PatientAssessment(ageInMonths = 24, bilateralPittingOedema = true), "SEVERE ACUTE MALNUTRITION")
    }

    @Test
    fun malnutrition_muacBelow11point5_isSevereAcuteMalnutrition() {
        assertClassifiedAs(PatientAssessment(ageInMonths = 24, muacCm = 11.4f), "SEVERE ACUTE MALNUTRITION")
    }

    @Test
    fun malnutrition_muacExactly11point5_isModerateAcuteMalnutrition() {
        assertClassifiedAs(PatientAssessment(ageInMonths = 24, muacCm = 11.5f), "MODERATE ACUTE MALNUTRITION")
    }

    @Test
    fun malnutrition_muacAt12point5OrAbove_noOtherSigns_isNoSpecificIllness() {
        assertEquals(listOf("NO SPECIFIC ILLNESS"), titles(PatientAssessment(ageInMonths = 24, muacCm = 12.5f)))
    }

    @Test
    fun malnutrition_muacIgnoredUnder6Months() {
        assertNotClassifiedAs(PatientAssessment(ageInMonths = 4, muacCm = 11.0f), "SEVERE ACUTE MALNUTRITION")
    }

    @Test
    fun anaemia_somePalmarPallor_isAnaemia() {
        assertClassifiedAs(PatientAssessment(ageInMonths = 24, somePalmarPallor = true), "ANAEMIA")
    }

    @Test
    fun anaemia_severePalmarPallor_isSevereAnaemia() {
        assertClassifiedAs(PatientAssessment(ageInMonths = 24, severePalmarPallor = true), "SEVERE ANAEMIA")
    }

    @Test
    fun malnutritionAndAnaemia_classifiedSeparately() {
        assertClassifiedAs(PatientAssessment(ageInMonths = 24, muacCm = 12.0f, somePalmarPallor = true), "MODERATE ACUTE MALNUTRITION", "ANAEMIA")
    }

    @Test
    fun malnutrition_noSignsAtAll_isNoSpecificIllness() {
        assertEquals(listOf("NO SPECIFIC ILLNESS"), titles(PatientAssessment(ageInMonths = 24)))
    }

    // ---------------------------------------------------------------------
    // 7. Integrated assessment: several illnesses in one child
    // ---------------------------------------------------------------------

    @Test
    fun integrated_coughWithSevereDehydration_bothClassified() {
        // Previously the cough result hid the dehydration and the urgent referral was missed
        val patient = PatientAssessment(
            ageInMonths = 18, hasCough = true,
            hasDiarrhoea = true, sunkenEyes = true, skinPinchGoesBackVerySlowly = true
        )
        val result = engine.assess(patient)
        assertTrue(result.titles.containsAll(listOf("COUGH OR COLD", "SEVERE DEHYDRATION")))
        assertTrue(result.needsUrgentReferral)
    }

    @Test
    fun integrated_pneumoniaDiarrhoeaMalaria_allClassified() {
        val patient = PatientAssessment(
            ageInMonths = 30, hasCough = true, breathingRatePerMin = 45,
            hasDiarrhoea = true, restlessOrIrritable = true, drinksEagerlyThirsty = true,
            hasFever = true, malariaTestResult = MalariaTestResult.POSITIVE
        )
        assertClassifiedAs(patient, "PNEUMONIA", "SOME DEHYDRATION", "MALARIA")
    }

    @Test
    fun integrated_malnutritionSurfacesAlongsideOtherIllness() {
        val patient = PatientAssessment(ageInMonths = 24, hasEarPain = true, muacCm = 11.0f)
        assertClassifiedAs(patient, "ACUTE EAR INFECTION", "SEVERE ACUTE MALNUTRITION")
    }

    @Test
    fun integrated_mostSevereClassificationListedFirst() {
        val patient = PatientAssessment(ageInMonths = 24, hasCough = true, tenderSwellingBehindEar = true)
        assertEquals(listOf("MASTOIDITIS", "COUGH OR COLD"), titles(patient))
    }

    @Test
    fun integrated_sameSeverityKeepsImciOrder() {
        val patient = PatientAssessment(
            ageInMonths = 24, hasCough = true, breathingRatePerMin = 45,
            hasFever = true, malariaTestResult = MalariaTestResult.POSITIVE
        )
        assertEquals(listOf("PNEUMONIA", "MALARIA"), titles(patient))
    }

    @Test
    fun integrated_noUrgentReferralWhenNoSevereClassification() {
        assertFalse(engine.assess(PatientAssessment(ageInMonths = 24, hasCough = true)).needsUrgentReferral)
    }

    @Test
    fun integrated_completelyHealthyChild_isNoSpecificIllness() {
        val result = engine.assess(PatientAssessment(ageInMonths = 24, muacCm = 14.0f))
        assertEquals(listOf("NO SPECIFIC ILLNESS"), result.titles)
        assertEquals(Severity.MILD, result.classifications.single().severity)
    }
}
