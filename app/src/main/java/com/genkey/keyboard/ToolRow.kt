package com.genkey.keyboard

import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView

fun GenKeyService.toolbarRow(): View {
    val outer = LinearLayout(this)
    outer.orientation = LinearLayout.VERTICAL
    outer.setPadding(dp(2), dp(2), dp(2), dp(2))

    val main = LinearLayout(this)
    main.orientation = LinearLayout.HORIZONTAL
    main.gravity = Gravity.CENTER_VERTICAL

    fun icon(symbol: String, width: Int = 36, action: () -> Unit): TextView {
        val t = TextView(this)
        t.text = symbol
        t.textSize = 19f
        t.gravity = Gravity.CENTER
        t.setTextColor(textOnBg)
        val shape = GradientDrawable()
        shape.setColor(0x332A86FF)
        shape.cornerRadius = dp(10).toFloat()
        t.background = shape
        val lp = LinearLayout.LayoutParams(dp(width), dp(40))
        lp.setMargins(dp(1), dp(1), dp(1), dp(1))
        main.addView(t, lp)
        t.setOnClickListener { feedback(t); action() }
        return t
    }

    icon(if (toolbarExpanded) "‹" else "☰", 36) {
        toolbarExpanded = !toolbarExpanded
        render()
    }

    for (i in 0..2) {
        val suggestion = TextView(this)
        suggestion.textSize = 15f
        suggestion.gravity = Gravity.CENTER
        suggestion.maxLines = 1
        suggestion.setTextColor(textOnBg)
        suggestion.setPadding(dp(2), 0, dp(2), 0)
        suggestion.setOnClickListener { pickSuggestion(suggestion.text.toString()) }
        sug[i] = suggestion
        main.addView(suggestion, LinearLayout.LayoutParams(0, dp(40), 1f))
    }

    icon("🎙", 38) {
        if (VoiceHolder.state == VoiceState.LISTENING) stopVoice() else voice()
    }
    outer.addView(main)

    if (toolbarExpanded) {
        val tools = LinearLayout(this)
        tools.orientation = LinearLayout.HORIZONTAL
        tools.gravity = Gravity.CENTER
        val actions = listOf(
            Triple("😊", 30) { layer = 2; render() },
            Triple("📋", 7) { toolbar(7) },
            Triple("⇄", 8) { toolbar(8) },
            Triple("⚙", 0) { toolbar(0) }
        )
        for ((symbol, _, action) in actions) {
            val t = TextView(this)
            t.text = symbol
            t.textSize = 19f
            t.gravity = Gravity.CENTER
            t.setTextColor(textOnBg)
            val shape = GradientDrawable()
            shape.setColor(0x332A86FF)
            shape.cornerRadius = dp(10).toFloat()
            t.background = shape
            val lp = LinearLayout.LayoutParams(0, dp(36), 1f)
            lp.setMargins(dp(3), dp(2), dp(3), dp(2))
            tools.addView(t, lp)
            t.setOnClickListener { feedback(t); action() }
        }
        outer.addView(tools)
    }
    return outer
}


fun GenKeyService.modernToolbarRow(): View {
    val bar = LinearLayout(this)
    bar.orientation = LinearLayout.HORIZONTAL
    bar.gravity = Gravity.CENTER_VERTICAL
    bar.setPadding(dp(1), dp(5), dp(1), dp(5))
    val items = listOf(
        "⊞" to { startActivity(android.content.Intent(this, MainActivity::class.java).addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)) },
        "▤" to { layer = 3; render() },
        "A" to { shift = !shift; render() },
        "😊" to { layer = 2; render() },
        "⠿" to { layer = if (layer == 1) 0 else 1; render() },
        "文A" to { cycleLanguage() },
        "🎤" to { if (VoiceHolder.state == VoiceState.LISTENING) stopVoice() else voice() },
        "⌄" to { requestHideSelf(0) }
    )
    items.forEachIndexed { index, item ->
        val t = TextView(this)
        t.text = item.first
        t.textSize = if (index == 5) 15f else 20f
        t.gravity = Gravity.CENTER
        t.setTextColor(0xFFF4F5FF.toInt())
        val shape = GradientDrawable(GradientDrawable.Orientation.TL_BR,
            intArrayOf(if (index == 0) 0xFF283C9B.toInt() else 0xFF182342.toInt(),
                if (index == 0) 0xFF342078.toInt() else 0xFF10172E.toInt()))
        shape.cornerRadius = dp(28).toFloat()
        shape.setStroke(dp(1), 0x663F65D9)
        t.background = shape
        t.elevation = dp(3).toFloat()
        val lp = LinearLayout.LayoutParams(0, dp(42), 1f)
        lp.setMargins(dp(2), 0, dp(2), 0)
        bar.addView(t, lp)
        t.setOnClickListener { feedback(t); item.second() }
    }
    return bar
}
