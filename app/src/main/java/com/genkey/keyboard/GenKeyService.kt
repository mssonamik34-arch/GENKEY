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
        if (VoiceActivity.pending != null) {
            handler.postDelayed({
                val t = VoiceActivity.pending
                if (t != null) {
                    currentInputConnection?.commitText("$t ", 1)
                    VoiceActivity.pending = null
                }
            }, 300)
        }
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
        root.addView(topBar())
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
}package com.genkey.keyboard

import android.content.Context
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.media.AudioManager
import android.view.Gravity
import android.view.HapticFeedbackConstants
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.PopupWindow
import android.widget.TextView

fun GenKeyService.commit(s: String) {
    flush()
    undoOrig = null
    currentInputConnection?.commitText(s, 1)
}

fun GenKeyService.typeLetter(s: String) {
    val c = if (shift) s.uppercase() else s
    if (lang == 2) {
        undoOrig = null
        buf.append(c)
        currentInputConnection?.setComposingText(Avro.convert(buf.toString()), 1)
    } else {
        commit(c)
    }
    if (shift) {
        shift = false
        render()
    }
}

fun GenKeyService.backspace() {
    val o = undoOrig
    val ic = currentInputConnection
    if (o != null && ic != null) {
        undoOrig = null
        ic.deleteSurroundingText(undoLen + 1, 0)
        ic.commitText(o, 1)
        Dict.learn(o, 2)
        return
    }
    if (buf.isNotEmpty()) {
        buf.deleteCharAt(buf.length - 1)
        if (buf.isEmpty()) {
            ic?.setComposingText("", 1)
            ic?.finishComposingText()
        } else {
            ic?.setComposingText(Avro.convert(buf.toString()), 1)
        }
    } else {
        sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL)
    }
}

fun GenKeyService.enter() {
    flush()
    undoOrig = null
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

fun GenKeyService.currentToken(): String {
    val before = currentInputConnection?.getTextBeforeCursor(30, 0) ?: return ""
    var i = before.length
    while (i > 0 && Dict.isWordChar(before[i - 1])) i--
    return before.subSequence(i, before.length).toString()
}

fun GenKeyService.space() {
    flush()
    val ic = currentInputConnection ?: return
    val tok = currentToken()
    if (tok.isNotEmpty()) {
        val low = tok.lowercase()
        val sc = shortcuts[low]
        if (sc != null) {
            ic.deleteSurroundingText(tok.length, 0)
            ic.commitText("$sc ", 1)
            undoOrig = null
            return
        }
        if (lang == 0 && layer == 0 && sp.getBoolean("autocorrect", false) &&
            tok.length >= 3 && !Dict.known(low)
        ) {
            val c = Dict.correct(low)
            if (c != null) {
                var out: String = c
                if (tok[0].isUpperCase()) out = out.replaceFirstChar { it.uppercase() }
                ic.deleteSurroundingText(tok.length, 0)
                ic.commitText("$out ", 1)
                undoOrig = tok
                undoLen = out.length
                return
            }
        }
        Dict.learn(tok)
    }
    undoOrig = null
    ic.commitText(" ", 1)
}

fun GenKeyService.updateSuggestions() {
    if (layer != 0) return
    for (i in 0..2) sug[i]?.text = ""
    if (!sp.getBoolean("suggest", true)) return
    val words = Dict.suggest(currentToken(), 3)
    val order = intArrayOf(1, 0, 2)
    words.forEachIndexed { i, w -> sug[order[i]]?.text = w }
}

fun GenKeyService.pickSuggestion(w: String) {
    if (w.isEmpty()) return
    flush()
    val ic = currentInputConnection ?: return
    val tok = currentToken()
    var out = w
    if (tok.isNotEmpty() && tok[0].isUpperCase()) out = out.replaceFirstChar { it.uppercase() }
    ic.deleteSurroundingText(tok.length, 0)
    ic.commitText("$out ", 1)
    undoOrig = null
    Dict.learn(out)
    updateSuggestions()
}

fun GenKeyService.voice() {
    flush()
    val i = Intent(this, VoiceActivity::class.java)
    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    i.putExtra("lang", if (lang == 0) "en-US" else "bn-BD")
    startActivity(i)
}

fun GenKeyService.feedback(v: View) {
    if (sp.getBoolean("vib", true)) {
        v.performHapticFeedback(
            HapticFeedbackConstants.KEYBOARD_TAP,
            HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
        )
    }
    if (sp.getBoolean("sound", true)) {
        (getSystemService(Context.AUDIO_SERVICE) as AudioManager)
            .playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD)
    }
}

fun GenKeyService.showPreview(v: View, s: String) {
    try {
        hidePreview()
        val t = TextView(this)
        t.text = s
        t.textSize = 28f
        t.gravity = Gravity.CENTER
        t.setTextColor(Themes.textOn(BLUE))
        val g = GradientDrawable()
        g.cornerRadius = dp(12).toFloat()
        g.setColor(BLUE)
        t.background = g
        val w = dp(56)
        val h = dp(64)
        val p = PopupWindow(t, w, h)
        p.isClippingEnabled = false
        p.showAsDropDown(v, (v.width - w) / 2, -(v.height + h))
        popup = p
    } catch (e: Exception) {
    }
}

fun GenKeyService.hidePreview() {
    try {
        popup?.dismiss()
    } catch (e: Exception) {
    }
    popup = null
}package com.genkey.keyboard

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView

fun GenKeyService.row(h: Int = 54): LinearLayout {
    val l = LinearLayout(this)
    l.orientation = LinearLayout.HORIZONTAL
    l.gravity = Gravity.CENTER
    l.layoutParams = LinearLayout.LayoutParams(MATCH, dp((h * scale).toInt()))
    return l
}

fun GenKeyService.label(s: String, size: Float, color: Int, weight: Float): TextView {
    val t = TextView(this)
    t.text = s
    t.textSize = size
    t.setTextColor(color)
    t.gravity = Gravity.CENTER
    t.layoutParams = LinearLayout.LayoutParams(0, MATCH, weight)
    return t
}

fun GenKeyService.toolbar(i: Int) {
    val ic = currentInputConnection
    when (i) {
        0 -> startActivity(Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        1 -> ic?.performContextMenuAction(android.R.id.undo)
        2 -> ic?.performContextMenuAction(android.R.id.redo)
        3 -> ic?.performContextMenuAction(android.R.id.selectAll)
        4 -> ic?.performContextMenuAction(android.R.id.copy)
        5 -> ic?.performContextMenuAction(android.R.id.cut)
        6 -> {
            flush()
            ic?.performContextMenuAction(android.R.id.paste)
        }
        7 -> {
            layer = 3
            render()
        }
        8 -> {
            oneHand = (oneHand + 1) % 3
            sp.edit().putInt("onehand", oneHand).apply()
            render()
        }
    }
}

fun GenKeyService.topBar(): LinearLayout {
    val r = row(40)
    val items = listOf("⚙", "↶", "↷", "▦", "⧉", "✂", "📋", "🕘", "⇄")
    items.forEachIndexed { i, s ->
        val t = label(s, 17f, textOnBg, 1f)
        t.setOnClickListener {
            feedback(it)
            toolbar(i)
        }
        r.addView(t)
    }
    return r
}

fun GenKeyService.langName(): String = when (lang) {
    0 -> "EN"
    1 -> "বাংলা"
    else -> "Avro"
}

fun GenKeyService.strip(): LinearLayout {
    val r = row(44)
    r.addView(label(langName(), 15f, textOnBg, 0.9f))
    for (i in 0..2) {
        val t = label("", 16f, textOnBg, 1.4f)
        t.maxLines = 1
        t.setOnClickListener { pickSuggestion(t.text.toString()) }
        sug[i] = t
        r.addView(t)
    }
    val g = label("🌐", 20f, textOnBg, 0.9f)
    g.setOnClickListener {
        feedback(it)
        flush()
        val e = enabledLangs()
        lang = e[(e.indexOf(lang) + 1) % e.size]
        bnPage = 0
        layer = 0
        render()
    }
    g.setOnLongClickListener {
        (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).showInputMethodPicker()
        true
    }
    r.addView(g)
    return r
}

fun GenKeyService.key(
    label: String,
    bg: Int,
    weight: Float,
    hint: String? = null,
    size: Float = 20f,
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

    val fg = Themes.textOn(bg)
    val main = TextView(this)
    main.text = label
    main.setTextColor(fg)
    main.textSize = size
    main.typeface = if (label.all { it.code < 128 }) keyFont else Typeface.DEFAULT
    main.gravity = Gravity.CENTER
    f.addView(main, FrameLayout.LayoutParams(MATCH, MATCH))

    if (hint != null) {
        val h = TextView(this)
        h.text = hint
        h.textSize = 10f
        h.setTextColor(if (fg == Color.BLACK) 0xFF555555.toInt() else 0xFFBBBBBB.toInt())
        val hp = FrameLayout.LayoutParams(WRAP, WRAP, Gravity.TOP or Gravity.END)
        hp.setMargins(0, dp(3), dp(6), 0)
        f.addView(h, hp)
        f.setOnLongClickListener {
            commit(hint)
            true
        }
    }
    if (label.length == 1 && label[0].isLetterOrDigit() && sp.getBoolean("popup", true)) {
        f.setOnTouchListener { v, e ->
            when (e.action) {
                MotionEvent.ACTION_DOWN -> showPreview(v, label)
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> hidePreview()
            }
            false
        }
    }
    f.setOnClickListener {
        feedback(f)
        onTap()
    }
    return f
}

fun GenKeyService.backspaceKey(): FrameLayout {
    val b = key("⌫", WHITE, 1.5f) { }
    b.setOnTouchListener { _, e ->
        when (e.action) {
            MotionEvent.ACTION_DOWN -> {
                feedback(b)
                backspace()
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

fun GenKeyService.spaceKey(weight: Float): FrameLayout =
    key("GENKEY", DARK, weight, size = 12f) { space() }

fun GenKeyService.bottomRow(first: String, goTo: Int): LinearLayout {
    val r = row()
    r.addView(key(first, RED, 1.3f, size = 14f) {
        layer = goTo
        render()
    })
    r.addView(key("😊", BLUE, 1f) {
        layer = 2
        render()
    })
    r.addView(key("🎤", YELLOW, 1f) { voice() })
    r.addView(key(",", WHITE, 0.8f) { commit(",") })
    r.addView(spaceKey(3f))
    r.addView(key(".", DARK, 0.8f) { commit(".") })
    r.addView(key("⏎", RED, 1.3f) { enter() })
    return r
}package com.genkey.keyboard

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
}package com.genkey.keyboard

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
