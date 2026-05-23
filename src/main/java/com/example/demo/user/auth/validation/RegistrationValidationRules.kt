package com.example.demo.user.auth.validation

object RegistrationValidationRules {
    const val USERNAME_MIN_LENGTH: Int = 8
    const val PASSWORD_MIN_LENGTH: Int = 8
    const val USERNAME_ALLOWED_PATTERN: String = "^[A-Za-z0-9_]+$"

    fun containsWhitespace(value: String): Boolean = value.any(Char::isWhitespace)
}
