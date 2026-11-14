package org.umn.projectuts.data

import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import org.umn.projectuts.data.remote.SupabaseConfig

/** Login & logout memakai Supabase Auth (Authentication -> Users di dashboard). */
class AuthRepository {

    private val auth = SupabaseConfig.client.auth

    suspend fun signIn(email: String, password: String) {
        auth.signInWith(Email) {
            this.email = email.trim().lowercase()
            this.password = password
        }
    }

    suspend fun signOut() {
        auth.signOut()
    }
}
