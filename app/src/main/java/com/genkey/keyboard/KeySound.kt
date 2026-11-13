
package com.genkey.keyboard

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool

object KeySound {
    private val sounds = listOf(
        "standard" to R.raw.keypress_standard,
        "spacebar" to R.raw.keypress_spacebar,
        "delete" to R.raw.keypress_delete,
        "return" to R.raw.keypress_return,
        "faah" to R.raw.faah
    )

    private var pool: SoundPool? = null
    private val ids = HashMap<String, Int>()
    private val ready = HashSet<Int>()

    fun init(context: Context) {
        if (pool != null) return

        val p = SoundPool.Builder()
            .setMaxStreams(8)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .build()

        p.setOnLoadCompleteListener { _, id, status ->
            if (status == 0) ready.add(id)
        }

        pool = p

        sounds.forEach { (name, res) ->
            ids[name] = p.load(context, res, 1)
        }
    }

    fun play(context: Context, type: String = "standard") {
        val enabled = context.getSharedPreferences(
            "genkey", Context.MODE_PRIVATE
        ).getBoolean("sound", true)

        if (!enabled) return

        val id = ids[type] ?: ids["standard"] ?: return
        if (id in ready) pool?.play(id, 1f, 1f, 1, 0, 1f)
    }

    fun release() {
        pool?.release()
        pool = null
        ids.clear()
        ready.clear()
    }
}
