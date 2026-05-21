package com.example.demo.user.auth.dto

@JvmRecord
data class TokenPairResponse(
    val accessToken: String,
    val refreshToken: String,
)
