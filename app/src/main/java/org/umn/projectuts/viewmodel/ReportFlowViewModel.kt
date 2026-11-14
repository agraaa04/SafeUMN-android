package org.umn.projectuts.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.umn.projectuts.data.EvidenceItem
import org.umn.projectuts.data.ReportCategory
import org.umn.projectuts.data.ReportRepository
import org.umn.projectuts.util.LocationResult
import java.io.File

class ReportFlowViewModel : ViewModel() {

    companion object {
        const val MAX_EVIDENCE = 4
        const val MAX_DESCRIPTION = 500
        const val MIN_DESCRIPTION = 10
    }

    var category by mutableStateOf<ReportCategory?>(null)
        private set

    var building by mutableStateOf("")
    var room by mutableStateOf("")
    var coordinates by mutableStateOf<String?>(null)
        private set
    var locationAttempted by mutableStateOf(false)
        private set

    var description by mutableStateOf("")
    var detailAttempted by mutableStateOf(false)
        private set
    val evidence = mutableStateListOf<EvidenceItem>()

    var isSubmitting by mutableStateOf(false)
        private set

    var submitError by mutableStateOf<String?>(null)
        private set

    private val repository = ReportRepository()

    private var evidenceCounter = 0

    val buildingError: String?
        get() = if (locationAttempted && building.isBlank()) "Gedung wajib diisi" else null

    val roomError: String?
        get() = if (locationAttempted && room.isBlank()) "Lantai/ruang wajib diisi" else null

    val descriptionError: String?
        get() = when {
            description.isBlank() -> if (detailAttempted) "Deskripsi wajib diisi" else null
            description.trim().length < MIN_DESCRIPTION -> "Deskripsi minimal $MIN_DESCRIPTION karakter"
            else -> null
        }

    fun selectCategory(value: ReportCategory) {
        category = value
    }

    fun onDescriptionChange(value: String) {
        description = value.take(MAX_DESCRIPTION)
    }

    var locationError by mutableStateOf<String?>(null)
        private set

    var isLocating by mutableStateOf(false)
        private set
    var locationAccuracy by mutableStateOf<Float?>(null)
        private set

    /** Dipanggil saat tombol GPS ditekan, sebelum izin & lokasi diproses. */
    fun startLocating() {
        isLocating = true
        locationError = null
    }

    /** Dipanggil setelah Fused Location Provider memberikan hasil. */
    fun onLocationResult(result: LocationResult) {
        isLocating = false
        when (result) {
            is LocationResult.Success -> {
                locationError = null
                locationAccuracy = result.location.accuracy
                coordinates = String.format(
                    java.util.Locale.US, "%.6f, %.6f",
                    result.location.latitude, result.location.longitude
                )
            }
            is LocationResult.Error -> locationError = result.message
        }
    }

    fun validateLocation(): Boolean {
        locationAttempted = true
        return buildingError == null && roomError == null
    }

    fun validateDetail(): Boolean {
        detailAttempted = true
        return descriptionError == null
    }

    /** Tambahkan foto bukti asli (file JPEG di cache) dari kamera atau galeri. */
    fun addEvidence(file: File, fromCamera: Boolean) {
        if (evidence.size >= MAX_EVIDENCE) {
            file.delete()
            return
        }
        evidenceCounter += 1
        val label = if (fromCamera) "Foto kamera $evidenceCounter" else "Galeri $evidenceCounter"
        evidence.add(EvidenceItem(evidenceCounter, label, fromCamera, file))
    }

    fun removeEvidence(id: Int) {
        evidence.filter { it.id == id }.forEach { it.file.delete() }
        evidence.removeAll { it.id == id }
    }

    fun submit(reportsViewModel: ReportsViewModel, onDone: (String) -> Unit) {
        if (isSubmitting) return
        viewModelScope.launch {
            isSubmitting = true
            submitError = null
            try {
                val firstLine = description.trim().lines().first()
                val title = if (firstLine.length > 50) firstLine.take(47).trimEnd() + "..." else firstLine
                val (lat, lng) = parseCoordinates(coordinates)
                val report = repository.createReport(
                    category = category ?: ReportCategory.LAINNYA,
                    title = title,
                    building = building.trim(),
                    room = room.trim(),
                    description = description.trim(),
                    latitude = lat,
                    longitude = lng,
                    evidence = evidence.toList()
                )
                reportsViewModel.addReport(report)
                onDone(report.id)
            } catch (e: Exception) {
                if (e is kotlin.coroutines.cancellation.CancellationException) throw e
                submitError = "Laporan gagal dikirim. Periksa koneksi internet lalu coba lagi."
            } finally {
                isSubmitting = false
            }
        }
    }

    /** "-6.2562, 106.6183" -> (-6.2562, 106.6183) */
    private fun parseCoordinates(value: String?): Pair<Double?, Double?> {
        val parts = value?.split(",")?.map { it.trim().toDoubleOrNull() } ?: return null to null
        return parts.getOrNull(0) to parts.getOrNull(1)
    }

    fun reset() {
        category = null
        building = ""
        room = ""
        coordinates = null
        locationError = null
        locationAccuracy = null
        isLocating = false
        locationAttempted = false
        description = ""
        detailAttempted = false
        evidence.forEach { it.file.delete() }
        evidence.clear()
        isSubmitting = false
        submitError = null
    }
}
