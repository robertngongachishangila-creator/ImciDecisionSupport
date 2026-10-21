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
import com.unza.imci.decisionsupport.data.model.AppetiteTestResult
import com.unza.imci.decisionsupport.data.model.AssessmentEntity
import com.unza.imci.decisionsupport.data.model.ClassificationEntity
import com.unza.imci.decisionsupport.data.model.MalariaTestResult
import com.unza.imci.decisionsupport.data.model.MuacInput
import com.unza.imci.decisionsupport.data.model.PatientAssessment
import com.unza.imci.decisionsupport.domain.decision_engine.AssessmentResult
import com.unza.imci.decisionsupport.domain.decision_engine.ImciDecisionEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.json.JSONObject

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
    var restlessOrIrritable by mutableStateOf(false)
    var sunkenEyes by mutableStateOf(false)
    var drinkingPoorly by mutableStateOf(false)
    var drinksEagerlyThirsty by mutableStateOf(false)
    var skinPinchSlowly by mutableStateOf(false)
    var skinPinchVerySlowly by mutableStateOf(false)

    // 5. Fever
    var hasFever by mutableStateOf(false)
    var feverDurationDays by mutableIntStateOf(0)
    var malariaRiskArea by mutableStateOf(true)
    var malariaTestResult by mutableStateOf(MalariaTestResult.NOT_DONE)
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
    var bilateralPittingOedema by mutableStateOf(false)
    var muacCm by mutableStateOf("")
    var appetiteTest by mutableStateOf(AppetiteTestResult.NOT_DONE)
    var somePalmarPallor by mutableStateOf(false)
    var severePalmarPallor by mutableStateOf(false)

    // 8. Classification Results
    var result by mutableStateOf<AssessmentResult?>(null)
        private set
    var isSaved by mutableStateOf(false)
        private set

    private fun buildPatientAssessment() = PatientAssessment(
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
        restlessOrIrritable = restlessOrIrritable,
        sunkenEyes = sunkenEyes,
        drinkingPoorly = drinkingPoorly,
        drinksEagerlyThirsty = drinksEagerlyThirsty,
        skinPinchGoesBackSlowly = skinPinchSlowly,
        skinPinchGoesBackVerySlowly = skinPinchVerySlowly,
        hasFever = hasFever,
        feverDurationDays = if (feverDurationDays == 0) null else feverDurationDays,
        malariaRiskArea = malariaRiskArea,
        malariaTestResult = malariaTestResult,
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
        bilateralPittingOedema = bilateralPittingOedema,
        muacCm = MuacInput.parseCm(muacCm),
        appetiteTest = appetiteTest,
        somePalmarPallor = somePalmarPallor,
        severePalmarPallor = severePalmarPallor
    )

    fun calculateResults() {
        result = engine.assess(buildPatientAssessment())
        isSaved = false
    }

    fun saveAssessment(onSuccess: () -> Unit) {
        val currentResult = result ?: return
        if (isSaved) return
        isSaved = true // Set before launching so a double tap cannot save the record twice

        viewModelScope.launch {
            repository.saveAssessment(
                AssessmentEntity(
                    patientId = patientId,
                    classification = currentResult.titles.joinToString(" + "),
                    treatmentRecommendation = currentResult.classifications.joinToString("\n") {
                        "${it.title}: ${it.treatment}"
                    },
                    assessmentJson = buildAssessmentJson(),
                    timestamp = System.currentTimeMillis()
                ),
                currentResult.classifications.map {
                    ClassificationEntity(
                        assessmentId = 0, // Assigned by the DAO once the assessment row exists
                        module = it.module.name,
                        title = it.title,
                        treatment = it.treatment,
                        severity = it.severity.name
                    )
                }
            )
            onSuccess()
        }
    }

    private fun buildAssessmentJson(): String {
        val a = buildPatientAssessment()
        return JSONObject()
            .put("ageInMonths", a.ageInMonths)
            .put("cannotDrinkOrBreastfeed", a.cannotDrinkOrBreastfeed)
            .put("vomitsEverything", a.vomitsEverything)
            .put("convulsions", a.convulsions)
            .put("lethargicOrUnconscious", a.lethargicOrUnconscious)
            .put("hasCough", a.hasCough)
            .put("hasDifficultBreathing", a.hasDifficultBreathing)
            .put("breathingRatePerMin", a.breathingRatePerMin ?: JSONObject.NULL)
            .put("chestIndrawing", a.chestIndrawing)
            .put("stridorInCalmChild", a.stridorInCalmChild)
            .put("hasDiarrhoea", a.hasDiarrhoea)
            .put("diarrhoeaDurationDays", a.diarrhoeaDurationDays ?: JSONObject.NULL)
            .put("bloodInStool", a.bloodInStool)
            .put("restlessOrIrritable", a.restlessOrIrritable)
            .put("sunkenEyes", a.sunkenEyes)
            .put("drinkingPoorly", a.drinkingPoorly)
            .put("drinksEagerlyThirsty", a.drinksEagerlyThirsty)
            .put("skinPinchGoesBackSlowly", a.skinPinchGoesBackSlowly)
            .put("skinPinchGoesBackVerySlowly", a.skinPinchGoesBackVerySlowly)
            .put("hasFever", a.hasFever)
            .put("feverDurationDays", a.feverDurationDays ?: JSONObject.NULL)
            .put("malariaRiskArea", a.malariaRiskArea)
            .put("malariaTestResult", a.malariaTestResult.name)
            .put("stiffNeck", a.stiffNeck)
            .put("runnyNose", a.runnyNose)
            .put("measles", a.measles)
            .put("cloudingOfCornea", a.cloudingOfCornea)
            .put("deepOrExtensiveMouthUlcers", a.deepOrExtensiveMouthUlcers)
            .put("mouthUlcers", a.mouthUlcers)
            .put("pusDrainingFromEye", a.pusDrainingFromEye)
            .put("hasEarPain", a.hasEarPain)
            .put("earDischarge", a.earDischarge)
            .put("earDischargeDurationDays", a.earDischargeDurationDays ?: JSONObject.NULL)
            .put("tenderSwellingBehindEar", a.tenderSwellingBehindEar)
            .put("bilateralPittingOedema", a.bilateralPittingOedema)
            .put("muacCm", a.muacCm?.toDouble() ?: JSONObject.NULL)
            .put("appetiteTest", a.appetiteTest.name)
            .put("somePalmarPallor", a.somePalmarPallor)
            .put("severePalmarPallor", a.severePalmarPallor)
            .toString(2)
    }

    fun getAllAssessments(): Flow<List<AssessmentEntity>> {
        return repository.getAllAssessments()
    }

    suspend fun getClassifications(assessmentId: Int): List<ClassificationEntity> {
        return repository.getClassifications(assessmentId)
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
        restlessOrIrritable = false
        sunkenEyes = false
        drinkingPoorly = false
        drinksEagerlyThirsty = false
        skinPinchSlowly = false
        skinPinchVerySlowly = false
        hasFever = false
        feverDurationDays = 0
        malariaRiskArea = true
        malariaTestResult = MalariaTestResult.NOT_DONE
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
        bilateralPittingOedema = false
        muacCm = ""
        appetiteTest = AppetiteTestResult.NOT_DONE
        somePalmarPallor = false
        severePalmarPallor = false
        result = null
        isSaved = false
    }
}
