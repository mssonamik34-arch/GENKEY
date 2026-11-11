package com.genkey.keyboard

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.inputmethodservice.InputMethodService
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView

class GenKeyService : InputMethodService() {

    private val YELLOW = 0xFFE3B33A.toInt()
    private val BLUE = 0xFF2A5288.toInt()
    private val DARK = 0xFF1C1D21.toInt()
    private val WHITE = 0xFFEAE7E2.toInt()
    private val RED = 0xFFC23333.toInt()
    private val palette = listOf(YELLOW, BLUE, DARK, WHITE, RED)
    private val MATCH = ViewGroup.LayoutParams.MATCH_PARENT
    private val WRAP = ViewGroup.LayoutParams.WRAP_CONTENT

    private lateinit var root: LinearLayout
    private val letterViews = mutableListOf<TextView>()
    private var layer = 0
    private var shift = false

    private val handler = Handler(Looper.getMainLooper())
    private var repeating = false
    private val repeatTask = object : Runnable {
        override fun run() {
            if (!repeating) return
            sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL)
            handler.postDelayed(this, 50)
        }
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    override fun onCreateInputView(): View {
        root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setBackgroundColor(Color.BLACK)
        root.setPadding(dp(3), dp(2), dp(3), dp(6))
        render()
        return root
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        layer = 0
        shift = false
        if (::root.isInitialized) render()
    }

    private fun render() {
        root.removeAllViews()
        letterViews.clear()
        root.addView(topBar())
        root.addView(strip())
        when (layer) {
            0 -> buildLetters()
            1 -> buildSymbols()
            else -> buildEmoji()
        }
    }

    private fun commit(s: String) {
        currentInputConnection?.commitText(s, 1)
    }

    private fun typeLetter(s: String) {
        commit(if (shift) s.uppercase() else s)
        if (shift) {
            shift = false
            for (tv in letterViews) tv.text = tv.text.toString().lowercase()
            render()
        }
    }

    private fun enter() {
        val ei = currentInputEditorInfo
        if (ei != null) {
            val a = ei.imeOptions and EditorInfo.IME_MASK_ACTION
            val noAction = (ei.imeOptions and EditorInfo.IME_FLAG_NO_ENTER_ACTION) != 0
            if (!noAction && a != EditorInfo.IME_ACTION_NONE && a != EditorInfo.IME_ACTION_UNSPECIFIED) {
                currentInputConnection?.performEditorAction(a)
                return
            }
        }
        sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER)
    }

    private fun row(h: Int = 54): LinearLayout {
        val l = LinearLayout(this)
        l.orientation = LinearLayout.HORIZONTAL
        l.gravity = Gravity.CENTER
        l.layoutParams = LinearLayout.LayoutParams(MATCH, dp(h))
        return l
    }

    private fun label(s: String, size: Float, color: Int, weight: Float): TextView {
        val t = TextView(this)
        t.text = s
        t.textSize = size
        t.setTextColor(color)
        t.gravity = Gravity.CENTER
        t.layoutParams = LinearLayout.LayoutParams(0, MATCH, weight)
        return t
    }

    private fun topBar(): LinearLayout {
        val r = row(40)
        for (s in listOf("•••", "↶", "↷", "✎", "▤", "◐", "⛶")) {
            r.addView(label(s, 18f, 0xFFDDDDDD.toInt(), 1f))
        }
        return r
    }

    private fun strip(): LinearLayout {
        val r = row(44)
        r.addView(label("", 16f, Color.WHITE, 0.6f))
        repeat(3) { r.addView(label("", 16f, Color.WHITE, 1.4f)) }
        val g = label("🌐", 20f, Color.WHITE, 0.6f)
        g.setOnClickListener {
            (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).showInputMethodPicker()
        }
        r.addView(g)
        return r
    }

    private fun key(
        label: String,
        bg: Int,
        weight: Float,
        hint: String? = null,
        size: Float = 20f,
        isLetter: Boolean = false,
        onTap: () -> Unit
    ): FrameLayout {
        val f = FrameLayout(this)
        val shape = GradientDrawable()
        shape.cornerRadius = dp(10).toFloat()
        shape.setColor(bg)
        f.background = shape
        val lp = LinearLayout.LayoutParams(0, MATCH, weight)
        lp.setMargins(dp(2), dp(3), dp(2), dp(3))
        f.layoutParams = lp

        val light = (bg == YELLOW || bg == WHITE)
        val main = TextView(this)
        main.text = label
        main.setTextColor(if (light) Color.BLACK else Color.WHITE)
        main.textSize = size
        main.typeface = Typeface.MONOSPACE
        main.gravity = Gravity.CENTER
        f.addView(main, FrameLayout.LayoutParams(MATCH, MATCH))
        if (isLetter) letterViews.add(main)

        if (hint != null) {
            val h = TextView(this)
            h.text = hint
            h.textSize = 10f
            h.setTextColor(if (light) 0xFF555555.toInt() else 0xFFBBBBBB.toInt())
            val hp = FrameLayout.LayoutParams(WRAP, WRAP, Gravity.TOP or Gravity.END)
            hp.setMargins(0, dp(3), dp(6), 0)
            f.addView(h, hp)
            f.setOnLongClickListener {
                commit(hint)
                true
            }
        }
        f.setOnClickListener { onTap() }
        return f
    }

    private fun backspaceKey(): FrameLayout {
        val b = key("⌫", WHITE, 1.5f) { }
        b.setOnTouchListener { _, e ->
            when (e.action) {
                MotionEvent.ACTION_DOWN -> {
                    sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL)
                    repeating = true
                    handler.postDelayed(repeatTask, 400)
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    repeating = false
                    handler.removeCallbacks(repeatTask)
                }
            }
            true
        }
        return b
    }

    private fun spaceKey(weight: Float): FrameLayout =
        key("GENKEY", DARK, weight, size = 12f) { commit(" ") }

    private fun buildLetters() {
        val r1 = row()
        val c1 = listOf(YELLOW, BLUE, DARK, WHITE, RED, BLUE, WHITE, RED, YELLOW, DARK)
        "qwertyuiop".forEachIndexed { i, ch ->
            val s = ch.toString()
            r1.addView(key(if (shift) s.uppercase() else s, c1[i], 1f, "${(i + 1) % 10}", isLetter = true) { typeLetter(s) })
        }
        root.addView(r1)

        val r2 = row()
        r2.setPadding(dp(16), 0, dp(16), 0)
        val c2 = listOf(WHITE, RED, BLUE, DARK, YELLOW, WHITE, RED, BLUE, DARK)
        val h2 = "@#&*-!?()"
        "asdfghjkl".forEachIndexed { i, ch ->
            val s = ch.toString()
            r2.addView(key(if (shift) s.uppercase() else s, c2[i], 1f, h2[i].toString(), isLetter = true) { typeLetter(s) })
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
            r3.addView(key(if (shift) s.uppercase() else s, c3[i], 1f, h3[i], isLetter = true) { typeLetter(s) })
        }
        r3.addView(backspaceKey())
        root.addView(r3)

        root.addView(bottomRow("?123", 1))
    }

    private fun bottomRow(first: String, goTo: Int): LinearLayout {
        val r = row()
        r.addView(key(first, RED, 1.5f, size = 14f) {
            layer = goTo
            render()
        })
        r.addView(key("😊", BLUE, 1.2f, size = 20f) {
            layer = 2
            render()
        })
        r.addView(key(",", WHITE, 1f) { commit(",") })
        r.addView(spaceKey(4f))
        r.addView(key(".", DARK, 1f) { commit(".") })
        r.addView(key("⏎", RED, 1.5f) { enter() })
        return r
    }

    private fun buildSymbols() {
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

    private fun buildEmoji() {
        val rows = listOf(
            listOf("😀", "😂", "🤣", "😊", "😍", "🥰", "😘", "😎"),
            listOf("🤔", "😢", "😭", "😡", "😱", "🙄", "😴", "🥳"),
            listOf("👍", "👎", "🙏", "👏", "💪", "🤝", "👀", "🔥"),
            listOf("❤️", "💔", "🎉", "✨", "💯", "✅", "❌", "🎶")
        )
        for ((ri, list) in rows.withIndex()) {
            val r = row(50)
            list.forEachIndexed { i, e ->
                r.addView(key(e, palette[(i + ri) % 5], 1f, size = 22f) { commit(e) })
            }
            root.addView(r)
        }
        val b = row()
        b.addView(key("ABC", RED, 1.5f, size = 14f) {
            layer = 0
            render()
        })
        b.addView(spaceKey(5f))
        b.addView(backspaceKey())
        root.addView(b)
    }
}
