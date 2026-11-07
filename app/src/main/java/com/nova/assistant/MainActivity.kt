package com.nova.assistant

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class MainActivity : Activity() {

    private val needed: Array<String> by lazy {
        val list = mutableListOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.CALL_PHONE
        )
        if (Build.VERSION.SDK_INT >= 33) list.add(Manifest.permission.POST_NOTIFICATIONS)
        list.toTypedArray()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val pad = (24 * resources.displayMetrics.density).toInt()
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(pad, pad * 2, pad, pad)
        }
        root.addView(TextView(this).apply { text = "Nova"; textSize = 40f })
        root.addView(TextView(this).apply {
            text = "No key needed. The first start takes about a minute. Say \"Nova\", pause a moment, then your command:\n\n" +
                "open Instagram\ncall Amma\ncall Amma on WhatsApp\nvideo call Amma on WhatsApp\nplay Anirudh on Spotify\npause / next song / previous song\n" +
                "volume up / volume down\nflashlight on / off\nbattery level\n" +
                "set a timer for 5 minutes\nwhat time is it\nhello / how are you / tell me a joke\n" +
                "wifi settings / bluetooth settings / go home\nsearch for biryani near me\n\nதமிழ்: வாட்ஸ்அப் திற, அம்மாவுக்கு கால் பண்ணு,\nவாட்ஸ்அப்பில் அம்மாவுக்கு கால் பண்ணு, ஜிபே திற,\nநேரம் என்ன, ஒலி கூட்டு, டார்ச் ஆன்\nSay \"Tamil mode\" or \"English mode\" to switch."
            textSize = 16f
            setPadding(0, pad / 2, 0, pad)
        })
        val langPrefs = getSharedPreferences("nova", MODE_PRIVATE)
        val langStatus = TextView(this).apply {
            textSize = 14f
            text = "Language: " + (if (langPrefs.getString("lang", "en") == "ta") "தமிழ் (Tamil)" else "English")
        }
        root.addView(langStatus)
        root.addView(Button(this).apply {
            text = "English"
            setOnClickListener { langPrefs.edit().putString("lang", "en").apply(); langStatus.text = "Language: English" }
        })
        root.addView(Button(this).apply {
            text = "தமிழ் (Tamil)"
            setOnClickListener { langPrefs.edit().putString("lang", "ta").apply(); langStatus.text = "Language: தமிழ் (Tamil)" }
        })
        root.addView(Button(this).apply {
            text = "1. Allow \"Display over other apps\""
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
            }
        })
        root.addView(Button(this).apply {
            text = "2. Allow \"Do not disturb access\" (silences beeps)"
            setOnClickListener { startActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)) }
        })
        root.addView(Button(this).apply {
            text = "3. Start Nova"
            setOnClickListener { startNova() }
        })
        root.addView(Button(this).apply {
            text = "Stop Nova"
            setOnClickListener { stopService(Intent(this@MainActivity, NovaService::class.java)) }
        })
        setContentView(ScrollView(this).apply { addView(root) })
    }

    private fun startNova() {
        val missing = needed.filter { checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED }
        if (missing.isNotEmpty()) {
            requestPermissions(missing.toTypedArray(), 1)
            return
        }
        val i = Intent(this, NovaService::class.java)
        if (Build.VERSION.SDK_INT >= 26) startForegroundService(i) else startService(i)
    }

    override fun onRequestPermissionsResult(code: Int, perms: Array<out String>, results: IntArray) {
        super.onRequestPermissionsResult(code, perms, results)
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) startNova()
    }
}
