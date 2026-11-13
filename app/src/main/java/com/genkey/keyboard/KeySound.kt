
package com.genkey.keyboard

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool

object KeySound {

    val sounds: List<Pair<String, Int>> = listOf(
        "Keyboard" to R.raw.keyboardsound,
        "Faah" to R.raw.faah
    )

    private var pool: SoundPool? = null
    private val ids = HashMap<Int, Int>()
    private val ready = HashSet<Int>()

    private var enabled = true
    private var selected = 0

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(
            "genkey",
            Context.MODE_PRIVATE
        )

        enabled = prefs.getBoolean("sound", true)
        selected = prefs.getInt("sound_index", 0)
            .coerceIn(0, sounds.size - 1)

        if (pool != null) return

        val p = SoundPool.Builder()
            .setMaxStreams(6)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(
                        AudioAttributes.USAGE_ASSISTANCE_SONIFICATION
                    )
                    .setContentType(
                        AudioAttributes.CONTENT_TYPE_SONIFICATION
                    )
                    .build()
            )
            .build()

        p.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) {
                ready.add(sampleId)
            }
        }

        sounds.forEach { (_, res) ->
            ids[res] = p.load(context, res, 1)
        }

        pool = p
    }

    fun setSelected(context: Context, index: Int) {
        selected = index.coerceIn(0, sounds.size - 1)

        context.getSharedPreferences("genkey", Context.MODE_PRIVATE)
            .edit()
            .putInt("sound_index", selected)
            .apply()

        play()
    }

    fun setEnabled(context: Context, on: Boolean) {
        enabled = on

        context.getSharedPreferences("genkey", Context.MODE_PRIVATE)
            .edit()
            .putBoolean("sound", on)
            .apply()
    }

    fun play() {
        if (!enabled) return

        val res = sounds[selected].second
        val id = ids[res] ?: return

        if (id in ready) {
            pool?.play(id, 1f, 1f, 1, 0, 1f)
        }
    }

    fun release() {
        pool?.release()
        pool = null
        ids.clear()
        ready.clear()
    }
}
