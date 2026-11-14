package org.umn.projectuts.data

import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import org.umn.projectuts.data.remote.SupabaseConfig
import org.umn.projectuts.data.remote.UserNotifUpdate
import org.umn.projectuts.data.remote.UserProfileUpdate
import org.umn.projectuts.data.remote.UserRow

class UserRepository {

    private val client = SupabaseConfig.client

    /**
     * Ambil profil akun yang sedang login dari tbl_users.
     * RLS hanya mengembalikan baris dengan auth_id = akun yang login,
     * jadi hasilnya null kalau akun belum terdaftar di tbl_users.
     */
    suspend fun getCurrentUser(): UserRow? {
        val authId = client.auth.currentUserOrNull()?.id ?: return null
        return client.from("tbl_users")
            .select {
                filter { eq("auth_id", authId) }
            }
            .decodeSingleOrNull<UserRow>()
    }

    suspend fun updateProfile(userId: Int, profile: UserProfile) {
        client.from("tbl_users").update(
            UserProfileUpdate(
                name = profile.name,
                nim = profile.nim,
                phone = profile.phone
            )
        ) {
            filter { eq("user_id", userId) }
        }
    }

    suspend fun updateNotifications(
        userId: Int,
        emergencyAlerts: Boolean,
        incidentStatusAlerts: Boolean,
        generalAnnouncements: Boolean
    ) {
        client.from("tbl_users").update(
            UserNotifUpdate(emergencyAlerts, incidentStatusAlerts, generalAnnouncements)
        ) {
            filter { eq("user_id", userId) }
        }
    }
}
