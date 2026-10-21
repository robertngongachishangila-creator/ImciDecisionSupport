package com.unza.imci.decisionsupport.domain.decision_engine

import com.unza.imci.decisionsupport.data.model.AppetiteTestResult
import com.unza.imci.decisionsupport.data.model.ImciScope
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
 * tables (WHO, March 2014), per the proposal's Evaluation Criteria: 50+ cases across all six
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
        assertClassifiedAs(PatientAssessment(ageInMonths = 10, cannotDrinkOrBreastfeed = true), "VERY SEVERE DISEASE")
    }

    @Test
    fun dangerSigns_vomitsEverything_isSevereGeneralDisease() {
        assertClassifiedAs(PatientAssessment(ageInMonths = 8, vomitsEverything = true), "VERY SEVERE DISEASE")
    }

    @Test
    fun dangerSigns_convulsions_isSevereGeneralDisease() {
        assertClassifiedAs(PatientAssessment(ageInMonths = 36, convulsions = true), "VERY SEVERE DISEASE")
    }

    @Test
    fun dangerSigns_lethargicOrUnconscious_isSevereGeneralDisease() {
        assertClassifiedAs(PatientAssessment(ageInMonths = 5, lethargicOrUnconscious = true), "VERY SEVERE DISEASE")
    }

    @Test
    fun dangerSigns_withCough_alsoClassifiesSeverePneumoniaOrVerySevereDisease() {
        // A danger sign no longer hides the other modules: the child still gets a respiratory classification
        val patient = PatientAssessment(ageInMonths = 24, convulsions = true, hasCough = true)
        assertClassifiedAs(patient, "VERY SEVERE DISEASE", "SEVERE PNEUMONIA OR VERY SEVERE DISEASE")
    }

    @Test
    fun dangerSigns_multipleSignsPresent_classifiedOnce() {
        val patient = PatientAssessment(ageInMonths = 18, vomitsEverything = true, convulsions = true, lethargicOrUnconscious = true)
        assertEquals(1, titles(patient).count { it == "VERY SEVERE DISEASE" })
    }

    @Test
    fun dangerSigns_none_notClassified() {
        assertNotClassifiedAs(PatientAssessment(ageInMonths = 24, hasCough = true), "VERY SEVERE DISEASE")
    }

    @Test
    fun dangerSigns_present_needsUrgentReferral() {
        assertTrue(engine.assess(PatientAssessment(ageInMonths = 12, convulsions = true)).needsUrgentReferral)
    }

    // ---------------------------------------------------------------------
    // 2. Cough / Difficult Breathing
    // ---------------------------------------------------------------------

    @Test
    fun respiratory_chestIndrawing_isPneumonia() {
        val patient = PatientAssessment(ageInMonths = 18, hasCough = true, chestIndrawing = true)
        assertClassifiedAs(patient, "PNEUMONIA")
        assertNotClassifiedAs(patient, "SEVERE PNEUMONIA OR VERY SEVERE DISEASE")
    }

    @Test
    fun respiratory_stridorInCalmChild_isSeverePneumoniaOrVerySevereDisease() {
        assertClassifiedAs(
            PatientAssessment(ageInMonths = 18, hasDifficultBreathing = true, stridorInCalmChild = true),
            "SEVERE PNEUMONIA OR VERY SEVERE DISEASE"
        )
    }

    @Test
    fun respiratory_fastBreathing_at2Months_thresholdIs50() {
        assertClassifiedAs(PatientAssessment(ageInMonths = 2, hasCough = true, breathingRatePerMin = 50), "PNEUMONIA")
    }

    @Test
    fun respiratory_belowThreshold_at2Months_isNotFastBreathing() {
        assertClassifiedAs(PatientAssessment(ageInMonths = 2, hasCough = true, breathingRatePerMin = 49), "COUGH OR COLD")
    }

    @Test
    fun respiratory_fastBreathing_at11Months_thresholdIs50() {
        assertClassifiedAs(PatientAssessment(ageInMonths = 11, hasCough = true, breathingRatePerMin = 50), "PNEUMONIA")
    }

    @Test
    fun respiratory_belowThreshold_at11Months_isNotFastBreathing() {
        assertClassifiedAs(PatientAssessment(ageInMonths = 11, hasCough = true, breathingRatePerMin = 49), "COUGH OR COLD")
    }

    @Test
    fun respiratory_fastBreathing_at12Months_thresholdIs40() {
        assertClassifiedAs(PatientAssessment(ageInMonths = 12, hasCough = true, breathingRatePerMin = 40), "PNEUMONIA")
    }

    @Test
    fun respiratory_belowThreshold_at12Months_isNotFastBreathing() {
        assertClassifiedAs(PatientAssessment(ageInMonths = 12, hasCough = true, breathingRatePerMin = 39), "COUGH OR COLD")
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
            "SEVERE PNEUMONIA OR VERY SEVERE DISEASE", "PNEUMONIA", "COUGH OR COLD"
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
    fun fever_noRiskArea_runnyNose_isFever() {
        val patient = PatientAssessment(ageInMonths = 24, hasFever = true, malariaRiskArea = false, runnyNose = true)
        assertClassifiedAs(patient, "FEVER")
    }

    @Test
    fun fever_noRiskArea_noTest_isFeverNotMalaria() {
        // Chart row: no malaria risk and no travel to a malaria area -> FEVER (green)
        val patient = PatientAssessment(ageInMonths = 24, hasFever = true, malariaRiskArea = false)
        assertClassifiedAs(patient, "FEVER")
        assertNotClassifiedAs(patient, "MALARIA")
    }

    @Test
    fun fever_noRiskArea_rdtNegative_noOtherCause_isFever() {
        // A negative RDT must never give MALARIA, even with no other cause of fever
        val patient = PatientAssessment(
            ageInMonths = 24, hasFever = true, malariaRiskArea = false, malariaTestResult = MalariaTestResult.NEGATIVE
        )
        assertClassifiedAs(patient, "FEVER")
        assertNotClassifiedAs(patient, "MALARIA")
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
    fun ear_noEarSigns_isNoEarInfection() {
        val result = engine.assess(PatientAssessment(ageInMonths = 30))
        val ear = result.classifications.single { it.module == ImciModule.EAR }
        assertEquals("NO EAR INFECTION", ear.title)
        assertEquals(Severity.MILD, ear.severity)
    }

    @Test
    fun ear_dischargeWithNoDurationRecorded_defaultsToAcute() {
        assertClassifiedAs(PatientAssessment(ageInMonths = 30, earDischarge = true), "ACUTE EAR INFECTION")
    }

    // ---------------------------------------------------------------------
    // 6. Malnutrition and Anaemia
    // ---------------------------------------------------------------------

    private val malnutritionTitles = arrayOf(
        "COMPLICATED SEVERE ACUTE MALNUTRITION", "UNCOMPLICATED SEVERE ACUTE MALNUTRITION",
        "MODERATE ACUTE MALNUTRITION", "NO ACUTE MALNUTRITION"
    )

    private fun malnutrition(patient: PatientAssessment) =
        engine.assess(patient).classifications.single { it.module == ImciModule.MALNUTRITION }

    @Test
    fun malnutrition_oedemaAppetiteNotDone_isComplicatedSam() {
        assertClassifiedAs(PatientAssessment(ageInMonths = 24, bilateralPittingOedema = true), "COMPLICATED SEVERE ACUTE MALNUTRITION")
    }

    @Test
    fun malnutrition_muacBelow11point5AppetiteNotDone_isComplicatedSam() {
        assertClassifiedAs(PatientAssessment(ageInMonths = 24, muacCm = 11.4f), "COMPLICATED SEVERE ACUTE MALNUTRITION")
    }

    @Test
    fun malnutrition_samAppetiteFailed_isComplicatedSam() {
        val result = malnutrition(PatientAssessment(ageInMonths = 24, muacCm = 11.0f, appetiteTest = AppetiteTestResult.FAILED))
        assertEquals("COMPLICATED SEVERE ACUTE MALNUTRITION", result.title)
        assertEquals(Severity.SEVERE, result.severity)
    }

    @Test
    fun malnutrition_samAppetitePassedNoComplication_isUncomplicatedSam() {
        val result = malnutrition(PatientAssessment(ageInMonths = 24, muacCm = 11.0f, appetiteTest = AppetiteTestResult.PASSED))
        assertEquals("UNCOMPLICATED SEVERE ACUTE MALNUTRITION", result.title)
        assertEquals(Severity.MODERATE, result.severity)
    }

    @Test
    fun malnutrition_oedemaAppetitePassedNoComplication_isComplicatedSam() {
        // Oedema of both feet appears only in the chart's pink row, so it is always complicated
        val patient = PatientAssessment(ageInMonths = 24, bilateralPittingOedema = true, appetiteTest = AppetiteTestResult.PASSED)
        assertEquals("COMPLICATED SEVERE ACUTE MALNUTRITION", malnutrition(patient).title)
    }

    @Test
    fun malnutrition_samAppetiteNotDone_isComplicatedSam() {
        val patient = PatientAssessment(ageInMonths = 24, muacCm = 11.0f, appetiteTest = AppetiteTestResult.NOT_DONE)
        assertEquals("COMPLICATED SEVERE ACUTE MALNUTRITION", malnutrition(patient).title)
    }

    @Test
    fun malnutrition_samAppetitePassedWithDangerSign_isComplicatedSam() {
        val patient = PatientAssessment(
            ageInMonths = 24, muacCm = 11.0f, appetiteTest = AppetiteTestResult.PASSED, vomitsEverything = true
        )
        assertEquals("COMPLICATED SEVERE ACUTE MALNUTRITION", malnutrition(patient).title)
    }

    @Test
    fun malnutrition_samAppetitePassedWithSevereDehydration_isComplicatedSam() {
        // A severe classification from another module counts as a medical complication
        val patient = PatientAssessment(
            ageInMonths = 24, muacCm = 11.0f, appetiteTest = AppetiteTestResult.PASSED,
            hasDiarrhoea = true, sunkenEyes = true, skinPinchGoesBackVerySlowly = true
        )
        assertClassifiedAs(patient, "SEVERE DEHYDRATION", "COMPLICATED SEVERE ACUTE MALNUTRITION")
    }

    @Test
    fun malnutrition_samAppetitePassedWithSevereAnaemia_isComplicatedSam() {
        val patient = PatientAssessment(
            ageInMonths = 24, muacCm = 11.0f, appetiteTest = AppetiteTestResult.PASSED, severePalmarPallor = true
        )
        assertClassifiedAs(patient, "SEVERE ANAEMIA", "COMPLICATED SEVERE ACUTE MALNUTRITION")
    }

    @Test
    fun malnutrition_samAppetitePassedNothingElse_isUncomplicatedSam() {
        val patient = PatientAssessment(ageInMonths = 24, muacCm = 11.0f, appetiteTest = AppetiteTestResult.PASSED)
        assertEquals(listOf("UNCOMPLICATED SEVERE ACUTE MALNUTRITION", "NO EAR INFECTION"), titles(patient))
    }

    @Test
    fun malnutrition_muacExactly11point5_isModerateAcuteMalnutrition() {
        assertClassifiedAs(PatientAssessment(ageInMonths = 24, muacCm = 11.5f), "MODERATE ACUTE MALNUTRITION")
    }

    @Test
    fun malnutrition_muacExactly12point5NoOedema_isNoAcuteMalnutrition() {
        val result = malnutrition(PatientAssessment(ageInMonths = 24, muacCm = 12.5f))
        assertEquals("NO ACUTE MALNUTRITION", result.title)
        assertEquals(Severity.MILD, result.severity)
    }

    @Test
    fun malnutrition_muacIgnoredUnder6Months() {
        assertNotClassifiedAs(PatientAssessment(ageInMonths = 4, muacCm = 11.0f), *malnutritionTitles)
    }

    @Test
    fun anaemia_somePalmarPallor_isAnaemia() {
        assertClassifiedAs(PatientAssessment(ageInMonths = 24, somePalmarPallor = true), "ANAEMIA")
    }

    private fun anaemiaTreatment(patient: PatientAssessment) =
        engine.assess(patient).classifications.single { it.title == "ANAEMIA" }.treatment

    @Test
    fun anaemia_withoutSam_givesIron() {
        assertTrue(anaemiaTreatment(PatientAssessment(ageInMonths = 24, somePalmarPallor = true)).startsWith("Give iron."))
    }

    @Test
    fun anaemia_withSam_doesNotGiveIron() {
        val treatment = anaemiaTreatment(PatientAssessment(ageInMonths = 24, muacCm = 11.0f, somePalmarPallor = true))
        assertTrue(treatment.contains("Do not give iron to a child on therapeutic feeding."))
        assertFalse(treatment.contains("Give iron"))
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
    fun malnutrition_noMuacAndNoOedema_notClassified() {
        assertNotClassifiedAs(PatientAssessment(ageInMonths = 24), *malnutritionTitles)
    }

    // ---------------------------------------------------------------------
    // Age scope: 2 months up to 5 years
    // ---------------------------------------------------------------------

    @Test
    fun scope_1Month_isOutOfScope() {
        assertFalse(ImciScope.isInScope(1))
    }

    @Test
    fun scope_2Months_isInScope() {
        assertTrue(ImciScope.isInScope(2))
    }

    @Test
    fun scope_59Months_isInScope() {
        assertTrue(ImciScope.isInScope(59))
    }

    @Test
    fun scope_60Months_isOutOfScope() {
        assertFalse(ImciScope.isInScope(60))
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
        assertClassifiedAs(patient, "ACUTE EAR INFECTION", "COMPLICATED SEVERE ACUTE MALNUTRITION")
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
        // Ear is now assessed for every child, so the green NO EAR INFECTION follows the yellow results
        assertEquals(listOf("PNEUMONIA", "MALARIA", "NO EAR INFECTION"), titles(patient))
    }

    @Test
    fun integrated_noUrgentReferralWhenNoSevereClassification() {
        assertFalse(engine.assess(PatientAssessment(ageInMonths = 24, hasCough = true)).needsUrgentReferral)
    }

    @Test
    fun integrated_completelyHealthyChild_isNoSpecificIllness() {
        // Only "no problem found" rows apply, so the child still gets the reassurance row
        val result = engine.assess(PatientAssessment(ageInMonths = 24, muacCm = 14.0f))
        assertEquals(listOf("NO EAR INFECTION", "NO ACUTE MALNUTRITION", "NO SPECIFIC ILLNESS"), result.titles)
        assertTrue(result.classifications.all { it.severity == Severity.MILD })
    }
}
