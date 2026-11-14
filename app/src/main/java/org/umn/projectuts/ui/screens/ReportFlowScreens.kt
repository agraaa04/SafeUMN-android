package org.umn.projectuts.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.compose.foundation.Image
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.umn.projectuts.data.EvidenceItem
import org.umn.projectuts.data.ReportCategory
import org.umn.projectuts.ui.components.FlowPage
import org.umn.projectuts.ui.components.InfoRow
import org.umn.projectuts.ui.components.PrimaryActionButton
import org.umn.projectuts.ui.theme.SosRed
import org.umn.projectuts.util.decodePortraitBitmap
import org.umn.projectuts.util.decodeRotatedBitmap
import org.umn.projectuts.util.loadThumbnail
import org.umn.projectuts.util.rememberLocationRequester
import org.umn.projectuts.util.saveEvidenceJpeg
import org.umn.projectuts.util.uriToFile
import org.umn.projectuts.viewmodel.ReportFlowViewModel
import org.umn.projectuts.viewmodel.ReportsViewModel
import java.io.File
import java.util.concurrent.Executors

private val SensitiveCategories = setOf(ReportCategory.PERUNDUNGAN, ReportCategory.KEKERASAN_SEKSUAL)

@Composable
fun ReportCategoryScreen(flowVm: ReportFlowViewModel, onBack: () -> Unit, onNext: () -> Unit) {
    FlowPage(
        title = "Buat Laporan",
        step = 1,
        totalSteps = 4,
        onBack = onBack,
        bottomContent = {
            PrimaryActionButton(
                text = "Lanjutkan",
                onClick = onNext,
                enabled = flowVm.category != null
            )
        }
    ) {
        Text(
            "Pilih Kategori masalah",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 12.dp, bottom = 12.dp)
        )
        ReportCategory.entries.chunked(2).forEach { pair ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                pair.forEach { category ->
                    CategoryChip(
                        category = category,
                        selected = flowVm.category == category,
                        onClick = { flowVm.selectCategory(category) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        if (flowVm.category in SensitiveCategories) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "Laporan kategori ini ditangani secara privat dan rahasia oleh petugas yang berwenang.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        } else if (flowVm.category == null) {
            Text(
                "Pilih salah satu kategori untuk melanjutkan.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun CategoryChip(
    category: ReportCategory,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val container = if (selected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surface
    val content = if (selected) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurface
    Surface(
        modifier = modifier
            .height(80.dp)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        color = container,
        contentColor = content,
        border = BorderStroke(
            1.dp,
            if (selected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (selected) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
            }
            Text(
                category.label,
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center
            )
            if (category.subtitle.isNotEmpty()) {
                Text(
                    "(${category.subtitle})",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}


@Composable
fun ReportLocationScreen(flowVm: ReportFlowViewModel, onBack: () -> Unit, onNext: () -> Unit) {
    FlowPage(
        title = "Buat Laporan",
        step = 2,
        totalSteps = 4,
        onBack = onBack,
        bottomContent = {
            PrimaryActionButton(
                text = "Lanjutkan",
                onClick = { if (flowVm.validateLocation()) onNext() }
            )
        }
    ) {
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = flowVm.building,
            onValueChange = { flowVm.building = it },
            label = { Text("Lokasi Gedung") },
            placeholder = { Text("Contoh: Gedung D") },
            singleLine = true,
            isError = flowVm.buildingError != null,
            supportingText = { flowVm.buildingError?.let { Text(it) } },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next
            ),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = flowVm.room,
            onValueChange = { flowVm.room = it },
            label = { Text("Lantai/Ruang") },
            placeholder = { Text("Contoh: Lantai 5, Ruang D0510") },
            singleLine = true,
            isError = flowVm.roomError != null,
            supportingText = { flowVm.roomError?.let { Text(it) } },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Done
            ),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth()
        )

        // Izin lokasi diminta dulu, baru koordinat GPS asli diambil
        val getCurrentLocation = rememberLocationRequester { result -> flowVm.onLocationResult(result) }
        val requestLocation = {
            if (!flowVm.isLocating) {
                flowVm.startLocating()
                getCurrentLocation()
            }
        }

        Spacer(Modifier.height(8.dp))
        MockMap(onPickOnMap = { requestLocation() })
        Text(
            "Ketuk peta atau tombol di bawah untuk mengambil koordinat GPS Anda saat ini.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 6.dp)
        )

        flowVm.coordinates?.let { coords ->
            Row(
                modifier = Modifier.padding(top = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Place,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(6.dp))
                Column {
                    Text("Koordinat: $coords", style = MaterialTheme.typography.bodyMedium)
                    flowVm.locationAccuracy?.let { acc ->
                        Text(
                            "Akurasi ± ${acc.toInt()} meter",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        flowVm.locationError?.let { message ->
            Text(
                message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(Modifier.height(12.dp))
        OutlinedButton(
            onClick = { requestLocation() },
            shape = MaterialTheme.shapes.large,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            if (flowVm.isLocating) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                Spacer(Modifier.width(8.dp))
                Text("Mengambil lokasi...")
            } else {
                Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Gunakan Lokasi Saat Ini (GPS)")
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun MockMap(onPickOnMap: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(170.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(Color(0xFFE5ECF3))
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val road = Color.White
            val w = size.width
            val h = size.height
            val thick = 16.dp.toPx()
            drawLine(road, Offset(0f, h * 0.30f), Offset(w, h * 0.45f), strokeWidth = thick)
            drawLine(road, Offset(0f, h * 0.80f), Offset(w, h * 0.70f), strokeWidth = thick * 0.7f)
            drawLine(road, Offset(w * 0.30f, 0f), Offset(w * 0.40f, h), strokeWidth = thick * 0.8f)
            drawLine(road, Offset(w * 0.75f, 0f), Offset(w * 0.68f, h), strokeWidth = thick)
            drawCircle(Color(0xFFBFD8C4), radius = 26.dp.toPx(), center = Offset(w * 0.15f, h * 0.62f))
        }
        Icon(
            Icons.Default.Place,
            contentDescription = "Pin lokasi",
            tint = SosRed,
            modifier = Modifier
                .align(Alignment.Center)
                .size(40.dp)
        )
        Button(
            onClick = onPickOnMap,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 10.dp)
        ) { Text("Pilih di Peta") }
    }
}

@Composable
fun ReportInputScreen(
    flowVm: ReportFlowViewModel,
    onBack: () -> Unit,
    onOpenCamera: () -> Unit,
    onNext: () -> Unit
) {
    val full = flowVm.evidence.size >= ReportFlowViewModel.MAX_EVIDENCE
    val dashColor = MaterialTheme.colorScheme.primary
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var galleryLoading by remember { mutableStateOf(false) }
    var galleryError by remember { mutableStateOf<String?>(null) }

    // Galeri: Photo Picker Android (tidak perlu izin penyimpanan)
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            galleryLoading = true
            galleryError = null
            scope.launch {
                val file = withContext(Dispatchers.IO) {
                    try {
                        val source = uriToFile(context, uri)
                        val bitmap = decodeRotatedBitmap(source)
                        source.delete()
                        bitmap?.let { saveEvidenceJpeg(context, it, "gallery").also { _ -> it.recycle() } }
                    } catch (e: Exception) {
                        null
                    }
                }
                galleryLoading = false
                if (file != null) {
                    flowVm.addEvidence(file, fromCamera = false)
                } else {
                    galleryError = "Gambar tidak dapat dibaca. Pilih gambar lain."
                }
            }
        }
    }

    FlowPage(
        title = "Buat Laporan",
        step = 3,
        totalSteps = 4,
        onBack = onBack,
        bottomContent = {
            PrimaryActionButton(
                text = "Lanjutkan",
                onClick = { if (flowVm.validateDetail()) onNext() }
            )
        }
    ) {
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = flowVm.description,
            onValueChange = { flowVm.onDescriptionChange(it) },
            label = { Text("Detail Laporan (Deskripsi)") },
            placeholder = { Text("Ceritakan yang terjadi secara singkat dan jelas...") },
            minLines = 5,
            maxLines = 8,
            isError = flowVm.descriptionError != null,
            supportingText = {
                Text(
                    flowVm.descriptionError
                        ?: "${flowVm.description.length}/${ReportFlowViewModel.MAX_DESCRIPTION}"
                )
            },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Default
            ),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))
        Text(
            "Tambahkan Bukti (Opsional)  ${flowVm.evidence.size}/${ReportFlowViewModel.MAX_EVIDENCE}",
            style = MaterialTheme.typography.titleSmall
        )
        Spacer(Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f))
                .drawBehind {
                    drawRoundRect(
                        color = dashColor,
                        style = Stroke(
                            width = 2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f))
                        ),
                        cornerRadius = CornerRadius(16.dp.toPx())
                    )
                }
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.CloudUpload,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
                Text(
                    "Upload an image",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(onClick = onOpenCamera, enabled = !full) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Kamera")
                    }
                    OutlinedButton(
                        onClick = {
                            galleryLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        enabled = !full && !galleryLoading
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Galeri")
                    }
                }
                if (galleryLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .size(20.dp),
                        strokeWidth = 2.dp
                    )
                }
                galleryError?.let { message ->
                    Text(
                        message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
                if (full) {
                    Text(
                        "Maksimal ${ReportFlowViewModel.MAX_EVIDENCE} foto.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }
        }

        if (flowVm.evidence.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Text("Image", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                flowVm.evidence.forEach { item ->
                    EvidenceTile(item = item, onRemove = { flowVm.removeEvidence(item.id) })
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun EvidenceThumbnail(item: EvidenceItem, size: androidx.compose.ui.unit.Dp) {
    val thumbnail = remember(item.file) { loadThumbnail(item.file) }
    Box(
        modifier = Modifier
            .size(size)
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        if (thumbnail != null) {
            Image(
                bitmap = thumbnail,
                contentDescription = item.label,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                if (item.fromCamera) Icons.Default.CameraAlt else Icons.Default.Image,
                contentDescription = item.label,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
private fun EvidenceTile(item: EvidenceItem, onRemove: () -> Unit) {
    Box(Modifier.padding(top = 8.dp, end = 8.dp)) {
        EvidenceThumbnail(item, 96.dp)
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 8.dp, y = (-8).dp)
                .size(26.dp)
                .background(MaterialTheme.colorScheme.error, CircleShape)
                .clickable(onClickLabel = "Hapus foto", onClick = onRemove),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Hapus ${item.label}",
                tint = MaterialTheme.colorScheme.onError,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun ReportCameraScreen(onClose: () -> Unit, onPhotoTaken: (File) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    var permissionMessage by remember { mutableStateOf("") }
    var photoBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var status by remember { mutableStateOf("Kamera belum dimulai.") }
    var capturing by remember { mutableStateOf(false) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var flashOn by remember { mutableStateOf(false) }
    var backCamera by remember { mutableStateOf(true) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(cameraExecutor) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        permissionMessage = if (granted) {
            ""
        } else {
            "Izin kamera ditolak. Izinkan akses kamera untuk mengambil foto bukti."
        }
    }

    // FULLSCREEN PHOTO VIEW (hasil foto)
    val displayedBitmap = photoBitmap
    if (displayedBitmap != null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            Image(
                bitmap = displayedBitmap.asImageBitmap(),
                contentDescription = "Foto bukti",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
            // Tombol X bulat untuk foto ulang
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
                    .size(48.dp)
                    .background(color = Color(0xFF90B4FF), shape = CircleShape)
                    .clickable(onClickLabel = "Foto ulang") {
                        photoBitmap = null
                        status = "Kamera siap mengambil foto."
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Close, contentDescription = "Foto ulang", tint = Color(0xFF173A70))
            }
            PrimaryActionButton(
                text = "Gunakan Foto Ini",
                onClick = {
                    val file = saveEvidenceJpeg(context, displayedBitmap, "camera")
                    onPhotoTaken(file)
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(20.dp)
            )
        }
        return
    }

    // CAMERA VIEW
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Tutup kamera", tint = Color.White)
            }
            Spacer(Modifier.weight(1f))
            Text(
                "Foto Bukti",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
            Spacer(Modifier.weight(1f))
            IconButton(onClick = { flashOn = !flashOn }, enabled = hasCameraPermission) {
                Icon(
                    if (flashOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                    contentDescription = if (flashOn) "Flash menyala" else "Flash mati",
                    tint = if (flashOn) Color(0xFFFFD54F) else Color.White
                )
            }
        }

        if (!hasCameraPermission) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.CameraAlt,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(48.dp)
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = permissionMessage.ifBlank {
                        "Izin kamera diperlukan untuk mengambil foto bukti."
                    },
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(16.dp))
                PrimaryActionButton(
                    text = "Allow Camera Access",
                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }
                )
            }
        } else {
            CameraPreview(
                lifecycleOwner = lifecycleOwner,
                useBackCamera = backCamera,
                flashOn = flashOn,
                onImageCaptureReady = { capture ->
                    imageCapture = capture
                    status = "Kamera siap mengambil foto."
                },
                onError = { message ->
                    status = message
                },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .clip(MaterialTheme.shapes.large)
            )
            Text(
                text = status,
                color = Color.White.copy(alpha = 0.8f),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(Modifier.weight(1f))
                // ROUND SHUTTER BUTTON
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .background(
                            color = if (capturing) Color.Gray else Color(0xFFE0E0E0),
                            shape = CircleShape
                        )
                        .padding(6.dp)
                        .background(color = Color.White, shape = CircleShape)
                        .padding(5.dp)
                        .background(
                            color = if (capturing) Color.Gray else Color(0xFF424242),
                            shape = CircleShape
                        )
                        .clickable(enabled = !capturing, onClickLabel = "Ambil foto") {
                            val capture = imageCapture
                            if (capture == null) {
                                status = "Menyiapkan kamera..."
                            } else {
                                capturing = true
                                status = "Mengambil foto..."
                                val photoFile = File(
                                    context.cacheDir,
                                    "capture_${System.currentTimeMillis()}.jpg"
                                )
                                val outputOptions =
                                    ImageCapture.OutputFileOptions.Builder(photoFile).build()
                                capture.takePicture(
                                    outputOptions,
                                    cameraExecutor,
                                    object : ImageCapture.OnImageSavedCallback {
                                        override fun onImageSaved(
                                            outputFileResults: ImageCapture.OutputFileResults
                                        ) {
                                            // Correct photo orientation.
                                            val bitmap = decodePortraitBitmap(photoFile)
                                            photoFile.delete()
                                            Handler(Looper.getMainLooper()).post {
                                                capturing = false
                                                if (bitmap != null) {
                                                    photoBitmap = bitmap
                                                    status = "Foto berhasil diambil."
                                                } else {
                                                    status = "Gagal menampilkan foto."
                                                }
                                            }
                                        }

                                        override fun onError(exception: ImageCaptureException) {
                                            Handler(Looper.getMainLooper()).post {
                                                capturing = false
                                                status = "Gagal mengambil foto: " + exception.message
                                            }
                                        }
                                    }
                                )
                            }
                        }
                )
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    IconButton(onClick = {
                        backCamera = !backCamera
                        imageCapture = null
                    }) {
                        Icon(
                            Icons.Default.Cameraswitch,
                            contentDescription = "Ganti kamera",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReportSummaryScreen(
    flowVm: ReportFlowViewModel,
    reportsVm: ReportsViewModel,
    onBack: () -> Unit,
    onSubmitted: (String) -> Unit
) {
    FlowPage(
        title = "Buat Laporan",
        step = 4,
        totalSteps = 4,
        onBack = onBack,
        bottomContent = {
            Column {
                flowVm.submitError?.let { message ->
                    Text(
                        message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                PrimaryActionButton(
                    text = "Konfirmasi",
                    onClick = { flowVm.submit(reportsVm, onSubmitted) },
                    loading = flowVm.isSubmitting
                )
            }
        }
    ) {
        Text(
            "Ringkasan Laporan",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 8.dp, bottom = 12.dp)
        )
        Card(
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp)) {
                val category = flowVm.category
                InfoRow(
                    "Kategori",
                    category?.let {
                        if (it.subtitle.isEmpty()) it.label else "${it.label} (${it.subtitle})"
                    } ?: "-"
                )
                InfoRow("Lokasi", "${flowVm.building.trim()}, ${flowVm.room.trim()}")
                flowVm.coordinates?.let { InfoRow("Koordinat", it) }
                InfoRow("Detail", flowVm.description.trim())
                Text(
                    "Bukti",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp)
                )
                if (flowVm.evidence.isEmpty()) {
                    Text("Tidak ada bukti dilampirkan", style = MaterialTheme.typography.bodyMedium)
                } else {
                    Row(
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        flowVm.evidence.forEach { item ->
                            EvidenceThumbnail(item, 72.dp)
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            "Periksa kembali data di atas sebelum mengirim laporan.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ReportSuccessScreen(reportId: String, onCheckStatus: () -> Unit, onDone: () -> Unit) {
    BackHandler(onBack = onDone)
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { shown = true }
    val scale by animateFloatAsState(
        targetValue = if (shown) 1f else 0.3f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "successCheck"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.weight(1f))
        Text(
            "Laporan Berhasil Dibuat",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(28.dp))
        Box(
            modifier = Modifier
                .size(180.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                            MaterialTheme.colorScheme.primary
                        )
                    ),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = "Berhasil",
                tint = Color.White,
                modifier = Modifier.size(110.dp)
            )
        }
        Spacer(Modifier.height(24.dp))
        Text(
            "Nomor laporan: $reportId",
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            "Petugas telah menerima laporan Anda. Pantau progresnya di Status Laporan.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
        Spacer(Modifier.weight(1f))
        PrimaryActionButton(
            text = "Cek Status Laporan",
            onClick = onCheckStatus,
            container = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(12.dp))
        PrimaryActionButton(text = "Kembali ke Home", onClick = onDone)
        Spacer(Modifier.navigationBarsPadding())
    }
}
