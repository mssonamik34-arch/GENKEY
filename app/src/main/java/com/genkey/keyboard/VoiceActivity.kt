package com.genkey.keyboard

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast

object VoiceState {
    var recognizer: SpeechRecognizer? = null
    var listening = false
}

private fun GenKeyService.say(s: String) {
    Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
}

fun GenKeyService.stopVoice() {
    val r = VoiceState.recognizer
    VoiceState.recognizer = null
    VoiceState.listening = false
    if (r != null) {
        try {
            r.cancel()
            r.destroy()
        } catch (e: Exception) {
        }
    }
}

fun GenKeyService.startVoice() {
    if (VoiceState.listening) {
        try {
            VoiceState.recognizer?.stopListening()
        } catch (e: Exception) {
        }
        return
    }
    if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
        val p = Intent(this, MicPermissionActivity::class.java)
        p.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(p)
        return
    }
    if (!SpeechRecognizer.isRecognitionAvailable(this)) {
        say("ভয়েস টাইপিং এর জন্য Google অ্যাপ লাগবে")
        return
    }
    flush()
    stopVoice()
    val r = SpeechRecognizer.createSpeechRecognizer(this)
    VoiceState.recognizer = r
    r.setRecognitionListener(object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            VoiceState.listening = true
            say("শুনছি... বলো")
        }

        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() {}

        override fun onError(error: Int) {
            val msg = when (error) {
                SpeechRecognizer.ERROR_NO_MATCH, SpeechRecognizer.ERROR_SPEECH_TIMEOUT ->
                    "কিছু শুনতে পাইনি, আবার বলো"
                SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT ->
                    "ইন্টারনেট সংযোগ দেখো"
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS ->
                    "মাইক্রোফোন অনুমতি দাও"
                else -> "ভয়েস টাইপিং হয়নি ($error)"
            }
            currentInputConnection?.finishComposingText()
            stopVoice()
            say(msg)
        }

        override fun onResults(results: Bundle?) {
            val list = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val t = if (list.isNullOrEmpty()) "" else list[0]
            val ic = currentInputConnection
            if (t.isNotEmpty() && ic != null) {
                ic.commitText("$t ", 1)
            } else {
                ic?.finishComposingText()
            }
            stopVoice()
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val list = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!list.isNullOrEmpty() && list[0].isNotEmpty()) {
                currentInputConnection?.setComposingText(list[0], 1)
            }
        }

        override fun onEvent(eventType: Int, params: Bundle?) {}
    })
    val i = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
    i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
    i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, if (lang == 0) "en-US" else "bn-BD")
    i.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
    i.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, packageName)
    r.startListening(i)
}
