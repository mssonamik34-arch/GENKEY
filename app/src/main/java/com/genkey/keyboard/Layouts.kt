package com.genkey.keyboard

private val bnPage0 = listOf(
    listOf("ক", "খ", "গ", "ঘ", "ঙ", "চ", "ছ", "জ", "ঝ", "ঞ"),
    listOf("ট", "ঠ", "ড", "ঢ", "ণ", "ত", "থ", "দ", "ধ", "ন"),
    listOf("প", "ফ", "ব", "ভ", "ম", "য", "র", "ল", "শ", "ষ"),
    listOf("স", "হ", "\u09DC", "\u09DD", "\u09DF", "ৎ", "ং", "ঃ", "ঁ", "\u09CD")
)

private val bnPage1 = listOf(
    listOf("অ", "আ", "ই", "ঈ", "উ", "ঊ", "ঋ", "এ", "ঐ", "ও"),
    listOf("\u09BE", "\u09BF", "\u09C0", "\u09C1", "\u09C2", "\u09C3", "\u09C7", "\u09C8", "\u09CB", "\u09CC"),
    listOf("০", "১", "২", "৩", "৪", "৫", "৬", "৭", "৮", "৯"),
    listOf("ঔ", "৳", "।", "!", "?", "-", "(", ")", "/", "@")
)

fun GenKeyService.buildLetters() {
    if (numRow) {
        val rn = row(40)
        "1234567890".forEachIndexed { i, ch ->
            val s = ch.toString()
            rn.addView(key(s, palette[(i + 2) % 5], 1f, size = 16f) { commit(s) })
        }
        root.addView(rn)
    }
    val r1 = row()
    val c1 = listOf(YELLOW, BLUE, DARK, WHITE, RED, BLUE, WHITE, RED, YELLOW, DARK)
    "qwertyuiop".forEachIndexed { i, ch ->
        val s = ch.toString()
        r1.addView(key(if (shift) s.uppercase() else s, c1[i], 1f, "${(i + 1) % 10}") { typeLetter(s) })
    }
    root.addView(r1)

    val r2 = row()
    r2.setPadding(dp(16), 0, dp(16), 0)
    val c2 = listOf(WHITE, RED, BLUE, DARK, YELLOW, WHITE, RED, BLUE, DARK)
    val h2 = "@#&*-!?()"
    "asdfghjkl".forEachIndexed { i, ch ->
        val s = ch.toString()
        r2.addView(key(if (shift) s.uppercase() else s, c2[i], 1f, h2[i].toString()) { typeLetter(s) })
    }
    root.addView(r2)

    val r3 = row()
    r3.addView(key(if (shift) "⇪" else "⇧", YELLOW, 1.5f) {
        shift = !shift
        render()
    })
    val c3 = listOf(DARK, YELLOW, WHITE, RED, BLUE, DARK, YELLOW)
    val h3 = listOf("\"", ";", "ç", null, "'", ":", "/")
    "zxcvbnm".forEachIndexed { i, ch ->
        val s = ch.toString()
        r3.addView(key(if (shift) s.uppercase() else s, c3[i], 1f, h3[i]) { typeLetter(s) })
    }
    r3.addView(backspaceKey())
    root.addView(r3)
    root.addView(bottomRow("?123", 1))
}

fun GenKeyService.buildBangla() {
    val page = if (bnPage == 0) bnPage0 else bnPage1
    for ((ri, line) in page.withIndex()) {
        val r = row(48)
        line.forEachIndexed { i, s ->
            val combining = s.length == 1 && (s[0] in '\u0981'..'\u0983' || s[0] in '\u09BE'..'\u09CD')
            val shown = if (combining) "\u25CC" + s else s
            r.addView(key(shown, palette[(i + ri) % 5], 1f) { commit(s) })
        }
        root.addView(r)
    }
    val b = row(50)
    b.addView(key(if (bnPage == 0) "অ আ" else "ক খ", RED, 1.3f, size = 14f) {
        bnPage = 1 - bnPage
        render()
    })
    b.addView(key("😊", BLUE, 1f) {
        layer = 2
        render()
    })
    b.addView(key("🌐", YELLOW, 1f) { cycleLanguage() })
    b.addView(key("।", WHITE, 0.8f) { commit("।") })
    b.addView(spaceKey(2.4f))
    b.addView(backspaceKey())
    b.addView(key("⏎", RED, 1.3f) { enter() })
    root.addView(b)
}

fun GenKeyService.buildSymbols() {
    val rows = listOf(
        "1234567890",
        "@#$%&*+-=",
        "()[]{}<>_",
        listOf('/', '\\', ':', ';', '\'', '"', '?', '!', '~').joinToString("")
    )
    for ((ri, chars) in rows.withIndex()) {
        val r = row(52)
        chars.forEachIndexed { i, ch ->
            val s = ch.toString()
            r.addView(key(s, palette[(i + ri) % 5], 1f, size = 19f) { commit(s) })
        }
        root.addView(r)
    }
    val bottom = row(48)
    bottom.addView(key("ABC", RED, 1.2f, size = 14f) { layer = 0; render() })
    bottom.addView(key("😊", BLUE, 0.9f) { layer = 2; render() })
    bottom.addView(key("🌐", YELLOW, 0.9f) { cycleLanguage() })
    bottom.addView(key(",", WHITE, 0.65f) { commit(",") })
    bottom.addView(spaceKey(2.1f))
    bottom.addView(key(".", DARK, 0.65f) { commit(".") })
    bottom.addView(backspaceKey())
    bottom.addView(key("⏎", RED, 1.1f) { enter() })
    root.addView(bottom)
}

fun GenKeyService.buildClipboard() {
    if (clips.isEmpty()) {
        val r = row(50)
        r.addView(label("ক্লিপবোর্ড খালি। কিছু কপি করলে এখানে জমবে।", 14f, textOnBg, 1f))
        root.addView(r)
    }
    for ((i, c) in clips.take(5).withIndex()) {
        val r = row(46)
        r.addView(key(c.replace("\n", " ").take(32), palette[i % 5], 1f, size = 14f) { commit(c) })
        root.addView(r)
    }
    val b = row()
    b.addView(key("ABC", RED, 1.5f, size = 14f) {
        layer = 0
        render()
    })
    b.addView(key("🗑", DARK, 1.5f) {
        clips.clear()
        sp.edit().putString("clipboard_history", "[]").apply()
        render()
    })
    b.addView(spaceKey(4f))
    b.addView(backspaceKey())
    root.addView(b)
}


fun GenKeyService.buildModernEnglishLayout() {
    val c = listOf(0xFF252E59.toInt(), 0xFF202A50.toInt(), 0xFF1B2448.toInt(),
        0xFF27315D.toInt(), 0xFF20294D.toInt())
    val r1 = row(58)
    "qwertyuiop".forEachIndexed { i, ch ->
        val v = ch.toString()
        r1.addView(key(if (shift) v.uppercase() else v, c[i % c.size], 1f, "0123456789"[i].toString(), 22f) { typeLetter(v) })
    }
    root.addView(r1)

    val r2 = row(58)
    r2.setPadding(dp(12), 0, dp(12), 0)
    val hints2 = listOf("@", "#", "_", "&", "-", "+", "(", ")", "")
    "asdfghjkl".forEachIndexed { i, ch ->
        val v = ch.toString()
        r2.addView(key(if (shift) v.uppercase() else v, c[(i + 1) % c.size], 1f, hints2[i], 22f) { typeLetter(v) })
    }
    root.addView(r2)

    val r3 = row(58)
    r3.addView(key(if (shift) "⇪" else "⇧", 0xFF4030A0.toInt(), 1.35f, size = 23f) {
        shift = !shift
        render()
    })
    val hints3 = listOf("*", "\"", "'", ":", ";", "!", "?")
    "zxcvbnm".forEachIndexed { i, ch ->
        val v = ch.toString()
        r3.addView(key(if (shift) v.uppercase() else v, c[(i + 2) % c.size], 1f, hints3[i], 22f) { typeLetter(v) })
    }
    r3.addView(backspaceKey())
    root.addView(r3)

    val bottom = row(60)
    bottom.addView(key("?123", 0xFF4030A0.toInt(), 1.25f, size = 15f) { layer = 1; render() })
    bottom.addView(key("🌐", c[1], 0.9f, size = 21f) { cycleLanguage() })
    bottom.addView(key(",", c[2], 0.8f, "!", 19f) { commit(",") })
    bottom.addView(modernSpaceKey(3.3f))
    bottom.addView(key(".", c[2], 0.8f, "…", 19f) { commit(".") })
    bottom.addView(key(modernEnterLabel(), 0xFF00B96B.toInt(), 1.45f, size = 20f) { enter() })
    root.addView(bottom)
}

fun GenKeyService.modernEnterLabel(): String {
    val info = currentInputEditorInfo
    val action = info?.imeOptions?.and(android.view.inputmethod.EditorInfo.IME_MASK_ACTION)
    return when (action) {
        android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH -> "⌕"
        android.view.inputmethod.EditorInfo.IME_ACTION_SEND -> "➤"
        android.view.inputmethod.EditorInfo.IME_ACTION_GO -> "Go"
        android.view.inputmethod.EditorInfo.IME_ACTION_NEXT -> "→"
        android.view.inputmethod.EditorInfo.IME_ACTION_DONE -> "✓"
        else -> "↵"
    }
}

fun GenKeyService.modernSpaceKey(weight: Float): android.widget.FrameLayout {
    val title = when (lang) { 1 -> "বাংলা"; 2 -> "Avro"; else -> "English" }
    val bar = key("◂  $title  ▸", 0xFF20294D.toInt(), weight, size = 15f) { space() }
    var startX = 0f
    bar.setOnTouchListener { _, event ->
        when (event.action) {
            android.view.MotionEvent.ACTION_DOWN -> { startX = event.x; false }
            android.view.MotionEvent.ACTION_UP -> {
                if (kotlin.math.abs(event.x - startX) > dp(35)) { cycleLanguage(); true } else false
            }
            else -> false
        }
    }
    return bar
}

fun GenKeyService.modernHideRow(): LinearLayout {
    val r = row(34)
    val hide = TextView(this)
    hide.text = "⌄"
    hide.textSize = 25f
    hide.gravity = Gravity.CENTER
    hide.setTextColor(0xFFE6E9FF.toInt())
    val shape = android.graphics.drawable.GradientDrawable(
        android.graphics.drawable.GradientDrawable.Orientation.LEFT_RIGHT,
        intArrayOf(0xFF17123B.toInt(), 0xFF101C39.toInt()))
    shape.cornerRadius = dp(20).toFloat()
    hide.background = shape
    r.addView(hide, LinearLayout.LayoutParams(MATCH, MATCH))
    hide.setOnClickListener { requestHideSelf(0) }
    return r
}
