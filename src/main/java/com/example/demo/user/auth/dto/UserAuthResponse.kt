package com.example.demo.user.auth.dto

import java.time.Instant

@JvmRecord
data class UserAuthResponse(
    val id: String,
    val username: String,
    val nickname: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
    val accessToken: String,
    val refreshToken: String,
)
