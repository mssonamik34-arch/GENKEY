package com.genkey.keyboard

import android.content.ClipboardManager
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Typeface
import android.inputmethodservice.InputMethodService
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView

internal val MATCH = ViewGroup.LayoutParams.MATCH_PARENT
internal val WRAP = ViewGroup.LayoutParams.WRAP_CONTENT

class GenKeyService : InputMethodService() {

    internal lateinit var sp: SharedPreferences
    internal lateinit var root: LinearLayout
    internal var palette: List<Int> = Themes.get(0).keys
    internal var bgColor: Int = Color.BLACK
    internal val YELLOW: Int get() = palette[0]
    internal val BLUE: Int get() = palette[1]
    internal val DARK: Int get() = palette[2]
    internal val WHITE: Int get() = palette[3]
    internal val RED: Int get() = palette[4]
    internal val textOnBg: Int get() = Themes.textOn(bgColor)
    internal var layer = 0
    internal var lang = 0
    internal var bnPage = 0
    internal var shift = false
    internal var numRow = false
    internal var oneHand = 0
    internal var scale = 1f
    internal var undoOrig: String? = null
    internal var undoLen = 0
    internal var popup: PopupWindow? = null
    internal var repeating = false
    internal val buf = StringBuilder()
    internal val clips = mutableListOf<String>()
    internal val shortcuts = HashMap<String, String>()
    internal val sug = arrayOfNulls<TextView>(3)
    internal val handler = Handler(Looper.getMainLooper())
    internal val keyFont: Typeface by lazy { loadFont() }
    internal val repeatTask: Runnable = object : Runnable {
        override fun run() {
            if (!repeating) return
            this@GenKeyService.backspace()
            handler.postDelayed(this, 50)
        }
    }

    internal fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()

    internal fun voice() {
        startVoice()
    }

    internal fun loadFont(): Typeface {
        val id = resources.getIdentifier("dotfont", "font", packageName)
        if (id != 0 && Build.VERSION.SDK_INT >= 26) {
            try {
                return resources.getFont(id)
            } catch (e: Exception) {
            }
        }
        return Typeface.MONOSPACE
    }

    override fun onCreate() {
        super.onCreate()
        sp = getSharedPreferences("genkey", MODE_PRIVATE)
        Dict.load(sp)
        watchClipboard()
    }

    override fun onDestroy() {
        stopVoice()
        super.onDestroy()
    }

    private fun watchClipboard() {
        val cm = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        cm.addPrimaryClipChangedListener {
            try {
                val t = cm.primaryClip?.getItemAt(0)?.coerceToText(this)?.toString()
                if (!t.isNullOrBlank()) {
                    clips.remove(t)
                    clips.add(0, t)
                    while (clips.size > 20) clips.removeAt(clips.size - 1)
                }
            } catch (e: Exception) {
            }
        }
    }

    internal fun enabledLangs(): List<Int> {
        val l = ArrayList<Int>()
        if (sp.getBoolean("lang_en", true)) l.add(0)
        if (sp.getBoolean("lang_bn", true)) l.add(1)
        if (sp.getBoolean("lang_avro", true)) l.add(2)
        if (l.isEmpty()) l.add(0)
        return l
    }

    private fun loadPrefs() {
        val t = Themes.get(sp.getInt("theme", 0))
        palette = t.keys
        bgColor = t.bg
        scale = sp.getInt("height", 100) / 100f
        numRow = sp.getBoolean("numrow", false)
        oneHand = sp.getInt("onehand", 0)
        shortcuts.clear()
        for (line in (sp.getString("shortcuts", "") ?: "").split("\n")) {
            val i = line.indexOf('=')
            if (i > 0) shortcuts[line.substring(0, i).trim().lowercase()] = line.substring(i + 1).trim()
        }
        val en = enabledLangs()
        if (!en.contains(lang)) lang = en[0]
    }

    override fun onCreateInputView(): View {
        root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        loadPrefs()
        render()
        return root
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        buf.setLength(0)
        undoOrig = null
        layer = 0
        shift = false
        loadPrefs()
        if (::root.isInitialized) render()
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        stopVoice()
    }

    override fun onFinishInput() {
        super.onFinishInput()
        Dict.save(sp)
    }

    override fun onUpdateSelection(
        oldSelStart: Int, oldSelEnd: Int, newSelStart: Int, newSelEnd: Int,
        candidatesStart: Int, candidatesEnd: Int
    ) {
        super.onUpdateSelection(oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd)
        updateSuggestions()
    }

    internal fun render() {
        hidePreview()
        root.removeAllViews()
        root.setBackgroundColor(bgColor)
        val side = (resources.displayMetrics.widthPixels * 0.22f).toInt()
        root.setPadding(
            dp(3) + (if (oneHand == 2) side else 0), dp(2),
            dp(3) + (if (oneHand == 1) side else 0), dp(6)
        )
        if (layer == 2) {
            buildEmoji()
            return
        }
        root.addView(toolbarRow())
        root.addView(strip())
        when (layer) {
            0 -> if (lang == 1) buildBangla() else buildLetters()
            1 -> buildSymbols()
            else -> buildClipboard()
        }
        updateSuggestions()
    }

    internal fun flush() {
        if (buf.isNotEmpty()) {
            currentInputConnection?.finishComposingText()
            buf.setLength(0)
        }
    }
}
