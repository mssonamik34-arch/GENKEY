
package com.genkey.keyboard

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView

class MainActivity : Activity() {
    private lateinit var sp: SharedPreferences
    private lateinit var box: LinearLayout
    private lateinit var scEdit: EditText

    private val WRAP = ViewGroup.LayoutParams.WRAP_CONTENT
    private val MATCH = ViewGroup.LayoutParams.MATCH_PARENT

    private fun dp(v: Int) =
        (v * resources.displayMetrics.density).toInt()

    private fun title(s: String) {
        val t = TextView(this)
        t.text = s
        t.textSize = 16f
        t.setTextColor(0xFFE3B33A.toInt())
        t.setPadding(0, dp(22), 0, dp(6))
        box.addView(t)
    }

    private fun switchRow(s: String, key: String, def: Boolean) {
        val sw = Switch(this)
        sw.text = s
        sw.textSize = 15f
        sw.setTextColor(Color.WHITE)
        sw.isChecked = sp.getBoolean(key, def)
        sw.setPadding(0, dp(8), 0, dp(8))
        sw.setOnCheckedChangeListener { _, c ->
            sp.edit().putBoolean(key, c).apply()
        }
        box.addView(sw)
    }

    private fun choice(names: List<String>, key: String, def: Int) {
        val btns = mutableListOf<Button>()

        fun paint() {
            val cur = sp.getInt(key, def)
            for (i in btns.indices) {
                btns[i].alpha = if (i == cur) 1f else 0.4f
            }
        }

        names.chunked(3).forEachIndexed { ci, chunk ->
            val row = LinearLayout(this)
            row.orientation = LinearLayout.HORIZONTAL

            chunk.forEachIndexed { j, n ->
                val idx = ci * 3 + j
                val b = Button(this)
                b.text = n
                b.isAllCaps = false
                b.layoutParams =
                    LinearLayout.LayoutParams(0, WRAP, 1f)

                b.setOnClickListener {
                    sp.edit().putInt(key, idx).apply()
                    paint()
                }

                btns.add(b)
                row.addView(b)
            }

            box.addView(row)
        }

        paint()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sp = getSharedPreferences("genkey", MODE_PRIVATE)

        val scroll = ScrollView(this)
        scroll.setBackgroundColor(Color.BLACK)

        box = LinearLayout(this)
        box.orientation = LinearLayout.VERTICAL
        box.setPadding(dp(20), dp(24), dp(20), dp(40))
        scroll.addView(box)

        val head = TextView(this)
        head.text = "GENKEY"
        head.textSize = 32f
        head.setTextColor(Color.WHITE)
        box.addView(head)

        val b1 = Button(this)
        b1.text = "1. কিবোর্ড চালু করো"
        b1.setOnClickListener {
            startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
        }
        box.addView(b1)

        val b2 = Button(this)
        b2.text = "2. GENKEY বেছে নাও"
        b2.setOnClickListener {
            (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager)
                .showInputMethodPicker()
        }
        box.addView(b2)

        title("থিম")
        choice(Themes.list.map { it.name }, "theme", 0)

        title("কিবোর্ডের উচ্চতা")

        val lab = TextView(this)
        lab.setTextColor(Color.WHITE)
        lab.text = sp.getInt("height", 100).toString() + "%"
        box.addView(lab)

        val sb = SeekBar(this)
        sb.max = 60
        sb.progress = sp.getInt("height", 100) - 70

        sb.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    s: SeekBar?,
                    p: Int,
                    u: Boolean
                ) {
                    val h = p + 70
                    lab.text = "$h%"
                    sp.edit().putInt("height", h).apply()
                }

                override fun onStartTrackingTouch(s: SeekBar?) {}
                override fun onStopTrackingTouch(s: SeekBar?) {}
            }
        )
        box.addView(sb)

        title("এক হাতে টাইপ")
        choice(
            listOf("বন্ধ", "বাঁ দিকে", "ডান দিকে"),
            "onehand",
            0
        )

        title("ভাষা (কোনগুলো থাকবে)")
        switchRow("English", "lang_en", true)
        switchRow("বাংলা", "lang_bn", true)
        switchRow("Avro (Banglish)", "lang_avro", true)

        title("টাইপিং")
        switchRow("শব্দ সাজেশন", "suggest", true)
        switchRow("অটো-কারেক্ট (শুধু English)", "autocorrect", false)
        switchRow("উপরে নম্বর রো", "numrow", false)
        switchRow("কী চাপলে অক্ষর ভেসে ওঠা", "popup", true)

        title("সাউন্ড ও ভাইব্রেশন")
        switchRow("কী সাউন্ড", "sound", true)

        title("কী সাউন্ড বেছে নাও")
        choice(
            listOf("Keyboard", "Faah"),
            "sound_type",
            0
        )

        switchRow("ভাইব্রেশন", "vib", true)

        title("শর্টকাট (প্রতি লাইনে শর্ট=পুরো লেখা)")
        scEdit = EditText(this)
        scEdit.setTextColor(Color.WHITE)
        scEdit.setHintTextColor(0xFF777777.toInt())
        scEdit.hint = "brb=be right back\naddr=আমার ঠিকানা"
        scEdit.setText(sp.getString("shortcuts", ""))
        scEdit.minLines = 3
        scEdit.inputType =
            InputType.TYPE_CLASS_TEXT or
            InputType.TYPE_TEXT_FLAG_MULTI_LINE
        box.addView(scEdit)

        title("অন্যান্য")

        val clear = Button(this)
        clear.text = "শেখা শব্দ মুছে ফেলো"
        clear.setOnClickListener {
            Dict.clearLearned(sp)
        }
        box.addView(clear)

        setContentView(scroll)
    }

    override fun onPause() {
        super.onPause()
        sp.edit()
            .putString("shortcuts", scEdit.text.toString())
            .apply()
    }
}
