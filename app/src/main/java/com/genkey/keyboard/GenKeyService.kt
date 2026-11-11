package com.genkey.keyboard

import android.inputmethodservice.InputMethodService
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout

class GenKeyService : InputMethodService() {

    private val rows = listOf("qwertyuiop", "asdfghjkl", "zxcvbnm")

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    private fun makeKey(label: String, weight: Float, onClick: () -> Unit): Button {
        val b = Button(this)
        b.text = label
        b.isAllCaps = false
        b.setTextColor(0xFFFFFFFF.toInt())
        b.setBackgroundColor(0xFF2A2A2A.toInt())
        val lp = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, weight)
        lp.setMargins(dp(2), dp(2), dp(2), dp(2))
        b.layoutParams = lp
        b.setOnClickListener { onClick() }
        return b
    }

    private fun makeRow(): LinearLayout {
        val line = LinearLayout(this)
        line.orientation = LinearLayout.HORIZONTAL
        line.gravity = Gravity.CENTER
        line.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, dp(52)
        )
        return line
    }

    override fun onCreateInputView(): View {
        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setBackgroundColor(0xFF111111.toInt())
        root.setPadding(dp(4), dp(4), dp(4), dp(4))

        for (row in rows) {
            val line = makeRow()
            for (ch in row) {
                val s = ch.toString()
                line.addView(makeKey(s, 1f) {
                    currentInputConnection?.commitText(s, 1)
                })
            }
            root.addView(line)
        }

        val bottom = makeRow()
        bottom.addView(makeKey("⌫", 1.5f) {
            currentInputConnection?.deleteSurroundingText(1, 0)
        })
        bottom.addView(makeKey("space", 5f) {
            currentInputConnection?.commitText(" ", 1)
        })
        bottom.addView(makeKey(".", 1f) {
            currentInputConnection?.commitText(".", 1)
        })
        bottom.addView(makeKey("↵", 1.5f) {
            currentInputConnection?.commitText("\n", 1)
        })
        root.addView(bottom)

        return root
    }
}
