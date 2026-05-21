package com.example.demo.user.auth.validation

object RegistrationValidationRules {
    const val USERNAME_MIN_LENGTH: Int = 8
    const val PASSWORD_MIN_LENGTH: Int = 8

    const val USERNAME_TOO_SHORT_MESSAGE: String = "Username must be at least $USERNAME_MIN_LENGTH characters."
    const val PASSWORD_TOO_SHORT_MESSAGE: String = "Password must be at least $PASSWORD_MIN_LENGTH characters."
}
