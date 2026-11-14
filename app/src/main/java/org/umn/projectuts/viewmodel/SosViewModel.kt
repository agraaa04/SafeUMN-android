package org.umn.projectuts.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.umn.projectuts.data.SosRepository
import org.umn.projectuts.util.LocationResult
import java.util.Locale

class SosViewModel : ViewModel() {

    private val repository = SosRepository()
    private var activeSosId: Int? = null

    var isActive by mutableStateOf(false)
        private set
    var isSending by mutableStateOf(false)
        private set
    var sentCoordinates by mutableStateOf<String?>(null)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    /** Dipanggil saat tombol SOS selesai ditahan 3 detik, sebelum lokasi diambil. */
    fun startLocating() {
        if (isActive || isSending) return
        isSending = true
        errorMessage = null
    }

    /** Dipanggil setelah Fused Location Provider memberikan hasil. */
    fun activate(result: LocationResult) {
        if (isActive) return
        val location = when (result) {
            is LocationResult.Success -> result.location
            is LocationResult.Error -> {
                errorMessage = "SOS gagal: ${result.message}"
                isSending = false
                return
            }
        }
        val latitude = location.latitude
        val longitude = location.longitude
        viewModelScope.launch {
            isSending = true
            errorMessage = null
            try {
                activeSosId = repository.sendSos(latitude, longitude)
                sentCoordinates = String.format(Locale.US, "%.6f, %.6f", latitude, longitude)
                isActive = true
            } catch (e: Exception) {
                if (e is kotlin.coroutines.cancellation.CancellationException) throw e
                errorMessage = "SOS gagal dikirim. Periksa koneksi internet lalu coba lagi."
            } finally {
                isSending = false
            }
        }
    }

    fun cancel(onDone: () -> Unit = {}) {
        val sosId = activeSosId
        isActive = false
        sentCoordinates = null
        activeSosId = null
        errorMessage = null
        if (sosId != null) {
            viewModelScope.launch {
                try {
                    repository.cancelSos(sosId)
                } catch (e: Exception) {
                    if (e is kotlin.coroutines.cancellation.CancellationException) throw e
                    errorMessage = "Pembatalan SOS gagal tersimpan. Periksa koneksi internet."
                }
                onDone()
            }
        } else {
            onDone()
        }
    }
}
