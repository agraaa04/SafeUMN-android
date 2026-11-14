package org.umn.projectuts.data

import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage
import io.ktor.http.ContentType
import org.umn.projectuts.data.remote.EvidenceInsert
import org.umn.projectuts.data.remote.EvidenceRow
import org.umn.projectuts.data.remote.InsertedReport
import org.umn.projectuts.data.remote.ReportInsert
import org.umn.projectuts.data.remote.ReportRow
import org.umn.projectuts.data.remote.SupabaseConfig
import org.umn.projectuts.util.DateUtils

class ReportRepository {

    private val client = SupabaseConfig.client

    /** Ambil laporan milik user yang sedang login dari view v_reports, terbaru di atas. */
    suspend fun getReports(): List<Report> =
        client.from("v_reports")
            .select {
                filter { eq("user_id", UserSession.requireUserId()) }
                order("created_at", Order.DESCENDING)
            }
            .decodeList<ReportRow>()
            .map { it.toReport() }

    /** Simpan laporan baru ke tbl_reports. Status otomatis DIPROSES dari database. */
    suspend fun createReport(
        category: ReportCategory,
        title: String,
        building: String,
        room: String,
        description: String,
        latitude: Double?,
        longitude: Double?,
        evidence: List<EvidenceItem>
    ): Report {
        val inserted = client.from("tbl_reports")
            .insert(
                ReportInsert(
                    userId = UserSession.requireUserId(),
                    category = category.name,
                    title = title,
                    building = building,
                    room = room,
                    description = description,
                    latitude = latitude,
                    longitude = longitude
                )
            ) {
                select()
            }
            .decodeSingle<InsertedReport>()

        val uploadedCount = uploadEvidence(inserted.reportId, evidence)

        return Report(
            id = formatCode(inserted.reportId),
            category = category,
            title = title,
            building = building,
            room = room,
            description = description,
            createdAt = DateUtils.parseTimestamp(inserted.createdAt),
            status = parseStatus(inserted.status),
            evidenceCount = uploadedCount,
            officerNote = inserted.officerNote
        )
    }

    /** Ambil URL publik semua foto bukti milik laporan (kode "RPT-0013" -> report_id 13). */
    suspend fun getEvidenceUrls(reportCode: String): List<String> {
        val reportId = reportCode.substringAfter("RPT-").toIntOrNull() ?: return emptyList()
        val bucket = client.storage.from(SupabaseConfig.EVIDENCE_BUCKET)
        return client.from("tbl_report_evidence")
            .select {
                filter { eq("report_id", reportId) }
                order("evidence_id", Order.ASCENDING)
            }
            .decodeList<EvidenceRow>()
            .map { bucket.publicUrl(it.imagePath) }
    }

    /**
     * Unggah setiap foto ke bucket report-evidence (folder report-<id>/),
     * lalu catat path-nya di tbl_report_evidence. Mengembalikan jumlah foto yang berhasil.
     */
    private suspend fun uploadEvidence(reportId: Int, evidence: List<EvidenceItem>): Int {
        val bucket = client.storage.from(SupabaseConfig.EVIDENCE_BUCKET)
        var uploaded = 0
        evidence.forEachIndexed { index, item ->
            try {
                val path = "report-$reportId/${System.currentTimeMillis()}_${index + 1}.jpg"
                bucket.upload(path, item.file.readBytes()) {
                    upsert = false
                    contentType = ContentType.Image.JPEG
                }
                client.from("tbl_report_evidence").insert(
                    EvidenceInsert(
                        reportId = reportId,
                        imagePath = path,
                        source = if (item.fromCamera) "CAMERA" else "GALLERY"
                    )
                )
                uploaded += 1
            } catch (e: Exception) {
                if (e is kotlin.coroutines.cancellation.CancellationException) throw e
                // Foto ini gagal diunggah; laporan tetap tersimpan.
            }
        }
        return uploaded
    }

    private fun ReportRow.toReport() = Report(
        id = reportCode,
        category = runCatching { ReportCategory.valueOf(category) }.getOrDefault(ReportCategory.LAINNYA),
        title = title,
        building = building,
        room = room,
        description = description,
        createdAt = DateUtils.parseTimestamp(createdAt),
        status = parseStatus(status),
        evidenceCount = evidenceCount,
        officerNote = officerNote
    )

    private fun formatCode(id: Int): String = "RPT-" + id.toString().padStart(4, '0')

    private fun parseStatus(value: String): ReportStatus =
        runCatching { ReportStatus.valueOf(value) }.getOrDefault(ReportStatus.DIPROSES)

}
