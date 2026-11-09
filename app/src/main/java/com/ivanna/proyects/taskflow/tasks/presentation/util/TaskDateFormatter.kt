package com.ivanna.proyects.taskflow.tasks.presentation.util

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

object TaskDateFormatter {

    private val apiOutputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
    private val spaceDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    private val spaceDateTimeSecondsFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    private val shortMonths = arrayOf(
        "ene",
        "feb",
        "mar",
        "abr",
        "may",
        "jun",
        "jul",
        "ago",
        "sep",
        "oct",
        "nov",
        "dic"
    )

    fun parseApiDateTime(raw: String): LocalDateTime? {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) {
            return null
        }
        return runCatching {
            OffsetDateTime.parse(trimmed)
                .atZoneSameInstant(ZoneId.systemDefault())
                .toLocalDateTime()
        }.getOrElse {
            val isoNormalized = trimmed.replace(' ', 'T')
            runCatching {
                LocalDateTime.parse(isoNormalized, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            }.getOrElse {
                runCatching {
                    LocalDateTime.parse(trimmed, spaceDateTimeSecondsFormatter)
                }.getOrElse {
                    runCatching {
                        LocalDateTime.parse(trimmed, spaceDateTimeFormatter)
                    }.getOrNull()
                }
            }
        }
    }

    fun formatToApi(date: LocalDate, time: LocalTime): String {
        return LocalDateTime.of(date, time)
            .withSecond(0)
            .withNano(0)
            .format(apiOutputFormatter)
    }

    fun formatShortForCard(raw: String): String {
        val parsed = parseApiDateTime(raw) ?: return raw
        return formatDate(parsed.toLocalDate())
    }

    fun formatForDisplay(raw: String): String {
        val parsed = parseApiDateTime(raw) ?: return raw
        return formatDateTimeForField(parsed.toLocalDate(), parsed.toLocalTime())
    }

    fun formatDateTimeForField(date: LocalDate, time: LocalTime): String {
        val hh = time.hour.toString().padStart(2, '0')
        val mm = time.minute.toString().padStart(2, '0')
        return "${formatDate(date)}, $hh:$mm"
    }

    private fun formatDate(date: LocalDate): String {
        val day = date.dayOfMonth.toString().padStart(2, '0')
        val month = shortMonths[date.monthValue - 1]
        return "$day $month ${date.year}"
    }

    fun toUtcMillis(date: LocalDate): Long {
        return date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    }

    fun fromUtcMillis(millis: Long): LocalDate {
        return Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
    }
}
