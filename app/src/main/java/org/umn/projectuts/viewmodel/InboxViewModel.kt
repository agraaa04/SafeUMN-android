package org.umn.projectuts.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.umn.projectuts.data.AlertItem
import org.umn.projectuts.data.Announcement
import org.umn.projectuts.data.InboxRepository

class InboxViewModel : ViewModel() {

    private val repository = InboxRepository()
    private var refreshJob: Job? = null

    var announcements by mutableStateOf<List<Announcement>>(emptyList())
        private set
    var alerts by mutableStateOf<List<AlertItem>>(emptyList())
        private set
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun findAnnouncement(id: String): Announcement? = announcements.firstOrNull { it.id == id }

    fun refresh() {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            isLoading = true
            try {
                announcements = repository.getAnnouncements()
                alerts = repository.getAlerts()
                errorMessage = null
            } catch (e: Exception) {
                if (e is kotlin.coroutines.cancellation.CancellationException) throw e
                errorMessage = "Gagal memuat inbox. Periksa koneksi internet."
            } finally {
                isLoading = false
            }
        }
    }
}
