package org.umn.projectuts.ui.screens

import android.view.Surface
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
@Composable
fun CameraPreview(
    lifecycleOwner: LifecycleOwner,
    useBackCamera: Boolean,
    flashOn: Boolean,
    onImageCaptureReady: (ImageCapture) -> Unit,
    onError: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }
    DisposableEffect(previewView, lifecycleOwner, useBackCamera, flashOn) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        var cameraProvider: ProcessCameraProvider? = null
        cameraProviderFuture.addListener(
            {
                try {
                    val provider = cameraProviderFuture.get()
                    cameraProvider = provider
                    val targetRotation = previewView.display?.rotation
                        ?: Surface.ROTATION_0

                    val preview = Preview.Builder().setTargetRotation(targetRotation)
                        .build().also {
                            it.surfaceProvider = previewView.surfaceProvider
                        }
                    val capture = ImageCapture.Builder()
                        .setTargetRotation(targetRotation)
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .setFlashMode(
                            if (flashOn) ImageCapture.FLASH_MODE_ON else ImageCapture.FLASH_MODE_OFF
                        )
                        .build()
                    val selector = if (useBackCamera) {
                        CameraSelector.DEFAULT_BACK_CAMERA
                    } else {
                        CameraSelector.DEFAULT_FRONT_CAMERA
                    }
                    provider.unbindAll()
                    provider.bindToLifecycle(
                        lifecycleOwner, selector,
                        preview, capture
                    )
                    onImageCaptureReady(capture)
                } catch (e: Exception) {
                    onError("Gagal membuka kamera: ${e.message}")
                }
            },
            ContextCompat.getMainExecutor(context)
        )
        onDispose {
            cameraProvider?.unbindAll()
        }
    }
    AndroidView(
        factory = { previewView },
        modifier = modifier
    )
}
