package work.baki.kodkopyala

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

/**
 * SMS uygulamasının bildirimlerini dinler, koddaki sayıyı bulur,
 * panoya kopyalar ve "Kopyala" butonlu kendi bildirimini gösterir.
 * Mesajlar hiçbir yere gönderilmez, saklanmaz.
 */
class OtpListenerService : NotificationListenerService() {

    private var lastCode: String? = null
    private var lastTime = 0L

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.packageName == packageName) return
        if (!Prefs.allApps(this) && sbn.packageName !in Prefs.SMS_PACKAGES) return

        val extras = sbn.notification.extras
        val text = buildString {
            append(extras.getCharSequence(Notification.EXTRA_TITLE) ?: "")
            append('\n')
            append(extras.getCharSequence(Notification.EXTRA_BIG_TEXT)
                ?: extras.getCharSequence(Notification.EXTRA_TEXT) ?: "")
        }

        val code = OtpExtractor.extract(text) ?: return

        // Aynı bildirim birkaç kez güncellenebiliyor; 60 sn içinde aynı kodu tekrar işleme.
        val now = SystemClock.elapsedRealtime()
        if (code == lastCode && now - lastTime < 60_000) return
        lastCode = code
        lastTime = now

        if (Prefs.autoCopy(this)) Clipboard.copy(this, code)
        if (Prefs.showNotification(this)) showCodeNotification(this, code)
    }

    companion object {
        private const val CHANNEL_ID = "codes"
        const val NOTIFICATION_ID = 1001

        fun showCodeNotification(context: Context, code: String) {
            val nm = context.getSystemService(NotificationManager::class.java) ?: return
            nm.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "Doğrulama kodları", NotificationManager.IMPORTANCE_HIGH)
            )

            val copyIntent = Intent(context, CopyReceiver::class.java)
                .putExtra(CopyReceiver.EXTRA_CODE, code)
            val pending = PendingIntent.getBroadcast(
                context, code.hashCode(), copyIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val copied = Prefs.autoCopy(context)
            val notification = Notification.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Kod: $code")
                .setContentText(if (copied) "Panoya kopyalandı" else "Kopyalamak için dokun")
                .setContentIntent(pending)
                .addAction(Notification.Action.Builder(null, "Kopyala", pending).build())
                .setAutoCancel(true)
                .setTimeoutAfter(5 * 60_000L) // 5 dk sonra kendiliğinden kaybolur
                .setCategory(Notification.CATEGORY_STATUS)
                .setVisibility(Notification.VISIBILITY_PRIVATE)
                .build()

            nm.notify(NOTIFICATION_ID, notification)
        }
    }
}
