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
    b.addView(key("🎤", YELLOW, 1f) { voice() })
    b.addView(key("।", WHITE, 0.8f) { commit("।") })
    b.addView(spaceKey(2.4f))
    b.addView(backspaceKey())
    b.addView(key("⏎", RED, 1.3f) { enter() })
    root.addView(b)
}

fun GenKeyService.buildSymbols() {
    val rows = listOf("1234567890", "@#$%&-+()/")
    for ((ri, text) in rows.withIndex()) {
        val r = row()
        text.forEachIndexed { i, ch ->
            val s = ch.toString()
            r.addView(key(s, palette[(i + ri) % 5], 1f) { commit(s) })
        }
        root.addView(r)
    }
    val r3 = row()
    val syms = listOf("*", "\"", "'", ":", ";", "!", "?")
    syms.forEachIndexed { i, s ->
        r3.addView(key(s, palette[(i + 2) % 5], 1f) { commit(s) })
    }
    r3.addView(backspaceKey())
    root.addView(r3)
    root.addView(bottomRow("ABC", 0))
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
        render()
    })
    b.addView(spaceKey(4f))
    b.addView(backspaceKey())
    root.addView(b)
}
