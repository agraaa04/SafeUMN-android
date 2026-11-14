package org.umn.projectuts.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

enum class BottomTab(
    val label: String,
    val route: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector
) {
    HOME("Home", Routes.HOME, Icons.Outlined.Home, Icons.Filled.Home),
    INBOX("Inbox", Routes.inbox(), Icons.Outlined.Email, Icons.Filled.Email),
    PROFILE("Profil", Routes.PROFILE, Icons.Outlined.Person, Icons.Filled.Person)
}

fun tabForRoute(route: String?): BottomTab? = when {
    route == null -> null
    route == Routes.SPLASH || route == Routes.LOGIN || route == Routes.REPORT_CAMERA -> null
    route.startsWith("inbox") || route.startsWith("announcement") -> BottomTab.INBOX
    route == Routes.PROFILE || route == Routes.ACCOUNT || route == Routes.NOTIF_SETTINGS ||
        route == Routes.HELP || route == Routes.TERMS -> BottomTab.PROFILE
    else -> BottomTab.HOME
}

@Composable
fun AppBottomBar(selected: BottomTab, onSelect: (BottomTab) -> Unit) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        BottomTab.entries.forEach { tab ->
            val isSelected = tab == selected
            NavigationBarItem(
                selected = isSelected,
                onClick = { onSelect(tab) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) tab.selectedIcon else tab.icon,
                        contentDescription = tab.label
                    )
                },
                label = { Text(tab.label) }
            )
        }
    }
}
