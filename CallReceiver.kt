package uz.uzbekcallerid

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.provider.ContactsContract
import android.telephony.TelephonyManager
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale

class CallReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != TelephonyManager.ACTION_PHONE_STATE_CHANGED) return
        val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
        val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        if (state == TelephonyManager.EXTRA_STATE_IDLE) {
            prefs.edit().remove("last_number").apply()
            return
        }
        if (state != TelephonyManager.EXTRA_STATE_RINGING || !prefs.getBoolean("enabled", true)) return
        val number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER) ?: return
        if (prefs.getString("last_number", null) == number) return
        prefs.edit().putString("last_number", number).apply()
        val name = lookupName(context, number)
        val phrase = if (name != null) "$name sizga telefon qilyapti" else "Telefon raqami ${numberToUzbek(number)} dan qo‘ng‘iroq bo‘lyapti"
        val pending = goAsync()
        val ttsRef = arrayOfNulls<TextToSpeech>(1)
        ttsRef[0] = TextToSpeech(context.applicationContext) { result ->
            val tts = ttsRef[0]
            if (result == TextToSpeech.SUCCESS && tts != null) {
                val language = tts.setLanguage(Locale("uz", "UZ"))
                if (language != TextToSpeech.LANG_MISSING_DATA && language != TextToSpeech.LANG_NOT_SUPPORTED) {
                    val female = tts.voices?.firstOrNull {
                        it.locale.language == "uz" && (it.name.contains("female", true) || it.name.contains("woman", true))
                    }
                    if (female != null) tts.voice = female
                    tts.setSpeechRate(0.92f)
                    tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) {}
                        override fun onDone(utteranceId: String?) { tts.shutdown(); pending.finish() }
                        @Deprecated("Deprecated in Java")
                        override fun onError(utteranceId: String?) { tts.shutdown(); pending.finish() }
                    })
                    val queued = tts.speak(phrase, TextToSpeech.QUEUE_FLUSH, null, "caller_announcement")
                    if (queued == TextToSpeech.ERROR) { tts.shutdown(); pending.finish() }
                } else {
                    tts.shutdown(); pending.finish()
                }
            } else {
                tts?.shutdown(); pending.finish()
            }
        }
    }

    private fun lookupName(context: Context, number: String): String? {
        if (context.checkSelfPermission(android.Manifest.permission.READ_CONTACTS) != android.content.pm.PackageManager.PERMISSION_GRANTED) return null
        val uri = ContactsContract.PhoneLookup.CONTENT_FILTER_URI.buildUpon().appendPath(number).build()
        var cursor: Cursor? = null
        return try {
            cursor = context.contentResolver.query(uri, arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME), null, null, null)
            if (cursor != null && cursor.moveToFirst()) cursor.getString(0) else null
        } catch (_: Exception) { null } finally { cursor?.close() }
    }

    companion object {
        private val digits = listOf("nol", "bir", "ikki", "uch", "to‘rt", "besh", "olti", "yetti", "sakkiz", "to‘qqiz")
        fun numberToUzbek(value: String): String = value.filter { it.isDigit() }.mapNotNull { it.digitToIntOrNull()?.let { d -> digits[d] } }.joinToString(", ")
    }
}
