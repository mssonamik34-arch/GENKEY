package com.genkey.keyboard

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat

enum class VoiceState { IDLE, LISTENING }

object VoiceHolder {
    var state: VoiceState = VoiceState.IDLE
    var receiver: BroadcastReceiver? = null
}

fun GenKeyService.startVoice() {
    if (VoiceHolder.state == VoiceState.LISTENING) return
    VoiceHolder.state = VoiceState.LISTENING

    val r = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val text = intent?.getStringExtra(VoiceActivity.EXTRA_TEXT)
            if (!text.isNullOrBlank()) {
                currentInputConnection?.commitText(text, 1)
            }
            stopVoice()
        }
    }
    VoiceHolder.receiver = r
    ContextCompat.registerReceiver(
        this, r,
        IntentFilter(VoiceActivity.ACTION_VOICE_RESULT),
        ContextCompat.RECEIVER_NOT_EXPORTED
    )

    startActivity(
        Intent(this, VoiceActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    )
}

fun GenKeyService.stopVoice() {
    VoiceHolder.receiver?.let {
        try { unregisterReceiver(it) } catch (_: Exception) {}
    }
    VoiceHolder.receiver = null
    VoiceHolder.state = VoiceState.IDLE
}
