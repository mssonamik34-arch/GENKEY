package com.genkey.keyboard

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.core.content.ContextCompat

enum class VoiceState { IDLE, LISTENING }

object VoiceHolder {
    var state: VoiceState = VoiceState.IDLE
    var recognizer: SpeechRecognizer? = null
}

fun GenKeyService.startVoice() {
    if (VoiceHolder.state == VoiceState.LISTENING) {
        stopVoice()
        return
    }
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
        startActivity(Intent(this, MicPermissionActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        return
    }
    if (!SpeechRecognizer.isRecognitionAvailable(this)) {
        Toast.makeText(this, "এই ফোনে Voice Typing সাপোর্ট নেই", Toast.LENGTH_SHORT).show()
        return
    }
    flush()
    VoiceHolder.state = VoiceState.LISTENING
    try { render() } catch (_: Exception) {}
    try {
        val recognizer = SpeechRecognizer.createSpeechRecognizer(this)
        VoiceHolder.recognizer = recognizer
        recognizer.setRecognitionListener(object : RecognitionListener {
            override fun onResults(results: Bundle?) {
                val spoken = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
                if (!spoken.isNullOrBlank()) currentInputConnection?.commitText(spoken, 1)
                stopVoice()
            }
            override fun onError(error: Int) { stopVoice() }
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
        val language = if (lang == 0) "en-US" else "bn-BD"
        val request = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, language)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, language)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
        recognizer.startListening(request)
    } catch (_: Exception) {
        stopVoice()
        Toast.makeText(this, "Voice Typing চালু করা যায়নি", Toast.LENGTH_SHORT).show()
    }
}

fun GenKeyService.stopVoice() {
    val active = VoiceHolder.recognizer
    VoiceHolder.recognizer = null
    VoiceHolder.state = VoiceState.IDLE
    try { active?.cancel() } catch (_: Exception) {}
    try { active?.destroy() } catch (_: Exception) {}
    try { render() } catch (_: Exception) {}
}