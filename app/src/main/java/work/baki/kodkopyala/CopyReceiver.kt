package work.baki.kodkopyala

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast

/** Bildirimdeki "Kopyala" butonu buraya gelir. */
class CopyReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val code = intent.getStringExtra(EXTRA_CODE) ?: return
        Clipboard.copy(context, code)

        // Android 13+ kopyalamayı zaten kendisi gösteriyor; eskilerde biz gösterelim.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            Toast.makeText(context, "$code kopyalandı", Toast.LENGTH_SHORT).show()
        }

        context.getSystemService(NotificationManager::class.java)
            ?.cancel(OtpListenerService.NOTIFICATION_ID)
    }

    companion object {
        const val EXTRA_CODE = "code"
    }
}
