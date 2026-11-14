package org.umn.projectuts.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Satu baris dari public.tbl_users */
@Serializable
data class UserRow(
    @SerialName("user_id") val userId: Int,
    val name: String,
    val email: String,
    val nim: String? = null,
    val phone: String? = null,
    @SerialName("emergency_alerts") val emergencyAlerts: Boolean = true,
    @SerialName("incident_status_alerts") val incidentStatusAlerts: Boolean = true,
    @SerialName("general_announcements") val generalAnnouncements: Boolean = true
)

/** Data untuk menyimpan perubahan di halaman Akun (email login tidak ikut diubah) */
@Serializable
data class UserProfileUpdate(
    val name: String,
    val nim: String,
    val phone: String
)

/** Data untuk menyimpan Pengaturan Notifikasi */
@Serializable
data class UserNotifUpdate(
    @SerialName("emergency_alerts") val emergencyAlerts: Boolean,
    @SerialName("incident_status_alerts") val incidentStatusAlerts: Boolean,
    @SerialName("general_announcements") val generalAnnouncements: Boolean
)
