package com.example.demo.user.auth.dto

@JvmRecord
data class RefreshTokenRequest(
    val refreshToken: String?,
)
