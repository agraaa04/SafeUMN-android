package org.umn.projectuts.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.umn.projectuts.ui.components.SafeTopBar
import org.umn.projectuts.ui.components.SafeUmnWordmark
import org.umn.projectuts.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(
    vm: ProfileViewModel,
    onAccount: () -> Unit,
    onNotif: () -> Unit,
    onHelp: () -> Unit,
    onTerms: () -> Unit,
    onSignOut: () -> Unit
) {
    var showSignOutDialog by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            SafeUmnWordmark()
        }
        Spacer(Modifier.height(20.dp))
        Text(
            "Profile Settings",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        Card(
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(vm.profile.name, style = MaterialTheme.typography.titleMedium)
                    Text(
                        vm.profile.email,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(onClick = onAccount, shape = CircleShape) { Text("Edit") }
            }
        }

        Spacer(Modifier.height(20.dp))
        Text(
            "Other Settings",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        Card(
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                SettingsRow("Account information", onAccount)
                HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                SettingsRow("Notification", onNotif)
                HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                SettingsRow("Help & Support", onHelp)
                HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                SettingsRow("Terms and Privacy Policy", onTerms)
            }
        }

        Spacer(Modifier.height(24.dp))
        FilledTonalButton(
            onClick = { showSignOutDialog = true },
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            ),
            shape = MaterialTheme.shapes.large,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Sign out", style = MaterialTheme.typography.titleMedium)
        }
    }

    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = { Text("Keluar dari akun?") },
            text = { Text("Anda perlu login kembali untuk memakai SafeUMN.") },
            confirmButton = {
                TextButton(onClick = {
                    showSignOutDialog = false
                    onSignOut()
                }) { Text("Sign out") }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) { Text("Batal") }
            }
        )
    }
}

@Composable
private fun SettingsRow(title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline
        )
    }
}

@Composable
fun AccountScreen(vm: ProfileViewModel, onBack: () -> Unit) {
    LaunchedEffect(Unit) { vm.startEdit() }
    LaunchedEffect(vm.justSaved) {
        if (vm.justSaved) {
            delay(2500)
            vm.clearSavedFlag()
        }
    }
    val show = vm.saveAttempted

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
    ) {
        SafeTopBar(title = "Account information", onBack = onBack)
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            if (vm.justSaved) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Text(
                        "Perubahan profil berhasil disimpan.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }

            vm.errorMessage?.let { message ->
                Text(
                    message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            OutlinedTextField(
                value = vm.editName,
                onValueChange = { vm.editName = it },
                label = { Text("Nama lengkap") },
                singleLine = true,
                isError = show && vm.nameError != null,
                supportingText = { if (show) vm.nameError?.let { Text(it) } },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                ),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = vm.editEmail,
                onValueChange = { },
                label = { Text("Email") },
                singleLine = true,
                readOnly = true,
                enabled = false,
                supportingText = { Text("Email login tidak dapat diubah") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = vm.editNim,
                onValueChange = { vm.onNimChange(it) },
                label = { Text("NIM") },
                singleLine = true,
                isError = show && vm.nimError != null,
                supportingText = {
                    Text(if (show) vm.nimError ?: "11 digit angka" else "11 digit angka")
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = vm.editPhone,
                onValueChange = { vm.onPhoneChange(it) },
                label = { Text("Nomor HP") },
                singleLine = true,
                isError = show && vm.phoneError != null,
                supportingText = {
                    Text(if (show) vm.phoneError ?: "9-13 digit angka" else "9-13 digit angka")
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Done
                ),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            )
        }
        Box(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            org.umn.projectuts.ui.components.PrimaryActionButton(
                text = "Simpan Perubahan",
                onClick = { vm.save() },
                loading = vm.isSaving
            )
        }
    }
}

@Composable
fun NotifSettingsScreen(vm: ProfileViewModel, onBack: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        SafeTopBar(title = "Notification", onBack = onBack)
        Column(Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
            vm.errorMessage?.let { message ->
                Text(
                    message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            SwitchRow(
                title = "Emergency Alerts",
                description = "Kamu akan mendapatkan notifikasi Emergency Alert dari pengguna lain.",
                checked = vm.emergencyAlerts,
                onCheckedChange = { vm.onEmergencyAlertsChange(it) }
            )
            HorizontalDivider()
            SwitchRow(
                title = "Incident Status Alerts",
                description = "Kamu akan mendapatkan notifikasi status dari incident yang di laporkan.",
                checked = vm.incidentStatusAlerts,
                onCheckedChange = { vm.onIncidentStatusAlertsChange(it) }
            )
            HorizontalDivider()
            SwitchRow(
                title = "General Announcements",
                description = "Kamu akan mendapatkan notifikasi pemberitahuan umum seperti perbaikan lift.",
                checked = vm.generalAnnouncements,
                onCheckedChange = { vm.onGeneralAnnouncementsChange(it) }
            )
        }
    }
}

@Composable
private fun SwitchRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.width(12.dp))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

private val Faqs = listOf(
    "Bagaimana cara memakai tombol SOS?" to
        "Tekan dan tahan tombol SOS di halaman Home selama 3 detik. Lokasi Anda otomatis dikirim ke petugas Security dan Building Management.",
    "Bagaimana membuat laporan insiden?" to
        "Buka Home > Buat Laporan, pilih kategori, isi lokasi kejadian, tulis detail dan tambahkan foto bukti (opsional), lalu konfirmasi.",
    "Apa arti status laporan?" to
        "Diproses: laporan sedang ditangani petugas. Selesai: laporan sudah ditindaklanjuti. Ditolak: laporan tidak dapat diproses (misalnya duplikat).",
    "Apakah laporan kasus sensitif dirahasiakan?" to
        "Ya. Laporan perundungan dan kekerasan seksual ditangani secara privat oleh petugas yang berwenang.",
    "Siapa yang menerima laporan saya?" to
        "Laporan diterima oleh tim Security dan Building Management kampus UMN."
)

@Composable
fun HelpScreen(onBack: () -> Unit) {
    var expandedIndex by rememberSaveable { mutableStateOf(-1) }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        SafeTopBar(title = "Help & Support", onBack = onBack)
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Text(
                "Pertanyaan yang sering diajukan (FAQ)",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Faqs.forEachIndexed { index, (question, answer) ->
                val expanded = expandedIndex == index
                Card(
                    onClick = { expandedIndex = if (expanded) -1 else index },
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                question,
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = if (expanded) "Tutup jawaban" else "Buka jawaban"
                            )
                        }
                        if (expanded) {
                            Spacer(Modifier.height(8.dp))
                            Text(answer, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TermsScreen(onBack: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        SafeTopBar(title = "Terms and Privacy Policy", onBack = onBack)
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            TermsSection(
                "Ketentuan Penggunaan",
                "SafeUMN digunakan untuk melaporkan kondisi darurat dan insiden di lingkungan kampus UMN. Penyalahgunaan tombol SOS atau laporan palsu dapat dikenai sanksi sesuai peraturan kampus."
            )
            TermsSection(
                "Data yang Dikumpulkan",
                "Aplikasi memproses data akun, lokasi kejadian, deskripsi laporan, dan foto bukti hanya untuk keperluan penanganan insiden."
            )
            TermsSection(
                "Privasi",
                "Laporan kasus sensitif (perundungan, kekerasan seksual) hanya dapat diakses oleh petugas yang berwenang dan tidak dibagikan kepada pihak lain."
            )
        }
    }
}

@Composable
private fun TermsSection(title: String, body: String) {
    Text(title, style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(4.dp))
    Text(
        body,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(Modifier.height(18.dp))
}
