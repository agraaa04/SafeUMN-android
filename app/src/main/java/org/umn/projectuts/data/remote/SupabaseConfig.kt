package org.umn.projectuts.data.remote

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.serializer.KotlinXSerializer
import io.github.jan.supabase.storage.Storage
import kotlinx.serialization.json.Json
import org.umn.projectuts.BuildConfig

object SupabaseConfig {

    // Bucket foto bukti laporan di Supabase Storage
    const val EVIDENCE_BUCKET = "report-evidence"

    val client = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_KEY
    ) {
        defaultSerializer = KotlinXSerializer(
            Json {
                ignoreUnknownKeys = true
                explicitNulls = false
            }
        )
        install(Auth)
        install(Postgrest)
        install(Storage)
    }
}
