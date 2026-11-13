
package com.genkey.keyboard

class KTheme(val name: String, val bg: Int, val keys: List<Int>)

object Themes {
    val list = listOf(
        KTheme(
            "Mondrian",
            0xFF000000.toInt(),
            listOf(0xFFE3B33A.toInt(), 0xFF2A5288.toInt(), 0xFF1C1D21.toInt(), 0xFFEAE7E2.toInt(), 0xFFC23333.toInt())
        ),
        KTheme(
            "Dark",
            0xFF000000.toInt(),
            listOf(0xFF3A3A3F.toInt(), 0xFF2E2E33.toInt(), 0xFF232327.toInt(), 0xFF45454B.toInt(), 0xFF5A2E2E.toInt())
        ),
        KTheme(
            "Light",
            0xFFE8E8EC.toInt(),
            listOf(0xFFFFFFFF.toInt(), 0xFFD9DCE3.toInt(), 0xFFF4F4F6.toInt(), 0xFFC9CEDA.toInt(), 0xFFAEB8D0.toInt())
        ),
        KTheme(
            "Neon",
            0xFF0A0A12.toInt(),
            listOf(0xFF00F5D4.toInt(), 0xFF9B5DE5.toInt(), 0xFF15151F.toInt(), 0xFFF15BB5.toInt(), 0xFFFEE440.toInt())
        ),
        KTheme(
            "Gen-Z",
            0xFF14101F.toInt(),
            listOf(0xFFFF6B6B.toInt(), 0xFFFFD93D.toInt(), 0xFF2D1B3D.toInt(), 0xFF6BCB77.toInt(), 0xFF4D96FF.toInt())
        ),
        KTheme(
            "PC Classic",
            0xFF2B2B2B.toInt(),
            listOf(0xFFC9C5BA.toInt(), 0xFFB8B3A6.toInt(), 0xFF3C3C3C.toInt(), 0xFFDAD6CB.toInt(), 0xFFA83232.toInt())
        ),
        KTheme(
            "Ultra Black",
            0xFF000000.toInt(),
            listOf(0xFF101010.toInt(), 0xFF181818.toInt(), 0xFF050505.toInt(), 0xFF242424.toInt(), 0xFF3A1010.toInt())
        ),
        KTheme(
            "Aurora",
            0xFF081522.toInt(),
            listOf(0xFF20C997.toInt(), 0xFF536DFE.toInt(), 0xFF142A40.toInt(), 0xFFB2F7EF.toInt(), 0xFFFF6B9D.toInt())
        )
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
