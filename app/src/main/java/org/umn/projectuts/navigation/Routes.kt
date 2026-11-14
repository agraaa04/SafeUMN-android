package org.umn.projectuts.navigation

object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val HOME = "home"

    // Alur Buat Laporan
    const val REPORT_CATEGORY = "report_category"
    const val REPORT_LOCATION = "report_location"
    const val REPORT_INPUT = "report_input"
    const val REPORT_CAMERA = "report_camera"
    const val REPORT_SUMMARY = "report_summary"
    const val REPORT_SUCCESS = "report_success/{reportId}"

    // Status & Riwayat
    const val STATUS = "status"
    const val HISTORY = "history"
    const val REPORT_DETAIL = "report/{reportId}"

    // Inbox & Alert
    const val INBOX = "inbox?tab={tab}"
    const val ANNOUNCEMENT = "announcement/{announcementId}"

    // Profil
    const val PROFILE = "profile"
    const val ACCOUNT = "account"
    const val NOTIF_SETTINGS = "notif_settings"
    const val HELP = "help"
    const val TERMS = "terms"

    // Nama argumen
    const val ARG_REPORT_ID = "reportId"
    const val ARG_ANNOUNCEMENT_ID = "announcementId"
    const val ARG_TAB = "tab"

    // Helper pembuat route dengan argumen
    fun reportDetail(id: String) = "report/$id"
    fun reportSuccess(id: String) = "report_success/$id"
    fun announcement(id: String) = "announcement/$id"
    fun inbox(tab: String = "inbox") = "inbox?tab=$tab"
}
