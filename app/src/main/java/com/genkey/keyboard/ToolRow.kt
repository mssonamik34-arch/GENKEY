package com.genkey.keyboard

import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView

fun GenKeyService.toolbarRow(): View {
    val items = listOf(
        Pair("🎤 ভয়েস", 9),
        Pair("📋 পেস্ট", 6),
        Pair("⧉ কপি", 4),
        Pair("✂ কাট", 5),
        Pair("▦ সব", 3),
        Pair("🕘 ক্লিপ", 7),
        Pair("↶ আন্ডু", 1),
        Pair("↷ রিডু", 2),
        Pair("⇄ এক হাত", 8),
        Pair("⚙ সেটিংস", 0)
    )
    val inner = LinearLayout(this)
    inner.orientation = LinearLayout.HORIZONTAL
    items.forEachIndexed { i, item ->
        val bg = palette[i % 5]
        val t = TextView(this)
        t.text = item.first
        t.textSize = 13f
        t.gravity = Gravity.CENTER
        t.setPadding(dp(12), 0, dp(12), 0)
        t.setTextColor(Themes.textOn(bg))
        val shape = GradientDrawable()
        shape.cornerRadius = dp(16).toFloat()
        shape.setColor(bg)
        t.background = shape
        val lp = LinearLayout.LayoutParams(WRAP, MATCH)
        lp.setMargins(dp(3), dp(4), dp(3), dp(4))
        t.layoutParams = lp
        t.setOnClickListener { v ->
            feedback(v)
            if (item.second == 9) voice() else toolbar(item.second)
        }
        inner.addView(t)
    }
    val sv = HorizontalScrollView(this)
    sv.isHorizontalScrollBarEnabled = false
    sv.layoutParams = LinearLayout.LayoutParams(MATCH, dp((42 * scale).toInt()))
    sv.addView(inner)
    return sv
}
