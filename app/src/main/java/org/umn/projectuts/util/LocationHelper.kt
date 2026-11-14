package org.umn.projectuts.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

/** Koordinat GPS asli (sama seperti data class GPSLocation di Modul 9) */
data class GPSLocation(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float
)

/** Hasil permintaan lokasi: berhasil (location) atau gagal (pesan error) */
sealed class LocationResult {
    data class Success(val location: GPSLocation) : LocationResult()
    data class Error(val message: String) : LocationResult()
}

/**
 * Mengembalikan fungsi untuk mengambil lokasi terkini.
 * Alurnya sama seperti GPSScreen di Modul 9:
 * 1. Cek izin lokasi (ACCESS_FINE_LOCATION / ACCESS_COARSE_LOCATION).
 * 2. Kalau belum diizinkan, tampilkan dialog izin terlebih dahulu.
 * 3. Setelah diizinkan, ambil lokasi lewat Fused Location Provider.
 */
@Composable
fun rememberLocationRequester(onResult: (LocationResult) -> Unit): () -> Unit {
    val context = LocalContext.current
    val currentOnResult by rememberUpdatedState(onResult)

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted =
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            getLocation(context) { currentOnResult(it) }
        } else {
            currentOnResult(LocationResult.Error("Izin lokasi ditolak. Lokasi tidak dapat dikirim."))
        }
    }

    return remember<() -> Unit>(context, permissionLauncher) {
        {
            if (hasLocationPermission(context)) {
                getLocation(context) { currentOnResult(it) }
            } else {
                permissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }
}

private fun hasLocationPermission(context: Context): Boolean {
    val fineGranted = ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    val coarseGranted = ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    return fineGranted || coarseGranted
}

@SuppressLint("MissingPermission")
private fun getLocation(context: Context, onResult: (LocationResult) -> Unit) {
    // Check location permission
    val fineGranted = ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    if (!hasLocationPermission(context)) {
        onResult(LocationResult.Error("Izin lokasi belum diberikan."))
        return
    }

    // Create Fused Location Client
    val fusedClient = LocationServices.getFusedLocationProviderClient(context)

    // Izin presisi -> akurasi tinggi (GPS), izin perkiraan -> akurasi seimbang
    val priority = if (fineGranted) {
        Priority.PRIORITY_HIGH_ACCURACY
    } else {
        Priority.PRIORITY_BALANCED_POWER_ACCURACY
    }

    fusedClient.getCurrentLocation(priority, CancellationTokenSource().token)
        .addOnSuccessListener { location ->
            if (location != null) {
                onResult(
                    LocationResult.Success(
                        GPSLocation(
                            latitude = location.latitude,
                            longitude = location.longitude,
                            accuracy = location.accuracy
                        )
                    )
                )
            } else {
                onResult(
                    LocationResult.Error(
                        "Lokasi tidak ditemukan. Pastikan GPS/Location di HP sudah aktif."
                    )
                )
            }
        }
        .addOnFailureListener { error ->
            onResult(LocationResult.Error("Gagal mengambil lokasi: ${error.message}"))
        }
}
