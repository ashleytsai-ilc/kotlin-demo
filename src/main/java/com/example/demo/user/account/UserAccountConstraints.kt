package com.example.demo.user.account

object UserAccountConstraints {
    const val USERNAME_MAX_LENGTH: Int = 15
    const val NICKNAME_MAX_LENGTH: Int = 30

    const val USERNAME_TOO_LONG_MESSAGE: String = "Username must be at most $USERNAME_MAX_LENGTH characters."
    const val NICKNAME_TOO_LONG_MESSAGE: String = "Nickname must be at most $NICKNAME_MAX_LENGTH characters."
}
