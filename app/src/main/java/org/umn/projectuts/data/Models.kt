package org.umn.projectuts.data

import java.io.File
import java.time.LocalDateTime

enum class ReportStatus(val label: String) {
    DIPROSES("Diproses"),
    SELESAI("Selesai"),
    DITOLAK("Ditolak")
}

enum class ReportCategory(val label: String, val subtitle: String) {
    KEAMANAN("Keamanan", "Security"),
    MEDIS("Medis", "Medical"),
    FASILITAS("Kerusakan Fasilitas", "Facilities"),
    KEHILANGAN("Kehilangan Barang", "Lost & Found"),
    PERUNDUNGAN("Perundungan", "Bullying"),
    KEKERASAN_SEKSUAL("Kekerasan Seksual", "Sexual Assault"),
    KEBAKARAN("Kebakaran", "Fire"),
    LAINNYA("Lainnya", "")
}

data class Report(
    val id: String,
    val category: ReportCategory,
    val title: String,
    val building: String,
    val room: String,
    val description: String,
    val createdAt: LocalDateTime,
    val status: ReportStatus,
    val evidenceCount: Int,
    val officerNote: String? = null
)

enum class AlertSeverity { EMERGENCY, SAFETY }

data class AlertItem(
    val id: String,
    val title: String,
    val message: String,
    val timeAgo: String,
    val severity: AlertSeverity
)

enum class AnnouncementIcon { PATROL, FIRE_DRILL, MAINTENANCE, PROCEDURE }

data class Announcement(
    val id: String,
    val title: String,
    val summary: String,
    val icon: AnnouncementIcon,
    val body: String
)

data class UserProfile(
    val name: String,
    val email: String,
    val nim: String,
    val phone: String
)

data class EvidenceItem(
    val id: Int,
    val label: String,
    val fromCamera: Boolean,
    val file: File
)

enum class TimeFilter(val label: String) {
    ONE_MONTH("1M"),
    THREE_MONTHS("3M"),
    SIX_MONTHS("6M"),
    ONE_YEAR("1T"),
    ALL("Semua")
}
