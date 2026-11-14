package org.umn.projectuts.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.exifinterface.media.ExifInterface
import java.io.File
import kotlin.math.max

/**
 * Decode foto dan sesuaikan rotasinya berdasarkan EXIF,
 * lalu pastikan tampil potret (sama seperti decodePortraitBitmap di Modul 9).
 */
fun decodePortraitBitmap(photoFile: File): Bitmap? {
    val originalBitmap =
        BitmapFactory.decodeFile(photoFile.absolutePath)
            ?: return null
    try {
        val exif = ExifInterface(photoFile.absolutePath)
        val orientation = exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )

        val matrix = Matrix()

        // Apply EXIF orientation.
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> {
                matrix.postRotate(90f)
            }
            ExifInterface.ORIENTATION_ROTATE_180 -> {
                matrix.postRotate(180f)
            }
            ExifInterface.ORIENTATION_ROTATE_270 -> {
                matrix.postRotate(270f)
            }
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> {
                matrix.postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> {
                matrix.postScale(1f, -1f)
            }
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                matrix.setValues(
                    floatArrayOf(
                        0f, 1f, 0f,
                        1f, 0f, 0f,
                        0f, 0f, 1f
                    )
                )
            }
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                matrix.setValues(
                    floatArrayOf(
                        0f, -1f, 0f,
                        -1f, 0f, 0f,
                        0f, 0f, 1f
                    )
                )
            }
        }
        var correctedBitmap = Bitmap.createBitmap(
            originalBitmap,
            0,
            0,
            originalBitmap.width,
            originalBitmap.height,
            matrix,
            true
        )
        // Display the image in portrait orientation
        if (correctedBitmap.width > correctedBitmap.height) {
            val portraitMatrix = Matrix().apply {
                postRotate(90f)
            }
            val portraitBitmap = Bitmap.createBitmap(
                correctedBitmap,
                0,
                0,
                correctedBitmap.width,
                correctedBitmap.height,
                portraitMatrix,
                true
            )
            if (portraitBitmap !== correctedBitmap) {
                correctedBitmap.recycle()
            }
            correctedBitmap = portraitBitmap
        }
        if (correctedBitmap !== originalBitmap) {
            originalBitmap.recycle()
        }
        return correctedBitmap
    } catch (e: Exception) {
        originalBitmap.recycle()
        return null
    }
}

/**
 * Decode foto sesuai EXIF tanpa memaksa potret (untuk foto dari galeri).
 */
fun decodeRotatedBitmap(photoFile: File): Bitmap? {
    val originalBitmap = BitmapFactory.decodeFile(photoFile.absolutePath) ?: return null
    return try {
        val orientation = ExifInterface(photoFile.absolutePath).getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )
        val degrees = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }
        if (degrees == 0f) {
            originalBitmap
        } else {
            val matrix = Matrix().apply { postRotate(degrees) }
            val rotated = Bitmap.createBitmap(
                originalBitmap, 0, 0, originalBitmap.width, originalBitmap.height, matrix, true
            )
            if (rotated !== originalBitmap) originalBitmap.recycle()
            rotated
        }
    } catch (e: Exception) {
        originalBitmap
    }
}

/** Salin gambar dari galeri (Uri) ke file sementara di cache aplikasi. */
fun uriToFile(context: Context, uri: Uri): File {
    val file = File(context.cacheDir, "gallery_${System.currentTimeMillis()}.jpg")
    context.contentResolver.openInputStream(uri).use { input ->
        requireNotNull(input) { "Unable to read the image" }
        file.outputStream().use { output -> input.copyTo(output) }
    }
    return file
}

/**
 * Simpan bitmap sebagai JPEG siap unggah (sisi terpanjang maks. 1600 px,
 * kualitas 85) supaya ukurannya jauh di bawah batas bucket 5 MB.
 */
fun saveEvidenceJpeg(context: Context, bitmap: Bitmap, prefix: String): File {
    val maxSide = 1600
    val longest = max(bitmap.width, bitmap.height)
    val scaled = if (longest > maxSide) {
        val ratio = maxSide.toFloat() / longest
        Bitmap.createScaledBitmap(
            bitmap,
            (bitmap.width * ratio).toInt(),
            (bitmap.height * ratio).toInt(),
            true
        )
    } else {
        bitmap
    }
    val file = File(context.cacheDir, "${prefix}_${System.currentTimeMillis()}.jpg")
    file.outputStream().use { output ->
        scaled.compress(Bitmap.CompressFormat.JPEG, 85, output)
    }
    if (scaled !== bitmap) scaled.recycle()
    return file
}

/** Thumbnail kecil untuk ditampilkan di daftar bukti. */
fun loadThumbnail(file: File, targetSize: Int = 300): ImageBitmap? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(file.absolutePath, bounds)
    var sample = 1
    while (bounds.outWidth / (sample * 2) >= targetSize && bounds.outHeight / (sample * 2) >= targetSize) {
        sample *= 2
    }
    val options = BitmapFactory.Options().apply { inSampleSize = sample }
    return BitmapFactory.decodeFile(file.absolutePath, options)?.asImageBitmap()
}
