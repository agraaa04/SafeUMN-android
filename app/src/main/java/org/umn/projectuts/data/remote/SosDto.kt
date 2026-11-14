package org.umn.projectuts.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Data yang dikirim saat tombol SOS ditekan (status otomatis AKTIF) */
@Serializable
data class SosInsert(
    @SerialName("user_id") val userId: Int,
    val latitude: Double,
    val longitude: Double
)

/** Kolom yang dikembalikan setelah SOS tersimpan */
@Serializable
data class SosRow(
    @SerialName("sos_id") val sosId: Int,
    val status: String
)

/** Data untuk membatalkan SOS */
@Serializable
data class SosCancel(
    // Tanpa nilai default: kotlinx.serialization tidak mengirim properti yang
    // nilainya sama dengan default, sehingga status tidak ikut terkirim.
    val status: String,
    @SerialName("cancelled_at") val cancelledAt: String
)
