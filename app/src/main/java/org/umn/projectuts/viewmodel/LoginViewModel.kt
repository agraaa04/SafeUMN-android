package org.umn.projectuts.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.jan.supabase.exceptions.RestException
import kotlinx.coroutines.launch
import org.umn.projectuts.BuildConfig
import org.umn.projectuts.data.AuthRepository
import org.umn.projectuts.util.Validators

private const val TAG = "SafeUMN-Login"

class LoginViewModel : ViewModel() {
    private val authRepository = AuthRepository()

    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var rememberMe by mutableStateOf(false)
    var passwordVisible by mutableStateOf(false)
    var isLoading by mutableStateOf(false)
        private set
    var submitted by mutableStateOf(false)
        private set
    var authError by mutableStateOf<String?>(null)
        private set

    val emailError: String?
        get() = when {
            email.isBlank() -> if (submitted) "Email wajib diisi" else null
            (submitted || email.contains("@")) && !Validators.isValidEmail(email) -> "Format email tidak valid"
            else -> null
        }

    val passwordError: String?
        get() = when {
            password.isEmpty() -> if (submitted) "Password wajib diisi" else null
            password.length < 6 -> "Password minimal 6 karakter"
            else -> null
        }

    fun login(onSuccess: (String) -> Unit) {
        if (isLoading) return
        submitted = true
        authError = null
        if (emailError != null || passwordError != null) return
        if (!Validators.isUmnAccount(email)) {
            authError = "Gunakan email UMN (@student.umn.ac.id atau @umn.ac.id)."
            return
        }
        signIn(email.trim(), password, onSuccess)
    }

    /**
     * SSO: masuk dengan akun yang diatur di local.properties
     * (SSO_EMAIL dan SSO_PASSWORD), tanpa perlu mengetik password.
     */
    fun loginWithSso(onSuccess: (String) -> Unit) {
        if (isLoading) return
        authError = null
        val ssoEmail = BuildConfig.SSO_EMAIL
        val ssoPassword = BuildConfig.SSO_PASSWORD
        if (ssoEmail.isBlank() || ssoPassword.isBlank()) {
            authError = "SSO belum dikonfigurasi. Isi SSO_EMAIL dan SSO_PASSWORD di local.properties."
            return
        }
        signIn(ssoEmail, ssoPassword, onSuccess)
    }

    private fun signIn(loginEmail: String, loginPassword: String, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            isLoading = true
            try {
                authRepository.signIn(loginEmail, loginPassword)
                onSuccess(loginEmail)
            } catch (e: RestException) {
                // Ditolak Supabase Auth: password salah atau akun tidak terdaftar
                Log.e(TAG, "Login ditolak Supabase Auth", e)
                authError = "Email atau password salah, atau akun belum terdaftar."
            } catch (e: Exception) {
                if (e is kotlin.coroutines.cancellation.CancellationException) throw e
                Log.e(TAG, "Login gagal", e)
                authError = "Login gagal: ${e::class.simpleName} - ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    /** Menampilkan pesan error di layar login (mis. gagal terhubung ke Supabase). */
    fun showError(message: String) {
        authError = message
    }

    fun reset() {
        email = ""
        password = ""
        rememberMe = false
        passwordVisible = false
        submitted = false
        authError = null
        isLoading = false
    }
}
