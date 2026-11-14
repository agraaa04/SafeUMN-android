package org.umn.projectuts.util

import java.time.Duration
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateUtils {
    private val locale: Locale = Locale.forLanguageTag("id-ID")
    private val fullFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy, HH:mm 'WIB'", locale)
    private val shortFormatter = DateTimeFormatter.ofPattern("d MMM yyyy | HH:mm 'WIB'", locale)
    private val monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", locale)

    fun formatFull(dateTime: LocalDateTime): String = dateTime.format(fullFormatter)
    fun formatShort(dateTime: LocalDateTime): String = dateTime.format(shortFormatter)
    fun formatMonth(dateTime: LocalDateTime): String = dateTime.format(monthFormatter)
    fun formatDate(dateTime: LocalDateTime): String =
        dateTime.format(DateTimeFormatter.ofPattern("d MMM yyyy", locale))

    /** Timestamp Supabase ("2026-11-14T02:24:51.261586+00:00") -> waktu lokal HP */
    fun parseTimestamp(value: String): LocalDateTime =
        OffsetDateTime.parse(value)
            .atZoneSameInstant(ZoneId.systemDefault())
            .toLocalDateTime()

    /** Contoh: "2 min ago", "3 hours ago", "Yesterday" */
    fun timeAgo(dateTime: LocalDateTime): String {
        val duration = Duration.between(dateTime, LocalDateTime.now())
        val minutes = duration.toMinutes()
        val hours = duration.toHours()
        val days = duration.toDays()
        return when {
            minutes < 1 -> "Just now"
            minutes < 60 -> "$minutes min ago"
            hours == 1L -> "1 hour ago"
            hours < 24 -> "$hours hours ago"
            days == 1L -> "Yesterday"
            days < 7 -> "$days days ago"
            else -> formatShort(dateTime)
        }
    }
}
