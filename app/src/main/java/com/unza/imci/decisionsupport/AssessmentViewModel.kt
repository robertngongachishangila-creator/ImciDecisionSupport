package com.unza.imci.decisionsupport

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.unza.imci.decisionsupport.data.database.AppDatabase
import com.unza.imci.decisionsupport.data.repository.AssessmentRepository
import com.unza.imci.decisionsupport.data.model.AssessmentEntity
import com.unza.imci.decisionsupport.data.model.PatientAssessment
import com.unza.imci.decisionsupport.domain.decision_engine.ImciDecisionEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class AssessmentViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AssessmentRepository(AppDatabase.getDatabase(application).assessmentDao())
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

    // 5. Fever
    var hasFever by mutableStateOf(false)
    var feverDurationDays by mutableIntStateOf(0)
    var stiffNeck by mutableStateOf(false)
    var runnyNose by mutableStateOf(false)
    var measles by mutableStateOf(false)
    var cloudingOfCornea by mutableStateOf(false)
    var deepOrExtensiveMouthUlcers by mutableStateOf(false)
    var mouthUlcers by mutableStateOf(false)
    var pusDrainingFromEye by mutableStateOf(false)

    // 6. Ear Problems
    var hasEarPain by mutableStateOf(false)
    var earDischarge by mutableStateOf(false)
    var earDischargeDurationDays by mutableIntStateOf(0)
    var tenderSwellingBehindEar by mutableStateOf(false)

    // 7. Malnutrition / Anaemia
    var visibleSevereWasting by mutableStateOf(false)
    var bilateralPittingOedema by mutableStateOf(false)
    var muacCm by mutableStateOf("")
    var palePalms by mutableStateOf(false)

    // 8. Classification Results
    var classificationTitle by mutableStateOf("")
    var treatmentText by mutableStateOf("")

    fun calculateResults() {
        val assessment = PatientAssessment(
            patientId = patientId,
            ageInMonths = ageInMonths,
            cannotDrinkOrBreastfeed = cannotDrink,
            vomitsEverything = vomitsEverything,
            convulsions = convulsions,
            lethargicOrUnconscious = lethargic,
            hasCough = hasCough,
            breathingRatePerMin = if (breathingRate == 0) null else breathingRate,
            chestIndrawing = chestIndrawing,
            hasDifficultBreathing = hasDifficultBreathing,
            stridorInCalmChild = stridorInCalmChild,
            hasDiarrhoea = hasDiarrhoea,
            diarrhoeaDurationDays = if (diarrhoeaDurationDays == 0) null else diarrhoeaDurationDays,
            bloodInStool = bloodInStool,
            sunkenEyes = sunkenEyes,
            hasFever = hasFever,
            feverDurationDays = if (feverDurationDays == 0) null else feverDurationDays,
            stiffNeck = stiffNeck,
            runnyNose = runnyNose,
            measles = measles,
            cloudingOfCornea = cloudingOfCornea,
            deepOrExtensiveMouthUlcers = deepOrExtensiveMouthUlcers,
            mouthUlcers = mouthUlcers,
            pusDrainingFromEye = pusDrainingFromEye,
            hasEarPain = hasEarPain,
            earDischarge = earDischarge,
            earDischargeDurationDays = if (earDischargeDurationDays == 0) null else earDischargeDurationDays,
            tenderSwellingBehindEar = tenderSwellingBehindEar,
            visibleSevereWasting = visibleSevereWasting,
            bilateralPittingOedema = bilateralPittingOedema,
            muacCm = muacCm.toFloatOrNull(),
            palePalms = palePalms
        )

        val result = engine.assess(assessment)
        classificationTitle = result.first
        treatmentText = result.second
    }

    fun saveAssessment(onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.saveAssessment(AssessmentEntity(
                patientId = patientId,
                classification = classificationTitle,
                treatmentRecommendation = treatmentText,
                assessmentJson = buildAssessmentJson(),
                timestamp = System.currentTimeMillis()
            ))
            onSuccess()
        }
    }

    private fun buildAssessmentJson(): String {
        return """
            {
              "ageInMonths": $ageInMonths,
              "cannotDrink": $cannotDrink,
              "vomitsEverything": $vomitsEverything,
              "convulsions": $convulsions,
              "lethargic": $lethargic,
              "hasCough": $hasCough,
              "breathingRate": $breathingRate,
              "chestIndrawing": $chestIndrawing,
              "hasDifficultBreathing": $hasDifficultBreathing,
              "stridorInCalmChild": $stridorInCalmChild,
              "hasDiarrhoea": $hasDiarrhoea,
              "diarrhoeaDurationDays": $diarrhoeaDurationDays,
              "bloodInStool": $bloodInStool,
              "sunkenEyes": $sunkenEyes,
              "hasFever": $hasFever,
              "feverDurationDays": $feverDurationDays,
              "stiffNeck": $stiffNeck,
              "runnyNose": $runnyNose,
              "measles": $measles,
              "cloudingOfCornea": $cloudingOfCornea,
              "deepOrExtensiveMouthUlcers": $deepOrExtensiveMouthUlcers,
              "mouthUlcers": $mouthUlcers,
              "pusDrainingFromEye": $pusDrainingFromEye,
              "hasEarPain": $hasEarPain,
              "earDischarge": $earDischarge,
              "earDischargeDurationDays": $earDischargeDurationDays,
              "tenderSwellingBehindEar": $tenderSwellingBehindEar,
              "visibleSevereWasting": $visibleSevereWasting,
              "bilateralPittingOedema": $bilateralPittingOedema,
              "muacCm": ${muacCm.toFloatOrNull()},
              "palePalms": $palePalms
            }
        """.trimIndent()
    }

    fun getAllAssessments(): Flow<List<AssessmentEntity>> {
        return repository.getAllAssessments()
    }

    suspend fun deleteAssessment(assessment: AssessmentEntity) {
        repository.deleteAssessment(assessment.id)
    }

    suspend fun deleteAllAssessments() {
        repository.deleteAllAssessments()
    }

    fun resetForm() {
        ageInMonths = 0
        patientId = ""
        cannotDrink = false
        vomitsEverything = false
        convulsions = false
        lethargic = false
        hasCough = false
        breathingRate = 0
        chestIndrawing = false
        hasDifficultBreathing = false
        stridorInCalmChild = false
        hasDiarrhoea = false
        diarrhoeaDurationDays = 0
        bloodInStool = false
        sunkenEyes = false
        hasFever = false
        feverDurationDays = 0
        stiffNeck = false
        runnyNose = false
        measles = false
        cloudingOfCornea = false
        deepOrExtensiveMouthUlcers = false
        mouthUlcers = false
        pusDrainingFromEye = false
        hasEarPain = false
        earDischarge = false
        earDischargeDurationDays = 0
        tenderSwellingBehindEar = false
        visibleSevereWasting = false
        bilateralPittingOedema = false
        muacCm = ""
        palePalms = false
        classificationTitle = ""
        treatmentText = ""
    }
}