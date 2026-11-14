package org.umn.projectuts.data

import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import org.umn.projectuts.data.remote.AlertRow
import org.umn.projectuts.data.remote.AnnouncementRow
import org.umn.projectuts.data.remote.SupabaseConfig
import org.umn.projectuts.util.DateUtils

class InboxRepository {

    private val client = SupabaseConfig.client

    /** Pengumuman yang sedang tayang (RLS hanya mengizinkan is_published = true). */
    suspend fun getAnnouncements(): List<Announcement> =
        client.from("tbl_announcements")
            .select {
                order("announcement_id", Order.ASCENDING)
            }
            .decodeList<AnnouncementRow>()
            .map { row ->
                Announcement(
                    id = row.announcementId.toString(),
                    title = row.title,
                    summary = row.summary,
                    icon = runCatching { AnnouncementIcon.valueOf(row.icon) }
                        .getOrDefault(AnnouncementIcon.PROCEDURE),
                    body = row.body
                )
            }

    /** Alert yang masih aktif (RLS hanya mengizinkan is_active = true), terbaru di atas. */
    suspend fun getAlerts(): List<AlertItem> =
        client.from("tbl_alerts")
            .select {
                order("created_at", Order.DESCENDING)
            }
            .decodeList<AlertRow>()
            .map { row ->
                AlertItem(
                    id = row.alertId.toString(),
                    title = row.title,
                    message = row.message,
                    timeAgo = DateUtils.timeAgo(DateUtils.parseTimestamp(row.createdAt)),
                    severity = runCatching { AlertSeverity.valueOf(row.severity) }
                        .getOrDefault(AlertSeverity.SAFETY)
                )
            }
}
