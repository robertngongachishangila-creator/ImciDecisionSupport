package com.unza.imci.decisionsupport.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Parsing and range checks for the MUAC text field. Runs on the local JVM. */
class MuacInputTest {

    @Test
    fun parse_dotDecimal() {
        assertEquals(11.5f, MuacInput.parseCm("11.5")!!, 0.0001f)
    }

    @Test
    fun parse_commaDecimal() {
        assertEquals(11.5f, MuacInput.parseCm("11,5")!!, 0.0001f)
    }

    @Test
    fun parse_surroundingSpacesIgnored() {
        assertEquals(12.0f, MuacInput.parseCm(" 12 ")!!, 0.0001f)
    }

    @Test
    fun parse_blank_isNotMeasured() {
        assertNull(MuacInput.parseCm(""))
        assertFalse(MuacInput.isOutOfRange(""))
    }

    @Test
    fun parse_notANumber_isNotMeasured() {
        assertNull(MuacInput.parseCm("abc"))
        assertFalse(MuacInput.isOutOfRange("abc"))
    }

    @Test
    fun range_lowerBoundary5_isAccepted() {
        assertEquals(5.0f, MuacInput.parseCm("5")!!, 0.0001f)
        assertFalse(MuacInput.isOutOfRange("5"))
    }

    @Test
    fun range_upperBoundary25_isAccepted() {
        assertEquals(25.0f, MuacInput.parseCm("25,0")!!, 0.0001f)
        assertFalse(MuacInput.isOutOfRange("25,0"))
    }

    @Test
    fun range_justBelow5_isOutOfRangeAndNotMeasured() {
        assertNull(MuacInput.parseCm("4.9"))
        assertTrue(MuacInput.isOutOfRange("4.9"))
    }

    @Test
    fun range_justAbove25_isOutOfRangeAndNotMeasured() {
        assertNull(MuacInput.parseCm("25.1"))
        assertTrue(MuacInput.isOutOfRange("25.1"))
    }

    @Test
    fun range_valueInMillimetres_isOutOfRangeAndNotMeasured() {
        assertNull(MuacInput.parseCm("115"))
        assertTrue(MuacInput.isOutOfRange("115"))
    }
}
