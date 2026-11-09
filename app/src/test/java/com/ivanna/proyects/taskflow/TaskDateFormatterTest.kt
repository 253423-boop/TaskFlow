package com.ivanna.proyects.taskflow

import com.ivanna.proyects.taskflow.tasks.presentation.util.TaskDateFormatter
import java.time.LocalDate
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class TaskDateFormatterTest {

    @Test
    fun formatToApiProducesIso8601Standard() {
        val formatted = TaskDateFormatter.formatToApi(
            date = LocalDate.of(2026, 11, 10),
            time = LocalTime.of(14, 5)
        )

        assertEquals("2026-11-10T14:05:00", formatted)
    }

    @Test
    fun parseApiDateTimeSupportsIsoLocalAndFractionalSeconds() {
        val parsedBasic = TaskDateFormatter.parseApiDateTime("2026-11-10T14:00:00")
        val parsedMicros = TaskDateFormatter.parseApiDateTime("2026-11-10T14:00:00.123456")
        val parsedSpaceMicros = TaskDateFormatter.parseApiDateTime("2026-11-10 14:00:00.123456")

        assertNotNull(parsedBasic)
        assertNotNull(parsedMicros)
        assertNotNull(parsedSpaceMicros)
        assertEquals(10, parsedBasic?.dayOfMonth)
        assertEquals(14, parsedMicros?.hour)
        assertEquals(14, parsedSpaceMicros?.hour)
    }

    @Test
    fun formatShortForCardProducesLowercaseSpanishMonthWithoutPeriod() {
        assertEquals("06 nov 2026", TaskDateFormatter.formatShortForCard("2026-11-06T23:59:00"))
        assertEquals("20 nov 2026", TaskDateFormatter.formatShortForCard("2026-11-20T23:59:00"))
        assertEquals("01 ene 2026", TaskDateFormatter.formatShortForCard("2026-01-01T08:00:00"))
    }

    @Test
    fun formatForDisplayProducesDateWithCommaAnd24HourTime() {
        assertEquals("05 nov 2026, 10:24", TaskDateFormatter.formatForDisplay("2026-11-05T10:24:00"))
        assertEquals("20 nov 2026, 23:59", TaskDateFormatter.formatForDisplay("2026-11-20T23:59:00"))
    }

    @Test
    fun utcMillisRoundTripPreservesExactLocalDate() {
        val original = LocalDate.of(2026, 11, 10)
        val millis = TaskDateFormatter.toUtcMillis(original)
        val roundTrip = TaskDateFormatter.fromUtcMillis(millis)

        assertEquals(original, roundTrip)
    }
}
