package com.genkey.keyboard

object AvroDict {
    private val table = HashMap<String, MutableList<String>>()
    private val order = ArrayList<Pair<String, String>>()
    private var ready = false

    fun loose(s: String): String {
        val t = s.lowercase()
            .replace("bh", "v").replace("ph", "f").replace("sh", "s")
            .replace("ii", "i").replace("ee", "i").replace("oo", "u").replace("uu", "u")
        val sb = StringBuilder()
        for (c in t) {
            if (sb.isEmpty() || sb[sb.length - 1] != c) sb.append(c)
        }
        return sb.toString()
    }

    private fun init() {
        if (ready) return
        ready = true
        for (line in AVRO_WORDS.lines()) {
            val i = line.indexOf('=')
            if (i <= 0) continue
            val bn = line.substring(i + 1).trim()
            if (bn.isEmpty()) continue
            for (k in line.substring(0, i).split(",")) {
                val key = loose(k.trim())
                if (key.isEmpty()) continue
                val l = table.getOrPut(key) { mutableListOf() }
                if (!l.contains(bn)) l.add(bn)
                order.add(Pair(key, bn))
            }
        }
    }

    fun exact(roman: String): String? {
        init()
        return table[loose(roman)]?.firstOrNull()
    }

    fun candidates(roman: String, max: Int): List<String> {
        init()
        val out = ArrayList<String>()
        if (roman.isEmpty()) return out
        val k = loose(roman)
        table[k]?.let { out.addAll(it) }
        for (p in order) {
            if (out.size >= max) break
            if (p.first.length > k.length && p.first.startsWith(k) && !out.contains(p.second)) {
                out.add(p.second)
            }
        }
        return out
    }
}
