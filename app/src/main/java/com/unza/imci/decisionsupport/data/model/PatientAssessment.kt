package com.unza.imci.decisionsupport.data.model

/**
 * Captures all symptoms and measurements entered by the CHW during an IMCI assessment.
 * Aligns with WHO IMCI Chart Booklet (2020) assessment flow.
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
    val skinPinchGoesBackSlowly: Boolean = false,
    val sunkenEyes: Boolean = false,
    val veryThirstyOrDrinksPoorly: Boolean = false,

    // Fever
    val hasFever: Boolean = false,
    val feverDurationDays: Int? = null,
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
    val visibleSevereWasting: Boolean = false,
    val bilateralPittingOedema: Boolean = false,
    val muacCm: Float? = null,
    val palePalms: Boolean = false
)

