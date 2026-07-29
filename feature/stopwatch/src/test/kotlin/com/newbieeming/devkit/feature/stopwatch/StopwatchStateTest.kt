package com.newbieeming.devkit.feature.stopwatch

import org.junit.Assert.assertEquals
import org.junit.Test

class StopwatchStateTest {

    @Test
    fun `formats elapsed time below one hour`() {
        assertEquals("02:03.40", 123_400L.toStopwatchText())
    }

    @Test
    fun `formats elapsed time with hours`() {
        assertEquals("1:02:03.40", 3_723_400L.toStopwatchText())
    }

    @Test
    fun `clamps negative elapsed time`() {
        assertEquals("00:00.00", (-1L).toStopwatchText())
    }
}
