
package com.genkey.keyboard

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView

fun GenKeyService.toolbarRow(): View {
    val outer = LinearLayout(this)
    outer.orientation = LinearLayout.VERTICAL

    val top = LinearLayout(this)
    top.orientation = LinearLayout.HORIZONTAL
    top.gravity = Gravity.CENTER_VERTICAL

    val menu = TextView(this)
    menu.text = if (toolbarExpanded) "✕" else "☰"
    menu.textSize = 21f
    menu.gravity = Gravity.CENTER
    menu.setTextColor(WHITE)

    val menuBg = GradientDrawable()
    menuBg.setColor(DARK)
    menuBg.cornerRadius = dp(9).toFloat()
    menu.background = menuBg

    val menuParams = LinearLayout.LayoutParams(dp(43), dp(38))
    menuParams.setMargins(dp(2), dp(2), dp(5), dp(4))
    top.addView(menu, menuParams)

    menu.setOnClickListener {
        toolbarExpanded = !toolbarExpanded
        render()
    }

    val label = TextView(this)
    label.text = if (VoiceHolder.state == VoiceState.LISTENING) "GENKEY • শুনছি..." else "GENKEY"
    label.textSize = 14f
    label.typeface = Typeface.DEFAULT_BOLD
    label.setTextColor(WHITE)
    label.gravity = Gravity.CENTER_VERTICAL

    top.addView(
        label,
        LinearLayout.LayoutParams(0, dp(38), 1f)
    )

    outer.addView(top)

    if (toolbarExpanded) {
        val items = listOf(
            Pair(if (VoiceHolder.state == VoiceState.LISTENING) "⏹ বন্ধ" else "🎤 ভয়েস", 9),
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

        val rows = items.chunked(5)

        for (group in rows) {
            val row = LinearLayout(this)
            row.orientation = LinearLayout.HORIZONTAL

            for (item in group) {
                val t = TextView(this)
                t.text = item.first
                t.textSize = 11f
                t.gravity = Gravity.CENTER
                t.setTextColor(WHITE)

                val shape = GradientDrawable()
                shape.setColor(DARK)
                shape.cornerRadius = dp(8).toFloat()
                t.background = shape

                val lp = LinearLayout.LayoutParams(
                    0,
                    dp(38),
                    1f
                )
                lp.setMargins(dp(2), dp(2), dp(2), dp(2))

                row.addView(t, lp)

                t.setOnClickListener { v ->
                    feedback(v)

                    if (item.second == 9) {
                        if (VoiceHolder.state == VoiceState.LISTENING) stopVoice() else voice()
                    } else {
                        toolbar(item.second)
                    }
                }
            }

            outer.addView(row)
        }
    }

    return outer
}
