package com.genkey.keyboard

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
}
