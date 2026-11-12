package com.genkey.keyboard

object Avro {
    private class Tok(val key: String, val type: Char, val a: String, val b: String = "")

    private val sorted = listOf(
        Tok("ksh", 'C', "ক্ষ"), Tok("kSh", 'C', "ক্ষ"), Tok("chh", 'C', "ছ"),
        Tok("rri", 'V', "ঋ", "\u09C3"),
        Tok("Ng", 'C', "ঙ"), Tok("NG", 'C', "ঞ"),
        Tok("kh", 'C', "খ"), Tok("gh", 'C', "ঘ"), Tok("ch", 'C', "চ"),
        Tok("jh", 'C', "ঝ"), Tok("Th", 'C', "ঠ"), Tok("Dh", 'C', "ঢ"),
        Tok("th", 'C', "থ"), Tok("dh", 'C', "ধ"), Tok("ph", 'C', "ফ"),
        Tok("bh", 'C', "ভ"), Tok("Sh", 'C', "ষ"), Tok("sh", 'C', "শ"),
        Tok("Rh", 'C', "\u09DD"),
        Tok("ng", 'S', "ং"),
        Tok("OI", 'V', "ঐ", "\u09C8"), Tok("oi", 'V', "ঐ", "\u09C8"),
        Tok("OU", 'V', "ঔ", "\u09CC"), Tok("ou", 'V', "ঔ", "\u09CC"),
        Tok("ii", 'V', "ঈ", "\u09C0"), Tok("ee", 'V', "ঈ", "\u09C0"),
        Tok("oo", 'V', "ঊ", "\u09C2"), Tok("uu", 'V', "ঊ", "\u09C2"),
        Tok("k", 'C', "ক"), Tok("g", 'C', "গ"), Tok("c", 'C', "চ"),
        Tok("j", 'C', "জ"), Tok("T", 'C', "ট"), Tok("D", 'C', "ড"),
        Tok("N", 'C', "ণ"), Tok("t", 'C', "ত"), Tok("d", 'C', "দ"),
        Tok("n", 'C', "ন"), Tok("p", 'C', "প"), Tok("f", 'C', "ফ"),
        Tok("b", 'C', "ব"), Tok("v", 'C', "ভ"), Tok("m", 'C', "ম"),
        Tok("z", 'C', "য"), Tok("r", 'C', "র"), Tok("l", 'C', "ল"),
        Tok("S", 'C', "শ"), Tok("s", 'C', "স"), Tok("h", 'C', "হ"),
        Tok("R", 'C', "\u09DC"), Tok("y", 'C', "\u09DF"), Tok("Y", 'C', "\u09DF"),
        Tok("w", 'C', "ওয়"), Tok("x", 'C', "ক্স"), Tok("q", 'C', "ক"),
        Tok("a", 'V', "আ", "\u09BE"), Tok("i", 'V', "ই", "\u09BF"),
        Tok("u", 'V', "উ", "\u09C1"), Tok("e", 'V', "এ", "\u09C7"),
        Tok("o", 'V', "অ", ""), Tok("O", 'V', "ও", "\u09CB"),
        Tok("I", 'V', "ঈ", "\u09C0"), Tok("U", 'V', "ঊ", "\u09C2")
    ).sortedByDescending { it.key.length }

    fun convert(s: String): String {
        val out = StringBuilder()
        var i = 0
        var afterCons = false
        while (i < s.length) {
            var m: Tok? = null
            for (t in sorted) {
                if (s.startsWith(t.key, i)) {
                    m = t
                    break
                }
            }
            if (m == null) {
                out.append(s[i])
                afterCons = false
                i++
                continue
            }
            when (m.type) {
                'C' -> {
                    var ch = m.a
                    if (afterCons) {
                        out.append('\u09CD')
                        if (m.key == "y" || m.key == "Y") ch = "য"
                    }
                    out.append(ch)
                    afterCons = true
                }
                'V' -> {
                    out.append(if (afterCons) m.b else m.a)
                    afterCons = false
                }
                else -> {
                    out.append(m.a)
                    afterCons = false
                }
            }
            i += m.key.length
        }
        return out.toString()
    }
}
