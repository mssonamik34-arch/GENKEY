package com.genkey.keyboard

import android.graphics.Paint
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

private var emojiTab = 1
private val emojiCache = HashMap<Int, List<String>>()

private val emojiTabs: List<Pair<String, List<IntRange>>> = listOf(
    Pair("🕘", emptyList<IntRange>()),
    Pair("😀", listOf(0x1F600..0x1F64F, 0x1F910..0x1F92F)),
    Pair("👍", listOf(0x1F440..0x1F4AF)),
    Pair("🐶", listOf(0x1F400..0x1F43F, 0x1F980..0x1F9AE)),
    Pair("🍔", listOf(0x1F32D..0x1F37F, 0x1F950..0x1F96F)),
    Pair("⚽", listOf(0x1F380..0x1F3D3, 0x1F3F8..0x1F3F9)),
    Pair("🚗", listOf(0x1F680..0x1F6C5, 0x1F3D4..0x1F3F0)),
    Pair("💡", listOf(0x1F4B0..0x1F4FF, 0x1F500..0x1F53D)),
    Pair("❤", listOf(0x2600..0x27BF, 0x1F300..0x1F32C)),
    Pair("🏳", emptyList<IntRange>())
)

private fun GenKeyService.emojiList(tab: Int): List<String> {
    if (tab == 0) {
        return (sp.getString("recent", "") ?: "").split(" ").filter { it.isNotEmpty() }
    }
    val cached = emojiCache[tab]
    if (cached != null) return cached
    val out = ArrayList<String>()
    if (tab == emojiTabs.size - 1) {
        val codes = "BD IN PK US GB SA AE CA AU DE FR IT ES BR AR JP KR CN RU TR EG NG ZA ID MY SG NP LK AF IR IQ QA KW OM TH VN PH MX NL SE NO CH PT GR UA PL PS".split(" ")
        for (c in codes) {
            val sb = StringBuilder()
            sb.appendCodePoint(0x1F1E6 + (c[0] - 'A'))
            sb.appendCodePoint(0x1F1E6 + (c[1] - 'A'))
            out.add(sb.toString())
        }
    } else {
        val p = Paint()
        for (range in emojiTabs[tab].second) {
            for (cp in range) {
                val s = String(Character.toChars(cp))
                if (p.hasGlyph(s)) out.add(s)
            }
        }
    }
    emojiCache[tab] = out
    return out
}

private fun GenKeyService.addRecent(e: String) {
    val l = (sp.getString("recent", "") ?: "").split(" ").filter { it.isNotEmpty() }.toMutableList()
    l.remove(e)
    l.add(0, e)
    sp.edit().putString("recent", l.take(30).joinToString(" ")).apply()
}

fun GenKeyService.buildEmoji() {
    val tabs = row(40)
    emojiTabs.forEachIndexed { i, t ->
        val tv = label(t.first, 18f, textOnBg, 1f)
        tv.alpha = if (i == emojiTab) 1f else 0.45f
        tv.setOnClickListener {
            emojiTab = i
            render()
        }
        tabs.addView(tv)
    }
    root.addView(tabs)

    val sv = ScrollView(this)
    sv.layoutParams = LinearLayout.LayoutParams(MATCH, dp((230 * scale).toInt()))
    val col = LinearLayout(this)
    col.orientation = LinearLayout.VERTICAL
    val list = emojiList(emojiTab)
    if (list.isEmpty()) {
        val m = TextView(this)
        m.text = "এখানে এখনো কিছু নেই"
        m.setTextColor(textOnBg)
        m.setPadding(dp(16), dp(16), dp(16), dp(16))
        col.addView(m)
    }
    for (chunk in list.chunked(8)) {
        val r = LinearLayout(this)
        r.orientation = LinearLayout.HORIZONTAL
        r.layoutParams = LinearLayout.LayoutParams(MATCH, dp(48))
        for (e in chunk) {
            val t = TextView(this)
            t.text = e
            t.textSize = 24f
            t.gravity = Gravity.CENTER
            t.layoutParams = LinearLayout.LayoutParams(0, MATCH, 1f)
            t.setOnClickListener {
                feedback(t)
                commit(e)
                addRecent(e)
            }
            r.addView(t)
        }
        for (k in chunk.size until 8) {
            val pad = View(this)
            pad.layoutParams = LinearLayout.LayoutParams(0, MATCH, 1f)
            r.addView(pad)
        }
        col.addView(r)
    }
    sv.addView(col)
    root.addView(sv)

    val b = row()
    b.addView(key("ABC", RED, 1.5f, size = 14f) {
        layer = 0
        render()
    })
    b.addView(spaceKey(5f))
    b.addView(backspaceKey())
    root.addView(b)
}
