package com.unza.imci.decisionsupport

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.unza.imci.decisionsupport.data.model.PatientAssessment
import com.unza.imci.decisionsupport.domain.decision_engine.ImciDecisionEngine

class AssessmentViewModel : ViewModel() {

    private val engine = ImciDecisionEngine()

    // 1. Patient Info
    var ageInMonths by mutableIntStateOf(0)
    var patientId by mutableStateOf("")

    // 2. Danger Signs
    var cannotDrink by mutableStateOf(false)
    var vomitsEverything by mutableStateOf(false)
    var convulsions by mutableStateOf(false)
    var lethargic by mutableStateOf(false)

    // 3. Respiratory
    var hasCough by mutableStateOf(false)
    var breathingRate by mutableIntStateOf(0)
    var chestIndrawing by mutableStateOf(false)
    var hasDifficultBreathing by mutableStateOf(false)
    var stridorInCalmChild by mutableStateOf(false)

    // 4. Diarrhoea
    var hasDiarrhoea by mutableStateOf(false)
    var diarrhoeaDurationDays by mutableIntStateOf(0)
    var bloodInStool by mutableStateOf(false)
    var sunkenEyes by mutableStateOf(false)

    // 5. Classification Results
    var classificationTitle by mutableStateOf("")
    var treatmentText by mutableStateOf("")

    fun calculateResults() {
        val assessment = PatientAssessment(
            ageInMonths = ageInMonths,
            patientId = patientId,
            cannotDrinkOrBreastfeed = cannotDrink,
            vomitsEverything = vomitsEverything,
            convulsions = convulsions,
            lethargicOrUnconscious = lethargic,
            hasCough = hasCough,
            hasDifficultBreathing = hasDifficultBreathing,
            breathingRatePerMin = if (breathingRate == 0) null else breathingRate,
            chestIndrawing = chestIndrawing,
            stridorInCalmChild = stridorInCalmChild,
            hasDiarrhoea = hasDiarrhoea,
            diarrhoeaDurationDays = if (diarrhoeaDurationDays == 0) null else diarrhoeaDurationDays,
            bloodInStool = bloodInStool,
            sunkenEyes = sunkenEyes
        )

        val result = engine.assess(assessment)
        classificationTitle = result.first
        treatmentText = result.second
    }
}
