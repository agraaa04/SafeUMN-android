package org.umn.projectuts.data

/** Menyimpan user_id (tbl_users) milik akun yang sedang login. */
object UserSession {
    var userId: Int? = null

    fun requireUserId(): Int =
        userId ?: throw IllegalStateException("Belum login")

    fun clear() {
        userId = null
    }
}
