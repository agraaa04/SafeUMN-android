package org.umn.projectuts.data

import io.github.jan.supabase.postgrest.from
import org.umn.projectuts.data.remote.SosCancel
import org.umn.projectuts.data.remote.SosInsert
import org.umn.projectuts.data.remote.SosRow
import org.umn.projectuts.data.remote.SupabaseConfig
import java.time.OffsetDateTime

class SosRepository {

    private val client = SupabaseConfig.client

    /** Simpan sinyal SOS baru ke tbl_sos_alerts dan kembalikan sos_id-nya. */
    suspend fun sendSos(latitude: Double, longitude: Double): Int =
        client.from("tbl_sos_alerts")
            .insert(SosInsert(UserSession.requireUserId(), latitude, longitude)) {
                select()
            }
            .decodeSingle<SosRow>()
            .sosId

    /** Ubah status SOS menjadi DIBATALKAN. */
    suspend fun cancelSos(sosId: Int) {
        client.from("tbl_sos_alerts").update(
            SosCancel(status = "DIBATALKAN", cancelledAt = OffsetDateTime.now().toString())
        ) {
            filter { eq("sos_id", sosId) }
        }
    }
}
