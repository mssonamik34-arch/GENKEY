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
    val outer = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(1), dp(1), dp(1), dp(1))
    }

    // Compact single-line toolbar: suggestions remain visible while controls stay small.
    val bar = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(dp(1), 0, dp(1), 0)
    }

    fun compactButton(symbol: String, width: Int, action: () -> Unit): TextView {
        val v = TextView(this)
        v.text = symbol
        v.textSize = 18f
        v.gravity = Gravity.CENTER
        v.setTextColor(0xFFF4F5FF.toInt())
        val shape = GradientDrawable()
        shape.setColor(0xFF181818.toInt())
        shape.cornerRadius = dp(18).toFloat()
        shape.setStroke(dp(1), 0xFF333333.toInt())
        v.background = shape
        val lp = LinearLayout.LayoutParams(dp(width), dp(34))
        lp.setMargins(dp(2), 0, dp(2), 0)
        bar.addView(v, lp)
        v.setOnClickListener { feedback(v); action() }
        return v
    }

    compactButton(if (toolbarHidden) ">" else "≡", 34) {
        toolbarHidden = !toolbarHidden
        render()
    }

    for (i in 0..2) {
        val suggestion = TextView(this)
        suggestion.textSize = 14f
        suggestion.gravity = Gravity.CENTER
        suggestion.maxLines = 1
        suggestion.setTextColor(0xFFF4F5FF.toInt())
        suggestion.setPadding(dp(2), 0, dp(2), 0)
        suggestion.setOnClickListener {
            val word = suggestion.text.toString()
            if (word.isNotBlank()) pickSuggestion(word)
        }
        sug[i] = suggestion
        bar.addView(suggestion, LinearLayout.LayoutParams(0, dp(34), 1f))
    }

    compactButton("🌐", 32) { cycleLanguage() }

    val mic = compactButton(if (VoiceHolder.state == VoiceState.LISTENING) "●" else "🎙", 34) {
        if (VoiceHolder.state == VoiceState.LISTENING) stopVoice() else voice()
        render()
    }
    if (VoiceHolder.state == VoiceState.LISTENING) {
        val pulse = android.view.animation.AlphaAnimation(0.45f, 1f).apply {
            duration = 650
            repeatMode = android.view.animation.Animation.REVERSE
            repeatCount = android.view.animation.Animation.INFINITE
        }
        mic.startAnimation(pulse)
        mic.setTextColor(0xFFFF4545.toInt())
    }

    outer.addView(bar)

    if (toolbarExpanded && !toolbarHidden) {
        val tools = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        val actions = listOf(
            "😊" to { layer = 2; render() },
            "📋" to { toolbar(7) },
            "⇄" to { toolbar(8) },
            "⚙" to { toolbar(0) }
        )
        actions.forEach { (symbol, action) ->
            val v = TextView(this)
            v.text = symbol
            v.textSize = 17f
            v.gravity = Gravity.CENTER
            v.setTextColor(0xFFF4F5FF.toInt())
            val shape = GradientDrawable()
            shape.setColor(0xFF181818.toInt())
            shape.cornerRadius = dp(12).toFloat()
            shape.setStroke(dp(1), 0xFF333333.toInt())
            v.background = shape
            val lp = LinearLayout.LayoutParams(0, dp(30), 1f)
            lp.setMargins(dp(2), dp(1), dp(2), dp(1))
            tools.addView(v, lp)
            v.setOnClickListener { feedback(v); action() }
        }
        outer.addView(tools)
    }
    return outer
}
