package com.genkey.keyboard

class KTheme(val name: String, val bg: Int, val keys: List<Int>)

object Themes {
    val list = listOf(
        KTheme("Mondrian", 0xFF000000.toInt(), listOf(0xFFE3B33A.toInt(), 0xFF2A5288.toInt(), 0xFF1C1D21.toInt(), 0xFFEAE7E2.toInt(), 0xFFC23333.toInt())),
        KTheme("Dark", 0xFF000000.toInt(), listOf(0xFF3A3A3F.toInt(), 0xFF2E2E33.toInt(), 0xFF232327.toInt(), 0xFF45454B.toInt(), 0xFF5A2E2E.toInt())),
        KTheme("Light", 0xFFE8E8EC.toInt(), listOf(0xFFFFFFFF.toInt(), 0xFFD9DCE3.toInt(), 0xFFF4F4F6.toInt(), 0xFFC9CEDA.toInt(), 0xFFAEB8D0.toInt())),
        KTheme("Neon", 0xFF0A0A12.toInt(), listOf(0xFF00F5D4.toInt(), 0xFF9B5DE5.toInt(), 0xFF15151F.toInt(), 0xFFF15BB5.toInt(), 0xFFFEE440.toInt())),
        KTheme("Gen-Z", 0xFF14101F.toInt(), listOf(0xFFFF6B6B.toInt(), 0xFFFFD93D.toInt(), 0xFF2D1B3D.toInt(), 0xFF6BCB77.toInt(), 0xFF4D96FF.toInt())),
        KTheme("PC Classic", 0xFF2B2B2B.toInt(), listOf(0xFFC9C5BA.toInt(), 0xFFB8B3A6.toInt(), 0xFF3C3C3C.toInt(), 0xFFDAD6CB.toInt(), 0xFFA83232.toInt())),
        KTheme("Ultra Black", 0xFF000000.toInt(), listOf(0xFF101010.toInt(), 0xFF181818.toInt(), 0xFF050505.toInt(), 0xFF242424.toInt(), 0xFF3A1010.toInt())),
        KTheme("Aurora", 0xFF081522.toInt(), listOf(0xFF20C997.toInt(), 0xFF536DFE.toInt(), 0xFF142A40.toInt(), 0xFFB2F7EF.toInt(), 0xFFFF6B9D.toInt())),

        // European football club-inspired colour themes
        KTheme("FC Barcelona", 0xFF160B2D.toInt(), listOf(0xFF004D98.toInt(), 0xFFA50044.toInt(), 0xFF1A1A1A.toInt(), 0xFFDBA111.toInt(), 0xFF6CABDD.toInt())),
        KTheme("Real Madrid", 0xFF101A35.toInt(), listOf(0xFFF5F5F5.toInt(), 0xFFCAD4E5.toInt(), 0xFF1B2A55.toInt(), 0xFFD4AF37.toInt(), 0xFF8FAADC.toInt())),
        KTheme("Manchester United", 0xFF210909.toInt(), listOf(0xFFDA291C.toInt(), 0xFF1A1A1A.toInt(), 0xFFFBE122.toInt(), 0xFF8B0000.toInt(), 0xFFE6E6E6.toInt())),
        KTheme("Liverpool", 0xFF260707.toInt(), listOf(0xFFC8102E.toInt(), 0xFF8B001D.toInt(), 0xFFF5F5F5.toInt(), 0xFF1B1B1B.toInt(), 0xFF00A398.toInt())),
        KTheme("Arsenal", 0xFF1C0B18.toInt(), listOf(0xFFDB0007.toInt(), 0xFFEF0107.toInt(), 0xFF063672.toInt(), 0xFFF5F5F5.toInt(), 0xFFD4AF37.toInt())),
        KTheme("Manchester City", 0xFF071C2C.toInt(), listOf(0xFF6CABDD.toInt(), 0xFF1C2C5B.toInt(), 0xFFFFFFFF.toInt(), 0xFF98C5E9.toInt(), 0xFF202A44.toInt())),
        KTheme("Bayern Munich", 0xFF260707.toInt(), listOf(0xFFDC052D.toInt(), 0xFF0066B2.toInt(), 0xFFFFFFFF.toInt(), 0xFF9E0B22.toInt(), 0xFF1A1A1A.toInt())),
        KTheme("Paris Saint-Germain", 0xFF07152B.toInt(), listOf(0xFF004170.toInt(), 0xFFDA291C.toInt(), 0xFFFFFFFF.toInt(), 0xFF172B4D.toInt(), 0xFF6CABDD.toInt())),
        KTheme("Borussia Dortmund", 0xFF171717.toInt(), listOf(0xFFFDE100.toInt(), 0xFF1A1A1A.toInt(), 0xFFFFE94A.toInt(), 0xFFFFFFFF.toInt(), 0xFF3B3B3B.toInt())),
        KTheme("Inter Milan", 0xFF071323.toInt(), listOf(0xFF0068A8.toInt(), 0xFF000000.toInt(), 0xFF00AEEF.toInt(), 0xFFFFFFFF.toInt(), 0xFF1B3155.toInt())),
        KTheme("AC Milan", 0xFF180909.toInt(), listOf(0xFFFB090B.toInt(), 0xFF111111.toInt(), 0xFFFFFFFF.toInt(), 0xFF8B0000.toInt(), 0xFF555555.toInt())),
        KTheme("Juventus", 0xFF101010.toInt(), listOf(0xFFFFFFFF.toInt(), 0xFF1B1B1B.toInt(), 0xFFDADADA.toInt(), 0xFF777777.toInt(), 0xFFF5F5F5.toInt()))
    )

    fun get(i: Int): KTheme = list[if (i in list.indices) i else 0]

    fun textOn(c: Int): Int {
        val r = (c shr 16) and 255
        val g = (c shr 8) and 255
        val b = c and 255
        val lum = 0.299 * r + 0.587 * g + 0.114 * b
        return if (lum > 150) 0xFF000000.toInt() else 0xFFFFFFFF.toInt()
    }
}
