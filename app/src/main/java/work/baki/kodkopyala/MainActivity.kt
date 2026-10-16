package work.baki.kodkopyala

import android.Manifest
import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView

class MainActivity : Activity() {

    private lateinit var status: TextView
    private lateinit var accessButton: Button
    private lateinit var restrictedHint: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val pad = dp(20)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, pad, pad, pad)
        }

        root.addView(TextView(this).apply {
            text = getString(R.string.app_name)
            textSize = 24f
        })
        root.addView(TextView(this).apply {
            text = "SMS ile gelen doğrulama kodunu bulur ve panoya kopyalar. " +
                "İnternet izni yoktur; mesajlar hiçbir yere gönderilmez."
            setPadding(0, dp(8), 0, dp(16))
        })

        status = TextView(this).apply { textSize = 16f }
        root.addView(status)

        accessButton = Button(this).apply {
            text = "Bildirim erişimini aç"
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            }
        }
        root.addView(accessButton)

        restrictedHint = TextView(this).apply {
            text = "İzin gri ve açılmıyorsa: aşağıdaki butonla App info'yu aç → sağ üstte ⋮ → " +
                "\"Allow restricted settings\" → sonra tekrar bildirim erişimini aç."
            setPadding(0, dp(8), 0, 0)
        }
        root.addView(restrictedHint)
        root.addView(Button(this).apply {
            text = "App info'yu aç"
            setOnClickListener {
                startActivity(
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName"))
                )
            }
        })

        root.addView(section("Ayarlar"))
        root.addView(prefSwitch("Kodu otomatik panoya kopyala", Prefs.AUTO_COPY, true))
        root.addView(prefSwitch("\"Kopyala\" butonlu bildirim göster", Prefs.SHOW_NOTIFICATION, true))
        root.addView(prefSwitch("Tüm uygulamaların bildirimlerini tara (sadece SMS değil)", Prefs.ALL_APPS, false))

        root.addView(section("Dene"))
        val input = EditText(this).apply {
            hint = "Örnek bir SMS yapıştır"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
            minLines = 2
        }
        root.addView(input)
        val result = TextView(this).apply { setPadding(0, dp(8), 0, 0) }
        root.addView(Button(this).apply {
            text = "Kodu bul"
            setOnClickListener {
                val code = OtpExtractor.extract(input.text.toString())
                result.text = if (code != null) "Bulunan kod: $code" else "Kod bulunamadı"
                if (code != null && Prefs.showNotification(this@MainActivity)) {
                    OtpListenerService.showCodeNotification(this@MainActivity, code)
                }
            }
        })
        root.addView(result)

        setContentView(ScrollView(this).apply { addView(root) })

        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
        }
    }

    override fun onResume() {
        super.onResume()
        val granted = hasNotificationAccess()
        status.text = if (granted) "✅ Bildirim erişimi açık, çalışıyor." else "⚠️ Bildirim erişimi kapalı."
        accessButton.visibility = if (granted) View.GONE else View.VISIBLE
        restrictedHint.visibility = if (granted) View.GONE else View.VISIBLE
    }

    private fun hasNotificationAccess(): Boolean {
        val flat = Settings.Secure.getString(contentResolver, "enabled_notification_listeners") ?: return false
        val me = ComponentName(this, OtpListenerService::class.java)
        return flat.split(':').any { ComponentName.unflattenFromString(it) == me }
    }

    private fun prefSwitch(label: String, key: String, default: Boolean) = Switch(this).apply {
        text = label
        isChecked = Prefs.get(context).getBoolean(key, default)
        setPadding(0, dp(8), 0, dp(8))
        setOnCheckedChangeListener { _, checked ->
            Prefs.get(context).edit().putBoolean(key, checked).apply()
        }
    }

    private fun section(title: String) = TextView(this).apply {
        text = title
        textSize = 18f
        gravity = Gravity.START
        setPadding(0, dp(24), 0, dp(4))
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
}
