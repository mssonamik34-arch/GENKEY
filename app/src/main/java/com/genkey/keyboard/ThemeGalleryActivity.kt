package com.genkey.keyboard

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class ThemeGalleryActivity : Activity() {
    private lateinit var prefs: android.content.SharedPreferences
    private lateinit var root: LinearLayout
    private val wrap = ViewGroup.LayoutParams.WRAP_CONTENT
    private val match = ViewGroup.LayoutParams.MATCH_PARENT
    private val colorInputs = mutableListOf<EditText>()

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    private fun heading(value: String) {
        val t = TextView(this)
        t.text = value
        t.textSize = 19f
        t.setTextColor(0xFFE3B33A.toInt())
        t.typeface = Typeface.DEFAULT_BOLD
        t.setPadding(0, dp(20), 0, dp(9))
        root.addView(t)
    }

    private fun button(value: String, action: () -> Unit) {
        val b = Button(this)
        b.text = value
        b.isAllCaps = false
        b.setOnClickListener { action() }
        root.addView(b, LinearLayout.LayoutParams(match, wrap))
    }

    private fun preview(theme: KTheme): LinearLayout {
        val panel = LinearLayout(this)
        panel.orientation = LinearLayout.VERTICAL
        panel.setPadding(dp(6), dp(6), dp(6), dp(6))
        panel.setBackgroundColor(theme.bg)
        listOf("QWERTYUIOP", "ASDFGHJKL", "⇧ZXCVBNM⌫").forEachIndexed { rowIndex, letters ->
            val row = LinearLayout(this)
            row.gravity = Gravity.CENTER
            letters.forEachIndexed { index, ch ->
                val key = TextView(this)
                key.text = ch.toString()
                key.gravity = Gravity.CENTER
                key.textSize = 12f
                val color = theme.keys[(index + rowIndex) % theme.keys.size]
                key.setTextColor(Themes.textOn(color))
                val shape = GradientDrawable()
                shape.setColor(color)
                shape.cornerRadius = dp(5).toFloat()
                key.background = shape
                val lp = LinearLayout.LayoutParams(0, dp(30), 1f)
                lp.setMargins(dp(2), dp(2), dp(2), dp(2))
                row.addView(key, lp)
            }
            panel.addView(row)
        }
        return panel
    }

    private fun themeCard(index: Int, theme: KTheme) {
        val card = LinearLayout(this)
        card.orientation = LinearLayout.VERTICAL
        card.setPadding(dp(10), dp(10), dp(10), dp(10))
        val bg = GradientDrawable()
        bg.setColor(0xFF1B1B1B.toInt())
        bg.cornerRadius = dp(12).toFloat()
        bg.setStroke(dp(1), if (prefs.getInt("theme", 0) == index && !prefs.getBoolean("custom_theme", false)) 0xFFE3B33A.toInt() else 0xFF555555.toInt())
        card.background = bg
        val name = TextView(this)
        name.text = theme.name + if (prefs.getInt("theme", 0) == index && !prefs.getBoolean("custom_theme", false)) "  ✓" else ""
        name.textSize = 16f
        name.setTextColor(Color.WHITE)
        name.typeface = Typeface.DEFAULT_BOLD
        card.addView(name)
        val p = preview(theme)
        val lp = LinearLayout.LayoutParams(match, wrap)
        lp.topMargin = dp(8)
        card.addView(p, lp)
        card.setOnClickListener {
            prefs.edit().putInt("theme", index).putBoolean("custom_theme", false).apply()
            recreate()
        }
        val params = LinearLayout.LayoutParams(match, wrap)
        params.bottomMargin = dp(9)
        root.addView(card, params)
    }

    private fun colorField(label: String, value: Int) {
        val t = TextView(this)
        t.text = label
        t.textSize = 14f
        t.setTextColor(Color.WHITE)
        root.addView(t)
        val input = EditText(this)
        input.setSingleLine(true)
        input.setText(String.format("#%06X", 0xFFFFFF and value))
        input.setTextColor(Color.WHITE)
        input.setHintTextColor(0xFF888888.toInt())
        input.hint = "#RRGGBB"
        root.addView(input)
        colorInputs.add(input)
    }

    private fun customColors() {
        colorInputs.clear()
        val base = Themes.get(prefs.getInt("theme", 0))
        colorField("Background color", prefs.getInt("custom_bg", base.bg))
        val labels = listOf("Key color 1", "Key color 2", "Key color 3", "Key color 4", "Key color 5")
        val keys = listOf("custom_key1", "custom_key2", "custom_key3", "custom_key4", "custom_key5")
        labels.forEachIndexed { i, label ->
            colorField(label, prefs.getInt(keys[i], base.keys[i]))
        }
        button("Apply my colors") {
            try {
                val bg = Color.parseColor(colorInputs[0].text.toString().trim())
                val colors = colorInputs.drop(1).map { Color.parseColor(it.text.toString().trim()) }
                val edit = prefs.edit().putInt("custom_bg", bg).putBoolean("custom_theme", true)
                keys.forEachIndexed { i, key -> edit.putInt(key, colors[i]) }
                edit.apply()
                android.widget.Toast.makeText(this, "Custom theme applied!", android.widget.Toast.LENGTH_SHORT).show()
            } catch (_: Exception) {
                android.widget.Toast.makeText(this, "রং লিখো #RRGGBB ফরম্যাটে", android.widget.Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun chooseWallpaper() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        }
        startActivityForResult(intent, 812)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 812 && resultCode == Activity.RESULT_OK) {
            val uri: Uri = data?.data ?: return
            try {
                contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                prefs.edit().putString("wallpaper_uri", uri.toString()).apply()
                android.widget.Toast.makeText(this, "Wallpaper saved! Keyboard reopen করো", android.widget.Toast.LENGTH_SHORT).show()
            } catch (_: Exception) {
                android.widget.Toast.makeText(this, "ছবিটি ব্যবহার করা যায়নি", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = getSharedPreferences("genkey", MODE_PRIVATE)
        val scroll = ScrollView(this)
        scroll.setBackgroundColor(0xFF101010.toInt())
        root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setPadding(dp(16), dp(16), dp(16), dp(32))
        scroll.addView(root)
        val top = TextView(this)
        top.text = "🎨 Theme Gallery"
        top.textSize = 27f
        top.typeface = Typeface.DEFAULT_BOLD
        top.setTextColor(Color.WHITE)
        root.addView(top)
        val hint = TextView(this)
        hint.text = "যে থিম ভালো লাগে সেটিতে চাপ দাও। সঙ্গে সঙ্গে সেভ হবে।"
        hint.setTextColor(0xFFBBBBBB.toInt())
        root.addView(hint)
        heading("Built-in themes")
        Themes.list.forEachIndexed { index, theme -> themeCard(index, theme) }
        heading("Custom colors")
        customColors()
        heading("Custom wallpaper")
        button("🖼 Choose wallpaper") { chooseWallpaper() }
        button("Remove wallpaper") { prefs.edit().remove("wallpaper_uri").apply() }
        button("Reset custom colors") {
            prefs.edit().putBoolean("custom_theme", false).apply()
            recreate()
        }
        setContentView(scroll)
    }
}
