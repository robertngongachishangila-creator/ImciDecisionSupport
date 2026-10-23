package com.unza.imci.decisionsupport.performance

import android.os.Build
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.unza.imci.decisionsupport.data.model.AppetiteTestResult
import com.unza.imci.decisionsupport.data.model.MalariaTestResult
import com.unza.imci.decisionsupport.data.model.PatientAssessment
import com.unza.imci.decisionsupport.domain.decision_engine.ImciDecisionEngine
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Measurement (not a correctness test): how long one ImciDecisionEngine.assess() call takes.
 *
 * What is measured: ImciDecisionEngine.assess(), 1000 calls after 200 warm-up calls, on one
 * 18-month-old child positive in all six IMCI modules (9 classifications). System.nanoTime() is
 * read either side of each individual call; the average and the worst single call are logged
 * under the tag ENGINE_TIMING (adb logcat -d -s ENGINE_TIMING).
 *
 * Recorded results (23 October 2026), Android emulator sdk_gphone16k_x86_64, Android 17
 * (API 37), four runs: average 0.054–0.067 ms per call, worst single call 0.21–2.83 ms. The
 * average was stable across runs; the worst call varied more than tenfold between identical
 * runs, so it reflects GC and scheduler jitter rather than a slow path in the engine.
 *
 * These figures come from an emulator running on a desktop PC. That is not a physical device,
 * and it is not the minimum supported API 24 device either, so it says nothing about how the
 * engine performs on the low-end hardware this app targets. Treat it as an order of magnitude
 * only; a physical phone would need its own run.
 */
@RunWith(AndroidJUnit4::class)
class EngineTimingInstrumentedTest {

    @Test
    fun timeAssessOverOneThousandCalls() {
        val engine = ImciDecisionEngine()
        val patient = complexPatient()

        // Sanity check that the fixture really does exercise several modules
        val classifications = engine.assess(patient).classifications
        assertTrue(
            "Expected a multi-module case, got ${classifications.map { it.title }}",
            classifications.size >= 6
        )

        repeat(WARMUP_CALLS) { engine.assess(patient) }

        var totalNanos = 0L
        var worstNanos = 0L
        repeat(MEASURED_CALLS) {
            val start = System.nanoTime()
            engine.assess(patient)
            val elapsed = System.nanoTime() - start
            totalNanos += elapsed
            if (elapsed > worstNanos) worstNanos = elapsed
        }

        val averageMs = totalNanos / MEASURED_CALLS.toDouble() / 1_000_000.0
        val worstMs = worstNanos / 1_000_000.0
        Log.i(TAG, "target=DEVICE")
        Log.i(TAG, "model=${Build.MODEL} manufacturer=${Build.MANUFACTURER} device=${Build.DEVICE} " +
            "product=${Build.PRODUCT} hardware=${Build.HARDWARE}")
        Log.i(TAG, "android_release=${Build.VERSION.RELEASE} sdk_int=${Build.VERSION.SDK_INT} " +
            "fingerprint=${Build.FINGERPRINT}")
        Log.i(TAG, "classifications=${classifications.size} ${classifications.map { it.title }}")
        Log.i(TAG, "calls=$MEASURED_CALLS (after $WARMUP_CALLS warmup)")
        Log.i(TAG, "average_ms_per_call=${"%.6f".format(averageMs)}")
        Log.i(TAG, "worst_single_call_ms=${"%.6f".format(worstMs)}")
    }

    /** A child with cough, diarrhoea, fever/measles, an ear problem, malnutrition and anaemia. */
    private fun complexPatient() = PatientAssessment(
        patientId = "TIMING-001",
        ageInMonths = 18,
        hasCough = true,
        hasDifficultBreathing = true,
        breathingRatePerMin = 46,
        hasDiarrhoea = true,
        diarrhoeaDurationDays = 16,
        bloodInStool = true,
        restlessOrIrritable = true,
        sunkenEyes = true,
        drinksEagerlyThirsty = true,
        skinPinchGoesBackSlowly = true,
        hasFever = true,
        feverDurationDays = 9,
        malariaRiskArea = true,
        malariaTestResult = MalariaTestResult.POSITIVE,
        measles = true,
        pusDrainingFromEye = true,
        mouthUlcers = true,
        hasEarPain = true,
        earDischarge = true,
        earDischargeDurationDays = 20,
        muacCm = 11.2f,
        appetiteTest = AppetiteTestResult.FAILED,
        somePalmarPallor = true
    )

    private companion object {
        const val TAG = "ENGINE_TIMING"
        const val WARMUP_CALLS = 200
        const val MEASURED_CALLS = 1000
    }
}
