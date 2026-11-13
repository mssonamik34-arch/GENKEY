package com.genkey.keyboard

import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView

fun GenKeyService.toolbarRow(): View {
    val outer = LinearLayout(this)
    outer.orientation = LinearLayout.VERTICAL
    outer.setPadding(dp(2), dp(2), dp(2), dp(2))

    val top = LinearLayout(this)
    top.orientation = LinearLayout.HORIZONTAL
    top.gravity = Gravity.CENTER_VERTICAL
    val topBg = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
        intArrayOf(0xFF17172D.toInt(), 0xFF10263B.toInt()))
    topBg.cornerRadius = dp(13).toFloat()
    top.background = topBg

    val menu = TextView(this)
    menu.text = if (toolbarExpanded) "✕" else "☰"
    menu.textSize = 20f
    menu.gravity = Gravity.CENTER
    menu.setTextColor(0xFFE7E8FF.toInt())
    val menuBg = GradientDrawable()
    menuBg.setColor(0xFF29294A.toInt())
    menuBg.cornerRadius = dp(10).toFloat()
    menu.background = menuBg
    val menuParams = LinearLayout.LayoutParams(dp(42), dp(36))
    menuParams.setMargins(dp(4), dp(3), dp(8), dp(3))
    top.addView(menu, menuParams)
    menu.setOnClickListener { toolbarExpanded = !toolbarExpanded; render() }

    val brand = LinearLayout(this)
    brand.orientation = LinearLayout.VERTICAL
    brand.gravity = Gravity.CENTER_VERTICAL
    val title = TextView(this)
    title.text = if (VoiceHolder.state == VoiceState.LISTENING) "GENKEY  •  শুনছি..." else "GENKEY"
    title.textSize = 12f
    title.letterSpacing = 0.06f
    title.typeface = Typeface.DEFAULT_BOLD
    title.setTextColor(0xFFF4F3FF.toInt())
    brand.addView(title)
    val sub = TextView(this)
    sub.text = "PREMIUM KEYBOARD"
    sub.textSize = 8f
    sub.typeface = Typeface.DEFAULT_BOLD
    sub.letterSpacing = 0.16f
    sub.setTextColor(0xFF9C9BEA.toInt())
    brand.addView(sub)
    top.addView(brand, LinearLayout.LayoutParams(0, dp(40), 1f))

    val live = TextView(this)
    live.text = "✦"
    live.textSize = 19f
    live.gravity = Gravity.CENTER
    live.setTextColor(0xFF79D8FF.toInt())
    top.addView(live, LinearLayout.LayoutParams(dp(36), dp(40)))
    outer.addView(top)

    if (toolbarExpanded) {
        val items = listOf(
            Pair(if (VoiceHolder.state == VoiceState.LISTENING) "⏹ বন্ধ" else "🎤 ভয়েস", 9),
            Pair("📋 পেস্ট", 6), Pair("⧉ কপি", 4), Pair("✂ কাট", 5), Pair("▦ সব", 3),
            Pair("🕘 ক্লিপ", 7), Pair("↶ আন্ডু", 1), Pair("↷ রিডু", 2), Pair("⇄ এক হাত", 8), Pair("⚙ সেটিংস", 0)
        )
        for (group in items.chunked(5)) {
            val row = LinearLayout(this)
            row.orientation = LinearLayout.HORIZONTAL
            for (item in group) {
                val t = TextView(this)
                t.text = item.first
                t.textSize = 11f
                t.gravity = Gravity.CENTER
                t.setTextColor(0xFFE9E9FF.toInt())
                val shape = GradientDrawable()
                shape.setColor(0xFF20213A.toInt())
                shape.cornerRadius = dp(9).toFloat()
                shape.setStroke(dp(1), 0x503F8CFF)
                t.background = shape
                val lp = LinearLayout.LayoutParams(0, dp(38), 1f)
                lp.setMargins(dp(2), dp(3), dp(2), dp(3))
                row.addView(t, lp)
                t.setOnClickListener { v ->
                    feedback(v)
                    if (item.second == 9) {
                        if (VoiceHolder.state == VoiceState.LISTENING) stopVoice() else voice()
                    } else toolbar(item.second)
                }
            }
            outer.addView(row)
        }
    }
    return outer
}
