package com.unza.imci.decisionsupport.data.model

/**
 * Captures all symptoms and measurements entered by the CHW during an IMCI assessment.
 * Aligns with the WHO IMCI Chart Booklet (March 2014) assessment flow for children aged
 * 2 months up to 5 years.
 */
data class PatientAssessment(
    // Patient metadata
    val patientId: String = "",
    val ageInMonths: Int? = null,

    // General Danger Signs (Checked FIRST per IMCI)
    val cannotDrinkOrBreastfeed: Boolean = false,
    val vomitsEverything: Boolean = false,
    val convulsions: Boolean = false,
    val lethargicOrUnconscious: Boolean = false,

    // Cough or Difficult Breathing
    val hasCough: Boolean = false,
    val hasDifficultBreathing: Boolean = false,
    val breathingRatePerMin: Int? = null,
    val chestIndrawing: Boolean = false,
    val stridorInCalmChild: Boolean = false,

    // Diarrhoea
    val hasDiarrhoea: Boolean = false,
    val diarrhoeaDurationDays: Int? = null,
    val bloodInStool: Boolean = false,
    val restlessOrIrritable: Boolean = false,
    val sunkenEyes: Boolean = false,
    val drinkingPoorly: Boolean = false,          // "Not able to drink or drinking poorly" (severe sign)
    val drinksEagerlyThirsty: Boolean = false,    // "Drinks eagerly, thirsty" (some-dehydration sign)
    val skinPinchGoesBackSlowly: Boolean = false,
    val skinPinchGoesBackVerySlowly: Boolean = false,

    // Fever
    val hasFever: Boolean = false,
    val feverDurationDays: Int? = null,
    val malariaRiskArea: Boolean = true,          // All of Zambia is classed as a malaria risk area
    val malariaTestResult: MalariaTestResult = MalariaTestResult.NOT_DONE,
    val stiffNeck: Boolean = false,
    val runnyNose: Boolean = false,
    val measles: Boolean = false,
    val cloudingOfCornea: Boolean = false,
    val deepOrExtensiveMouthUlcers: Boolean = false,
    val mouthUlcers: Boolean = false,
    val pusDrainingFromEye: Boolean = false,

    // Ear Problems
    val hasEarPain: Boolean = false,
    val earDischarge: Boolean = false,
    val earDischargeDurationDays: Int? = null,
    val tenderSwellingBehindEar: Boolean = false,

    // Malnutrition / Anaemia
    val bilateralPittingOedema: Boolean = false,
    val muacCm: Float? = null,
    val appetiteTest: AppetiteTestResult = AppetiteTestResult.NOT_DONE,
    val somePalmarPallor: Boolean = false,
    val severePalmarPallor: Boolean = false
)

/** Result of a malaria rapid diagnostic test (RDT), if one was done. */
enum class MalariaTestResult { POSITIVE, NEGATIVE, NOT_DONE }

/** Result of the RUTF appetite test for a child with signs of severe acute malnutrition. */
enum class AppetiteTestResult { PASSED, FAILED, NOT_DONE }

/** Age range this app's Chart Booklet tables cover: 2 months up to 5 years. */
object ImciScope {
    const val MIN_AGE_MONTHS = 2
    const val MAX_AGE_MONTHS = 59
    fun isInScope(ageInMonths: Int) = ageInMonths in MIN_AGE_MONTHS..MAX_AGE_MONTHS
}

/**
 * Reads the MUAC text the CHW typed. A comma decimal is accepted (11,5 = 11.5). A value outside
 * 5–25 cm is almost always a wrong unit (e.g. mm), so it is treated as not measured.
 */
object MuacInput {
    const val MIN_CM = 5.0f
    const val MAX_CM = 25.0f

    private fun number(text: String): Float? = text.trim().replace(',', '.').toFloatOrNull()

    /** The MUAC in cm, or null if blank, not a number, or out of range. */
    fun parseCm(text: String): Float? = number(text)?.takeIf { it in MIN_CM..MAX_CM }

    /** True when a number was typed but it is outside the plausible range. */
    fun isOutOfRange(text: String): Boolean = number(text)?.let { it !in MIN_CM..MAX_CM } ?: false
}
