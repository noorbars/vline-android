package com.vpnclient.app.ui.components

object PersianDigits {
    private val PERSIAN_NUMBERS = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')

    fun convert(input: String): String {
        val out = StringBuilder()
        for (c in input) {
            if (c in '0'..'9') {
                out.append(PERSIAN_NUMBERS[c - '0'])
            } else {
                out.append(c)
            }
        }
        return out.toString()
    }

    fun convert(number: Long): String {
        return convert(number.toString())
    }

    fun convert(number: Int): String {
        return convert(number.toString())
    }
}
