package uz.uzbekcallerid

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import java.util.Locale

class MainActivity : Activity() {
    private lateinit var status: TextView
    private var tts: TextToSpeech? = null
    private var uzbekVoiceAvailable = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(28, 36, 28, 36)
        }
        val title = TextView(this).apply {
            text = "O‘zbekcha Caller ID"
            textSize = 27f
            gravity = Gravity.CENTER
        }
        status = TextView(this).apply {
            textSize = 16f
            gravity = Gravity.CENTER
            setPadding(0, 28, 0, 28)
        }
        val enabled = Switch(this).apply {
            text = "Qo‘ng‘iroqni ovoz chiqarib aytish"
            textSize = 18f
            isChecked = prefs.getBoolean("enabled", true)
            setOnCheckedChangeListener { _, checked -> prefs.edit().putBoolean("enabled", checked).apply(); updateStatus() }
        }
        val test = Button(this).apply {
            text = "Ovozni sinab ko‘rish"
            setOnClickListener {
                val engine = tts
                if (engine != null && uzbekVoiceAvailable) engine.speak("Bu o‘zbekcha ovoz sinovi", TextToSpeech.QUEUE_FLUSH, null, "test")
                else status.text = "Telefoningizda o‘zbekcha TTS ovozi topilmadi. Android sozlamalaridan o‘zbekcha matnni ovozga aylantirish ovozini o‘rnating."
            }
        }
        val permissions = Button(this).apply {
            text = "Ruxsatlarni berish"
            setOnClickListener { requestNeededPermissions() }
        }
        root.addView(title)
        root.addView(status)
        root.addView(enabled)
        root.addView(test)
        root.addView(permissions)
        setContentView(root)
        requestNeededPermissions()
        tts = TextToSpeech(this) { result ->
            if (result == TextToSpeech.SUCCESS) {
                val engine = tts ?: return@TextToSpeech
                val lang = engine.setLanguage(Locale("uz", "UZ"))
                uzbekVoiceAvailable = lang != TextToSpeech.LANG_MISSING_DATA && lang != TextToSpeech.LANG_NOT_SUPPORTED
                if (uzbekVoiceAvailable) {
                    val female = engine.voices?.firstOrNull {
                        it.locale.language == "uz" && (it.name.contains("female", true) || it.name.contains("woman", true))
                    }
                    if (female != null) engine.voice = female
                    engine.setSpeechRate(0.92f)
                }
                updateStatus()
            }
        }
    }

    private fun updateStatus() {
        val enabled = getSharedPreferences("settings", MODE_PRIVATE).getBoolean("enabled", true)
        status.text = when {
            !hasPermissions() -> "Telefon va kontaktlar ruxsati kerak."
            !uzbekVoiceAvailable -> "O‘zbekcha ovoz topilmadi. Hozircha boshqa aksentda gapirmaydi."
            !enabled -> "Ovozli e’lon o‘chirilgan."
            else -> "Tayyor. O‘zbekcha ovozli e’lon yoqilgan."
        }
    }

    private fun hasPermissions() = checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED &&
        checkSelfPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED

    private fun requestNeededPermissions() {
        val missing = arrayOf(Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CONTACTS)
            .filter { checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED }
        if (missing.isNotEmpty()) requestPermissions(missing.toTypedArray(), 10) else updateStatus()
    }

    override fun onDestroy() {
        tts?.shutdown()
        super.onDestroy()
    }
}
