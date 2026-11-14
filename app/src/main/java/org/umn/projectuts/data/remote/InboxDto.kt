package org.umn.projectuts.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Satu baris dari public.tbl_announcements */
@Serializable
data class AnnouncementRow(
    @SerialName("announcement_id") val announcementId: Int,
    val title: String,
    val summary: String,
    val body: String,
    val icon: String
)

/** Satu baris dari public.tbl_alerts */
@Serializable
data class AlertRow(
    @SerialName("alert_id") val alertId: Int,
    val title: String,
    val message: String,
    val severity: String,
    @SerialName("created_at") val createdAt: String
)
