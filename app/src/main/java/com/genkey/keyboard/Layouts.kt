
fun GenKeyService.buildSymbols() {
    val rows = listOf(
        "1234567890",
        "@#$%&*+-=",
        "()[]{}<>_",
        "/\\:;'\"?!~"
    )

    for ((ri, chars) in rows.withIndex()) {
        val r = row(52)
        chars.forEachIndexed { i, ch ->
            val s = ch.toString()
            r.addView(key(s, palette[(i + ri) % 5], 1f, size = 19f) {
                commit(s)
            })
        }
        root.addView(r)
    }

    val bottom = row(48)
    bottom.addView(key("ABC", RED, 1.2f, size = 14f) {
        layer = 0
        render()
    })
    bottom.addView(key("😊", BLUE, 0.9f) {
        layer = 2
        render()
    })
    bottom.addView(key("🎤", YELLOW, 0.9f) { voice() })
    bottom.addView(spaceKey(2.4f))
    bottom.addView(backspaceKey())
    bottom.addView(key("⏎", RED, 1.1f) { enter() })
    root.addView(bottom)
}
