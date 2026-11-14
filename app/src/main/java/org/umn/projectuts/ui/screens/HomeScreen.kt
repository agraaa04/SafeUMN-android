package org.umn.projectuts.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.umn.projectuts.ui.components.ScreenHeader
import org.umn.projectuts.ui.theme.SosRed
import org.umn.projectuts.ui.theme.SosRedDeep
import org.umn.projectuts.util.rememberLocationRequester
import org.umn.projectuts.viewmodel.SosViewModel
import kotlin.math.ceil

@Composable
fun HomeScreen(
    sosVm: SosViewModel,
    userName: String,
    onCreateReport: () -> Unit,
    onStatus: () -> Unit,
    onHistory: () -> Unit,
    onNotifications: () -> Unit
) {
    val active = sosVm.isActive
    val requestSosLocation = rememberLocationRequester { result -> sosVm.activate(result) }
    val tint = if (active) SosRed else MaterialTheme.colorScheme.primary

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        ScreenHeader(userName, Modifier.padding(top = 12.dp))

        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            val sosSize = maxOf(
                160.dp,
                minOf(maxWidth, maxHeight - 190.dp, 320.dp)
            )

            SosBanner(
                visible = active,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 8.dp)
            )

            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SosButton(
                    active = active,
                    size = sosSize,
                    onTriggered = {
                        if (!sosVm.isActive && !sosVm.isSending) {
                            sosVm.startLocating()
                            requestSosLocation()
                        }
                    }
                )
                Spacer(Modifier.height(6.dp))
                Text("Panic Button", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(2.dp))
                Text(
                    text = if (active) {
                        "Lokasi Anda sudah dikirim ke petugas keamanan.\nKoordinat: ${sosVm.sentCoordinates}"
                    } else if (sosVm.isSending) {
                        "Mengirim lokasi Anda ke petugas keamanan..."
                    } else {
                        "Press & hold for 3 seconds to alert emergency.\nHelp will be instantly notified!"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                sosVm.errorMessage?.let { message ->
                    Text(
                        message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                if (active) {
                    TextButton(onClick = { sosVm.cancel() }) {
                        Text("Batalkan SOS (salah tekan)", color = SosRed)
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            ShortcutCard(
                icon = Icons.Default.AttachFile,
                label = "Buat\nLaporan",
                tint = tint,
                onClick = onCreateReport,
                modifier = Modifier.weight(1f)
            )
            ShortcutCard(
                icon = Icons.Default.Inbox,
                label = "Status\nLaporan",
                tint = tint,
                onClick = onStatus,
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 14.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            ShortcutCard(
                icon = Icons.Default.Archive,
                label = "Riwayat\nLaporan",
                tint = tint,
                onClick = onHistory,
                modifier = Modifier.weight(1f)
            )
            ShortcutCard(
                icon = Icons.Default.Notifications,
                label = "Notifikasi",
                tint = tint,
                onClick = onNotifications,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SosBanner(visible: Boolean, modifier: Modifier = Modifier) {
    AnimatedVisibility(visible = visible, modifier = modifier) {
        Surface(shape = CircleShape, color = SosRed) {
            Row(
                Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.NotificationsActive,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Help is on their way!",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White
                )
            }
        }
    }
}

private const val ArcOneStart = 192f
private const val ArcOneSweep = 88f
private const val ArcTwoStart = 0f
private const val ArcTwoSweep = 60f

private fun sectorPath(
    center: Offset,
    innerRadius: Float,
    outerRadius: Float,
    startAngle: Float,
    sweep: Float
): Path = Path().apply {
    arcTo(Rect(center, outerRadius), startAngle, sweep, true)
    arcTo(Rect(center, innerRadius), startAngle + sweep, -sweep, false)
    close()
}

@Composable
private fun SosButton(active: Boolean, size: Dp, onTriggered: () -> Unit) {
    val progress = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (pressed) 0.96f else 1f, label = "sosScale")
    val secondsLeft by remember {
        derivedStateOf { ceil((1f - progress.value) * 3f).toInt().coerceIn(1, 3) }
    }
    val currentOnTriggered by rememberUpdatedState(onTriggered)
    val ringColor = if (active) SosRed else MaterialTheme.colorScheme.primary
    val outlineColor = MaterialTheme.colorScheme.outline
    val faceBrush = if (active) {
        Brush.verticalGradient(listOf(SosRed, SosRedDeep))
    } else {
        Brush.verticalGradient(
            listOf(
                MaterialTheme.colorScheme.surface,
                MaterialTheme.colorScheme.surfaceContainerHigh
            )
        )
    }

    LaunchedEffect(active) {
        progress.snapTo(if (active) 1f else 0f)
    }

    Box(
        modifier = Modifier
            .size(size)
            .semantics {
                contentDescription = "Tombol SOS. Tekan dan tahan 3 detik untuk memanggil bantuan"
            }
            .pointerInput(active) {
                detectTapGestures(
                    onPress = {
                        if (!active) {
                            pressed = true
                            val job = scope.launch {
                                progress.snapTo(0f)
                                progress.animateTo(1f, tween(3000, easing = LinearEasing))
                                pressed = false
                                currentOnTriggered()
                            }
                            tryAwaitRelease()
                            pressed = false
                            if (job.isActive) {
                                job.cancel()
                                scope.launch { progress.animateTo(0f, tween(300)) }
                            }
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val innerR = this.size.minDimension * 0.42f
            val outerR = this.size.minDimension * 0.485f
            val lineWidth = 1.5.dp.toPx()

            val filled = progress.value * (ArcOneSweep + ArcTwoSweep)
            val fillOne = filled.coerceIn(0f, ArcOneSweep)
            val fillTwo = (filled - ArcOneSweep).coerceIn(0f, ArcTwoSweep)
            if (fillOne > 0f) {
                drawPath(sectorPath(center, innerR, outerR, ArcOneStart, fillOne), color = ringColor)
            }
            if (fillTwo > 0f) {
                drawPath(sectorPath(center, innerR, outerR, ArcTwoStart, fillTwo), color = ringColor)
            }
            drawPath(
                sectorPath(center, innerR, outerR, ArcOneStart, ArcOneSweep),
                color = outlineColor,
                style = Stroke(width = lineWidth)
            )
            drawPath(
                sectorPath(center, innerR, outerR, ArcTwoStart, ArcTwoSweep),
                color = outlineColor,
                style = Stroke(width = lineWidth)
            )
        }
        Box(
            modifier = Modifier
                .size(size * 0.82f)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .shadow(14.dp, CircleShape)
                .background(faceBrush),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "SOS",
                    fontSize = (size.value * 0.2f).sp,
                    fontWeight = FontWeight.Bold,
                    color = if (active) Color.White.copy(alpha = 0.92f)
                    else MaterialTheme.colorScheme.primary
                )
                if (pressed && !active) {
                    Text(
                        "Tahan... $secondsLeft",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ShortcutCard(
    icon: ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(76.dp),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(32.dp))
            Spacer(Modifier.width(12.dp))
            Box(
                Modifier
                    .width(1.dp)
                    .height(40.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )
            Spacer(Modifier.width(12.dp))
            Text(label, style = MaterialTheme.typography.labelLarge, maxLines = 2)
        }
    }
}
