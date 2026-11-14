package org.umn.projectuts.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Satu baris dari view public.v_reports */
@Serializable
data class ReportRow(
    @SerialName("report_id") val reportId: Int,
    @SerialName("report_code") val reportCode: String,
    val category: String,
    val title: String,
    val building: String,
    val room: String,
    val description: String,
    val status: String,
    @SerialName("officer_note") val officerNote: String? = null,
    @SerialName("created_at") val createdAt: String,
    @SerialName("evidence_count") val evidenceCount: Int = 0
)

/** Data yang dikirim saat membuat laporan baru ke public.tbl_reports */
@Serializable
data class ReportInsert(
    @SerialName("user_id") val userId: Int,
    val category: String,
    val title: String,
    val building: String,
    val room: String,
    val description: String,
    val latitude: Double? = null,
    val longitude: Double? = null
)

/** Satu baris dari public.tbl_report_evidence */
@Serializable
data class EvidenceRow(
    @SerialName("evidence_id") val evidenceId: Int,
    @SerialName("image_path") val imagePath: String
)

/** Data satu foto bukti untuk public.tbl_report_evidence */
@Serializable
data class EvidenceInsert(
    @SerialName("report_id") val reportId: Int,
    @SerialName("image_path") val imagePath: String,
    val source: String
)

/** Kolom yang dikembalikan Supabase setelah laporan berhasil dibuat */
@Serializable
data class InsertedReport(
    @SerialName("report_id") val reportId: Int,
    val status: String,
    @SerialName("officer_note") val officerNote: String? = null,
    @SerialName("created_at") val createdAt: String
)
