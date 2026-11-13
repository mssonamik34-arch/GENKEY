package com.genkey.keyboard

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView

fun GenKeyService.row(h: Int = 54): LinearLayout {
    val l = LinearLayout(this)
    l.orientation = LinearLayout.HORIZONTAL
    l.gravity = Gravity.CENTER
    l.layoutParams = LinearLayout.LayoutParams(MATCH, dp((h * scale).toInt()))
    return l
}

fun GenKeyService.label(s: String, size: Float, color: Int, weight: Float): TextView {
    val t = TextView(this)
    t.text = s
    t.textSize = size
    t.setTextColor(color)
    t.gravity = Gravity.CENTER
    t.layoutParams = LinearLayout.LayoutParams(0, MATCH, weight)
    return t
}

fun GenKeyService.toolbar(i: Int) {
    val ic = currentInputConnection
    when (i) {
        0 -> startActivity(Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        1 -> ic?.performContextMenuAction(android.R.id.undo)
        2 -> ic?.performContextMenuAction(android.R.id.redo)
        3 -> ic?.performContextMenuAction(android.R.id.selectAll)
        4 -> ic?.performContextMenuAction(android.R.id.copy)
        5 -> ic?.performContextMenuAction(android.R.id.cut)
        6 -> { flush(); ic?.performContextMenuAction(android.R.id.paste) }
        7 -> { layer = 3; render() }
        8 -> { oneHand = (oneHand + 1) % 3; sp.edit().putInt("onehand", oneHand).apply(); render() }
    }
}

fun GenKeyService.topBar(): LinearLayout {
    val r = row(40)
    val items = listOf("⚙", "↶", "↷", "▦", "⧉", "✂", "📋", "🕘", "⇄")
    items.forEachIndexed { i, s ->
        val t = label(s, 17f, textOnBg, 1f)
        t.setOnClickListener { feedback(it); toolbar(i) }
        r.addView(t)
    }
    return r
}

fun GenKeyService.cycleLanguage() {
    flush()
    val enabled = enabledLangs()
    lang = enabled[(enabled.indexOf(lang) + 1) % enabled.size]
    bnPage = 0
    layer = 0
    render()
}

fun GenKeyService.langName(): String = when (lang) { 0 -> "EN"; 1 -> "বাংলা"; else -> "Avro" }

fun GenKeyService.strip(): LinearLayout {
    val r = row(44)
    r.addView(label(langName(), 14f, textOnBg, 0.9f))
    for (i in 0..2) {
        val t = label("", 15f, textOnBg, 1.4f)
        t.maxLines = 1
        t.setOnClickListener { pickSuggestion(t.text.toString()) }
        sug[i] = t
        r.addView(t)
    }
    val g = label("🌐", 19f, textOnBg, 0.9f)
    g.setOnClickListener { feedback(it); cycleLanguage() }
    g.setOnLongClickListener {
        (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).showInputMethodPicker()
        true
    }
    r.addView(g)
    return r
}

fun GenKeyService.key(
    label: String,
    bg: Int,
    weight: Float,
    hint: String? = null,
    size: Float = 20f,
    onTap: () -> Unit
): FrameLayout {
    val f = FrameLayout(this)
    val shape = GradientDrawable()
    val red = ((Color.red(bg) * 1.12f).toInt()).coerceAtMost(255)
    val green = ((Color.green(bg) * 1.12f).toInt()).coerceAtMost(255)
    val blue = ((Color.blue(bg) * 1.12f).toInt()).coerceAtMost(255)
    val topColor = Color.rgb(red, green, blue)
    shape.cornerRadius = dp(11).toFloat()
    shape.orientation = GradientDrawable.Orientation.TOP_BOTTOM
    shape.colors = intArrayOf(topColor, bg)
    shape.setStroke(dp(1), 0x45FFFFFF)
    f.background = shape
    f.elevation = dp(3).toFloat()
    val lp = LinearLayout.LayoutParams(0, MATCH, weight)
    lp.setMargins(dp(2), dp(3), dp(2), dp(4))
    f.layoutParams = lp

    val fg = Themes.textOn(bg)
    val main = TextView(this)
    main.text = label
    main.setTextColor(fg)
    main.textSize = size
    main.typeface = if (label.all { it.code < 128 }) Typeface.create(keyFont, Typeface.BOLD) else Typeface.DEFAULT
    main.gravity = Gravity.CENTER
    f.addView(main, FrameLayout.LayoutParams(MATCH, MATCH))

    if (hint != null) {
        val h = TextView(this)
        h.text = hint
        h.textSize = 10f
        h.setTextColor(if (fg == Color.BLACK) 0xFF555555.toInt() else 0xFFBBBBBB.toInt())
        val hp = FrameLayout.LayoutParams(WRAP, WRAP, Gravity.TOP or Gravity.END)
        hp.setMargins(0, dp(3), dp(6), 0)
        f.addView(h, hp)
        f.setOnLongClickListener { commit(hint); true }
    }
    if (label.length == 1 && label[0].isLetterOrDigit() && sp.getBoolean("popup", true)) {
        f.setOnTouchListener { v, e ->
            when (e.action) {
                MotionEvent.ACTION_DOWN -> showPreview(v, label)
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> hidePreview()
            }
            false
        }
    }
    f.setOnClickListener {
        val soundType = when (label) { "GENKEY" -> "spacebar"; "⏎" -> "return"; else -> "standard" }
        feedback(f, soundType)
        onTap()
    }
    return f
}

fun GenKeyService.backspaceKey(): FrameLayout {
    val b = key("⌫", WHITE, 1.5f) { }
    b.setOnTouchListener { _, e ->
        when (e.action) {
            MotionEvent.ACTION_DOWN -> { feedback(b, "delete"); backspace(); repeating = true; handler.postDelayed(repeatTask, 400) }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> { repeating = false; handler.removeCallbacks(repeatTask) }
        }
        true
    }
    return b
}

fun GenKeyService.spaceKey(weight: Float): FrameLayout = key("GENKEY", DARK, weight, size = 12f) { space() }

fun GenKeyService.bottomRow(first: String, goTo: Int): LinearLayout {
    val r = row()
    r.addView(key(first, RED, 1.3f, size = 14f) { layer = goTo; render() })
    r.addView(key("😊", BLUE, 1f) { layer = 2; render() })
    r.addView(key("🌐", YELLOW, 1f) { cycleLanguage() })
    r.addView(key(",", WHITE, 0.8f) { commit(",") })
    r.addView(spaceKey(3f))
    r.addView(key(".", DARK, 0.8f) { commit(".") })
    r.addView(key("⏎", RED, 1.3f) { enter() })
    return r
}
