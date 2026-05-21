package com.example.demo.common

object Strings {
    @JvmStatic
    fun trimToNull(value: String?): String? {
        if (value == null) {
            return null
        }
        return value.trim { it <= ' ' }.ifEmpty { null }
    }
}
