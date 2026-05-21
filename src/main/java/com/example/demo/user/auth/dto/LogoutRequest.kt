package com.example.demo.user.auth.dto

@JvmRecord
data class LogoutRequest(
    val refreshToken: String?,
)
