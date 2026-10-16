package work.baki.kodkopyala

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.SharedPreferences

object Prefs {
    const val AUTO_COPY = "auto_copy"
    const val SHOW_NOTIFICATION = "show_notification"
    const val ALL_APPS = "all_apps"

    /** Varsayılan olarak sadece bu SMS uygulamalarının bildirimleri okunur. */
    val SMS_PACKAGES = setOf(
        "org.fossify.messages",              // Fossify Messages
        "com.google.android.apps.messaging", // Google Messages
        "com.samsung.android.messaging",     // Samsung Messages
    )

    fun get(context: Context): SharedPreferences =
        context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    fun autoCopy(c: Context) = get(c).getBoolean(AUTO_COPY, true)
    fun showNotification(c: Context) = get(c).getBoolean(SHOW_NOTIFICATION, true)
    fun allApps(c: Context) = get(c).getBoolean(ALL_APPS, false)
}

object Clipboard {
    fun copy(context: Context, code: String) {
        val cm = context.getSystemService(ClipboardManager::class.java) ?: return
        cm.setPrimaryClip(ClipData.newPlainText("Doğrulama kodu", code))
    }
}
