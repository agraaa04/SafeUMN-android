package org.umn.projectuts.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import org.umn.projectuts.data.AlertItem
import org.umn.projectuts.data.AlertSeverity
import org.umn.projectuts.data.Announcement
import org.umn.projectuts.data.AnnouncementIcon
import org.umn.projectuts.ui.components.EmptyState
import org.umn.projectuts.ui.components.SafeTabRow
import org.umn.projectuts.ui.components.SafeTopBar
import org.umn.projectuts.ui.components.ScreenHeader
import org.umn.projectuts.ui.theme.AlertEmergency
import org.umn.projectuts.ui.theme.AlertSafety
import org.umn.projectuts.viewmodel.InboxViewModel

private fun announcementIcon(icon: AnnouncementIcon): ImageVector = when (icon) {
    AnnouncementIcon.PATROL -> Icons.Default.Security
    AnnouncementIcon.FIRE_DRILL -> Icons.Default.LocalFireDepartment
    AnnouncementIcon.MAINTENANCE -> Icons.Default.Build
    AnnouncementIcon.PROCEDURE -> Icons.Default.Info
}

@Composable
fun InboxScreen(
    vm: InboxViewModel,
    initialTab: String,
    userName: String,
    onOpenAnnouncement: (String) -> Unit
) {
    LaunchedEffect(Unit) { vm.refresh() }
    var selectedTab by rememberSaveable(initialTab) {
        mutableStateOf(if (initialTab == "alert") 1 else 0)
    }
    var alertFilter by remember { mutableStateOf<AlertSeverity?>(null) }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        ScreenHeader(
            userName = userName,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
        )
        SafeTabRow(
            tabs = listOf("Inbox", "Alert (${vm.alerts.size})"),
            selectedIndex = selectedTab,
            onSelect = { selectedTab = it },
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        if (vm.isLoading) {
            LinearProgressIndicator(Modifier.fillMaxWidth())
        }
        vm.errorMessage?.let { message ->
            Text(
                message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
            )
        }

        if (selectedTab == 0) {
            Text(
                "Announcement",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(start = 20.dp, top = 14.dp)
            )
            Text(
                "Welcome to the safe umn!",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 20.dp, bottom = 6.dp)
            )
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(vm.announcements, key = { it.id }) { item ->
                    AnnouncementCard(item) { onOpenAnnouncement(item.id) }
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = alertFilter == null,
                    onClick = { alertFilter = null },
                    label = { Text("Semua") }
                )
                FilterChip(
                    selected = alertFilter == AlertSeverity.EMERGENCY,
                    onClick = { alertFilter = AlertSeverity.EMERGENCY },
                    label = { Text("Darurat") }
                )
                FilterChip(
                    selected = alertFilter == AlertSeverity.SAFETY,
                    onClick = { alertFilter = AlertSeverity.SAFETY },
                    label = { Text("Keselamatan") }
                )
            }
            val alerts = vm.alerts.filter { alertFilter == null || it.severity == alertFilter }
            if (alerts.isEmpty()) {
                EmptyState(Icons.Default.NotificationsActive, "Tidak ada alert pada kategori ini.")
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(alerts, key = { it.id }) { alert -> AlertCard(alert) }
                }
            }
        }
    }
}

@Composable
private fun AnnouncementCard(item: Announcement, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    announcementIcon(item.icon),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(item.title, style = MaterialTheme.typography.titleSmall)
                Text(
                    item.summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AlertCard(alert: AlertItem) {
    val color = if (alert.severity == AlertSeverity.EMERGENCY) AlertEmergency else AlertSafety
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.height(IntrinsicSize.Min)) {
            Box(
                Modifier
                    .width(8.dp)
                    .fillMaxHeight()
                    .background(color)
            )
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(10.dp)
                            .background(color, CircleShape)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(alert.title, style = MaterialTheme.typography.titleSmall)
                }
                Text(alert.message, style = MaterialTheme.typography.bodyMedium)
                Text(
                    alert.timeAgo,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun AnnouncementDetailScreen(vm: InboxViewModel, announcementId: String, onBack: () -> Unit) {
    val item = vm.findAnnouncement(announcementId)

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        SafeTopBar(title = "Pengumuman", onBack = onBack)
        if (item == null) {
            EmptyState(Icons.Default.Inbox, "Pengumuman tidak ditemukan.")
        } else {
            Column(
                Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        announcementIcon(item.icon),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(30.dp)
                    )
                }
                Spacer(Modifier.height(16.dp))
                Text(item.title, style = MaterialTheme.typography.headlineSmall)
                Text(
                    item.summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(16.dp))
                Text(item.body, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}
