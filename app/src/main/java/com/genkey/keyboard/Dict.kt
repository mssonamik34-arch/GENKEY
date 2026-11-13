package com.genkey.keyboard

import android.content.SharedPreferences
import kotlin.math.abs

object Dict {
    private val en = ("the be to of and a in that have i it for not on with he as you do at this but his by from they we say her she or an will my one all would there their what so up out if about who get which go me when make can like time no just him know take people into year your good some could them see other than then now look only come its over think also back after use two how our work first well way even new want because any these give day most us is was are been has had were said did may call down find long little very still every great where much through before must same too does another tell each right big again off old need feel high last never between life world ask small end put home read hand large here such follow why men change went light kind house try animal point mother near build earth father head stand own page should country found answer school grow study learn plant food sun four thing girl boy name man help turn mean move set three air play line state keep eye let thought city tree cross hard start might story far sea draw left late run while open seem together next white children begin walk example paper group always music those both often letter until river car care second book carry took eat room friend idea mountain stop once hear cut sure watch color face enough young ready above ever red list though talk soon body dog family leave song door black short class wind question happen complete ship half order fire problem pass since top whole king space heard best hour better true during five remember step early hold west ground interest reach fast sing listen six table travel less morning ten simple toward war lay against slow center love person money serve appear road rain rule pull cold notice voice power town fine certain fly fall lead dark machine note wait plan figure star box rest correct able done beauty drive front teach week final green quick develop ocean warm free minute strong special mind behind clear produce street nothing course stay wheel full force blue object decide surface deep moon island foot system busy test record boat common gold possible dry wonder laugh thousand ago check game shape hot miss heat snow bring yes fill east paint language among hello hi hey thanks thank please sorry okay ok yeah bro brother sister mom dad friends today tomorrow yesterday tonight evening happy sad angry tired hungry message phone call online offline video photo welcome congratulations birthday football match team player goal win lose going doing coming wanna gonna really maybe texting reply send sent received waiting working studying playing eating sleeping watching thinking looking talking running trying using making taking giving getting having being lol omg btw bye yo wow haha hehe idk plz pls " +
        "ami tumi apni ki keno kothay kokhon kemon achho achi bhalo kharap dhonnobad ache nai hya na accha thik ekhon pore age kal aj bondhu bhai bon baba ma khabo jabo asbo korbo korchi korcho bolo bolchi dekho dekhi jani chai pari lagbe hobe hoyeche gese eto onek ektu abar tai kintu ar ebong jonno theke shathe moddhe ekhane sekhane ei oi sei kothai valo bhalobashi kaj taka khabar pani ghum porchi khelbo khelchi match game ase aso jao jacchi asche").split(" ") + ENGLISH_EXTRA_WORDS;

    private val bn = ("আমি তুমি সে আমরা তোমরা তারা আপনি কি কী কেন কোথায় কখন কিভাবে কেমন আছো আছি আছে নেই হ্যাঁ না ভালো খারাপ ধন্যবাদ ঠিক আজ কাল এখন পরে আগে সময় দিন রাত সকাল বিকাল সন্ধ্যা বাড়ি কাজ টাকা খাবার পানি বন্ধু ভাই বোন মা বাবা ভালোবাসি দুঃখিত করে আসো যাও যাচ্ছি আসছি করছি করছো বলো বলছি দেখো দেখি শোনো জানি জানো চাই চাও পারি পারো লাগবে হবে হয় হয়েছে গেছে এসেছে খেয়েছো ঘুম পড়া খেলা গান ছবি ফোন মেসেজ কল অনেক একটু আরো আর এবং কিন্তু তবে যদি তাহলে তাই কারণ সাথে জন্য থেকে পর্যন্ত মধ্যে উপর নিচে এখানে সেখানে এই ওই সেই একটা দুইটা প্রথম শেষ নতুন পুরনো বড় ছোট সুন্দর দারুণ মজা রাগ খুশি কষ্ট সবাই কেউ কিছু সব মানুষ দেশ বাংলাদেশ ঢাকা স্কুল কলেজ পরীক্ষা ফুটবল ম্যাচ গেম").split(" ")

    private val base = LinkedHashMap<String, Int>()
    private val learned = HashMap<String, Int>()
    private var loaded = false
    private var dirty = false

    fun isWordChar(c: Char): Boolean {
        if (Character.isLetter(c)) return true
        val t = Character.getType(c)
        return t == Character.NON_SPACING_MARK.toInt() || t == Character.COMBINING_SPACING_MARK.toInt()
    }

    fun load(sp: SharedPreferences) {
        if (loaded) return
        loaded = true
        var s = 1000
        for (w in en) if (w.isNotEmpty() && !base.containsKey(w)) base[w] = s--
        s = 1000
        for (w in bn) if (w.isNotEmpty() && !base.containsKey(w)) base[w] = s--
        val saved = sp.getString("learned", "") ?: ""
        for (p in saved.split(",")) {
            val kv = p.split(":")
            if (kv.size == 2) {
                val n = kv[1].toIntOrNull()
                if (n != null) learned[kv[0]] = n
            }
        }
    }

    fun save(sp: SharedPreferences) {
        if (!dirty) return
        dirty = false
        sp.edit().putString("learned", learned.entries.joinToString(",") { it.key + ":" + it.value }).apply()
    }

    fun clearLearned(sp: SharedPreferences) {
        learned.clear()
        dirty = false
        sp.edit().remove("learned").apply()
    }

    fun learn(w: String, n: Int = 1) {
        val l = w.lowercase()
        if (l.length < 2 || l.length > 24 || !l.all { isWordChar(it) }) return
        learned[l] = (learned[l] ?: 0) + n
        dirty = true
        if (learned.size > 3000) learned.entries.removeAll { it.value <= 1 }
    }

    fun known(w: String): Boolean = base.containsKey(w) || (learned[w] ?: 0) >= 2

    private fun score(w: String): Int = (base[w] ?: 0) + (learned[w] ?: 0) * 200

    private fun forEachWord(f: (String) -> Unit) {
        for (w in base.keys) f(w)
        for (w in learned.keys) if (!base.containsKey(w)) f(w)
    }

    fun suggest(prefix: String, max: Int = 3): List<String> {
        if (prefix.isEmpty()) return emptyList()
        val p = prefix.lowercase()
        val res = ArrayList<Pair<String, Int>>()
        forEachWord { w -> if (w.startsWith(p) && w != p) res.add(Pair(w, score(w))) }
        res.sortByDescending { it.second }
        val out = res.take(max).map { it.first }.toMutableList()
        if (p.length >= 3 && p[0].code < 0x0980 && !known(p)) {
            val c = correct(p)
            if (c != null && !out.contains(c)) out.add(0, c)
        }
        return out.take(max)
    }

    fun correct(p: String): String? {
        if (p.length < 3) return null
        var best: String? = null
        var bd = 9
        var bs = -1
        forEachWord { w ->
            if (w[0] == p[0] && abs(w.length - p.length) <= 1) {
                val d = dist(p, w)
                if (d <= 1) {
                    val s = score(w)
                    if (d < bd || (d == bd && s > bs)) {
                        best = w
                        bd = d
                        bs = s
                    }
                }
            }
        }
        return best
    }

    private fun dist(a: String, b: String): Int {
        val d = Array(a.length + 1) { IntArray(b.length + 1) }
        for (i in 0..a.length) d[i][0] = i
        for (j in 0..b.length) d[0][j] = j
        for (i in 1..a.length) {
            for (j in 1..b.length) {
                val c = if (a[i - 1] == b[j - 1]) 0 else 1
                d[i][j] = minOf(d[i - 1][j] + 1, d[i][j - 1] + 1, d[i - 1][j - 1] + c)
                if (i > 1 && j > 1 && a[i - 1] == b[j - 2] && a[i - 2] == b[j - 1]) {
                    d[i][j] = minOf(d[i][j], d[i - 2][j - 2] + 1)
                }
            }
        }
        return d[a.length][b.length]
    }
}
