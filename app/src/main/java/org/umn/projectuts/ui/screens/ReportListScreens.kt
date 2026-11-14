package org.umn.projectuts.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.clickable
import coil.compose.AsyncImage
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.umn.projectuts.data.ReportStatus
import org.umn.projectuts.data.TimeFilter
import org.umn.projectuts.ui.components.EmptyState
import org.umn.projectuts.ui.components.InfoRow
import org.umn.projectuts.ui.components.ReportListItem
import org.umn.projectuts.ui.components.SafeTabRow
import org.umn.projectuts.ui.components.SafeTopBar
import org.umn.projectuts.ui.components.StatusChip
import org.umn.projectuts.ui.components.statusColor
import org.umn.projectuts.util.DateUtils
import org.umn.projectuts.viewmodel.ReportsViewModel

@Composable
fun StatusScreen(vm: ReportsViewModel, onBack: () -> Unit, onOpenReport: (String) -> Unit) {
    LaunchedEffect(Unit) { vm.refresh() }

    val tabs: List<ReportStatus?> = listOf(null) + ReportStatus.entries
    val selectedIndex = tabs.indexOf(vm.statusFilter).coerceAtLeast(0)
    val reports = vm.statusFiltered

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        SafeTopBar(title = "Status Laporan", onBack = onBack)
        SafeTabRow(
            tabs = tabs.map { it?.label ?: "Semua" },
            selectedIndex = selectedIndex,
            onSelect = { vm.statusFilter = tabs[it] },
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        if (vm.isLoading) {
            LinearProgressIndicator(Modifier.fillMaxWidth())
        } else {
            Spacer(Modifier.height(4.dp))
        }

        vm.errorMessage?.let { message ->
            Text(
                message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
            )
        }

        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ReportStatus.entries.forEach { status ->
                Box(
                    Modifier
                        .size(10.dp)
                        .background(statusColor(status), CircleShape)
                )
                Spacer(Modifier.width(6.dp))
                Text(status.label, style = MaterialTheme.typography.labelSmall)
                Spacer(Modifier.width(14.dp))
            }
            Spacer(Modifier.weight(1f))
            Text(
                "${reports.size} laporan",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (reports.isEmpty()) {
            EmptyState(
                icon = Icons.Default.Description,
                message = "Belum ada laporan dengan status ini."
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(reports, key = { it.id }) { report ->
                    ReportListItem(report = report, onClick = { onOpenReport(report.id) })
                }
            }
        }
    }
}

@Composable
fun HistoryScreen(vm: ReportsViewModel, onBack: () -> Unit, onOpenReport: (String) -> Unit) {
    LaunchedEffect(Unit) { vm.refresh() }
    val focus = LocalFocusManager.current
    val results = vm.historyFiltered
    val grouped = results.groupBy { DateUtils.formatMonth(it.createdAt) }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        SafeTopBar(title = "Riwayat Laporan", onBack = onBack)

        OutlinedTextField(
            value = vm.query,
            onValueChange = { vm.query = it },
            placeholder = { Text("Cari laporan...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (vm.query.isNotEmpty()) {
                    IconButton(onClick = { vm.query = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "Hapus pencarian")
                    }
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(onSearch = { focus.clearFocus() }),
            shape = MaterialTheme.shapes.large,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
        )

        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TimeFilter.entries.forEach { filter ->
                FilterChip(
                    selected = vm.timeFilter == filter,
                    onClick = { vm.timeFilter = filter },
                    label = { Text(filter.label) }
                )
            }
        }

        val periodText = when (vm.timeFilter) {
            TimeFilter.ONE_MONTH -> "1 bulan terakhir"
            TimeFilter.THREE_MONTHS -> "3 bulan terakhir"
            TimeFilter.SIX_MONTHS -> "6 bulan terakhir"
            TimeFilter.ONE_YEAR -> "1 tahun terakhir"
            TimeFilter.ALL -> "semua waktu"
        }
        val sinceText = vm.historyCutoff?.let { " (sejak ${DateUtils.formatDate(it)})" } ?: ""
        Text(
            "${results.size} dari ${vm.reports.size} laporan · $periodText$sinceText",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
        )

        if (results.isEmpty()) {
            EmptyState(
                icon = Icons.Default.Search,
                message = "Tidak ada laporan yang cocok dengan pencarian atau filter waktu."
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                grouped.forEach { (month, list) ->
                    item(key = "header-$month") {
                        Text(
                            month,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                    items(list, key = { it.id }) { report ->
                        ReportListItem(report = report, onClick = { onOpenReport(report.id) })
                    }
                }
            }
        }
    }
}

@Composable
fun ReportDetailScreen(reportId: String, vm: ReportsViewModel, onBack: () -> Unit) {
    val report = vm.findById(reportId)
    var previewUrl by remember { mutableStateOf<String?>(null) }

    // Ambil foto bukti laporan ini dari Supabase Storage
    LaunchedEffect(reportId) { vm.loadEvidence(reportId) }

    // Tampilan foto layar penuh saat thumbnail diketuk
    previewUrl?.let { url ->
        Dialog(
            onDismissRequest = { previewUrl = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                AsyncImage(
                    model = url,
                    contentDescription = "Foto bukti",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
                IconButton(
                    onClick = { previewUrl = null },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .statusBarsPadding()
                        .padding(12.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Tutup", tint = Color.White)
                }
            }
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        SafeTopBar(title = "Detail Laporan", onBack = onBack)

        if (report == null) {
            EmptyState(
                icon = Icons.Default.Description,
                message = "Laporan dengan ID \"$reportId\" tidak ditemukan."
            )
            return@Column
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Text(
                report.category.label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(report.title, style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                StatusChip(report.status)
                Spacer(Modifier.width(10.dp))
                Text(
                    report.id,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(16.dp))
            Card(
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    InfoRow("Waktu kejadian", DateUtils.formatFull(report.createdAt))
                    InfoRow("Gedung", report.building)
                    InfoRow("Lantai / Ruang", report.room)
                    InfoRow("Deskripsi", report.description)
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("Bukti", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            when {
                vm.evidenceLoading -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        repeat(report.evidenceCount.coerceAtLeast(1)) {
                            Box(
                                modifier = Modifier
                                    .size(88.dp)
                                    .clip(MaterialTheme.shapes.small)
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
                            }
                        }
                    }
                }
                vm.evidenceUrls.isNotEmpty() -> {
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        vm.evidenceUrls.forEachIndexed { index, url ->
                            AsyncImage(
                                model = url,
                                contentDescription = "Bukti ${index + 1}",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(88.dp)
                                    .clip(MaterialTheme.shapes.small)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable(onClickLabel = "Lihat foto") { previewUrl = url }
                            )
                        }
                    }
                    Text(
                        "Ketuk foto untuk memperbesar.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
                else -> {
                    Text(
                        vm.evidenceError ?: "Tidak ada bukti dilampirkan.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (vm.evidenceError != null) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("Progres Laporan", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            StatusTimeline(report.status)

            report.officerNote?.let { note ->
                Spacer(Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            "Catatan petugas",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            note,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun StatusTimeline(status: ReportStatus) {
    val finalLabel = if (status == ReportStatus.DITOLAK) "Ditolak" else "Selesai"
    val steps = listOf("Laporan diterima", "Sedang diproses", finalLabel)
    val reached = when (status) {
        ReportStatus.DIPROSES -> 1
        ReportStatus.SELESAI, ReportStatus.DITOLAK -> 2
    }
    Column {
        steps.forEachIndexed { index, label ->
            val done = index <= reached
            val color = if (done) {
                if (index == 2) statusColor(status) else MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outlineVariant
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        Modifier
                            .size(14.dp)
                            .background(color, CircleShape)
                    )
                    if (index < steps.lastIndex) {
                        Box(
                            Modifier
                                .width(2.dp)
                                .height(22.dp)
                                .background(
                                    if (index < reached) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outlineVariant
                                )
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (done) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = if (index < steps.lastIndex) 22.dp else 0.dp)
                )
            }
        }
    }
}
