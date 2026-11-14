package org.umn.projectuts.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.umn.projectuts.data.AuthRepository
import org.umn.projectuts.data.UserProfile
import org.umn.projectuts.data.UserRepository
import org.umn.projectuts.data.UserSession
import org.umn.projectuts.util.Validators

class ProfileViewModel : ViewModel() {

    private val repository = UserRepository()
    private val authRepository = AuthRepository()

    var profile by mutableStateOf(UserProfile(name = "", email = "", nim = "", phone = ""))
        private set

    var isLoading by mutableStateOf(false)
        private set
    var isSaving by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    // Form edit akun
    var editName by mutableStateOf("")
    var editEmail by mutableStateOf("")
    var editNim by mutableStateOf("")
    var editPhone by mutableStateOf("")
    var saveAttempted by mutableStateOf(false)
        private set
    var justSaved by mutableStateOf(false)
        private set

    var emergencyAlerts by mutableStateOf(true)
        private set
    var incidentStatusAlerts by mutableStateOf(true)
        private set
    var generalAnnouncements by mutableStateOf(true)
        private set

    val firstName: String
        get() = profile.name.trim().substringBefore(' ').ifEmpty { "User" }

    val nameError: String?
        get() = when {
            editName.isBlank() -> "Nama wajib diisi"
            editName.trim().length < 3 -> "Nama minimal 3 karakter"
            else -> null
        }

    val emailError: String?
        get() = when {
            editEmail.isBlank() -> "Email wajib diisi"
            !Validators.isValidEmail(editEmail) -> "Format email tidak valid"
            else -> null
        }

    val nimError: String?
        get() = when {
            editNim.isEmpty() -> "NIM wajib diisi"
            !Validators.isDigitsOnly(editNim) -> "NIM hanya boleh berisi angka"
            editNim.length != 11 -> "NIM harus 11 digit"
            else -> null
        }

    val phoneError: String?
        get() = when {
            editPhone.isEmpty() -> "Nomor HP wajib diisi"
            !Validators.isDigitsOnly(editPhone) -> "Nomor HP hanya boleh berisi angka"
            editPhone.length !in 9..13 -> "Nomor HP harus 9-13 digit"
            else -> null
        }

    /**
     * Dipanggil setelah login Supabase Auth berhasil.
     * Ambil profil dari tbl_users; kalau akun tidak terdaftar di tbl_users, login dibatalkan.
     */
    fun loadCurrentUser(onLoaded: () -> Unit, onError: (String) -> Unit) {
        if (isLoading) return
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val user = repository.getCurrentUser()
                if (user == null) {
                    authRepository.signOut()
                    onError("Akun ini belum terdaftar di SafeUMN. Hubungi admin.")
                    return@launch
                }
                UserSession.userId = user.userId
                profile = UserProfile(
                    name = user.name,
                    email = user.email,
                    nim = user.nim.orEmpty(),
                    phone = user.phone.orEmpty()
                )
                emergencyAlerts = user.emergencyAlerts
                incidentStatusAlerts = user.incidentStatusAlerts
                generalAnnouncements = user.generalAnnouncements
                onLoaded()
            } catch (e: Exception) {
                if (e is kotlin.coroutines.cancellation.CancellationException) throw e
                errorMessage = "Gagal memuat profil. Periksa koneksi internet."
                onError(errorMessage.orEmpty())
            } finally {
                isLoading = false
            }
        }
    }

    fun signOut() {
        UserSession.clear()
        profile = UserProfile(name = "", email = "", nim = "", phone = "")
        errorMessage = null
        viewModelScope.launch {
            try {
                authRepository.signOut()
            } catch (e: Exception) {
                if (e is kotlin.coroutines.cancellation.CancellationException) throw e
                // Sesi lokal tetap dihapus walaupun server tidak terjangkau
            }
        }
    }

    fun onEmergencyAlertsChange(value: Boolean) {
        emergencyAlerts = value
        saveNotifications()
    }

    fun onIncidentStatusAlertsChange(value: Boolean) {
        incidentStatusAlerts = value
        saveNotifications()
    }

    fun onGeneralAnnouncementsChange(value: Boolean) {
        generalAnnouncements = value
        saveNotifications()
    }

    private fun saveNotifications() {
        val userId = UserSession.userId ?: return
        viewModelScope.launch {
            try {
                repository.updateNotifications(
                    userId, emergencyAlerts, incidentStatusAlerts, generalAnnouncements
                )
                errorMessage = null
            } catch (e: Exception) {
                if (e is kotlin.coroutines.cancellation.CancellationException) throw e
                errorMessage = "Pengaturan gagal disimpan. Periksa koneksi internet."
            }
        }
    }

    fun startEdit() {
        editName = profile.name
        editEmail = profile.email
        editNim = profile.nim
        editPhone = profile.phone
        saveAttempted = false
    }

    fun onNimChange(value: String) {
        editNim = Validators.filterDigits(value, 11)
    }

    fun onPhoneChange(value: String) {
        editPhone = Validators.filterDigits(value, 13)
    }

    fun save(): Boolean {
        saveAttempted = true
        val valid = nameError == null && emailError == null && nimError == null && phoneError == null
        val userId = UserSession.userId
        if (!valid || userId == null || isSaving) return false

        val updated = UserProfile(
            name = editName.trim(),
            email = profile.email, // email login tidak bisa diubah dari aplikasi
            nim = editNim,
            phone = editPhone
        )
        viewModelScope.launch {
            isSaving = true
            errorMessage = null
            try {
                repository.updateProfile(userId, updated)
                profile = updated
                justSaved = true
            } catch (e: Exception) {
                if (e is kotlin.coroutines.cancellation.CancellationException) throw e
                errorMessage = "Gagal menyimpan. NIM mungkin sudah dipakai akun lain, " +
                    "atau koneksi internet bermasalah."
            } finally {
                isSaving = false
            }
        }
        return true
    }

    fun clearSavedFlag() {
        justSaved = false
    }
}
