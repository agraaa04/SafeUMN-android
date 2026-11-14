package org.umn.projectuts.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import org.umn.projectuts.ui.screens.AccountScreen
import org.umn.projectuts.ui.screens.AnnouncementDetailScreen
import org.umn.projectuts.ui.screens.HelpScreen
import org.umn.projectuts.ui.screens.HistoryScreen
import org.umn.projectuts.ui.screens.HomeScreen
import org.umn.projectuts.ui.screens.InboxScreen
import org.umn.projectuts.ui.screens.LoginScreen
import org.umn.projectuts.ui.screens.NotifSettingsScreen
import org.umn.projectuts.ui.screens.ProfileScreen
import org.umn.projectuts.ui.screens.ReportCameraScreen
import org.umn.projectuts.ui.screens.ReportCategoryScreen
import org.umn.projectuts.ui.screens.ReportDetailScreen
import org.umn.projectuts.ui.screens.ReportInputScreen
import org.umn.projectuts.ui.screens.ReportLocationScreen
import org.umn.projectuts.ui.screens.ReportSuccessScreen
import org.umn.projectuts.ui.screens.ReportSummaryScreen
import org.umn.projectuts.ui.screens.SplashScreen
import org.umn.projectuts.ui.screens.StatusScreen
import org.umn.projectuts.ui.screens.TermsScreen
import org.umn.projectuts.viewmodel.LoginViewModel
import org.umn.projectuts.viewmodel.InboxViewModel
import org.umn.projectuts.viewmodel.ProfileViewModel
import org.umn.projectuts.viewmodel.ReportFlowViewModel
import org.umn.projectuts.viewmodel.ReportsViewModel
import org.umn.projectuts.viewmodel.SosViewModel

private fun navigateToTab(navController: NavHostController, tab: BottomTab) {
    if (tab == BottomTab.HOME) {
        // Kembali ke Home yang sudah ada di back stack.
        // Tidak memakai saveState/restoreState di sini, karena state yang tersimpan
        // atas nama Home berisi tab sebelumnya (mis. Inbox) dan akan memunculkannya lagi.
        val returned = navController.popBackStack(Routes.HOME, inclusive = false)
        if (!returned) {
            navController.navigate(Routes.HOME) { launchSingleTop = true }
        }
        return
    }
    navController.navigate(tab.route) {
        popUpTo(Routes.HOME) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
fun SafeUmnApp() {
    val navController = rememberNavController()

    val loginVm: LoginViewModel = viewModel()
    val profileVm: ProfileViewModel = viewModel()
    val reportsVm: ReportsViewModel = viewModel()
    val flowVm: ReportFlowViewModel = viewModel()
    val sosVm: SosViewModel = viewModel()
    val inboxVm: InboxViewModel = viewModel()

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentTab = tabForRoute(backStackEntry?.destination?.route)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
        bottomBar = {
            if (currentTab != null) {
                AppBottomBar(selected = currentTab) { tab -> navigateToTab(navController, tab) }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.SPLASH,
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
        ) {
            // Splash & Login
            composable(Routes.SPLASH) {
                SplashScreen {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            }
            composable(Routes.LOGIN) {
                LoginScreen(vm = loginVm) { email ->
                    // Login Supabase Auth berhasil -> ambil profil dari tbl_users,
                    // lalu muat laporan milik user tersebut
                    profileVm.loadCurrentUser(
                        onLoaded = {
                            reportsVm.refresh()
                            navController.navigate(Routes.HOME) {
                                popUpTo(Routes.LOGIN) { inclusive = true }
                            }
                        },
                        onError = { message -> loginVm.showError(message) }
                    )
                }
            }

            // Home
            composable(Routes.HOME) {
                HomeScreen(
                    sosVm = sosVm,
                    userName = profileVm.firstName,
                    onCreateReport = {
                        flowVm.reset()
                        navController.navigate(Routes.REPORT_CATEGORY)
                    },
                    onStatus = { navController.navigate(Routes.STATUS) },
                    onHistory = { navController.navigate(Routes.HISTORY) },
                    onNotifications = { navController.navigate(Routes.inbox("alert")) }
                )
            }

            // Alur Buat Laporan
            composable(Routes.REPORT_CATEGORY) {
                ReportCategoryScreen(
                    flowVm = flowVm,
                    onBack = { navController.popBackStack() },
                    onNext = { navController.navigate(Routes.REPORT_LOCATION) }
                )
            }
            composable(Routes.REPORT_LOCATION) {
                ReportLocationScreen(
                    flowVm = flowVm,
                    onBack = { navController.popBackStack() },
                    onNext = { navController.navigate(Routes.REPORT_INPUT) }
                )
            }
            composable(Routes.REPORT_INPUT) {
                ReportInputScreen(
                    flowVm = flowVm,
                    onBack = { navController.popBackStack() },
                    onOpenCamera = { navController.navigate(Routes.REPORT_CAMERA) },
                    onNext = { navController.navigate(Routes.REPORT_SUMMARY) }
                )
            }
            composable(Routes.REPORT_CAMERA) {
                ReportCameraScreen(
                    onClose = { navController.popBackStack() },
                    onPhotoTaken = { file ->
                        flowVm.addEvidence(file, fromCamera = true)
                        navController.popBackStack()
                    }
                )
            }
            composable(Routes.REPORT_SUMMARY) {
                ReportSummaryScreen(
                    flowVm = flowVm,
                    reportsVm = reportsVm,
                    onBack = { navController.popBackStack() },
                    onSubmitted = { newId ->
                        flowVm.reset()
                        navController.navigate(Routes.reportSuccess(newId)) {
                            popUpTo(Routes.HOME)
                        }
                    }
                )
            }
            composable(
                route = Routes.REPORT_SUCCESS,
                arguments = listOf(navArgument(Routes.ARG_REPORT_ID) { type = NavType.StringType })
            ) { entry ->
                val reportId = entry.arguments?.getString(Routes.ARG_REPORT_ID).orEmpty()
                ReportSuccessScreen(
                    reportId = reportId,
                    onCheckStatus = {
                        navController.navigate(Routes.STATUS) { popUpTo(Routes.HOME) }
                    },
                    onDone = {
                        navController.popBackStack(Routes.HOME, inclusive = false)
                    }
                )
            }

            // Status, Riwayat, Detail (argumen reportId)
            composable(Routes.STATUS) {
                StatusScreen(
                    vm = reportsVm,
                    onBack = { navController.popBackStack() },
                    onOpenReport = { id -> navController.navigate(Routes.reportDetail(id)) }
                )
            }
            composable(Routes.HISTORY) {
                HistoryScreen(
                    vm = reportsVm,
                    onBack = { navController.popBackStack() },
                    onOpenReport = { id -> navController.navigate(Routes.reportDetail(id)) }
                )
            }
            composable(
                route = Routes.REPORT_DETAIL,
                arguments = listOf(navArgument(Routes.ARG_REPORT_ID) { type = NavType.StringType })
            ) { entry ->
                val reportId = entry.arguments?.getString(Routes.ARG_REPORT_ID).orEmpty()
                ReportDetailScreen(
                    reportId = reportId,
                    vm = reportsVm,
                    onBack = { navController.popBackStack() }
                )
            }

            // Inbox & Alert
            composable(
                route = Routes.INBOX,
                arguments = listOf(
                    navArgument(Routes.ARG_TAB) {
                        type = NavType.StringType
                        defaultValue = "inbox"
                    }
                )
            ) { entry ->
                val tab = entry.arguments?.getString(Routes.ARG_TAB) ?: "inbox"
                InboxScreen(
                    vm = inboxVm,
                    initialTab = tab,
                    userName = profileVm.firstName,
                    onOpenAnnouncement = { id -> navController.navigate(Routes.announcement(id)) }
                )
            }
            composable(
                route = Routes.ANNOUNCEMENT,
                arguments = listOf(navArgument(Routes.ARG_ANNOUNCEMENT_ID) { type = NavType.StringType })
            ) { entry ->
                val id = entry.arguments?.getString(Routes.ARG_ANNOUNCEMENT_ID).orEmpty()
                AnnouncementDetailScreen(
                    vm = inboxVm,
                    announcementId = id,
                    onBack = { navController.popBackStack() }
                )
            }

            //  Profil
            composable(Routes.PROFILE) {
                ProfileScreen(
                    vm = profileVm,
                    onAccount = { navController.navigate(Routes.ACCOUNT) },
                    onNotif = { navController.navigate(Routes.NOTIF_SETTINGS) },
                    onHelp = { navController.navigate(Routes.HELP) },
                    onTerms = { navController.navigate(Routes.TERMS) },
                    onSignOut = {
                        loginVm.reset()
                        flowVm.reset()
                        reportsVm.clear()
                        // SOS aktif dibatalkan dulu (masih butuh sesi login), baru keluar
                        sosVm.cancel { profileVm.signOut() }
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.HOME) { inclusive = true }
                        }
                    }
                )
            }
            composable(Routes.ACCOUNT) {
                AccountScreen(vm = profileVm, onBack = { navController.popBackStack() })
            }
            composable(Routes.NOTIF_SETTINGS) {
                NotifSettingsScreen(vm = profileVm, onBack = { navController.popBackStack() })
            }
            composable(Routes.HELP) {
                HelpScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.TERMS) {
                TermsScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
