package org.umn.projectuts.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.umn.projectuts.data.Report
import org.umn.projectuts.data.ReportRepository
import org.umn.projectuts.data.ReportStatus
import org.umn.projectuts.data.TimeFilter
import org.umn.projectuts.data.UserSession
import java.time.LocalDateTime

class ReportsViewModel : ViewModel() {

    private val repository = ReportRepository()

    var reports by mutableStateOf<List<Report>>(emptyList())
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var statusFilter by mutableStateOf<ReportStatus?>(null)
    var isLoading by mutableStateOf(false)
        private set

    var query by mutableStateOf("")
    var timeFilter by mutableStateOf(TimeFilter.ALL)

    private var refreshJob: Job? = null

    // Foto bukti untuk halaman Detail Laporan
    var evidenceUrls by mutableStateOf<List<String>>(emptyList())
        private set
    var evidenceLoading by mutableStateOf(false)
        private set
    var evidenceError by mutableStateOf<String?>(null)
        private set
    private var evidenceJob: Job? = null

    val statusFiltered: List<Report>
        get() = reports
            .filter { statusFilter == null || it.status == statusFilter }
            .sortedByDescending { it.createdAt }

    /** Batas waktu paling lama untuk filter Riwayat (null = semua waktu). */
    val historyCutoff: LocalDateTime?
        get() {
            val now = LocalDateTime.now()
            return when (timeFilter) {
                TimeFilter.ONE_MONTH -> now.minusMonths(1)
                TimeFilter.THREE_MONTHS -> now.minusMonths(3)
                TimeFilter.SIX_MONTHS -> now.minusMonths(6)
                TimeFilter.ONE_YEAR -> now.minusYears(1)
                TimeFilter.ALL -> null
            }
        }

    val historyFiltered: List<Report>
        get() {
            val cutoff = historyCutoff
            val keyword = query.trim()
            return reports
                .filter { cutoff == null || it.createdAt.isAfter(cutoff) }
                .filter {
                    keyword.isEmpty() ||
                        it.title.contains(keyword, ignoreCase = true) ||
                        it.category.label.contains(keyword, ignoreCase = true) ||
                        it.building.contains(keyword, ignoreCase = true)
                }
                .sortedByDescending { it.createdAt }
        }

    fun countByStatus(status: ReportStatus?): Int =
        if (status == null) reports.size else reports.count { it.status == status }

    fun findById(id: String): Report? = reports.firstOrNull { it.id == id }

    fun addReport(report: Report) {
        reports = listOf(report) + reports
    }

    fun refresh() {
        if (UserSession.userId == null) return
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            isLoading = true
            try {
                reports = repository.getReports()
                errorMessage = null
            } catch (e: Exception) {
                if (e is kotlin.coroutines.cancellation.CancellationException) throw e
                errorMessage = "Gagal memuat laporan. Periksa koneksi internet."
            } finally {
                isLoading = false
            }
        }
    }

    fun loadEvidence(reportId: String) {
        evidenceJob?.cancel()
        evidenceUrls = emptyList()
        evidenceError = null
        evidenceJob = viewModelScope.launch {
            evidenceLoading = true
            try {
                evidenceUrls = repository.getEvidenceUrls(reportId)
            } catch (e: Exception) {
                if (e is kotlin.coroutines.cancellation.CancellationException) throw e
                evidenceError = "Foto bukti gagal dimuat. Periksa koneksi internet."
            } finally {
                evidenceLoading = false
            }
        }
    }

    /** Dipanggil saat logout supaya laporan akun sebelumnya tidak tampil. */
    fun clear() {
        refreshJob?.cancel()
        reports = emptyList()
        errorMessage = null
        isLoading = false
    }
}
