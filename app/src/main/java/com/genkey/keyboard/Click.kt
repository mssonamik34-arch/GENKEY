package com.genkey.keyboard

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

object Click {
    private var pool: SoundPool? = null
    private var id = 0
    private var ready = false

    fun init(ctx: Context) {
        if (pool != null) return
        try {
            val f = File(ctx.cacheDir, "genkey_click.wav")
            f.writeBytes(makeWav())
            val attrs = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            val p = SoundPool.Builder().setMaxStreams(4).setAudioAttributes(attrs).build()
            p.setOnLoadCompleteListener { _, _, status -> ready = (status == 0) }
            id = p.load(f.absolutePath, 1)
            pool = p
        } catch (e: Exception) {
        }
    }

    fun play() {
        if (ready) pool?.play(id, 0.8f, 0.8f, 1, 0, 1f)
    }

    private fun makeWav(): ByteArray {
        val rate = 22050
        val n = rate * 35 / 1000
        val data = ByteBuffer.allocate(n * 2).order(ByteOrder.LITTLE_ENDIAN)
        for (i in 0 until n) {
            val env = exp(-i / 90.0)
            val v = sin(2.0 * PI * 1600.0 * i / rate) * env
            data.putShort((v * 26000).toInt().toShort())
        }
        val len = n * 2
        val bb = ByteBuffer.allocate(44 + len).order(ByteOrder.LITTLE_ENDIAN)
        bb.put("RIFF".toByteArray())
        bb.putInt(36 + len)
        bb.put("WAVE".toByteArray())
        bb.put("fmt ".toByteArray())
        bb.putInt(16)
        bb.putShort(1)
        bb.putShort(1)
        bb.putInt(rate)
        bb.putInt(rate * 2)
        bb.putShort(2)
        bb.putShort(16)
        bb.put("data".toByteArray())
        bb.putInt(len)
        bb.put(data.array())
        return bb.array()
    }
}
