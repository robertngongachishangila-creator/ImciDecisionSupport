package com.unza.imci.decisionsupport.domain.decision_engine

import com.unza.imci.decisionsupport.data.model.PatientAssessment
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * Local Unit Tests for the Decision Engine.
 * Validates WHO IMCI classification rules.
 * Runs on the local JVM (No emulator needed!).
 */
class ImciDecisionEngineTest {

    private lateinit var engine: ImciDecisionEngine

    @Before
    fun setup() {
        engine = ImciDecisionEngine()
    }

    @Test
    fun testSevereGeneralDisease() {
        // Scenario: Child cannot drink
        val patient = PatientAssessment(
            ageInMonths = 24,
            cannotDrinkOrBreastfeed = true
        )
        val result = engine.assess(patient)
        assertEquals("SEVERE GENERAL DISEASE", result.first)
    }

    @Test
    fun testPneumonia_FastBreathing() {
        // Scenario: 2 year old (24 months) breathing 45 times/min
        // Threshold for 12-59 months is >= 40
        val patient = PatientAssessment(
            ageInMonths = 24,
            breathingRatePerMin = 45,
            hasCough = true
        )
        val result = engine.assess(patient)
        assertEquals("PNEUMONIA", result.first)
    }

    @Test
    fun testNoIllness() {
        // Scenario: Healthy child
        val patient = PatientAssessment(ageInMonths = 12)
        val result = engine.assess(patient)
        assertEquals("NO SPECIFIC ILLNESS", result.first)
    }
}